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
 * hbm's "Silk" tool harvest-ability, delegated directly to hbm's own
 * {@link IToolHarvestAbility#SILK} object (its {@code preHarvestAll}/{@code postHarvestAll}
 * default methods take no {@code ItemToolAbility}-specific parameter - just level/world/player -
 * so they're safe to call on a foreign tool). hbm's own implementation works by temporarily
 * granting vanilla Silk Touch around the harvest, letting vanilla's real drop calculation run in
 * between, rather than us reimplementing the drop swap ourselves. Competes with
 * {@link FortuneTrait}/{@link AutoSmeltTrait}/{@link AutoShredTrait} for the tool's single
 * "harvest ability" slot (see {@link AbilitySlots}).
 */
public class SilkTouchTrait extends AbstractTrait implements CompetingHarvestTrait {

    public SilkTouchTrait(String identifier) {
        super(identifier, TextFormatting.WHITE);
    }

    @Override
    public IToolHarvestAbility getHarvestAbility() {
        return IToolHarvestAbility.SILK;
    }

    @Override
    public int getHbmLevel() {
        return 0;
    }

    private boolean isActive(ItemStack stack) {
        return ToolTypes.isHarvestTool(stack) && AbilitySlots.getActivePreset(stack).harvestAbility == IToolHarvestAbility.SILK;
    }

    @Override
    public void beforeBlockBreak(ItemStack stack, BlockEvent.BreakEvent event) {
        if (!event.getWorld().isRemote && isActive(stack)) {
            IToolHarvestAbility.SILK.preHarvestAll(0, event.getWorld(), event.getPlayer());
        }
    }

    @Override
    public void afterBlockBreak(ItemStack stack, World world, IBlockState state, BlockPos pos, EntityLivingBase living, boolean wasEffective) {
        if (!world.isRemote && living instanceof EntityPlayer && isActive(stack)) {
            IToolHarvestAbility.SILK.postHarvestAll(0, world, (EntityPlayer) living);
        }
    }
}
