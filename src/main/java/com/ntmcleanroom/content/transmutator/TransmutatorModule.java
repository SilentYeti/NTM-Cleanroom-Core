package com.ntmcleanroom.content.transmutator;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.recipes.AssemblyMachineRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.ItemEnums.EnumCircuitType;
import com.hbm.items.ModItems;
import com.hbm.items.block.ItemBlockBase;
import com.hbm.items.machine.ItemBatteryPack;
import com.hbm.main.MainRegistry;
import com.ntmcleanroom.NTMCleanroomCore;
import com.ntmcleanroom.NTMCleanroomTab;
import com.ntmcleanroom.Tags;
import com.ntmcleanroom.api.machine.CleanroomMachineRegistry;
import com.ntmcleanroom.api.machine.MachineDefinition;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

import static com.hbm.inventory.OreDictManager.ANY_RESISTANTALLOY;
import static com.hbm.inventory.OreDictManager.GOLD;
import static com.hbm.inventory.OreDictManager.MAGTUNG;
import static com.hbm.inventory.OreDictManager.NB;
import static com.hbm.inventory.OreDictManager.RUBBER;
import static com.hbm.inventory.OreDictManager.STEEL;
import static com.hbm.inventory.OreDictManager.TI;

/** Registration glue for the restored Schrabidium Transmutator. */
public class TransmutatorModule {

    public static final String TE_REGISTRY_NAME = Tags.MODID + ":tileentity_schrabidium_transmutator";
    private static final String ASSEMBLY_RECIPE_NAME = "ass.ntmcleanroom.schrabidium_transmutator";
    private static final String REDCOIL_RECIPE_NAME = "ass.ntmcleanroom.redcoil_capacitor";
    private static final String EUPHEMIUM_RECIPE_NAME = "ass.ntmcleanroom.euphemium_capacitor";

    public static Block block;
    public static Item itemBlock;

    public static void preInit() {
        block = new MachineSchrabidiumTransmutator(Material.IRON, "machine_schrabidium_transmutator")
                .setHardness(5.0F).setResistance(100.0F)
                .setCreativeTab(NTMCleanroomTab.MACHINES);
        ForgeRegistries.BLOCKS.register(block);

        itemBlock = new ItemBlockBase(block).setRegistryName(block.getRegistryName());
        ForgeRegistries.ITEMS.register(itemBlock);

        CleanroomMachineRegistry.registerMachine(new MachineDefinition(
                Tags.MODID,
                TE_REGISTRY_NAME,
                TileEntityMachineSchrabidiumTransmutator.class,
                new SchrabidiumTransmutatorGuiProvider()));

        // Plugs our own recipe handler into hbm's SerializableRecipe pipeline, same as every other hbm
        // machine: it gets deleteRecipes()/registerDefaults() called and its own JSON file written to
        // config/hbmRecipes during hbm's postInit (SerializableRecipe.initialize()), and is free to be
        // hand-edited or replaced by players exactly like the built-in recipe files.
        SerializableRecipe.recipeHandlers.add(SchrabidiumTransmutatorRecipes.INSTANCE);

        // Covers the fresh-install case: no hbmAssemblyMachine.json yet, so hbm takes its
        // registerDefaults() branch and calls this for every handler it (re)builds.
        SerializableRecipe.additionalListeners.add(recipeClassName -> {
            if ("AssemblyMachineRecipes".equals(recipeClassName)) {
                AssemblyMachineRecipes.INSTANCE.register(buildAssemblyRecipe());
                AssemblyMachineRecipes.INSTANCE.register(buildRedcoilCapacitorRecipe());
                AssemblyMachineRecipes.INSTANCE.register(buildEuphemiumCapacitorRecipe());
            }
        });
    }

    public static void init() {
        // hbm only calls registerDefaults() (and therefore additionalListeners, above) for a
        // SerializableRecipe handler when its JSON file DOESN'T exist yet. In a modpack that has already
        // been run before - i.e. almost any real-world pack this mod gets dropped into -
        // hbmAssemblyMachine.json already exists, hbm just reads it verbatim, and that listener never
        // fires, so our craft recipes would silently never get added. Handle that case up front by
        // injecting them straight into the existing file (whichever ones aren't already there) before
        // hbm's postInit reads it.
        //
        // This has to run in init(), not preInit(): the ingredients (hbm's own items/blocks) aren't
        // guaranteed to be registered yet during preInit, but are always registered by the end of init,
        // which - thanks to "required-after:hbm" - is guaranteed to run after hbm's own init and before
        // hbm's postInit (where the file actually gets read).
        ensureRecipesInExistingFile(buildAssemblyRecipe(), buildRedcoilCapacitorRecipe(), buildEuphemiumCapacitorRecipe());
    }

