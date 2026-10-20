package com.newcolleague.mod;

import com.newcolleague.mod.entity.KoboldGirlEntity;
import com.newcolleague.mod.entity.ModCreativeTabs;
import com.newcolleague.mod.entity.ModEntities;
import com.newcolleague.mod.entity.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(NewColleagueMod.MODID)
public class NewColleagueMod {
    public static final String MODID = "newcolleaguemod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public NewColleagueMod(IEventBus modEventBus) {
        LOGGER.info("Initializing {}", MODID);

        // Registros en el bus de eventos del mod
        ModEntities.ENTITIES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        com.newcolleague.mod.menu.ModMenus.MENUS.register(modEventBus);

        // Registro de paquetes de red
        modEventBus.addListener(com.newcolleague.mod.network.ModPayloads::register);

        // Atributos de la entidad
        modEventBus.addListener(this::registerAttributes);
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.KOBOLD_GIRL.get(), KoboldGirlEntity.createAttributes().build());
    }
}
