package com.newcolleague.mod.client;

import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.newcolleague.mod.item.BaculoItem;

public class BaculoRenderer extends GeoItemRenderer<BaculoItem> {

    public static final DataTicket<Integer> STAFF_LEVEL = DataTickets.create("staff_level", Integer.class);

    public BaculoRenderer() {
        super(new BaculoModel());
    }

    @Override
    public void captureDefaultRenderState(BaculoItem animatable, GeoItemRenderer.RenderData renderData, GeoRenderState renderState, float partialTick) {
        super.captureDefaultRenderState(animatable, renderData, renderState, partialTick);
        int level = BaculoItem.getStaffLevel(renderData.itemStack());
        renderState.addGeckolibData(STAFF_LEVEL, level);
    }
}
