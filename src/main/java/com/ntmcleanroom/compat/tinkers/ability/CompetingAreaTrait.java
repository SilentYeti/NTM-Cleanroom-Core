package com.ntmcleanroom.compat.tinkers.ability;

import com.hbm.handler.ability.IToolAreaAbility;

/** Implemented by traits that represent one of hbm's {@link IToolAreaAbility} options (Vein Miner/Hammer/Hammer Flat) - these compete for a tool's single active area-ability slot, see {@link AbilitySlots}. */
public interface CompetingAreaTrait {
    IToolAreaAbility getAreaAbility();
    int getHbmLevel();
}
