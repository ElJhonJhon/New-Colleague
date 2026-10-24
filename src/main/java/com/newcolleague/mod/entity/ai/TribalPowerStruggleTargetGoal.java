package com.newcolleague.mod.entity.ai;

import com.newcolleague.mod.entity.KoboldGirlEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

/**
 * Meta: Lucha encarnizada por el trono de la tribu (guerra civil entre todas las súbditas)
 */
public class TribalPowerStruggleTargetGoal extends Goal {
    private final KoboldGirlEntity mob;
    private int unseenTicks = 0;

    public TribalPowerStruggleTargetGoal(KoboldGirlEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (!this.mob.isInPowerStruggle() || !this.mob.isAlive()) return false;
        LivingEntity target = this.mob.getTarget();
        if (target != null && target.isAlive() && target instanceof KoboldGirlEntity k && k.isInPowerStruggle()) {
            if (this.mob.hasLineOfSight(k)) {
                return false;
            }
        }

        // NO detectar a través de muros: Solo rivales con línea de visión directa
        List<KoboldGirlEntity> rivals = this.mob.level().getEntitiesOfClass(
                KoboldGirlEntity.class,
                this.mob.getBoundingBox().inflate(24.0D, 8.0D, 24.0D),
                k -> k != this.mob && k.isAlive() && k.isInPowerStruggle() && this.mob.hasLineOfSight(k)
        );
        if (rivals.isEmpty()) return false;

        // Atacar al rival visible más cercano en la guerra por el poder
        rivals.sort(Comparator.comparingDouble(this.mob::distanceToSqr));
        KoboldGirlEntity targetRival = rivals.get(0);

        this.mob.setTarget(targetRival);
        this.mob.equipForCombat(true);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.mob.isInPowerStruggle() || !this.mob.isAlive()) return false;
        LivingEntity target = this.mob.getTarget();
        if (target == null || !target.isAlive() || !(target instanceof KoboldGirlEntity k) || !k.isInPowerStruggle()) {
            return false;
        }
        if (this.mob.distanceToSqr(k) > 36.0D * 36.0D) {
            return false;
        }
        if (this.unseenTicks > 80 && this.mob.distanceToSqr(k) > 16.0D) {
            return false;
        }
        return true;
    }

    @Override
    public void start() {
        this.unseenTicks = 0;
    }

    @Override
    public void stop() {
        this.unseenTicks = 0;
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target != null) {
            if (!this.mob.hasLineOfSight(target)) {
                this.unseenTicks++;
            } else {
                this.unseenTicks = 0;
            }
        }
    }
}
