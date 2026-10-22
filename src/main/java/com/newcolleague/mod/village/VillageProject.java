package com.newcolleague.mod.village;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class VillageProject {

    private final StructureBlueprint.StructureType structureType;
    private final BlockPos origin;
    private final int targetY;
    private final List<StructureBlueprint.BlueprintEntry> entries;

    private boolean terraformed = false;
    private int buildIndex = 0;

    public VillageProject(StructureBlueprint.StructureType structureType, BlockPos origin) {
        this.structureType = structureType;
        this.origin = origin;
        this.targetY = origin.getY();
        this.entries = StructureBlueprint.createBlueprint(structureType);
    }

    public StructureBlueprint.StructureType getStructureType() {
        return this.structureType;
    }

    public BlockPos getOrigin() {
        return this.origin;
    }

    public int getTargetY() {
        return this.targetY;
    }

    public boolean isTerraformed() {
        return this.terraformed;
    }

    public void setTerraformed(boolean terraformed) {
        this.terraformed = terraformed;
    }

    public boolean isCompleted() {
        return this.buildIndex >= this.entries.size();
    }

    public int getBuildIndex() {
        return this.buildIndex;
    }

    public int getTotalBlocks() {
        return this.entries.size();
    }

    public StructureBlueprint.BlueprintEntry getCurrentEntry() {
        if (this.isCompleted()) return null;
        return this.entries.get(this.buildIndex);
    }

    public void advanceBuild() {
        if (!this.isCompleted()) {
            this.buildIndex++;
        }
    }

    public BlockPos getWorldPosForEntry(StructureBlueprint.BlueprintEntry entry) {
        if (entry == null) return null;
        return this.origin.offset(entry.relX(), entry.relY(), entry.relZ());
    }

    /**
     * Busca la siguiente posición dentro del área de la estructura que requiere aplanado o relleno.
     * Retorna null si toda el área ya está perfectamente nivelada.
     */
    public BlockPos findNextTerraformTarget(Level level) {
        if (this.terraformed) return null;

        int width = this.structureType.getWidth();
        int length = this.structureType.getLength();

        // 1. Prioridad: Despejar elevaciones y montículos sobre el nivel objetivo Y
        for (int y = this.targetY + 4; y > this.targetY; y--) {
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < length; z++) {
                    BlockPos pos = new BlockPos(this.origin.getX() + x, y, this.origin.getZ() + z);
                    BlockState state = level.getBlockState(pos);
                    if (isClearableNaturalBlock(state)) {
                        return pos; // Requiere excavación / despeje
                    }
                }
            }
        }

        // 2. Prioridad: Rellenar huecos y desniveles en el suelo a nivel targetY
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < length; z++) {
                BlockPos pos = new BlockPos(this.origin.getX() + x, this.targetY, this.origin.getZ() + z);
                BlockState state = level.getBlockState(pos);
                if (isHoleNeedingFill(state)) {
                    return pos; // Requiere colocar tierra a nivel de suelo
                }
            }
        }

        // 3. Prioridad: Asegurar base sólida justo debajo del suelo (targetY - 1)
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < length; z++) {
                BlockPos pos = new BlockPos(this.origin.getX() + x, this.targetY - 1, this.origin.getZ() + z);
                BlockState state = level.getBlockState(pos);
                if (isHoleNeedingFill(state)) {
                    return pos; // Requiere colocar base de cimiento
                }
            }
        }

        // El terreno está 100% plano y listo para construir
        this.terraformed = true;
        return null;
    }

    /**
     * Determina si un bloque sobre el suelo debe ser removido (montículos de tierra, césped, piedra, hojas, etc.)
     * Nunca destruye bloques de jugadores, contenedores o lecho de roca.
     */
    public static boolean isClearableNaturalBlock(BlockState state) {
        if (state.isAir()) return false;
        if (state.is(Blocks.BEDROCK) || state.is(Blocks.OBSIDIAN)) return false;
        if (state.hasBlockEntity()) return false; // Proteger cofres y entidades de bloques

        return state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.DIRT)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.PODZOL)
                || state.is(Blocks.STONE)
                || state.is(Blocks.ANDESITE)
                || state.is(Blocks.DIORITE)
                || state.is(Blocks.GRANITE)
                || state.is(Blocks.GRAVEL)
                || state.is(Blocks.SAND)
                || state.is(Blocks.SANDSTONE)
                || state.is(Blocks.SHORT_GRASS)
                || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.FERN)
                || state.is(Blocks.LARGE_FERN)
                || state.is(Blocks.SNOW)
                || state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.OAK_LEAVES)
                || state.is(Blocks.BIRCH_LEAVES)
                || state.is(Blocks.SPRUCE_LEAVES)
                || state.is(Blocks.OAK_LOG)
                || state.is(Blocks.BIRCH_LOG)
                || state.is(Blocks.DANDELION)
                || state.is(Blocks.POPPY)
                || state.is(Blocks.BLUE_ORCHID)
                || state.is(Blocks.ALLIUM)
                || state.is(Blocks.AZURE_BLUET)
                || state.is(Blocks.RED_TULIP)
                || state.is(Blocks.ORANGE_TULIP)
                || state.is(Blocks.WHITE_TULIP)
                || state.is(Blocks.PINK_TULIP)
                || state.is(Blocks.OXEYE_DAISY)
                || state.is(Blocks.CORNFLOWER)
                || state.is(Blocks.LILY_OF_THE_VALLEY);
    }

    /**
     * Determina si una posición a nivel o bajo el suelo es un hueco o vacío que debe rellenarse con tierra.
     */
    public static boolean isHoleNeedingFill(BlockState state) {
        return state.isAir() || state.is(Blocks.WATER) || state.is(Blocks.LAVA)
                || state.is(Blocks.SHORT_GRASS) || state.is(Blocks.TALL_GRASS) || state.is(Blocks.FERN);
    }
}
