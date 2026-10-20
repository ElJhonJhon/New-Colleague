package com.newcolleague.mod.network;

import com.newcolleague.mod.NewColleagueMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenQueenDialoguePayload(int queenEntityId, String queenTitle, String tribeName, boolean pacifistRouteChosen, boolean isRuler) implements CustomPacketPayload {

    public static final Type<OpenQueenDialoguePayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(NewColleagueMod.MODID, "open_queen_dialogue"));

    public static final StreamCodec<ByteBuf, OpenQueenDialoguePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, OpenQueenDialoguePayload::queenEntityId,
                    ByteBufCodecs.STRING_UTF8, OpenQueenDialoguePayload::queenTitle,
                    ByteBufCodecs.STRING_UTF8, OpenQueenDialoguePayload::tribeName,
                    ByteBufCodecs.BOOL, OpenQueenDialoguePayload::pacifistRouteChosen,
                    ByteBufCodecs.BOOL, OpenQueenDialoguePayload::isRuler,
                    OpenQueenDialoguePayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenQueenDialoguePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            com.newcolleague.mod.client.ClientPayloadHandlers.handleOpenDialogue(payload);
        });
    }
}
