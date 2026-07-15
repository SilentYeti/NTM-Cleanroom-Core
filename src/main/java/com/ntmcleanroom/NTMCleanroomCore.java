package com.ntmcleanroom;

import com.ntmcleanroom.api.machine.CleanroomGuiHandler;
import com.ntmcleanroom.content.transmutator.TransmutatorModule;
import com.ntmcleanroom.init.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Tags.MODID, version = Tags.VERSION, name = Tags.MODNAME, acceptedMinecraftVersions = "[1.12.2]",
        dependencies = "required-after:hbm@[2.5.0.0,);")
public class NTMCleanroomCore {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MODID);

    @Mod.Instance(Tags.MODID)
    public static NTMCleanroomCore instance;

    @SidedProxy(clientSide = "com.ntmcleanroom.init.proxy.ClientProxy", serverSide = "com.ntmcleanroom.init.proxy.CommonProxy")
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // Shared gui handler addons plug into via CleanroomMachineRegistry - see that class for usage.
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new CleanroomGuiHandler());

        TransmutatorModule.preInit();

        proxy.preInit(event);
        proxy.registerRenderInfo();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }
}
