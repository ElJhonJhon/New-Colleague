package com.newcolleague.mod.client;

import com.newcolleague.mod.NewColleagueMod;
import com.newcolleague.mod.entity.ModItems;
import com.newcolleague.mod.network.FireballCastPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

@EventBusSubscriber(modid = NewColleagueMod.MODID, value = Dist.CLIENT)
public class BaculoClientHandler {

    private static int leftClickCharge = 0;
    public static final int MIN_FIREBALL_CHARGE = 10;
    public static final int MAX_FIREBALL_CHARGE = 25;

    public static int getLeftClickCharge() {
        return leftClickCharge;
    }

    /**
     * Calcula la velocidad de animación del báculo según si se está atacando o curando.
     * En reposo es 1.0f, pero acelera progresivamente al cargar la bola de fuego (hasta 5x)
     * o se acelera a 3.5x mientras se canaliza la curación.
     */
    public static float getStaffAnimationSpeed() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return 1.0f;
        int charge = leftClickCharge;
        boolean isUsingRight = mc.player.isUsingItem() && (mc.player.getUseItem().is(ModItems.BACULO.get()) || mc.player.getMainHandItem().is(ModItems.BACULO.get()));

        if (charge > 0) {
            // Acelera entre 2.2x y 5.5x según el nivel de carga de bola de fuego
            return 2.2f + Math.min(charge, MAX_FIREBALL_CHARGE) * 0.14f;
        } else if (isUsingRight) {
            // Acelera a 3.8x mientras canaliza regeneración
            return 3.8f;
        }
        return 1.0f;
    }

    /**
     * Calcula la posición exacta 3D en el mundo de la punta del báculo
     * para que las partículas se generen directamente en el bloque flotante superior.
     */
    public static Vec3 getStaffTipPosition(Player player, boolean isUsingRight) {
        Minecraft mc = Minecraft.getInstance();
        Vec3 eyePos = player.getEyePosition();
        Vec3 look = player.getLookAngle();

        if (mc.options.getCameraType().isFirstPerson() && player == mc.player) {
            double rad = Math.toRadians(player.getYRot());
            Vec3 right = new Vec3(-Math.cos(rad), 0, -Math.sin(rad)).normalize();
            Vec3 up = right.cross(look).normalize();

            if (isUsingRight) {
                // Al elevar el báculo en click derecho (curación)
                return eyePos.add(look.scale(0.62)).add(right.scale(0.22)).add(up.scale(0.20));
            } else {
                // Posición estándar en mano principal derecha (flotando sobre el orbe [8, 22, 8])
                return eyePos.add(look.scale(0.65)).add(right.scale(0.36)).add(up.scale(0.14));
            }
        } else {
            // Tercera persona: posición orientada a la altura de la mano/pecho donde está el orbe
            float bodyYaw = player.yBodyRot;
            double bodyRad = Math.toRadians(bodyYaw);
            double forwardX = -Math.sin(bodyRad);
            double forwardZ = Math.cos(bodyRad);
            double rightX = -Math.cos(bodyRad);
            double rightZ = -Math.sin(bodyRad);

            double crouchOffset = player.isCrouching() ? -0.20 : 0.0;

            if (isUsingRight) {
                // Postura de curación (BOW pose): brazo derecho elevado hacia adelante y al centro
                double posX = player.getX() + rightX * 0.22 + forwardX * 0.40;
                double posZ = player.getZ() + rightZ * 0.22 + forwardZ * 0.40;
                double posY = player.getY() + 1.25 + crouchOffset;
                return new Vec3(posX, posY, posZ);
            } else {
                // Postura estándar / carga de ataque con clic izquierdo: báculo sostenido a un costado
                double posX = player.getX() + rightX * 0.35 + forwardX * 0.15;
                double posZ = player.getZ() + rightZ * 0.35 + forwardZ * 0.15;
                double posY = player.getY() + 1.10 + crouchOffset;
                return new Vec3(posX, posY, posZ);
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            leftClickCharge = 0;
            return;
        }

        if (mc.gui.screen() != null) {
            leftClickCharge = 0;
            return;
        }

        ItemStack mainHand = mc.player.getMainHandItem();
        boolean holdingBaculo = mainHand.is(ModItems.BACULO.get());
        if (!holdingBaculo) {
            leftClickCharge = 0;
            return;
        }

        // Partículas mientras se canaliza curación con clic derecho
        boolean isUsingRight = mc.player.isUsingItem() && mc.player.getUseItem().is(ModItems.BACULO.get());
        if (isUsingRight) {
            Vec3 tipPos = getStaffTipPosition(mc.player, true);
            mc.level.addParticle(
                    ParticleTypes.HAPPY_VILLAGER,
                    tipPos.x + (mc.level.getRandom().nextDouble() - 0.5) * 0.15,
                    tipPos.y + 0.05 + (mc.level.getRandom().nextDouble() - 0.5) * 0.15,
                    tipPos.z + (mc.level.getRandom().nextDouble() - 0.5) * 0.15,
                    0.0, 0.03, 0.0
            );
        }

        if (mc.player.getCooldowns().isOnCooldown(mainHand)) {
            leftClickCharge = 0;
            return;
        }

        // Carga de ataque con clic izquierdo
        boolean attackKeyDown = mc.options.keyAttack.isDown();
        if (attackKeyDown) {
            leftClickCharge++;

            Vec3 tipPos = getStaffTipPosition(mc.player, false);

            if (leftClickCharge >= 5) {
                // Partículas flotando sobre el bloque en la punta del báculo
                mc.level.addParticle(
                        ParticleTypes.FLAME,
                        tipPos.x + (mc.level.getRandom().nextDouble() - 0.5) * 0.15,
                        tipPos.y + 0.05 + (mc.level.getRandom().nextDouble() - 0.5) * 0.15,
                        tipPos.z + (mc.level.getRandom().nextDouble() - 0.5) * 0.15,
                        0.0, 0.02, 0.0
                );
                mc.level.addParticle(
                        ParticleTypes.SMOKE,
                        tipPos.x + (mc.level.getRandom().nextDouble() - 0.5) * 0.15,
                        tipPos.y + 0.05 + (mc.level.getRandom().nextDouble() - 0.5) * 0.15,
                        tipPos.z + (mc.level.getRandom().nextDouble() - 0.5) * 0.15,
                        0.0, 0.01, 0.0
                );
            }

            if (leftClickCharge == MAX_FIREBALL_CHARGE) {
                mc.player.playSound(SoundEvents.FIRECHARGE_USE, 0.7F, 1.6F);
            }
        } else {
            if (leftClickCharge >= MIN_FIREBALL_CHARGE) {
                ClientPacketDistributor.sendToServer(new FireballCastPayload(leftClickCharge));
            }
            leftClickCharge = 0;
        }
    }
}