    private static GenericRecipe buildAssemblyRecipe() {
        // Assembly Machine recipe to craft the machine itself - ported from hbm's old AssemblerRecipes entry.
        return new GenericRecipe(ASSEMBLY_RECIPE_NAME)
                .setup(500, 20_000)
                .outputItems(new ItemStack(block))
                .inputItems(
                        new OreDictStack(MAGTUNG.ingot(), 1),
                        new OreDictStack(TI.ingot(), 24),
                        new OreDictStack(STEEL.plate(), 18),
                        new OreDictStack(STEEL.plateWelded(), 12),
                        new ComparableStack(ModItems.plate_desh, 6),
                        new OreDictStack(RUBBER.ingot(), 8),
                        new ComparableStack(ModItems.battery_pack, 1, ItemBatteryPack.EnumBatteryPack.BATTERY_LITHIUM),
                        new ComparableStack(ModItems.circuit, 2, EnumCircuitType.ADVANCED.ordinal()));
    }

    private static GenericRecipe buildRedcoilCapacitorRecipe() {
        // Ported from hbm's old AssemblerRecipes entry. The original used Advanced Alloy fine wire/coil,
        // both since restructured away (see the "resistant alloy" ore dict group and the current coil
        // item list); substituted with a resistant-alloy wire and a magnetized tungsten coil.
        return new GenericRecipe(REDCOIL_RECIPE_NAME)
                .setup(200, 2_000)
                .outputItems(new ItemStack(ModItems.redcoil_capacitor))
                .inputItems(
                        new OreDictStack(GOLD.plate(), 3),
                        new ComparableStack(ModItems.fuse, 1),
                        new OreDictStack(ANY_RESISTANTALLOY.wireFine(), 4),
                        new ComparableStack(ModItems.coil_magnetized_tungsten, 6),
                        new ComparableStack(Blocks.REDSTONE_BLOCK, 2));
    }

    private static GenericRecipe buildEuphemiumCapacitorRecipe() {
        // Ported from hbm's old AssemblerRecipes entry, unchanged ingredient-wise.
        return new GenericRecipe(EUPHEMIUM_RECIPE_NAME)
                .setup(600, 6_000)
                .outputItems(new ItemStack(ModItems.euphemium_capacitor))
                .inputItems(
                        new OreDictStack(NB.ingot(), 4),
                        new ComparableStack(ModItems.redcoil_capacitor, 1),
                        new ComparableStack(ModItems.ingot_euphemium, 4),
                        new ComparableStack(ModItems.circuit, 8, EnumCircuitType.CAPACITOR_BOARD.ordinal()),
                        new ComparableStack(ModItems.powder_nitan_mix, 18));
    }

    /**
     * If config/hbmRecipes/hbmAssemblyMachine.json already exists (i.e. hbm won't take its
     * "no file, use defaults" branch this run), splice in whichever of these recipes aren't already
     * present - reusing hbm's own writeRecipe() so each entry matches its format exactly.
     */
    private static void ensureRecipesInExistingFile(GenericRecipe... recipesToEnsure) {
        File recipeFile = new File(MainRegistry.configDir, "hbmRecipes" + File.separator + "hbmAssemblyMachine.json");
        if (!recipeFile.isFile()) {
            return;
        }

        try {
            String json = new String(Files.readAllBytes(recipeFile.toPath()), StandardCharsets.UTF_8);
            JsonObject root = new JsonParser().parse(json).getAsJsonObject();
            JsonArray recipes = root.getAsJsonArray("recipes");
            if (recipes == null) {
                return;
            }

            Set<String> existingNames = new HashSet<>();
            for (JsonElement element : recipes) {
                if (element.isJsonObject() && element.getAsJsonObject().has("name")) {
                    existingNames.add(element.getAsJsonObject().get("name").getAsString());
                }
            }

            boolean changed = false;
            for (GenericRecipe recipe : recipesToEnsure) {
                if (existingNames.contains(recipe.getInternalName())) {
                    continue; // already present
                }

                StringWriter buffer = new StringWriter();
                JsonWriter writer = new JsonWriter(buffer);
                writer.beginObject();
                AssemblyMachineRecipes.INSTANCE.writeRecipe(recipe, writer);
                writer.endObject();
                writer.close();
                recipes.add(new JsonParser().parse(buffer.toString()));
                changed = true;

                NTMCleanroomCore.LOGGER.info("Added the {} recipe to the existing {}", recipe.getInternalName(), recipeFile.getName());
            }

            if (changed) {
                try (FileWriter out = new FileWriter(recipeFile)) {
                    new GsonBuilder().setPrettyPrinting().create().toJson(root, out);
                }
            }
        } catch (Exception e) {
            NTMCleanroomCore.LOGGER.error("Failed to add recipes to {}", recipeFile, e);
        }
    }

    @SideOnly(Side.CLIENT)
    public static void registerModels() {
        ModelLoader.setCustomModelResourceLocation(itemBlock, 0,
                new ModelResourceLocation(block.getRegistryName(), "normal"));
    }
}
