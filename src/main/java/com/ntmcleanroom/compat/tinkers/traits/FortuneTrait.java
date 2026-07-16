package com.ntmcleanroom.compat.tinkers.traits;

import com.hbm.handler.ability.IToolHarvestAbility;
import com.ntmcleanroom.compat.tinkers.ability.AbilitySlots;
import com.ntmcleanroom.compat.tinkers.ability.CompetingHarvestTrait;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import slimeknights.tconstruct.library.traits.AbstractTrait;

/**
 * hbm's "Luck" tool harvest-ability, delegated directly to hbm's own
 * {@link IToolHarvestAbility#LUCK} object: it temporarily grants vanilla Fortune at
 * {@code powerAtLevel[level]} (hbm's own lookup table - {1,2,3,4,5,9}, not a 1:1 level mapping)
 * around the harvest, so {@code hbmLevel} here is hbm's raw registered level, not the resolved
 * fortune tier. Competes with {@link SilkTouchTrait}/{@link AutoSmeltTrait}/{@link AutoShredTrait}
 * for the tool's single "harvest ability" slot (see {@link AbilitySlots}).
 */
public class FortuneTrait extends AbstractTrait implements CompetingHarvestTrait {

    private final int hbmLevel;

    public FortuneTrait(String identifier, int hbmLevel) {
        super(identifier, TextFormatting.BLUE);
        this.hbmLevel = hbmLevel;
    }

    @Override
    public IToolHarvestAbility getHarvestAbility() {
        return IToolHarvestAbility.LUCK;
    }

    @Override
    public int getHbmLevel() {
        return hbmLevel;
    }

    private boolean isActive(ItemStack stack) {
        return ToolTypes.isHarvestTool(stack) && AbilitySlots.getActivePreset(stack).harvestAbility == IToolHarvestAbility.LUCK;
    }

    @Override
    public void beforeBlockBreak(ItemStack stack, BlockEvent.BreakEvent event) {
        if (!event.getWorld().isRemote && isActive(stack)) {
            IToolHarvestAbility.LUCK.preHarvestAll(hbmLevel, event.getWorld(), event.getPlayer());
        }
    }

    @Override
    public void afterBlockBreak(ItemStack stack, World world, IBlockState state, BlockPos pos, EntityLivingBase living, boolean wasEffective) {
        if (!world.isRemote && living instanceof EntityPlayer && isActive(stack)) {
            IToolHarvestAbility.LUCK.postHarvestAll(hbmLevel, world, (EntityPlayer) living);
        }
    }
}
