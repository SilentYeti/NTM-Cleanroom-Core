package com.ntmcleanroom.compat.tinkers.ability;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

/**
 * Replicates hbm's real trigger for tool ability selection. hbm does NOT hook a normal item-use
 * event for this - {@code HbmKeybinds} registers a {@code KeyBinding} whose default keycode
 * literally equals vanilla's "Use Item" (right mouse button) and polls its raw down-state every
 * client tick, independent of whether a block is being looked at. That distinction matters:
 * {@code PlayerInteractEvent.RightClickItem} (the first attempt here) only fires when *not*
 * looking at any block, which in practice is rare - the player is almost always looking at some
 * block, so the handler effectively never fired. Polling the raw key state instead fires
 * regardless of what's targeted, matching hbm exactly.
 */
@SideOnly(Side.CLIENT)
public class AbilityRightClickHandler {

    private boolean wasDown;

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new AbilityRightClickHandler());
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || mc.currentScreen != null) {
            wasDown = false;
            return;
        }

        boolean isDown = mc.gameSettings.keyBindUseItem.isKeyDown();
        boolean justPressed = isDown && !wasDown;
        wasDown = isDown;

        if (!justPressed) {
            return;
        }

        ItemStack stack = mc.player.getHeldItemMainhand();
        if (!AbilitySlots.hasCompetingAbilities(stack)) {
            return;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU)) {
            mc.displayGuiScreen(new GuiNTMToolAbility(AbilitySlots.buildAvailableAbilities(stack)));
        } else {
            AbilityNetworking.CHANNEL.sendToServer(new CycleAbilityMessage());
        }
    }
}
