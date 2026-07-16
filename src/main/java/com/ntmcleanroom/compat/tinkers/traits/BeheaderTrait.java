package com.ntmcleanroom.compat.tinkers.traits;

import com.hbm.handler.ability.IWeaponAbility;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import slimeknights.tconstruct.library.traits.AbstractTrait;

/**
 * hbm's "Beheader" weapon-ability, delegated directly to hbm's own {@link IWeaponAbility#BEHEADER}
 * object (confirmed via bytecode: on the killing blow, a guaranteed per-mob-type drop -
 * skeleton/wither skeleton/zombie/creeper/magma cube/slime/player/generic fallback - purely via
 * {@code EntityLivingBase.entityDropItem}, zero dependency on the weapon being an
 * {@code ItemToolAbility}). Level-independent in hbm, and axe-only, so one shared instance covers
 * all 5 materials' axe bucket. A weapon ability, always-on for every kill like every other
 * {@code IWeaponAbility} in hbm.
 */
public class BeheaderTrait extends AbstractTrait {

    public BeheaderTrait(String identifier) {
        super(identifier, TextFormatting.DARK_GRAY);
    }

    @Override
    public void onHit(ItemStack stack, EntityLivingBase attacker, EntityLivingBase target, float damage, boolean wasCritical) {
        if (!attacker.world.isRemote && attacker instanceof EntityPlayer && ToolTypes.isAxe(stack)) {
            IWeaponAbility.BEHEADER.onHit(0, attacker.world, (EntityPlayer) attacker, target, stack.getItem());
        }
    }
}
