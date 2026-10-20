package com.newcolleague.mod.client;

import com.geckolib.model.DefaultedItemGeoModel;
import com.newcolleague.mod.NewColleagueMod;
import com.newcolleague.mod.item.BaculoItem;
import net.minecraft.resources.Identifier;

public class BaculoModel extends DefaultedItemGeoModel<BaculoItem> {
    public BaculoModel() {
        super(Identifier.fromNamespaceAndPath(NewColleagueMod.MODID, "baculo"));
    }
}
