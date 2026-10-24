package com.newcolleague.mod.entity.ai;

import com.newcolleague.mod.entity.KoboldGirlEntity;
import com.newcolleague.mod.entity.AbstractTribalEntity.CommandStance;
import com.newcolleague.mod.entity.AbstractTribalEntity.KoboldProfession;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

/**
 * Meta: Minería sustantiva con consumo de durabilidad de picos, antorchas automáticas en luz < 7 y escoltas
 */
public class KoboldMiningSquadGoal extends Goal {
    private final KoboldGirlEntity kobold;
    private final double speedModifier;
    private BlockPos currentOreTarget = null;
    private int mineTicks = 0;
    private int torchCooldown = 0;

    public KoboldMiningSquadGoal(KoboldGirlEntity kobold, double speedModifier) {
        this.kobold = kobold;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.kobold.isSleeping() || this.kobold.isInPowerStruggle()) return false;
        if (this.kobold.getProfession() != KoboldProfession.MINER) return false;
        if (this.kobold.isOwned() && this.kobold.getCommandStance() != CommandStance.WORK) return false;
        if (!this.kobold.hasPickaxe()) return false;

        this.currentOreTarget = this.findNearbyMineableBlock(this.kobold.level(), this.kobold.blockPosition(), 16);
        return this.currentOreTarget != null;
    }

    @Override
    public void start() {
        this.mineTicks = 0;
        if (this.currentOreTarget != null) {
            this.kobold.getNavigation().moveTo(this.currentOreTarget.getX() + 0.5D, this.currentOreTarget.getY(), this.currentOreTarget.getZ() + 0.5D, this.speedModifier);
            this.summonGuardEscort();
        }
    }

    @Override
    public boolean canContinueToUse() {
        return this.currentOreTarget != null && !this.kobold.level().getBlockState(this.currentOreTarget).isAir() && this.kobold.hasPickaxe();
    }

    @Override
    public void tick() {
        if (this.currentOreTarget == null) return;
        Level level = this.kobold.level();

        // Iluminación automática de cavernas: colocar antorcha si la luz es < 7
        if (this.torchCooldown <= 0) {
            BlockPos standPos = this.kobold.blockPosition();
            if (level.getMaxLocalRawBrightness(standPos) < 7 && level.getBlockState(standPos).isAir()) {
                if (this.kobold.consumeItem(Items.TORCH, 1) || !this.kobold.isOwned()) {
                    level.setBlock(standPos, Blocks.TORCH.defaultBlockState(), 3);
                    this.kobold.playSound(SoundEvents.WOOD_PLACE, 1.0F, 1.0F);
                    this.torchCooldown = 60;
                }
            }
        } else {
            this.torchCooldown--;
        }

        this.kobold.getLookControl().setLookAt(this.currentOreTarget.getX() + 0.5D, this.currentOreTarget.getY() + 0.5D, this.currentOreTarget.getZ() + 0.5D, 30.0F, 30.0F);
        double distSqr = this.kobold.distanceToSqr(this.currentOreTarget.getX() + 0.5D, this.currentOreTarget.getY() + 0.5D, this.currentOreTarget.getZ() + 0.5D);

        if (distSqr <= 9.0D) {
            this.kobold.getNavigation().stop();
            this.mineTicks++;
            if (this.mineTicks % 4 == 0) {
                this.kobold.playSound(SoundEvents.STONE_HIT, 0.7F, 1.0F);
            }
            if (this.mineTicks >= 30) {
                this.mineTicks = 0;
                this.mineTargetBlock(level, this.currentOreTarget);
                this.currentOreTarget = null;
            }
        } else if (this.kobold.getNavigation().isDone()) {
            this.kobold.getNavigation().moveTo(this.currentOreTarget.getX() + 0.5D, this.currentOreTarget.getY(), this.currentOreTarget.getZ() + 0.5D, this.speedModifier);
        }
    }

    private void mineTargetBlock(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return;

        if (level instanceof ServerLevel sl) {
            sl.levelEvent(2001, pos, Block.getId(state));
            sl.destroyBlock(pos, false);

            ItemStack drop;
            if (state.is(Blocks.COAL_ORE) || state.is(Blocks.DEEPSLATE_COAL_ORE)) {
                drop = new ItemStack(Items.COAL);
            } else if (state.is(Blocks.IRON_ORE) || state.is(Blocks.DEEPSLATE_IRON_ORE)) {
                drop = new ItemStack(Items.RAW_IRON);
            } else if (state.is(Blocks.COPPER_ORE) || state.is(Blocks.DEEPSLATE_COPPER_ORE)) {
                drop = new ItemStack(Items.RAW_COPPER);
            } else if (state.is(Blocks.GOLD_ORE) || state.is(Blocks.DEEPSLATE_GOLD_ORE)) {
                drop = new ItemStack(Items.RAW_GOLD);
            } else if (state.is(Blocks.DIAMOND_ORE) || state.is(Blocks.DEEPSLATE_DIAMOND_ORE)) {
                drop = new ItemStack(Items.DIAMOND);
            } else {
                drop = new ItemStack(Items.COBBLESTONE);
            }

            ItemStack leftover = this.kobold.getInventory().addItem(drop);
            if (!leftover.isEmpty()) {
                this.kobold.spawnAtLocation(sl, leftover);
            }

            this.kobold.damagePickaxe(1);
            this.kobold.playSound(SoundEvents.STONE_BREAK, 1.0F, 1.0F);
            sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.kobold.getX(), this.kobold.getY() + 1.2D, this.kobold.getZ(), 4, 0.2D, 0.2D, 0.2D, 0.05D);
        }
    }

    private void summonGuardEscort() {
        List<KoboldGirlEntity> guards = this.kobold.level().getEntitiesOfClass(
                KoboldGirlEntity.class,
                this.kobold.getBoundingBox().inflate(24.0D),
                k -> k != this.kobold && k.isAlive() && (k.getProfession() == KoboldProfession.GUARD || k.getProfession() == KoboldProfession.VIGILANT) && k.getTarget() == null
        );
        int recruited = 0;
        for (KoboldGirlEntity guard : guards) {
            if (recruited >= 2) break;
            guard.getNavigation().moveTo(this.kobold, 1.15D);
            recruited++;
        }
    }

    private @Nullable BlockPos findNearbyMineableBlock(Level level, BlockPos center, int radius) {
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        BlockPos bestPos = null;
        double bestDist = Double.MAX_VALUE;

        for (int dy = -6; dy <= 4; dy++) {
            for (int dx = -radius; dx <= radius; dx += 2) {
                for (int dz = -radius; dz <= radius; dz += 2) {
                    mpos.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    BlockState state = level.getBlockState(mpos);
                    if (isMineableOreOrStone(state)) {
                        double d = center.distSqr(mpos);
                        if (d < bestDist) {
                            bestDist = d;
                            bestPos = mpos.immutable();
                        }
                    }
                }
            }
        }
        return bestPos;
    }

    private boolean isMineableOreOrStone(BlockState state) {
        return state.is(Blocks.COAL_ORE) || state.is(Blocks.DEEPSLATE_COAL_ORE)
                || state.is(Blocks.IRON_ORE) || state.is(Blocks.DEEPSLATE_IRON_ORE)
                || state.is(Blocks.COPPER_ORE) || state.is(Blocks.DEEPSLATE_COPPER_ORE)
                || state.is(Blocks.GOLD_ORE) || state.is(Blocks.DEEPSLATE_GOLD_ORE)
                || state.is(Blocks.DIAMOND_ORE) || state.is(Blocks.DEEPSLATE_DIAMOND_ORE)
                || state.is(Blocks.STONE) || state.is(Blocks.DEEPSLATE);
    }
}
