package com.newcolleague.mod.entity.ai;

import com.newcolleague.mod.entity.KoboldGirlEntity;
import com.newcolleague.mod.entity.AbstractTribalEntity.CommandStance;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;

import java.util.EnumSet;
import java.util.List;

/**
 * Ganadería autónoma: alimentar y reproducir especies con baja población (<= 2)
 */
public class KoboldHerdingGoal extends Goal {
    private final KoboldGirlEntity kobold;
    private final double speedModifier;
    private Animal targetAnimal;
    private int feedCooldown = 0;

    public KoboldHerdingGoal(KoboldGirlEntity kobold, double speedModifier) {
        this.kobold = kobold;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.kobold.isSleeping() || this.kobold.isInPowerStruggle()) return false;
        if (this.kobold.getTarget() != null) return false;
        if (this.feedCooldown > 0) {
            this.feedCooldown--;
            return false;
        }
        if (this.kobold.isOwned() && this.kobold.getCommandStance() != CommandStance.WORK) {
            return false;
        }
        if (!this.kobold.isMorningWorkTime()) {
            return false;
        }

        List<Animal> nearbyAnimals = this.kobold.level().getEntitiesOfClass(
                Animal.class,
                this.kobold.getBoundingBox().inflate(14.0D),
                a -> a.isAlive() && !a.isBaby() && !a.isInLove() && a.getAge() == 0 && KoboldGirlEntity.canBreedAnimal(this.kobold, a)
        );
        if (nearbyAnimals.isEmpty()) return false;

        for (Animal a : nearbyAnimals) {
            List<? extends Animal> localSpecies = this.kobold.level().getEntitiesOfClass(
                    a.getClass(),
                    a.getBoundingBox().inflate(16.0D),
                    la -> la.isAlive() && !la.isBaby()
            );
            if (localSpecies.size() <= 2) {
                this.targetAnimal = a;
                return true;
            }
        }
        return false;
    }

    @Override
    public void start() {
        if (this.targetAnimal != null) {
            this.kobold.getNavigation().moveTo(this.targetAnimal, this.speedModifier);
        }
    }

    @Override
    public boolean canContinueToUse() {
        return this.targetAnimal != null && this.targetAnimal.isAlive() && !this.targetAnimal.isInLove() && this.kobold.distanceToSqr(this.targetAnimal) < 196.0D;
    }

    @Override
    public void tick() {
        if (this.targetAnimal == null) return;
        this.kobold.getLookControl().setLookAt(this.targetAnimal, 30.0F, 30.0F);
        double distSqr = this.kobold.distanceToSqr(this.targetAnimal);
        if (distSqr <= 5.0D) {
            KoboldGirlEntity.tryFeedAnimal(this.kobold, this.targetAnimal);
            this.feedCooldown = 80;
            this.targetAnimal = null;
            this.kobold.getNavigation().stop();
        } else if (this.kobold.getNavigation().isDone()) {
            this.kobold.getNavigation().moveTo(this.targetAnimal, this.speedModifier);
        }
    }

    @Override
    public void stop() {
        this.targetAnimal = null;
    }
}
