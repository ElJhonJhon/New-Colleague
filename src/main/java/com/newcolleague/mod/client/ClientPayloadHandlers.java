package com.newcolleague.mod.client;

import com.newcolleague.mod.network.OpenQueenDialoguePayload;
import net.minecraft.client.Minecraft;

public class ClientPayloadHandlers {

    public static void handleOpenDialogue(OpenQueenDialoguePayload payload) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            mc.setScreenAndShow(new QueenDialogueScreen(
                    payload.queenEntityId(),
                    payload.queenTitle(),
                    payload.tribeName(),
                    payload.pacifistRouteChosen()
            ));
        }
    }
}
