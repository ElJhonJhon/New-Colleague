package com.newcolleague.mod.entity.ai;

import com.newcolleague.mod.entity.KoboldGirlEntity;
import com.newcolleague.mod.entity.AbstractTribalEntity.CommandStance;
import com.newcolleague.mod.entity.AbstractTribalEntity.KoboldProfession;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

/**
 * Meta de Minería Progresiva por Cantera ("Zona de Minería"):
 * Exclusiva para Mineras. Establece una cantera comunal de 5x5, retira capa por capa
 * de arriba hacia abajo (tierra/arena -> piedra/minerales), manteniendo una columna de escaleras
 * en una esquina para que la minera siempre pueda subir y bajar sin quedar atrapada en hoyos.
 * Los materiales son depositados en los cofres comunales para que las Constructoras construyan.
 */
public class KoboldMiningSquadGoal extends Goal {

    private final KoboldGirlEntity kobold;
    private final double speedModifier;

    private @Nullable BlockPos currentMineTarget = null;
    private int mineTicks = 0;
    private int torchCooldown = 0;
    private int searchCooldown = 0;

    public KoboldMiningSquadGoal(KoboldGirlEntity kobold, double speedModifier) {
        this.kobold = kobold;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.kobold.isSleeping() || this.kobold.isInPowerStruggle()) {
            return false;
        }
        if (this.kobold.getProfession() != KoboldProfession.MINER) {
            return false;
        }
        if (this.kobold.isOwned() && this.kobold.getCommandStance() != CommandStance.WORK) {
            return false;
        }
        if (!this.kobold.isWorkScheduleTime() || this.kobold.isSleepTime()) {
            return false;
        }
        if (!this.kobold.hasPickaxe() && !this.kobold.hasShovel()) {
            return false;
        }
        if (--this.searchCooldown > 0) {
            return false;
        }
        this.searchCooldown = 20 + this.kobold.getRandom().nextInt(20);

        Level level = this.kobold.level();
        BlockPos zoneCenter = this.getOrEstablishMiningZone(level);
        if (zoneCenter == null) {
            return false;
        }

