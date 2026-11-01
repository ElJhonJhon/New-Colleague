package com.newcolleague.mod.village;

import com.newcolleague.mod.NewColleagueMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Motor central de estructuras tribales 100% basado en NBT.
 * Carga estructuras desde archivos .nbt exportados con el Structure Block,
 * calcula los materiales requeridos y gestiona las parcelas de la aldea.
 */
public class NbtStructureLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(NbtStructureLoader.class);
    private static Field PALETTES_FIELD = null;

    static {
        try {
            PALETTES_FIELD = StructureTemplate.class.getDeclaredField("palettes");
            PALETTES_FIELD.setAccessible(true);
        } catch (Throwable t) {
            LOGGER.debug("Campo 'palettes' no accesible directamente, se utilizará fallback NBT nativo: {}", t.getMessage());
            PALETTES_FIELD = null;
        }
    }

    public enum StructureType {
        CAMPFIRE_BASE("Campamento Central", "campfire_base", 5, 5, 3),
        TENT("Tienda de Descanso", "tent", 5, 5, 4),
        FARM("Granja Comunal", "farm", 5, 5, 2),
        STOREHOUSE("Depósito de Suministros", "storehouse", 5, 5, 4),
        WATCHTOWER("Torre de Guardia", "watchtower", 5, 5, 7);

        private final String displayName;
        private final String structurePath;
        private final int width;
        private final int length;
        private final int height;

        StructureType(String displayName, String structurePath, int width, int length, int height) {
            this.displayName = displayName;
            this.structurePath = structurePath;
            this.width = width;
            this.length = length;
            this.height = height;
        }

        public String getDisplayName() {
            return this.displayName;
        }

        public String getStructurePath() {
            return this.structurePath;
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
    }

    public record BlueprintEntry(int relX, int relY, int relZ, BlockState state, boolean isBed, boolean isChest) {}

    public record VillagePlot(int minX, int minZ, int maxX, int maxZ, @Nullable StructureType structureType) {
        public VillagePlot(int minX, int minZ, int maxX, int maxZ) {
            this(minX, minZ, maxX, maxZ, null);
        }

        public boolean intersects(int otherMinX, int otherMinZ, int otherMaxX, int otherMaxZ, int buffer) {
            return !(otherMaxX + buffer < this.minX - buffer
                    || otherMinX - buffer > this.maxX + buffer
                    || otherMaxZ - buffer < this.minZ - buffer
                    || otherMinZ - buffer > this.maxZ + buffer);
        }

        public boolean contains(int x, int z) {
            return x >= this.minX && x <= this.maxX && z >= this.minZ && z <= this.maxZ;
        }

        public double centerDistanceSqr(int x, int z) {
            double cx = (this.minX + this.maxX) / 2.0D;
            double cz = (this.minZ + this.maxZ) / 2.0D;
            double dx = cx - x;
            double dz = cz - z;
            return dx * dx + dz * dz;
        }
    }

    /**
     * Calcula qué ítem requiere un bloque para ser colocado por una Kobold constructora.
     */
    public static Item getRequiredItemForBlock(BlockState state) {
        if (state == null || state.isAir()) return Items.AIR;
        if (state.is(Blocks.FARMLAND) || state.is(Blocks.DIRT_PATH)) {
            return Items.DIRT;
        }
        if (state.is(Blocks.WATER)) {
            return Items.AIR; // Las kobolds riegan o llenan el pozo comunal sin requerir cubos de hierro
        }
        if (state.is(Blocks.WHEAT) || state.is(Blocks.CARROTS) || state.is(Blocks.POTATOES) || state.is(Blocks.BEETROOTS)) {
            return Items.WHEAT_SEEDS; // Admite semillas de trigo recolectables para toda la siembra
        }
        if (state.is(Blocks.LANTERN) || state.is(Blocks.SOUL_LANTERN)) {
            return Items.TORCH; // Sustituto automático tribal para iluminación
        }
        if (state.is(BlockTags.WOOL)) {
            return Blocks.WOOL.white().asItem(); // Lana genérica crafteable / hilable
        }
        if (state.getBlock() instanceof BedBlock && state.hasProperty(BedBlock.PART)) {
            if (state.getValue(BedBlock.PART) == BedPart.HEAD) {
                return Items.AIR; // La cama se consume completamente al colocar la parte FOOT
            }
            return Blocks.BED.white().asItem();
        }
        Item item = state.getBlock().asItem();
        return item != null ? item : Items.AIR;
    }

    /**
     * Obtiene el identificador estándar en el namespace del mod para un nombre de estructura dado.
     */
    public static Identifier getStructureId(String name) {
        if (name.contains(":")) {
            String[] parts = name.split(":", 2);
            return Identifier.fromNamespaceAndPath(parts[0], parts[1]);
        }
        return Identifier.fromNamespaceAndPath(NewColleagueMod.MODID, name);
    }

    /**
     * Intenta obtener la plantilla StructureTemplate desde el StructureTemplateManager de Minecraft.
     * Busca primero en el namespace del mod (newcolleaguemod) y si no existe, prueba en minecraft (por si se guardó sin prefijo).
     */
    public static Optional<StructureTemplate> getTemplate(Level level, Identifier id) {
        if (!(level instanceof ServerLevel sl)) return Optional.empty();
        StructureTemplateManager manager = sl.getStructureManager();

        Optional<StructureTemplate> opt = manager.get(id);
        if (opt.isPresent()) return opt;

        if (!id.getNamespace().equals("minecraft")) {
            Optional<StructureTemplate> fallbackOpt = manager.get(Identifier.fromNamespaceAndPath("minecraft", id.getPath()));
            if (fallbackOpt.isPresent()) return fallbackOpt;
        }

        return Optional.empty();
    }

    /**
     * Comprueba si existe un archivo de plantilla NBT registrado para el identificador dado.
     */
    public static boolean hasTemplate(Level level, Identifier id) {
        return getTemplate(level, id).isPresent();
    }

    /**
     * Obtiene las dimensiones físicas (ancho X, alto Y, largo Z) definidas en la plantilla NBT.
     */
    public static Optional<Vec3i> getDimensions(Level level, Identifier id) {
        return getTemplate(level, id).map(StructureTemplate::getSize);
    }

    /**
     * Obtiene las dimensiones físicas para un tipo de estructura predeterminado.
     */
    public static Vec3i getDimensions(Level level, StructureType type) {
        if (level != null) {
            Identifier id = getStructureId(type.getStructurePath());
            Optional<Vec3i> dims = getDimensions(level, id);
            if (dims.isPresent()) {
                return dims.get();
            }
        }
        return new Vec3i(type.getWidth(), type.getHeight(), type.getLength());
    }

    /**
     * Carga y convierte todos los bloques de la plantilla NBT para un tipo de estructura.
     */
    public static List<BlueprintEntry> loadEntries(Level level, StructureType type) {
        if (level == null) return Collections.emptyList();
        Identifier id = getStructureId(type.getStructurePath());
        return loadEntries(level, id);
    }

    /**
     * Carga y convierte todos los bloques de la plantilla NBT en una lista ordenada de BlueprintEntry.
     * Los bloques son ordenados por altura relY ascendente para que se construyan cimientos -> paredes -> techo.
     */
    public static List<BlueprintEntry> loadEntries(Level level, Identifier id) {
        Optional<StructureTemplate> templateOpt = getTemplate(level, id);
        if (templateOpt.isEmpty()) {
            LOGGER.warn("No se encontró la plantilla NBT para el ID: {}", id);
            return Collections.emptyList();
        }
        return extractEntries(level, templateOpt.get());
    }

    /**
     * Extrae todos los bloques de una StructureTemplate y los transforma en BlueprintEntry.
     */
    public static List<BlueprintEntry> extractEntries(Level level, StructureTemplate template) {
        List<BlueprintEntry> entries = new ArrayList<>();

        // 1. Intento por reflexión en palettes (acceso directo en memoria a los StructureBlockInfo)
        if (PALETTES_FIELD != null) {
            try {
                List<?> palettes = (List<?>) PALETTES_FIELD.get(template);
                if (palettes != null && !palettes.isEmpty()) {
                    Object firstPalette = palettes.get(0);
                    Method blocksMethod = firstPalette.getClass().getMethod("blocks");
                    @SuppressWarnings("unchecked")
                    List<StructureTemplate.StructureBlockInfo> blockInfos =
                            (List<StructureTemplate.StructureBlockInfo>) blocksMethod.invoke(firstPalette);
                    for (StructureTemplate.StructureBlockInfo info : blockInfos) {
                        BlockState state = info.state();
                        if (state == null || state.isAir()) continue;
                        boolean isBed = state.getBlock() instanceof BedBlock;
                        boolean isChest = state.is(Blocks.CHEST) || state.is(Blocks.BARREL);
                        entries.add(new BlueprintEntry(
                                info.pos().getX(), info.pos().getY(), info.pos().getZ(),
                                state, isBed, isChest
                        ));
                    }
                    if (!entries.isEmpty()) {
                        entries.sort(Comparator.comparingInt(BlueprintEntry::relY));
                        return entries;
                    }
                }
            } catch (Throwable t) {
                LOGGER.debug("Extracción por reflexión falló, recurriendo al método NBT nativo: {}", t.getMessage());
            }
        }

        // 2. Método NBT Fallback nativo: serializa la plantilla en CompoundTag y lee palette + blocks
        try {
            CompoundTag tag = template.save(new CompoundTag());
            ListTag paletteTag = tag.getListOrEmpty("palette");
            ListTag blocksTag = tag.getListOrEmpty("blocks");
            HolderGetter<Block> blockGetter = level.registryAccess().lookupOrThrow(Registries.BLOCK);

            List<BlockState> paletteStates = new ArrayList<>();
            for (int i = 0; i < paletteTag.size(); i++) {
                CompoundTag stateTag = paletteTag.getCompoundOrEmpty(i);
                paletteStates.add(NbtUtils.readBlockState(blockGetter, stateTag));
            }

            for (int i = 0; i < blocksTag.size(); i++) {
                CompoundTag bTag = blocksTag.getCompoundOrEmpty(i);
                ListTag posTag = bTag.getListOrEmpty("pos");
                if (posTag.size() < 3) continue;
                int x = posTag.getIntOr(0, 0);
                int y = posTag.getIntOr(1, 0);
                int z = posTag.getIntOr(2, 0);
                int stateIdx = bTag.getIntOr("state", 0);
                if (stateIdx >= 0 && stateIdx < paletteStates.size()) {
                    BlockState state = paletteStates.get(stateIdx);
                    if (state == null || state.isAir()) continue;
                    boolean isBed = state.getBlock() instanceof BedBlock;
                    boolean isChest = state.is(Blocks.CHEST) || state.is(Blocks.BARREL);
                    entries.add(new BlueprintEntry(x, y, z, state, isBed, isChest));
                }
            }
        } catch (Throwable t) {
            LOGGER.error("Error al extraer bloques de StructureTemplate mediante NBT: {}", t.getMessage(), t);
        }

        entries.sort(Comparator.comparingInt(BlueprintEntry::relY));
        return entries;
    }

    /**
     * Devuelve una lista con todos los IDs de plantillas disponibles en el namespace del mod.
     */
    public static List<Identifier> listAvailableModTemplates(Level level) {
        if (!(level instanceof ServerLevel sl)) return Collections.emptyList();
        return sl.getStructureManager().listTemplates()
                .filter(id -> id.getNamespace().equals(NewColleagueMod.MODID))
                .toList();
    }
}
