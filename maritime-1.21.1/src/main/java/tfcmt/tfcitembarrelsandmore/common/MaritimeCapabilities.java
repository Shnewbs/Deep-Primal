package tfcmt.tfcitembarrelsandmore.common;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/** Capability providers are registered on the mod bus; block entities retain side filtering. */
public final class MaritimeCapabilities {
    private MaritimeCapabilities() {}
    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TIBAMBlockEntities.BOILER.get(), (be, side) -> be.maritimeItems(side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TIBAMBlockEntities.GRINDER.get(), (be, side) -> be.maritimeItems(side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TIBAMBlockEntities.EXPELLER_PRESS.get(), (be, side) -> be.maritimeItems(side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TIBAMBlockEntities.SINGLE_PISTON_ENGINE.get(), (be, side) -> be.maritimeItems(side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TIBAMBlockEntities.MECHANICAL_POWER_HAMMER.get(), (be, side) -> be.maritimeItems(side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TIBAMBlockEntities.PNEUMATIC_POWER_HAMMER.get(), (be, side) -> be.maritimeItems(side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TIBAMBlockEntities.SMOKE_SHACK.get(), (be, side) -> be.maritimeItems(side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TIBAMBlockEntities.ITEM_BARREL.get(), (be, side) -> be.maritimeItems(side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, TIBAMBlockEntities.EXPELLER_PRESS.get(), (be, side) -> be.maritimeFluids(side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, TIBAMBlockEntities.SINGLE_PISTON_ENGINE.get(), (be, side) -> be.maritimeFluids(side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, TIBAMBlockEntities.BOILER_TANK.get(), (be, side) -> be.maritimeFluids(side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, TIBAMBlockEntities.STEEL_BOILER_TANK.get(), (be, side) -> be.maritimeFluids(side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, TIBAMBlockEntities.EXPLORATION_LAMP.get(), (be, side) -> be.maritimeFluids(side));
        event.registerBlockEntity(net.dries007.tfc.common.capabilities.BlockCapabilities.HEAT, TIBAMBlockEntities.BOILER.get(), (be, side) -> be.maritimeHeat(side));
    }
}
