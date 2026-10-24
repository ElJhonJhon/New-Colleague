package com.newcolleague.mod.entity.ai;

import com.newcolleague.mod.entity.KoboldGirlEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

/**
 * Meta: La Reina de la tribu se aproxima pacífica y respetuosamente al portador del Báculo Sagrado
 */
public class KoboldQueenApproachBaculoGoal extends Goal {
    private final KoboldGirlEntity mob;
    private Player targetPlayer;
    private int repathTimer = 0;
    private int particleTimer = 0;
    private boolean hasArrived = false;

    public KoboldQueenApproachBaculoGoal(KoboldGirlEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.mob.isAlive() || !this.mob.isAnyQueen() || this.mob.isInQueenDuel() || this.mob.isInPlayerDuel() || this.mob.isSleeping()) {
            return false;
        }
        this.targetPlayer = KoboldGirlEntity.findNearbyBaculoPlayer(this.mob, 24.0D);
        return this.targetPlayer != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.mob.isAlive() || !this.mob.isAnyQueen() || this.mob.isInQueenDuel() || this.mob.isInPlayerDuel() || this.mob.isSleeping()) {
            return false;
        }
        if (this.targetPlayer == null || !this.targetPlayer.isAlive() || this.targetPlayer.isSpectator()) {
            return false;
        }
        if (!this.mob.isReveringBaculoHolder(this.targetPlayer)) {
            return false;
        }
        return this.mob.distanceToSqr(this.targetPlayer) <= 28.0D * 28.0D
                && this.mob.hasLineOfSight(this.targetPlayer);
    }

    @Override
    public void start() {
        this.mob.markTribeWitnessedBaculo();
        this.mob.setApproachingBaculo(true);
        if (this.mob.getTarget() != null) {
            this.mob.setTarget(null);
        }
        if (this.mob.isUsingItem()) {
            this.mob.stopUsingItem();
        }
        this.mob.unequipHands();
        this.repathTimer = 0;
        this.particleTimer = 0;
        this.hasArrived = false;
    }

    @Override
    public void stop() {
        this.mob.markTribeWitnessedBaculo();
        this.mob.setApproachingBaculo(false);
        this.mob.getNavigation().stop();
        this.targetPlayer = null;
        this.hasArrived = false;
    }

    @Override
    public void tick() {
        if (this.targetPlayer == null) return;

        // Mantener las manos desarmadas para aproximarse con respeto
        if (!this.mob.getMainHandItem().isEmpty() || !this.mob.getOffhandItem().isEmpty()) {
            this.mob.unequipHands();
        }
        if (this.mob.getTarget() != null) {
            this.mob.setTarget(null);
        }

        // Mirar al jugador
        this.mob.getLookControl().setLookAt(this.targetPlayer, 30.0F, 30.0F);
        this.mob.setYBodyRot(this.mob.getYHeadRot());

        double distSq = this.mob.distanceToSqr(this.targetPlayer);
        double approachDist = 2.8D;
        double stopDistSq = approachDist * approachDist;

        if (distSq > stopDistSq) {
            this.repathTimer--;
            if (this.repathTimer <= 0) {
                this.repathTimer = 10;
                this.mob.getNavigation().moveTo(this.targetPlayer, 1.0D);
            }
        } else {
            this.mob.getNavigation().stop();
            if (!this.hasArrived) {
                this.hasArrived = true;
                if (this.mob.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                            this.mob.getX(), this.mob.getY() + 1.3D, this.mob.getZ(),
                            8, 0.3D, 0.3D, 0.3D, 0.05D);
                }
                this.mob.playSound(SoundEvents.VILLAGER_YES, 0.8F, 1.1F);
            }

            this.particleTimer++;
            if (this.particleTimer >= 50) {
                this.particleTimer = 0;
                if (this.mob.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                            this.mob.getX(), this.mob.getY() + 1.2D, this.mob.getZ(),
                            2, 0.25D, 0.25D, 0.25D, 0.02D);
                }
            }
        }
    }
}
