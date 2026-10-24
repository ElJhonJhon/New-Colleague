package com.newcolleague.mod.entity.ai;

import com.newcolleague.mod.entity.KoboldGirlEntity;
import com.newcolleague.mod.entity.AbstractTribalEntity.CommandStance;
import com.newcolleague.mod.entity.AbstractTribalEntity.KoboldProfession;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Meta de IA para Granjeras: Cosechar cultivos maduros, replantar y sembrar tierra arada.
 */
public class HarvestCropGoal extends MoveToBlockGoal {
    private final KoboldGirlEntity kobold;

    public HarvestCropGoal(KoboldGirlEntity mob, double speed) {
        super(mob, speed, 16);
        this.kobold = mob;
    }

    @Override
    public boolean canUse() {
        if (this.kobold.isSleeping() || this.kobold.isThreatened() || this.kobold.isHunting()) {
            return false;
        }
        if (this.kobold.isOwned() && this.kobold.getCommandStance() != CommandStance.WORK) {
            return false;
        }
        if (this.kobold.getProfession() != KoboldProfession.FARMER && !this.kobold.isAnyQueen()) {
            return false;
        }
        if (!this.kobold.isMorningWorkTime()) {
            return false;
        }
        if (!this.kobold.hasFreeInventorySlot()) {
            return false;
        }
        return super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        if (this.kobold.isSleeping() || this.kobold.isThreatened() || this.kobold.isHunting()) {
            return false;
        }
        if (this.kobold.isOwned() && this.kobold.getCommandStance() != CommandStance.WORK) {
            return false;
        }
        if (this.kobold.getProfession() != KoboldProfession.FARMER && !this.kobold.isAnyQueen()) {
            return false;
        }
        if (!this.kobold.isMorningWorkTime()) {
            return false;
        }
        if (!this.kobold.hasFreeInventorySlot()) {
            return false;
        }
        return super.canContinueToUse();
    }

    @Override
    public void start() {
        super.start();
        this.kobold.setHarvesting(true);
        if (this.kobold.getProfession() == KoboldProfession.FARMER) {
            this.kobold.equipHoeForHarvesting();
        } else {
            this.kobold.unequipHands();
        }
    }

    @Override
    public void stop() {
        super.stop();
        this.kobold.setHarvesting(false);
        this.kobold.unequipHands();
    }

    @Override
    protected int nextStartTick(PathfinderMob mob) {
        return 20 + mob.getRandom().nextInt(20);
    }

    @Override
    public double acceptedDistance() {
        return 2.2D;
    }

    @Override
    protected boolean isValidTarget(LevelReader level, BlockPos pos) {
        BlockState aboveState = level.getBlockState(pos.above());
        if (isMatureCropOrBerry(aboveState)) {
            return true;
        }
        BlockState state = level.getBlockState(pos);
        if (isMatureCropOrBerry(state)) {
            return true;
        }

        if (this.kobold.hasPlantableSeeds()) {
            if (state.is(Blocks.FARMLAND) && aboveState.isAir()) {
                return true;
            }
            if (level.getBlockState(pos.below()).is(Blocks.FARMLAND) && state.isAir()) {
                return true;
            }
        }

        if (this.kobold.hasPlantableSeeds() && isTillableSoilNearWater(level, pos)) {
            return true;
        }

        return false;
    }

