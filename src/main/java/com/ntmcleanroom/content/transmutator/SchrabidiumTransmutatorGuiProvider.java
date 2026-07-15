package com.ntmcleanroom.content.transmutator;

import com.ntmcleanroom.api.machine.IMachineGuiProvider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Wires the transmutator's container/gui into the core's shared {@code CleanroomGuiHandler}. */
public class SchrabidiumTransmutatorGuiProvider implements IMachineGuiProvider {

    @Override
    public Object getServerGuiElement(EntityPlayer player, TileEntity tileEntity) {
        return new ContainerMachineSchrabidiumTransmutator(player.inventory, (TileEntityMachineSchrabidiumTransmutator) tileEntity);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Object getClientGuiElement(EntityPlayer player, TileEntity tileEntity) {
        return new GUIMachineSchrabidiumTransmutator(player.inventory, (TileEntityMachineSchrabidiumTransmutator) tileEntity);
    }
}
