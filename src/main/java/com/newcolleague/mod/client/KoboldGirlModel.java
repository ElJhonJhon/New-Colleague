package com.newcolleague.mod.client;

import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.newcolleague.mod.NewColleagueMod;
import com.newcolleague.mod.entity.KoboldGirlEntity;
import com.newcolleague.mod.entity.KoboldTribeVariant;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KoboldGirlModel extends DefaultedEntityGeoModel<KoboldGirlEntity> {

    private static final Identifier DEFAULT_TEXTURE =
            Identifier.fromNamespaceAndPath(NewColleagueMod.MODID, "textures/entity/kobold_girl.png");

    private static final Identifier GENERIC_SURVIVOR_TEXTURE =
            Identifier.fromNamespaceAndPath(NewColleagueMod.MODID, "textures/entity/kobold_girl_survivor.png");

    private static final Map<Integer, Identifier> VARIANT_TEXTURE_CACHE = new ConcurrentHashMap<>();
    private static final Map<Integer, Identifier> VARIANT_SURVIVOR_TEXTURE_CACHE = new ConcurrentHashMap<>();
    private static final Map<Identifier, Boolean> RESOURCE_EXISTS_CACHE = new ConcurrentHashMap<>();

    public KoboldGirlModel() {
        super(Identifier.fromNamespaceAndPath(NewColleagueMod.MODID, "kobold_girl"));
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        boolean isSurvivor = renderState.getOrDefaultGeckolibData(KoboldGirlRenderer.IS_SURVIVOR_QUEEN, false);
        int variantId = renderState.getOrDefaultGeckolibData(KoboldGirlRenderer.TRIBE_VARIANT, 0);

        if (isSurvivor) {
            // 1. Intentar la textura de sobreviviente específica de este color/tribu (ej. kobold_girl_red_survivor.png)
            Identifier survivorVariantTexture = VARIANT_SURVIVOR_TEXTURE_CACHE.computeIfAbsent(variantId, id -> {
                KoboldTribeVariant variant = KoboldTribeVariant.byId(id);
                return Identifier.fromNamespaceAndPath(NewColleagueMod.MODID, variant.getSurvivorTexturePath());
            });
            if (isResourceAvailable(survivorVariantTexture)) {
                return survivorVariantTexture;
            }

            // 2. Si aún no existe la versión por color, intentar la textura de sobreviviente genérica (kobold_girl_survivor.png)
            if (isResourceAvailable(GENERIC_SURVIVOR_TEXTURE)) {
                return GENERIC_SURVIVOR_TEXTURE;
            }
        }

        // 3. Textura estándar de la variante de tribu (ej. kobold_girl_red.png)
        Identifier texture = VARIANT_TEXTURE_CACHE.computeIfAbsent(variantId, id -> {
            KoboldTribeVariant variant = KoboldTribeVariant.byId(id);
            return Identifier.fromNamespaceAndPath(NewColleagueMod.MODID, variant.getTexturePath());
        });

        if (isResourceAvailable(texture)) {
            return texture;
        }

        // 4. Fallback final seguro si aún no se ha dibujado el archivo
        return DEFAULT_TEXTURE;
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