    private static boolean isTillableSoilNearWater(LevelReader level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.is(Blocks.GRASS_BLOCK) && !state.is(Blocks.DIRT) && !state.is(Blocks.COARSE_DIRT)) {
            return false;
        }
        if (!level.getBlockState(pos.above()).isAir()) {
            return false;
        }
        for (BlockPos checkPos : BlockPos.betweenClosed(pos.offset(-4, 0, -4), pos.offset(4, 1, 4))) {
            if (level.getFluidState(checkPos).is(FluidTags.WATER)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isMatureCropOrBerry(BlockState state) {
        if (state.getBlock() instanceof CropBlock crop) {
            return crop.isMaxAge(state);
        }
        if (state.getBlock() instanceof SweetBerryBushBlock) {
            return state.getValue(SweetBerryBushBlock.AGE) >= 2;
        }
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isReachedTarget()) {
            if (this.kobold.level() instanceof ServerLevel serverLevel) {
                BlockPos cropPos = this.blockPos.above();
                if (!isMatureCropOrBerry(serverLevel.getBlockState(cropPos))) {
                    cropPos = this.blockPos;
                }
                BlockState cropState = serverLevel.getBlockState(cropPos);

                if (cropState.getBlock() instanceof CropBlock crop && crop.isMaxAge(cropState)) {
                    serverLevel.destroyBlock(cropPos, true, this.kobold.getMainHandItem().isEmpty() ? null : this.kobold);
                    this.kobold.playSound(SoundEvents.CROP_BREAK, 1.0F, 1.0F);

                    List<ItemEntity> drops = serverLevel.getEntitiesOfClass(
                            ItemEntity.class,
                            new AABB(cropPos).inflate(2.5D),
                            item -> item.isAlive() && this.kobold.wantsToPickUpItem(item.getItem())
                    );
                    for (ItemEntity drop : drops) {
                        this.kobold.pickUpItem(serverLevel, drop);
                    }

                    BlockPos soilPos = cropPos.below();
                    if (serverLevel.getBlockState(soilPos).is(Blocks.FARMLAND) && serverLevel.getBlockState(cropPos).isAir()) {
                        serverLevel.setBlock(cropPos, crop.getStateForAge(0), 3);
                        serverLevel.playSound(null, cropPos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
                    }
                    this.stop();
                } else if (cropState.getBlock() instanceof SweetBerryBushBlock) {
                    serverLevel.setBlock(cropPos, cropState.setValue(SweetBerryBushBlock.AGE, 1), 3);
                    ItemStack berries = new ItemStack(Items.SWEET_BERRIES, 2 + this.kobold.getRandom().nextInt(2));
                    if (this.kobold.getInventory().canAddItem(berries)) {
                        this.kobold.getInventory().addItem(berries);
                        this.kobold.playSound(SoundEvents.ITEM_PICKUP, 0.25F, 1.2F);
                    } else {
                        Block.popResource(serverLevel, cropPos, berries);
                    }
                    this.kobold.playSound(SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, 1.0F, 1.0F);
                    this.stop();
                } else if (serverLevel.getBlockState(this.blockPos).is(Blocks.FARMLAND)
                        || serverLevel.getBlockState(this.blockPos.below()).is(Blocks.FARMLAND)) {
                    BlockPos plantPos = this.blockPos.above();
                    BlockPos soilPos = this.blockPos;
                    if (!serverLevel.getBlockState(soilPos).is(Blocks.FARMLAND)) {
                        soilPos = this.blockPos.below();
                        plantPos = this.blockPos;
                    }

                    if (serverLevel.getBlockState(soilPos).is(Blocks.FARMLAND) && serverLevel.getBlockState(plantPos).isAir()) {
                        ItemStack seed = this.kobold.takeOneSeed();
                        if (!seed.isEmpty()) {
                            BlockState newCrop = this.kobold.getCropForSeed(seed);
                            if (newCrop != null) {
                                serverLevel.setBlock(plantPos, newCrop, 3);
                                serverLevel.playSound(null, plantPos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
                            }
                        }
                    }
                    this.stop();
                } else {
                    BlockPos targetPos = this.blockPos;
                    if (!isTillableSoilNearWater(serverLevel, targetPos)) {
                        targetPos = this.blockPos.below();
                    }

                    if (isTillableSoilNearWater(serverLevel, targetPos)) {
                        serverLevel.setBlock(targetPos, Blocks.FARMLAND.defaultBlockState(), 3);
                        serverLevel.playSound(null, targetPos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                                targetPos.getX() + 0.5D, targetPos.getY() + 1.1D, targetPos.getZ() + 0.5D,
                                4, 0.2D, 0.1D, 0.2D, 0.0D);

                        ItemStack hoe = this.kobold.getMainHandItem();
                        if (KoboldGirlEntity.isHoe(hoe)) {
                            hoe.hurtAndBreak(1, serverLevel, null, item -> this.kobold.onEquippedItemBroken(item, EquipmentSlot.MAINHAND));
                        }

                        BlockPos plantPos = targetPos.above();
                        if (serverLevel.getBlockState(plantPos).isAir() && this.kobold.hasPlantableSeeds()) {
                            ItemStack seed = this.kobold.takeOneSeed();
                            if (!seed.isEmpty()) {
                                BlockState newCrop = this.kobold.getCropForSeed(seed);
                                if (newCrop != null) {
                                    serverLevel.setBlock(plantPos, newCrop, 3);
                                    serverLevel.playSound(null, plantPos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
                                }
                            }
                        }
                    }
                    this.stop();
                }
            }
        }
    }
}
