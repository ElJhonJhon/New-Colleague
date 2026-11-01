package com.newcolleague.mod.entity;

import com.newcolleague.mod.NewColleagueMod;
import com.newcolleague.mod.entity.boss.SpikeBeastEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, NewColleagueMod.MODID);

    public static final ResourceKey<EntityType<?>> KOBOLD_GIRL_KEY =
            ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(NewColleagueMod.MODID, "kobold_girl"));

    public static final DeferredHolder<EntityType<?>, EntityType<KoboldGirlEntity>> KOBOLD_GIRL =
            ENTITIES.register("kobold_girl", () -> EntityType.Builder.of(KoboldGirlEntity::new, MobCategory.CREATURE)
                    .sized(0.75F, 1.8F)
                    .clientTrackingRange(10)
                    .build(KOBOLD_GIRL_KEY));

    public static final ResourceKey<EntityType<?>> SPIKE_BEAST_KEY =
            ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(NewColleagueMod.MODID, "spike_beast"));

    public static final DeferredHolder<EntityType<?>, EntityType<SpikeBeastEntity>> SPIKE_BEAST =
            ENTITIES.register("spike_beast", () -> EntityType.Builder.of(SpikeBeastEntity::new, MobCategory.MONSTER)
                    .sized(2.2F, 3.4F)
                    .clientTrackingRange(12)
                    .build(SPIKE_BEAST_KEY));
}
