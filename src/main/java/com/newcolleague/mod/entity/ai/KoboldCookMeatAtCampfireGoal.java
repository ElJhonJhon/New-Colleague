package com.newcolleague.mod.entity.ai;

import com.newcolleague.mod.entity.KoboldGirlEntity;
import com.newcolleague.mod.entity.AbstractTribalEntity.CommandStance;
import com.newcolleague.mod.entity.AbstractTribalEntity.KoboldProfession;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Meta: Cazadoras cocinan carne cruda en fogatas cercanas
 */
public class KoboldCookMeatAtCampfireGoal extends MoveToBlockGoal {
    private final KoboldGirlEntity kobold;
    private int cookingTicks = 0;

    public KoboldCookMeatAtCampfireGoal(KoboldGirlEntity mob, double speed) {
        super(mob, speed, 24);
        this.kobold = mob;
    }

    @Override
    public boolean canUse() {
        if (this.kobold.isSleeping() || this.kobold.isThreatened() || this.kobold.isInPowerStruggle()) {
            return false;
        }
        if (this.kobold.isOwned() && this.kobold.getCommandStance() != CommandStance.WORK) {
            return false;
        }
        if (this.kobold.getProfession() != KoboldProfession.HUNTER) {
            return false;
        }
        if (!this.kobold.hasRawMeatInInventory()) {
            return false;
        }
        // Cocinar durante la hora de cocina (1:00 PM a 3:30 PM: ticks 7000 a 9500), o si ya llenó su capacidad de carne
        if (!this.kobold.isCookingAndCraftingTime() && !this.kobold.isInventoryFullOfMeat()) {
            return false;
        }
        return super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        if (this.kobold.isSleeping() || this.kobold.isThreatened() || this.kobold.isInPowerStruggle()) {
            return false;
        }
        if (this.kobold.isOwned() && this.kobold.getCommandStance() != CommandStance.WORK) {
            return false;
        }
        if (!this.kobold.hasRawMeatInInventory()) {
            return false;
        }
        return super.canContinueToUse();
    }

    @Override
    public void start() {
        super.start();
        this.cookingTicks = 0;
        this.kobold.unequipHands();
    }

    @Override
    public void stop() {
        super.stop();
        this.cookingTicks = 0;
        this.kobold.unequipHands();
    }

    @Override
    public double acceptedDistance() {
        return 2.5D;
    }

    @Override
    protected boolean isValidTarget(LevelReader level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof CampfireBlock) {
            return state.getValue(CampfireBlock.LIT);
        }
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isReachedTarget()) {
            this.kobold.getLookControl().setLookAt(this.blockPos.getX() + 0.5D, this.blockPos.getY() + 0.5D, this.blockPos.getZ() + 0.5D);
            this.cookingTicks++;

            if (this.kobold.level() instanceof ServerLevel serverLevel) {
                if (this.cookingTicks % 5 == 0) {
                    serverLevel.sendParticles(ParticleTypes.SMOKE,
                            this.blockPos.getX() + 0.5D, this.blockPos.getY() + 0.5D, this.blockPos.getZ() + 0.5D,
                            2, 0.1D, 0.2D, 0.1D, 0.02D);
                }
                if (this.cookingTicks >= 25) {
                    this.cookingTicks = 0;
                    if (this.kobold.cookOneRawMeat()) {
                        serverLevel.playSound(null, this.blockPos, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F);
                        serverLevel.sendParticles(ParticleTypes.FLAME,
                                this.blockPos.getX() + 0.5D, this.blockPos.getY() + 0.6D, this.blockPos.getZ() + 0.5D,
                                4, 0.15D, 0.1D, 0.15D, 0.01D);
                        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                                this.kobold.getX(), this.kobold.getY() + 1.2D, this.kobold.getZ(),
                                3, 0.2D, 0.2D, 0.2D, 0.0D);
                    }
                }
            }
        }
    }
}
