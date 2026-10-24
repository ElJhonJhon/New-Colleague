package com.newcolleague.mod.entity.ai;

import com.newcolleague.mod.entity.KoboldGirlEntity;
import com.newcolleague.mod.entity.AbstractTribalEntity.CommandStance;
import com.newcolleague.mod.entity.AbstractTribalEntity.KoboldProfession;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Meta: Exploración e Inteligencia táctica (Mapeo de POIs, sigilo ante hostiles y reporte presencial a la Reina)
 */
public class KoboldScoutGoal extends Goal {
    private final KoboldGirlEntity kobold;
    private final double speedModifier;
    private final Set<BlockPos> discoveredPOIs = new HashSet<>();
    private int scanCooldown = 0;
    private boolean hasReportedTonight = false;
    private BlockPos currentExploreTarget = null;
    private int exploreTargetTicks = 0;

    public KoboldScoutGoal(KoboldGirlEntity kobold, double speedModifier) {
        this.kobold = kobold;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.kobold.isSleeping() || this.kobold.isInPowerStruggle()) return false;
        if (this.kobold.getProfession() != KoboldProfession.SCOUT) return false;
        if (this.kobold.isOwned() && this.kobold.getCommandStance() != CommandStance.WORK) return false;
        return true;
    }

    @Override
    public void start() {
        this.hasReportedTonight = false;
        this.exploreTargetTicks = 0;
        this.currentExploreTarget = null;
    }

    @Override
    public void stop() {
        this.kobold.setShiftKeyDown(false);
        this.currentExploreTarget = null;
    }

    @Override
    public void tick() {
        Level level = this.kobold.level();
        boolean isNight = this.kobold.isNightTime();

        // 1. Sigilo y evasión (SNEAK_AND_EVADE) si hay monstruos o amenazas hostiles cerca (16 bloques)
        List<LivingEntity> threats = level.getEntitiesOfClass(
                LivingEntity.class,
                this.kobold.getBoundingBox().inflate(16.0D),
                e -> e.isAlive() && (e instanceof Monster || e instanceof Enemy) && this.kobold.hasLineOfSight(e)
        );
        if (!threats.isEmpty()) {
            this.kobold.setShiftKeyDown(true);
            LivingEntity nearest = threats.get(0);
            Vec3 awayVec = this.kobold.position().subtract(nearest.position()).normalize().scale(8.0D);
            BlockPos awayPos = BlockPos.containing(this.kobold.position().add(awayVec));
            if (this.kobold.getNavigation().isDone() && this.kobold.isStepSafe(awayPos)) {
                this.kobold.getNavigation().moveTo(awayPos.getX() + 0.5D, awayPos.getY(), awayPos.getZ() + 0.5D, this.speedModifier * 0.8D);
            }
            return;
        } else {
            this.kobold.setShiftKeyDown(false);
        }

        // 2. Al atardecer o noche: Regresar a reportar presencialmente a la Reina de la tribu
        if (isNight) {
            KoboldGirlEntity queen = this.kobold.getTribeQueen();
            if (queen != null && queen.isAlive()) {
                double distToQueen = this.kobold.distanceToSqr(queen);
                if (distToQueen > 9.0D) {
                    this.kobold.getNavigation().moveTo(queen, this.speedModifier);
                } else {
                    this.kobold.getNavigation().stop();
                    this.kobold.getLookControl().setLookAt(queen, 30.0F, 30.0F);
                    if (!this.hasReportedTonight) {
                        this.hasReportedTonight = true;
                        this.deliverReportToQueen(queen);
                    }
                }
                return;
            }
        } else {
            this.hasReportedTonight = false;
        }

        // 3. Mapeo de POIs cada 40 ticks
        if (this.scanCooldown <= 0) {
            this.scanCooldown = 40;
            this.scanNearbyPOIs(level);
        } else {
            this.scanCooldown--;
        }

        // 4. Exploración y patrullaje de radio extendido durante el día
        this.exploreTargetTicks++;
        if (this.currentExploreTarget == null || this.exploreTargetTicks > 200 || this.kobold.getNavigation().isDone()) {
            this.exploreTargetTicks = 0;
            this.pickNextExplorationTarget(level);
        }
    }

    private void scanNearbyPOIs(Level level) {
        BlockPos current = this.kobold.blockPosition();
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();

        for (int dx = -12; dx <= 12; dx += 3) {
            for (int dy = -4; dy <= 4; dy += 2) {
                for (int dz = -12; dz <= 12; dz += 3) {
                    mpos.set(current.getX() + dx, current.getY() + dy, current.getZ() + dz);
                    BlockState state = level.getBlockState(mpos);

                    if (state.is(Blocks.IRON_ORE) || state.is(Blocks.DEEPSLATE_IRON_ORE)
                            || state.is(Blocks.GOLD_ORE) || state.is(Blocks.DEEPSLATE_GOLD_ORE)
                            || state.is(Blocks.DIAMOND_ORE) || state.is(Blocks.DEEPSLATE_DIAMOND_ORE)) {
                        BlockPos imm = mpos.immutable();
                        if (this.discoveredPOIs.add(imm)) {
                            this.kobold.playSound(SoundEvents.SPYGLASS_USE, 0.6F, 1.4F);
                        }
                    }

                    if (state.is(BlockTags.BEDS)) {
                        this.discoveredPOIs.add(mpos.immutable());
                    }
                }
            }
        }

        List<KoboldGirlEntity> rivalQueens = level.getEntitiesOfClass(
                KoboldGirlEntity.class,
                this.kobold.getBoundingBox().inflate(24.0D),
                q -> q.isAlive() && q.isAnyQueen() && !q.getTribeName().equals(this.kobold.getTribeName())
        );
        for (KoboldGirlEntity rq : rivalQueens) {
            this.discoveredPOIs.add(rq.blockPosition());
        }
    }

    private void pickNextExplorationTarget(Level level) {
        RandomSource rand = this.kobold.getRandom();
        double angle = rand.nextDouble() * 2.0D * Math.PI;
        double dist = 24.0D + rand.nextDouble() * 24.0D;
        int tx = this.kobold.getBlockX() + (int)(Math.cos(angle) * dist);
        int tz = this.kobold.getBlockZ() + (int)(Math.sin(angle) * dist);
        int ty = level.getHeight(Heightmap.Types.WORLD_SURFACE, tx, tz);

        BlockPos target = new BlockPos(tx, ty, tz);
        if (this.kobold.isStepSafe(target)) {
            this.currentExploreTarget = target;
            this.kobold.getNavigation().moveTo(tx + 0.5D, ty, tz + 0.5D, this.speedModifier);
        }
    }

    private void deliverReportToQueen(KoboldGirlEntity queen) {
        this.kobold.playSound(SoundEvents.VILLAGER_YES, 1.0F, 1.2F);
        queen.playSound(SoundEvents.VILLAGER_YES, 1.0F, 1.0F);
        if (this.kobold.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.kobold.getX(), this.kobold.getY() + 1.2D, this.kobold.getZ(), 8, 0.3D, 0.3D, 0.3D, 0.05D);
            sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, queen.getX(), queen.getY() + 1.2D, queen.getZ(), 8, 0.3D, 0.3D, 0.3D, 0.05D);
        }
        if (!this.discoveredPOIs.isEmpty()) {
            this.discoveredPOIs.clear();
            this.kobold.addProsperity(5);
        }
    }
}
