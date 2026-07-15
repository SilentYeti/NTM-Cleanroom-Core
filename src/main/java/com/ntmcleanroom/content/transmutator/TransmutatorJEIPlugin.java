package com.ntmcleanroom.content.transmutator;

import com.hbm.handler.jei.JEIConfig;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import net.minecraft.item.ItemStack;

/**
 * hbm still ships the "hbm.transmutation" JEI category (see {@code JEIConfig}/{@code TransmutationRecipeHandler}
 * in the hbm jar), and its own plugin still feeds it from the still-present (but now orphaned)
 * {@code NuclearTransmutationRecipes} class - the recipe data was never actually deleted, only the
 * machine that used it. So we must NOT add our own copy of the recipes here (that would just duplicate
 * every entry); we only need to wire our recreated block/gui back into that existing category as its
 * catalyst and click area, since hbm's own registration still points at the removed block/gui classes.
 */
@JEIPlugin
public class TransmutatorJEIPlugin implements IModPlugin {

    @Override
    public void register(IModRegistry registry) {
        registry.addRecipeCatalyst(new ItemStack(TransmutatorModule.block), JEIConfig.TRANSMUTATION);
        registry.addRecipeClickArea(GUIMachineSchrabidiumTransmutator.class, 64, 56, 66, 31, JEIConfig.TRANSMUTATION);
    }
}
