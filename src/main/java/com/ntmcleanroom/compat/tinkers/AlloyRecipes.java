package com.ntmcleanroom.compat.tinkers;

import com.hbm.inventory.OreDictManager;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.shared.TinkerFluids;

/**
 * Ports all 13 of hbm's real "Crucible Alloying" recipes (confirmed via bytecode of
 * {@code com.hbm.inventory.recipes.CrucibleRecipes.registerDefaults()}) onto Tinkers' Antique's
 * own native alloying ({@code TinkerRegistry.registerAlloy}: fluids mixed in a seared tank
 * automatically produce a new fluid) - the closest equivalent Tinkers has to hbm's multi-ingredient
 * Crucible alloying.
 *
 * <p>Amounts are hbm's own real per-recipe ratios, scaled x2: hbm's own internal unit is 8 per
 * nugget / 72 per ingot, and our bridge melts 1 ingot into 144 mB by default, i.e. exactly 2x
 * hbm's unit - so every recipe below reproduces hbm's real proportions exactly, just expressed on
 * our own mB scale.
 *
 * <p>Two of hbm's 13 recipes aren't true multi-input alloys - Hematite/Malachite are ore->metal
 * smelts that simply share the same JEI "Crucible Alloying" tab - so those are registered as
 * plain single-input melts instead ({@code registerAlloy} itself requires >= 2 inputs). Where a
 * recipe's real output includes a secondary byproduct alongside the primary one (Slag, from
 * ore-smelting) it's dropped, since Tinkers' {@code registerAlloy} only supports one result fluid;
 * Flux is dropped throughout since hbm's own mass balance shows it acts as a non-incorporated
 * catalyst (input mass without it already accounts for the full output mass).
 *
 * <p><b>Every ingredient/output here except Carbon and Redstone is sourced from
 * {@link FoundryShapeBridge#extraFluids()}, not a fluid of our own.</b> A duplicate "Molten BSCCO"
 * turned up in-game from an earlier version of this class that gave BSCCO its own bare fluid -
 * decompiling {@code Mats.java} confirmed Bismuth, Arsenic, Tungsten, Magnetized Tungsten, Cobalt,
 * Uranium-238, Technetium-99, Cadmium, Strontium, Calcium, Mingrade, DuraSteel, Ferrouranium,
 * TcAlloy, CdAlloy, and BSCCO are *all* {@code SmeltingBehavior.SMELTABLE}, meaning
 * {@link FoundryShapeBridge} already generically bridges every one of them (fluid + melting recipe
 * + full generic shape-casting, dense wire included, via {@link ShapeCasts}) before this class ever
 * runs. Reusing those fluids directly, instead of minting a second orphaned one with none of that
 * casting infrastructure, is both the fix for the duplicate and the fix for dense wire (and every
 * other shape) never having worked for these outputs. Only Carbon (hbm marks it
 * {@code SmeltingBehavior.ADDITIVE}, not {@code SMELTABLE}, so {@link FoundryShapeBridge} skips it)
 * and Redstone (a vanilla item, never an hbm-material candidate at all) genuinely need a fluid of
 * their own here.
 */
public final class AlloyRecipes {

    private static final ResourceFluid CARBON = new ResourceFluid("ntmalloy_carbon", 0xFF303030);
    private static final ResourceFluid REDSTONE = new ResourceFluid("ntmalloy_redstone", 0xFFAA0000);

    private AlloyRecipes() {}

