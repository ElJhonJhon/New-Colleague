package com.newcolleague.mod.client;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.builtin.ItemInHandGeoLayer;
import com.newcolleague.mod.entity.KoboldGirlEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.jspecify.annotations.Nullable;

public class KoboldGirlRenderer extends GeoEntityRenderer<KoboldGirlEntity, LivingEntityRenderState> {

    public static final DataTicket<Boolean> IS_QUEEN = DataTicket.create("is_queen", Boolean.class);
    public static final DataTicket<Boolean> IS_REINCARNATING = DataTicket.create("is_reincarnating", Boolean.class);

    @SuppressWarnings({"rawtypes", "unchecked"})
    public KoboldGirlRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new KoboldGirlModel());
        this.shadowRadius = 0.5F;
        this.withRenderLayer(new ItemInHandGeoLayer(renderManager, this, "rigth_item", "left_item"));
    }

    @Override
    public void addRenderData(KoboldGirlEntity animatable, @Nullable Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
        super.addRenderData(animatable, relatedObject, renderState, partialTick);
        if ((Object) renderState instanceof com.geckolib.renderer.base.GeoRenderState geoState) {
            geoState.addGeckolibData(IS_QUEEN, animatable.isAnyQueen());
            geoState.addGeckolibData(IS_REINCARNATING, animatable.isReincarnating());
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void adjustModelBonesForRender(RenderPassInfo renderPassInfo, BoneSnapshots snapshots) {
        super.adjustModelBonesForRender(renderPassInfo, snapshots);
        boolean isQueen = false;
        if (renderPassInfo.renderState() instanceof com.geckolib.renderer.base.GeoRenderState geoState) {
            isQueen = geoState.getOrDefaultGeckolibData(IS_QUEEN, false);
        }

        final boolean queen = isQueen;
        snapshots.ifPresent("corona", s -> {
            s.skipRender(!queen);
            s.skipChildrenRender(!queen);
        });
        snapshots.ifPresent("brazaleteI", s -> {
            s.skipRender(!queen);
            s.skipChildrenRender(!queen);
        });
        snapshots.ifPresent("brazaleteD", s -> {
            s.skipRender(!queen);
            s.skipChildrenRender(!queen);
        });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void scaleModelForRender(RenderPassInfo renderPassInfo, float widthScale, float heightScale) {
        boolean isQueen = false;
        boolean isReincarnating = false;
        if (renderPassInfo.renderState() instanceof com.geckolib.renderer.base.GeoRenderState geoState) {
            isQueen = geoState.getOrDefaultGeckolibData(IS_QUEEN, false);
            isReincarnating = geoState.getOrDefaultGeckolibData(IS_REINCARNATING, false);
        }
        if (isReincarnating) {
            float jx = (float) ((Math.random() - 0.5F) * 0.08F);
            float jz = (float) ((Math.random() - 0.5F) * 0.08F);
            renderPassInfo.poseStack().translate(jx, 0.0F, jz);
        }
        float scale = isQueen ? 1.05F : 1.0F;
        super.scaleModelForRender(renderPassInfo, widthScale * scale, heightScale * scale);
    }
}
