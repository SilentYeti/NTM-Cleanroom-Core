package com.ntmcleanroom.compat.tinkers.fuel;

import com.hbm.api.block.IToolable;
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
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

/** Placeholder-textured block; see {@link TileEntityHeSmelteryFueler} for the actual behavior. */
public class BlockHeSmelteryFueler extends BlockContainer implements IToolable {

    public BlockHeSmelteryFueler(String registryName) {
        super(Material.IRON);
        this.setTranslationKey(registryName);
        this.setRegistryName(registryName);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityHeSmelteryFueler();
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        InventoryHelper.dropInventoryItems(worldIn, pos, worldIn.getTileEntity(pos));
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, EnumFacing side, float fX, float fY, float fZ, EnumHand hand, IToolable.ToolType tool) {
        if (tool != IToolable.ToolType.SCREWDRIVER) {
            return false;
        }

        if (world.isRemote) {
            return true;
        }

        TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
        if (!(te instanceof TileEntityHeSmelteryFueler)) {
            return false;
        }

        TileEntityHeSmelteryFueler fueler = (TileEntityHeSmelteryFueler) te;
        fueler.cycleTier();
        player.sendStatusMessage(new TextComponentString("Speed: " + fueler.getTier().label), true);
        return true;
    }
}
