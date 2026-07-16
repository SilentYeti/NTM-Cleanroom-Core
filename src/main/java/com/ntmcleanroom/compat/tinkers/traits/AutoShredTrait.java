package com.ntmcleanroom.compat.tinkers.traits;

import com.hbm.handler.ability.IToolHarvestAbility;
import com.hbm.inventory.recipes.ShredderRecipes;
import com.hbm.items.ModItems;
import com.ntmcleanroom.compat.tinkers.ability.AbilitySlots;
import com.ntmcleanroom.compat.tinkers.ability.CompetingHarvestTrait;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.world.BlockEvent;
import slimeknights.tconstruct.library.traits.AbstractTrait;

/**
 * hbm's Schrabidium-tier "Shredder" tool harvest-ability. Like {@link AutoSmeltTrait}, hbm's own
 * {@code onHarvestBlock} implementation is too invasive to call directly (manual drop capture/
 * respawn), but its actual transform is simple and now called on hbm's real data directly: look
 * the drop up via {@link ShredderRecipes#getShredderResult}, and replace it *only* if the result
 * is non-empty and isn't {@link ModItems#scrap} (confirmed from hbm's own bytecode - an unmapped
 * or scrap-mapped drop is left completely unchanged, not forced into scrap). Competes with
 * {@link SilkTouchTrait}/{@link FortuneTrait}/{@link AutoSmeltTrait} for the tool's single
 * "harvest ability" slot (see {@link AbilitySlots}).
 */
public class AutoShredTrait extends AbstractTrait implements CompetingHarvestTrait {

    public AutoShredTrait(String identifier) {
        super(identifier, TextFormatting.DARK_GREEN);
    }

    @Override
    public IToolHarvestAbility getHarvestAbility() {
        return IToolHarvestAbility.SHREDDER;
    }

    @Override
    public int getHbmLevel() {
        return 0;
    }

    @Override
    public void blockHarvestDrops(ItemStack stack, BlockEvent.HarvestDropsEvent event) {
        if (event.isSilkTouching() || !ToolTypes.isHarvestTool(stack)
                || AbilitySlots.getActivePreset(stack).harvestAbility != IToolHarvestAbility.SHREDDER) {
            return;
        }

        for (int i = 0; i < event.getDrops().size(); i++) {
            ItemStack drop = event.getDrops().get(i);
            if (drop.isEmpty()) {
                continue;
            }

            ItemStack result = ShredderRecipes.getShredderResult(drop);
            if (!result.isEmpty() && result.getItem() != ModItems.scrap) {
                event.getDrops().set(i, result.copy());
            }
        }
    }
}
