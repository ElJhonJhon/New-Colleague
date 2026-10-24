package com.newcolleague.mod.entity.ai;

import com.newcolleague.mod.entity.KoboldGirlEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

/**
 * Meta: La Reina leal defiende a su gobernante aplicando un solo puñetazo disciplinario con los puños a súbditas rebeldes
 */
public class QueenReprimandRebelGoal extends Goal {
    private final KoboldGirlEntity queen;
    private KoboldGirlEntity targetRebel = null;
    private boolean deliveredHit = false;

    public QueenReprimandRebelGoal(KoboldGirlEntity queen) {
        this.queen = queen;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.queen.isAnyQueen() || !this.queen.isOwned() || this.queen.isRebel() || !this.queen.isAlive() || this.queen.isSleeping()) {
            return false;
        }
        if (this.queen.isInPlayerDuel() || this.queen.isInQueenDuel() || this.queen.isInPowerStruggle()) {
            return false;
        }
        if (this.queen.getForcedReprimandTarget() != null) {
            KoboldGirlEntity forced = this.queen.getForcedReprimandTarget();
            if (forced != null && forced.isAlive() && forced.isRebel()) {
                this.targetRebel = forced;
                return true;
            } else {
                this.queen.setForcedReprimandTarget(null);
            }
        }
        if (this.queen.getReprimandCooldown() > 0) {
            return false;
        }
        Player owner = this.queen.getOwner();
        if (owner == null || !owner.isAlive()) {
            return false;
        }

        // Buscar si hay alguna súbdita rebelde de la tribu que esté atacando o teniendo como objetivo al gobernante
        List<KoboldGirlEntity> nearbyRebels = this.queen.level().getEntitiesOfClass(
                KoboldGirlEntity.class,
                this.queen.getBoundingBox().inflate(48.0D, 16.0D, 48.0D),
                k -> k != this.queen && k.isAlive() && k.isRebel()
                        && (k.getTarget() == owner || owner.getLastHurtByMob() == k)
                        && (this.queen.getTribeName().isEmpty() || k.getTribeName().equals(this.queen.getTribeName()) || k.distanceToSqr(this.queen) <= 48.0D * 48.0D)
        );

        if (nearbyRebels.isEmpty()) return false;

