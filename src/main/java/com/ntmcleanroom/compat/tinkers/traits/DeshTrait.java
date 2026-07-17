package com.ntmcleanroom.compat.tinkers.traits;

import com.google.common.collect.Multimap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import slimeknights.tconstruct.library.traits.AbstractTrait;

import java.util.UUID;

/**
 * hbm's real Desh tools (confirmed via bytecode of {@code ModItems}/{@code ItemToolAbility}) are
 * built on an {@code Item.ToolMaterial} with {@code maxUses=0} - vanilla's own
 * {@code Item.isDamageable()} returns {@code maxDamage > 0}, so a 0 max damage makes the item
 * durability-immune through the ordinary damage pathway with no custom override needed - and carry
 * a fixed {@code movement=-0.05} attribute, applied only in the mainhand slot (confirmed in
 * {@code getItemAttributeModifiers}), i.e. a flat 5% movement speed penalty while held.
 *
 * <p>Tinkers tools manage their own durability NBT instead of vanilla item damage, so the
 * "maxUses=0" trick doesn't apply here - {@link #onToolDamage} is the real hook
 * ({@code ToolHelper.damageTool} chains every trait's return value as the applied damage, so
 * returning 0 negates it outright), and {@link #getAttributeModifiers} is how Tinkers traits
 * contribute extra attribute modifiers, matching hbm's own mainhand-only movement penalty.
 */
public class DeshTrait extends AbstractTrait {

    private static final UUID MOVEMENT_MODIFIER = UUID.fromString("d9f9a916-1b1a-4f6e-8b1a-5f1b2c3d4e5f");

    public DeshTrait(String identifier) {
        super(identifier, TextFormatting.AQUA);
    }

    @Override
    public int onToolDamage(ItemStack stack, int damage, int newDamage, EntityLivingBase entity) {
        return 0;
    }

    @Override
    public void getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack, Multimap<String, AttributeModifier> modifiers) {
        if (slot == EntityEquipmentSlot.MAINHAND) {
            // Operation 1 = multiply-by-percentage-of-base (1.12.2 has no Operation enum, just
            // this raw int - confirmed against hbm's own identical call, which uses the same 1).
            modifiers.put(SharedMonsterAttributes.MOVEMENT_SPEED.getName(), new AttributeModifier(
                    MOVEMENT_MODIFIER, "Desh weight", -0.05, 1));
        }
    }
}
