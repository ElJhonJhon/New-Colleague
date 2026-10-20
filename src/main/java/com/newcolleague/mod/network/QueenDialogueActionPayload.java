package com.newcolleague.mod.network;

import com.newcolleague.mod.NewColleagueMod;
import com.newcolleague.mod.entity.KoboldGirlEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record QueenDialogueActionPayload(int queenEntityId, int actionId) implements CustomPacketPayload {

    public static final int ACTION_JUST_PASSING = 1;
    public static final int ACTION_CHALLENGE_DUEL = 2;
    public static final int ACTION_PASSIVE_PROGRESS = 3;

    public static final Type<QueenDialogueActionPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(NewColleagueMod.MODID, "queen_dialogue_action"));

    public static final StreamCodec<ByteBuf, QueenDialogueActionPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, QueenDialogueActionPayload::queenEntityId,
                    ByteBufCodecs.VAR_INT, QueenDialogueActionPayload::actionId,
                    QueenDialogueActionPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(QueenDialogueActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            Entity entity = player.level().getEntity(payload.queenEntityId());
            if (entity instanceof KoboldGirlEntity queen && queen.isAnyQueen() && queen.isAlive()) {
                queen.handleDialogueAction(player, payload.actionId());
            }
        });
    }
}
