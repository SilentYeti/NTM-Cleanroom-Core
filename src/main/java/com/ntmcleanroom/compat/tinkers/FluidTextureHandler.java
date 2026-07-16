package com.ntmcleanroom.compat.tinkers;

import com.ntmcleanroom.Tags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Stitches the shared placeholder molten-metal still/flow sprites into the blocks texture map.
 * Without this, {@link net.minecraftforge.fluids.Fluid} never gets its icons registered and
 * renders as the missing-texture checkerboard - registering the {@link Fluid} itself is not
 * enough. Also used by the HE fuel fluid, which reuses the same two sprite locations.
 */
@SideOnly(Side.CLIENT)
public final class FluidTextureHandler {

    public static final ResourceLocation FLUID_STILL = new ResourceLocation(Tags.MODID, "fluids/molten_metal_still");
    public static final ResourceLocation FLUID_FLOW = new ResourceLocation(Tags.MODID, "fluids/molten_metal_flow");

    FluidTextureHandler() {}

    @SubscribeEvent
    public void onTextureStitch(TextureStitchEvent.Pre event) {
        event.getMap().registerSprite(FLUID_STILL);
        event.getMap().registerSprite(FLUID_FLOW);
    }
}
