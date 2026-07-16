package com.ntmcleanroom.compat.tinkers;

import com.hbm.inventory.OreDictManager;
import com.hbm.inventory.material.Mats;
import com.ntmcleanroom.compat.tinkers.traits.AoeTrait;
import com.ntmcleanroom.compat.tinkers.traits.AutoShredTrait;
import com.ntmcleanroom.compat.tinkers.traits.AutoSmeltTrait;
import com.ntmcleanroom.compat.tinkers.traits.BeheaderTrait;
import com.ntmcleanroom.compat.tinkers.traits.FlatAoeTrait;
import com.ntmcleanroom.compat.tinkers.traits.FortuneTrait;
import com.ntmcleanroom.compat.tinkers.traits.RadioactiveBladeTrait;
import com.ntmcleanroom.compat.tinkers.traits.SilkTouchTrait;
import com.ntmcleanroom.compat.tinkers.traits.StunTrait;
import com.ntmcleanroom.compat.tinkers.traits.VampireTrait;
import com.ntmcleanroom.compat.tinkers.traits.VeinMinerTrait;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.oredict.OreDictionary;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.materials.HandleMaterialStats;
import slimeknights.tconstruct.library.materials.HeadMaterialStats;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.traits.ITrait;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Registers a Tinkers' Antique {@link Material} + molten {@link Fluid} for each hbm metal we
 * bridge in, using {@code TinkerSmeltery.registerOredictMeltingCasting} (ingot/nugget/block/ore
 * melting + casting) and {@code registerToolpartMeltingCasting} (standard tool-part casting) -
 * both of which are the same helpers Tinkers' own material integration uses internally, rather
 * than hand-rolling per-part cast items ourselves.
 */
public class TinkersMaterialRegistry {

    private static final List<NTMTinkerMaterial> MATERIALS = new ArrayList<>();

    // Traits reused across more than one material must be a single shared instance, registered
    // with TinkerRegistry.addTrait() exactly once - Tinkers' Antique treats a second addTrait()
    // call with the same identifier as an error, even if it's a distinct-but-same-named instance.
    // Numbers below are all pulled from hbm's own real per-tool-type ability table and effect
    // lookup tables (radiusAtLevel/rangeAtLevel/powerAtLevel/amountAtLevel/durationAtLevel/
    // radAtLevel - see the plan), not invented tiers.
    private static final SilkTouchTrait SILK_TOUCH = new SilkTouchTrait("ntmcleanroom_silk_touch");
    private static final AutoSmeltTrait AUTO_SMELT = new AutoSmeltTrait("ntmcleanroom_auto_smelt");
    private static final AutoShredTrait AUTO_SHRED = new AutoShredTrait("ntmcleanroom_auto_shred");
    private static final BeheaderTrait BEHEADER = new BeheaderTrait("ntmcleanroom_beheader");
    // Steel and Desh both register Vein Miner at hbm's level 0 (radiusAtLevel[0]=3) - shared instance.
    private static final VeinMinerTrait VEIN_MINER_WEAK = new VeinMinerTrait("ntmcleanroom_vein_miner_weak", 3, 0);
    private static final VeinMinerTrait VEIN_MINER_MEDIUM = new VeinMinerTrait("ntmcleanroom_vein_miner_medium", 5, 2);
    private static final VeinMinerTrait VEIN_MINER_STRONG = new VeinMinerTrait("ntmcleanroom_vein_miner_strong", 10, 6);
    // Steel, Desh and Combine Steel all register Stun at hbm's level 0 (durationAtLevel[0]=2s) - shared instance.
    private static final StunTrait STUN = new StunTrait("ntmcleanroom_stun", 0);
    // Combine Steel and Schrabidium both register Vampire at hbm's level 0 (amountAtLevel[0]=2).
    private static final VampireTrait VAMPIRE = new VampireTrait("ntmcleanroom_vampire", 0);

