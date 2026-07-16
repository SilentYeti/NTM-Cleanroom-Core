package com.ntmcleanroom.compat.tinkers.ability;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

/**
 * Replicates hbm's real trigger for tool ability selection: right-click to cycle, Alt-click to
 * open the full editor GUI (see the plan - hbm's own keybind is literally bound to vanilla's
 * right-click keycode by default, so it functionally *is* right-click). Handled client-side only:
 * the GUI open is inherently client-only, and the cycle action is carried to the server
 * authoritatively via {@link CycleAbilityMessage} (matching hbm's own client/server split).
 */
@SideOnly(Side.CLIENT)
public class AbilityRightClickHandler {

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new AbilityRightClickHandler());
    }

    @SubscribeEvent
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!event.getWorld().isRemote) {
            return;
        }

        ItemStack stack = event.getItemStack();
        if (!AbilitySlots.hasCompetingAbilities(stack)) {
            return;
        }

        event.setCanceled(true);

        if (Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU)) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiNTMToolAbility(AbilitySlots.buildAvailableAbilities(stack)));
        } else {
            AbilityNetworking.CHANNEL.sendToServer(new CycleAbilityMessage());
        }
    }
}
