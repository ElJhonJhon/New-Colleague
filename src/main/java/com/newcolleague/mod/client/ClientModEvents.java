package com.newcolleague.mod.client;

import com.newcolleague.mod.NewColleagueMod;
import com.newcolleague.mod.entity.ModEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = NewColleagueMod.MODID, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.KOBOLD_GIRL.get(), KoboldGirlRenderer::new);
    }

    @SubscribeEvent
    public static void registerScreens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event) {
        event.register(com.newcolleague.mod.menu.ModMenus.KOBOLD_GIRL_MENU.get(), KoboldGirlScreen::new);
    }
}
