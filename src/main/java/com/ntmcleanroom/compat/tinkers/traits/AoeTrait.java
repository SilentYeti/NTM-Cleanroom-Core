package com.ntmcleanroom.compat.tinkers.traits;

import com.hbm.handler.ability.IToolAreaAbility;
import com.ntmcleanroom.compat.tinkers.ability.AbilitySlots;
import com.ntmcleanroom.compat.tinkers.ability.CompetingAreaTrait;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import slimeknights.tconstruct.library.traits.AbstractTrait;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Recreation of hbm's "Hammer" tool area-ability: breaks every harvestable block in a cube of the
 * given range around the block that was broken (hbm's {@code rangeAtLevel} table), regardless of
 * block type. Competes with {@link VeinMinerTrait}/{@link FlatAoeTrait} for the tool's single
 * "area ability" slot (see {@link AbilitySlots}).
 */
public class AoeTrait extends AbstractTrait implements CompetingAreaTrait {

    private static final ThreadLocal<Boolean> EXPANDING = ThreadLocal.withInitial(() -> false);
    // Same-tick belt-and-suspenders alongside AreaAbilityGuard (see markSelfDestroyed/isEcho
    // below) - the real fix for runaway spreading, since an echo can land on a later tick.
    private static final Map<EntityPlayer, Long> lastTriggerTick = new WeakHashMap<>();

    private final int radius;
    private final int hbmLevel;

    public AoeTrait(String identifier, int radius, int hbmLevel) {
        super(identifier, TextFormatting.AQUA);
        this.radius = radius;
        this.hbmLevel = hbmLevel;
    }

    @Override
    public IToolAreaAbility getAreaAbility() {
        return IToolAreaAbility.HAMMER;
    }

    @Override
    public int getHbmLevel() {
        return hbmLevel;
    }

    @Override
    public void afterBlockBreak(ItemStack stack, World world, IBlockState state, BlockPos pos, EntityLivingBase living, boolean wasEffective) {
        if (world.isRemote || !wasEffective || !(living instanceof EntityPlayer) || EXPANDING.get()) {
            return;
        }
        if (AreaAbilityGuard.isEcho(pos)) {
            return;
        }
        if (!ToolTypes.isHarvestTool(stack) || AbilitySlots.getActivePreset(stack).areaAbility != IToolAreaAbility.HAMMER) {
            return;
        }

        EntityPlayer player = (EntityPlayer) living;

        long tick = world.getTotalWorldTime();
        Long last = lastTriggerTick.get(player);
        if (last != null && last == tick) {
            return;
        }
        lastTriggerTick.put(player, tick);

        EXPANDING.set(true);
        try {
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        if (x == 0 && y == 0 && z == 0) {
                            continue;
                        }

                        BlockPos target = pos.add(x, y, z);
                        IBlockState targetState = world.getBlockState(target);
                        if (targetState.getBlock().isAir(targetState, world, target)) {
                            continue;
                        }
                        // ForgeHooks.canHarvestBlock returns true unconditionally for any block
                        // whose material doesn't require a tool at all (water, dirt, plants...),
                        // completely bypassing the harvest-level check below it - explicitly
                        // exclude those, or AoE "mines" water/dirt/etc regardless of tool tier.
                        if (targetState.getMaterial().isToolNotRequired()) {
                            continue;
                        }
                        if (targetState.getBlockHardness(world, target) < 0) {
                            continue;
                        }

                        if (net.minecraftforge.common.ForgeHooks.canHarvestBlock(targetState.getBlock(), player, world, target)) {
                            AreaAbilityGuard.markSelfDestroyed(target);
                            world.destroyBlock(target, true);
                            stack.damageItem(1, player);
                        }
                    }
                }
            }
        } finally {
            EXPANDING.set(false);
        }
    }
}
