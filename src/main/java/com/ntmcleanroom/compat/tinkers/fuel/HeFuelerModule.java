package com.ntmcleanroom.compat.tinkers.fuel;

import com.ntmcleanroom.NTMCleanroomTab;
import com.ntmcleanroom.Tags;
import com.ntmcleanroom.api.machine.CleanroomMachineRegistry;
import com.ntmcleanroom.api.machine.MachineDefinition;
import com.ntmcleanroom.compat.tinkers.FluidTextureHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.tconstruct.library.TinkerRegistry;

/** Registration glue for the HE-powered smeltery fueler block and its fuel fluid. */
public class HeFuelerModule {

    /** How many ticks of smeltery heat 1 mB of this fuel provides - matches vanilla lava's rate (50 mB = 100 ticks). */
    private static final int FUEL_DURATION_PER_MB = 2;

    public static Fluid heFuelFluid;
    public static Block block;
    public static Item itemBlock;

    public static void preInit() {
        heFuelFluid = new Fluid("ntmcleanroom_he_fuel", FluidTextureHandler.FLUID_STILL, FluidTextureHandler.FLUID_FLOW)
                .setColor(0xFF60C0FF);
        FluidRegistry.registerFluid(heFuelFluid);
        FluidRegistry.addBucketForFluid(heFuelFluid);

        block = new BlockHeSmelteryFueler("he_smeltery_fueler")
                .setHardness(5.0F).setResistance(50.0F)
                .setCreativeTab(NTMCleanroomTab.MACHINES);
        ForgeRegistries.BLOCKS.register(block);

        itemBlock = new ItemBlock(block).setRegistryName(block.getRegistryName());
        ForgeRegistries.ITEMS.register(itemBlock);

        CleanroomMachineRegistry.registerMachine(new MachineDefinition(
                Tags.MODID,
                Tags.MODID + ":tileentity_he_smeltery_fueler",
                TileEntityHeSmelteryFueler.class));
    }

    public static void init() {
        TinkerRegistry.registerSmelteryFuel(new FluidStack(heFuelFluid, 1), FUEL_DURATION_PER_MB);
    }

    public static void postInit() {
    }

    @SideOnly(Side.CLIENT)
    public static void registerModels() {
        ModelLoader.setCustomModelResourceLocation(itemBlock, 0,
                new ModelResourceLocation(block.getRegistryName(), "normal"));
    }
}
