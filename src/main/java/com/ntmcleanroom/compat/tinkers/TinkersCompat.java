package com.ntmcleanroom.compat.tinkers;

import com.ntmcleanroom.compat.tinkers.ability.AbilityNetworking;
import com.ntmcleanroom.compat.tinkers.ability.AbilityRightClickHandler;
import com.ntmcleanroom.compat.tinkers.fuel.HeFuelerModule;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Entry point for the Tinkers' Antique compat: hbm metals become meltable/castable in the Tinkers'
 * smeltery, with materials, stats and trait-based abilities mirroring hbm's own tool tiers, plus an
 * HE-powered smeltery fueler block. Everything here is a no-op unless Tinkers' Antique (modid
 * "tconstruct") is actually loaded, so the core mod works fine without it.
 */
public final class TinkersCompat {

    public static final String TCONSTRUCT_MODID = "tconstruct";

    private TinkersCompat() {}

    public static boolean isLoaded() {
        return Loader.isModLoaded(TCONSTRUCT_MODID);
    }

    public static void preInit(FMLPreInitializationEvent event) {
        if (!isLoaded()) {
            return;
        }

        HeFuelerModule.preInit();
        TinkersMaterialRegistry.registerFluidsAndMaterials();
        // Item creation only - must happen before proxy.registerRenderInfo() (also preInit) tries
        // to register models for these items.
        ShapeCasts.registerItems();
        AbilityNetworking.init();
    }

    /** Client-only event registration - called from ClientProxy.preInit(), separate from the
     * common preInit() above since this touches client-only classes not present on a server. */
    @SideOnly(Side.CLIENT)
    public static void registerKeybinds() {
        if (!isLoaded()) {
            return;
        }

        AbilityRightClickHandler.register();
        TraitTooltipFilter.register();
    }

    public static void init(FMLInitializationEvent event) {
        if (!isLoaded()) {
            return;
        }

        HeFuelerModule.init();
        // registerMeltingAndCasting() loads TinkerSmelteryRecipes first - must run before
        // FoundryShapeBridge/ShapeCasts.registerCasting()/AlloyRecipes.register(), which also read
        // configured mB amounts from it. FoundryShapeBridge must run before ShapeCasts.registerCasting()
        // too, since that also wires generic shape casting for every extra fluid it bridges.
        TinkersMaterialRegistry.registerMeltingAndCasting();
        FoundryShapeBridge.register();
        ShapeCasts.registerCasting();
        AlloyRecipes.register();
    }

    public static void postInit(FMLPostInitializationEvent event) {
        if (!isLoaded()) {
            return;
        }

        HeFuelerModule.postInit();
    }

    @SideOnly(Side.CLIENT)
    public static void registerModels() {
        if (!isLoaded()) {
            return;
        }

        ShapeCasts.registerModels();
        HeFuelerModule.registerModels();
        MinecraftForge.EVENT_BUS.register(new FluidTextureHandler());
    }
}
