package com.newcolleague.mod.network;

import com.newcolleague.mod.NewColleagueMod;
import com.newcolleague.mod.entity.KoboldGirlEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RenameKoboldPayload(int entityId, String newName, boolean isTribeRename) implements CustomPacketPayload {

    public static final Type<RenameKoboldPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(NewColleagueMod.MODID, "rename_kobold"));

    public static final StreamCodec<ByteBuf, RenameKoboldPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, RenameKoboldPayload::entityId,
                    ByteBufCodecs.STRING_UTF8, RenameKoboldPayload::newName,
                    ByteBufCodecs.BOOL, RenameKoboldPayload::isTribeRename,
                    RenameKoboldPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RenameKoboldPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            Entity entity = player.level().getEntity(payload.entityId());
            if (!(entity instanceof KoboldGirlEntity kobold) || !kobold.isAlive()) return;
            if (!kobold.isOwnedBy(player)) {
                player.sendSystemMessage(Component.literal("§cNo tienes permiso para renombrar integrantes de otra tribu."));
                return;
            }

            String cleanedName = payload.newName().trim();
            if (cleanedName.length() > 32) {
                cleanedName = cleanedName.substring(0, 32);
            }

            if (payload.isTribeRename()) {
                if (!kobold.isAnyQueen()) {
                    player.sendSystemMessage(Component.literal("§cSolo la Reina de la tribu puede renombrar la tribu entera."));
                    return;
                }
                if (cleanedName.isEmpty()) {
                    player.sendSystemMessage(Component.literal("§cEl nombre de la tribu no puede estar vacío."));
                    return;
                }
                kobold.renameTribe(player, cleanedName);
            } else {
                kobold.setCustomGivenName(cleanedName);
                kobold.updateRulerCustomName();
                kobold.playSound(SoundEvents.VILLAGER_YES, 1.0F, 1.2F);
                if (kobold.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, kobold.getX(), kobold.getY() + 1.2D, kobold.getZ(), 8, 0.3D, 0.3D, 0.3D, 0.05D);
                }
                if (cleanedName.isEmpty()) {
                    player.sendSystemMessage(Component.literal("§aHas restablecido el nombre predeterminado de esta Kobold."));
                } else {
                    player.sendSystemMessage(Component.literal("§aHas nombrado a esta Kobold como: §e" + cleanedName));
                }
            }
        });
    }
}
