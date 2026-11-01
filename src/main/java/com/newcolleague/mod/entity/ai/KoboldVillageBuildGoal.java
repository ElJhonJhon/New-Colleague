package com.newcolleague.mod.entity.ai;

import com.newcolleague.mod.entity.KoboldGirlEntity;
import com.newcolleague.mod.entity.AbstractTribalEntity.CommandStance;
import com.newcolleague.mod.entity.AbstractTribalEntity.KoboldProfession;
import com.newcolleague.mod.village.NbtStructureLoader;
import com.newcolleague.mod.village.VillageProject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

/**
 * Meta: Aplanar y nivelar terreno, buscar recursos en cofres o naturaleza, y construir estructuras
 */
public class KoboldVillageBuildGoal extends Goal {
    private final KoboldGirlEntity kobold;
    private final double speedModifier;

    public enum BuildPhase {
        TERRAFORMING_CLEAR,
        TERRAFORMING_FILL,
        FETCHING_FROM_CHEST,
        GATHERING_FROM_NATURE,
        BUILDING,
        WAITING_FOR_MATERIALS
    }

    private BuildPhase currentPhase = BuildPhase.BUILDING;
    private @Nullable BlockPos targetPos;
    private @Nullable BlockPos chestTarget;
    private @Nullable BlockPos gatherTarget;
    private @Nullable Item itemToFetch;
    private @Nullable Item missingMaterialItem;
    private int workTicks = 0;
    private int searchCooldown = 0;
    private int waitingTicks = 0;
    private int stuckTicks = 0;

    public KoboldVillageBuildGoal(KoboldGirlEntity kobold, double speedModifier) {
        this.kobold = kobold;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.kobold.isSleeping() || this.kobold.isThreatened() || this.kobold.isInQueenDuel() || this.kobold.isInPlayerDuel() || this.kobold.isSpectatingDuel()) {
            return false;
        }
        if (this.kobold.isOwned() && this.kobold.getCommandStance() != CommandStance.WORK) {
            return false;
        }
        // Exclusivo para la profesión de Constructora dentro de su horario laboral diurno
        if (this.kobold.getProfession() != KoboldProfession.BUILDER) {
            return false;
        }
        if (!this.kobold.isWorkScheduleTime() || this.kobold.isSleepTime()) {
            return false;
        }

        if (--this.searchCooldown > 0) return false;
        this.searchCooldown = 20 + this.kobold.getRandom().nextInt(20);

        VillageProject project = this.getProject();
        if (project == null || project.isCompleted()) return false;

