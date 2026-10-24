package com.newcolleague.mod.entity.ai;

import com.newcolleague.mod.entity.KoboldGirlEntity;
import com.newcolleague.mod.entity.AbstractTribalEntity.CommandStance;
import com.newcolleague.mod.entity.AbstractTribalEntity.KoboldProfession;
import com.newcolleague.mod.village.StructureBlueprint;
import com.newcolleague.mod.village.VillageProject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
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
        KoboldProfession prof = this.kobold.getProfession();
        boolean canBuild = prof == KoboldProfession.BUILDER
                || (prof == KoboldProfession.NONE && this.kobold.isOwned() && this.kobold.getCommandStance() == CommandStance.WORK)
                || (!this.kobold.isOwned() && !this.kobold.isDefender() && !this.kobold.isAnyQueen());
        if (!canBuild) return false;

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
            StructureBlueprint.BlueprintEntry current = project.getCurrentEntry();
            if (current == null) break;
            BlockPos checkPos = project.getWorldPosForEntry(current);
            if (level.getBlockState(checkPos).equals(current.state())) {
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

        StructureBlueprint.BlueprintEntry entry = project.getCurrentEntry();
        if (entry != null) {
            BlockPos worldPos = project.getWorldPosForEntry(entry);
            Item required = StructureBlueprint.getRequiredItemForBlock(entry.state());

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

            // 2.B: Buscar en cofres comunales cercanos
            BlockPos chest = this.findNearbyChestWithItem(level, required);
            if (chest != null) {
                this.chestTarget = chest;
                this.itemToFetch = required;
                this.targetPos = chest;
                this.currentPhase = BuildPhase.FETCHING_FROM_CHEST;
                return true;
            }

            // 2.C: Recolectar directamente de la naturaleza si es madera, piedra o tierra
            if (isWoodProduct(required)) {
                BlockPos treePos = this.findNearbyTreeLog(level, this.kobold.blockPosition(), 20);
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
                BlockPos grassPos = this.findNearbyTallGrass(level, this.kobold.blockPosition(), 16);
                if (grassPos != null) {
                    this.gatherTarget = grassPos;
                    this.targetPos = grassPos;
                    this.currentPhase = BuildPhase.GATHERING_FROM_NATURE;
                    return true;
                }
            }

            // 2.D: El ítem no está disponible ni se puede recolectar a mano (Cama, Lana, Linterna, etc.)
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
        if (this.targetPos != null) {
            this.kobold.getNavigation().moveTo(this.targetPos.getX() + 0.5D, this.targetPos.getY(), this.targetPos.getZ() + 0.5D, this.speedModifier);
        }
    }

    @Override
    public void stop() {
        this.targetPos = null;
        this.chestTarget = null;
        this.gatherTarget = null;
        this.workTicks = 0;
        this.waitingTicks = 0;
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
                            this.kobold.getNavigation().moveTo(this.targetPos.getX() + 0.5D, this.targetPos.getY(), this.targetPos.getZ() + 0.5D, this.speedModifier);
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
            withinReach = (horizontalDistSq <= 12.25D && Math.abs(dy) <= 5.0D) || distSq <= 20.25D;
        }

        if (!withinReach) {
            if (this.kobold.getNavigation().isDone() || this.kobold.tickCount % 20 == 0) {
                this.kobold.getNavigation().moveTo(this.targetPos.getX() + 0.5D, this.targetPos.getY(), this.targetPos.getZ() + 0.5D, this.speedModifier);
            }
            return;
        }

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
                        this.kobold.getNavigation().moveTo(this.targetPos.getX() + 0.5D, this.targetPos.getY(), this.targetPos.getZ() + 0.5D, this.speedModifier);
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
                        } else if (state.is(Blocks.STONE) || state.is(Blocks.ANDESITE) || state.is(Blocks.DIORITE) || state.is(Blocks.GRANITE) || state.is(Blocks.COBBLESTONE)) {
                            this.kobold.getInventory().addItem(new ItemStack(Items.COBBLESTONE));
                            this.kobold.playSound(SoundEvents.STONE_BREAK, 1.0F, 1.0F);
                        } else if (state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK)) {
                            this.kobold.getInventory().addItem(new ItemStack(Items.DIRT));
                            this.kobold.playSound(SoundEvents.GRASS_BREAK, 1.0F, 1.0F);
                        } else if (state.is(Blocks.SHORT_GRASS) || state.is(Blocks.TALL_GRASS)) {
                            this.kobold.getInventory().addItem(new ItemStack(Items.WHEAT_SEEDS));
                            this.kobold.playSound(SoundEvents.GRASS_BREAK, 1.0F, 1.0F);
                        }
                        sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.kobold.getX(), this.kobold.getY() + 1.2D, this.kobold.getZ(), 4, 0.2D, 0.2D, 0.2D, 0.05D);
                    }
                    this.findTarget(project);
                    if (this.targetPos != null) {
                        this.kobold.getNavigation().moveTo(this.targetPos.getX() + 0.5D, this.targetPos.getY(), this.targetPos.getZ() + 0.5D, this.speedModifier);
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
                        if (state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.PODZOL)) {
                            this.kobold.getInventory().addItem(new ItemStack(Items.DIRT));
                            this.kobold.playSound(SoundEvents.GRASS_BREAK, 1.0F, 1.0F);
                        } else if (state.is(Blocks.STONE) || state.is(Blocks.ANDESITE) || state.is(Blocks.DIORITE) || state.is(Blocks.GRANITE)) {
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
                        this.kobold.getNavigation().moveTo(this.targetPos.getX() + 0.5D, this.targetPos.getY(), this.targetPos.getZ() + 0.5D, this.speedModifier);
                    }
                }
            }
            case TERRAFORMING_FILL -> {
                if (this.workTicks >= 16) {
                    this.workTicks = 0;
                    BlockState state = level.getBlockState(this.targetPos);
                    if (VillageProject.isHoleNeedingFill(state)) {
                        if (this.consumeItem(Items.DIRT, 1)) {
                            sl.setBlock(this.targetPos, Blocks.DIRT.defaultBlockState(), 3);
                            this.kobold.playSound(SoundEvents.GRASS_PLACE, 1.0F, 1.0F);
                            sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.targetPos.getX() + 0.5D, this.targetPos.getY() + 1.1D, this.targetPos.getZ() + 0.5D, 3, 0.2D, 0.2D, 0.2D, 0.05D);
                        }
                    }
                    this.findTarget(project);
                    if (this.targetPos != null) {
                        this.kobold.getNavigation().moveTo(this.targetPos.getX() + 0.5D, this.targetPos.getY(), this.targetPos.getZ() + 0.5D, this.speedModifier);
                    }
                }
            }
            case BUILDING -> {
                if (this.workTicks >= 18) {
                    this.workTicks = 0;
                    StructureBlueprint.BlueprintEntry entry = project.getCurrentEntry();
                    if (entry != null) {
                        Item required = StructureBlueprint.getRequiredItemForBlock(entry.state());
                        boolean canPlace = (required == Items.AIR) || this.consumeOrCraftItem(required);
                        if (canPlace) {
                            BlockPos worldPos = project.getWorldPosForEntry(entry);
                            sl.setBlock(worldPos, entry.state(), 3);

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
                            this.kobold.getNavigation().moveTo(this.targetPos.getX() + 0.5D, this.targetPos.getY(), this.targetPos.getZ() + 0.5D, this.speedModifier);
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

    private boolean hasOrCanCraft(Item item) {
        if (this.hasItem(item)) return true;
        if (item == Items.OAK_PLANKS) return this.hasItem(Items.OAK_LOG);
        if (item == Items.OAK_SLAB) return this.hasItem(Items.OAK_PLANKS) || this.hasItem(Items.OAK_LOG);
        if (item == Items.OAK_FENCE || item == Items.OAK_FENCE_GATE) return this.hasItem(Items.OAK_PLANKS) || this.hasItem(Items.OAK_LOG);
        if (item == Items.CRAFTING_TABLE) return this.countItem(Items.OAK_PLANKS) >= 4 || this.hasItem(Items.OAK_LOG);
        if (item == Items.CHEST || item == Items.BARREL) return this.countItem(Items.OAK_PLANKS) >= 8 || this.countItem(Items.OAK_LOG) >= 2;
        if (item == Items.CAMPFIRE) return this.hasItem(Items.OAK_LOG);
        if (item == Items.TORCH) return this.hasItem(Items.TORCH) || this.hasItem(Items.COAL) || this.hasItem(Items.CHARCOAL);
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
        if (item == Items.TORCH && (this.consumeItem(Items.COAL, 1) || this.consumeItem(Items.CHARCOAL, 1))) {
            this.kobold.getInventory().addItem(new ItemStack(Items.TORCH, 3));
            return true;
        }
        return false;
    }

    private static boolean isWoodProduct(Item item) {
        return item == Items.OAK_PLANKS || item == Items.OAK_SLAB || item == Items.OAK_FENCE
                || item == Items.OAK_FENCE_GATE || item == Items.CRAFTING_TABLE || item == Items.CHEST
                || item == Items.BARREL || item == Items.CAMPFIRE || item == Items.OAK_LOG;
    }

    private @Nullable BlockPos findNearbyChestWithItem(Level level, Item item) {
        List<BlockPos> chests = KoboldGirlEntity.findNearbyUniqueChests(level, this.kobold.blockPosition(), 16, 6);
        for (BlockPos pos : chests) {
            Container c = KoboldGirlEntity.getChestContainer(level, pos);
            if (c == null || c.isEmpty()) continue;
            for (int i = 0; i < c.getContainerSize(); i++) {
                ItemStack st = c.getItem(i);
                if (!st.isEmpty()) {
                    if (st.is(item) || (isWoodProduct(item) && (st.is(ItemTags.LOGS) || st.is(Items.OAK_LOG)))) {
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
            for (StructureBlueprint.VillagePlot plot : queen.getOccupiedVillagePlots()) {
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
                int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, checkX, checkZ);
                mpos.set(checkX, y - 1, checkZ);
                if (level.getBlockState(mpos).is(BlockTags.LOGS)) {
                    trees++;
                    if (trees > 4) return trees;
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
                    if (state.is(Blocks.STONE) || state.is(Blocks.ANDESITE) || state.is(Blocks.DIORITE) || state.is(Blocks.GRANITE) || state.is(Blocks.COBBLESTONE)) {
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
                    if (state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK)) {
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
                    if (state.is(Blocks.SHORT_GRASS) || state.is(Blocks.TALL_GRASS)) {
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
}
