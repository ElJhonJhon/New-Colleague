package com.newcolleague.mod.client.boss;

import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.newcolleague.mod.NewColleagueMod;
import com.newcolleague.mod.entity.boss.SpikeBeastEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SpikeBeastModel extends DefaultedEntityGeoModel<SpikeBeastEntity> {

    private static final Identifier SPIKE_TEXTURE =
            Identifier.fromNamespaceAndPath(NewColleagueMod.MODID, "textures/entity/spike.png");
    private static final Identifier SPIKE_BEAST_TEXTURE =
            Identifier.fromNamespaceAndPath(NewColleagueMod.MODID, "textures/entity/spike_beast.png");

    private static final Map<Identifier, Boolean> RESOURCE_EXISTS_CACHE = new ConcurrentHashMap<>();

    public SpikeBeastModel() {
        super(Identifier.fromNamespaceAndPath(NewColleagueMod.MODID, "spike"));
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        if (isResourceAvailable(SPIKE_TEXTURE)) {
            return SPIKE_TEXTURE;
        }
        return SPIKE_BEAST_TEXTURE;
    }

    private static boolean isResourceAvailable(Identifier id) {
        return RESOURCE_EXISTS_CACHE.computeIfAbsent(id, targetId -> {
            try {
                return Minecraft.getInstance().getResourceManager().getResource(targetId).isPresent();
            } catch (Throwable ignored) {
                return false;
            }
        });
    }
}

