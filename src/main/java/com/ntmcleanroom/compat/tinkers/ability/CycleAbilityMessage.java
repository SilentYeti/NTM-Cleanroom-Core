package com.ntmcleanroom.compat.tinkers.ability;

import com.hbm.handler.ability.ToolPreset;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.PlayerInformPacketLegacy;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;

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

            // Matches hbm's own ItemToolAbility.handleKeybind exactly: a real hbm "informer"
            // packet (top-left popup, not vanilla action bar) plus its confirmation ding, pitched
            // down when the tool's abilities were just turned off.
            PacketDispatcher.wrapper.sendTo(new PlayerInformPacketLegacy(AbilitySlots.getMessage(preset), 11), player);
            player.getServerWorld().playSound(null, player.posX, player.posY, player.posZ,
                    SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.25F,
                    preset.isNone() ? 0.75F : 1.25F);
        }
    }
}
