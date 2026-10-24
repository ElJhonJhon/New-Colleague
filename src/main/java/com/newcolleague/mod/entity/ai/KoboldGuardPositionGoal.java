package com.newcolleague.mod.entity.ai;

import com.newcolleague.mod.entity.KoboldGirlEntity;
import com.newcolleague.mod.entity.AbstractTribalEntity.CommandStance;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Meta del gobernante: Mantener posición vigilando cuando está en postura GUARD
 */
public class KoboldGuardPositionGoal extends Goal {
    private final KoboldGirlEntity mob;
    private final double speedModifier;

    public KoboldGuardPositionGoal(KoboldGirlEntity mob, double speedModifier) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!this.mob.isOwned() || this.mob.isRebel() || !this.mob.isAlive() || this.mob.isSleeping()) return false;
        if (this.mob.getCommandStance() != CommandStance.GUARD) return false;
        if (this.mob.getTarget() != null && this.mob.getTarget().isAlive()) return false;
        BlockPos guardPos = this.mob.getGuardPosition();
        if (guardPos == null) return false;
        return this.mob.distanceToSqr(Vec3.atBottomCenterOf(guardPos)) > 16.0D;
    }

    @Override
    public void start() {
        BlockPos guardPos = this.mob.getGuardPosition();
        if (guardPos != null) {
            this.mob.getNavigation().moveTo(guardPos.getX() + 0.5D, guardPos.getY(), guardPos.getZ() + 0.5D, this.speedModifier);
        }
    }
}
