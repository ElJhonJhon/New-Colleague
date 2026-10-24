package com.newcolleague.mod.entity.ai;

import com.newcolleague.mod.entity.KoboldGirlEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

/**
 * Meta: Duelo de dominancia de la Reina contra el Jugador
 */
public class QueenPlayerDuelTargetGoal extends Goal {
    private final KoboldGirlEntity mob;

    public QueenPlayerDuelTargetGoal(KoboldGirlEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (!this.mob.isAnyQueen() || !this.mob.isInPlayerDuel() || !this.mob.isAlive()) {
            return false;
        }
        Player duelPlayer = this.mob.getPlayerDuelTarget();
        return duelPlayer != null && duelPlayer.isAlive() && !duelPlayer.isSpectator();
    }

    @Override
    public void start() {
        Player duelPlayer = this.mob.getPlayerDuelTarget();
        if (duelPlayer != null && duelPlayer.isAlive()) {
            this.mob.setTarget(duelPlayer);
            this.mob.equipForCombat(true);
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.mob.isAnyQueen() || !this.mob.isInPlayerDuel() || !this.mob.isAlive()) {
            return false;
        }
        Player duelPlayer = this.mob.getPlayerDuelTarget();
        return duelPlayer != null && duelPlayer.isAlive() && !duelPlayer.isSpectator();
    }

    @Override
    public void tick() {
        Player duelPlayer = this.mob.getPlayerDuelTarget();
        if (duelPlayer != null && duelPlayer.isAlive()) {
            if (this.mob.getTarget() != duelPlayer) {
                this.mob.setTarget(duelPlayer);
            }
            if (!KoboldGirlEntity.isWeapon(this.mob.getMainHandItem())) {
                this.mob.equipForCombat(true);
            }
        }
    }
}
