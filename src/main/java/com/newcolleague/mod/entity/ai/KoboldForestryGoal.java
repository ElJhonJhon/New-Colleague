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
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;

/**
 * Meta de Silvicultura y Carpintería Autónoma:
 * Exclusiva para Carpinteras. Tala árboles de manera sostenible, recolecta madera,
 * replanta retoños asegurando un espaciado de al menos 4 bloques entre árboles
 * (con excepción del roble oscuro que se siembra en grupos de 2x2 de 4 retoños juntos),
 * y almacena los materiales en los cofres comunales.
 */
public class KoboldForestryGoal extends Goal {

    private final KoboldGirlEntity kobold;
    private final double speedModifier;

    private @Nullable BlockPos currentTreeLog = null;
    private @Nullable BlockPos currentPlantSpot = null;
    private int chopTicks = 0;
    private int searchCooldown = 0;

    public KoboldForestryGoal(KoboldGirlEntity kobold, double speedModifier) {
        this.kobold = kobold;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.kobold.isSleeping() || this.kobold.isThreatened() || this.kobold.isInPowerStruggle()) {
            return false;
        }
        if (this.kobold.getProfession() != KoboldProfession.CARPENTER) {
            return false;
        }
        if (this.kobold.isOwned() && this.kobold.getCommandStance() != CommandStance.WORK) {
            return false;
        }
        if (!this.kobold.isWorkScheduleTime() || this.kobold.isSleepTime()) {
            return false;
        }
        if (!this.kobold.hasAxe()) {
            return false;
        }
        if (--this.searchCooldown > 0) {
            return false;
        }
        this.searchCooldown = 20 + this.kobold.getRandom().nextInt(20);

        Level level = this.kobold.level();
        BlockPos center = this.kobold.blockPosition();

        // 1. Prioridad: Replantar retoños si tiene en inventario y hay tierra desprovista de árboles
        ItemStack saplingStack = this.getFirstSaplingStack();
        if (!saplingStack.isEmpty()) {
            this.currentPlantSpot = this.findNearbyPlantSpot(level, center, 18, saplingStack);
            if (this.currentPlantSpot != null) {
                this.currentTreeLog = null;
                return true;
            }
        }

