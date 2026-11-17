package com.chronoarsenal;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ChronoArsenalMod.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ARSENAL_TAB =
            TABS.register("arsenal", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.chronoarsenal.arsenal"))
                    .icon(() -> new ItemStack(ModItems.TIME_SCYTHE.get()))
                    .displayItems((params, output) ->
                            ModItems.ITEMS.getEntries().forEach(entry -> output.accept(entry.get())))
                    .build());
}
