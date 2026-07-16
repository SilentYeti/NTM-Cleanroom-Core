package com.ntmcleanroom.compat.tinkers;

import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.NTMMaterial;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import slimeknights.tconstruct.library.TinkerRegistry;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Bridges every other hbm material its own Foundry/Mold system can shape - not just our 5 full +
 * 6 placeholder Tinkers materials from {@link TinkersMaterialRegistry}. Bare minimum only, per the
 * user: a molten fluid + melting registration + the same generic shape-casting recipes
 * {@link ShapeCasts} already wires up for our bridged materials - no Tinkers {@code Material}/
 * stats/traits/tool-part integration, since these aren't meant to become tool heads, just match
 * whatever hbm's own Foundry could already produce for any of its ~100 smeltable materials
 * ({@link Mats#orderedList}, filtered to {@code SmeltingBehavior.SMELTABLE}).
 */
public final class FoundryShapeBridge {

    private static final Map<String, Fluid> EXTRA_FLUIDS = new LinkedHashMap<>();

    private FoundryShapeBridge() {}

    /** Registers a fluid + melting recipe for every smeltable hbm material not already one of our bridged 11. Runs in init, alongside {@link TinkersMaterialRegistry#registerMeltingAndCasting()}. */
    public static void register() {
        EXTRA_FLUIDS.clear();

        Set<String> bridgedSuffixes = new HashSet<>();
        for (NTMTinkerMaterial ntm : TinkersMaterialRegistry.all()) {
            bridgedSuffixes.add(ntm.ingotOreDict.substring("ingot".length()));
        }

        for (NTMMaterial material : Mats.orderedList) {
            if (material.smeltable != NTMMaterial.SmeltingBehavior.SMELTABLE) {
                continue;
            }
            if (material.names == null || material.names.length == 0) {
                continue;
            }

            String suffix = material.names[0];
            if (bridgedSuffixes.contains(suffix) || EXTRA_FLUIDS.containsKey(suffix)) {
                continue;
            }

            String fluidName = suffix.toLowerCase();
            Fluid fluid;
            if (FluidRegistry.isFluidRegistered(fluidName)) {
                // Reuse whatever mod already registered this base metal's fluid (confirmed via
                // testing: Tinkers itself already registers plain "copper", and this is true for
                // most shared base metals - iron/gold/tin/silver/nickel/zinc/aluminum/etc) instead
                // of creating a redundant, confusing duplicate fluid + melting recipe.
                fluid = FluidRegistry.getFluid(fluidName);
            } else {
                fluid = new Fluid("molten_hbm_" + fluidName, FluidTextureHandler.FLUID_STILL, FluidTextureHandler.FLUID_FLOW)
                        .setColor(0xFF000000 | material.moltenColor);
                FluidRegistry.registerFluid(fluid);
                TinkerRegistry.registerMelting("ingot" + suffix, fluid, TinkerSmelteryRecipes.mbPerIngot("hbm_" + suffix));
            }

            EXTRA_FLUIDS.put(suffix, fluid);
        }
    }

    /** Suffix (e.g. "Copper") -> fluid, for every extra material bridged here. Used by {@link ShapeCasts} to extend generic shape-casting to all of them too. */
    public static Map<String, Fluid> extraFluids() {
        return EXTRA_FLUIDS;
    }
}
