package tfcmt.tfcitembarrelsandmore.common;

import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import tfcmt.tfcitembarrelsandmore.common.blockentities.AirCompressorBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.BoilerBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.BoilerTankBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.ExpellerPressBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.ItemBarrelBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.MechanicalPowerHammerBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.SinglePistonEngineBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.SmokeShackBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.container.AirCompressorContainer;
import tfcmt.tfcitembarrelsandmore.common.container.BoilerContainer;
import tfcmt.tfcitembarrelsandmore.common.container.BoilerTankContainer;
import tfcmt.tfcitembarrelsandmore.common.container.ExpellerPressContainer;
import tfcmt.tfcitembarrelsandmore.common.container.ItemBarrelContainer;
import tfcmt.tfcitembarrelsandmore.common.container.MechanicalPowerHammerContainer;
import tfcmt.tfcitembarrelsandmore.common.container.SinglePistonEngineContainer;
import tfcmt.tfcitembarrelsandmore.common.container.SmokeShackContainer;

public class TIBAMContainerTypes {
    public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(Registries.MENU, (String)"tfc_item_barrels_and_more");
    public static final DeferredHolder<MenuType<?>, MenuType<BoilerContainer>> BOILER = CONTAINERS.register("boiler", () -> IMenuTypeExtension.create((windowId, inv, data) -> BoilerContainer.create((BoilerBlockEntity)inv.player.level().getBlockEntity(data.readBlockPos()), inv, windowId)));
    public static final DeferredHolder<MenuType<?>, MenuType<SinglePistonEngineContainer>> SINGLE_PISTON_ENGINE = CONTAINERS.register("single_piston_engine", () -> IMenuTypeExtension.create((windowId, inv, data) -> SinglePistonEngineContainer.create((SinglePistonEngineBlockEntity)inv.player.level().getBlockEntity(data.readBlockPos()), inv, windowId)));
    public static final DeferredHolder<MenuType<?>, MenuType<BoilerTankContainer>> BOILER_TANK = CONTAINERS.register("boiler_tank", () -> IMenuTypeExtension.create((windowId, inv, data) -> BoilerTankContainer.create((BoilerTankBlockEntity)inv.player.level().getBlockEntity(data.readBlockPos()), inv, windowId)));
    public static final DeferredHolder<MenuType<?>, MenuType<ItemBarrelContainer>> ITEM_BARREL = CONTAINERS.register("item_barrel", () -> IMenuTypeExtension.create((windowId, inv, data) -> ItemBarrelContainer.create((ItemBarrelBlockEntity)inv.player.level().getBlockEntity(data.readBlockPos()), inv, windowId)));
    public static final DeferredHolder<MenuType<?>, MenuType<ExpellerPressContainer>> EXPELLER_PRESS = CONTAINERS.register("expeller_press", () -> IMenuTypeExtension.create((windowId, inv, data) -> ExpellerPressContainer.create((ExpellerPressBlockEntity)inv.player.level().getBlockEntity(data.readBlockPos()), inv, windowId)));
    public static final DeferredHolder<MenuType<?>, MenuType<SmokeShackContainer>> SMOKE_SHACK = CONTAINERS.register("smoke_shack", () -> IMenuTypeExtension.create((windowId, inv, data) -> SmokeShackContainer.create((SmokeShackBlockEntity)inv.player.level().getBlockEntity(data.readBlockPos()), inv, windowId)));
    public static final DeferredHolder<MenuType<?>, MenuType<MechanicalPowerHammerContainer>> MECHANICAL_POWER_HAMMER = CONTAINERS.register("mechanical_power_hammer", () -> IMenuTypeExtension.create((windowId, inv, data) -> MechanicalPowerHammerContainer.create((MechanicalPowerHammerBlockEntity)inv.player.level().getBlockEntity(data.readBlockPos()), inv, windowId)));
    public static final DeferredHolder<MenuType<?>, MenuType<MechanicalPowerHammerContainer>> PNEUMATIC_POWER_HAMMER = CONTAINERS.register("pneumatic_power_hammer", () -> IMenuTypeExtension.create((windowId, inv, data) -> MechanicalPowerHammerContainer.createForType((MenuType)PNEUMATIC_POWER_HAMMER.get(), (MechanicalPowerHammerBlockEntity)inv.player.level().getBlockEntity(data.readBlockPos()), inv, windowId)));
    public static final DeferredHolder<MenuType<?>, MenuType<AirCompressorContainer>> AIR_COMPRESSOR = CONTAINERS.register("air_compressor", () -> IMenuTypeExtension.create((windowId, inv, data) -> AirCompressorContainer.create((AirCompressorBlockEntity)inv.player.level().getBlockEntity(data.readBlockPos()), inv, windowId)));
}

