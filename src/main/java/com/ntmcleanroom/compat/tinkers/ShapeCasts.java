package com.ntmcleanroom.compat.tinkers;

import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.items.ModItems;
import com.ntmcleanroom.Tags;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import slimeknights.tconstruct.library.TinkerRegistry;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Casts for hbm's own material shapes, matching hbm's real Foundry/Mold casting system
 * ({@code com.hbm.items.machine.ItemMold}). Most shapes are generic and oredict-templated exactly
 * like Tinkers' own casts - one reusable cast item works for any material with a matching
 * {@code "<shapePrefix><MaterialName>"} oredict entry (see the plan). A few (blade/blades) are
 * hardcoded by hbm to a handful of specific materials instead of being oredict-generic, so those
 * stay directly wired. {@code cast_stamp}/{@code cast_c9}/{@code cast_c50} are genuine
 * placeholders: real items, no recipe attempt (hbm hardcodes those molds to specific materials
 * entirely outside our bridged set).
 */
public class ShapeCasts {

    /** Shape id (also our item's name suffix and config key) -> hbm's oredict shape prefix. */
    private static final Map<String, String> GENERIC_SHAPES = new LinkedHashMap<>();
    private static final String[] PLACEHOLDER_ONLY_SHAPES = {"stamp", "c9", "c50"};

    static {
        // "plate" -> hbm's own "Cast <Material> Plate" family (oredict "plateTriple"+suffix),
        // NOT the plain plate_<material> item (a different item hbm makes via press/rolling).
        GENERIC_SHAPES.put("plate", "plateTriple");
        GENERIC_SHAPES.put("billet", "billet");
        GENERIC_SHAPES.put("wire", "wireFine");
        GENERIC_SHAPES.put("shell", "shell");
        GENERIC_SHAPES.put("pipe", "ntmpipe");
        GENERIC_SHAPES.put("barrel_light", "barrelLight");
        GENERIC_SHAPES.put("barrel_heavy", "barrelHeavy");
        GENERIC_SHAPES.put("receiver_light", "receiverLight");
        GENERIC_SHAPES.put("receiver_heavy", "receiverHeavy");
        GENERIC_SHAPES.put("mechanism", "gunMechanism");
        GENERIC_SHAPES.put("stock", "stock");
        GENERIC_SHAPES.put("grip", "grip");
    }

    /** Every registered cast item, keyed by shape id - covers the generic ones, the hardcoded-material ones, and the placeholder-only ones. */
    private static final Map<String, Item> ALL_CASTS = new LinkedHashMap<>();

    public static Item castBlade;
    public static Item castBlades;
    public static Item castDenseWire;

    /** Creates and registers the cast items. Must run in preInit, before {@link #registerModels()}. */
    public static void registerItems() {
        for (String shape : GENERIC_SHAPES.keySet()) {
            ALL_CASTS.put(shape, register("cast_" + shape));
        }

        castBlade = register("cast_blade");
        castBlades = register("cast_blades");
        castDenseWire = register("cast_dense_wire");
        ALL_CASTS.put("blade", castBlade);
        ALL_CASTS.put("blades", castBlades);
        ALL_CASTS.put("dense_wire", castDenseWire);

        for (String shape : PLACEHOLDER_ONLY_SHAPES) {
            ALL_CASTS.put(shape, register("cast_" + shape));
        }
    }

    private static Item register(String name) {
        Item item = new ItemShapeCast(name);
        ForgeRegistries.ITEMS.register(item);
        return item;
    }

    /**
     * Registers the shape-casting recipes. Runs in init (not preInit): needs both our own fluids
     * (registered in preInit) and hbm's own shape items, which aren't guaranteed registered until
     * hbm's init has completed - same reasoning as {@code TransmutatorModule}'s assembly recipe.
     */
    public static void registerCasting() {
        for (NTMTinkerMaterial ntm : TinkersMaterialRegistry.all()) {
            if (ntm.fluid == null) {
                continue;
            }
            String suffix = ntm.ingotOreDict.substring("ingot".length());
            for (Map.Entry<String, String> entry : GENERIC_SHAPES.entrySet()) {
                registerGenericCasting(ntm.fluid, entry.getKey(), entry.getValue() + suffix);
            }
        }

        // Every other hbm material its own Foundry could shape (not just our 5 full + 6
        // placeholder Tinkers materials above) - see FoundryShapeBridge.
        for (Map.Entry<String, Fluid> extra : FoundryShapeBridge.extraFluids().entrySet()) {
            String suffix = extra.getKey();
            for (Map.Entry<String, String> entry : GENERIC_SHAPES.entrySet()) {
                registerGenericCasting(extra.getValue(), entry.getKey(), entry.getValue() + suffix);
            }
        }

        // Not oredict-generic in hbm - hardcoded to a handful of specific materials instead.
        castItem("ntmtitanium", "blade", castBlade, ModItems.blade_titanium);
        castItem("ntmsteel", "blades", castBlades, ModItems.blades_steel);
        castItem("ntmtitanium", "blades", castBlades, ModItems.blades_titanium);
        // hbm gives "blades" only to Steel and Titanium - no Desh recipe here (v2 incorrectly had one).

        castDenseWire("ntmsteel", Mats.MAT_STEEL);
        castDenseWire("ntmtitanium", Mats.MAT_TITANIUM);
        castDenseWire("ntmdesh", Mats.MAT_DESH);
        castDenseWire("ntmcmbsteel", Mats.MAT_CMB);
        castDenseWire("ntmschrabidium", Mats.MAT_SCHRABIDIUM);

        // cast_stamp/c9/c50 are intentional blank placeholders - hbm hardcodes those molds to
        // specific materials entirely outside our bridged set, so there's nothing to wire here.
    }

    private static void registerGenericCasting(Fluid fluid, String shape, String oreDictName) {
        Item cast = ALL_CASTS.get(shape);
        int amount = TinkerSmelteryRecipes.mbPerShapeCast(shape);
        for (ItemStack output : OreDictionary.getOres(oreDictName)) {
            TinkerRegistry.registerTableCasting(output.copy(), new ItemStack(cast), fluid, amount);
        }
    }

    private static void castItem(String materialId, String shapeName, Item cast, Item output) {
        if (output == null) {
            return;
        }
        registerTableCasting(materialId, shapeName, cast, new ItemStack(output));
    }

    private static void castDenseWire(String materialId, NTMMaterial hbmMaterial) {
        registerTableCasting(materialId, "dense_wire", castDenseWire, new ItemStack(ModItems.wire_dense, 1, hbmMaterial.id));
    }

    private static void registerTableCasting(String materialId, String shapeName, Item cast, ItemStack output) {
        NTMTinkerMaterial ntm = TinkersMaterialRegistry.get(materialId);
        if (ntm == null || ntm.fluid == null) {
            return;
        }
        int amount = TinkerSmelteryRecipes.mbPerShapeCast(shapeName);
        TinkerRegistry.registerTableCasting(output, new ItemStack(cast), ntm.fluid, amount);
    }

    @SideOnly(Side.CLIENT)
    public static void registerModels() {
        for (Item item : ALL_CASTS.values()) {
            registerModel(item);
        }
    }

    @SideOnly(Side.CLIENT)
    private static void registerModel(Item item) {
        ModelLoader.setCustomModelResourceLocation(item, 0,
                new ModelResourceLocation(new ResourceLocation(Tags.MODID, item.getRegistryName().getPath()), "inventory"));
    }
}
