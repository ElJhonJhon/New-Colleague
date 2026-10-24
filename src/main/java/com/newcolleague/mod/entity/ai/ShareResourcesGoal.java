package com.newcolleague.mod.entity.ai;

import com.newcolleague.mod.entity.KoboldGirlEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;

import java.util.EnumSet;
import java.util.List;

/**
 * Meta: Compartir recursos (comida y munición) según la necesidad entre Kobold Girls
 */
public class ShareResourcesGoal extends Goal {
    private final KoboldGirlEntity mob;
    private KoboldGirlEntity targetKobold;
    private int cooldown = 0;
    private int shareTimeout = 0;

    public ShareResourcesGoal(KoboldGirlEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.mob.isSleeping()) {
            return false;
        }
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }

        boolean hasFood = this.mob.hasFoodInInventory();
        boolean hasArrows = this.mob.hasPhysicalArrows();

        if (!hasFood && !hasArrows) {
            return false;
        }

        // Buscar otra Kobold Girl cercana con línea de visión directa que no esté durmiendo
        List<KoboldGirlEntity> nearbyKobolds = this.mob.level().getEntitiesOfClass(
                KoboldGirlEntity.class,
                this.mob.getBoundingBox().inflate(12.0D, 4.0D, 12.0D),
                k -> k != this.mob && k.isAlive() && !k.isSleeping() && this.mob.hasLineOfSight(k) && this.needsAssistance(k, hasFood, hasArrows)
        );

        if (nearbyKobolds.isEmpty()) {
            return false;
        }

        this.targetKobold = nearbyKobolds.get(0);
        return true;
    }

    private boolean needsAssistance(KoboldGirlEntity k, boolean iHaveFood, boolean iHaveArrows) {
        // No asistir ni compartir recursos con una Kobold Girl que esté durmiendo en la cama o durante duelos/guerra por el poder
        if (k.isSleeping() || this.mob.isInQueenDuel() || k.isInQueenDuel() || this.mob.isInPowerStruggle() || k.isInPowerStruggle()) {
            return false;
        }
        // Solo compartir recursos con compañeras de la misma tribu y que no sean rebeldes enemigas
        if (k.isRebel() != this.mob.isRebel() || (!k.getTribeName().isEmpty() && !this.mob.getTribeName().isEmpty() && !k.getTribeName().equals(this.mob.getTribeName()))) {
            return false;
        }
        // 1. Necesidad de comida: SOLO si la compañera tiene hambre por debajo de 18 Y no tiene nada de comida en su inventario
        if (iHaveFood && k.getFoodLevel() < 18 && !k.hasFoodInInventory()) {
            return true;
        }
        // 2. Necesidad de munición: si la compañera tiene un arma a distancia (en mano o inventario) pero NO tiene flechas
        if (iHaveArrows && k.hasRangedWeapon() && !k.hasArrows()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return !this.mob.isSleeping()
                && this.targetKobold != null
                && this.targetKobold.isAlive()
                && !this.targetKobold.isSleeping()
                && this.mob.hasLineOfSight(this.targetKobold)
                && this.mob.distanceToSqr(this.targetKobold) < 144.0D
                && this.needsAssistance(this.targetKobold, this.mob.hasFoodInInventory(), this.mob.hasPhysicalArrows());
    }

    @Override
    public void start() {
        this.shareTimeout = 0;
        if (this.targetKobold != null) {
            this.mob.getNavigation().moveTo(this.targetKobold, 1.25D);
        }
    }

    @Override
    public void tick() {
        if (this.targetKobold == null || this.targetKobold.isSleeping() || this.mob.isSleeping()) {
            this.targetKobold = null;
            this.cooldown = 40;
            this.mob.getNavigation().stop();
            return;
        }

        this.shareTimeout++;
        if (this.shareTimeout > 120) {
            this.targetKobold = null;
            this.cooldown = 60;
            this.mob.getNavigation().stop();
            return;
        }

        this.mob.getLookControl().setLookAt(this.targetKobold, 30.0F, 30.0F);
        this.mob.getNavigation().moveTo(this.targetKobold, 1.25D);

        if (this.mob.distanceToSqr(this.targetKobold) < 4.0D) {
            boolean shared = false;

            // 1. Compartir comida: SOLO si la compañera tiene hambre < 18 Y no tiene comida en su inventario
            if (this.mob.hasFoodInInventory() && this.targetKobold.getFoodLevel() < 18 && !this.targetKobold.hasFoodInInventory()) {
                for (int i = 0; i < this.mob.getInventory().getContainerSize(); i++) {
                    ItemStack stack = this.mob.getInventory().getItem(i);
                    if (stack.has(DataComponents.FOOD)) {
                        ItemStack foodToShare = this.mob.getInventory().removeItem(i, 1);
                        ItemStack remaining = this.targetKobold.getInventory().addItem(foodToShare);
                        if (!remaining.isEmpty()) {
                            this.mob.getInventory().addItem(remaining);
                        } else {
                            shared = true;
                            if (!this.targetKobold.isSleeping()) {
                                this.targetKobold.tryEatFromInventory();
                            }
                        }
                        break;
                    }
                }
            }

            // 2. Compartir flechas si la compañera tiene arma a distancia pero no tiene flechas, y esta Kobold tiene flechas físicas
            if (this.mob.hasPhysicalArrows() && this.targetKobold.hasRangedWeapon() && !this.targetKobold.hasArrows()) {
                for (int i = 0; i < this.mob.getInventory().getContainerSize(); i++) {
                    ItemStack stack = this.mob.getInventory().getItem(i);
                    if (stack.is(ItemTags.ARROWS)) {
                        int count = stack.getCount();
                        int toShare = count > 1 ? Math.min(count / 2, 16) : 1;
                        ItemStack arrowsToShare = this.mob.getInventory().removeItem(i, toShare);
                        ItemStack remaining = this.targetKobold.getInventory().addItem(arrowsToShare);
                        if (!remaining.isEmpty()) {
                            this.mob.getInventory().addItem(remaining);
                        } else {
                            shared = true;
                            if (this.targetKobold.isThreatened() || this.targetKobold.isHunting()) {
                                this.targetKobold.equipBestWeapon();
                            }
                        }
                        break;
                    }
                }
            }

            if (shared) {
                if (this.mob.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                            this.targetKobold.getX(), this.targetKobold.getY() + 1.2D, this.targetKobold.getZ(),
                            8, 0.25D, 0.25D, 0.25D, 0.05D);
                    serverLevel.sendParticles(ParticleTypes.HEART,
                            this.mob.getX(), this.mob.getY() + 1.2D, this.mob.getZ(),
                            3, 0.2D, 0.2D, 0.2D, 0.05D);
                    this.mob.playSound(SoundEvents.ITEM_PICKUP, 0.4F, 1.3F);
                }
            }

            this.cooldown = 100; // 5 segundos de espera entre intercambios
            this.targetKobold = null;
            this.mob.getNavigation().stop();
        }
    }

    @Override
    public void stop() {
        this.targetKobold = null;
    }
}
