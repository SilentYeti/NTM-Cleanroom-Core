package com.ntmcleanroom.compat.tinkers.ability;

import com.ntmcleanroom.Tags;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

/** Network channel carrying ability-selection messages (right-click cycle, GUI edits) to the server. */
public final class AbilityNetworking {

    public static SimpleNetworkWrapper CHANNEL;

    private AbilityNetworking() {}

    public static void init() {
        CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(Tags.MODID + "_abilities");
        CHANNEL.registerMessage(CycleAbilityMessage.Handler.class, CycleAbilityMessage.class, 0, Side.SERVER);
        CHANNEL.registerMessage(SyncAbilityConfigMessage.Handler.class, SyncAbilityConfigMessage.class, 1, Side.SERVER);
    }
}
