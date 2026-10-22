package tfcmt.tfcitembarrelsandmore.common;

import net.neoforged.neoforge.common.ModConfigSpec;

public class TIBAMConfig {
    public static final ModConfigSpec COMMON_SPEC;
    public static final Common COMMON;
    public static Common SERVER;
    public static ModConfigSpec SERVER_SPEC;

    public static float baseSpeedRadPerTick() {
        double rpm = (Double)TIBAMConfig.COMMON.engineBaseSpeedRPM.get();
        return (float)(rpm * Math.PI / 600.0);
    }

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        COMMON = new Common(builder);
        COMMON_SPEC = builder.build();
        SERVER = null;
        SERVER_SPEC = null;
        SERVER = COMMON;
        SERVER_SPEC = COMMON_SPEC;
    }

    public static class Common {
        public final ModConfigSpec.IntValue saltConsumptionInterval;
        public final ModConfigSpec.IntValue honeyConsumptionInterval;
        public final ModConfigSpec.DoubleValue boilerDestructionTemp;
        public final ModConfigSpec.DoubleValue copperRodPopTemp;
        public final ModConfigSpec.BooleanValue boilerExplosionOnOverheat;
        public final ModConfigSpec.DoubleValue boilerExplosionRadius;
        public final ModConfigSpec.BooleanValue boilerExplosionBlockDamage;
        public final ModConfigSpec.DoubleValue boilerHeatLossPerTick;
        public final ModConfigSpec.DoubleValue boilerHeatGainFactor;
        public final ModConfigSpec.DoubleValue boilerBackPressureExponent;
        public final ModConfigSpec.IntValue boilerSmokeMultiplier;
        public final ModConfigSpec.IntValue engineWaterCapacity;
        public final ModConfigSpec.DoubleValue steamEquilibriumExponent;
        public final ModConfigSpec.DoubleValue steamEquilibriumCeiling;
        public final ModConfigSpec.IntValue tankWaterCapacity;
        public final ModConfigSpec.DoubleValue tankTempApproachRate;
        public final ModConfigSpec.DoubleValue tankSteamApproachRate;
        public final ModConfigSpec.DoubleValue tankRupturePsiBuffer;
        public final ModConfigSpec.DoubleValue tankSteamDrainAtWorking;
        public final ModConfigSpec.DoubleValue tankSteamDrainAtMax;
        public final ModConfigSpec.DoubleValue tankExplosionRadius;
        public final ModConfigSpec.BooleanValue tankExplosionBlockDamage;
        public final ModConfigSpec.IntValue engineWaterPerSteam;
        public final ModConfigSpec.IntValue engineWaterTickRate;
        public final ModConfigSpec.IntValue engineMaxSteam;
        public final ModConfigSpec.DoubleValue engineMinSteamTemp;
        public final ModConfigSpec.DoubleValue engineTempApproachRate;
        public final ModConfigSpec.DoubleValue engineSteamApproachRate;
        public final ModConfigSpec.DoubleValue engineSteamDrainAtWorking;
        public final ModConfigSpec.DoubleValue engineSteamDrainAtMax;
        public final ModConfigSpec.DoubleValue engineMaxSpeedMultiplier;
        public final ModConfigSpec.DoubleValue bleederVentDropMinPsi;
        public final ModConfigSpec.DoubleValue bleederVentDropMaxPsi;
        public final ModConfigSpec.IntValue tollRateLight;
        public final ModConfigSpec.IntValue tollRateMedium;
        public final ModConfigSpec.IntValue tollRateHeavy;
        public final ModConfigSpec.DoubleValue engineBaseSpeedRPM;
        public final ModConfigSpec.DoubleValue engineSoundVolume;
        public final ModConfigSpec.DoubleValue telegraphOutputStressCapacity;
        public final ModConfigSpec.DoubleValue pressMaxSpeedRPM;
        public final ModConfigSpec.IntValue compressorMaxAir;
        public final ModConfigSpec.IntValue compressorAirPerPulse;
        public final ModConfigSpec.IntValue compressorPulseRate;
        public final ModConfigSpec.DoubleValue compressorMinRpm;
        public final ModConfigSpec.DoubleValue compressorRpmCeiling;
        public final ModConfigSpec.DoubleValue compressorExplosionRadius;
        public final ModConfigSpec.BooleanValue compressorExplosionBlockDamage;
        public final ModConfigSpec.IntValue compressorHoseRangePerItem;
        public final ModConfigSpec.IntValue compressorHoseMaxRange;
        public final ModConfigSpec.IntValue diveSuitAirPerSecond;
        public final ModConfigSpec.DoubleValue propulsionForcePerTon;
        public final ModConfigSpec.DoubleValue brassRatedTonnage;
        public final ModConfigSpec.DoubleValue brassPeakRpm;
        public final ModConfigSpec.DoubleValue brassMaxRpm;
        public final ModConfigSpec.DoubleValue copperRatedTonnage;
        public final ModConfigSpec.DoubleValue copperMaxRpm;
        public final ModConfigSpec.DoubleValue maxShipSpeedMps;
        public final ModConfigSpec.DoubleValue paddleRatedTonnage;
        public final ModConfigSpec.DoubleValue paddleMaxRpm;
        public final ModConfigSpec.DoubleValue speedTaperFraction;
        public final ModConfigSpec.DoubleValue propulsionSpindownSeconds;
        public final ModConfigSpec.DoubleValue paddleSpinupSeconds;
        public final ModConfigSpec.DoubleValue paddleSpinupWithPropellersSeconds;
        public final ModConfigSpec.BooleanValue icebreakerEnabled;
        public final ModConfigSpec.IntValue icebreakerScanIntervalTicks;
        public final ModConfigSpec.DoubleValue icebreakerPlayerSearchRadius;
        public final ModConfigSpec.IntValue icebreakerBreakRadius;
        public final ModConfigSpec.BooleanValue windBridgeEnabled;
        public final ModConfigSpec.IntValue windBridgeIntervalTicks;
        public final ModConfigSpec.DoubleValue windBridgeSpeedScale;
        public final ModConfigSpec.DoubleValue windBridgePlayerSearchRadius;
        public final ModConfigSpec.IntValue mastMaxHeight;

        public Common(ModConfigSpec.Builder builder) {
            builder.comment("Settings for item barrel preservation (salting, honey).").push("preservation");
            this.saltConsumptionInterval = builder.comment(new String[]{"How many ticks between each salt top-up when preserving meat.", "20 ticks = 1 second. 24000 ticks = 1 in-game day (default).", "Lower this to make salt run out faster."}).defineInRange("saltConsumptionInterval", 24000, 1, Integer.MAX_VALUE);
            this.honeyConsumptionInterval = builder.comment(new String[]{"How many ticks between each Firmalife honey top-up when preserving meat.", "20 ticks = 1 second. 24000 ticks = 1 in-game day (default).", "Lower this to make honey run out faster."}).defineInRange("honeyConsumptionInterval", 24000, 1, Integer.MAX_VALUE);
            builder.pop();
            builder.comment("Settings for the cast-iron steam boiler.").push("boiler");
            this.boilerDestructionTemp = builder.comment(new String[]{"The temperature (in Celsius) at which the boiler explodes or breaks.", "Cast iron melts around 1535 C. Default is 1500 C \u2014 hot enough to", "run the engine hard, but you still need to watch the gauge.", "Raise this for a more forgiving experience. Lower it for danger."}).defineInRange("boilerDestructionTemp", 1500.0, 100.0, 2000.0);
            this.copperRodPopTemp = builder.comment(new String[]{"The temperature (in Celsius) at which the copper connecting rod", "melts and snaps, breaking the boiler-to-engine link.", "This is your first warning before the boiler destroys itself.", "Copper melts around 1085 C. Default is 900 C.", "Set lower to make the rod pop sooner; higher to give more headroom."}).defineInRange("copperRodPopTemp", 1000.0, 50.0, 1534.0);
            this.boilerExplosionOnOverheat = builder.comment(new String[]{"What happens when the boiler overheats past boilerDestructionTemp?", "  true  = small explosion (no block damage) with salvage drops.", "  false = the boiler quietly disappears with a sound cue.", "Default: true"}).define("boilerExplosionOnOverheat", true);
            this.boilerExplosionRadius = builder.comment(new String[]{"Explosion radius/strength for the boiler's overheat explosion.", "Only used when boilerExplosionOnOverheat is true.", "Default: 1.5"}).defineInRange("boilerExplosionRadius", 1.5, 0.0, 10.0);
            this.boilerExplosionBlockDamage = builder.comment(new String[]{"Whether the boiler's overheat explosion damages surrounding blocks.", "  true  = normal block-breaking explosion.", "  false = non-griefing explosion, no block damage (default).", "Only used when boilerExplosionOnOverheat is true.", "Default: false"}).define("boilerExplosionBlockDamage", false);
            this.tankExplosionRadius = builder.comment(new String[]{"Explosion radius/strength for the boiler tank's over-pressure", "explosion (triggered at PistonPressure.DANGER psi). The tank", "always explodes on over-pressure; there is no quiet-removal option.", "Default: 3.0"}).defineInRange("tankExplosionRadius", 3.0, 0.0, 10.0);
            this.tankExplosionBlockDamage = builder.comment(new String[]{"Whether the boiler tank's over-pressure explosion damages", "surrounding blocks. A high-pressure tank rupture is more dangerous", "than a simple boiler overheat, so this defaults to true.", "Default: true"}).define("tankExplosionBlockDamage", true);
            this.boilerHeatLossPerTick = builder.comment(new String[]{"How many degrees Celsius the boiler loses per tick when no fuel is burning.", "20 ticks = 1 second, so 0.2 per tick = 4 degrees per second.", "At that rate a boiler at 1000 C takes about 4 minutes to cool fully.", "Raise this number to make the boiler cool down faster.", "Default: 0.2"}).defineInRange("heatLossPerTick", 0.2, 0.0, 10.0);
            this.boilerHeatGainFactor = builder.comment(new String[]{"How quickly the boiler climbs toward its fuel's target temperature", "each tick while burning. Uses a smooth curve so it never jumps instantly.", "0.002 is a slow, realistic ramp (takes a few minutes to reach full heat).", "0.01 warms up roughly 5x faster. 0.05 is nearly instant.", "Default: 0.002"}).defineInRange("heatGainFactor", 0.002, 1.0E-4, 1.0);
            this.boilerBackPressureExponent = builder.comment(new String[]{"Controls the shape of the back-pressure curve on steam production.", "Formula: backPressureFactor = 1 - (psi/100)^exponent", "1.0 = linear drop-off across the full pressure range.", "2.0 = gentle convex curve (default). Production holds well through", "      WORKING tier and falls sharply only in HIGH and DANGER.", "3.0 = aggressive. WORKING is very stable; DANGER collapses fast.", "Raise to make working pressure feel more 'locked in' and DANGER", "more punishing. Lower toward 1.0 for a linear, predictable feel.", "Default: 2.0"}).defineInRange("backPressureExponent", 2.0, 0.5, 5.0);
            this.boilerSmokeMultiplier = builder.comment(new String[]{"Multiplies exhaust smoke particle count per tick.", "Applies to the boiler's own chimney smoke when no tank is on top,", "the boiler tank's campfire-style plume when one is on top, AND the", "Fume Hood output's plume when smoke is vented there instead \u2014", "one knob for exhaust density everywhere it can appear.", "1 = vanilla-campfire-like density. Default: 8"}).defineInRange("smokeMultiplier", 8, 1, 50);
            builder.pop();
            builder.comment("Settings for the single piston steam engine.").push("engine");
            builder.comment("--- Water tank ---");
            this.engineWaterCapacity = builder.comment(new String[]{"How much water the engine's internal tank holds, in millibuckets (mB).", "1000 mB = 1 bucket. Default 4000 mB = 4 buckets.", "NOTE: only takes effect when a new engine is placed.", "Existing engines keep their old tank size until broken and replaced."}).defineInRange("waterCapacity", 4000, 100, 64000);
            builder.comment("--- Steam Equilibrium Curve (shared by boiler tank + single piston engine) ---");
            this.steamEquilibriumExponent = builder.comment(new String[]{"Exponent on the equilibrium PSI curve:", "equilibriumPsi = 100 * clamp((boilerTemp - minSteamTemp) / (equilibriumCeiling - 100), 0, 1) ^ this", "Exponent > 1 gives an accelerating curve, matching real saturation-pressure physics.", "Default: 1.56"}).defineInRange("steamEquilibriumExponent", 1.56, 0.1, 10.0);
            this.steamEquilibriumCeiling = builder.comment(new String[]{"Boiler temperature (C) at which the equilibrium PSI curve reaches its ceiling.", "Default: 1500.0"}).defineInRange("steamEquilibriumCeiling", 1500.0, 101.0, 5000.0);
            builder.comment("--- Boiler Tank ---");
            this.tankWaterCapacity = builder.comment(new String[]{"Water capacity of the boiler tank in mB.", "Feeds up to 3 triple-expansion engines so needs more headroom", "than the single piston engine tank.", "Default: 12000 mB (12 buckets)."}).defineInRange("tankWaterCapacity", 12000, 1000, 256000);
            this.tankTempApproachRate = builder.comment(new String[]{"How fast the tank's effectiveBoilerTemp eases toward the real boiler", "temperature, once per second: effectiveBoilerTemp += (boilerTemp - this) * rate.", "Separate from tankSteamApproachRate on purpose \u2014 the iron heats up faster", "than steam can catch up to it.", "Default: 0.20"}).defineInRange("tankTempApproachRate", 0.2, 0.001, 1.0);
            this.tankSteamApproachRate = builder.comment(new String[]{"How fast actual steam quantity eases toward the equilibrium-implied quantity,", "computed from effectiveBoilerTemp. Recomputed once per second (heartbeat) but", "spread evenly across the following 20 ticks, not added in one lump.", "0.02 keeps a full coal burn's peak production ~43 mB/s, under the bleeder's", "default 60 mB/s vent rate. Steady-state PSI is unaffected by this value.", "Default: 0.02"}).defineInRange("tankSteamApproachRate", 0.02, 0.001, 1.0);
            this.tankRupturePsiBuffer = builder.comment(new String[]{"PSI added on top of PistonPressure.HIGH.getMin() for the boiler tank's rupture", "threshold: rupturePsi = HIGH.getMin() + this. Fixed offset, not randomized.", "Default: 6.0"}).defineInRange("tankRupturePsiBuffer", 6.0, 0.0, 100.0);
            this.tankSteamDrainAtWorking = builder.comment(new String[]{"Steam drained per tick PER linked triple-expansion engine at PSI 45 (WORKING pivot).", "Total drain = linkedEngineCount x drain(psi).", "Controls 2-engine equilibrium PSI (the below-45 region of the drain curve).", "Default 0.30 -> 2 engines equilibrate at PSI ~52 (WORKING tier),", "             -> 1 engine  equilibrates at PSI ~70 (HIGH tier).", "Lower this value to raise equilibria; raise it to lower them.", "See tankSteamDrainAtMax for the above-45 tuning knob."}).defineInRange("tankSteamDrainAtWorking", 0.3, 0.01, 1000.0);
            this.tankSteamDrainAtMax = builder.comment(new String[]{"Steam drained per tick PER engine at maximum PSI (100, DANGER).", "Must be greater than tankSteamDrainAtWorking.", "Drain curve: linear through (0,0), (45,drainAtWorking), (100,this).", "Controls 1-engine equilibrium PSI (the above-45 region of the drain curve).", "Default 0.72 -> 1 engine equilibrates at PSI ~70 (HIGH tier).", "Raise this value to lower the 1-engine equilibrium; lower it to raise PSI.", "See tankSteamDrainAtWorking for the below-45 tuning knob."}).defineInRange("tankSteamDrainAtMax", 0.72, 0.01, 10000.0);
            this.engineWaterPerSteam = builder.comment(new String[]{"How many mB of water are drawn from the tank per steam unit produced.", "Default: 2 mB per steam unit.", "Raise this to make the engine drink water faster.", "Water drain and steam production are independent \u2014 changing this does not affect steam output."}).defineInRange("waterPerSteam", 2, 1, 1000);
            this.engineWaterTickRate = builder.comment(new String[]{"How many ticks between each water drain event.", "Water drains on this fixed timer, fully independent of steam production.", "20 ticks = 1 second (default). mB consumed per second = waterDrainPerEvent / (waterTickRate/20).", "Set higher to make water last longer. Set to 1 to drain every tick.", "Default: 20"}).defineInRange("waterTickRate", 20, 1, 1200);
            builder.comment("--- Steam production ---");
            this.engineMaxSteam = builder.comment(new String[]{"The maximum number of steam units the engine can store.", "PSI displayed in the GUI = (current steam / this value) x 100.", "Larger values mean slower PSI build-up but more buffer before DANGER.", "Default: 3000"}).defineInRange("maxSteam", 3000, 100, 100000);
            this.engineMinSteamTemp = builder.comment(new String[]{"The boiler must reach this temperature (in Celsius) before the engine", "starts producing steam. Below this, nothing happens even with full water.", "100 C is the boiling point of water \u2014 a sensible minimum.", "Default: 100"}).defineInRange("minSteamTemp", 100.0, 1.0, 2000.0);
            this.engineTempApproachRate = builder.comment(new String[]{"How fast the single piston's effectiveBoilerTemp eases toward the real boiler", "temperature, once per second. Same mechanic as tankTempApproachRate but its", "own knob \u2014 the single piston is thin and direct-mounted, so it should read", "boiler heat noticeably faster than the cast-iron tank.", "Default: 0.35"}).defineInRange("engineTempApproachRate", 0.35, 0.001, 1.0);
            this.engineSteamApproachRate = builder.comment(new String[]{"How fast the single piston's actual steam quantity eases toward the", "equilibrium-implied quantity, once per second, spread across the following", "20 ticks. Same mechanic as tankSteamApproachRate but its own knob, tuned", "faster to match the single piston's twitchier feel \u2014 a full coal burn reaches", "the LOW/rupture boundary (~30 PSI) in roughly 6 seconds at this default.", "Default: 0.12"}).defineInRange("engineSteamApproachRate", 0.12, 0.001, 1.0);
            this.engineSteamDrainAtWorking = builder.comment(new String[]{"Steam consumed per tick at WORKING pressure (PSI 45, midpoint of the WORKING tier).", "This is your equilibrium tuning knob \u2014 match it to what the boiler produces", "per tick at normal operating temperature and the engine will hold near working", "pressure. Below working PSI the engine burns less; above it burns more.", "Production rate = steamPerPulse / 4 per tick at full boiler heat.", "Default steamPerPulse=2 -> 0.5 steam/tick produced, so default here is 0.5."}).defineInRange("steamDrainAtWorking", 0.5, 0.01, 1000.0);
            this.engineSteamDrainAtMax = builder.comment(new String[]{"Steam consumed per tick at maximum PSI (100, DANGER tier).", "Must be larger than steamDrainAtWorking to guarantee the engine is always", "net-negative above working pressure, pulling PSI back down automatically.", "Drain interpolates linearly between 0 at PSI 0 and this value at PSI 100,", "passing through steamDrainAtWorking at PSI 45.", "", "Equilibrium (steamPerPulse=2, drainAtWorking=0.5):", "  drain(psi) = drainAtWorking + t*(drainAtMax-drainAtWorking),", "  where t = (psi-45)/55  for psi > 45.", "  Default 1.6 -> 1 engine equilibrates at PSI ~70 (HIGH tier),", "              -> 2 engines equilibrate at PSI ~45 (WORKING tier).", "  Raise drainAtMax to lower equilibrium PSI; lower it to raise PSI."}).defineInRange("steamDrainAtMax", 1.6, 0.01, 10000.0);
            builder.comment("--- Rotation speed ---");
            this.engineMaxSpeedMultiplier = builder.comment(new String[]{"The speed multiplier applied at maximum PSI (100).", "Speed at any PSI = baseSpeedRPM x (psi / 100) x this value.", "At PSI 50 the engine runs at half this multiplier x baseSpeedRPM.", "At PSI 10 (just above LOW threshold) it runs at 10% x this x baseSpeedRPM.", "Raise this to make high-pressure running faster without changing low-PSI feel.", "Default: 1.6"}).defineInRange("maxSpeedMultiplier", 1.6, 0.1, 10.0);
            builder.comment("--- Bleeder (thermostat-style relief valve) ---");
            this.bleederVentDropMinPsi = builder.comment(new String[]{"Minimum PSI-equivalent ejected each time the bleeder fires. Thermostat-", "style: watches for PSI creeping past the selected tier's own ceiling,", "then instantly ejects a random amount between this and bleederVentDropMaxPsi.", "Default: 7.0"}).defineInRange("bleederVentDropMinPsi", 7.0, 0.1, 100.0);
            this.bleederVentDropMaxPsi = builder.comment(new String[]{"Maximum PSI-equivalent ejected each time the bleeder fires.", "See bleederVentDropMinPsi for the full mechanic.", "Default: 10.0"}).defineInRange("bleederVentDropMaxPsi", 10.0, 0.1, 100.0);
            builder.comment("--- Sink Toll System ---");
            this.tollRateLight = builder.comment(new String[]{"mB/second drained by a LIGHT sink while actively working.", "Default tier for anything not explicitly listed as medium/heavy \u2014", "covers our own grinder/press/air compressor, TFC's quern and", "crankshaft, and any unrecognized mod's rotation sink.", "Default: 2"}).defineInRange("tollRateLight", 2, 0, 10000);
            this.tollRateMedium = builder.comment(new String[]{"mB/second drained by a MEDIUM sink while actively working.", "Covers the mechanical power hammer and the two-blade copper propeller.", "Default: 5"}).defineInRange("tollRateMedium", 5, 0, 10000);
            this.tollRateHeavy = builder.comment(new String[]{"mB/second drained by a HEAVY sink while actively working.", "Covers the pneumatic power hammer, water paddle, and four-blade brass propeller.", "Default: 10"}).defineInRange("tollRateHeavy", 10, 0, 10000);
            this.engineBaseSpeedRPM = builder.comment(new String[]{"How fast the engine spins its output axle at WORKING pressure,", "measured in RPM (rotations per minute). This is the easiest", "number to adjust if the engine feels too fast or too slow.", "The code converts this to the internal format automatically.", "", "TFC reference speeds:", "  Windmill    ~  60 RPM  (slow)", "  Water wheel ~ 120 RPM  (medium)", "", "At WORKING pressure the engine runs at this speed.", "At HIGH pressure it runs faster (x1.4 by default).", "At DANGER pressure it runs even faster (x1.6 by default).", "", "Safe range: 10 to 600 RPM.", "Above 600 RPM you may see visual jitter in TFC axles and gearboxes.", "Default: 120"}).defineInRange("baseSpeedRPM", 120.0, 10.0, 600.0);
            this.engineSoundVolume = builder.comment(new String[]{"Master volume for the triple-expansion engine's looping steam sound.", "Applied every tick \u2014 changes take effect immediately without restarting Minecraft.", "1.0 = default full volume. 0.0 = completely silent. Values above 1.0 boost volume.", "Note: the sound is also attenuated naturally by distance (LINEAR attenuation)", "and by solid blocks between the listener and the engine (~55% reduction per block).", "Default: 1.0"}).defineInRange("soundVolume", 1.0, 0.0, 4.0);
            builder.pop();
            builder.comment("Settings for the speed telegraph output's Create stress contribution.").push("speed_telegraph_output");
            this.telegraphOutputStressCapacity = builder.comment(new String[]{"Stress units (SU) the telegraph output adds to Create's kinetic", "network while actively driven by a running triple-expansion engine.", "This is a flat value \u2014 it does not scale with engine RPM.", "Default: 12000"}).defineInRange("stressCapacity", 12000.0, 0.0, 1000000.0);
            builder.pop();
            builder.comment("Settings for expeller press speed-vs-yield trade-off.").push("expeller_press");
            this.pressMaxSpeedRPM = builder.comment(new String[]{"The axle speed (RPM) at which the press produces minimum fluid yield.", "At working speed (BASE_SPEED ~60 RPM equivalent) you get full yield.", "At this speed you get the minimum yield defined by result_fluid_min.", "Between the two speeds, yield scales linearly.", "", "Set lower to make the yield penalty kick in sooner.", "Set higher (or equal to base speed) to effectively disable scaling.", "", "Default: 120 RPM (matches engine max output)."}).defineInRange("pressMaxSpeedRPM", 120.0, 10.0, 600.0);
            builder.pop();
            builder.comment("Settings for the TFC-shaft driven air compressor.").push("air_compressor");
            this.compressorMaxAir = builder.comment(new String[]{"Maximum compressed air units the compressor can store.", "PSI displayed = (current air / this value) x 100.", "Larger values mean slower PSI build-up but more buffer before DANGER.", "Default: 3000"}).defineInRange("maxAir", 3000, 100, 100000);
            this.compressorAirPerPulse = builder.comment(new String[]{"Air units produced per pulse at full RPM (see pulseRate).", "Scales down proportionally at lower RPM between minRpm and rpmCeiling.", "Production/tick = airPerPulse / pulseRate.", "Default: 4"}).defineInRange("airPerPulse", 4, 1, 10000);
            this.compressorPulseRate = builder.comment(new String[]{"Ticks between each production pulse at full RPM.", "20 = 1 pulse per second. 4 = 5 pulses per second (default).", "Lower values make the compressor fill faster at the same RPM.", "Default: 4"}).defineInRange("pulseRate", 4, 1, 500);
            this.compressorMinRpm = builder.comment(new String[]{"Minimum shaft RPM required for the compressor to produce any air.", "Below this threshold nothing is produced \u2014 the dead-band.", "Analogous to minSteamTemp on the boiler.", "Default: 10.0 RPM"}).defineInRange("minRpm", 10.0, 0.0, 600.0);
            this.compressorRpmCeiling = builder.comment(new String[]{"Shaft RPM at which air production reaches its maximum rate.", "Between minRpm and this value production scales linearly.", "Above this value production is capped at the full rate.", "Default: 120.0 RPM (matches engine baseSpeedRPM default)"}).defineInRange("rpmCeiling", 120.0, 1.0, 600.0);
            this.compressorExplosionRadius = builder.comment(new String[]{"Explosion radius when the compressor reaches DANGER pressure (PSI >= 85).", "Default: 3.0"}).defineInRange("explosionRadius", 3.0, 0.0, 10.0);
            this.compressorExplosionBlockDamage = builder.comment(new String[]{"Whether the over-pressure explosion destroys blocks.", "Default: true"}).define("explosionBlockDamage", true);
            this.compressorHoseRangePerItem = builder.comment(new String[]{"Blocks of range added per Air Hose item stored in a", "compressor's hose inventory (see AirCompressorBlockEntity", ".getTotalHoseCount(), summed across all hose slots).", "Default: 4"}).defineInRange("hoseRangePerItem", 4, 1, 64);
            this.compressorHoseMaxRange = builder.comment(new String[]{"Hard cap on hose range in blocks, regardless of stack size.", "Default: 64"}).defineInRange("hoseMaxRange", 64, 1, 512);
            builder.pop();
            builder.comment(new String[]{"Settings for the dive suit's infinite-breathing mechanic.", "See TIBAMDiveSuitEvents."}).push("dive_suit");
            this.diveSuitAirPerSecond = builder.comment(new String[]{"Compressor air units drained per second while a fully-suited,", "linked-and-connected player is underwater and breathing off the", "hose. Checked once per second, not per tick \u2014 see", "TIBAMDiveSuitEvents.EFFECT_DURATION_TICKS for the buffer window.", "A compressor that can't afford the full amount still grants", "breathing for that window as long as it has ANY air left \u2014", "this is not a partial/rationed effect.", "Default: 5"}).defineInRange("airPerSecond", 5, 1, 1000);
            builder.pop();
            builder.comment(new String[]{"Settings for propeller thrust on Valkyrien Skies ships.", "Only takes effect when Valkyrien Skies is installed."}).push("propulsion");
            this.propulsionForcePerTon = builder.comment(new String[]{"Global tuning constant: Newtons of thrust per kg of a", "propeller's rated tonnage, applied at peak RPM.", "Tuning guide: at 0.2 N/kg (default), two brass props rated", "at 750 tons should cruise a 1530-ton ship near 18 knots", "(9.26 m/s) with VS Sailing fluid drag providing resistance.", "Raise if ships feel sluggish; lower if too fast.", "Reference: 18 knots = 9.26 m/s, 30 knots = 15.43 m/s.", "Default: 2.0"}).defineInRange("forcePerTon", 2.0, 0.001, 100.0);
            this.brassRatedTonnage = builder.comment(new String[]{"Ship tonnage (metric tons) a single brass 4-blade propeller", "is rated to push at peak RPM.", "Default: 750.0"}).defineInRange("brassRatedTonnage", 800.0, 1.0, 1000000.0);
            this.brassPeakRpm = builder.comment(new String[]{"Shaft RPM at which the brass propeller produces peak thrust.", "Thrust ramps linearly from 0 to this RPM, then falls back off", "symmetrically up to brassMaxRpm (cavitation band).", "Default: 120.0"}).defineInRange("brassPeakRpm", 120.0, 1.0, 1000.0);
            this.brassMaxRpm = builder.comment(new String[]{"Absolute maximum safe RPM for the brass propeller.", "Thrust at this RPM equals thrust at (2 x brassPeakRpm - brassMaxRpm).", "Exceeding this RPM destroys the propeller.", "Default: 140.0"}).defineInRange("brassMaxRpm", 140.0, 1.0, 1000.0);
            this.copperRatedTonnage = builder.comment(new String[]{"Ship tonnage (metric tons) a single copper 2-blade propeller", "is rated to push at peak RPM.", "Default: 300.0"}).defineInRange("copperRatedTonnage", 400.0, 1.0, 1000000.0);
            this.copperMaxRpm = builder.comment(new String[]{"Maximum RPM for the copper propeller. Thrust ramps linearly", "from 0 to this RPM with no cavitation band.", "Exceeding this RPM destroys the propeller immediately.", "Default: 80.0"}).defineInRange("copperMaxRpm", 80.0, 1.0, 1000.0);
            this.maxShipSpeedMps = builder.comment(new String[]{"Hard speed ceiling for propeller-driven VS ships, in metres per second.", "Propellers stop applying thrust in their direction of motion once the", "ship reaches this speed. Does not cap speed from other sources", "(collisions, other mods, etc.).", "2 chunks/second = 32 m/s. 1 chunk/second = 16 m/s.", "Set very high (e.g. 10000) to effectively disable the cap.", "Default: 32.0"}).defineInRange("maxShipSpeedMps", 15.43, 0.1, 10000.0);
            this.paddleRatedTonnage = builder.comment(new String[]{"Ship tonnage (metric tons) a single cast-iron water paddle wheel", "is rated to push at peak RPM. Uses the same forcePerTon constant", "as the propellers above. Paddle wheels are meant to be run in", "matched pairs (one per side), so this is deliberately half of a", "pair's combined rating, not a full ship's worth on its own.", "Design target: a matched pair (2 x 333 t = 666 t) tops out at", "~15 knots vs the copper propeller's ~18 knots at 400 t peak.", "Default: 333.0"}).defineInRange("paddleRatedTonnage", 333.0, 1.0, 1000000.0);
            this.paddleMaxRpm = builder.comment(new String[]{"Maximum RPM for the water paddle wheel. Thrust ramps linearly", "from 0 to paddleMaxRpm, same shape as the copper propeller (no", "cavitation band \u2014 that is a screw-propeller-specific effect).", "Matches copperMaxRpm (80) so the paddle thrust curve spans the", "same RPM range as copper \u2014 a lower value causes the paddle to", "hit 100% thrust at mid-throttle and exceed its design speed.", "Exceeding this RPM destroys the paddle wheel immediately.", "Default: 80.0"}).defineInRange("paddleMaxRpm", 80.0, 1.0, 1000.0);
            this.speedTaperFraction = builder.comment(new String[]{"Fraction of maxShipSpeedMps, measured back from the cap, over which", "thrust tapers linearly to zero instead of cutting off abruptly.", "E.g. 0.2 means thrust starts easing off at 80% of max speed and", "reaches zero exactly at max speed. Set to 0 for a hard cutoff.", "Default: 0.2"}).defineInRange("speedTaperFraction", 0.2, 0.0, 0.9);
            this.propulsionSpindownSeconds = builder.comment(new String[]{"How many seconds it takes propeller thrust to decay from full", "to zero after the shaft stops turning (engine cut). This is", "independent of the shaft's own mechanical RPM ramp-down \u2014 it lets", "the ship coast down gradually instead of losing thrust the instant", "RPM hits zero, so overall deceleration time depends on both this", "value and whatever fluid drag your VS addon (VS Sailing, Eureka,", "etc.) provides.", "Default: 12.0"}).defineInRange("propulsionSpindownSeconds", 12.0, 0.1, 120.0);
            this.paddleSpinupSeconds = builder.comment(new String[]{"How many seconds paddle wheel thrust takes to climb from zero to", "full when running at max throttle with no propellers present on", "the same ship. Models the sluggish acceleration of a paddle steamer", "hull vs a screw-driven one. Spindown uses propulsionSpindownSeconds", "regardless of this setting.", "Default: 10.0"}).defineInRange("paddleSpinupSeconds", 10.0, 0.1, 120.0);
            this.paddleSpinupWithPropellersSeconds = builder.comment(new String[]{"Spinup time (seconds, same meaning as paddleSpinupSeconds) used", "when at least one propeller is also active on the same ship.", "Propeller wash over the hull reduces paddle drag, so mixed", "propulsion gets up to speed faster. Half of paddleSpinupSeconds", "by default.", "Default: 5.0"}).defineInRange("paddleSpinupWithPropellersSeconds", 5.0, 0.1, 120.0);
            builder.pop();
            builder.comment(new String[]{"Settings for the optional valkib ice-breaker compat \u2014 lets TFC", "hardwood and metal blocks (see the hardwood/icebreaker block tags)", "break nearby ice when built into a moving Valkyrien Skies ship.", "Only takes effect when both valkib and Valkyrien Skies are installed."}).push("icebreaker");
            this.icebreakerEnabled = builder.comment(new String[]{"Master switch for the icebreaker compat. Has no effect unless", "both valkib and Valkyrien Skies are loaded.", "Default: true"}).define("enabled", true);
            this.icebreakerScanIntervalTicks = builder.comment(new String[]{"How often (in ticks) each candidate ship is rescanned for", "icebreaker-tagged blocks. Lower values react faster but cost more;", "the scan walks every block in the ship's bounding box.", "Default: 20 (once per second)"}).defineInRange("scanIntervalTicks", 20, 1, 1200);
            this.icebreakerPlayerSearchRadius = builder.comment(new String[]{"Radius (blocks) around each online player used to find candidate", "ships to scan. Ships entirely outside this radius of every player", "are skipped that cycle. Default: 128.0"}).defineInRange("playerSearchRadius", 128.0, 16.0, 1024.0);
            this.icebreakerBreakRadius = builder.comment(new String[]{"Radius (blocks) around a matched icebreaker block, in world space,", "within which valkib:breakables blocks (ice, kelp, coral, etc.) are", "destroyed. 1 reproduces valkib's own 3x3x3 IceBreakerBlock area.", "Default: 1"}).defineInRange("breakRadius", 1, 1, 4);
            builder.pop();
            builder.comment(new String[]{"Feeds TFC's real wind (net.dries007.tfc.util.climate.Climate.getWindVector)", "into each loaded ship's Valkyrien Skies DragController, so ship physics", "responds to TFC's actual weather-driven wind instead of vs_sails' own", "independent procedural wind (which has no rule configured for TFC worlds", "and does nothing on its own). Only takes effect when Valkyrien Skies is", "installed; vs_sails is not required for this."}).push("windBridge");
            this.windBridgeEnabled = builder.comment(new String[]{"Master switch for the TFC-to-VS wind bridge. Has no effect unless", "Valkyrien Skies is loaded.", "Default: true"}).define("enabled", true);
            this.windBridgeIntervalTicks = builder.comment(new String[]{"How often (in ticks) each loaded ship's wind is resampled from TFC", "and pushed to its DragController. Lower values track weather changes", "more closely but cost more per ship.", "Default: 10 (twice per second)"}).defineInRange("intervalTicks", 10, 1, 600);
            this.windBridgePlayerSearchRadius = builder.comment(new String[]{"Radius (blocks) around each online player used to find candidate", "ships to feed wind to (same search pattern as the icebreaker compat \u2014", "there is no direct 'all loaded ships in this dimension' query", "available without depending on Valkyrien Skies' internal (non-api)", "classes, which this mod deliberately avoids). Ships entirely outside", "this radius of every player are skipped that cycle.", "Default: 256.0"}).defineInRange("playerSearchRadius", 256.0, 16.0, 2048.0);
            this.windBridgeSpeedScale = builder.comment(new String[]{"Multiplier converting TFC's wind intensity (0.0-1.0, from", "Climate.getWindVector) into the wind speed value handed to", "DragController.setWindSpeed. There is no verified real-world", "conversion factor for this \u2014 VS's drag/aerodynamics math is not", "TFC-aware, so this is a balance number, not a derived one. Tune it", "in-game against your ship's mass/drag/sail area until wind-driven", "speeds feel right; players can also override it per modpack.", "PLACEHOLDER default, not yet playtested: 10.0"}).defineInRange("speedScale", 10.0, 0.0, 1000.0);
            builder.pop();
            builder.comment("Settings for the mast/rigging/sail system.").push("masts");
            this.mastMaxHeight = builder.comment(new String[]{"Hard cap on total mast height in blocks, server-enforced.", "Width stops growing after 5 widenings regardless of this value;", "this only limits how many more 3-block tiers can be added on top.", "Default: 300"}).defineInRange("mastMaxHeight", 300, 3, 2048);
            builder.pop();
        }

        public float pressMaxSpeedRadPerTick() {
            double rpm = (Double)this.pressMaxSpeedRPM.get();
            return (float)(rpm * Math.PI / 600.0);
        }

        public double brassPeakThrustNewtons() {
            return (Double)this.brassRatedTonnage.get() * 1000.0 * (Double)this.propulsionForcePerTon.get();
        }

        public double copperPeakThrustNewtons() {
            return (Double)this.copperRatedTonnage.get() * 1000.0 * (Double)this.propulsionForcePerTon.get();
        }

        public double paddlePeakThrustNewtons() {
            return (Double)this.paddleRatedTonnage.get() * 1000.0 * (Double)this.propulsionForcePerTon.get();
        }

        public double brassThrustNewtons(double rpm) {
            double peak = (Double)this.brassPeakRpm.get();
            double max = (Double)this.brassMaxRpm.get();
            double maxThrust = this.brassPeakThrustNewtons();
            if (rpm <= 0.0) {
                return 0.0;
            }
            if (rpm > max) {
                return 0.0;
            }
            if (rpm <= peak) {
                return maxThrust * (rpm / peak);
            }
            double overshoot = rpm - peak;
            double bandWidth = max - peak;
            if (bandWidth <= 0.0) {
                return maxThrust;
            }
            double mirroredRpm = Math.max(0.0, peak - overshoot);
            return maxThrust * (mirroredRpm / peak);
        }

        public double copperThrustNewtons(double rpm) {
            double max = (Double)this.copperMaxRpm.get();
            double maxThrust = this.copperPeakThrustNewtons();
            if (rpm <= 0.0) {
                return 0.0;
            }
            if (rpm > max) {
                return 0.0;
            }
            return maxThrust * (rpm / max);
        }

        public double paddleThrustNewtons(double rpm) {
            double max = (Double)this.paddleMaxRpm.get();
            double maxThrust = this.paddlePeakThrustNewtons();
            if (rpm <= 0.0) {
                return 0.0;
            }
            if (rpm > max) {
                return 0.0;
            }
            return maxThrust * (rpm / max);
        }
    }
}

