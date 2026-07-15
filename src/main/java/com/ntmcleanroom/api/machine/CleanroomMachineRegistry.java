package com.ntmcleanroom.api.machine;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Entry point for addons to hook their own custom NTM machines into NTM: Cleanroom Core.
 * <p>
 * Usage from an addon:
 * <pre>{@code
 * // In your @Mod annotation:
 * // dependencies = "required-after:ntmcleanroom;required-after:hbm"
 *
 * // In your preInit/init:
 * CleanroomMachineRegistry.registerMachine(
 *         new MachineDefinition("mymod", "mymod:my_machine", MyMachineTileEntity.class, myGuiProvider));
 * }</pre>
 * This registers the tile entity and, if a gui provider was supplied, wires it into the core's
 * shared gui handler. To open the gui:
 * <pre>{@code
 * int guiId = CleanroomMachineRegistry.getGuiId("mymod:my_machine");
 * player.openGui(NTMCleanroomCore.instance, guiId, world, pos.getX(), pos.getY(), pos.getZ());
 * }</pre>
 */
public final class CleanroomMachineRegistry {

    private static final Map<String, MachineDefinition> MACHINES_BY_REGISTRY_NAME = new LinkedHashMap<>();
    private static final Map<Integer, MachineDefinition> MACHINES_BY_GUI_ID = new LinkedHashMap<>();
    private static final Map<String, Integer> GUI_IDS_BY_REGISTRY_NAME = new LinkedHashMap<>();
    private static int nextGuiId = 0;

    private CleanroomMachineRegistry() {}

    public static void registerMachine(MachineDefinition definition) {
        String registryName = definition.getRegistryName();
        if (MACHINES_BY_REGISTRY_NAME.containsKey(registryName)) {
            throw new IllegalArgumentException("A machine is already registered under the name \"" + registryName + "\"");
        }

        GameRegistry.registerTileEntity(definition.getTileEntityClass(), new ResourceLocation(registryName));
        MACHINES_BY_REGISTRY_NAME.put(registryName, definition);

        if (definition.getGuiProvider() != null) {
            int guiId = nextGuiId++;
            MACHINES_BY_GUI_ID.put(guiId, definition);
            GUI_IDS_BY_REGISTRY_NAME.put(registryName, guiId);
        }
    }

    /** Returns the gui id assigned to the given machine. Throws if it was registered without a gui provider. */
    public static int getGuiId(String registryName) {
        Integer guiId = GUI_IDS_BY_REGISTRY_NAME.get(registryName);
        if (guiId == null) {
            throw new IllegalArgumentException("No gui-enabled machine registered under the name \"" + registryName + "\"");
        }
        return guiId;
    }

    static MachineDefinition getByGuiId(int guiId) {
        return MACHINES_BY_GUI_ID.get(guiId);
    }

    public static List<MachineDefinition> getAllMachines() {
        return Collections.unmodifiableList(new ArrayList<>(MACHINES_BY_REGISTRY_NAME.values()));
    }
}
