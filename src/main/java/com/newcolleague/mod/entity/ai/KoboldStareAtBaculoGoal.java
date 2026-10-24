package com.newcolleague.mod.entity.ai;

import com.newcolleague.mod.entity.KoboldGirlEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

/**
 * Meta: Todas las Kobold Girls súbditas se quedan inmóviles mirando al jugador con el báculo
 */
public class KoboldStareAtBaculoGoal extends Goal {
    private final KoboldGirlEntity mob;
    private Player targetPlayer;
    private int stareTimer = 0;
    private boolean isFirstTimeForTribe = false;

    public KoboldStareAtBaculoGoal(KoboldGirlEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (!this.mob.isAlive() || this.mob.isAnyQueen() || this.mob.isSleeping() || this.mob.isRebel()
                || this.mob.isInQueenDuel() || this.mob.isInPlayerDuel()
                || KoboldGirlEntity.getNearbyQueenDuel(this.mob.level(), this.mob.position(), 24.0D) != null) {
            return false;
        }
        // Si la tribu ya presenció el báculo anteriormente y está en cooldown, no interrumpir sus actividades normales
        if (this.mob.hasTribeWitnessedBaculo() && this.mob.getBaculoStareCooldown() > 0) {
            return false;
        }
        this.targetPlayer = KoboldGirlEntity.findNearbyBaculoPlayer(this.mob, 20.0D);
        return this.targetPlayer != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.mob.isAlive() || this.mob.isAnyQueen() || this.mob.isSleeping() || this.mob.isRebel()
                || this.mob.isInQueenDuel() || this.mob.isInPlayerDuel()
                || KoboldGirlEntity.getNearbyQueenDuel(this.mob.level(), this.mob.position(), 24.0D) != null) {
            return false;
        }
        if (this.targetPlayer == null || !this.targetPlayer.isAlive() || this.targetPlayer.isSpectator()) {
            return false;
        }
        if (!this.mob.isReveringBaculoHolder(this.targetPlayer)) {
            return false;
        }
        if (this.mob.distanceToSqr(this.targetPlayer) > 24.0D * 24.0D || !this.mob.hasLineOfSight(this.targetPlayer)) {
            return false;
        }
        // Si después de la primera vez ya la tribu presenció el báculo:
        // Solo mirar al jugador por unos segundos (70 ticks = 3.5s) y regresar a actividades normales
        if (!this.isFirstTimeForTribe && this.stareTimer >= 70) {
            return false;
        }
        return true;
    }

    @Override
    public void start() {
        this.stareTimer = 0;
        this.isFirstTimeForTribe = !this.mob.hasTribeWitnessedBaculo();
        this.mob.setStaringAtBaculo(true);
        this.mob.getNavigation().stop();
        if (this.mob.getTarget() != null) {
            this.mob.setTarget(null);
        }
        if (this.mob.isUsingItem()) {
            this.mob.stopUsingItem();
        }
        this.mob.setHarvesting(false);
        if (this.isFirstTimeForTribe) {
            this.mob.unequipHands();
        }
    }

    @Override
    public void stop() {
        this.mob.setStaringAtBaculo(false);
        this.mob.getNavigation().stop();
        this.mob.markTribeWitnessedBaculo();
        this.mob.setBaculoStareCooldown(400); // 20 segundos de cooldown antes de volver a mirar fijamente
        this.targetPlayer = null;
        this.mob.autoEquipWeaponsAndShield();
    }

    @Override
    public void tick() {
        if (this.targetPlayer == null) return;
        this.stareTimer++;

        // Si pasa un tiempo considerable en la primera vez (15 segundos), registrar que la tribu ya lo presenció
        if (this.stareTimer >= 300) {
            this.mob.markTribeWitnessedBaculo();
        }

        // Permanecer completamente inmóvil durante la observación
        this.mob.getNavigation().stop();

        if (this.isFirstTimeForTribe && (!this.mob.getMainHandItem().isEmpty() || !this.mob.getOffhandItem().isEmpty())) {
            this.mob.unequipHands();
        }
        if (this.mob.getTarget() != null) {
            this.mob.setTarget(null);
        }

        // Mirar fijamente al jugador portador del báculo
        this.mob.getLookControl().setLookAt(this.targetPlayer.getX(), this.targetPlayer.getEyeY(), this.targetPlayer.getZ(), 30.0F, 30.0F);
        this.mob.setYBodyRot(this.mob.getYHeadRot());
    }
}
