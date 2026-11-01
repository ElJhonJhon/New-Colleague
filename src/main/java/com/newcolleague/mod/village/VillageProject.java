package com.newcolleague.mod.village;

import com.newcolleague.mod.village.NbtStructureLoader.BlueprintEntry;
import com.newcolleague.mod.village.NbtStructureLoader.StructureType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class VillageProject {

    private final @Nullable StructureType structureType;
    private final @Nullable Identifier customTemplateId;
    private final @Nullable String customDisplayName;
    private final BlockPos origin;
    private final int targetY;
    private final int width;
    private final int length;
    private final int height;
    private final List<BlueprintEntry> entries;

    private boolean terraformed = false;
    private int buildIndex = 0;

    /**
     * Constructor estándar para tipos de estructura conocidos, con carga NBT o fallback procedural.
     */
    public VillageProject(StructureType structureType, BlockPos origin, @Nullable Level level) {
        this.structureType = structureType;
        this.customTemplateId = null;
        this.customDisplayName = null;
        this.origin = origin;
        this.targetY = origin.getY();

        Vec3i dims = NbtStructureLoader.getDimensions(level, structureType);
        this.width = dims.getX();
        this.height = dims.getY();
        this.length = dims.getZ();

        this.entries = NbtStructureLoader.loadEntries(level, structureType);
    }

    public VillageProject(StructureType structureType, BlockPos origin) {
        this(structureType, origin, null);
    }

    /**
     * Constructor dinámico para cualquier estructura NBT personalizada (ej: casas, templos, talleres creados con structure_block).
     */
    public VillageProject(Identifier customTemplateId, String displayName, BlockPos origin, Level level) {
        this.structureType = null;
        this.customTemplateId = customTemplateId;
        this.customDisplayName = displayName;
        this.origin = origin;
        this.targetY = origin.getY();

        Vec3i dims = NbtStructureLoader.getDimensions(level, customTemplateId).orElse(new Vec3i(5, 5, 5));
        this.width = dims.getX();
        this.height = dims.getY();
        this.length = dims.getZ();

        this.entries = NbtStructureLoader.loadEntries(level, customTemplateId);
    }

    public StructureType getStructureType() {
        return this.structureType != null ? this.structureType : StructureType.TENT;
    }

    public @Nullable Identifier getCustomTemplateId() {
        return this.customTemplateId;
    }

    public @Nullable String getCustomStructureName() {
        return this.customDisplayName;
    }

    public boolean isCustom() {
        return this.customTemplateId != null;
    }

    public String getDisplayName() {
        if (this.customDisplayName != null) return this.customDisplayName;
        return this.structureType != null ? this.structureType.getDisplayName() : "Estructura Tribal";
    }

    public BlockPos getOrigin() {
        return this.origin;
    }

    public int getTargetY() {
        return this.targetY;
    }

    public int getWidth() {
        return this.width;
    }

    public int getLength() {
        return this.length;
    }

    public int getHeight() {
        return this.height;
    }

    public boolean isInsideOrNear(BlockPos pos, int padding) {
        int minX = this.origin.getX() - padding;
        int maxX = this.origin.getX() + this.width + padding;
        int minZ = this.origin.getZ() - padding;
        int maxZ = this.origin.getZ() + this.length + padding;
        int minY = this.origin.getY() - 1;
        int maxY = this.origin.getY() + this.height + padding;
        return pos.getX() >= minX && pos.getX() <= maxX
                && pos.getZ() >= minZ && pos.getZ() <= maxZ
                && pos.getY() >= minY && pos.getY() <= maxY;
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

    public BlueprintEntry getCurrentEntry() {
        if (this.isCompleted()) return null;
        return this.entries.get(this.buildIndex);
    }

    public void advanceBuild() {
        if (!this.isCompleted()) {
            this.buildIndex++;
        }
    }

    public BlockPos getWorldPosForEntry(BlueprintEntry entry) {
        if (entry == null) return null;
        return this.origin.offset(entry.relX(), entry.relY(), entry.relZ());
    }

    /**
     * Busca la siguiente posición dentro del área de la estructura que requiere aplanado o relleno.
     * Retorna null si toda el área ya está perfectamente nivelada.
     */
    public BlockPos findNextTerraformTarget(Level level) {
        if (this.terraformed) return null;

        int w = this.width;
        int l = this.length;

        // 1. Prioridad: Despejar elevaciones, árboles naturales y montículos sobre el nivel objetivo Y
        // Se escanea hasta targetY + 10 hacia abajo para limpiar copas completas de árboles sin dejar bloques flotantes
        for (int y = this.targetY + 10; y > this.targetY; y--) {
            for (int x = 0; x < w; x++) {
                for (int z = 0; z < l; z++) {
                    BlockPos pos = new BlockPos(this.origin.getX() + x, y, this.origin.getZ() + z);
                    BlockState state = level.getBlockState(pos);
                    if (isClearableNaturalBlock(state)) {
                        return pos; // Requiere excavación / despeje
                    }
                }
            }
        }

        // 2. Prioridad: Rellenar huecos y desniveles en el suelo a nivel targetY
        for (int x = 0; x < w; x++) {
            for (int z = 0; z < l; z++) {
                BlockPos pos = new BlockPos(this.origin.getX() + x, this.targetY, this.origin.getZ() + z);
                BlockState state = level.getBlockState(pos);
                if (isHoleNeedingFill(state)) {
                    return pos; // Requiere colocar tierra a nivel de suelo
                }
            }
        }

        // 3. Prioridad: Asegurar base sólida justo debajo del suelo (targetY - 1)
        for (int x = 0; x < w; x++) {
            for (int z = 0; z < l; z++) {
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
     * Determina si un bloque sobre el suelo debe ser removido (montículos de tierra, césped, piedra, hojas, árboles, flores, etc.)
     * Utiliza tags estándar de Minecraft para soportar cualquier bioma (acacias, cerezos, manglares, etc.).
     * Nunca destruye bloques de jugadores, contenedores o lecho de roca.
     */
    public static boolean isClearableNaturalBlock(BlockState state) {
        if (state.isAir()) return false;
        if (state.is(Blocks.BEDROCK) || state.is(Blocks.OBSIDIAN)) return false;
        if (state.hasBlockEntity()) return false; // Proteger cofres y entidades de bloques
        if (state.getBlock() instanceof BedBlock) return false;

        return state.is(BlockTags.LEAVES)
                || state.is(BlockTags.LOGS)
                || state.is(BlockTags.FLOWERS)
                || state.is(BlockTags.DIRT)
                || state.is(BlockTags.BASE_STONE_OVERWORLD)
                || state.is(BlockTags.SAND)
                || state.is(BlockTags.SNOW)
                || state.is(Blocks.GRAVEL)
                || state.is(Blocks.SANDSTONE)
                || state.is(Blocks.RED_SANDSTONE)
                || state.is(Blocks.SHORT_GRASS)
                || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.FERN)
                || state.is(Blocks.LARGE_FERN)
                || state.is(Blocks.BAMBOO)
                || state.is(Blocks.SUGAR_CANE)
                || state.is(Blocks.CACTUS)
                || state.is(Blocks.VINE)
                || state.is(Blocks.HANGING_ROOTS);
    }

    /**
     * Determina si una posición a nivel o bajo el suelo es un hueco o vacío que debe rellenarse con tierra.
     */
    public static boolean isHoleNeedingFill(BlockState state) {
        return state.isAir() || state.is(Blocks.WATER) || state.is(Blocks.LAVA)
                || state.is(Blocks.SHORT_GRASS) || state.is(Blocks.TALL_GRASS) || state.is(Blocks.FERN)
                || state.is(BlockTags.FLOWERS) || state.is(BlockTags.LEAVES);
    }
}
