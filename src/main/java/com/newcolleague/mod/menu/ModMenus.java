package com.newcolleague.mod.menu;

import com.newcolleague.mod.NewColleagueMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, NewColleagueMod.MODID);

    public static final Supplier<MenuType<KoboldGirlMenu>> KOBOLD_GIRL_MENU =
            MENUS.register("kobold_girl_menu", () -> IMenuTypeExtension.create(KoboldGirlMenu::new));
}
