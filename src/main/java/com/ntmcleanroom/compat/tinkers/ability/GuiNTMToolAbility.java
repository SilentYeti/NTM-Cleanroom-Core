package com.ntmcleanroom.compat.tinkers.ability;

import com.hbm.handler.ability.AvailableAbilities;
import com.hbm.inventory.gui.GUIScreenToolAbility;
import com.hbm.main.MainRegistry;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;

/**
 * Subclasses hbm's real {@link GUIScreenToolAbility} instead of building a simplified stand-in
 * (see the plan): that class is a plain {@code GuiScreen} with no server-side {@code Container},
 * and its only hbm-specific dependency is two spots (confirmed via bytecode) that cast the held
 * stack's {@code Item} to {@code ItemToolAbility} to read/write NBT - everything else (icon-grid
 * rendering, click handling, the full multi-preset editor letting you set an area ability *and* a
 * harvest ability at once) is reused untouched by overriding just {@code initGui()}/
 * {@code doClose()} with our own item-agnostic {@link AbilitySlots} read/write.
 */
public class GuiNTMToolAbility extends GUIScreenToolAbility {

    public GuiNTMToolAbility(AvailableAbilities availableAbilities) {
        super(availableAbilities);
    }

    @Override
    public void initGui() {
        this.toolStack = mc.player.getHeldItemMainhand();
        if (this.toolStack.isEmpty()) {
            this.doClose();
        }
        this.config = AbilitySlots.getConfiguration(this.toolStack, this.availableAbilities);
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;
    }

    @Override
    protected void doClose() {
        AbilitySlots.writeConfiguration(this.toolStack, this.config);
        AbilityNetworking.CHANNEL.sendToServer(new SyncAbilityConfigMessage(this.toolStack.getTagCompound()));
        mc.player.closeScreen();
        MainRegistry.proxy.displayTooltipLegacy(AbilitySlots.getMessage(this.config.getActivePreset()).getFormattedText(), 11);
        mc.world.playSound(mc.player.getPosition(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS,
                0.25f, this.config.getActivePreset().isNone() ? 0.75f : 1.25f, false);
    }
}