        this.targetRebel = nearbyRebels.get(0);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.deliveredHit) return false;
        if (this.targetRebel == null || !this.targetRebel.isAlive() || !this.targetRebel.isRebel()) return false;
        if (!this.queen.isAlive() || this.queen.isSleeping()) return false;
        return this.queen.distanceToSqr(this.targetRebel) <= 48.0D * 48.0D;
    }

    @Override
    public void start() {
        this.deliveredHit = false;
        // Desequipar armas letales para golpear estrictamente con los puños
        this.queen.unequipHands();
        this.queen.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        this.queen.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        if (this.targetRebel != null) {
            this.queen.getNavigation().moveTo(this.targetRebel, 1.45D);
        }
    }

    @Override
    public void stop() {
        this.targetRebel = null;
        this.deliveredHit = false;
        this.queen.setTarget(null);
        this.queen.setForcedReprimandTarget(null);
    }

    @Override
    public void tick() {
        if (this.targetRebel == null || !this.targetRebel.isAlive() || this.deliveredHit) return;

        // Asegurar que las manos permanezcan desarmadas (golpe con los puños)
        if (!this.queen.getMainHandItem().isEmpty() || !this.queen.getOffhandItem().isEmpty()) {
            this.queen.unequipHands();
        }

        this.queen.getLookControl().setLookAt(this.targetRebel, 30.0F, 30.0F);

        if (this.queen.getNavigation().isDone() || this.queen.tickCount % 10 == 0) {
            this.queen.getNavigation().moveTo(this.targetRebel, 1.45D);
        }

        double distSqr = this.queen.distanceToSqr(this.targetRebel);
        if (distSqr <= 5.5D) { // Alcance cuerpo a cuerpo (aprox 2.3 bloques)
            // Entregar exactamente UN golpe con los puños para reprender
            this.deliveredHit = true;
            this.queen.swing(InteractionHand.MAIN_HAND);

            if (this.queen.level() instanceof ServerLevel serverLevel) {
                DamageSource punchSource = this.queen.damageSources().mobAttack(this.queen);
                this.targetRebel.hurtServer(serverLevel, punchSource, 1.0F);

                double dx = this.targetRebel.getX() - this.queen.getX();
                double dz = this.targetRebel.getZ() - this.queen.getZ();
                this.targetRebel.knockback(0.65D, -dx, -dz, punchSource, 1.0F, false);

                serverLevel.sendParticles(ParticleTypes.CRIT, this.targetRebel.getX(), this.targetRebel.getY() + 1.2D, this.targetRebel.getZ(), 10, 0.3D, 0.3D, 0.3D, 0.05D);
                serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER, this.targetRebel.getX(), this.targetRebel.getY() + 1.4D, this.targetRebel.getZ(), 8, 0.3D, 0.3D, 0.3D, 0.02D);
            }

            this.queen.playSound(SoundEvents.PLAYER_ATTACK_KNOCKBACK, 1.0F, 1.2F);
            this.targetRebel.playSound(SoundEvents.VILLAGER_NO, 1.0F, 1.2F);

            // La rebelde detiene inmediatamente su ataque contra el jugador
            this.targetRebel.setTarget(null);
            this.targetRebel.unequipHands();
            this.targetRebel.setReprimandedCooldown(300); // 15 segundos sin volver a atacar

            // La rebelde retrocede asustada y avergonzada sin separarse a más de 14 bloques de la Reina
            Vec3 away = this.targetRebel.findPosAwayFromWithAnchor(this.queen.position(), 8.0D, this.queen.position(), 14.0D);
            if (away != null) {
                this.targetRebel.getNavigation().moveTo(away.x, away.y, away.z, 1.3D);
            }

            // Diálogo de reprimenda de la Reina
            String[] reprimands = {
                "¡Basta! ¡No permitiré que ataques a nuestro gobernante!",
                "¡Compórtate! Acepta el mandato o retírate, pero no toleraré tu insubordinación.",
                "¡Suficiente insolencia! ¡Respeta a quien ahora guía con honor a nuestra tribu!",
                "¡Basta ya! ¡Acepta con respeto el nuevo nombre que nuestro gobernante ha otorgado a la tribu!"
            };
            String rebuke = reprimands[this.queen.getRandom().nextInt(reprimands.length)];
            Component speaker = Component.literal("<" + this.queen.getQueenTitle() + "> ").withStyle(ChatFormatting.GOLD);
            Component msg = Component.literal(rebuke).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD);

            Player owner = this.queen.getOwner();
            if (owner != null) {
                owner.sendSystemMessage(speaker.copy().append(msg));
            }

            // La Reina cesa inmediatamente la agresión
            this.queen.setTarget(null);
            this.queen.setForcedReprimandTarget(null);

            // Si aún hay otra rebelde atacando al dueño, dejar reprimandCooldown en 0 para reprenderla de inmediato
            boolean hasMoreRebelsAttacking = false;
            if (owner != null && this.queen.level() instanceof ServerLevel sl) {
                hasMoreRebelsAttacking = !sl.getEntitiesOfClass(
                        KoboldGirlEntity.class,
                        this.queen.getBoundingBox().inflate(48.0D, 16.0D, 48.0D),
                        k -> k != this.queen && k != this.targetRebel && k.isAlive() && k.isRebel()
                                && (k.getTarget() == owner || owner.getLastHurtByMob() == k)
                ).isEmpty();
            }
            this.queen.setReprimandCooldown(hasMoreRebelsAttacking ? 0 : 100);
            this.queen.getNavigation().stop();
        }
    }
}
