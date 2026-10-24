package com.newcolleague.mod.entity.ai;

import com.newcolleague.mod.entity.KoboldGirlEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Meta: Al levantarse de la cama, caminar por Dirt Path hacia otra zona para deambular
 */
public class KoboldMorningWalkGoal extends Goal {
    private final KoboldGirlEntity mob;
    private BlockPos destination;
    private int timeout;

    public KoboldMorningWalkGoal(KoboldGirlEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.mob.isNeedsMorningWalk()) {
            return false;
        }
        // Guardias y Vigilantes patrullan de forma activa y continua con KoboldPatrolPathsGoal
        if (this.mob.isDefender()) {
            this.mob.setNeedsMorningWalk(false);
            return false;
        }
        if (this.mob.isSleepTime()) {
            this.mob.setNeedsMorningWalk(false);
            return false;
        }
        if (this.mob.getTarget() != null) {
            this.mob.setNeedsMorningWalk(false);
            return false;
        }

        this.destination = this.mob.findMorningWalkDestination();
        if (this.destination == null) {
            // No hay caminos de Dirt Path cercanos o seguros que se alejen de la cama
            this.mob.setNeedsMorningWalk(false);
            return false;
        }
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.mob.isNeedsMorningWalk() || this.mob.isSleepTime() || this.mob.getTarget() != null) {
            return false;
        }
        if (this.destination == null) {
            return false;
        }
        if (this.mob.distanceToSqr(Vec3.atCenterOf(this.destination)) < 4.0D) {
            return false;
        }
        return this.timeout > 0 && !this.mob.getNavigation().isDone();
    }

    @Override
    public void start() {
        this.timeout = 300; // Máximo 15 segundos
        if (this.destination != null) {
            this.mob.getNavigation().moveTo(
                    this.destination.getX() + 0.5D,
                    this.destination.getY(),
                    this.destination.getZ() + 0.5D,
                    1.15D
            );
        }
    }

    @Override
    public void tick() {
        this.timeout--;
        if (this.destination != null && (this.mob.getNavigation().isDone() || this.timeout % 40 == 0)) {
            this.mob.getNavigation().moveTo(
                    this.destination.getX() + 0.5D,
                    this.destination.getY(),
                    this.destination.getZ() + 0.5D,
                    1.15D
            );
        }
    }

    @Override
    public void stop() {
        this.mob.setNeedsMorningWalk(false);
        this.destination = null;
        this.mob.getNavigation().stop();
    }
}
