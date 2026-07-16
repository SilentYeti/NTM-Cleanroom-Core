package com.ntmcleanroom.compat.tinkers;

import com.hbm.inventory.OreDictManager;
import com.hbm.items.ModItems;
import net.minecraft.item.Item;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.shared.TinkerFluids;

/**
 * Brings hbm's Crucible alloy recipes into the Tinkers' smeltery, using Tinkers' own native
 * {@code TinkerRegistry.registerAlloy} (fluids combined in the tank automatically produce a new
 * fluid) - the closest equivalent Tinkers has to hbm's multi-ingredient Crucible alloying. Not a
 * 1:1 port: hbm's real ratios (see the plan/research) use its own quanta/nugget units that don't
 * map cleanly onto Tinkers' mB. Flux is dropped (hbm itself classifies it as a non-metal
 * "additive", built via {@code makeAdditive}, with nothing sensible to melt); Mud is kept, using
 * hbm's own real Mud fluid directly. Chain: Iron+Carbon for Steel, Copper+Bismuth/Arsenic for the
 * two bronzes, and a Tungsten+Schrabidium "Magnetized Tungsten" intermediate that then alloys with
 * Mud into Combine Steel, mirroring hbm's own multi-step chain (Tungsten -> Magnetized Tungsten ->
 * Combine Steel).
 *
 * Every ratio here is a proportion of the (configurable) {@code TinkerSmelteryRecipes.mbPerIngot}
 * total for the OUTPUT material, so tuning that config also scales these alloys.
 */
public final class AlloyRecipes {

    private static final ResourceFluid CARBON = new ResourceFluid("ntmalloy_carbon", 0xFF303030);
    private static final ResourceFluid BISMUTH = new ResourceFluid("ntmalloy_bismuth", 0xFFC299CC);
    private static final ResourceFluid ARSENIC = new ResourceFluid("ntmalloy_arsenic", 0xFFB5B36A);
    private static final ResourceFluid TUNGSTEN = new ResourceFluid("ntmalloy_tungsten", 0xFF4A4A52);
    private static final ResourceFluid MAGNETIZED_TUNGSTEN = new ResourceFluid("ntmalloy_magnetized_tungsten", 0xFF5A6A82);

    private AlloyRecipes() {}

    public static void register() {
        for (ResourceFluid resource : new ResourceFluid[] {CARBON, BISMUTH, ARSENIC, TUNGSTEN, MAGNETIZED_TUNGSTEN}) {
            resource.registerFluid();
        }

        registerMelting(CARBON, OreDictManager.CARBON.nugget());
        registerMelting(BISMUTH, ModItems.nugget_bismuth);
        registerMelting(ARSENIC, ModItems.nugget_arsenic);
        registerMelting(TUNGSTEN, ModItems.ingot_tungsten);
        registerMelting(MAGNETIZED_TUNGSTEN, ModItems.ingot_magnetized_tungsten);

        // Tungsten + a trace of Schrabidium -> Magnetized Tungsten (hbm's crucible.magtung).
        NTMTinkerMaterial schrabidium = TinkersMaterialRegistry.get("ntmschrabidium");
        if (schrabidium != null && schrabidium.fluid != null) {
            int total = TinkerSmelteryRecipes.mbPerIngot("ntmschrabidium");
            int trace = Math.max(1, total / 10);
            TinkerRegistry.registerAlloy(
                    new FluidStack(MAGNETIZED_TUNGSTEN.fluid, total),
                    new FluidStack(TUNGSTEN.fluid, total - trace),
                    new FluidStack(schrabidium.fluid, trace));
        }

        alloy("ntmsteel", TinkerFluids.iron, CARBON.fluid);
        alloy("ntmbismuthbronze", TinkerFluids.copper, BISMUTH.fluid);
        alloy("ntmarsenicbronze", TinkerFluids.copper, ARSENIC.fluid);
        // hbm's crucible.cmb mixes Magnetized Tungsten with Mud - hbm already has its own real
        // "Mud" fluid (com.hbm.blocks.fluid.ModFluids.mud_fluid), so this uses that directly rather
        // than a substitute (also satisfies Tinkers' registerAlloy, which rejects single-ingredient
        // alloys outright).
        alloy("ntmcmbsteel", MAGNETIZED_TUNGSTEN.fluid, com.hbm.blocks.fluid.ModFluids.mud_fluid);
    }

    /** Registers a majority-metal + minor-ingredient alloy (Iron+Carbon, Copper+Bismuth/Arsenic), sized off the output material's configured mB/ingot. */
    private static void alloy(String outputMaterialId, Fluid majorFluid, Fluid minorFluid) {
        NTMTinkerMaterial ntm = TinkersMaterialRegistry.get(outputMaterialId);
        if (ntm == null || ntm.fluid == null) {
            return;
        }

        int total = TinkerSmelteryRecipes.mbPerIngot(outputMaterialId);
        int minor = total / 4;
        int major = total - minor;

        TinkerRegistry.registerAlloy(
                new FluidStack(ntm.fluid, total),
                new FluidStack(majorFluid, major),
                new FluidStack(minorFluid, minor));
    }

    private static void registerMelting(ResourceFluid resource, Item item) {
        if (item != null) {
            TinkerRegistry.registerMelting(item, resource.fluid, TinkerSmelteryRecipes.mbPerIngot(resource.id));
        }
    }

    private static void registerMelting(ResourceFluid resource, String oreDictName) {
        TinkerRegistry.registerMelting(oreDictName, resource.fluid, TinkerSmelteryRecipes.mbPerIngot(resource.id));
    }

    /** A bare Forge fluid for an alloy ingredient that isn't one of our bridged materials - no Tinkers Material/traits/casting, just meltable enough to feed an alloy recipe. */
    private static final class ResourceFluid {
        final String id;
        final int color;
        Fluid fluid;

        ResourceFluid(String id, int color) {
            this.id = id;
            this.color = color;
        }

        void registerFluid() {
            fluid = new Fluid(id, FluidTextureHandler.FLUID_STILL, FluidTextureHandler.FLUID_FLOW)
                    .setColor(0xFF000000 | color);
            FluidRegistry.registerFluid(fluid);
        }
    }
}
