package com.newcolleague.mod.entity.ai;

import com.newcolleague.mod.entity.KoboldGirlEntity;
import com.newcolleague.mod.entity.AbstractTribalEntity.CommandStance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.PathType;

import java.util.EnumSet;

/**
 * Meta del gobernante: Sigue fielmente al dueño / rey si está en postura FOLLOW
 */
public class KoboldFollowOwnerGoal extends Goal {
    private final KoboldGirlEntity mob;
    private LivingEntity owner;
    private final double speedModifier;
    private int timeToRecalcPath;
    private float oldWaterCost;

    public KoboldFollowOwnerGoal(KoboldGirlEntity mob, double speedModifier) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.mob.isOwned() || this.mob.isRebel() || !this.mob.isAlive() || this.mob.isSleeping()) {
            return false;
        }
        if (this.mob.getCommandStance() != CommandStance.FOLLOW) {
            return false;
        }
        if (this.mob.isInQueenDuel() || this.mob.isInPlayerDuel() || this.mob.isInPowerStruggle()) {
            return false;
        }
        Player owner = this.mob.getOwner();
        if (owner == null || owner.isSpectator()) {
            return false;
        }
        if (this.mob.distanceToSqr(owner) < 9.0D) {
            return false;
        }
        if (this.mob.getTarget() != null && this.mob.getTarget().isAlive() && this.mob.distanceToSqr(owner) < 64.0D) {
            return false;
        }
        this.owner = owner;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.mob.getNavigation().isDone()) {
            return false;
        }
        if (this.mob.getCommandStance() != CommandStance.FOLLOW) {
            return false;
        }
        if (!this.mob.isOwned() || this.mob.isRebel() || !this.mob.isAlive() || this.mob.isSleeping()) {
            return false;
        }
        return this.owner != null && this.mob.distanceToSqr(this.owner) > 4.0D;
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = this.mob.getPathfindingMalus(PathType.WATER);
        this.mob.setPathfindingMalus(PathType.WATER, 0.0F);
    }

    @Override
    public void stop() {
        this.owner = null;
        this.mob.getNavigation().stop();
        this.mob.setPathfindingMalus(PathType.WATER, this.oldWaterCost);
    }

    @Override
    public void tick() {
        if (this.owner == null) return;
        this.mob.getLookControl().setLookAt(this.owner, 10.0F, (float) this.mob.getMaxHeadXRot());
        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = 10;
            double distSqr = this.mob.distanceToSqr(this.owner);
            if (distSqr >= 256.0D) {
                this.teleportToOwner();
            } else {
                this.mob.getNavigation().moveTo(this.owner, this.speedModifier);
            }
        }
    }

    private void teleportToOwner() {
        BlockPos ownerPos = this.owner.blockPosition();
        for (int i = 0; i < 10; i++) {
            int dx = this.mob.getRandom().nextIntBetweenInclusive(-3, 3);
            int dz = this.mob.getRandom().nextIntBetweenInclusive(-3, 3);
            int dy = this.mob.getRandom().nextIntBetweenInclusive(-1, 1);
            if (Math.abs(dx) >= 2 || Math.abs(dz) >= 2) {
                BlockPos targetPos = ownerPos.offset(dx, dy, dz);
                if (KoboldGirlEntity.isBedSideStandable(this.mob.level(), targetPos)) {
                    this.mob.teleportTo(targetPos.getX() + 0.5D, (double) targetPos.getY(), targetPos.getZ() + 0.5D);
                    this.mob.getNavigation().stop();
                    if (this.mob.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.PORTAL, this.mob.getX(), this.mob.getY() + 0.8D, this.mob.getZ(), 10, 0.3D, 0.4D, 0.3D, 0.05D);
                    }
                    return;
                }
            }
        }
    }
}
