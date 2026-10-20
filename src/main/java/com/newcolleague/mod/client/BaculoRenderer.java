package com.newcolleague.mod.client;

import com.geckolib.renderer.GeoItemRenderer;
import com.newcolleague.mod.item.BaculoItem;

public class BaculoRenderer extends GeoItemRenderer<BaculoItem> {

    public BaculoRenderer() {
        super(new BaculoModel());
    }
}
