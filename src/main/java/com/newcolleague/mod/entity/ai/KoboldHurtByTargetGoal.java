package com.newcolleague.mod.entity.ai;

import com.newcolleague.mod.entity.KoboldGirlEntity;
import com.newcolleague.mod.entity.AbstractTribalEntity.KoboldProfession;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;

/**
 * Meta: Represalia al ser dañada y alertar a aliadas cercanas SIN atravesar muros
 */
public class KoboldHurtByTargetGoal extends HurtByTargetGoal {
    private final KoboldGirlEntity kobold;

    public KoboldHurtByTargetGoal(KoboldGirlEntity mob) {
        super(mob, KoboldGirlEntity.class);
        this.kobold = mob;
    }

    @Override
    public boolean canUse() {
        LivingEntity attacker = this.kobold.getLastHurtByMob();
        if (attacker instanceof KoboldGirlEntity k) {
            // Reina o súbditas de la tribu del jugador nunca toman represalia contra rebeldes de su propia tribu
            if (this.kobold.isOwned() && k.isRebel() && (this.kobold.getTribeName().equals(k.getTribeName()) || this.kobold.distanceToSqr(k) <= 64.0D * 64.0D)) {
                return false;
            }
            // Rebeldes de la tribu del jugador nunca toman represalia contra la reina leal de su tribu
            if (this.kobold.isRebel() && k.isAnyQueen() && k.isOwned()) {
                return false;
            }
        }
        return super.canUse();
    }

    @Override
    protected void alertOther(Mob other, LivingEntity hurtByMob) {
        if (other instanceof KoboldGirlEntity koboldOther) {
            // Súbditas y reinas leales no toman represalia ni se alertan contra rebeldes de su tribu
            if (hurtByMob instanceof KoboldGirlEntity k && k.isRebel() && koboldOther.isOwned()
                    && (koboldOther.getTribeName().equals(k.getTribeName()) || koboldOther.distanceToSqr(k) <= 64.0D * 64.0D)) {
                return;
            }
            // Rebeldes no se alertan contra la reina leal de su tribu
            if (hurtByMob instanceof KoboldGirlEntity k && k.isAnyQueen() && k.isOwned() && koboldOther.isRebel()) {
                return;
            }
            // Si la víctima o la reina está en un duelo formal (de reinas o con el jugador), NO alertar a las demás
            if (this.kobold.isInQueenDuel() || this.kobold.isInPlayerDuel()
                    || (hurtByMob instanceof Player p && this.kobold.isInPlayerDuelWith(p))) {
                return;
            }
            // No alertar si la compañera está durmiendo, en duelo, espectando un duelo o en guerra por el poder
            if (koboldOther.isSleeping() || koboldOther.isInQueenDuel() || koboldOther.isInPlayerDuel()
                    || koboldOther.isSpectatingDuel() || koboldOther.isInPowerStruggle()) {
                return;
            }
            // Granjeras, Cazadoras y Constructoras son trabajadoras civiles pacíficas y no se alertan para pelear
            if (koboldOther.getProfession() == KoboldProfession.FARMER 
                    || koboldOther.getProfession() == KoboldProfession.HUNTER
                    || koboldOther.getProfession() == KoboldProfession.BUILDER) {
                return;
            }
            // Solo alertar a compañeras de la misma tribu
            if (!this.kobold.getTribeName().isEmpty() && !koboldOther.getTribeName().isEmpty()
                    && !this.kobold.getTribeName().equals(koboldOther.getTribeName())) {
                return;
            }
            // ¡NO DETECTAR A TRAVÉS DE MUROS! Debe tener línea de visión directa hacia la víctima o hacia el agresor
            if (!koboldOther.hasLineOfSight(this.kobold) && !koboldOther.hasLineOfSight(hurtByMob)) {
                return;
            }
        }
        super.alertOther(other, hurtByMob);
    }
}
