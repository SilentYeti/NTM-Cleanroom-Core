package com.ntmcleanroom.compat.tinkers.traits;

import com.hbm.handler.ability.IWeaponAbility;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import slimeknights.tconstruct.library.traits.AbstractTrait;

/**
 * hbm's "Stun" weapon-ability, delegated directly to hbm's own {@link IWeaponAbility#STUN} object
 * (its {@code onHit} is fully generic - just world/player/target/item - confirmed via bytecode to
 * have zero dependency on the weapon being an {@code ItemToolAbility}, safe to call on any tool).
 * A weapon ability, always-on for every hit like every other {@code IWeaponAbility} in hbm - no
 * selection/slot involved, just gated to swords.
 */
public class StunTrait extends AbstractTrait {

    private final int hbmLevel;

    public StunTrait(String identifier, int hbmLevel) {
        super(identifier, TextFormatting.YELLOW);
        this.hbmLevel = hbmLevel;
    }

    @Override
    public void onHit(ItemStack stack, EntityLivingBase attacker, EntityLivingBase target, float damage, boolean wasCritical) {
        if (!attacker.world.isRemote && attacker instanceof EntityPlayer && ToolTypes.isSword(stack)) {
            IWeaponAbility.STUN.onHit(hbmLevel, attacker.world, (EntityPlayer) attacker, target, stack.getItem());
        }
    }
}
