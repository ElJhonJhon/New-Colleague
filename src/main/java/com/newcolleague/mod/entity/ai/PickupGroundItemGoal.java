package com.newcolleague.mod.entity.ai;

import com.newcolleague.mod.entity.KoboldGirlEntity;
import com.newcolleague.mod.entity.AbstractTribalEntity.KoboldProfession;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.EnumSet;
import java.util.List;

/**
 * Meta: Caminar hacia ítems en el suelo y recogerlos
 */
public class PickupGroundItemGoal extends Goal {
    private final KoboldGirlEntity mob;
    private ItemEntity targetItem;

    public PickupGroundItemGoal(KoboldGirlEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.mob.isSleeping()) {
            return false;
        }
        List<ItemEntity> items = this.mob.level().getEntitiesOfClass(
                ItemEntity.class,
                this.mob.getBoundingBox().inflate(12.0D, 4.0D, 12.0D),
                item -> !item.hasPickUpDelay() && item.isAlive()
                        && this.mob.hasLineOfSight(item)
                        && this.mob.wantsToPickUpItem(item.getItem())
                        && this.mob.getInventory().canAddItem(item.getItem())
        );
        if (items.isEmpty()) return false;

        this.targetItem = this.getBestTargetItem(items);
        return this.targetItem != null;
    }

    private ItemEntity getBestTargetItem(List<ItemEntity> items) {
        ItemEntity bestItem = null;
        double bestScore = -Double.MAX_VALUE;

        for (ItemEntity item : items) {
            ItemStack stack = item.getItem();
            double priority = 10.0D;

            // Prioridad máxima: Comida si tiene hambre
            if (this.mob.needsFood() && KoboldGirlEntity.isReadyToEatFood(stack)) {
                priority = 100.0D;
            } else if (this.mob.isArmorUpgrade(stack)) {
                priority = 90.0D;
            } else if (this.mob.isDefender() && KoboldGirlEntity.isEnemyLoot(stack)) {
                priority = 85.0D;
            } else if (this.mob.getProfession() == KoboldProfession.FARMER && (KoboldGirlEntity.isHarvestCropItem(stack) || KoboldGirlEntity.isSeedItem(stack))) {
                priority = 80.0D;
            } else if (this.mob.getProfession() == KoboldProfession.HUNTER && (KoboldGirlEntity.isRawMeat(stack) || KoboldGirlEntity.isCookedMeat(stack) || KoboldGirlEntity.isAnimalProduct(stack))) {
                priority = 80.0D;
            } else if (this.mob.getProfession() == KoboldProfession.VIGILANT && KoboldGirlEntity.isArrowItem(stack)) {
                priority = 80.0D;
            } else if (this.mob.getProfession() == KoboldProfession.GUARD && (KoboldGirlEntity.isShield(stack) || KoboldGirlEntity.isWeapon(stack))) {
                priority = 80.0D;
            }

            // Descuento por distancia (más cercano = mayor score)
            double distSq = this.mob.distanceToSqr(item);
            double score = priority - (distSq * 0.1D);
            if (score > bestScore) {
                bestScore = score;
                bestItem = item;
            }
        }
        return bestItem;
    }

    @Override
    public boolean canContinueToUse() {
        return !this.mob.isSleeping() && this.targetItem != null && this.targetItem.isAlive() && !this.targetItem.hasPickUpDelay()
                && this.mob.hasLineOfSight(this.targetItem)
                && this.mob.wantsToPickUpItem(this.targetItem.getItem())
                && this.mob.getInventory().canAddItem(this.targetItem.getItem())
                && this.mob.distanceToSqr(this.targetItem) < 144.0D;
    }

    @Override
    public void tick() {
        if (this.targetItem != null) {
            this.mob.getNavigation().moveTo(this.targetItem, 1.25D);
            if (this.mob.distanceToSqr(this.targetItem) < 2.5D) {
                if (this.mob.level() instanceof ServerLevel serverLevel) {
                    this.mob.pickUpItem(serverLevel, this.targetItem);
                }
            }
        }
    }
}
