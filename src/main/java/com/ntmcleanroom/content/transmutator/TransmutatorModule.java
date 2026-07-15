package com.ntmcleanroom.content.transmutator;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.recipes.AssemblyMachineRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.ItemEnums.EnumCircuitType;
import com.hbm.items.ModItems;
import com.ntmcleanroom.NTMCleanroomTab;
import com.ntmcleanroom.Tags;
import com.ntmcleanroom.api.machine.CleanroomMachineRegistry;
import com.ntmcleanroom.api.machine.MachineDefinition;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static com.hbm.inventory.OreDictManager.ALLOY;
import static com.hbm.inventory.OreDictManager.MAGTUNG;
import static com.hbm.inventory.OreDictManager.RUBBER;
import static com.hbm.inventory.OreDictManager.STEEL;
import static com.hbm.inventory.OreDictManager.TI;

/** Registration glue for the restored Schrabidium Transmutator. */
public class TransmutatorModule {

    public static final String TE_REGISTRY_NAME = Tags.MODID + ":tileentity_schrabidium_transmutator";

    public static Block block;
    public static Item itemBlock;

    public static void preInit() {
        block = new MachineSchrabidiumTransmutator(Material.IRON, "machine_schrabidium_transmutator")
                .setHardness(5.0F).setResistance(100.0F)
                .setCreativeTab(NTMCleanroomTab.MACHINES);
        ForgeRegistries.BLOCKS.register(block);

        itemBlock = new ItemBlock(block).setRegistryName(block.getRegistryName());
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

        // hbm rebuilds AssemblyMachineRecipes (deleteRecipes() + registerDefaults()) from scratch during
        // its own postInit; registering our recipe any earlier just gets wiped out afterwards. This
        // listener is hbm's own documented, mixin-free hook for addons to add recipes at the right moment.
        SerializableRecipe.additionalListeners.add(recipeClassName -> {
            if ("AssemblyMachineRecipes".equals(recipeClassName)) {
                registerAssemblyRecipe();
            }
        });
    }

    private static void registerAssemblyRecipe() {
        // Assembly Machine recipe to craft the machine itself - ported from hbm's old AssemblerRecipes entry.
        AssemblyMachineRecipes.INSTANCE.register(new GenericRecipe("ass.ntmcleanroom.schrabidium_transmutator")
                .setup(500, 20_000)
                .outputItems(new ItemStack(block))
                .inputItems(
                        new OreDictStack(MAGTUNG.ingot(), 1),
                        new OreDictStack(TI.ingot(), 24),
                        new OreDictStack(ALLOY.plate(), 18),
                        new OreDictStack(STEEL.plateWelded(), 12),
                        new ComparableStack(ModItems.plate_desh, 6),
                        new OreDictStack(RUBBER.ingot(), 8),
                        new ComparableStack(ModBlocks.machine_battery, 5),
                        new ComparableStack(ModItems.circuit, 2, EnumCircuitType.ADVANCED.ordinal())));
    }

    @SideOnly(Side.CLIENT)
    public static void registerModels() {
        ModelLoader.setCustomModelResourceLocation(itemBlock, 0,
                new ModelResourceLocation(block.getRegistryName(), "normal"));
    }
}
