package com.ntmcleanroom.init.proxy;

import com.ntmcleanroom.compat.tinkers.TinkersCompat;
import com.ntmcleanroom.content.transmutator.TransmutatorModule;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        TinkersCompat.registerKeybinds();
    }

    @Override
    public void registerRenderInfo() {
        TransmutatorModule.registerModels();
        TinkersCompat.registerModels();
    }
}
