package com.shnewbs.deepprimal.validation;

import com.shnewbs.deepprimal.core.WaterAccount;
import com.shnewbs.deepprimal.engine.EngineChunkGenerator;
import com.shnewbs.deepprimal.engine.EngineFluid;
import com.shnewbs.deepprimal.engine.EngineRegistry;
import com.shnewbs.deepprimal.engine.HydrologyConfig;
import com.shnewbs.deepprimal.engine.HydrologyData;
import com.shnewbs.deepprimal.engine.HydrologyRuntime;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Two local game processes share a disposable seed-20261020 world and an empty receipt directory. */
@Mod(value = "deepprimal", dist = Dist.CLIENT)
public final class MultiplayerWaterChecks {
  private static final String ROLE = System.getenv("DEEPPRIMAL_MULTIPLAYER_WATER");
  private static final BlockPos[] RIVERS = {new BlockPos(822, 172, 1089), new BlockPos(50, 187, 1427)};
  private static final int TICKS_PER_VISIT = 1200;
  private static final int ROUNDS = 2;
  private final boolean host;
  private final Path dir;
  private boolean listening, filled, drained, sawFull, completed, clientFinished, clientReady;
  private int round, clientRound, clientTicks, ticks, joinedAt, began;
  private final long[] lastStep = {Long.MIN_VALUE, Long.MIN_VALUE};
  private final int[] steps = new int[2];

  static boolean enabled() { return "host".equals(ROLE) || "guest".equals(ROLE); }

  public MultiplayerWaterChecks() {
    host = "host".equals(ROLE);
    if (enabled()) {
      dir = Path.of(System.getenv("DEEPPRIMAL_MULTIPLAYER_RECORD"));
      NeoForge.EVENT_BUS.register(this);
    } else dir = null;
  }

  private static void require(boolean condition, String message) {
    if (!condition) throw new AssertionError(message);
  }

  private void mark(String name) throws IOException { Files.createFile(dir.resolve(name)); }
  private boolean marked(String name) { return Files.isRegularFile(dir.resolve(name)); }
  private static BlockPos station(int site) { return RIVERS[site].above(9); }
  private static WaterAccount.Parcel parcel(int round, int site) {
    return new WaterAccount.Parcel(4096, 15_003L + 17 * site + round, 851L + 13 * round + site);
  }

  private static boolean riverVisible(Level level, BlockPos center) {
    for (int dz = -4; dz <= 4; dz += 2) for (int dx = -4; dx <= 4; dx += 2) {
      var sample = center.offset(dx, 0, dz);
      if (!level.hasChunkAt(sample)) continue;
      for (int dy = -4; dy <= 3; dy++)
        if (level.getBlockState(sample.above(dy)).is(EngineRegistry.WATER_BLOCK.get())) return true;
    }
    return false;
  }

  private static void move(ServerPlayer player, int site) {
    var p = station(site);
    player.setGameMode(GameType.CREATIVE);
    player.getAbilities().flying = true;
    player.onUpdateAbilities();
    player.teleportTo(p.getX() + .5, p.getY() + 2.05, p.getZ() + .5);
  }

  private boolean both(String state) {
    return marked("host-" + round + "-" + state) && marked("guest-" + round + "-" + state);
  }

