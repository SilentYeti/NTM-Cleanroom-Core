package com.ntmcleanroom.compat.tinkers;

import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.NTMMaterial;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Bridges every other hbm material its own Foundry/Mold system can shape - not just our 5 full +
 * 6 placeholder Tinkers materials from {@link TinkersMaterialRegistry}. Bare minimum only, per the
 * user: a molten fluid + melting/basin-casting registration + the same generic shape-casting
 * recipes {@link ShapeCasts} already wires up for our bridged materials - no Tinkers
 * {@code Material}/stats/traits/tool-part integration, since these aren't meant to become tool
 * heads, just match whatever hbm's own Foundry could already produce for any of its ~100 smeltable
 * materials ({@link Mats#orderedList}, filtered to {@code SmeltingBehavior.SMELTABLE}).
 *
 * <p>Ingots/nuggets cast on the casting table using Tinkers' own reusable gold Ingot/Nugget Cast
 * items, blocks basin-cast, ores melt-only - the identical per-form treatment (and shared code)
 * as the 11 curated materials, see
 * {@link TinkersMaterialRegistry#registerStandardMeltingCasting}.
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
                // Named via hbm's own material translation key ("hbmmat.<name>", resolved lazily
                // at display time), so each fluid shows the material's REAL display name -
                // "Minecraft Grade Copper", "Technetium Steel", "High-Speed Steel" - not a
                // prettified version of the internal oredict tag ("Mingrade", "TcAlloy",
                // "DuraSteel"). The prettified suffix stays only as a fallback for any material
                // missing an hbmmat lang entry.
                fluid = new NamedFluid("molten_hbm_" + fluidName, material.getTranslationKey(),
                        "Molten " + prettify(suffix))
                        .setColor(0xFF000000 | material.moltenColor);
                FluidRegistry.registerFluid(fluid);
                // Full standard treatment, shared with the curated materials: ingot/nugget
                // table-cast via Tinkers' own gold Ingot/Nugget Casts, block basin-cast, ore
                // melt-only (see TinkersMaterialRegistry.registerStandardMeltingCasting).
                TinkersMaterialRegistry.registerStandardMeltingCasting(fluid, suffix,
                        TinkerSmelteryRecipes.mbPerIngot("hbm_" + suffix));
            }

            EXTRA_FLUIDS.put(suffix, fluid);
        }
    }

    /** Suffix (e.g. "Copper") -> fluid, for every extra material bridged here. Used by {@link ShapeCasts} to extend generic shape-casting to all of them too. */
    public static Map<String, Fluid> extraFluids() {
        return EXTRA_FLUIDS;
    }

    /**
     * Splits hbm's raw camelCase material suffix into a readable name: a space before each
     * capital that follows a lowercase letter or precedes one ("MagnetizedTungsten" -> "Magnetized
     * Tungsten", "CdAlloy" -> "Cd Alloy"), a hyphen before a run of digits following a letter
     * ("Uranium238" -> "Uranium-238", matching this project's own existing "Uranium-238"/
     * "Technetium-99" convention), and an all-caps acronym left untouched ("BSCCO" stays "BSCCO").
     */
    private static String prettify(String suffix) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < suffix.length(); i++) {
            char c = suffix.charAt(i);
            if (i > 0) {
                char prev = suffix.charAt(i - 1);
                boolean upperBoundary = Character.isUpperCase(c)
                        && (Character.isLowerCase(prev)
                                || (Character.isUpperCase(prev) && i + 1 < suffix.length() && Character.isLowerCase(suffix.charAt(i + 1))));
                boolean digitBoundary = Character.isDigit(c) && !Character.isDigit(prev);
                if (upperBoundary) {
                    sb.append(' ');
                } else if (digitBoundary) {
                    sb.append('-');
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * A {@link Fluid} named "Molten " + the material's real display name, resolved lazily through
     * hbm's own {@code hbmmat.<name>} material translation key (the same one hbm's own UIs use) -
     * with a fixed fallback string for any material missing a lang entry. Lazy resolution (at
     * display time, not construction) matters because lang data isn't loaded yet when fluids are
     * registered. Uses the same server-safe {@code translation.I18n} Forge's own
     * {@code Fluid.getLocalizedName} default implementation uses.
     */
    @SuppressWarnings("deprecation")
    private static final class NamedFluid extends Fluid {
        private final String materialTranslationKey;
        private final String fallbackName;

        NamedFluid(String name, String materialTranslationKey, String fallbackName) {
            super(name, FluidTextureHandler.FLUID_STILL, FluidTextureHandler.FLUID_FLOW);
            this.materialTranslationKey = materialTranslationKey;
            this.fallbackName = fallbackName;
        }

        @Override
        public String getLocalizedName(FluidStack stack) {
            if (net.minecraft.util.text.translation.I18n.canTranslate(materialTranslationKey)) {
                return "Molten " + net.minecraft.util.text.translation.I18n.translateToLocal(materialTranslationKey);
            }
            return fallbackName;
        }
    }
}
