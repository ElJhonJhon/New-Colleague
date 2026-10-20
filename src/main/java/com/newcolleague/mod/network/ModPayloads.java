package com.newcolleague.mod.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModPayloads {

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
                FireballCastPayload.TYPE,
                FireballCastPayload.STREAM_CODEC,
                FireballCastPayload::handle
        );
        registrar.playToClient(
                OpenQueenDialoguePayload.TYPE,
                OpenQueenDialoguePayload.STREAM_CODEC,
                OpenQueenDialoguePayload::handle
        );
        registrar.playToServer(
                QueenDialogueActionPayload.TYPE,
                QueenDialogueActionPayload.STREAM_CODEC,
                QueenDialogueActionPayload::handle
        );
    }
}
