package com.ntmcleanroom.compat.tinkers.traits;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import slimeknights.tconstruct.library.tools.SwordCore;
import slimeknights.tconstruct.tools.tools.Hatchet;
import slimeknights.tconstruct.tools.tools.Pickaxe;
import slimeknights.tconstruct.tools.tools.Shovel;

/**
 * Tool-type checks matching hbm's own per-tool-type ability table: hbm's harvest abilities
 * (Vein Miner, Hammer, Silk Touch, Fortune, Auto-Smelt, Auto-Shred) apply identically to
 * pickaxe/shovel/axe, hbm's Beheader is axe-only, and hbm's weapon abilities (Stun, Vampire,
 * Radioactive) apply to swords (and, for Schrabidium, harvest tools too).
 */
public final class ToolTypes {

    private ToolTypes() {}

    public static boolean isHarvestTool(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof Pickaxe || item instanceof Shovel || item instanceof Hatchet;
    }

    public static boolean isAxe(ItemStack stack) {
        return stack.getItem() instanceof Hatchet;
    }

    public static boolean isSword(ItemStack stack) {
        return stack.getItem() instanceof SwordCore;
    }
}
