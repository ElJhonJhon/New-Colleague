package com.newcolleague.mod.entity.ai;

import com.newcolleague.mod.entity.KoboldGirlEntity;
import com.newcolleague.mod.entity.AbstractTribalEntity.KoboldProfession;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

/**
 * Meta: Kobolds aliadas defienden activamente a su dueño / gobernante (estilo lobo domesticado)
 */
public class KoboldDefendOwnerGoal extends Goal {
    private final KoboldGirlEntity mob;
    private LivingEntity threatToTarget;

    public KoboldDefendOwnerGoal(KoboldGirlEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (!this.mob.isOwned() || this.mob.isRebel() || !this.mob.isAlive() || this.mob.isSleeping()) return false;
        if (this.mob.isInQueenDuel() || this.mob.isInPlayerDuel() || this.mob.isInPowerStruggle()) return false;
        if (this.mob.getProfession() == KoboldProfession.FARMER) return false;

        LivingEntity currentTarget = this.mob.getTarget();
        if (currentTarget != null && currentTarget.isAlive() && this.mob.hasLineOfSight(currentTarget)) {
            return false;
        }

        Player owner = this.mob.getOwner();
        if (owner == null || !owner.isAlive() || this.mob.distanceToSqr(owner) > 32.0D * 32.0D) {
            return false;
        }

        // 1. Defender al dueño si fue atacado recientemente
        LivingEntity attacker = owner.getLastHurtByMob();
        if (attacker != null && attacker.isAlive() && (owner.tickCount - owner.getLastHurtByMobTimestamp() < 100)) {
            if (this.isValidThreat(attacker, owner) && (this.mob.hasLineOfSight(attacker) || this.mob.distanceToSqr(attacker) <= 16.0D)) {
                this.threatToTarget = attacker;
                return true;
            }
        }

        // 2. Apoyar al dueño si atacó a un objetivo recientemente
        LivingEntity target = owner.getLastHurtMob();
        if (target != null && target.isAlive() && (owner.tickCount - owner.getLastHurtMobTimestamp() < 100)) {
            if (this.isValidThreat(target, owner) && (this.mob.hasLineOfSight(target) || this.mob.distanceToSqr(target) <= 16.0D)) {
                this.threatToTarget = target;
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.mob.isOwned() || this.mob.isRebel() || !this.mob.isAlive() || this.mob.isSleeping()) return false;
        if (this.mob.isInQueenDuel() || this.mob.isInPlayerDuel() || this.mob.isInPowerStruggle()) return false;
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

    private boolean isValidThreat(LivingEntity threat, Player owner) {
        if (threat == null || threat == this.mob || threat == owner) return false;
        // Las súbditas leales NUNCA atacan a rebeldes de su pueblo (la Reina leal se encarga)
        if (threat instanceof KoboldGirlEntity koboldThreat && koboldThreat.isRebel()) {
            return false;
        }
        if (threat instanceof KoboldGirlEntity otherKobold) {
            return this.mob.canFightKobold(otherKobold);
        }
        return this.mob.canAttack(threat);
    }
}
