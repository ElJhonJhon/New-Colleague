package com.newcolleague.mod.entity.ai;

import com.newcolleague.mod.entity.KoboldGirlEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;
import java.util.List;

/**
 * Meta: Rebeldes seleccionan objetivos hostiles y protegen su autonomía
 */
public class RebelTargetGoal extends Goal {
    private final KoboldGirlEntity mob;
    private int unseenTicks = 0;

    public RebelTargetGoal(KoboldGirlEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (!this.mob.isRebel() || !this.mob.isAlive() || this.mob.isInPowerStruggle()) return false;
        LivingEntity target = this.mob.getTarget();
        if (target != null && target.isAlive() && this.mob.hasLineOfSight(target)) return false;

        // 1. La evasión pacífica respecto a jugadores es gestionada por KoboldRebelAvoidAndDefendPlayerGoal
        // (evita al jugador sin atacarlo y se mantiene a <= 15 bloques de la Reina)

        // 2. NO detectar a través de muros: Solo enemigas de TRIBUS RIVALES con línea de visión directa
        // (no ataca a las súbditas sometidas de su misma tribu)
        List<KoboldGirlEntity> enemies = this.mob.level().getEntitiesOfClass(
                KoboldGirlEntity.class,
                this.mob.getBoundingBox().inflate(20.0D, 8.0D, 20.0D),
                k -> k != this.mob && k.isAlive() && !k.isRebel()
                        && !this.mob.getTribeName().isEmpty() && !k.getTribeName().isEmpty()
                        && !k.getTribeName().equals(this.mob.getTribeName())
                        && !(k.isAnyQueen() && k.isOwned())
                        && this.mob.hasLineOfSight(k)
        );
        if (enemies.isEmpty()) return false;

        KoboldGirlEntity targetEnemy = null;
        for (KoboldGirlEntity k : enemies) {
            if (k.isQueen()) {
                targetEnemy = k;
                break;
            }
        }
        if (targetEnemy == null) {
            targetEnemy = enemies.get(0);
        }
        this.mob.setTarget(targetEnemy);
        this.mob.equipForCombat(true);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.mob.isRebel() || !this.mob.isAlive() || this.mob.isInPowerStruggle()) return false;
        LivingEntity target = this.mob.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        if (target instanceof Player player) {
            if (player.isCreative() || player.isSpectator() || KoboldGirlEntity.isHoldingBaculo(player) || this.mob.getReprimandedCooldown() > 0) return false;
            if (this.mob.distanceTo(player) > 6.0D) return false;
        } else if (target instanceof KoboldGirlEntity other) {
            if (other.isRebel()) return false;
            if (other.getTribeName().equals(this.mob.getTribeName())) return false;
        } else {
            return false;
        }
        if (this.mob.distanceToSqr(target) > 28.0D * 28.0D) {
            return false;
        }
        if (this.unseenTicks > 80 && this.mob.distanceToSqr(target) > 16.0D) {
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
