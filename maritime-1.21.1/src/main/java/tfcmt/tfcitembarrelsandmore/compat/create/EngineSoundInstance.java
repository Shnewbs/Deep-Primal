package tfcmt.tfcitembarrelsandmore.compat.create;

import java.lang.ref.WeakReference;
import net.dries007.tfc.util.rotation.Rotation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import tfcmt.tfcitembarrelsandmore.common.TIBAMConfig;
import tfcmt.tfcitembarrelsandmore.common.TIBAMSounds;

public class EngineSoundInstance
extends AbstractTickableSoundInstance {
    private static final float PITCH_MIN = 0.55f;
    private static final float PITCH_MAX = 1.85f;
    private static final float SPEED_MAX = 2.0f;
    private static final float VOLUME_INIT = 1.0f;
    private static final int FADE_TICKS = 10;
    private static final int OCCLUSION_INTERVAL = 4;
    private static final float OCCLUSION_PER_BLOCK = 0.45f;
    private static final int OCCLUSION_MAX_BLOCKS = 4;
    private static final float DIST_REFERENCE = 3.0f;
    private static final float DIST_MAX = 24.0f;
    private final WeakReference<SteamEngineAccessor> beRef;
    private final WeakReference<Level> levelRef;
    private final BlockPos enginePos;
    private final BooleanProperty runningProp;
    private int fadeTick = 10;
    private boolean stopping = false;
    private float prevAngle = Float.NaN;
    private int occlusionCountdown = 0;
    private float occlusionMultiplier = 1.0f;
    private float distanceMultiplier = 1.0f;

    public EngineSoundInstance(SteamEngineAccessor be, Level level, BlockPos pos, BooleanProperty runningProp) {
        super((SoundEvent)TIBAMSounds.ENGINE_STEAM_HISS.get(), SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
        this.beRef = new WeakReference<SteamEngineAccessor>(be);
        this.levelRef = new WeakReference<Level>(level);
        this.enginePos = pos;
        this.runningProp = runningProp;
        this.x = (double)pos.getX() + 0.5;
        this.y = (double)pos.getY() + 0.5;
        this.z = (double)pos.getZ() + 0.5;
        this.volume = 1.0f;
        this.pitch = 0.55f;
        this.looping = true;
        this.delay = 0;
        this.relative = false;
        this.attenuation = SoundInstance.Attenuation.NONE;
    }

    public void tick() {
        SteamEngineAccessor be = (SteamEngineAccessor)this.beRef.get();
        Level level = (Level)this.levelRef.get();
        if (be == null || be.isRemoved() || level == null) {
            this.stop();
            return;
        }
        BlockState currentState = level.getBlockState(this.enginePos);
        if (!currentState.getProperties().contains(this.runningProp)) {
            this.stop();
            return;
        }
        boolean running = (Boolean)currentState.getValue((Property)this.runningProp);
        if (!running && !this.stopping) {
            this.stopping = true;
        }
        if (this.stopping) {
            this.fadeTick = Math.max(0, this.fadeTick - 1);
            if (this.fadeTick == 0) {
                this.stop();
                return;
            }
        } else {
            this.fadeTick = Math.min(10, this.fadeTick + 1);
        }
        if (--this.occlusionCountdown <= 0) {
            this.occlusionCountdown = 4;
            this.occlusionMultiplier = this.computeOcclusionMultiplier(level);
            this.distanceMultiplier = this.computeDistanceMultiplier();
        }
        float speed = 0.0f;
        Rotation rot = be.getSourceRotation();
        if (rot != null) {
            float cur = Rotation.angle((Rotation)rot, (float)1.0f);
            if (!Float.isNaN(this.prevAngle)) {
                float delta = cur - this.prevAngle;
                if (delta > (float)Math.PI) {
                    delta -= (float)Math.PI * 2;
                }
                if (delta < (float)(-Math.PI)) {
                    delta += (float)Math.PI * 2;
                }
                speed = Math.abs(delta);
            }
            this.prevAngle = cur;
        }
        float configVolume = ((Double)TIBAMConfig.COMMON.engineSoundVolume.get()).floatValue();
        float speedNorm = Math.min(1.0f, speed / 2.0f);
        this.pitch = 0.55f + 1.3f * speedNorm;
        this.volume = configVolume * ((float)this.fadeTick / 10.0f) * this.occlusionMultiplier * this.distanceMultiplier;
    }

    private float computeOcclusionMultiplier(Level level) {
        BlockHitResult hit;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return 1.0f;
        }
        Vec3 listenerPos = mc.player.getEyePosition(1.0f);
        Vec3 engineCentre = Vec3.atCenterOf((Vec3i)this.enginePos);
        float multiplier = 1.0f;
        Vec3 rayStart = listenerPos;
        for (int i = 0; i < 4 && (hit = level.clip(new ClipContext(rayStart, engineCentre, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, (Entity)mc.player))).getType() != HitResult.Type.MISS; ++i) {
            multiplier *= 0.45f;
            Vec3 dir = engineCentre.subtract(rayStart).normalize();
            rayStart = hit.getLocation().add(dir.scale(0.1));
            if (rayStart.distanceToSqr(listenerPos) >= engineCentre.distanceToSqr(listenerPos)) break;
        }
        return multiplier;
    }

    private float computeDistanceMultiplier() {
        Vec3 engineCentre;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return 1.0f;
        }
        Vec3 listenerPos = mc.player.getEyePosition(1.0f);
        float dist = (float)listenerPos.distanceTo(engineCentre = Vec3.atCenterOf((Vec3i)this.enginePos));
        if (dist <= 3.0f) {
            return 1.0f;
        }
        if (dist >= 24.0f) {
            return 0.0f;
        }
        float t = (dist - 3.0f) / 21.0f;
        return 1.0f - t * t;
    }

    public static void startFor(SteamEngineAccessor be, Level level, BlockPos pos, BooleanProperty runningProp) {
        EngineSoundInstance instance = new EngineSoundInstance(be, level, pos, runningProp);
        Minecraft mc = Minecraft.getInstance();
        mc.getSoundManager().play((SoundInstance)instance);
    }

    public static interface SteamEngineAccessor {
        public boolean isRemoved();

        public Rotation getSourceRotation();
    }
}

