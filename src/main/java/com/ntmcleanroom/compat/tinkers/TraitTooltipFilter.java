package com.ntmcleanroom.compat.tinkers;

import com.hbm.handler.ability.ToolPreset;
import com.ntmcleanroom.compat.tinkers.ability.AbilitySlots;
import com.ntmcleanroom.compat.tinkers.ability.CompetingAreaTrait;
import com.ntmcleanroom.compat.tinkers.ability.CompetingHarvestTrait;
import com.ntmcleanroom.compat.tinkers.traits.BeheaderTrait;
import com.ntmcleanroom.compat.tinkers.traits.RadioactiveBladeTrait;
import com.ntmcleanroom.compat.tinkers.traits.StunTrait;
import com.ntmcleanroom.compat.tinkers.traits.ToolTypes;
import com.ntmcleanroom.compat.tinkers.traits.VampireTrait;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.tconstruct.library.traits.ITrait;
import slimeknights.tconstruct.library.utils.TinkerUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Tinkers attaches our traits to a material's HEAD stat-type bucket (see the head-only fix), which
 * is shared by every tool type built from that head (pickaxe/axe/shovel/sword all use the same
 * "head" part-type) - so a Desh Pickaxe's tooltip lists Stun/Beheader too, even though our own
 * runtime gates ({@link ToolTypes}) already correctly prevent them from ever firing on it.
 * There's no registration-side fix for this (confirmed {@code IToolMod.isHidden()} takes no
 * stack/context parameter, so it can't vary per built-tool-type) - this strips the inapplicable
 * lines from the rendered tooltip directly instead.
 */
@SideOnly(Side.CLIENT)
public class TraitTooltipFilter {

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new TraitTooltipFilter());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<ITrait> traits = TinkerUtil.getTraitsOrdered(stack);

        if (!traits.isEmpty()) {
            boolean isSword = ToolTypes.isSword(stack);
            boolean isAxe = ToolTypes.isAxe(stack);
            boolean isHarvest = ToolTypes.isHarvestTool(stack);

            Set<String> inapplicableNames = new HashSet<>();
            for (ITrait trait : traits) {
                if (!applies(trait, isSword, isAxe, isHarvest)) {
                    inapplicableNames.add(trait.getLocalizedName());
                }
            }

            if (!inapplicableNames.isEmpty()) {
                event.getToolTip().removeIf(line -> {
                    for (String name : inapplicableNames) {
                        if (line.contains(name)) {
                            return true;
                        }
                    }
                    return false;
                });
            }
        }

        if (AbilitySlots.hasCompetingAbilities(stack)) {
            ToolPreset active = AbilitySlots.getActivePreset(stack);
            event.getToolTip().add("");
            event.getToolTip().add(TextFormatting.GRAY + "Active: " + TextFormatting.YELLOW + AbilitySlots.getMessage(active).getFormattedText());
            event.getToolTip().add(TextFormatting.GRAY + "Right click to cycle through presets!");
            event.getToolTip().add(TextFormatting.GRAY + "Sneak-click to go to first preset!");
            event.getToolTip().add(TextFormatting.GRAY + "Alt-click to open customization GUI!");
        }
    }

    private static boolean applies(ITrait trait, boolean isSword, boolean isAxe, boolean isHarvest) {
        if (trait instanceof CompetingAreaTrait || trait instanceof CompetingHarvestTrait) {
            return isHarvest;
        }
        if (trait instanceof BeheaderTrait) {
            return isAxe;
        }
        if (trait instanceof StunTrait || trait instanceof VampireTrait) {
            return isSword;
        }
        if (trait instanceof RadioactiveBladeTrait) {
            return isSword || isHarvest;
        }
        return true;
    }
}