  @SubscribeEvent public void serverTick(ServerTickEvent.Post event) throws IOException {
    if (!host || completed) return;
    var server = event.getServer();
    var level = server.overworld();
    var players = server.getPlayerList();
    var owner = players.getPlayerByName("BasinHost");
    if (owner == null) return;
    if (!listening) {
      require(server.isSingleplayer() && !server.isPublished() && level.getSeed() == 20261020L, "Disposable integrated river world required");
      // Only the opt-in fixture opens a loopback listener for the second development client.
      server.setUsesAuthentication(false);
      server.getConnection().startTcpServerListener(InetAddress.getByName("127.0.0.1"),
          Integer.parseInt(System.getenv("DEEPPRIMAL_MULTIPLAYER_PORT")));
      HydrologyConfig.ENABLED.set(true);
      mark("listening");
      listening = true;
    }
    require(++ticks < 9600, "Multiplayer fixture timed out at visit " + round);
    var guest = players.getPlayerByName("BasinGuest");
    if (guest == null) {
      require(joinedAt == 0, "Guest disconnected before the last receipt");
      return;
    }
    // Wait for both clients to finish their own first spawn before moving either player.
    if (!marked("host-joined") || !marked("guest-joined")) return;
    if (joinedAt == 0) joinedAt = ticks;
    if (began == 0) {
      move(owner, round);
      move(guest, 1 - round);
      began = ticks;
    }
    var data = HydrologyData.get(level);
    for (int i = 0; i < RIVERS.length; i++) {
      var p = RIVERS[i];
      if (level.getChunkSource().getChunkNow(p.getX() >> 4, p.getZ() >> 4) == null) return;
      var aq = data.aquifers.get(HydrologyData.key(p.getX(), p.getZ()));
      if (aq != null && aq.lastTick() > lastStep[i]) {
        if (lastStep[i] != Long.MIN_VALUE) steps[i]++;
        lastStep[i] = aq.lastTick();
      }
    }
    if (!filled) {
      var terrain = ((EngineChunkGenerator)level.getChunkSource().getGenerator()).geography();
      for (int i = 0; i < RIVERS.length; i++) {
        var river = RIVERS[i];
        var sample = terrain.column(river.getX(), river.getZ());
        require(sample.wet() && sample.speed() > .05 && riverVisible(level, river), "Generated flowing river at " + river);
        var cell = station(i);
        if (round == 0) {
          for (var side : Direction.values()) require(HydrologyRuntime.write(level, cell.relative(side),
              Blocks.BEDROCK.defaultBlockState()), "Station enclosure at " + cell);
          require(HydrologyRuntime.write(level, cell, Blocks.AIR.defaultBlockState()), "Clear measured station");
        }
        require(HydrologyRuntime.place(level, cell, parcel(round, i)), "Measured placement at " + cell);
      }
      // Player tickets delivered the chunks. Both observation positions now have a solid floor.
      move(owner, round);
      move(guest, 1 - round);
      mark("fill-" + round);
      filled = true;
    }
    if (!drained && both("full") && ticks - began >= TICKS_PER_VISIT) {
      for (int i = 0; i < RIVERS.length; i++)
        require(parcel(round, i).equals(HydrologyRuntime.pickup(level, station(i))), "Exact water, salt and sediment on pickup");
      drained = true;
      mark("drain-" + round);
    }
    if (!drained || !both("empty")) return;
    require(steps[0] >= 30 && steps[1] >= 30, "Both river regions must keep advancing: " + steps[0] + ", " + steps[1]);
    System.out.println("MULTIPLAYER_WATER_VISIT round=" + round + " ticks=" + (ticks - began)
        + " aquiferSteps=" + steps[0] + "," + steps[1] + " synchronizedCells=2");
    if (++round == ROUNDS) {
      System.out.println("MULTIPLAYER_WATER_PASS clients=" + players.getPlayerCount() + " ticks=" + (ticks - joinedAt)
          + " riverRegions=2 playerSwaps=1 measuredParcels=4 exactChemistry=true");
      mark("complete");
      completed = true;
    } else {
      began = 0;
      filled = false;
      drained = false;
      steps[0] = steps[1] = 0;
    }
  }

  @SubscribeEvent public void clientTick(ClientTickEvent.Post event) throws IOException {
    var mc = Minecraft.getInstance();
    if (mc.level == null || mc.player == null) return;
    if (!clientReady) {
      if (mc.screen != null) return;
      mark(ROLE + "-joined");
      clientReady = true;
    }
    if (clientRound == ROUNDS) {
      if (marked("complete")) {
        if (!clientFinished) {
          System.out.println("MULTIPLAYER_WATER_CLIENT_PASS role=" + ROLE + " visits=" + clientRound);
          mark(ROLE + "-complete");
          clientFinished = true;
        }
        if (host && !marked("guest-complete")) return;
        if (host) mc.disconnectWithSavingScreen();
        mc.stop();
      }
      return;
    }
    int site = host ? clientRound : 1 - clientRound;
    var river = RIVERS[site];
    var p = station(site);
    if (++clientTicks % 240 == 0 && marked("fill-" + clientRound) && !sawFull) {
      System.out.println("MULTIPLAYER_WATER_WAIT role=" + ROLE + " player=" + mc.player.blockPosition()
          + " expected=" + p + " loaded=" + mc.level.hasChunkAt(p)
          + " station=" + (mc.level.hasChunkAt(p) ? mc.level.getBlockState(p) : "pending")
          + " river=" + riverVisible(mc.level, river));
    }
    if (mc.player.position().distanceToSqr(p.getX() + .5, p.getY() + 2.05, p.getZ() + .5) > 25
        || !mc.level.hasChunkAt(p)) return;
    var state = mc.level.getBlockState(p);
    String prefix = (host ? "host-" : "guest-") + clientRound;
    if (!sawFull && marked("fill-" + clientRound) && state.is(EngineRegistry.WATER_BLOCK.get())
        && state.getValue(EngineFluid.UNITS) == 16 && riverVisible(mc.level, river)) {
      mark(prefix + "-full");
      sawFull = true;
    } else if (sawFull && marked("drain-" + clientRound) && state.isAir()) {
      mark(prefix + "-empty");
      clientRound++;
      sawFull = false;
    }
  }
}
