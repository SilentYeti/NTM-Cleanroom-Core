package com.ntmcleanroom.compat.tinkers.traits;

import com.hbm.handler.ability.IToolAreaAbility;
import com.ntmcleanroom.compat.tinkers.ability.AbilitySlots;
import com.ntmcleanroom.compat.tinkers.ability.CompetingAreaTrait;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import slimeknights.tconstruct.library.traits.AbstractTrait;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

/**
 * Recreation of hbm's "Recursion" (Vein Miner) tool area-ability: breaking one block of a vein
 * breaks the rest of the directly-connected same-block vein too, capped by travel distance from
 * the origin block (hbm's own {@code radiusAtLevel} table, not a block-count cap) - applies to
 * pickaxe/shovel/axe (hbm gives Recursion to all three identically), and competes with
 * {@link AoeTrait}/{@link FlatAoeTrait} for the tool's single "area ability" slot (see
 * {@link AbilitySlots}).
 */
public class VeinMinerTrait extends AbstractTrait implements CompetingAreaTrait {

    private static final ThreadLocal<Boolean> EXPANDING = ThreadLocal.withInitial(() -> false);

    /** Max travel distance from the origin block, matching hbm's radiusAtLevel table. */
    private final int radius;
    private final int hbmLevel;

    public VeinMinerTrait(String identifier, int radius, int hbmLevel) {
        super(identifier, TextFormatting.AQUA);
        this.radius = radius;
        this.hbmLevel = hbmLevel;
    }

    @Override
    public IToolAreaAbility getAreaAbility() {
        return IToolAreaAbility.RECURSION;
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
        if (!ToolTypes.isHarvestTool(stack) || AbilitySlots.getActivePreset(stack).areaAbility != IToolAreaAbility.RECURSION) {
            return;
        }

        EntityPlayer player = (EntityPlayer) living;
        Block target = state.getBlock();
        int radiusSq = radius * radius;

        EXPANDING.set(true);
        try {
            Set<BlockPos> visited = new HashSet<>();
            Deque<BlockPos> queue = new ArrayDeque<>();
            visited.add(pos);
            queue.add(pos);

            while (!queue.isEmpty()) {
                BlockPos current = queue.poll();

                for (BlockPos neighbor : neighbors(current)) {
                    if (!visited.add(neighbor) || neighbor.distanceSq(pos) > radiusSq) {
                        continue;
                    }

                    IBlockState neighborState = world.getBlockState(neighbor);
                    if (neighborState.getBlock() != target) {
                        continue;
                    }

                    if (net.minecraftforge.common.ForgeHooks.canHarvestBlock(target, player, world, neighbor)) {
                        world.destroyBlock(neighbor, true);
                        stack.damageItem(1, player);
                    }

                    queue.add(neighbor);
                }
            }
        } finally {
            EXPANDING.set(false);
        }
    }

    private static Iterable<BlockPos> neighbors(BlockPos pos) {
        return java.util.Arrays.asList(
                pos.north(), pos.south(), pos.east(), pos.west(), pos.up(), pos.down(),
                pos.north().east(), pos.north().west(), pos.south().east(), pos.south().west());
    }
}
