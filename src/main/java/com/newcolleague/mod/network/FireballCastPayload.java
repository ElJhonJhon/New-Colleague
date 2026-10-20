package com.newcolleague.mod.network;

import com.newcolleague.mod.NewColleagueMod;
import com.newcolleague.mod.entity.ModItems;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record FireballCastPayload(int chargeTicks) implements CustomPacketPayload {
    public static final Type<FireballCastPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(NewColleagueMod.MODID, "fireball_cast"));

    public static final StreamCodec<ByteBuf, FireballCastPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, FireballCastPayload::chargeTicks,
                    FireballCastPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(FireballCastPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            ItemStack stack = player.getMainHandItem();
            if (!stack.is(ModItems.BACULO.get())) return;
            if (player.getCooldowns().isOnCooldown(stack)) return;

            Level level = player.level();
            Vec3 eyePos = player.getEyePosition();
            Vec3 look = player.getLookAngle();
            Vec3 spawnPos = eyePos.add(look.scale(1.2));

            if (payload.chargeTicks() >= 25) {
                // Carga completa: Bola de fuego grande con explosión y alto impacto
                LargeFireball largeFireball = new LargeFireball(level, player, look, 1);
                largeFireball.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
                level.addFreshEntity(largeFireball);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.GHAST_SHOOT, SoundSource.PLAYERS, 1.0F, 0.8F);
            } else if (payload.chargeTicks() >= 10) {
                // Carga media: Bola de fuego rápida que quema al impacto
                SmallFireball smallFireball = new SmallFireball(level, player, look);
                smallFireball.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
                level.addFreshEntity(smallFireball);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0F, 1.1F);
            } else {
                return;
            }

            stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
            player.getCooldowns().addCooldown(stack, 20); // 1 segundo de cooldown
        });
    }
}
