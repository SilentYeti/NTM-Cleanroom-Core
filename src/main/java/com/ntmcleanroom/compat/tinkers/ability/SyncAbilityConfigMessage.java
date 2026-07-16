package com.ntmcleanroom.compat.tinkers.ability;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/** Client->server sync after {@link GuiNTMToolAbility} edits a tool's ability configuration (mirrors hbm's own {@code NBTItemControlPacket}, since our tool's Item can't implement hbm's IItemControlReceiver interface). */
public class SyncAbilityConfigMessage implements IMessage {

    private NBTTagCompound tag;

    public SyncAbilityConfigMessage() {}

    public SyncAbilityConfigMessage(NBTTagCompound tag) {
        this.tag = tag;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, tag);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        tag = ByteBufUtils.readTag(buf);
    }

    public static class Handler implements IMessageHandler<SyncAbilityConfigMessage, IMessage> {
        @Override
        public IMessage onMessage(SyncAbilityConfigMessage message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> handle(message, player));
            return null;
        }

        private void handle(SyncAbilityConfigMessage message, EntityPlayerMP player) {
            ItemStack stack = player.getHeldItemMainhand();
            if (!stack.isEmpty() && message.tag != null) {
                stack.setTagCompound(message.tag);
            }
        }
    }
}
