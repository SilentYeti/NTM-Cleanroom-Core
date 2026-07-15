package com.ntmcleanroom.api.machine;

import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;

/**
 * Describes a custom machine an addon wants to hook into NTM: Cleanroom Core.
 * Pass one of these to {@link CleanroomMachineRegistry#registerMachine(MachineDefinition)}.
 */
public final class MachineDefinition {

    private final String ownerModId;
    private final String registryName;
    private final Class<? extends TileEntity> tileEntityClass;
    @Nullable
    private final IMachineGuiProvider guiProvider;

    public MachineDefinition(String ownerModId, String registryName, Class<? extends TileEntity> tileEntityClass) {
        this(ownerModId, registryName, tileEntityClass, null);
    }

    public MachineDefinition(String ownerModId, String registryName, Class<? extends TileEntity> tileEntityClass,
            @Nullable IMachineGuiProvider guiProvider) {
        this.ownerModId = ownerModId;
        this.registryName = registryName;
        this.tileEntityClass = tileEntityClass;
        this.guiProvider = guiProvider;
    }

    /** The modid of the addon that owns this machine. */
    public String getOwnerModId() {
        return ownerModId;
    }

    /** Unique registry name for the tile entity, e.g. "mymod:my_machine". */
    public String getRegistryName() {
        return registryName;
    }

    public Class<? extends TileEntity> getTileEntityClass() {
        return tileEntityClass;
    }

    /** Null if this machine has no gui. */
    @Nullable
    public IMachineGuiProvider getGuiProvider() {
        return guiProvider;
    }
}
