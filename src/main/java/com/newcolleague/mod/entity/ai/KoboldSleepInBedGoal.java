package com.newcolleague.mod.entity.ai;

import com.newcolleague.mod.entity.KoboldGirlEntity;
import com.newcolleague.mod.entity.AbstractTribalEntity.CommandStance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Meta: Dormir en cama asignada durante la noche
 */
public class KoboldSleepInBedGoal extends Goal {
    private final KoboldGirlEntity mob;
    private int waitTicks = 0;

    public KoboldSleepInBedGoal(KoboldGirlEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (this.mob.getSleepCooldown() > 0 || this.mob.isInPowerStruggle()) {
            return false;
        }
        if (this.mob.isOwned() && this.mob.getCommandStance() == CommandStance.FOLLOW) {
            return false;
        }
        if (this.mob.isInQueenDuel() || KoboldGirlEntity.getNearbyQueenDuel(this.mob.level(), this.mob.position(), 24.0D) != null) {
            return false;
        }
        if (!this.mob.isSleepTime()) {
            return false;
        }
        if (this.mob.getTarget() != null) {
            return false;
        }
        if (!this.mob.hasAssignedBed()) {
            BlockPos bed = this.mob.findNearestUnclaimedBed(32);
            if (bed != null) {
                this.mob.setAssignedBed(bed);
            } else {
                return false;
            }
        }
        return this.mob.isAssignedBedValid();
    }

    @Override
    public boolean canContinueToUse() {
        if (this.mob.getSleepCooldown() > 0 || this.mob.isInPowerStruggle()) {
            return false;
        }
        if (this.mob.isOwned() && this.mob.getCommandStance() == CommandStance.FOLLOW) {
            return false;
        }
        if (this.mob.isInQueenDuel() || KoboldGirlEntity.getNearbyQueenDuel(this.mob.level(), this.mob.position(), 24.0D) != null) {
            return false;
        }
        if (!this.mob.isSleepTime()) {
            return false;
        }
        if (this.mob.getTarget() != null) {
            return false;
        }
        if (!this.mob.hasAssignedBed() || !this.mob.isAssignedBedValid()) {
            return false;
        }
        return true;
    }

    @Override
    public void start() {
        this.waitTicks = 0;
        BlockPos bed = this.mob.getAssignedBedPos();
        if (bed != null) {
            Vec3 center = getBedCenter(bed);
            this.mob.getNavigation().moveTo(center.x, bed.getY(), center.z, 1.15D);
        }
    }

    private Vec3 getBedCenter(BlockPos headPos) {
        BlockState state = this.mob.level().getBlockState(headPos);
        Direction facing = (state.getBlock() instanceof BedBlock) ? state.getValue(BedBlock.FACING) : Direction.NORTH;
        BlockPos footPos = headPos.relative(facing.getOpposite());
        double centerX = (Math.min(headPos.getX(), footPos.getX()) + Math.max(headPos.getX(), footPos.getX()) + 1.0D) / 2.0D;
        double centerZ = (Math.min(headPos.getZ(), footPos.getZ()) + Math.max(headPos.getZ(), footPos.getZ()) + 1.0D) / 2.0D;
        return new Vec3(centerX, headPos.getY(), centerZ);
    }

    public boolean isBesideBed(BlockPos bed) {
        if (this.mob.isSleeping()) {
            return false;
        }

        BlockState state = this.mob.level().getBlockState(bed);
        if (!(state.getBlock() instanceof BedBlock)) {
            return false;
        }
        Direction facing = state.getValue(BedBlock.FACING);
        BlockPos footPos = bed.relative(facing.getOpposite());

        double minX = Math.min(bed.getX(), footPos.getX());
        double maxX = Math.max(bed.getX(), footPos.getX()) + 1.0D;
        double minZ = Math.min(bed.getZ(), footPos.getZ());
        double maxZ = Math.max(bed.getZ(), footPos.getZ()) + 1.0D;

        double clampedX = Math.clamp(this.mob.getX(), minX, maxX);
        double clampedZ = Math.clamp(this.mob.getZ(), minZ, maxZ);
        double dx = this.mob.getX() - clampedX;
        double dz = this.mob.getZ() - clampedZ;
        double horizDistSq = dx * dx + dz * dz;

        // Histéresis: si ya estaba esperando junto a la cama, tolerar pequeños empujes de otras entidades (hasta 1.6 bloques)
        // Si apenas va llegando, requiere estar en el perímetro inmediato adyacente (a 1.25 bloques o menos del borde)
        double maxHorizDistSq = (this.waitTicks > 0) ? 2.56D : 1.5625D;
        if (horizDistSq > maxHorizDistSq) {
            return false;
        }

        // Misma altura aproximada (diferencia vertical menor a 1.0 bloque)
        double vertDist = Math.abs(this.mob.getY() - bed.getY());
        if (vertDist > 1.0D) {
            return false;
        }

        return true;
    }