        // 2. Talar árboles naturales en los alrededores fuera de las parcelas de la aldea
        this.currentTreeLog = this.findNearbyHarvestableTree(level, center, 32);
        return this.currentTreeLog != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.kobold.isSleeping() || this.kobold.isThreatened() || !this.kobold.hasAxe()) {
            return false;
        }
        if (this.currentPlantSpot != null) {
            return this.hasSaplingsInInventory() && this.kobold.level().getBlockState(this.currentPlantSpot).isAir();
        }
        if (this.currentTreeLog != null) {
            return this.kobold.level().getBlockState(this.currentTreeLog).is(BlockTags.LOGS);
        }
        return false;
    }

    @Override
    public void start() {
        this.chopTicks = 0;
        BlockPos target = this.currentPlantSpot != null ? this.currentPlantSpot : this.currentTreeLog;
        if (target != null) {
            this.kobold.getNavigation().moveTo(target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D, this.speedModifier);
        }
    }

    @Override
    public void stop() {
        this.currentTreeLog = null;
        this.currentPlantSpot = null;
        this.chopTicks = 0;
        this.searchCooldown = 20;
    }

    @Override
    public void tick() {
        Level level = this.kobold.level();

        // Tarea A: Replantar retoño
        if (this.currentPlantSpot != null) {
            this.kobold.getLookControl().setLookAt(
                    this.currentPlantSpot.getX() + 0.5D,
                    this.currentPlantSpot.getY() + 0.5D,
                    this.currentPlantSpot.getZ() + 0.5D,
                    30.0F, 30.0F
            );
            double distSqr = this.kobold.distanceToSqr(this.currentPlantSpot.getX() + 0.5D, this.currentPlantSpot.getY(), this.currentPlantSpot.getZ() + 0.5D);
            if (distSqr <= 9.0D) {
                this.kobold.getNavigation().stop();
                ItemStack saplingStack = this.getFirstSaplingStack();
                if (!saplingStack.isEmpty() && (level.getBlockState(this.currentPlantSpot).isAir() || level.getBlockState(this.currentPlantSpot).canBeReplaced())
                        && level.getBlockState(this.currentPlantSpot.below()).is(BlockTags.DIRT)) {

                    BlockState saplingState = this.getSaplingBlockState(saplingStack);
                    level.setBlock(this.currentPlantSpot, saplingState, 3);
                    this.consumeSpecificSapling(saplingStack.getItem());
                    this.kobold.playSound(SoundEvents.GRASS_PLACE, 1.0F, 1.0F);

                    if (level instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.currentPlantSpot.getX() + 0.5D, this.currentPlantSpot.getY() + 0.5D, this.currentPlantSpot.getZ() + 0.5D, 5, 0.2D, 0.2D, 0.2D, 0.05D);
                    }
                }
                this.currentPlantSpot = null;
            } else if (this.kobold.getNavigation().isDone()) {
                this.kobold.getNavigation().moveTo(this.currentPlantSpot.getX() + 0.5D, this.currentPlantSpot.getY(), this.currentPlantSpot.getZ() + 0.5D, this.speedModifier);
            }
            return;
        }

        // Tarea B: Talar tronco
        if (this.currentTreeLog != null) {
            this.kobold.getLookControl().setLookAt(
                    this.currentTreeLog.getX() + 0.5D,
                    this.currentTreeLog.getY() + 0.5D,
                    this.currentTreeLog.getZ() + 0.5D,
                    30.0F, 30.0F
            );
            double distSqr = this.kobold.distanceToSqr(this.currentTreeLog.getX() + 0.5D, this.currentTreeLog.getY(), this.currentTreeLog.getZ() + 0.5D);
            if (distSqr <= 9.0D) {
                this.kobold.getNavigation().stop();
                this.chopTicks++;
                if (this.chopTicks % 4 == 0) {
                    this.kobold.playSound(SoundEvents.WOOD_HIT, 0.7F, 1.0F);
                }
                if (this.chopTicks >= 25) {
                    this.chopTicks = 0;
                    this.harvestTreeColumn(level, this.currentTreeLog);
                    this.currentTreeLog = null;
                }
            } else if (this.kobold.getNavigation().isDone()) {
                this.kobold.getNavigation().moveTo(this.currentTreeLog.getX() + 0.5D, this.currentTreeLog.getY(), this.currentTreeLog.getZ() + 0.5D, this.speedModifier);
            }
        }
    }

    private void harvestTreeColumn(Level level, BlockPos baseLog) {
        if (!(level instanceof ServerLevel sl)) return;

        BlockPos.MutableBlockPos mpos = baseLog.mutable();
        int logsCut = 0;

        while (logsCut < 8 && sl.getBlockState(mpos).is(BlockTags.LOGS)) {
            BlockState logState = sl.getBlockState(mpos);
            sl.levelEvent(2001, mpos, Block.getId(logState));
            sl.destroyBlock(mpos, false);

            ItemStack logDrop = new ItemStack(logState.getBlock().asItem());
            if (logDrop.isEmpty()) logDrop = new ItemStack(Items.OAK_LOG);

            ItemStack leftover = this.kobold.getInventory().addItem(logDrop);
            if (!leftover.isEmpty()) {
                this.kobold.spawnAtLocation(sl, leftover);
            }

            // Ocasionalmente obtiene retoños de la especie adecuada y palos
            if (sl.getRandom().nextFloat() < 0.45F) {
                ItemStack saplingDrop = this.getMatchingSaplingForLog(logState);
                ItemStack saplingLeftover = this.kobold.getInventory().addItem(saplingDrop);
                if (!saplingLeftover.isEmpty()) {
                    this.kobold.spawnAtLocation(sl, saplingLeftover);
                }
            }
            if (sl.getRandom().nextFloat() < 0.5F) {
                this.kobold.getInventory().addItem(new ItemStack(Items.STICK, 2));
            }

            logsCut++;
            mpos.move(Direction.UP);
        }

        this.kobold.damageAxe(logsCut);
        this.kobold.playSound(SoundEvents.WOOD_BREAK, 1.0F, 1.0F);
        sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.kobold.getX(), this.kobold.getY() + 1.2D, this.kobold.getZ(), 6, 0.3D, 0.3D, 0.3D, 0.05D);

        // Si tiene madera suficiente, almacena parte en cofres comunales
        this.tryStoreExcessWoodInVillageChests(sl);
    }

    private ItemStack getMatchingSaplingForLog(BlockState logState) {
        if (logState.is(Blocks.DARK_OAK_LOG) || logState.is(Blocks.DARK_OAK_WOOD)) return new ItemStack(Items.DARK_OAK_SAPLING);
        if (logState.is(Blocks.BIRCH_LOG) || logState.is(Blocks.BIRCH_WOOD)) return new ItemStack(Items.BIRCH_SAPLING);
        if (logState.is(Blocks.SPRUCE_LOG) || logState.is(Blocks.SPRUCE_WOOD)) return new ItemStack(Items.SPRUCE_SAPLING);
        if (logState.is(Blocks.JUNGLE_LOG) || logState.is(Blocks.JUNGLE_WOOD)) return new ItemStack(Items.JUNGLE_SAPLING);
        if (logState.is(Blocks.ACACIA_LOG) || logState.is(Blocks.ACACIA_WOOD)) return new ItemStack(Items.ACACIA_SAPLING);
        if (logState.is(Blocks.CHERRY_LOG) || logState.is(Blocks.CHERRY_WOOD)) return new ItemStack(Items.CHERRY_SAPLING);
        return new ItemStack(Items.OAK_SAPLING);
    }

    private void tryStoreExcessWoodInVillageChests(ServerLevel sl) {
        int totalLogs = 0;
        for (int i = 0; i < this.kobold.getInventory().getContainerSize(); i++) {
            ItemStack stack = this.kobold.getInventory().getItem(i);
            if (stack.is(ItemTags.LOGS) || stack.is(ItemTags.PLANKS)) {
                totalLogs += stack.getCount();
            }
        }
        if (totalLogs < 16) return;

        BlockPos center = this.kobold.getCampCenterPos();
        BlockPos chestPos = this.kobold.findNearbyUniqueChests(sl, center != null ? center : this.kobold.blockPosition(), 36, 1)
                .stream().findFirst().orElse(null);

        if (chestPos != null) {
            Container chest = KoboldGirlEntity.getChestContainer(sl, chestPos);
            if (chest != null) {
                for (int i = 0; i < this.kobold.getInventory().getContainerSize(); i++) {
                    ItemStack stack = this.kobold.getInventory().getItem(i);
                    if (stack.is(ItemTags.LOGS) || stack.is(ItemTags.PLANKS) || stack.is(Items.STICK)) {
                        ItemStack leftover = KoboldGirlEntity.addItemToContainer(chest, stack);
                        this.kobold.getInventory().setItem(i, leftover);
                    }
                }
                chest.setChanged();
                this.kobold.playSound(SoundEvents.CHEST_CLOSE, 0.7F, 1.0F);
            }
        }
    }

    private boolean hasSaplingsInInventory() {
        return !this.getFirstSaplingStack().isEmpty();
    }

    private ItemStack getFirstSaplingStack() {
        for (int i = 0; i < this.kobold.getInventory().getContainerSize(); i++) {
            ItemStack stack = this.kobold.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.is(ItemTags.SAPLINGS)) return stack;
        }
        return ItemStack.EMPTY;
    }

    private void consumeSpecificSapling(net.minecraft.world.item.Item saplingItem) {
        for (int i = 0; i < this.kobold.getInventory().getContainerSize(); i++) {
            ItemStack stack = this.kobold.getInventory().getItem(i);
            if (stack.is(saplingItem)) {
                stack.shrink(1);
                return;
            }
        }
    }

    private BlockState getSaplingBlockState(ItemStack saplingStack) {
        if (saplingStack.is(Items.DARK_OAK_SAPLING)) return Blocks.DARK_OAK_SAPLING.defaultBlockState();
        if (saplingStack.is(Items.BIRCH_SAPLING)) return Blocks.BIRCH_SAPLING.defaultBlockState();
        if (saplingStack.is(Items.SPRUCE_SAPLING)) return Blocks.SPRUCE_SAPLING.defaultBlockState();
        if (saplingStack.is(Items.JUNGLE_SAPLING)) return Blocks.JUNGLE_SAPLING.defaultBlockState();
        if (saplingStack.is(Items.ACACIA_SAPLING)) return Blocks.ACACIA_SAPLING.defaultBlockState();
        if (saplingStack.is(Items.CHERRY_SAPLING)) return Blocks.CHERRY_SAPLING.defaultBlockState();
        if (saplingStack.getItem() instanceof BlockItem bi) {
            return bi.getBlock().defaultBlockState();
        }
        return Blocks.OAK_SAPLING.defaultBlockState();
    }

    private @Nullable BlockPos findNearbyPlantSpot(Level level, BlockPos center, int radius, ItemStack saplingStack) {
        boolean isDarkOak = saplingStack.is(Items.DARK_OAK_SAPLING);

        if (isDarkOak) {
            // El roble oscuro exige forzosamente 4 retoños en un cuadrado de 2x2 para crecer:
            // 1. Buscar si ya existe un grupo de 2x2 de roble oscuro incompleto para completar sus esquinas
            BlockPos incompleteCorner = this.findIncompleteDarkOakClusterSpot(level, center, radius);
            if (incompleteCorner != null) {
                return incompleteCorner;
            }
            // 2. Si no hay grupo incompleto, buscar un nuevo cuadrado de 2x2 despejado con separación de 4 bloques
            return this.findNewDarkOak2x2Plot(level, center, radius);
        }

        // Retoños normales (Roble, Abedul, Abeto, etc.):
        // Requieren al menos 4 bloques de separación entre sí para que sus copas crezcan adecuadamente
        return this.findRegularSaplingSpot(level, center, radius);
    }

    private @Nullable BlockPos findIncompleteDarkOakClusterSpot(Level level, BlockPos center, int radius) {
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int cx = center.getX() + dx;
                int cz = center.getZ() + dz;
                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, cx, cz);

                mpos.set(cx, y, cz);
                if (level.getBlockState(mpos).is(Blocks.DARK_OAK_SAPLING)) {
                    // Evaluar los 4 posibles cuadrados de 2x2 que contienen a este retoño
                    int[][] offsets = { {0, 0}, {-1, 0}, {0, -1}, {-1, -1} };
                    for (int[] base : offsets) {
                        int bx = cx + base[0];
                        int bz = cz + base[1];
                        // Comprobar las 4 esquinas de este cuadrado 2x2
                        BlockPos missingSpot = null;
                        int darkOakCount = 0;
                        boolean validCluster = true;

                        for (int ox = 0; ox <= 1; ox++) {
                            for (int oz = 0; oz <= 1; oz++) {
                                int px = bx + ox;
                                int pz = bz + oz;
                                int py = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, px, pz);
                                BlockPos corner = new BlockPos(px, py, pz);
                                BlockState state = level.getBlockState(corner);

                                if (state.is(Blocks.DARK_OAK_SAPLING)) {
                                    darkOakCount++;
                                } else if ((state.isAir() || state.canBeReplaced()) && level.getBlockState(corner.below()).is(BlockTags.DIRT)) {
                                    if (missingSpot == null) {
                                        missingSpot = corner;
                                    }
                                } else {
                                    validCluster = false;
                                    break;
                                }
                            }
                            if (!validCluster) break;
                        }

                        if (validCluster && darkOakCount >= 1 && darkOakCount < 4 && missingSpot != null) {
                            return missingSpot;
                        }
                    }
                }
            }
        }
        return null;
    }

    private @Nullable BlockPos findNewDarkOak2x2Plot(Level level, BlockPos center, int radius) {
        for (int dx = -radius; dx <= radius; dx += 2) {
            for (int dz = -radius; dz <= radius; dz += 2) {
                int bx = center.getX() + dx;
                int bz = center.getZ() + dz;
                if (this.kobold.isPosInsideAnyOccupiedPlot(bx, bz)) continue;

                // Comprobar que las 4 esquinas del cuadrado de 2x2 tengan tierra y aire
                boolean valid2x2 = true;
                BlockPos firstCorner = null;

                for (int ox = 0; ox <= 1; ox++) {
                    for (int oz = 0; oz <= 1; oz++) {
                        int px = bx + ox;
                        int pz = bz + oz;
                        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, px, pz);
                        BlockPos corner = new BlockPos(px, y, pz);
                        if (!level.getBlockState(corner.below()).is(BlockTags.DIRT)
                                || (!level.getBlockState(corner).isAir() && !level.getBlockState(corner).canBeReplaced())) {
                            valid2x2 = false;
                            break;
                        }
                        if (firstCorner == null) firstCorner = corner;
                    }
                    if (!valid2x2) break;
                }

                if (!valid2x2 || firstCorner == null) continue;

                // Separación mínima de 4 bloques desde el perímetro de este 2x2 hacia cualquier otro árbol o retoño
                if (this.hasObstacleWithinClearance(level, bx, bz, 2, 4)) {
                    continue;
                }

                return firstCorner;
            }
        }
        return null;
    }

    private @Nullable BlockPos findRegularSaplingSpot(Level level, BlockPos center, int radius) {
        for (int dx = -radius; dx <= radius; dx += 2) {
            for (int dz = -radius; dz <= radius; dz += 2) {
                int cx = center.getX() + dx;
                int cz = center.getZ() + dz;
                if (this.kobold.isPosInsideAnyOccupiedPlot(cx, cz)) continue;

                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, cx, cz);
                BlockPos ground = new BlockPos(cx, y, cz);
                while (ground.getY() > level.getMinY() && (level.getBlockState(ground).isAir() || level.getBlockState(ground).canBeReplaced())) {
                    ground = ground.below();
                }

                if (level.getBlockState(ground).is(BlockTags.DIRT)) {
                    BlockPos plantPos = ground.above();
                    if (!level.getBlockState(plantPos).isAir() && !level.getBlockState(plantPos).canBeReplaced()) {
                        continue;
                    }

                    // Separación estricta de 4 bloques en todas las direcciones hacia otros retoños o troncos
                    if (!this.hasObstacleWithinClearance(level, plantPos.getX(), plantPos.getZ(), 1, 4)) {
                        return plantPos;
                    }
                }
            }
        }
        return null;
    }

    private boolean hasObstacleWithinClearance(Level level, int baseX, int baseZ, int plotSize, int clearance) {
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        int minX = baseX - clearance;
        int maxX = baseX + (plotSize - 1) + clearance;
        int minZ = baseZ - clearance;
        int maxZ = baseZ + (plotSize - 1) + clearance;

        for (int cx = minX; cx <= maxX; cx++) {
            for (int cz = minZ; cz <= maxZ; cz++) {
                // Si el punto cae dentro del propio lote que se está plantando, ignorar
                if (cx >= baseX && cx < baseX + plotSize && cz >= baseZ && cz < baseZ + plotSize) {
                    continue;
                }
                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, cx, cz);
                for (int dy = -2; dy <= 4; dy++) {
                    mpos.set(cx, y + dy, cz);
                    BlockState st = level.getBlockState(mpos);
                    if (st.getBlock() instanceof SaplingBlock || st.is(BlockTags.LOGS)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private @Nullable BlockPos findNearbyHarvestableTree(Level level, BlockPos center, int radius) {
        // Silvicultura sostenible: solo talar si hay árboles disponibles (se reforesta de inmediato tras talar)
        if (countNearbyAdultTrees(level, center, 32) <= 0) {
            return null;
        }

        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        BlockPos closest = null;
        double closestDist = Double.MAX_VALUE;

        for (int dx = -radius; dx <= radius; dx += 3) {
            for (int dz = -radius; dz <= radius; dz += 3) {
                int cx = center.getX() + dx;
                int cz = center.getZ() + dz;
                if (this.kobold.isPosInsideAnyOccupiedPlot(cx, cz)) continue;

                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, cx, cz);
                for (int dy = 0; dy <= 6; dy++) {
                    mpos.set(cx, y - dy, cz);
                    if (level.getBlockState(mpos).is(BlockTags.LOGS)) {
                        while (mpos.getY() > level.getMinY() && level.getBlockState(mpos.below()).is(BlockTags.LOGS)) {
                            mpos.move(Direction.DOWN);
                        }
                        double d = center.distSqr(mpos);
                        if (d < closestDist) {
                            closestDist = d;
                            closest = mpos.immutable();
                        }
                        break;
                    }
                }
            }
        }
        return closest;
    }

    private int countNearbyAdultTrees(Level level, BlockPos center, int radius) {
        int count = 0;
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx += 6) {
            for (int dz = -radius; dz <= radius; dz += 6) {
                int cx = center.getX() + dx;
                int cz = center.getZ() + dz;
                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, cx, cz);
                for (int dy = 0; dy <= 5; dy++) {
                    mpos.set(cx, y - dy, cz);
                    if (level.getBlockState(mpos).is(BlockTags.LOGS)) {
                        count++;
                        break;
                    }
                }
            }
        }
        return count;
    }
}
