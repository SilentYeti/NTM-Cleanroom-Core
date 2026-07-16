package com.ntmcleanroom.compat.tinkers.traits;

import com.hbm.handler.ability.IWeaponAbility;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import slimeknights.tconstruct.library.traits.AbstractTrait;

/**
 * hbm's "Radiation" weapon-ability, delegated directly to hbm's own
 * {@link IWeaponAbility#RADIATION} object (fully generic {@code onHit}, zero dependency on the
 * weapon being an {@code ItemToolAbility}). A weapon ability, always-on for every hit like every
 * other {@code IWeaponAbility} in hbm. Schrabidium is the only material with this, and hbm gives
 * it a *different* level on its harvest tools (0 -> 15 rad) vs. its sword (1 -> 50 rad) - since
 * Tinkers attaches one trait instance per material (not per built-tool-type), that branch has to
 * live inside this single instance rather than as two separately-registered traits.
 */
public class RadioactiveBladeTrait extends AbstractTrait {

    private final int harvestHbmLevel;
    private final int swordHbmLevel;

    public RadioactiveBladeTrait(String identifier, int harvestHbmLevel, int swordHbmLevel) {
        super(identifier, TextFormatting.GREEN);
        this.harvestHbmLevel = harvestHbmLevel;
        this.swordHbmLevel = swordHbmLevel;
    }

    @Override
    public void onHit(ItemStack stack, EntityLivingBase attacker, EntityLivingBase target, float damage, boolean wasCritical) {
        if (attacker.world.isRemote || !(attacker instanceof EntityPlayer)) {
            return;
        }

        int level;
        if (ToolTypes.isSword(stack)) {
            level = swordHbmLevel;
        } else if (ToolTypes.isHarvestTool(stack)) {
            level = harvestHbmLevel;
        } else {
            return;
        }

        IWeaponAbility.RADIATION.onHit(level, attacker.world, (EntityPlayer) attacker, target, stack.getItem());
    }
}
