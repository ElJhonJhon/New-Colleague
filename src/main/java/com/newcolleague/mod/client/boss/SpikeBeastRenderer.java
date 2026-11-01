package com.newcolleague.mod.client.boss;

import com.geckolib.renderer.GeoEntityRenderer;
import com.newcolleague.mod.entity.boss.SpikeBeastEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class SpikeBeastRenderer extends GeoEntityRenderer<SpikeBeastEntity, LivingEntityRenderState> {

    public SpikeBeastRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SpikeBeastModel());
        this.shadowRadius = 1.4F;
        // Escalar el modelo para que tenga un tamaño imponente similar al Wither Boss (~3.5 bloques de altura)
        this.withScale(2.8F);
    }
}
