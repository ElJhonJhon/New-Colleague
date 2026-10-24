package com.newcolleague.mod.entity.ai;

import com.newcolleague.mod.entity.KoboldGirlEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;

import java.util.EnumSet;
import java.util.List;

/**
 * Meta: Guardias y Vigilantes defienden activamente a todas las demás Kobold Girls de su tribu
 */
public class KoboldDefendAlliesGoal extends Goal {
    private final KoboldGirlEntity mob;
    private LivingEntity threatToTarget;

    public KoboldDefendAlliesGoal(KoboldGirlEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (!this.mob.isDefender() || !this.mob.isAlive() || this.mob.isSleeping()) return false;
        if (this.mob.isInQueenDuel() || this.mob.isInPowerStruggle()) return false;
        LivingEntity target = this.mob.getTarget();
        if (target != null && target.isAlive() && this.mob.hasLineOfSight(target)) return false;

        this.threatToTarget = this.findThreatAgainstAllies();
        return this.threatToTarget != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.mob.isDefender() || !this.mob.isAlive() || this.mob.isSleeping()) return false;
        if (this.mob.isInQueenDuel() || this.mob.isInPowerStruggle()) return false;
        return this.threatToTarget != null && this.threatToTarget.isAlive() && this.mob.hasLineOfSight(this.threatToTarget);
    }

    @Override
    public void start() {
        if (this.threatToTarget != null) {
            this.mob.setTarget(this.threatToTarget);
            this.mob.equipForCombat(true);
        }
    }

    @Override
    public void stop() {
        this.threatToTarget = null;
    }

    private LivingEntity findThreatAgainstAllies() {
        List<KoboldGirlEntity> allies = this.mob.level().getEntitiesOfClass(
                KoboldGirlEntity.class,
                this.mob.getBoundingBox().inflate(24.0D, 8.0D, 24.0D),
                k -> k != this.mob && k.isAlive() && !k.isInQueenDuel() && k.getTribeName().equals(this.mob.getTribeName())
        );

        for (KoboldGirlEntity ally : allies) {
            LivingEntity attacker = ally.getLastHurtByMob();
            if (attacker != null && attacker.isAlive() && ally.tickCount - ally.getLastHurtByMobTimestamp() < 100) {
                if (this.isValidThreat(attacker) && (this.mob.hasLineOfSight(attacker) || (this.mob.hasLineOfSight(ally) && this.mob.distanceToSqr(attacker) <= 16.0D))) {
                    return attacker;
                }
            }

            LivingEntity allyTarget = ally.getTarget();
            if (allyTarget != null && allyTarget.isAlive() && !(allyTarget instanceof Animal)) {
                if (this.isValidThreat(allyTarget) && (this.mob.hasLineOfSight(allyTarget) || (this.mob.hasLineOfSight(ally) && this.mob.distanceToSqr(allyTarget) <= 16.0D))) {
                    return allyTarget;
                }
            }
        }
        return null;
    }

    private boolean isValidThreat(LivingEntity threat) {
        if (threat == this.mob) return false;
        if (threat instanceof KoboldGirlEntity koboldThreat && koboldThreat.isRebel() && this.mob.isOwned()) {
            return false;
        }
        if (threat instanceof KoboldGirlEntity koboldThreat) {
            return this.mob.canFightKobold(koboldThreat);
        }
        return this.mob.canAttack(threat);
    }
}