        // Buscar el siguiente bloque a excavar en la cantera (capa por capa de arriba hacia abajo)
        this.currentMineTarget = this.findNextQuarryBlock(level, zoneCenter);
        return this.currentMineTarget != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.kobold.isSleeping() || (!this.kobold.hasPickaxe() && !this.kobold.hasShovel())) {
            return false;
        }
        return this.currentMineTarget != null && !this.kobold.level().getBlockState(this.currentMineTarget).isAir();
    }

    @Override
    public void start() {
        this.mineTicks = 0;
        if (this.currentMineTarget != null) {
            BlockPos standPos = this.findStandablePosNear(this.kobold.level(), this.currentMineTarget);
            BlockPos navPos = standPos != null ? standPos : this.currentMineTarget;
            this.kobold.getNavigation().moveTo(
                    navPos.getX() + 0.5D,
                    navPos.getY(),
                    navPos.getZ() + 0.5D,
                    this.speedModifier
            );
            this.summonGuardEscort();
        }
    }

    @Override
    public void stop() {
        this.currentMineTarget = null;
        this.mineTicks = 0;
        this.searchCooldown = 20;
    }

    @Override
    public void tick() {
        Level level = this.kobold.level();

        // 1. Auto-rescate si la minera quedó atrapada en un hoyo estrecho
        this.checkAndRescueIfTrapped(level);

        if (this.currentMineTarget == null) return;

        // 2. Iluminación de seguridad en la cantera: colocar antorcha si la luz es < 7
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

        this.kobold.getLookControl().setLookAt(
                this.currentMineTarget.getX() + 0.5D,
                this.currentMineTarget.getY() + 0.5D,
                this.currentMineTarget.getZ() + 0.5D,
                30.0F, 30.0F
        );
        double distSqr = this.kobold.distanceToSqr(
                this.currentMineTarget.getX() + 0.5D,
                this.currentMineTarget.getY() + 0.5D,
                this.currentMineTarget.getZ() + 0.5D
        );

        if (distSqr <= 16.0D || (this.kobold.getNavigation().isDone() && distSqr <= 20.0D)) {
            this.kobold.getNavigation().stop();
            this.mineTicks++;

            BlockState state = level.getBlockState(this.currentMineTarget);
            boolean isSurface = isSurfaceBlock(state);

            if (this.mineTicks % 4 == 0) {
                this.kobold.playSound(isSurface ? SoundEvents.GRAVEL_HIT : SoundEvents.STONE_HIT, 0.7F, 1.0F);
            }

            if (this.mineTicks >= (isSurface ? 18 : 28)) {
                this.mineTicks = 0;
                this.mineTargetBlock(level, this.currentMineTarget);
                this.currentMineTarget = null;
            }
        } else if (this.kobold.getNavigation().isDone()) {
            BlockPos standPos = this.findStandablePosNear(level, this.currentMineTarget);
            BlockPos navPos = standPos != null ? standPos : this.currentMineTarget;
            this.kobold.getNavigation().moveTo(
                    navPos.getX() + 0.5D,
                    navPos.getY(),
                    navPos.getZ() + 0.5D,
                    this.speedModifier
            );
        }
    }

    private void checkAndRescueIfTrapped(Level level) {
        BlockPos feet = this.kobold.blockPosition();
        BlockPos head = feet.above();
        // Si tiene pared sólida en las 4 direcciones a la altura del cuerpo y no puede moverse
        boolean north = level.getBlockState(feet.north()).isSolid();
        boolean south = level.getBlockState(feet.south()).isSolid();
        boolean east = level.getBlockState(feet.east()).isSolid();
        boolean west = level.getBlockState(feet.west()).isSolid();

        if (north && south && east && west && level.getBlockState(feet.below()).isSolid()) {
            // Hoyo de 1x1: colocar un bloque de apoyo debajo o saltar para salir
            if (level instanceof ServerLevel sl) {
                BlockPos aboveHead = head.above();
                if (sl.getBlockState(aboveHead).isAir()) {
                    this.kobold.setPos(this.kobold.getX(), feet.getY() + 1.0D, this.kobold.getZ());
                    sl.setBlock(feet, Blocks.COBBLESTONE.defaultBlockState(), 3);
                    this.kobold.playSound(SoundEvents.STONE_PLACE, 1.0F, 1.0F);
                }
            }
        }
    }

    private void mineTargetBlock(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return;

        if (level instanceof ServerLevel sl) {
            BlockPos above = pos.above();
            if (level.getBlockState(above).canBeReplaced() && !level.getBlockState(above).isAir()) {
                sl.destroyBlock(above, true);
            }

            sl.levelEvent(2001, pos, Block.getId(state));
            sl.destroyBlock(pos, false);

            ItemStack drop;
            if (isSurfaceBlock(state)) {
                if (state.is(Blocks.SAND) || state.is(Blocks.RED_SAND)) {
                    drop = new ItemStack(Items.SAND);
                } else if (state.is(Blocks.GRAVEL)) {
                    drop = new ItemStack(Items.GRAVEL);
                } else if (state.is(Blocks.CLAY)) {
                    drop = new ItemStack(Items.CLAY_BALL, 4);
                } else {
                    drop = new ItemStack(Items.DIRT);
                }
                this.kobold.damageShovel(1);
                this.kobold.playSound(SoundEvents.GRAVEL_BREAK, 1.0F, 1.0F);
            } else {
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
                this.kobold.damagePickaxe(1);
                this.kobold.playSound(SoundEvents.STONE_BREAK, 1.0F, 1.0F);
            }

            ItemStack leftover = this.kobold.getInventory().addItem(drop);
            if (!leftover.isEmpty()) {
                this.kobold.spawnAtLocation(sl, leftover);
            }

            sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.kobold.getX(), this.kobold.getY() + 1.2D, this.kobold.getZ(), 4, 0.2D, 0.2D, 0.2D, 0.05D);

            // Asegurar escaleras de acceso en la columna de esquina si la cantera desciende
            BlockPos zoneCenter = this.getOrEstablishMiningZone(level);
            if (zoneCenter != null) {
                this.ensureQuarryLadderAccess(sl, zoneCenter, pos.getY());
            }

            // Almacenar recursos en los cofres comunales si acumula buena cantidad
            this.tryStoreMinedResourcesInVillageChests(sl);
        }
    }

    private void ensureQuarryLadderAccess(ServerLevel sl, BlockPos zoneCenter, int currentMinY) {
        int radius = 2;
        int lx = zoneCenter.getX() - radius;
        int lz = zoneCenter.getZ() - radius;
        int surfaceY = sl.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, lx, lz);

        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        for (int y = currentMinY; y <= surfaceY; y++) {
            mpos.set(lx, y, lz);
            BlockPos wallPos = mpos.north(); // Pared sólida al norte
            if (!sl.getBlockState(wallPos).isSolid()) {
                sl.setBlock(wallPos, Blocks.COBBLESTONE.defaultBlockState(), 3);
            }
            if (sl.getBlockState(mpos).isAir() || sl.getBlockState(mpos).canBeReplaced()) {
                sl.setBlock(mpos, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH), 3);
            }
        }
    }

    private void tryStoreMinedResourcesInVillageChests(ServerLevel sl) {
        int mineralAndStoneCount = 0;
        for (int i = 0; i < this.kobold.getInventory().getContainerSize(); i++) {
            ItemStack stack = this.kobold.getInventory().getItem(i);
            if (stack.is(Items.COBBLESTONE) || stack.is(Items.DIRT) || stack.is(Items.SAND) || stack.is(Items.GRAVEL)
                    || stack.is(Items.COAL) || stack.is(Items.RAW_IRON) || stack.is(Items.RAW_COPPER)) {
                mineralAndStoneCount += stack.getCount();
            }
        }
        if (mineralAndStoneCount < 16) return;

        BlockPos center = this.kobold.getCampCenterPos();
        BlockPos chestPos = this.kobold.findNearbyUniqueChests(sl, center != null ? center : this.kobold.blockPosition(), 36, 1)
                .stream().findFirst().orElse(null);

        if (chestPos != null) {
            Container chest = KoboldGirlEntity.getChestContainer(sl, chestPos);
            if (chest != null) {
                for (int i = 0; i < this.kobold.getInventory().getContainerSize(); i++) {
                    ItemStack stack = this.kobold.getInventory().getItem(i);
                    if (stack.is(Items.COBBLESTONE) || stack.is(Items.DIRT) || stack.is(Items.SAND) || stack.is(Items.GRAVEL)
                            || stack.is(Items.COAL) || stack.is(Items.RAW_IRON) || stack.is(Items.RAW_COPPER)) {
                        ItemStack leftover = KoboldGirlEntity.addItemToContainer(chest, stack);
                        this.kobold.getInventory().setItem(i, leftover);
                    }
                }
                chest.setChanged();
                this.kobold.playSound(SoundEvents.CHEST_CLOSE, 0.7F, 1.0F);
            }
        }
    }

    private @Nullable BlockPos getOrEstablishMiningZone(Level level) {
        BlockPos existing = this.kobold.getMiningZonePos();
        if (existing != null) return existing;

        BlockPos center = this.kobold.getCampCenterPos();
        if (center == null) center = this.kobold.blockPosition();

        int baseAngle = this.kobold.getVillageOrientationAngle();
        int quarryDist = 28;
        int angle = (baseAngle + 270) % 360;
        double rad = Math.toRadians(angle);
        int qx = center.getX() + (int) Math.round(quarryDist * Math.sin(rad));
        int qz = center.getZ() - (int) Math.round(quarryDist * Math.cos(rad));

        // Ajustar si colisiona con parcelas de la aldea
        for (int da = 0; da <= 90; da += 30) {
            double testRad = Math.toRadians((angle + da) % 360);
            int tx = center.getX() + (int) Math.round(quarryDist * Math.sin(testRad));
            int tz = center.getZ() - (int) Math.round(quarryDist * Math.cos(testRad));
            if (!this.kobold.isPosInsideAnyOccupiedPlot(tx, tz)) {
                int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, tx, tz);
                BlockPos zonePos = new BlockPos(tx, surfaceY, tz);
                this.kobold.setMiningZonePos(zonePos);
                return zonePos;
            }
        }

        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, qx, qz);
        BlockPos zonePos = new BlockPos(qx, surfaceY, qz);
        this.kobold.setMiningZonePos(zonePos);
        return zonePos;
    }

    private @Nullable BlockPos findNextQuarryBlock(Level level, BlockPos zoneCenter) {
        int radius = 2;
        int maxSolidY = Integer.MIN_VALUE;

        // 1. Encontrar la altura máxima actual con bloques sólidos en la cantera usando MOTION_BLOCKING_NO_LEAVES
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                // La esquina de acceso (-radius, -radius) se reserva para la columna de escaleras
                if (dx == -radius && dz == -radius) continue;

                int cx = zoneCenter.getX() + dx;
                int cz = zoneCenter.getZ() + dz;
                int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, cx, cz);

                BlockPos checkPos = new BlockPos(cx, surfaceY, cz);
                while (checkPos.getY() > level.getMinY() + 5 && (level.getBlockState(checkPos).isAir() || level.getBlockState(checkPos).canBeReplaced())) {
                    checkPos = checkPos.below();
                }

                BlockState state = level.getBlockState(checkPos);
                if (isExcavatableBlock(state) && checkPos.getY() > maxSolidY) {
                    maxSolidY = checkPos.getY();
                }
            }
        }

        if (maxSolidY == Integer.MIN_VALUE || maxSolidY <= level.getMinY() + 5) {
            return null;
        }

        // 2. Extraer bloques de la capa superior actual (priorizando materiales superficiales de tierra/arena)
        // Regla esencial: NO picar el bloque directamente bajo sus pies si hay otros disponibles en la capa
        BlockPos currentFeet = this.kobold.blockPosition().below();
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        BlockPos candidateSurface = null;
        BlockPos candidateStone = null;
        BlockPos feetFallback = null;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx == -radius && dz == -radius) continue; // Columna de escaleras reservada

                int cx = zoneCenter.getX() + dx;
                int cz = zoneCenter.getZ() + dz;
                mpos.set(cx, maxSolidY, cz);
                BlockState state = level.getBlockState(mpos);

                if (isExcavatableBlock(state)) {
                    BlockPos candidate = mpos.immutable();
                    if (candidate.equals(currentFeet) || candidate.equals(this.kobold.blockPosition())) {
                        feetFallback = candidate;
                        continue;
                    }
                    if (isSurfaceBlock(state)) {
                        candidateSurface = candidate;
                        break;
                    } else if (candidateStone == null) {
                        candidateStone = candidate;
                    }
                }
            }
            if (candidateSurface != null) break;
        }

        if (candidateSurface != null) return candidateSurface;
        if (candidateStone != null) return candidateStone;
        return feetFallback;
    }

    private @Nullable BlockPos findStandablePosNear(Level level, BlockPos target) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos adj = target.relative(dir);
            BlockPos below = adj.below();
            if (level.getBlockState(below).isSolid() && level.getBlockState(adj).isAir() && level.getBlockState(adj.above()).isAir()) {
                return adj;
            }
        }
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos adjAbove = target.relative(dir).above();
            BlockPos below = target.relative(dir);
            if (level.getBlockState(below).isSolid() && level.getBlockState(adjAbove).isAir() && level.getBlockState(adjAbove.above()).isAir()) {
                return adjAbove;
            }
        }
        return null;
    }

    private boolean isExcavatableBlock(BlockState state) {
        if (state.isAir() || state.is(Blocks.BEDROCK) || state.is(Blocks.WATER) || state.is(Blocks.LAVA)) {
            return false;
        }
        return isSurfaceBlock(state) || isStoneOrOreBlock(state);
    }

    private boolean isSurfaceBlock(BlockState state) {
        return state.is(BlockTags.DIRT) || state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT)
                || state.is(BlockTags.SAND) || state.is(Blocks.GRAVEL)
                || state.is(Blocks.CLAY) || state.is(Blocks.MUD);
    }

    private boolean isStoneOrOreBlock(BlockState state) {
        return state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(Blocks.STONE)
                || state.is(Blocks.COBBLESTONE) || state.is(Blocks.DEEPSLATE)
                || state.is(Blocks.COBBLED_DEEPSLATE) || state.is(Blocks.ANDESITE)
                || state.is(Blocks.DIORITE) || state.is(Blocks.GRANITE)
                || state.is(Blocks.COAL_ORE) || state.is(Blocks.DEEPSLATE_COAL_ORE)
                || state.is(Blocks.IRON_ORE) || state.is(Blocks.DEEPSLATE_IRON_ORE)
                || state.is(Blocks.COPPER_ORE) || state.is(Blocks.DEEPSLATE_COPPER_ORE)
                || state.is(Blocks.GOLD_ORE) || state.is(Blocks.DEEPSLATE_GOLD_ORE)
                || state.is(Blocks.DIAMOND_ORE) || state.is(Blocks.DEEPSLATE_DIAMOND_ORE);
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
}
