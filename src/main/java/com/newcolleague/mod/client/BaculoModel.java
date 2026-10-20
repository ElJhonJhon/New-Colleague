package com.newcolleague.mod.client;

import com.geckolib.model.DefaultedItemGeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.newcolleague.mod.NewColleagueMod;
import com.newcolleague.mod.item.BaculoItem;
import com.newcolleague.mod.item.BaculoTier;
import net.minecraft.resources.Identifier;

public class BaculoModel extends DefaultedItemGeoModel<BaculoItem> {
    public BaculoModel() {
        super(Identifier.fromNamespaceAndPath(NewColleagueMod.MODID, "baculo"));
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        int level = renderState != null ? renderState.getOrDefaultGeckolibData(BaculoRenderer.STAFF_LEVEL, 1) : 1;
        BaculoTier tier = BaculoTier.byLevel(level);
        return tier.getTextureLocation();
    }
}
