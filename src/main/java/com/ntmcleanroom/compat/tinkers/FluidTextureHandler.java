package com.ntmcleanroom.compat.tinkers;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Stitches the shared molten-metal still/flow sprites into the blocks texture map. Without this,
 * {@link net.minecraftforge.fluids.Fluid} never gets its icons registered and renders as the
 * missing-texture checkerboard - registering the {@link Fluid} itself is not enough. Also used by
 * the HE fuel fluid, which reuses the same two sprite locations.
 *
 * <p>Points at Tinkers' Antique's own animated "blocks/fluids/molten_metal[.png/_flow.png]"
 * sprites (the ones its own vanilla-metal fluids use, tinted per-material via
 * {@link net.minecraftforge.fluids.Fluid#setColor}) instead of a bespoke texture - this is what
 * gives Tinkers' own molten metals their glowing, lava-like look, which our own flat placeholder
 * sprite didn't have.
 */
@SideOnly(Side.CLIENT)
public final class FluidTextureHandler {

    public static final ResourceLocation FLUID_STILL = new ResourceLocation("tconstruct", "blocks/fluids/molten_metal");
    public static final ResourceLocation FLUID_FLOW = new ResourceLocation("tconstruct", "blocks/fluids/molten_metal_flow");

    FluidTextureHandler() {}

    @SubscribeEvent
    public void onTextureStitch(TextureStitchEvent.Pre event) {
        event.getMap().registerSprite(FLUID_STILL);
        event.getMap().registerSprite(FLUID_FLOW);
    }
}
