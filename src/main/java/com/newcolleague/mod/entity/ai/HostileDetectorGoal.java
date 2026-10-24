package com.newcolleague.mod.entity.ai;

import com.newcolleague.mod.entity.KoboldGirlEntity;
import com.newcolleague.mod.entity.AbstractTribalEntity.KoboldProfession;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

/**
 * Meta: Detectar entidades agresivas (Radio extendido a 16 bloques para patrullas de Guardias y Vigilantes)
 */
public class HostileDetectorGoal extends NearestAttackableTargetGoal<LivingEntity> {
    private static final double RANGE = 10.0D;
    private final KoboldGirlEntity kobold;

    public HostileDetectorGoal(KoboldGirlEntity mob) {
        super(mob, LivingEntity.class, 0, true, false, (target, level) -> {
            if (target instanceof KoboldGirlEntity) return false;
            double maxRange = mob.isDefender() ? 16.0D : RANGE;
            if (mob.distanceToSqr(target) > (maxRange * maxRange)) return false;
            if (!mob.hasLineOfSight(target)) return false;
            if (target instanceof Enemy || target instanceof Monster) return true;
            if (target instanceof Mob otherMob && otherMob.getTarget() instanceof Player) return true;
            return false;
        });
        this.kobold = mob;
    }

    @Override
    public boolean canUse() {
        if (this.kobold.isSleeping() || this.kobold.isInPowerStruggle()) {
            return false;
        }
        if (this.kobold.getProfession() == KoboldProfession.FARMER || this.kobold.getProfession() == KoboldProfession.HUNTER) {
            return false;
        }
        return super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        if (this.kobold.isSleeping() || this.kobold.isInPowerStruggle()) {
            return false;
        }
        if (this.kobold.getProfession() == KoboldProfession.FARMER || this.kobold.getProfession() == KoboldProfession.HUNTER) {
            return false;
        }
        LivingEntity target = this.mob.getTarget();
        if (target != null && !this.mob.hasLineOfSight(target)) {
            return false;
        }
        return super.canContinueToUse();
    }

    @Override
    protected AABB getTargetSearchArea(double targetDistance) {
        return this.mob.getBoundingBox().inflate(RANGE, 4.0D, RANGE);
    }
}
