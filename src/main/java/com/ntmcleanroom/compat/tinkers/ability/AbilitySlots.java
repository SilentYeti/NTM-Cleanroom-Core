package com.ntmcleanroom.compat.tinkers.ability;

import com.hbm.handler.ability.AvailableAbilities;
import com.hbm.handler.ability.ToolPreset;
import com.hbm.items.tool.ItemToolAbility;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import slimeknights.tconstruct.library.traits.ITrait;
import slimeknights.tconstruct.library.utils.TinkerUtil;

import java.util.List;

/**
 * Drives hbm's own {@link AvailableAbilities}/{@link ItemToolAbility.Configuration}/
 * {@link ToolPreset} directly (see the plan: all three are standalone, no dependency on a stack's
 * item actually being {@code ItemToolAbility}), rather than reimplementing hbm's one-active-
 * ability-at-a-time preset mechanic ourselves. "Available" abilities for a tool are derived from
 * which {@link CompetingAreaTrait}/{@link CompetingHarvestTrait} instances are actually attached
 * to the built tool (via {@link TinkerUtil#getTraitsOrdered}), and state is persisted using hbm's
 * own NBT tag names/format so it round-trips through {@code Configuration.readFromNBT}/
 * {@code writeToNBT} unmodified.
 */
public final class AbilitySlots {

    private AbilitySlots() {}

    public static AvailableAbilities buildAvailableAbilities(ItemStack stack) {
        AvailableAbilities available = new AvailableAbilities();
        for (ITrait trait : TinkerUtil.getTraitsOrdered(stack)) {
            if (trait instanceof CompetingAreaTrait) {
                CompetingAreaTrait area = (CompetingAreaTrait) trait;
                available.addAbility(area.getAreaAbility(), area.getHbmLevel());
            }
            if (trait instanceof CompetingHarvestTrait) {
                CompetingHarvestTrait harvest = (CompetingHarvestTrait) trait;
                available.addAbility(harvest.getHarvestAbility(), harvest.getHbmLevel());
            }
        }
        return available;
    }

    /** Reads the tool's current {@code Configuration} from NBT, building a fresh default (first preset active) if none is stored yet. */
    public static ItemToolAbility.Configuration getConfiguration(ItemStack stack, AvailableAbilities available) {
        ItemToolAbility.Configuration config = new ItemToolAbility.Configuration();
        if (stack.hasTagCompound()) {
            config.readFromNBT(stack.getTagCompound());
        }
        if (config.presets == null || config.presets.isEmpty()) {
            config.reset(available);
        } else {
            config.restrictTo(available);
        }
        return config;
    }

    public static void writeConfiguration(ItemStack stack, ItemToolAbility.Configuration config) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
        }
        config.writeToNBT(tag);
        stack.setTagCompound(tag);
    }

    public static ToolPreset getActivePreset(ItemStack stack) {
        AvailableAbilities available = buildAvailableAbilities(stack);
        return getConfiguration(stack, available).getActivePreset();
    }

    public static boolean hasCompetingAbilities(ItemStack stack) {
        return buildAvailableAbilities(stack).size() > 0;
    }

    /** Advances to the tool's next preset (or resets to the blank preset if sneaking); returns the new active preset, or null if the tool has no competing abilities at all. */
    public static ToolPreset cycle(ItemStack stack, boolean sneaking) {
        AvailableAbilities available = buildAvailableAbilities(stack);
        if (available.size() == 0) {
            return null;
        }

        ItemToolAbility.Configuration config = getConfiguration(stack, available);
        List<ToolPreset> presets = config.presets;
        if (presets.size() < 2) {
            return config.getActivePreset();
        }

        if (sneaking) {
            config.currentPreset = 0;
        } else {
            config.currentPreset = (config.currentPreset + 1) % presets.size();
        }

        writeConfiguration(stack, config);
        return config.getActivePreset();
    }
}
