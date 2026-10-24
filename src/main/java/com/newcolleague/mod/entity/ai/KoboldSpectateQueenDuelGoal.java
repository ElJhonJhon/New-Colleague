package com.newcolleague.mod.entity.ai;

import com.newcolleague.mod.entity.KoboldGirlEntity;
import com.newcolleague.mod.entity.AbstractTribalEntity.QueenDuel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

/**
 * Meta: Formar un círculo y espectar el duelo a muerte entre Reinas
 */
public class KoboldSpectateQueenDuelGoal extends Goal {
    private final KoboldGirlEntity mob;
    private QueenDuel currentDuel;
    private int repathTimer = 0;

    public KoboldSpectateQueenDuelGoal(KoboldGirlEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.mob.isAnyQueen() || this.mob.isRebel() || this.mob.isInPowerStruggle() || !this.mob.isAlive()) return false;
        this.currentDuel = KoboldGirlEntity.getNearbyQueenDuel(this.mob.level(), this.mob.position(), 24.0D);
        if (this.currentDuel == null || !this.currentDuel.isActive()) {
            return false;
        }
        // NO detectar a través de muros: Solo espectar si tiene línea de visión directa hacia alguna de las dos reinas
        return this.mob.hasLineOfSight(this.currentDuel.queen1) || this.mob.hasLineOfSight(this.currentDuel.queen2);
    }

    @Override
    public boolean canContinueToUse() {
        if (this.mob.isAnyQueen() || this.mob.isRebel() || this.mob.isInPowerStruggle() || !this.mob.isAlive()) return false;
        if (this.currentDuel == null || !this.currentDuel.isActive()) return false;
        if (this.mob.distanceToSqr(this.currentDuel.getCenter()) > 36.0D * 36.0D) return false;
        // Si ambas reinas quedan fuera de línea de visión tras muros, dejar de espectar
        return this.mob.hasLineOfSight(this.currentDuel.queen1) || this.mob.hasLineOfSight(this.currentDuel.queen2);
    }

    @Override
    public void start() {
        this.mob.setSpectatingDuel(true);
        if (this.mob.isSleeping()) {
            this.mob.stopSleeping();
        }
        if (this.mob.getTarget() != null) {
            this.mob.setTarget(null);
        }
        if (this.mob.isUsingItem()) {
            this.mob.stopUsingItem();
        }
        this.mob.unequipHands();
        this.repathTimer = 0;
    }

    @Override
    public void stop() {
        this.mob.setSpectatingDuel(false);
        this.currentDuel = null;
        if (this.mob.getTarget() != null && !this.mob.isDefender()) {
            this.mob.setTarget(null);
        }
        this.mob.setLastHurtByMob(null);
        this.mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.currentDuel == null || !this.currentDuel.isActive()) {
            this.mob.setSpectatingDuel(false);
            if (this.mob.getTarget() != null && !this.mob.isDefender()) {
                this.mob.setTarget(null);
            }
            this.mob.setLastHurtByMob(null);
            return;
        }
        this.mob.setSpectatingDuel(true);

        if (!this.mob.getMainHandItem().isEmpty() || !this.mob.getOffhandItem().isEmpty()) {
            this.mob.unequipHands();
        }
        if (this.mob.getTarget() != null) {
            this.mob.setTarget(null);
        }

        // Partículas místicas periódicas sobre las espectadoras canalizando la arena del duelo
        if (this.mob.tickCount % 20 == 0 && this.mob.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.ENCHANT, this.mob.getX(), this.mob.getY() + 1.2D, this.mob.getZ(), 2, 0.2D, 0.2D, 0.2D, 0.05D);
        }

        Vec3 center = this.currentDuel.getCenter();
        this.mob.getLookControl().setLookAt(center.x, center.y + 1.0D, center.z, 30.0F, 30.0F);

        this.repathTimer--;
        if (this.repathTimer <= 0) {
            this.repathTimer = 10 + this.mob.getRandom().nextInt(10);

            // Solo incluir en el círculo a las súbditas que realmente tienen línea de visión hacia el combate
            List<KoboldGirlEntity> spectators = this.mob.level().getEntitiesOfClass(
                    KoboldGirlEntity.class,
                    new AABB(center.x - 24.0D, center.y - 12.0D, center.z - 24.0D, center.x + 24.0D, center.y + 12.0D, center.z + 24.0D),
                    k -> !k.isAnyQueen() && !k.isRebel() && k.isAlive()
                            && (k.hasLineOfSight(this.currentDuel.queen1) || k.hasLineOfSight(this.currentDuel.queen2))
            );

            spectators.sort(Comparator.comparing(Entity::getUUID));
            int index = spectators.indexOf(this.mob);
            if (index < 0) index = 0;
            int total = Math.max(1, spectators.size());

            double angle = (2.0D * Math.PI * index) / total;
            double radius = KoboldGirlEntity.getDuelSpectatorRadius(this.mob.level(), center, this.currentDuel.queen1, this.currentDuel.queen2);

            double targetX = center.x + Math.cos(angle) * radius;
            double targetZ = center.z + Math.sin(angle) * radius;
            double targetY = center.y;

            BlockPos basePos = BlockPos.containing(targetX, targetY, targetZ);
            BlockPos standPos = KoboldGirlEntity.findStandablePosNear(this.mob.level(), basePos);
            if (standPos != null) {
                targetX = standPos.getX() + 0.5D;
                targetY = standPos.getY();
                targetZ = standPos.getZ() + 0.5D;
            }

            double distSq = this.mob.distanceToSqr(targetX, targetY, targetZ);
            if (distSq > 2.0D) {
                this.mob.getNavigation().moveTo(targetX, targetY, targetZ, 1.2D);
            } else {
                this.mob.getNavigation().stop();
            }
        }
    }
}
