package com.newcolleague.mod.entity.ai;

import com.newcolleague.mod.entity.KoboldGirlEntity;
import com.newcolleague.mod.entity.AbstractTribalEntity.ChestTask;
import com.newcolleague.mod.entity.AbstractTribalEntity.CommandStance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Meta: Usar cofres para categorizar, almacenar, reabastecerse y organizar recursos
 */
public class KoboldChestGoal extends Goal {
    private final KoboldGirlEntity kobold;
    private ChestTask currentTask = null;
    private int chestOpenTicks = 0;
    private int searchCooldown = 0;
    private int reachTimeout = 0;

    public KoboldChestGoal(KoboldGirlEntity mob) {
        this.kobold = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.kobold.isSleeping() || this.kobold.isThreatened() || this.kobold.isHunting()) {
            return false;
        }
        if (this.kobold.isOwned() && this.kobold.getCommandStance() != CommandStance.WORK) {
            return false;
        }
        if (this.searchCooldown > 0) {
            this.searchCooldown--;
            return false;
        }

        ChestTask task = this.kobold.findBestChestTask();
        if (task == null) {
            this.searchCooldown = 30; // 1.5s antes de volver a escanear
            return false;
        }

        this.currentTask = task;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.kobold.isSleeping() || this.kobold.isThreatened() || this.kobold.isHunting()) {
            return false;
        }
        if (this.kobold.isOwned() && this.kobold.getCommandStance() != CommandStance.WORK) {
            return false;
        }
        if (this.currentTask == null || this.currentTask.chestPos == null) {
            return false;
        }
        Container container = KoboldGirlEntity.getChestContainer(this.kobold.level(), this.currentTask.chestPos);
        return container != null;
    }

    private BlockPos getStandablePosNearChest(BlockPos chestPos) {
        Level level = this.kobold.level();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos adj = chestPos.relative(dir);
            BlockPos below = adj.below();
            BlockState belowState = level.getBlockState(below);
            BlockState state = level.getBlockState(adj);
            BlockState above = level.getBlockState(adj.above());

            boolean groundOk = belowState.isFaceSturdy(level, below, Direction.UP) || belowState.isSolid();
            boolean spaceOk = !state.blocksMotion() && !above.blocksMotion();
            boolean fluidOk = !state.getFluidState().is(FluidTags.LAVA) && !state.getFluidState().is(FluidTags.WATER);

            if (groundOk && spaceOk && fluidOk) {
                double d = this.kobold.distanceToSqr(Vec3.atCenterOf(adj));
                if (d < bestDist) {
                    bestDist = d;
                    best = adj;
                }
            }
        }
        if (best != null) return best;
        return chestPos;
    }

    @Override
    public void start() {
        this.chestOpenTicks = 0;
        this.reachTimeout = 200; // 10 segundos máximo para alcanzar el cofre
        if (this.currentTask != null) {
            this.kobold.unequipHands();
            BlockPos standPos = this.getStandablePosNearChest(this.currentTask.chestPos);
            this.kobold.getNavigation().moveTo(
                    standPos.getX() + 0.5D,
                    standPos.getY(),
                    standPos.getZ() + 0.5D,
                    1.2D
            );
        }
    }

    @Override
    public void stop() {
        if (this.chestOpenTicks > 0 && this.currentTask != null) {
            BlockState state = this.kobold.level().getBlockState(this.currentTask.chestPos);
            this.kobold.playSound(state.is(Blocks.BARREL) ? SoundEvents.BARREL_CLOSE : SoundEvents.CHEST_CLOSE, 0.5F, 1.0F);
        }
        this.currentTask = null;
        this.chestOpenTicks = 0;
        this.reachTimeout = 0;
        this.searchCooldown = 30;
    }

    @Override
    public void tick() {
        if (this.currentTask == null) return;

        this.kobold.getLookControl().setLookAt(
                this.currentTask.chestPos.getX() + 0.5D,
                this.currentTask.chestPos.getY() + 0.5D,
                this.currentTask.chestPos.getZ() + 0.5D,
                30.0F, 30.0F
        );

        double distSqr = this.kobold.distanceToSqr(Vec3.atCenterOf(this.currentTask.chestPos));

        if (distSqr > 9.0D) { // A más de 3 bloques
            this.reachTimeout--;
            if (this.reachTimeout <= 0) {
                this.searchCooldown = 60; // 3s cooldown tras atasco
                this.currentTask = null;
                return;
            }
            this.chestOpenTicks = 0;
            if (this.kobold.getNavigation().isDone() || this.kobold.tickCount % 15 == 0) {
                BlockPos standPos = this.getStandablePosNearChest(this.currentTask.chestPos);
                this.kobold.getNavigation().moveTo(
                        standPos.getX() + 0.5D,
                        standPos.getY(),
                        standPos.getZ() + 0.5D,
                        1.2D
                );
            }
        } else {
            // Llegó junto al cofre (a <= 3 bloques)
            this.kobold.getNavigation().stop();
            this.kobold.setDeltaMovement(Vec3.ZERO);

            if (this.chestOpenTicks == 0) {
                BlockState state = this.kobold.level().getBlockState(this.currentTask.chestPos);
                this.kobold.playSound(state.is(Blocks.BARREL) ? SoundEvents.BARREL_OPEN : SoundEvents.CHEST_OPEN, 0.5F, 1.0F);
                this.chestOpenTicks = 1;
            } else if (this.chestOpenTicks < 15) {
                this.chestOpenTicks++;
            } else {
                Container container = KoboldGirlEntity.getChestContainer(this.kobold.level(), this.currentTask.chestPos);
                if (container != null) {
                    this.kobold.performCategorizedChestOperations(container, this.currentTask);
                }

                BlockState state = this.kobold.level().getBlockState(this.currentTask.chestPos);
                this.kobold.playSound(state.is(Blocks.BARREL) ? SoundEvents.BARREL_CLOSE : SoundEvents.CHEST_CLOSE, 0.5F, 1.0F);

                if (this.kobold.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                            this.currentTask.chestPos.getX() + 0.5D,
                            this.currentTask.chestPos.getY() + 0.8D,
                            this.currentTask.chestPos.getZ() + 0.5D,
                            6, 0.25D, 0.25D, 0.25D, 0.05D
                    );
                }

                this.chestOpenTicks = 0;
                this.searchCooldown = 40;
                this.currentTask = null;
            }
        }
    }
}
