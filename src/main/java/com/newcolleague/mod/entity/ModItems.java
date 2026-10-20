package com.newcolleague.mod.entity;

import com.newcolleague.mod.NewColleagueMod;
import com.newcolleague.mod.item.BaculoItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NewColleagueMod.MODID);

    public static final DeferredItem<SpawnEggItem> KOBOLD_GIRL_SPAWN_EGG =
            ITEMS.registerItem("kobold_girl_spawn_egg",
                    properties -> new SpawnEggItem(properties.spawnEgg(ModEntities.KOBOLD_GIRL.get())));

    public static final DeferredItem<BaculoItem> BACULO =
            ITEMS.registerItem("baculo", BaculoItem::new, properties -> properties
                    .durability(250)
                    .enchantable(15));
}
