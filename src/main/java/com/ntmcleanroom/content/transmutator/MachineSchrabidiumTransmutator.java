package com.ntmcleanroom.content.transmutator;

import com.ntmcleanroom.NTMCleanroomCore;
import com.ntmcleanroom.api.machine.CleanroomMachineRegistry;
import com.hbm.lib.InventoryHelper;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Restored from NTM-CE (removed upstream); see {@link TransmutatorModule} for registration
 * and {@link SchrabidiumTransmutatorRecipes} for the input/output recipe table.
 */
public class MachineSchrabidiumTransmutator extends BlockContainer {

    public MachineSchrabidiumTransmutator(Material materialIn, String s) {
        super(materialIn);
        this.setTranslationKey(s);
        this.setRegistryName(s);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityMachineSchrabidiumTransmutator();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return true;
        } else if (!player.isSneaking()) {
            int guiId = CleanroomMachineRegistry.getGuiId(TransmutatorModule.TE_REGISTRY_NAME);
            player.openGui(NTMCleanroomCore.instance, guiId, world, pos.getX(), pos.getY(), pos.getZ());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        InventoryHelper.dropInventoryItems(worldIn, pos, worldIn.getTileEntity(pos));
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }
}
