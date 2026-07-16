package com.ntmcleanroom.compat.tinkers;

import net.minecraft.item.Item;
import slimeknights.tconstruct.library.smeltery.ICast;

/**
 * A reusable cast for one of hbm's own material shapes (billet, plate, dense wire, etc.) rather
 * than a standard Tinkers tool part - see {@link ShapeCasts}. Implementing {@link ICast} marks it
 * as a reusable cast (not consumed by casting), matching the gold-cast convention its texture implies.
 */
public class ItemShapeCast extends Item implements ICast {

    public ItemShapeCast(String registryName) {
        this.setTranslationKey(registryName);
        this.setRegistryName(registryName);
        this.setMaxStackSize(1);
    }
}
