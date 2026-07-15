package com.ntmcleanroom.api.machine;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Supplies the server Container and client Gui(Screen) for a {@link MachineDefinition}.
 * Implement this if your machine has a gui; the core's shared {@link CleanroomGuiHandler}
 * will dispatch to it, so you don't need to register your own IGuiHandler.
 */
public interface IMachineGuiProvider {

    Object getServerGuiElement(EntityPlayer player, TileEntity tileEntity);

    @SideOnly(Side.CLIENT)
    Object getClientGuiElement(EntityPlayer player, TileEntity tileEntity);
}