    static {
        // Material identifiers are prefixed "ntm" since Tinkers' Antique already ships its own
        // built-in "steel"/"lead" materials and rejects a second registration under the same name.
        MATERIALS.add(full("ntmsteel", "Steel", Mats.MAT_STEEL.moltenColor, OreDictManager.STEEL,
                new HeadMaterialStats(500, 2.0F, 7.5F, 2),
                new HandleMaterialStats(1.0F, 500),
                VEIN_MINER_WEAK, STUN, BEHEADER));

        MATERIALS.add(full("ntmtitanium", "Titanium", Mats.MAT_TITANIUM.moltenColor, OreDictManager.TI,
                new HeadMaterialStats(750, 2.5F, 9.0F, 2),
                new HandleMaterialStats(1.1F, 750),
                BEHEADER));

        MATERIALS.add(full("ntmdesh", "Desh", Mats.MAT_DESH.moltenColor, OreDictManager.DESH,
                new HeadMaterialStats(1600, 2.0F, 7.5F, 2),
                new HandleMaterialStats(1.1F, 1600),
                VEIN_MINER_WEAK,
                new AoeTrait("ntmcleanroom_hammer_weak", 1, 0),
                new FlatAoeTrait("ntmcleanroom_hammer_flat_weak", 1, 0),
                SILK_TOUCH,
                new FortuneTrait("ntmcleanroom_fortune_2", 1),
                STUN, BEHEADER));

        MATERIALS.add(full("ntmcmbsteel", "Combine Steel", Mats.MAT_CMB.moltenColor, OreDictManager.CMB,
                new HeadMaterialStats(8500, 55.0F, 40.0F, 4),
                new HandleMaterialStats(1.6F, 8500),
                VEIN_MINER_MEDIUM,
                AUTO_SMELT,
                SILK_TOUCH,
                new FortuneTrait("ntmcleanroom_fortune_3", 2),
                STUN, VAMPIRE, BEHEADER));

        MATERIALS.add(full("ntmschrabidium", "Schrabidium", Mats.MAT_SCHRABIDIUM.moltenColor, OreDictManager.SA326,
                new HeadMaterialStats(10000, 100.0F, 50.0F, 4),
                new HandleMaterialStats(2.0F, 10000),
                VEIN_MINER_STRONG,
                new AoeTrait("ntmcleanroom_hammer_strong", 2, 1),
                new FlatAoeTrait("ntmcleanroom_hammer_flat_strong", 2, 1),
                SILK_TOUCH,
                new FortuneTrait("ntmcleanroom_fortune_5", 4),
                AUTO_SMELT,
                AUTO_SHRED,
                new RadioactiveBladeTrait("ntmcleanroom_radioactive", 0, 1),
                VAMPIRE, BEHEADER));

        // Placeholder materials: meltable/castable, generic default stats, no traits.
        MATERIALS.add(placeholder("ntmlead", "Lead", Mats.MAT_LEAD.moltenColor, OreDictManager.PB));
        MATERIALS.add(placeholder("ntmaluminium", "Aluminium", Mats.MAT_ALUMINIUM.moltenColor, OreDictManager.AL));
        MATERIALS.add(placeholder("ntmbismuthbronze", "Bismuth Bronze", Mats.MAT_BBRONZE.moltenColor, OreDictManager.BBRONZE));
        MATERIALS.add(placeholder("ntmarsenicbronze", "Arsenic Bronze", Mats.MAT_ABRONZE.moltenColor, OreDictManager.ABRONZE));
        MATERIALS.add(placeholder("ntmgunmetal", "Gunmetal", Mats.MAT_GUNMETAL.moltenColor, OreDictManager.GUNMETAL));
        MATERIALS.add(placeholder("ntmweaponsteel", "Weaponsteel", Mats.MAT_WEAPONSTEEL.moltenColor, OreDictManager.WEAPONSTEEL));
    }

    private static NTMTinkerMaterial full(String id, String name, int color, OreDictManager.DictFrame frame,
            HeadMaterialStats head, HandleMaterialStats handle, ITrait... traits) {
        return new NTMTinkerMaterial(id, name, color, frame.ingot(), head, handle, traits);
    }

    private static NTMTinkerMaterial placeholder(String id, String name, int color, OreDictManager.DictFrame frame) {
        return new NTMTinkerMaterial(id, name, color, frame.ingot());
    }

    public static void registerFluidsAndMaterials() {
        Set<ITrait> registeredTraits = new HashSet<>();

        for (NTMTinkerMaterial ntm : MATERIALS) {
            Fluid fluid = new Fluid("molten_" + ntm.id, FluidTextureHandler.FLUID_STILL, FluidTextureHandler.FLUID_FLOW)
                    .setColor(0xFF000000 | ntm.color);
            FluidRegistry.registerFluid(fluid);
            FluidRegistry.addBucketForFluid(fluid);
            ntm.fluid = fluid;

            Material material = new Material(ntm.id, ntm.color);
            material.setFluid(fluid);
            material.setCastable(true);
            material.setCraftable(true);
            ntm.material = material;

            TinkerRegistry.addMaterial(material);
            if (ntm.isFull()) {
                TinkerRegistry.addMaterialStats(material, ntm.headStats, ntm.handleStats);
                for (ITrait trait : ntm.traits) {
                    if (registeredTraits.add(trait)) {
                        TinkerRegistry.addTrait(trait);
                    }
                    // HeadMaterialStats.TYPE ("head"), not null - null buckets the trait as a
                    // fallback applied to ANY part type lacking its own bucket, which is what
                    // leaked these onto tool rods/handles before this fix.
                    TinkerRegistry.addMaterialTrait(material, trait, HeadMaterialStats.TYPE);
                }
            }
        }
    }

    public static void registerMeltingAndCasting() {
        TinkerSmelteryRecipes.load();

        for (NTMTinkerMaterial ntm : MATERIALS) {
            // ntm.ingotOreDict is e.g. "ingotSteel" - we want just the "Steel" suffix, to build
            // "ingot"/"nugget"/"block"/"ore" + suffix ourselves (TinkerSmeltery's own
            // registerOredictMeltingCasting does the same thing internally, but hardcodes its own
            // amount with no way to override it - see TinkerSmelteryRecipes).
            String suffix = ntm.ingotOreDict.substring("ingot".length());
            int mbPerIngot = TinkerSmelteryRecipes.mbPerIngot(ntm.id);

            registerOredictMeltingCasting(ntm.fluid, "ingot" + suffix, mbPerIngot);
            registerOredictMeltingCasting(ntm.fluid, "nugget" + suffix, mbPerIngot / 9);
            registerOredictMeltingCasting(ntm.fluid, "block" + suffix, mbPerIngot * 9);
            registerOredictMeltingCasting(ntm.fluid, "ore" + suffix, mbPerIngot);

            if (ntm.isFull()) {
                TinkerSmeltery.registerToolpartMeltingCasting(ntm.material);
            }
        }
    }

    private static void registerOredictMeltingCasting(Fluid fluid, String oredictName, int amount) {
        TinkerRegistry.registerMelting(oredictName, fluid, amount);
        for (ItemStack stack : OreDictionary.getOres(oredictName)) {
            TinkerRegistry.registerBasinCasting(stack, ItemStack.EMPTY, fluid, amount);
        }
    }

    public static NTMTinkerMaterial get(String id) {
        for (NTMTinkerMaterial ntm : MATERIALS) {
            if (ntm.id.equals(id)) {
                return ntm;
            }
        }
        return null;
    }

    public static List<NTMTinkerMaterial> all() {
        return MATERIALS;
    }
}
