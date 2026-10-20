package com.newcolleague.mod.entity;

import com.newcolleague.mod.NewColleagueMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NewColleagueMod.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> NEW_COLLEAGUE_TAB =
            CREATIVE_MODE_TABS.register("new_colleague_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.newcolleaguemod"))
                    .icon(() -> ModItems.KOBOLD_GIRL_SPAWN_EGG.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.KOBOLD_GIRL_SPAWN_EGG.get());
                        output.accept(ModItems.BACULO.get());
                    })
                    .build());
}
