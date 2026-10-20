package com.newcolleague.mod.client;

import com.geckolib.model.DefaultedEntityGeoModel;
import com.newcolleague.mod.NewColleagueMod;
import com.newcolleague.mod.entity.KoboldGirlEntity;
import net.minecraft.resources.Identifier;

public class KoboldGirlModel extends DefaultedEntityGeoModel<KoboldGirlEntity> {
    public KoboldGirlModel() {
        super(Identifier.fromNamespaceAndPath(NewColleagueMod.MODID, "kobold_girl"));
    }
}
