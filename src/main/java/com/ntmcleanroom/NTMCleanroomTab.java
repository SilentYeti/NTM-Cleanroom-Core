package com.ntmcleanroom;

import com.ntmcleanroom.content.transmutator.TransmutatorModule;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class NTMCleanroomTab {

    public static final CreativeTabs MACHINES = new CreativeTabs(Tags.MODID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(TransmutatorModule.block);
        }
    };
}
