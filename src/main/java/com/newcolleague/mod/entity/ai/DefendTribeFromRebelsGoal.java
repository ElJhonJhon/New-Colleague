package com.newcolleague.mod.entity.ai;

import com.newcolleague.mod.entity.KoboldGirlEntity;
import com.newcolleague.mod.entity.AbstractTribalEntity.KoboldProfession;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.List;

/**
 * Meta: Defender a la tribu contra ataques de súbditas rebeldes
 */
public class DefendTribeFromRebelsGoal extends Goal {
    private final KoboldGirlEntity mob;
    private int unseenTicks = 0;

    public DefendTribeFromRebelsGoal(KoboldGirlEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (this.mob.isRebel() || !this.mob.isAlive() || this.mob.isInQueenDuel() || this.mob.isInPowerStruggle()) return false;
        // ¡Las súbditas leales al jugador NO atacan a las rebeldes! (La reina se encarga con un golpe disciplinario)
        if (this.mob.isOwned() || this.mob.getOwnerUUID() != null) return false;
        if (this.mob.getProfession() == KoboldProfession.FARMER || this.mob.getProfession() == KoboldProfession.HUNTER) return false;
        LivingEntity target = this.mob.getTarget();
        if (target != null && target.isAlive() && this.mob.hasLineOfSight(target)) return false;

        // NO detectar a través de muros: Solo rebeldes con línea de visión directa
        List<KoboldGirlEntity> rebels = this.mob.level().getEntitiesOfClass(
                KoboldGirlEntity.class,
                this.mob.getBoundingBox().inflate(16.0D, 8.0D, 16.0D),
                k -> k != this.mob && k.isAlive() && k.isRebel()
                        && this.mob.hasLineOfSight(k)
        );
        if (rebels.isEmpty()) return false;

        this.mob.setTarget(rebels.get(0));
        this.mob.equipForCombat(true);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.mob.isRebel() || !this.mob.isAlive() || this.mob.isInQueenDuel() || this.mob.isInPowerStruggle()) return false;
        if (this.mob.isOwned() || this.mob.getOwnerUUID() != null) return false;
        if (this.mob.getProfession() == KoboldProfession.FARMER || this.mob.getProfession() == KoboldProfession.HUNTER) return false;
        LivingEntity target = this.mob.getTarget();
        if (target == null || !target.isAlive() || !(target instanceof KoboldGirlEntity rebel)
                || !rebel.isRebel()) {
            return false;
        }
        if (this.mob.distanceToSqr(rebel) > 24.0D * 24.0D) {
            return false;
        }
        if (this.unseenTicks > 80 && this.mob.distanceToSqr(rebel) > 16.0D) {
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
