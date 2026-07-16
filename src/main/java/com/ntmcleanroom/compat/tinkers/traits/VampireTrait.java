package com.ntmcleanroom.compat.tinkers.traits;

import com.hbm.handler.ability.IWeaponAbility;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import slimeknights.tconstruct.library.traits.AbstractTrait;

/**
 * hbm's "Vampire" weapon-ability, delegated directly to hbm's own {@link IWeaponAbility#VAMPIRE}
 * object. Its real implementation (confirmed via bytecode) sets the target's health directly and
 * calls {@code onDeath} manually if it drops to zero - true damage that bypasses the normal
 * damage-event pipeline entirely, not a {@code DamageSource} attack - fully generic, zero
 * dependency on the weapon being an {@code ItemToolAbility}. A weapon ability, always-on for every
 * hit like every other {@code IWeaponAbility} in hbm - gated to swords.
 */
public class VampireTrait extends AbstractTrait {

    private final int hbmLevel;

    public VampireTrait(String identifier, int hbmLevel) {
        super(identifier, TextFormatting.DARK_RED);
        this.hbmLevel = hbmLevel;
    }

    @Override
    public void onHit(ItemStack stack, EntityLivingBase attacker, EntityLivingBase target, float damage, boolean wasCritical) {
        if (!attacker.world.isRemote && attacker instanceof EntityPlayer && ToolTypes.isSword(stack)) {
            IWeaponAbility.VAMPIRE.onHit(hbmLevel, attacker.world, (EntityPlayer) attacker, target, stack.getItem());
        }
    }
}
