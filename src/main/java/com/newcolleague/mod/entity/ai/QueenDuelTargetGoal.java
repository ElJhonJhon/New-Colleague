package com.newcolleague.mod.entity.ai;

import com.newcolleague.mod.entity.KoboldGirlEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

/**
 * Meta: Duelo a muerte entre Reinas de tribus rivales
 */
public class QueenDuelTargetGoal extends Goal {
    private final KoboldGirlEntity mob;
    private KoboldGirlEntity rivalQueen;
    private int unseenTicks = 0;

    public QueenDuelTargetGoal(KoboldGirlEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (!this.mob.isQueen() || !this.mob.hasTribe() || !this.mob.isAlive()) {
            return false;
        }
        LivingEntity currentTarget = this.mob.getTarget();
        if (currentTarget instanceof KoboldGirlEntity rival && rival.isQueen() && rival.isAlive() && rival.hasTribe()
                && !rival.getTribeName().equals(this.mob.getTribeName()) && this.mob.hasLineOfSight(rival)) {
            this.rivalQueen = rival;
            return true;
        }
        // NO detectar a través de muros: Solo reinas rivales vivas con línea de visión directa
        List<KoboldGirlEntity> candidates = this.mob.level().getEntitiesOfClass(
                KoboldGirlEntity.class,
                this.mob.getBoundingBox().inflate(24.0D, 8.0D, 24.0D),
                q -> q != this.mob && q.isQueen() && q.isAlive() && q.hasTribe()
                        && !q.getTribeName().equals(this.mob.getTribeName())
                        && this.mob.hasLineOfSight(q)
        );
        if (!candidates.isEmpty()) {
            candidates.sort(Comparator.comparingDouble(this.mob::distanceToSqr));
            this.rivalQueen = candidates.get(0);
            return true;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.mob.isQueen() || !this.mob.isAlive() || !this.mob.hasTribe()) {
            return false;
        }
        LivingEntity target = this.mob.getTarget();
        if (!(target instanceof KoboldGirlEntity rival) || !rival.isQueen() || !rival.isAlive()
                || !rival.hasTribe() || rival.getTribeName().equals(this.mob.getTribeName())) {
            return false;
        }
        if (this.mob.distanceToSqr(rival) > 36.0D * 36.0D) {
            return false;
        }
        // Si la reina rival se oculta tras muros por más de 80 ticks (4 seg) y no está en rango cuerpo a cuerpo, de-escalar
        if (this.unseenTicks > 80 && this.mob.distanceToSqr(rival) > 16.0D) {
            return false;
        }
        return true;
    }

    @Override
    public void start() {
        this.unseenTicks = 0;
        if (this.rivalQueen != null && this.rivalQueen.isAlive()) {
            if (this.mob.isSleeping()) this.mob.stopSleeping();
            if (this.rivalQueen.isSleeping() && this.rivalQueen.hasLineOfSight(this.mob)) {
                this.rivalQueen.stopSleeping();
            }

            Vec3 center = this.mob.position().add(this.rivalQueen.position()).scale(0.5D);
            this.mob.setDuelArenaCenter(center);
            this.rivalQueen.setDuelArenaCenter(center);

            this.mob.setTarget(this.rivalQueen);
            if (this.rivalQueen.hasLineOfSight(this.mob) && this.rivalQueen.getTarget() != this.mob) {
                this.rivalQueen.setTarget(this.mob);
                this.rivalQueen.equipForCombat(true);
            }
            this.mob.equipForCombat(true);
        }
    }

    @Override
    public void stop() {
        this.mob.setDuelArenaCenter(null);
        if (this.rivalQueen != null) {
            this.rivalQueen.setDuelArenaCenter(null);
        }
        this.rivalQueen = null;
        this.unseenTicks = 0;
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target instanceof KoboldGirlEntity rival && rival.isQueen() && rival.isAlive()) {
            if (!this.mob.hasLineOfSight(rival)) {
                this.unseenTicks++;
            } else {
                this.unseenTicks = 0;
                if (rival.getTarget() != this.mob && rival.hasLineOfSight(this.mob)) {
                    rival.setTarget(this.mob);
                    rival.equipForCombat(true);
                }
            }
        }
    }
}
