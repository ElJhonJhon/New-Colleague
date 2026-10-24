package com.newcolleague.mod.entity.ai;

import com.newcolleague.mod.entity.KoboldGirlEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Meta: Tomar inmediatamente sus piezas de armadura del soporte de armadura al despertar
 */
public class KoboldRetrieveArmorGoal extends Goal {
    private final KoboldGirlEntity mob;
    private int timeout = 0;

    public KoboldRetrieveArmorGoal(KoboldGirlEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.mob.isSleeping()) {
            return false;
        }
        if (!this.mob.isNeedsArmorRetrieval() && !(!this.mob.isSleepTime() && this.mob.doesAssignedArmorStandHaveArmor())) {
            return false;
        }
        ArmorStand stand = this.mob.getAssignedArmorStandEntity();
        if (stand == null || !stand.isAlive()) {
            this.mob.setNeedsArmorRetrieval(false);
            return false;
        }
        return this.mob.doesAssignedArmorStandHaveArmor();
    }

    @Override
    public boolean canContinueToUse() {
        if (this.mob.isSleeping()) {
            return false;
        }
        if (this.timeout > 120) {
            return false;
        }
        ArmorStand stand = this.mob.getAssignedArmorStandEntity();
        if (stand == null || !stand.isAlive()) {
            this.mob.setNeedsArmorRetrieval(false);
            return false;
        }
        return this.mob.doesAssignedArmorStandHaveArmor();
    }

    @Override
    public void start() {
        this.timeout = 0;
        ArmorStand stand = this.mob.getAssignedArmorStandEntity();
        if (stand != null) {
            this.mob.getNavigation().moveTo(stand, 1.3D);
        }
    }

    @Override
    public void stop() {
        this.timeout = 0;
        this.mob.setNeedsArmorRetrieval(false);
        this.mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        ArmorStand stand = this.mob.getAssignedArmorStandEntity();
        if (stand == null || !stand.isAlive()) {
            this.mob.setNeedsArmorRetrieval(false);
            return;
        }

        this.timeout++;
        this.mob.getLookControl().setLookAt(stand, 30.0F, 30.0F);

        double distSqr = this.mob.distanceToSqr(stand);
        if (distSqr > 4.0D) {
            if (this.mob.getNavigation().isDone() || this.mob.tickCount % 10 == 0) {
                this.mob.getNavigation().moveTo(stand, 1.3D);
            }
        } else {
            // Llegó al soporte de armadura: retirar y equipar cada pieza
            this.mob.getNavigation().stop();
            this.mob.setDeltaMovement(Vec3.ZERO);
            boolean retrievedAny = false;
            for (EquipmentSlot slot : KoboldGirlEntity.ARMOR_SLOTS) {
                ItemStack standArmor = stand.getItemBySlot(slot);
                if (!standArmor.isEmpty()) {
                    stand.setItemSlot(slot, ItemStack.EMPTY);
                    ItemStack current = this.mob.getItemBySlot(slot);
                    if (!current.isEmpty()) {
                        ItemStack leftover = this.mob.getInventory().addItem(current);
                        if (!leftover.isEmpty() && this.mob.level() instanceof ServerLevel serverLevel) {
                            this.mob.spawnAtLocation(serverLevel, leftover);
                        }
                    }
                    this.mob.setItemSlot(slot, standArmor);
                    retrievedAny = true;
                }
            }

            if (retrievedAny) {
                this.mob.playSound(SoundEvents.ARMOR_EQUIP_GENERIC.value(), 1.0F, 1.0F);
                if (this.mob.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                            stand.getX(), stand.getY() + 1.2D, stand.getZ(),
                            6, 0.25D, 0.25D, 0.25D, 0.05D);
                }
            }

            this.mob.setNeedsArmorRetrieval(false);
        }
    }
}
