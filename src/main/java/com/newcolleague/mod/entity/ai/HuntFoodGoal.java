package com.newcolleague.mod.entity.ai;

import com.newcolleague.mod.entity.KoboldGirlEntity;
import com.newcolleague.mod.entity.AbstractTribalEntity.CommandStance;
import com.newcolleague.mod.entity.AbstractTribalEntity.KoboldProfession;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;

import java.util.List;

/**
 * Meta: Cazar animales exclusivamente (Cazadoras con hachas o arcos)
 */
public class HuntFoodGoal extends NearestAttackableTargetGoal<Animal> {
    private final KoboldGirlEntity kobold;

    public HuntFoodGoal(KoboldGirlEntity mob) {
        super(mob, Animal.class, 0, false, false, (target, level) -> {
            if (!(target instanceof Animal animal)) return false;
            // Proteger crías siempre
            if (animal.isBaby()) return false;
            if (mob.distanceToSqr(target) > 256.0D) return false;
            if (!mob.hasLineOfSight(target)) return false;

            // Proteger el último ejemplar adulto solitario salvo si la cazadora necesita comida con urgencia
            if (!mob.needsFood() && mob.countRawMeatInInventory() >= 4) {
                List<? extends Animal> localSpecies = level.getEntitiesOfClass(
                        animal.getClass(),
                        animal.getBoundingBox().inflate(16.0D),
                        a -> a.isAlive() && !a.isBaby()
                );
                if (localSpecies.size() <= 1) {
                    return false;
                }
            }

            return KoboldGirlEntity.isFoodAnimal(target);
        });
        this.kobold = mob;
    }

    @Override
    public boolean canUse() {
        if (this.kobold.isSleeping() || this.kobold.isInPowerStruggle()) {
            return false;
        }
        if (this.kobold.isOwned() && this.kobold.getCommandStance() != CommandStance.WORK) {
            return false;
        }
        if (this.kobold.getProfession() != KoboldProfession.HUNTER && !this.kobold.isAnyQueen()) {
            return false;
        }
        // Cazar durante la jornada diurna de trabajo mientras no sea hora de dormir
        if (!this.kobold.isWorkScheduleTime() || this.kobold.isSleepTime()) {
            return false;
        }
        if (this.kobold.isInventoryFullOfMeat()) {
            return false;
        }
        // Si la reina ya tiene suficiente carne recolectada (>= 16), cede el paso a las cazadoras
        if (this.kobold.isAnyQueen() && this.kobold.countRawMeatInInventory() >= 16) {
            return false;
        }
        return super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        if (this.kobold.isSleeping() || this.kobold.isInPowerStruggle()) {
            return false;
        }
        if (this.kobold.isOwned() && this.kobold.getCommandStance() != CommandStance.WORK) {
            return false;
        }
        if (this.kobold.getProfession() != KoboldProfession.HUNTER && !this.kobold.isAnyQueen()) {
            return false;
        }
        if (!this.kobold.isWorkScheduleTime() || this.kobold.isSleepTime()) {
            return false;
        }
        if (this.kobold.isInventoryFullOfMeat()) {
            return false;
        }
        if (this.kobold.isAnyQueen() && this.kobold.countRawMeatInInventory() >= 16) {
            return false;
        }
        LivingEntity target = this.mob.getTarget();
        if (target != null && !this.mob.hasLineOfSight(target)) {
            return false;
        }
        return super.canContinueToUse();
    }

    @Override
    public void start() {
        super.start();
        this.kobold.equipForCombat(false); // Caza de animales: solo arma, sin escudo
    }

    @Override
    public void stop() {
        super.stop();
        this.kobold.unequipHands(); // Desequipar manos al terminar la caza
    }
}
