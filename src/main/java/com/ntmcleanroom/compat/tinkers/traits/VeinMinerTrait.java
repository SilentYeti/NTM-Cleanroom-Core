package com.ntmcleanroom.compat.tinkers.traits;

import com.hbm.handler.ability.IToolAreaAbility;
import com.ntmcleanroom.compat.tinkers.ability.AbilitySlots;
import com.ntmcleanroom.compat.tinkers.ability.CompetingAreaTrait;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import slimeknights.tconstruct.library.traits.AbstractTrait;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Recreation of hbm's "Recursion" (Vein Miner) tool area-ability: breaking one block of a vein
 * breaks the rest of the directly-connected same-block vein too, capped by travel distance from
 * the origin block (hbm's own {@code radiusAtLevel} table, not a block-count cap) - applies to
 * pickaxe/shovel/axe (hbm gives Recursion to all three identically), and competes with
 * {@link AoeTrait}/{@link FlatAoeTrait} for the tool's single "area ability" slot (see
 * {@link AbilitySlots}).
 *
 * <p>Chaining is further restricted per tool type so it can't be used to instantly clear out
 * common terrain: shovels only chain shovel-harvestable blocks (dirt/grass/sand/etc), axes only
 * chain axe-harvestable blocks (logs), and pickaxes only chain ores (any oredict "ore*" block) or
 * cobblestone - explicitly NOT plain stone/andesite/diorite/granite/etc, which would otherwise let
 * a single break clear an entire mountain.
 */
public class VeinMinerTrait extends AbstractTrait implements CompetingAreaTrait {

    private static final ThreadLocal<Boolean> EXPANDING = ThreadLocal.withInitial(() -> false);
    // Same-tick belt-and-suspenders alongside AreaAbilityGuard (see markSelfDestroyed/isEcho
    // below) - the real fix for runaway spreading, since an echo can land on a later tick.
    private static final Map<EntityPlayer, Long> lastTriggerTick = new WeakHashMap<>();

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
        if (AreaAbilityGuard.isEcho(pos)) {
            return;
        }
        if (!ToolTypes.isHarvestTool(stack) || AbilitySlots.getActivePreset(stack).areaAbility != IToolAreaAbility.RECURSION) {
            return;
        }
        if (!isVeinMinable(state, stack)) {
            return;
        }

        EntityPlayer player = (EntityPlayer) living;
        Block target = state.getBlock();
        int radiusSq = radius * radius;

        long tick = world.getTotalWorldTime();
        Long last = lastTriggerTick.get(player);
        if (last != null && last == tick) {
            return;
        }
        lastTriggerTick.put(player, tick);

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
                    // ForgeHooks.canHarvestBlock returns true unconditionally for any block whose
                    // material doesn't require a tool at all, bypassing the harvest-level check
                    // below it - explicitly exclude those as a safety net (belt-and-suspenders
                    // alongside the same-block-type restriction above).
                    if (neighborState.getMaterial().isToolNotRequired() || neighborState.getBlockHardness(world, neighbor) < 0) {
                        continue;
                    }

                    if (net.minecraftforge.common.ForgeHooks.canHarvestBlock(target, player, world, neighbor)) {
                        AreaAbilityGuard.markSelfDestroyed(neighbor);
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

    private static boolean isVeinMinable(IBlockState state, ItemStack stack) {
        Block block = state.getBlock();
        String harvestTool = block.getHarvestTool(state);
        if (ToolTypes.isAxe(stack)) {
            return "axe".equals(harvestTool);
        }
        if (ToolTypes.isShovel(stack)) {
            return "shovel".equals(harvestTool);
        }
        if (ToolTypes.isPickaxe(stack)) {
            return isCobblestone(block) || isOre(block, state);
        }
        return false;
    }

    private static boolean isCobblestone(Block block) {
        if (block == Blocks.COBBLESTONE) {
            return true;
        }
        for (int id : OreDictionary.getOreIDs(new ItemStack(block))) {
            if (OreDictionary.getOreName(id).equals("cobblestone")) {
                return true;
            }
        }
        return false;
    }

    private static boolean isOre(Block block, IBlockState state) {
        ItemStack stack = new ItemStack(block, 1, block.damageDropped(state));
        for (int id : OreDictionary.getOreIDs(stack)) {
            if (OreDictionary.getOreName(id).startsWith("ore")) {
                return true;
            }
        }
        return false;
    }

    private static Iterable<BlockPos> neighbors(BlockPos pos) {
        return java.util.Arrays.asList(
                pos.north(), pos.south(), pos.east(), pos.west(), pos.up(), pos.down(),
                pos.north().east(), pos.north().west(), pos.south().east(), pos.south().west());
    }
}
