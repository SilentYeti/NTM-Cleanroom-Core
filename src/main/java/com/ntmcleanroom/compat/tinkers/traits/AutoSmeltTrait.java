package com.ntmcleanroom.compat.tinkers.traits;

import com.hbm.handler.ability.IToolHarvestAbility;
import com.ntmcleanroom.compat.tinkers.ability.AbilitySlots;
import com.ntmcleanroom.compat.tinkers.ability.CompetingHarvestTrait;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.world.BlockEvent;
import slimeknights.tconstruct.library.traits.AbstractTrait;

import java.util.List;

/**
 * hbm's "Smelter" tool harvest-ability. hbm's own implementation lives in {@code onHarvestBlock},
 * which manually captures and re-spawns drops via {@code ItemToolAbility.harvestAndCapture} - too
 * invasive to call directly without risking a double-break against Tinkers' own break flow (see
 * the plan). Its actual transform is simple and confirmed byte-for-byte from hbm's own bytecode
 * though (replace each drop with {@code FurnaceRecipes.getSmeltingResult(drop)}, scaling count),
 * so this reproduces that exact logic via Tinkers' own {@code blockHarvestDrops} hook instead -
 * same result, safer integration. Competes with {@link SilkTouchTrait}/{@link FortuneTrait}/
 * {@link AutoShredTrait} for the tool's single "harvest ability" slot (see {@link AbilitySlots}).
 */
public class AutoSmeltTrait extends AbstractTrait implements CompetingHarvestTrait {

    public AutoSmeltTrait(String identifier) {
        super(identifier, TextFormatting.GOLD);
    }

    @Override
    public IToolHarvestAbility getHarvestAbility() {
        return IToolHarvestAbility.SMELTER;
    }

    @Override
    public int getHbmLevel() {
        return 0;
    }

    @Override
    public void blockHarvestDrops(ItemStack stack, BlockEvent.HarvestDropsEvent event) {
        if (event.isSilkTouching() || !ToolTypes.isHarvestTool(stack)
                || AbilitySlots.getActivePreset(stack).harvestAbility != IToolHarvestAbility.SMELTER) {
            return;
        }

        List<ItemStack> drops = event.getDrops();
        for (int i = 0; i < drops.size(); i++) {
            ItemStack drop = drops.get(i);
            if (drop.isEmpty()) {
                continue;
            }

            ItemStack smelted = FurnaceRecipes.instance().getSmeltingResult(drop);
            if (!smelted.isEmpty()) {
                ItemStack result = smelted.copy();
                result.setCount(smelted.getCount() * drop.getCount());
                drops.set(i, result);
            }
        }
    }
}
