package com.ntmcleanroom.compat.tinkers.ability;

import com.hbm.handler.ability.ToolPreset;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

/** Client->server request to cycle the held tool's active ability preset (see {@link AbilitySlots#cycle}), sent on right-click. */
public class CycleAbilityMessage implements net.minecraftforge.fml.common.network.simpleimpl.IMessage {

    public CycleAbilityMessage() {}

    @Override
    public void toBytes(ByteBuf buf) {}

    @Override
    public void fromBytes(ByteBuf buf) {}

    public static class Handler implements net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler<CycleAbilityMessage, net.minecraftforge.fml.common.network.simpleimpl.IMessage> {
        @Override
        public net.minecraftforge.fml.common.network.simpleimpl.IMessage onMessage(CycleAbilityMessage message, net.minecraftforge.fml.common.network.simpleimpl.MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> handle(player));
            return null;
        }

        private void handle(EntityPlayerMP player) {
            ItemStack stack = player.getHeldItemMainhand();
            if (stack.isEmpty()) {
                return;
            }

            ToolPreset preset = AbilitySlots.cycle(stack, player.isSneaking());
            if (preset == null) {
                return;
            }

            player.sendStatusMessage(preset.getMessage(), true);
        }
    }
}
