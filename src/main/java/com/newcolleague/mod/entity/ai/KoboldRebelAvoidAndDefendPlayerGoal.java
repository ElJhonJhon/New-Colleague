package com.newcolleague.mod.entity.ai;

import com.newcolleague.mod.entity.KoboldGirlEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;

/**
 * Meta: Súbditas rebeldes se alejan del jugador sin atacarlo (incluso si se acerca a <= 3 bloques),
 * no se separan de la Reina a más de 15 bloques, y solo permiten acercamiento si el jugador sostiene el báculo en mano
 */
public class KoboldRebelAvoidAndDefendPlayerGoal extends Goal {
    private final KoboldGirlEntity mob;
    private Player targetPlayer = null;
    private int fleeCooldown = 0;

    public KoboldRebelAvoidAndDefendPlayerGoal(KoboldGirlEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.mob.isRebel() || !this.mob.isAlive() || this.mob.isSleeping() || this.mob.isInPowerStruggle()) {
            return false;
        }
        KoboldGirlEntity queen = this.mob.getTribeQueen();
        // Si la reina está viva y la rebelde se alejó a más de 150 bloques de la reina, activar para regresar hacia ella
        if (queen != null && queen.isAlive() && this.mob.distanceTo(queen) > 150.0D) {
            return true;
        }
        // Buscar al jugador más cercano en un radio de 14 bloques
        Player nearest = this.mob.level().getNearestPlayer(this.mob, 14.0D);
        if (nearest == null || !nearest.isAlive() || nearest.isSpectator() || nearest.isCreative()) {
            return false;
        }
        if (!this.mob.hasLineOfSight(nearest)) {
            return false;
        }
        this.targetPlayer = nearest;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.mob.isRebel() || !this.mob.isAlive() || this.mob.isSleeping() || this.mob.isInPowerStruggle()) {
            return false;
        }
        KoboldGirlEntity queen = this.mob.getTribeQueen();
        if (queen != null && queen.isAlive() && this.mob.distanceTo(queen) > 15.0D) {
            return true;
        }
        if (this.targetPlayer == null || !this.targetPlayer.isAlive() || this.targetPlayer.isSpectator() || this.targetPlayer.isCreative()) {
            return false;
        }
        return this.mob.distanceToSqr(this.targetPlayer) <= 16.0D * 16.0D;
    }

    @Override
    public void start() {
        this.fleeCooldown = 0;
    }

    @Override
    public void stop() {
        this.targetPlayer = null;
    }

    @Override
    public void tick() {
        if (this.fleeCooldown > 0) this.fleeCooldown--;

        KoboldGirlEntity queen = this.mob.getTribeQueen();

        // 1. REGLA ESTRICTA DE PROXIMIDAD: No separarse a más de 150 bloques de la Reina
        // "esas rebeldes que rechazan AL JUGADOR no se deben separar de la reina a mas de 15 bloques"
        if (queen != null && queen.isAlive()) {
            double distToQueen = this.mob.distanceTo(queen);
            if (distToQueen > 150.0D) {
                if (this.mob.getTarget() != null && this.mob.getTarget() instanceof Player) {
                    this.mob.setTarget(null);
                    this.mob.unequipHands();
                }
                if (this.mob.getNavigation().isDone() || this.mob.tickCount % 20 == 0) {
                    this.mob.getNavigation().moveTo(queen, 1.35D);
                }
                this.mob.getLookControl().setLookAt(queen, 30.0F, 30.0F);
                return;
            }
        }

        // Actualizar o verificar jugador objetivo
        if (this.targetPlayer == null || !this.targetPlayer.isAlive() || this.targetPlayer.isSpectator() || this.targetPlayer.isCreative()) {
            this.targetPlayer = this.mob.level().getNearestPlayer(this.mob, 14.0D);
        }
        if (this.targetPlayer == null) return;

        double distToPlayer = this.mob.distanceTo(this.targetPlayer);

        // 2. Si el jugador tiene el báculo en mano:
        // "la unica manera que le permitan acercarse sea si el jugador tiene el báculo en mano"
        if (KoboldGirlEntity.isHoldingBaculo(this.targetPlayer)) {
            if (this.mob.getTarget() == this.targetPlayer) {
                this.mob.setTarget(null);
                this.mob.unequipHands();
            }
            // Permitir que el jugador se acerque sin huir ni atacar
            this.mob.getLookControl().setLookAt(this.targetPlayer, 30.0F, 30.0F);
            this.mob.getNavigation().stop();
            return;
        }

        // 3. Si el jugador NO tiene el báculo en mano:
        Vec3 queenPos = (queen != null && queen.isAlive()) ? queen.position() : null;

        // Si la rebelde fue recientemente reprendida por la Reina: huir activamente sin volver a atacar
        if (this.mob.getReprimandedCooldown() > 0) {
            if (this.mob.getTarget() == this.targetPlayer) {
                this.mob.setTarget(null);
                this.mob.unequipHands();
            }
            this.mob.getLookControl().setLookAt(this.targetPlayer, 30.0F, 30.0F);
            double speed = distToPlayer <= 3.0D ? 1.35D : 1.2D;
            this.fleeFromWithAnchor(this.targetPlayer.position(), 8.0D, queenPos, 15.0D, speed);
            return;
        }

        // Si la rebelde ya está atacando activamente al jugador (por ejemplo, en protesta por cambio de nombre o por provocación):
        if (this.mob.getTarget() == this.targetPlayer) {
            if (distToPlayer > 18.0D) {
                this.mob.setTarget(null);
                this.mob.unequipHands();
            } else {
                this.mob.getLookControl().setLookAt(this.targetPlayer, 30.0F, 30.0F);
                return; // Permitir que KoboldAdaptiveCombatGoal ejecute el combate hacia el jugador hasta la reprimenda de la Reina
            }
        }

        // Si no está atacando y el jugador está a más de 3 bloques: alejarse de él manteniéndose cerca de la Reina
        if (distToPlayer > 3.0D) {
            this.mob.getLookControl().setLookAt(this.targetPlayer, 30.0F, 30.0F);
            this.fleeFromWithAnchor(this.targetPlayer.position(), 8.0D, queenPos, 15.0D, 1.2D);
        } else {
            // Si el jugador se acerca a <= 3 bloques sin el báculo en mano:
            // "las rebeldes son las que tienen que atacar al jugador si se acerca demasiado y es ahí cuando la reina se debe acercar para reprenderlas"
            if (this.mob.getTarget() != this.targetPlayer) {
                this.mob.setTarget(this.targetPlayer);
                this.mob.equipForCombat(true);
            }
            this.mob.getLookControl().setLookAt(this.targetPlayer, 30.0F, 30.0F);
        }
    }

    private void fleeFromWithAnchor(Vec3 dangerPos, double distance, @Nullable Vec3 anchorPos, double maxDistFromAnchor, double speedModifier) {
        if (this.mob.getNavigation().isDone() || this.fleeCooldown <= 0) {
            this.fleeCooldown = 15;
            Vec3 awayPos = this.mob.findPosAwayFromWithAnchor(dangerPos, distance, anchorPos, maxDistFromAnchor);
            if (awayPos != null) {
                this.mob.getNavigation().moveTo(awayPos.x, awayPos.y, awayPos.z, speedModifier);
            } else if (anchorPos != null) {
                this.mob.getNavigation().moveTo(anchorPos.x, anchorPos.y, anchorPos.z, speedModifier);
            }
        }
    }
}
