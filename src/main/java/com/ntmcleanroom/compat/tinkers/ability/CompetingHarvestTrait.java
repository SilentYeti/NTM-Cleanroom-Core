package com.ntmcleanroom.compat.tinkers.ability;

import com.hbm.handler.ability.IToolHarvestAbility;

/** Implemented by traits that represent one of hbm's {@link IToolHarvestAbility} options (Silk/Luck/Smelter/Shredder) - these compete for a tool's single active harvest-ability slot, see {@link AbilitySlots}. */
public interface CompetingHarvestTrait {
    IToolHarvestAbility getHarvestAbility();
    int getHbmLevel();
}