        return this.findTarget(project);
    }

    private @Nullable VillageProject getProject() {
        if (this.kobold.isAnyQueen()) {
            return this.kobold.getActiveVillageProject();
        }
        KoboldGirlEntity queen = this.kobold.getTribeQueen();
        if (queen != null && queen.isAlive()) {
            return queen.getActiveVillageProject();
        }
        return null;
    }

    private boolean findTarget(VillageProject project) {
        Level level = this.kobold.level();

        // 1. Fase de Nivelado / Terraforming
        if (!project.isTerraformed()) {
            BlockPos terraformPos = project.findNextTerraformTarget(level);
            if (terraformPos != null) {
                BlockState state = level.getBlockState(terraformPos);
                if (VillageProject.isClearableNaturalBlock(state)) {
                    this.targetPos = terraformPos;
                    this.currentPhase = BuildPhase.TERRAFORMING_CLEAR;
                    return true;
                } else if (VillageProject.isHoleNeedingFill(state)) {
                    if (this.hasItem(Items.DIRT)) {
                        this.targetPos = terraformPos;
                        this.currentPhase = BuildPhase.TERRAFORMING_FILL;
                        return true;
                    }

                    BlockPos chest = this.findNearbyChestWithItem(level, Items.DIRT);
                    if (chest != null) {
                        this.chestTarget = chest;
                        this.itemToFetch = Items.DIRT;
                        this.targetPos = chest;
                        this.currentPhase = BuildPhase.FETCHING_FROM_CHEST;
                        return true;
                    }

                    BlockPos dirtSource = this.findNearbyNaturalDirt(level, this.kobold.blockPosition(), 16);
                    if (dirtSource != null) {
                        this.gatherTarget = dirtSource;
                        this.targetPos = dirtSource;
                        this.currentPhase = BuildPhase.GATHERING_FROM_NATURE;
                        return true;
                    }

                    this.missingMaterialItem = Items.DIRT;
                    this.currentPhase = BuildPhase.WAITING_FOR_MATERIALS;
                    this.targetPos = project.getOrigin().offset(2, 1, 2);
                    return true;
                }
            } else {
                project.setTerraformed(true);
            }
        }

        // 2. Fase de Construcción de Bloques del Blueprint
        // Fast-forward si los bloques del blueprint ya están colocados físicamente en el mundo
        while (!project.isCompleted()) {
            NbtStructureLoader.BlueprintEntry current = project.getCurrentEntry();
            if (current == null) break;
            BlockPos checkPos = project.getWorldPosForEntry(current);
            if (matchesBlueprint(level.getBlockState(checkPos), current.state())) {
                project.advanceBuild();
            } else {
                break;
            }
        }

        if (project.isCompleted()) {
            KoboldGirlEntity queen = this.kobold.isAnyQueen() ? this.kobold : this.kobold.getTribeQueen();
            if (queen != null) {
                queen.onStructureCompleted(project);
            }
            return false;
        }

        NbtStructureLoader.BlueprintEntry entry = project.getCurrentEntry();
        if (entry != null) {
            BlockPos worldPos = project.getWorldPosForEntry(entry);
            Item required = NbtStructureLoader.getRequiredItemForBlock(entry.state());

            if (required == Items.AIR) {
                this.targetPos = worldPos;
                this.currentPhase = BuildPhase.BUILDING;
                return true;
            }

            // 2.A: ¿Tiene el ítem en su inventario o puede craftearlo?
            if (this.hasOrCanCraft(required)) {
                this.targetPos = worldPos;
                this.currentPhase = BuildPhase.BUILDING;
                this.missingMaterialItem = null;
                KoboldGirlEntity queen = this.kobold.isAnyQueen() ? this.kobold : this.kobold.getTribeQueen();
                if (queen != null) queen.setVillageProjectMissingItem(null);
                return true;
            }

            // 2.B: Buscar en cofres comunales cercanos (radio ampliado a toda la aldea)
            BlockPos chest = this.findNearbyChestWithItem(level, required);
            if (chest != null) {
                this.chestTarget = chest;
                this.itemToFetch = required;
                this.targetPos = chest;
                this.currentPhase = BuildPhase.FETCHING_FROM_CHEST;
                return true;
            }

            // 2.C: Recolectar directamente de la naturaleza si es madera, piedra, tierra o semillas
            if (isWoodProduct(required)) {
                BlockPos treePos = this.findNearbyTreeLog(level, this.kobold.blockPosition(), 24);
                if (treePos != null) {
                    this.gatherTarget = treePos;
                    this.targetPos = treePos;
                    this.currentPhase = BuildPhase.GATHERING_FROM_NATURE;
                    return true;
                }
            }

            if (required == Items.COBBLESTONE) {
                BlockPos stonePos = this.findNearbyStone(level, this.kobold.blockPosition(), 20);
                if (stonePos != null) {
                    this.gatherTarget = stonePos;
                    this.targetPos = stonePos;
                    this.currentPhase = BuildPhase.GATHERING_FROM_NATURE;
                    return true;
                }
            }

            if (required == Items.DIRT) {
                BlockPos dirtPos = this.findNearbyNaturalDirt(level, this.kobold.blockPosition(), 16);
                if (dirtPos != null) {
                    this.gatherTarget = dirtPos;
                    this.targetPos = dirtPos;
                    this.currentPhase = BuildPhase.GATHERING_FROM_NATURE;
                    return true;
                }
            }

            if (required == Items.WHEAT_SEEDS) {
                BlockPos grassPos = this.findNearbyTallGrass(level, this.kobold.blockPosition(), 20);
                if (grassPos != null) {
                    this.gatherTarget = grassPos;
                    this.targetPos = grassPos;
                    this.currentPhase = BuildPhase.GATHERING_FROM_NATURE;
                    return true;
                }
            }

            if (required == Items.TORCH) {
                BlockPos treePos = this.findNearbyTreeLog(level, this.kobold.blockPosition(), 24);
                if (treePos != null) {
                    this.gatherTarget = treePos;
                    this.targetPos = treePos;
                    this.currentPhase = BuildPhase.GATHERING_FROM_NATURE;
                    return true;
                }
            }

            if (isWoolProduct(required) || isBedProduct(required)) {
                BlockPos grassPos = this.findNearbyTallGrass(level, this.kobold.blockPosition(), 24);
                if (grassPos != null) {
                    this.gatherTarget = grassPos;
                    this.targetPos = grassPos;
                    this.currentPhase = BuildPhase.GATHERING_FROM_NATURE;
                    return true;
                }
                BlockPos treePos = this.findNearbyTreeLog(level, this.kobold.blockPosition(), 24);
                if (treePos != null) {
                    this.gatherTarget = treePos;
                    this.targetPos = treePos;
                    this.currentPhase = BuildPhase.GATHERING_FROM_NATURE;
                    return true;
                }
            }

            // 2.D: El ítem no está disponible ni se puede recolectar a mano
            this.missingMaterialItem = required;
            this.currentPhase = BuildPhase.WAITING_FOR_MATERIALS;
            this.targetPos = project.getOrigin().offset(2, 1, 2);
            KoboldGirlEntity queen = this.kobold.isAnyQueen() ? this.kobold : this.kobold.getTribeQueen();
            if (queen != null) {
                queen.setVillageProjectMissingItem(required);
            }
            return true;
        }

        return false;
    }

    public static boolean matchesBlueprint(BlockState worldState, BlockState blueprintState) {
        if (blueprintState == null) return true;
        if (worldState == null || worldState.isAir()) {
            return blueprintState.isAir();
        }
        if (worldState.is(blueprintState.getBlock())) {
            if (blueprintState.getBlock() instanceof BedBlock && blueprintState.hasProperty(BedBlock.PART)) {
                return worldState.hasProperty(BedBlock.PART) && worldState.getValue(BedBlock.PART) == blueprintState.getValue(BedBlock.PART);
            }
            if (blueprintState.hasProperty(SlabBlock.TYPE)) {
                return worldState.hasProperty(SlabBlock.TYPE) && worldState.getValue(SlabBlock.TYPE) == blueprintState.getValue(SlabBlock.TYPE);
            }
            return true;
        }
        if (blueprintState.is(BlockTags.WOOL) && worldState.is(BlockTags.WOOL)) {
            return true;
        }
        if (blueprintState.getBlock() instanceof BedBlock && worldState.getBlock() instanceof BedBlock) {
            if (blueprintState.hasProperty(BedBlock.PART) && worldState.hasProperty(BedBlock.PART)) {
                return worldState.getValue(BedBlock.PART) == blueprintState.getValue(BedBlock.PART);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.kobold.isSleeping() || this.kobold.isThreatened()) return false;
        if (this.kobold.isOwned() && this.kobold.getCommandStance() != CommandStance.WORK) return false;
        VillageProject project = this.getProject();
        return project != null && !project.isCompleted() && this.targetPos != null;
    }

    @Override
    public void start() {
        this.workTicks = 0;
        this.stuckTicks = 0;
        if (this.targetPos != null) {
            this.navigateTowardsTarget(this.targetPos);
        }
    }

    @Override
    public void stop() {
        this.targetPos = null;
        this.chestTarget = null;
        this.gatherTarget = null;
        this.workTicks = 0;
        this.waitingTicks = 0;
        this.stuckTicks = 0;
        this.kobold.getNavigation().stop();
    }

    @Override
    public void tick() {
        VillageProject project = this.getProject();
        if (project == null || project.isCompleted() || this.targetPos == null) return;

        Level level = this.kobold.level();
        if (!(level instanceof ServerLevel sl)) return;

        // 1. Manejo de fase WAITING_FOR_MATERIALS
        if (this.currentPhase == BuildPhase.WAITING_FOR_MATERIALS) {
            this.waitingTicks++;
            this.kobold.getLookControl().setLookAt(project.getOrigin().getX() + 2.5D, project.getOrigin().getY() + 1.0D, project.getOrigin().getZ() + 2.5D, 30.0F, 30.0F);

            if (this.waitingTicks % 40 == 0) {
                sl.sendParticles(ParticleTypes.SMOKE, this.kobold.getX(), this.kobold.getY() + 1.8D, this.kobold.getZ(), 3, 0.15D, 0.15D, 0.15D, 0.02D);
            }

            if (this.waitingTicks % 60 == 0) {
                if (this.missingMaterialItem != null) {
                    BlockPos foundChest = this.findNearbyChestWithItem(level, this.missingMaterialItem);
                    if (foundChest != null || this.hasOrCanCraft(this.missingMaterialItem)) {
                        this.waitingTicks = 0;
                        this.findTarget(project);
                        if (this.targetPos != null) {
                            this.navigateTowardsTarget(this.targetPos);
                        }
                    }
                }
            }
            return;
        }

        // 2. Acercamiento al objetivo
        double dx = this.kobold.getX() - (this.targetPos.getX() + 0.5D);
        double dz = this.kobold.getZ() - (this.targetPos.getZ() + 0.5D);
        double dy = this.kobold.getY() - (this.targetPos.getY() + 0.5D);
        double horizontalDistSq = dx * dx + dz * dz;
        double distSq = horizontalDistSq + dy * dy;
        this.kobold.getLookControl().setLookAt(this.targetPos.getX() + 0.5D, this.targetPos.getY() + 0.5D, this.targetPos.getZ() + 0.5D, 30.0F, 30.0F);

        boolean withinReach;
        if (this.currentPhase == BuildPhase.FETCHING_FROM_CHEST) {
            withinReach = distSq <= 9.0D;
        } else {
            // Alcance vertical ampliado a 8 bloques para techos y torres, tolerancia horizontal y rescate de atasco
            withinReach = (horizontalDistSq <= 16.0D && Math.abs(dy) <= 8.0D) || distSq <= 36.0D || this.stuckTicks > 80;
        }

        if (!withinReach) {
            this.stuckTicks++;
            if (this.kobold.getNavigation().isDone() || this.kobold.tickCount % 20 == 0) {
                this.navigateTowardsTarget(this.targetPos);
            }
            return;
        }

        this.stuckTicks = 0;
        this.kobold.getNavigation().stop();
        this.workTicks++;

        if (this.workTicks % 6 == 0) {
            this.kobold.swing(InteractionHand.MAIN_HAND);
        }

        switch (this.currentPhase) {
            case FETCHING_FROM_CHEST -> {
                if (this.workTicks >= 15) {
                    this.workTicks = 0;
                    if (this.chestTarget != null && this.itemToFetch != null) {
                        sl.playSound(null, this.chestTarget, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.8F, 1.0F);
                        boolean retrieved = this.extractMaterialFromChest(this.chestTarget, this.itemToFetch);
                        sl.playSound(null, this.chestTarget, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.8F, 1.0F);
                        if (retrieved) {
                            sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.kobold.getX(), this.kobold.getY() + 1.5D, this.kobold.getZ(), 5, 0.3D, 0.3D, 0.3D, 0.05D);
                        }
                    }
                    this.findTarget(project);
                    if (this.targetPos != null) {
                        this.navigateTowardsTarget(this.targetPos);
                    }
                }
            }
            case GATHERING_FROM_NATURE -> {
                if (this.workTicks >= 20) {
                    this.workTicks = 0;
                    if (this.gatherTarget != null) {
                        BlockState state = level.getBlockState(this.gatherTarget);
                        sl.levelEvent(2001, this.gatherTarget, Block.getId(state));
                        sl.destroyBlock(this.gatherTarget, false);
                        if (state.is(BlockTags.LOGS)) {
                            this.kobold.getInventory().addItem(new ItemStack(Items.OAK_LOG));
                            this.kobold.playSound(SoundEvents.WOOD_BREAK, 1.0F, 1.0F);

                            // Silvicultura Sostenible: replantar brote inmediatamente si el tocón tocaba tierra o césped
                            BlockPos belowPos = this.gatherTarget.below();
                            BlockState belowState = sl.getBlockState(belowPos);
                            if (belowState.is(Blocks.DIRT) || belowState.is(Blocks.GRASS_BLOCK) || belowState.is(Blocks.COARSE_DIRT) || belowState.is(Blocks.PODZOL)) {
                                sl.setBlock(this.gatherTarget, Blocks.OAK_SAPLING.defaultBlockState(), 3);
                                this.kobold.playSound(SoundEvents.GRASS_PLACE, 1.0F, 1.0F);
                            }
                        } else if (state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(Blocks.COBBLESTONE) || state.is(Blocks.GRAVEL)) {
                            this.kobold.getInventory().addItem(new ItemStack(Items.COBBLESTONE));
                            this.kobold.playSound(SoundEvents.STONE_BREAK, 1.0F, 1.0F);
                        } else if (state.is(BlockTags.DIRT)) {
                            this.kobold.getInventory().addItem(new ItemStack(Items.DIRT));
                            this.kobold.playSound(SoundEvents.GRASS_BREAK, 1.0F, 1.0F);
                        } else if (state.is(Blocks.SHORT_GRASS) || state.is(Blocks.TALL_GRASS) || state.is(Blocks.FERN)) {
                            this.kobold.getInventory().addItem(new ItemStack(Items.WHEAT_SEEDS));
                            this.kobold.playSound(SoundEvents.GRASS_BREAK, 1.0F, 1.0F);
                        }
                        sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.kobold.getX(), this.kobold.getY() + 1.2D, this.kobold.getZ(), 4, 0.2D, 0.2D, 0.2D, 0.05D);
                    }
                    this.findTarget(project);
                    if (this.targetPos != null) {
                        this.navigateTowardsTarget(this.targetPos);
                    }
                }
            }
            case TERRAFORMING_CLEAR -> {
                if (this.workTicks >= 16) {
                    this.workTicks = 0;
                    BlockState state = level.getBlockState(this.targetPos);
                    if (VillageProject.isClearableNaturalBlock(state)) {
                        sl.levelEvent(2001, this.targetPos, Block.getId(state));
                        sl.destroyBlock(this.targetPos, false);
                        if (state.is(BlockTags.DIRT)) {
                            this.kobold.getInventory().addItem(new ItemStack(Items.DIRT));
                            this.kobold.playSound(SoundEvents.GRASS_BREAK, 1.0F, 1.0F);
                        } else if (state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(Blocks.COBBLESTONE) || state.is(Blocks.GRAVEL)) {
                            this.kobold.getInventory().addItem(new ItemStack(Items.COBBLESTONE));
                            this.kobold.playSound(SoundEvents.STONE_BREAK, 1.0F, 1.0F);
                        } else if (state.is(BlockTags.LOGS)) {
                            this.kobold.getInventory().addItem(new ItemStack(Items.OAK_LOG));
                            this.kobold.playSound(SoundEvents.WOOD_BREAK, 1.0F, 1.0F);
                        } else {
                            this.kobold.playSound(SoundEvents.GRASS_BREAK, 1.0F, 1.0F);
                        }
                    }
                    this.findTarget(project);
                    if (this.targetPos != null) {
                        this.navigateTowardsTarget(this.targetPos);
                    }
                }
            }
            case TERRAFORMING_FILL -> {
                if (this.workTicks >= 16) {
                    this.workTicks = 0;
                    BlockState state = level.getBlockState(this.targetPos);
                    if (VillageProject.isHoleNeedingFill(state)) {
                        if (!this.clearEntitiesInBlock(sl, this.targetPos)) {
                            return;
                        }
                        if (this.consumeItem(Items.DIRT, 1)) {
                            sl.setBlock(this.targetPos, Blocks.DIRT.defaultBlockState(), 3);
                            this.kobold.playSound(SoundEvents.GRASS_PLACE, 1.0F, 1.0F);
                            sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.targetPos.getX() + 0.5D, this.targetPos.getY() + 1.1D, this.targetPos.getZ() + 0.5D, 3, 0.2D, 0.2D, 0.2D, 0.05D);
                        }
                    }
                    this.findTarget(project);
                    if (this.targetPos != null) {
                        this.navigateTowardsTarget(this.targetPos);
                    }
                }
            }
            case BUILDING -> {
                if (this.workTicks >= 18) {
                    this.workTicks = 0;
                    NbtStructureLoader.BlueprintEntry entry = project.getCurrentEntry();
                    if (entry != null) {
                        Item required = NbtStructureLoader.getRequiredItemForBlock(entry.state());
                        boolean canPlace = (required == Items.AIR) || this.consumeOrCraftItem(required);
                        if (canPlace) {
                            BlockPos worldPos = project.getWorldPosForEntry(entry);

                            // Protección contra asfixia: verificar que no haya entidades ocupando el bloque a colocar
                            if (entry.state().blocksMotion() || entry.isBed()) {
                                if (!this.clearEntitiesInBlock(sl, worldPos)) {
                                    return;
                                }
                                if (entry.isBed() && entry.state().getBlock() instanceof BedBlock) {
                                    BedPart part = entry.state().getValue(BedBlock.PART);
                                    Direction facing = entry.state().getValue(BedBlock.FACING);
                                    BlockPos otherPos = (part == BedPart.FOOT) ? worldPos.relative(facing) : worldPos.relative(facing.getOpposite());
                                    if (!this.clearEntitiesInBlock(sl, otherPos)) {
                                        return;
                                    }
                                }
                            }

                            // Colocación atómica para camas: previene que la cama se destruya por orfandad
                            if (entry.isBed() && entry.state().getBlock() instanceof BedBlock) {
                                BedPart part = entry.state().getValue(BedBlock.PART);
                                Direction facing = entry.state().getValue(BedBlock.FACING);
                                BlockPos otherPos = (part == BedPart.FOOT) ? worldPos.relative(facing) : worldPos.relative(facing.getOpposite());
                                BedPart otherPart = (part == BedPart.FOOT) ? BedPart.HEAD : BedPart.FOOT;
                                BlockState otherState = entry.state().setValue(BedBlock.PART, otherPart);

                                sl.setBlock(worldPos, entry.state(), 2);
                                sl.setBlock(otherPos, otherState, 3);
                                sl.sendBlockUpdated(worldPos, Blocks.AIR.defaultBlockState(), entry.state(), 3);
                            } else {
                                sl.setBlock(worldPos, entry.state(), 3);
                            }

                            if (entry.isBed() || entry.isChest()) {
                                this.kobold.playSound(SoundEvents.WOOD_PLACE, 1.0F, 1.0F);
                            } else if (entry.state().is(BlockTags.WOOL)) {
                                this.kobold.playSound(SoundEvents.WOOL_PLACE, 1.0F, 1.0F);
                            } else if (entry.state().is(Blocks.LANTERN) || entry.state().is(Blocks.TORCH)) {
                                this.kobold.playSound(SoundEvents.LANTERN_PLACE, 1.0F, 1.0F);
                            } else if (entry.state().is(BlockTags.LOGS) || entry.state().is(BlockTags.PLANKS)) {
                                this.kobold.playSound(SoundEvents.WOOD_PLACE, 1.0F, 1.0F);
                            } else {
                                this.kobold.playSound(SoundEvents.STONE_PLACE, 1.0F, 1.0F);
                            }

                            sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, worldPos.getX() + 0.5D, worldPos.getY() + 1.1D, worldPos.getZ() + 0.5D, 4, 0.2D, 0.2D, 0.2D, 0.05D);
                            project.advanceBuild();
                        }
                    }

                    if (project.isCompleted()) {
                        KoboldGirlEntity queen = this.kobold.isAnyQueen() ? this.kobold : this.kobold.getTribeQueen();
                        if (queen != null) {
                            queen.onStructureCompleted(project);
                        }
                        this.targetPos = null;
                    } else {
                        this.findTarget(project);
                        if (this.targetPos != null) {
                            this.navigateTowardsTarget(this.targetPos);
                        }
                    }
                }
            }
        }
    }

    private boolean hasItem(Item item) {
        for (int i = 0; i < this.kobold.getInventory().getContainerSize(); i++) {
            ItemStack s = this.kobold.getInventory().getItem(i);
            if (!s.isEmpty() && s.is(item) && s.getCount() > 0) return true;
        }
        return false;
    }

    private int countItem(Item item) {
        int count = 0;
        for (int i = 0; i < this.kobold.getInventory().getContainerSize(); i++) {
            ItemStack s = this.kobold.getInventory().getItem(i);
            if (!s.isEmpty() && s.is(item)) count += s.getCount();
        }
        return count;
    }

    private boolean consumeItem(Item item, int amount) {
        int needed = amount;
        for (int i = 0; i < this.kobold.getInventory().getContainerSize(); i++) {
            ItemStack s = this.kobold.getInventory().getItem(i);
            if (!s.isEmpty() && s.is(item)) {
                if (s.getCount() >= needed) {
                    s.shrink(needed);
                    return true;
                } else {
                    needed -= s.getCount();
                    this.kobold.getInventory().setItem(i, ItemStack.EMPTY);
                }
            }
        }
        return needed <= 0;
    }

    private boolean hasAnyWool() {
        for (int i = 0; i < this.kobold.getInventory().getContainerSize(); i++) {
            ItemStack s = this.kobold.getInventory().getItem(i);
            if (!s.isEmpty() && s.is(ItemTags.WOOL) && s.getCount() > 0) return true;
        }
        return false;
    }

    private boolean consumeAnyWool(int amount) {
        int needed = amount;
        for (int i = 0; i < this.kobold.getInventory().getContainerSize(); i++) {
            ItemStack s = this.kobold.getInventory().getItem(i);
            if (!s.isEmpty() && s.is(ItemTags.WOOL)) {
                if (s.getCount() >= needed) {
                    s.shrink(needed);
                    return true;
                } else {
                    needed -= s.getCount();
                    this.kobold.getInventory().setItem(i, ItemStack.EMPTY);
                }
            }
        }
        return needed <= 0;
    }

    private boolean hasAnyBed() {
        for (int i = 0; i < this.kobold.getInventory().getContainerSize(); i++) {
            ItemStack s = this.kobold.getInventory().getItem(i);
            if (!s.isEmpty() && s.is(ItemTags.BEDS) && s.getCount() > 0) return true;
        }
        return false;
    }

    private boolean consumeAnyBed() {
        for (int i = 0; i < this.kobold.getInventory().getContainerSize(); i++) {
            ItemStack s = this.kobold.getInventory().getItem(i);
            if (!s.isEmpty() && s.is(ItemTags.BEDS)) {
                s.shrink(1);
                return true;
            }
        }
        return false;
    }

    private boolean hasOrCanCraft(Item item) {
        if (this.hasItem(item)) return true;
        if (item == Items.OAK_PLANKS) return this.hasItem(Items.OAK_LOG);
        if (item == Items.OAK_SLAB) return this.hasItem(Items.OAK_PLANKS) || this.hasItem(Items.OAK_LOG);
        if (item == Items.OAK_FENCE || item == Items.OAK_FENCE_GATE) return this.hasItem(Items.OAK_PLANKS) || this.hasItem(Items.OAK_LOG);
        if (item == Items.CRAFTING_TABLE) return this.countItem(Items.OAK_PLANKS) >= 4 || this.hasItem(Items.OAK_LOG);
        if (item == Items.CHEST || item == Items.BARREL) return this.countItem(Items.OAK_PLANKS) >= 8 || this.countItem(Items.OAK_LOG) >= 2;
        if (item == Items.CAMPFIRE) return this.hasItem(Items.OAK_LOG);
        if (item == Items.TORCH) return this.hasItem(Items.TORCH) || this.hasItem(Items.COAL) || this.hasItem(Items.CHARCOAL) || this.hasItem(Items.OAK_LOG) || this.hasItem(Items.OAK_PLANKS) || this.hasItem(Items.STICK);
        if (item == Items.LADDER) return this.hasItem(Items.LADDER) || this.hasItem(Items.OAK_PLANKS) || this.hasItem(Items.OAK_LOG) || this.hasItem(Items.STICK);
        if (isWoolProduct(item)) return this.hasAnyWool() || this.hasItem(Items.STRING) || this.countItem(Items.WHEAT_SEEDS) >= 2 || this.hasItem(Items.OAK_LOG);
        if (isBedProduct(item)) return this.hasAnyBed() || ((this.hasAnyWool() || this.hasItem(Items.STRING) || this.countItem(Items.WHEAT_SEEDS) >= 2) && (this.hasItem(Items.OAK_PLANKS) || this.hasItem(Items.OAK_LOG))) || this.countItem(Items.OAK_LOG) >= 2;
        return false;
    }

    private boolean consumeOrCraftItem(Item item) {
        if (this.hasItem(item)) {
            return this.consumeItem(item, 1);
        }
        if (item == Items.OAK_PLANKS && this.consumeItem(Items.OAK_LOG, 1)) {
            this.kobold.getInventory().addItem(new ItemStack(Items.OAK_PLANKS, 3));
            return true;
        }
        if (item == Items.OAK_SLAB) {
            if (this.consumeItem(Items.OAK_PLANKS, 1)) {
                this.kobold.getInventory().addItem(new ItemStack(Items.OAK_SLAB, 1));
                return true;
            } else if (this.consumeItem(Items.OAK_LOG, 1)) {
                this.kobold.getInventory().addItem(new ItemStack(Items.OAK_SLAB, 5));
                return true;
            }
        }
        if (item == Items.OAK_FENCE || item == Items.OAK_FENCE_GATE) {
            if (this.consumeItem(Items.OAK_PLANKS, 2)) {
                return true;
            } else if (this.consumeItem(Items.OAK_LOG, 1)) {
                this.kobold.getInventory().addItem(new ItemStack(Items.OAK_PLANKS, 2));
                return true;
            }
        }
        if (item == Items.CRAFTING_TABLE) {
            if (this.consumeItem(Items.OAK_PLANKS, 4)) return true;
            if (this.consumeItem(Items.OAK_LOG, 1)) return true;
        }
        if (item == Items.CHEST || item == Items.BARREL) {
            if (this.consumeItem(Items.OAK_PLANKS, 8)) return true;
            if (this.consumeItem(Items.OAK_LOG, 2)) return true;
        }
        if (item == Items.CAMPFIRE && this.consumeItem(Items.OAK_LOG, 1)) {
            return true;
        }
        if (item == Items.TORCH) {
            if (this.consumeItem(Items.COAL, 1) || this.consumeItem(Items.CHARCOAL, 1)) {
                this.kobold.getInventory().addItem(new ItemStack(Items.TORCH, 3));
                return true;
            } else if (this.consumeItem(Items.STICK, 1) || this.consumeItem(Items.OAK_PLANKS, 1) || this.consumeItem(Items.OAK_LOG, 1)) {
                this.kobold.getInventory().addItem(new ItemStack(Items.TORCH, 3));
                return true;
            }
        }
        if (item == Items.LADDER) {
            if (this.consumeItem(Items.STICK, 7) || this.consumeItem(Items.OAK_PLANKS, 1) || this.consumeItem(Items.OAK_LOG, 1)) {
                this.kobold.getInventory().addItem(new ItemStack(Items.LADDER, 2));
                return true;
            }
        }
        if (isWoolProduct(item)) {
            if (this.consumeAnyWool(1)) return true;
            if (this.consumeItem(Items.STRING, 4)) return true;
            if (this.consumeItem(Items.WHEAT_SEEDS, 2) || this.consumeItem(Items.OAK_LOG, 1)) return true;
        }
        if (isBedProduct(item)) {
            if (this.consumeAnyBed()) return true;
            if ((this.consumeAnyWool(3) || this.consumeItem(Items.STRING, 4) || this.consumeItem(Items.WHEAT_SEEDS, 4)) && (this.consumeItem(Items.OAK_PLANKS, 3) || this.consumeItem(Items.OAK_LOG, 1))) {
                return true;
            }
            if (this.consumeItem(Items.OAK_LOG, 2)) return true;
        }
        return false;
    }

    private static boolean isWoodProduct(Item item) {
        return item == Items.OAK_PLANKS || item == Items.OAK_SLAB || item == Items.OAK_FENCE
                || item == Items.OAK_FENCE_GATE || item == Items.CRAFTING_TABLE || item == Items.CHEST
                || item == Items.BARREL || item == Items.CAMPFIRE || item == Items.OAK_LOG || item == Items.LADDER;
    }

    private static boolean isWoolProduct(Item item) {
        return item == Blocks.WOOL.white().asItem() || item == Blocks.WOOL.red().asItem() || item == Blocks.WOOL.yellow().asItem();
    }

    private static boolean isBedProduct(Item item) {
        return item == Blocks.BED.white().asItem() || item == Blocks.BED.red().asItem();
    }

    private @Nullable BlockPos findNearbyChestWithItem(Level level, Item item) {
        List<BlockPos> chests = KoboldGirlEntity.findNearbyUniqueChests(level, this.kobold.blockPosition(), 48, 10);
        for (BlockPos pos : chests) {
            Container c = KoboldGirlEntity.getChestContainer(level, pos);
            if (c == null || c.isEmpty()) continue;
            for (int i = 0; i < c.getContainerSize(); i++) {
                ItemStack st = c.getItem(i);
                if (!st.isEmpty()) {
                    boolean matches = st.is(item)
                            || (isWoodProduct(item) && (st.is(ItemTags.LOGS) || st.is(Items.OAK_LOG)))
                            || (isWoolProduct(item) && (st.is(ItemTags.WOOL) || st.is(Items.STRING)))
                            || (isBedProduct(item) && st.is(ItemTags.BEDS))
                            || (item == Items.TORCH && (st.is(Items.TORCH) || st.is(Items.LANTERN) || st.is(Items.SOUL_TORCH) || st.is(Items.SOUL_LANTERN)))
                            || (item == Items.WHEAT_SEEDS && (st.is(Items.WHEAT_SEEDS) || st.is(Items.CARROT) || st.is(Items.POTATO)));
                    if (matches) {
                        return pos;
                    }
                }
            }
        }
        return null;
    }

    private boolean extractMaterialFromChest(BlockPos chestPos, Item itemToFetch) {
        Container container = KoboldGirlEntity.getChestContainer(this.kobold.level(), chestPos);
        if (container == null) return false;
        int taken = 0;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) continue;

            boolean matches = stack.is(itemToFetch);
            if (!matches && isWoodProduct(itemToFetch) && (stack.is(ItemTags.LOGS) || stack.is(Items.OAK_LOG))) {
                matches = true;
            }
            if (!matches && isWoolProduct(itemToFetch) && (stack.is(ItemTags.WOOL) || stack.is(Items.STRING))) {
                matches = true;
            }
            if (!matches && isBedProduct(itemToFetch) && stack.is(ItemTags.BEDS)) {
                matches = true;
            }
            if (!matches && itemToFetch == Items.TORCH && (stack.is(Items.TORCH) || stack.is(Items.LANTERN) || stack.is(Items.SOUL_TORCH) || stack.is(Items.SOUL_LANTERN))) {
                matches = true;
            }
            if (!matches && itemToFetch == Items.WHEAT_SEEDS && (stack.is(Items.WHEAT_SEEDS) || stack.is(Items.CARROT) || stack.is(Items.POTATO))) {
                matches = true;
            }

            if (matches) {
                int toExtract = Math.min(stack.getCount(), 16 - taken);
                ItemStack extracted = container.removeItem(i, toExtract);
                ItemStack leftover = this.kobold.getInventory().addItem(extracted);
                if (!leftover.isEmpty()) {
                    KoboldGirlEntity.addItemToContainer(container, leftover);
                } else {
                    taken += toExtract;
                }
                if (taken >= 16) break;
            }
        }
        container.setChanged();
        return taken > 0;
    }

    private boolean isPosInsideAnyOccupiedPlot(int x, int z) {
        KoboldGirlEntity queen = this.kobold.isAnyQueen() ? this.kobold : this.kobold.getTribeQueen();
        if (queen != null) {
            for (NbtStructureLoader.VillagePlot plot : queen.getOccupiedVillagePlots()) {
                if (plot.contains(x, z)) return true;
            }
        }
        return false;
    }

    private @Nullable BlockPos findNearbyTreeLog(Level level, BlockPos center, int radius) {
        // Regla de Silvicultura Sostenible: no talar si quedan <= 3 árboles en el perímetro de la aldea
        if (countNearbyAdultTrees(level, center, 32) <= 3) {
            return null;
        }

        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        BlockPos closest = null;
        double closestDist = Double.MAX_VALUE;
        for (int dy = -3; dy <= 8; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    mpos.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    BlockState state = level.getBlockState(mpos);
                    if (state.is(BlockTags.LOGS)) {
                        if (this.isPosInsideAnyOccupiedPlot(mpos.getX(), mpos.getZ())) continue;
                        double dist = center.distSqr(mpos);
                        if (dist < closestDist) {
                            closestDist = dist;
                            closest = mpos.immutable();
                        }
                    }
                }
            }
        }
        return closest;
    }

    private int countNearbyAdultTrees(Level level, BlockPos center, int radius) {
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        int trees = 0;
        for (int dx = -radius; dx <= radius; dx += 4) {
            for (int dz = -radius; dz <= radius; dz += 4) {
                int checkX = center.getX() + dx;
                int checkZ = center.getZ() + dz;
                int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, checkX, checkZ);
                // Escanear la columna hacia abajo bajo la copa de hojas buscando el tronco (LOGS)
                for (int y = surfaceY; y >= surfaceY - 12 && y > level.getMinY(); y--) {
                    mpos.set(checkX, y, checkZ);
                    BlockState st = level.getBlockState(mpos);
                    if (st.is(BlockTags.LOGS)) {
                        trees++;
                        if (trees > 4) return trees;
                        break;
                    }
                    if (st.is(BlockTags.LEAVES)) {
                        // Comprobar si el tronco central está a 1 bloque adyacente
                        boolean foundAdj = false;
                        for (Direction dir : Direction.Plane.HORIZONTAL) {
                            if (level.getBlockState(mpos.relative(dir)).is(BlockTags.LOGS)) {
                                trees++;
                                foundAdj = true;
                                break;
                            }
                        }
                        if (foundAdj) {
                            if (trees > 4) return trees;
                            break;
                        }
                    }
                    if (st.is(Blocks.DIRT) || st.is(Blocks.GRASS_BLOCK) || st.is(Blocks.STONE)) {
                        break; // Llegó al suelo sin encontrar tronco en esta columna
                    }
                }
            }
        }
        return trees;
    }

    private @Nullable BlockPos findNearbyStone(Level level, BlockPos center, int radius) {
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        BlockPos closest = null;
        double closestDist = Double.MAX_VALUE;
        for (int dy = -3; dy <= 4; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    mpos.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    BlockState state = level.getBlockState(mpos);
                    if (state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(Blocks.COBBLESTONE)) {
                        if (this.isPosInsideAnyOccupiedPlot(mpos.getX(), mpos.getZ())) continue;
                        double dist = center.distSqr(mpos);
                        if (dist < closestDist) {
                            closestDist = dist;
                            closest = mpos.immutable();
                        }
                    }
                }
            }
        }
        return closest;
    }

    private @Nullable BlockPos findNearbyNaturalDirt(Level level, BlockPos center, int radius) {
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        BlockPos closest = null;
        double closestDist = Double.MAX_VALUE;
        for (int dy = -3; dy <= 3; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    mpos.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    BlockState state = level.getBlockState(mpos);
                    if (state.is(BlockTags.DIRT)) {
                        if (this.isPosInsideAnyOccupiedPlot(mpos.getX(), mpos.getZ())) continue;
                        double dist = center.distSqr(mpos);
                        if (dist < closestDist) {
                            closestDist = dist;
                            closest = mpos.immutable();
                        }
                    }
                }
            }
        }
        return closest;
    }

    private @Nullable BlockPos findNearbyTallGrass(Level level, BlockPos center, int radius) {
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        BlockPos closest = null;
        double closestDist = Double.MAX_VALUE;
        for (int dy = -2; dy <= 3; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    mpos.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    BlockState state = level.getBlockState(mpos);
                    if (state.is(Blocks.SHORT_GRASS) || state.is(Blocks.TALL_GRASS) || state.is(Blocks.FERN)) {
                        if (this.isPosInsideAnyOccupiedPlot(mpos.getX(), mpos.getZ())) continue;
                        double dist = center.distSqr(mpos);
                        if (dist < closestDist) {
                            closestDist = dist;
                            closest = mpos.immutable();
                        }
                    }
                }
            }
        }
        return closest;
    }

    private void navigateTowardsTarget(BlockPos target) {
        if (target == null) return;
        Level level = this.kobold.level();
        if (this.currentPhase == BuildPhase.FETCHING_FROM_CHEST) {
            this.kobold.getNavigation().moveTo(target.getX() + 0.5D, target.getY() + 1.0D, target.getZ() + 0.5D, this.speedModifier);
            return;
        }

        BlockPos standPos = findStandPosNear(target);
        if (standPos != null) {
            this.kobold.getNavigation().moveTo(standPos.getX() + 0.5D, standPos.getY(), standPos.getZ() + 0.5D, this.speedModifier);
        } else {
            int navY = target.getY();
            if (navY > this.kobold.blockPosition().getY() + 2) {
                int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, target.getX(), target.getZ());
                navY = Math.min(navY, surfaceY);
            }
            this.kobold.getNavigation().moveTo(target.getX() + 0.5D, navY, target.getZ() + 0.5D, this.speedModifier);
        }
    }

    private @Nullable BlockPos findStandPosNear(BlockPos target) {
        Level level = this.kobold.level();
        BlockPos best = null;
        double bestDistSq = Double.MAX_VALUE;
        for (Direction dir : Direction.values()) {
            if (dir.getAxis().isHorizontal()) {
                for (int d = 1; d <= 2; d++) {
                    BlockPos candidate = target.relative(dir, d);
                    int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, candidate.getX(), candidate.getZ());
                    BlockPos stand = new BlockPos(candidate.getX(), surfaceY, candidate.getZ());
                    if (!level.getBlockState(stand).blocksMotion() && level.getBlockState(stand.below()).blocksMotion()) {
                        double dSq = this.kobold.distanceToSqr(Vec3.atCenterOf(stand));
                        if (dSq < bestDistSq) {
                            bestDistSq = dSq;
                            best = stand;
                        }
                    }
                }
            }
        }
        return best;
    }

    private boolean clearEntitiesInBlock(ServerLevel sl, BlockPos pos) {
        AABB box = new AABB(pos);
        List<LivingEntity> occupying = sl.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive);
        if (!occupying.isEmpty()) {
            for (LivingEntity e : occupying) {
                Vec3 center = Vec3.atCenterOf(pos);
                Vec3 push = e.position().subtract(center);
                double distSq = push.horizontalDistanceSqr();
                if (distSq < 0.001D) {
                    double angle = this.kobold.getRandom().nextDouble() * Math.PI * 2.0D;
                    push = new Vec3(Math.cos(angle), 0.0D, Math.sin(angle));
                } else {
                    push = push.normalize();
                }

                // Empuje físico suave alejando a las entidades de las coordenadas del bloque
                e.setDeltaMovement(e.getDeltaMovement().add(push.x * 0.45D, 0.15D, push.z * 0.45D));
                e.hurtMarked = true;

                // Si la entidad se encuentra completamente dentro de la caja, desplazar de inmediato
                if (box.contains(e.position())) {
                    e.setPos(e.getX() + push.x * 0.8D, e.getY(), e.getZ() + push.z * 0.8D);
                }
            }

            // Si la propia constructora se solapa con el bloque objetivo, hacerla retroceder
            if (this.kobold.getBoundingBox().intersects(box)) {
                Vec3 back = this.kobold.position().subtract(Vec3.atCenterOf(pos)).normalize().scale(1.8D);
                this.kobold.getNavigation().moveTo(this.kobold.getX() + back.x, this.kobold.getY(), this.kobold.getZ() + back.z, this.speedModifier);
            }
            return false;
        }
        return true;
    }
}