    @Override
    public void tick() {
        BlockPos bed = this.mob.getAssignedBedPos();
        if (bed == null || !this.mob.isAssignedBedValid()) {
            if (this.mob.isSleeping()) {
                this.mob.stopSleeping();
            }
            this.waitTicks = 0;
            return;
        }

        // Si ya está durmiendo en la cama, mantenerse inmóvil
        if (this.mob.isSleeping()) {
            this.mob.getNavigation().stop();
            this.waitTicks = 0;
            return;
        }

        // 1. Si lleva armadura equipada y tiene o encuentra un soporte de armadura asignado cerca de la cama,
        // primero depositar las piezas de armadura en el soporte antes de acostarse
        if (this.mob.hasAnyArmorEquipped()) {
            ArmorStand stand = this.mob.getAssignedArmorStandEntity();
            if (stand == null) {
                stand = this.mob.findNearestUnclaimedArmorStand(8.0D);
                if (stand != null) {
                    this.mob.setAssignedArmorStand(stand);
                }
            }

            if (stand != null) {
                double distToStandSqr = this.mob.distanceToSqr(stand);
                if (distToStandSqr > 4.0D) {
                    this.waitTicks = 0;
                    this.mob.getLookControl().setLookAt(stand, 30.0F, 30.0F);
                    if (this.mob.getNavigation().isDone() || this.mob.tickCount % 10 == 0) {
                        this.mob.getNavigation().moveTo(stand, 1.15D);
                    }
                    return;
                } else {
                    // Junto al soporte de armadura: depositar cada pieza
                    this.mob.getNavigation().stop();
                    this.mob.setDeltaMovement(Vec3.ZERO);
                    this.mob.getLookControl().setLookAt(stand, 30.0F, 30.0F);

                    boolean depositedAny = false;
                    for (EquipmentSlot slot : KoboldGirlEntity.ARMOR_SLOTS) {
                        ItemStack worn = this.mob.getItemBySlot(slot);
                        if (!worn.isEmpty()) {
                            ItemStack standCurrent = stand.getItemBySlot(slot);
                            if (standCurrent.isEmpty()) {
                                stand.setItemSlot(slot, worn.copy());
                                this.mob.setItemSlot(slot, ItemStack.EMPTY);
                                depositedAny = true;
                            } else {
                                ItemStack leftover = this.mob.getInventory().addItem(worn);
                                this.mob.setItemSlot(slot, leftover);
                            }
                        }
                    }

                    if (depositedAny) {
                        this.mob.playSound(SoundEvents.ARMOR_STAND_PLACE, 1.0F, 1.0F);
                        if (this.mob.level() instanceof ServerLevel serverLevel) {
                            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                                    stand.getX(), stand.getY() + 1.2D, stand.getZ(),
                                    6, 0.25D, 0.25D, 0.25D, 0.05D);
                        }
                    }
                    return;
                }
            }
        }

        Vec3 bedCenter = getBedCenter(bed);

        // Verificar si ya está al lado de la cama
        if (this.isBesideBed(bed)) {
            // Detener movimiento y mirar fijamente hacia el centro de la cama
            this.mob.getNavigation().stop();
            this.mob.setDeltaMovement(Vec3.ZERO);
            this.mob.getLookControl().setLookAt(bedCenter.x, bed.getY() + 0.6D, bedCenter.z, 30.0F, 30.0F);

            // Esperar exactamente 2 segundos (40 ticks) antes de acostarse
            this.waitTicks++;
            if (this.waitTicks >= 40) {
                this.mob.startSleeping(bed);
                this.waitTicks = 0;
            }
        } else {
            // Si todavía no está junto a la cama, reiniciar la espera y navegar hacia la cama
            this.waitTicks = 0;
            if (this.mob.getNavigation().isDone() || this.mob.tickCount % 10 == 0) {
                this.mob.getNavigation().moveTo(bedCenter.x, bed.getY(), bedCenter.z, 1.15D);
            }
        }
    }

    @Override
    public void stop() {
        this.waitTicks = 0;
        if (this.mob.isSleeping()) {
            this.mob.stopSleeping();
        }
        this.mob.getNavigation().stop();
    }
}