    public static void register() {
        CARBON.registerFluid();
        REDSTONE.registerFluid();

        registerMelting(CARBON, OreDictManager.CARBON.nugget());
        registerMelting(REDSTONE, Items.REDSTONE);

        // Hematite/Malachite aren't true multi-input alloys (see class doc) - plain single-input
        // melts, reusing Tinkers' own native Iron/Copper fluids, at the same per-item rate used
        // for every other ore in this bridge.
        TinkerRegistry.registerMelting(OreDictManager.HEMATITE.ore(), TinkerFluids.iron, 144);
        TinkerRegistry.registerMelting(OreDictManager.MALACHITE.ore(), TinkerFluids.copper, 144);

        Fluid steel = fluidOf("ntmsteel");
        Fluid cmbSteel = fluidOf("ntmcmbsteel");
        Fluid schrabidium = fluidOf("ntmschrabidium");
        Fluid bismuthBronze = fluidOf("ntmbismuthbronze");
        Fluid arsenicBronze = fluidOf("ntmarsenicbronze");

        Fluid bismuth = extra("Bismuth");
        Fluid arsenic = extra("Arsenic");
        Fluid tungsten = extra("Tungsten");
        Fluid magnetizedTungsten = extra("MagnetizedTungsten");
        Fluid cobalt = extra("Cobalt");
        Fluid uranium238 = extra("Uranium238");
        Fluid technetium99 = extra("Technetium99");
        Fluid cadmium = extra("Cadmium");
        Fluid strontium = extra("Strontium");
        Fluid calcium = extra("Calcium");
        Fluid mingrade = extra("Mingrade");
        Fluid duraSteel = extra("DuraSteel");
        Fluid ferrouranium = extra("Ferrouranium");
        Fluid tcAlloy = extra("TcAlloy");
        Fluid cdAlloy = extra("CdAlloy");
        Fluid bscco = extra("BSCCO");

        // crucible.steel: Iron 16 + Carbon 24 (+ Flux 8, dropped) -> Steel 16.
        tryAlloy(steel, 32, TinkerFluids.iron, 32, CARBON.fluid, 48);

        // crucible.redcopper: Copper 8 + Redstone 8 -> Mingrade 16.
        tryAlloy(mingrade, 32, TinkerFluids.copper, 16, REDSTONE.fluid, 16);

        // crucible.hss: Steel 40 + Tungsten 24 + Cobalt 8 -> DuraSteel 72.
        tryAlloy(duraSteel, 144, steel, 80, tungsten, 48, cobalt, 16);

        // crucible.ferro: Steel 16 + Uranium238 8 -> Ferrouranium 24.
        tryAlloy(ferrouranium, 48, steel, 32, uranium238, 16);

        // crucible.tcalloy: Steel 64 + Technetium99 8 -> TcAlloy 72.
        tryAlloy(tcAlloy, 144, steel, 128, technetium99, 16);

        // crucible.cdalloy: Steel 64 + Cadmium 8 -> CdAlloy 72.
        tryAlloy(cdAlloy, 144, steel, 128, cadmium, 16);

        // crucible.bbronze: Copper 64 + Bismuth 8 (+ Flux 24, Slag 24, dropped) -> BismuthBronze 72.
        tryAlloy(bismuthBronze, 144, TinkerFluids.copper, 128, bismuth, 16);

        // crucible.abronze: Copper 64 + Arsenic 8 (+ Flux 24, Slag 24, dropped) -> ArsenicBronze 72.
        tryAlloy(arsenicBronze, 144, TinkerFluids.copper, 128, arsenic, 16);

        // crucible.magtung: Tungsten 72 + Schrabidium 8 -> Magnetized Tungsten 72 (not mass
        // conserving in hbm's own recipe either - the Schrabidium is a ~10% trace additive).
        tryAlloy(magnetizedTungsten, 144, tungsten, 144, schrabidium, 16);

        // crucible.cmb: Magnetized Tungsten 48 + WatzMud 24 -> Combine Steel 72. hbm already has
        // its own real Mud fluid (com.hbm.blocks.fluid.ModFluids.mud_fluid) - used directly rather
        // than a substitute.
        tryAlloy(cmbSteel, 144, magnetizedTungsten, 96, com.hbm.blocks.fluid.ModFluids.mud_fluid, 48);

        // crucible.bscco: Bismuth 16 + Strontium 16 + Calcium 16 + Copper 24 -> BSCCO 72.
        tryAlloy(bscco, 144, bismuth, 32, strontium, 32, calcium, 32, TinkerFluids.copper, 48);
    }

    private static Fluid fluidOf(String materialId) {
        NTMTinkerMaterial ntm = TinkersMaterialRegistry.get(materialId);
        return ntm != null ? ntm.fluid : null;
    }

    /** Looks up a fluid {@link FoundryShapeBridge} already bridged generically, keyed by hbm's own material suffix string (e.g. "BSCCO", "MagnetizedTungsten"). */
    private static Fluid extra(String suffix) {
        return FoundryShapeBridge.extraFluids().get(suffix);
    }

    /** Registers an alloy if the result fluid and every input fluid resolved to something real; silently skips otherwise (e.g. a material not present in this hbm build). */
    private static void tryAlloy(Fluid result, int resultAmount, Object... inputPairs) {
        if (result == null) {
            return;
        }
        FluidStack[] inputs = new FluidStack[inputPairs.length / 2];
        for (int i = 0; i < inputs.length; i++) {
            Fluid fluid = (Fluid) inputPairs[i * 2];
            if (fluid == null) {
                return;
            }
            inputs[i] = new FluidStack(fluid, (Integer) inputPairs[i * 2 + 1]);
        }
        TinkerRegistry.registerAlloy(new FluidStack(result, resultAmount), inputs);
    }

    private static void registerMelting(ResourceFluid resource, Item item) {
        if (item != null) {
            TinkerRegistry.registerMelting(item, resource.fluid, TinkerSmelteryRecipes.mbPerIngot(resource.id));
        }
    }

    private static void registerMelting(ResourceFluid resource, String oreDictName) {
        TinkerRegistry.registerMelting(oreDictName, resource.fluid, TinkerSmelteryRecipes.mbPerIngot(resource.id));
    }

    /** A bare Forge fluid for an alloy ingredient that isn't one of our bridged materials and isn't already bridged generically by {@link FoundryShapeBridge} - no Tinkers Material/traits/casting, just meltable enough to feed an alloy recipe. */
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
