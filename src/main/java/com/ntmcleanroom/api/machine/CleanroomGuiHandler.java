package com.ntmcleanroom.api.machine;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

/**
 * Shared gui handler registered once by NTM: Cleanroom Core. Dispatches to whichever
 * {@link IMachineGuiProvider} was supplied for the {@link MachineDefinition} matching the gui id.
 * Addons don't need to register their own IGuiHandler for machines registered through
 * {@link CleanroomMachineRegistry}.
 */
public final class CleanroomGuiHandler implements IGuiHandler {

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        MachineDefinition definition = CleanroomMachineRegistry.getByGuiId(id);
        if (definition == null || definition.getGuiProvider() == null) return null;
        TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
        return definition.getGuiProvider().getServerGuiElement(player, tileEntity);
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        MachineDefinition definition = CleanroomMachineRegistry.getByGuiId(id);
        if (definition == null || definition.getGuiProvider() == null) return null;
        TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
        return definition.getGuiProvider().getClientGuiElement(player, tileEntity);
    }
}
