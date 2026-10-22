package com.newcolleague.mod.village;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.SlabType;

import java.util.ArrayList;
import java.util.List;

public class StructureBlueprint {

    public enum StructureType {
        CAMPFIRE_BASE("Campamento Central", 5, 5, 3),
        TENT("Tienda de Descanso", 5, 5, 4),
        FARM("Granja Comunal", 5, 5, 2),
        STOREHOUSE("Depósito de Suministros", 5, 5, 4),
        WATCHTOWER("Torre de Guardia", 5, 5, 5);

        private final String displayName;
        private final int width;
        private final int length;
        private final int height;

        StructureType(String displayName, int width, int length, int height) {
            this.displayName = displayName;
            this.width = width;
            this.length = length;
            this.height = height;
        }

        public String getDisplayName() {
            return this.displayName;
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

    public record VillagePlot(int minX, int minZ, int maxX, int maxZ) {
        public boolean intersects(int otherMinX, int otherMinZ, int otherMaxX, int otherMaxZ, int buffer) {
            return !(otherMaxX + buffer < this.minX - buffer
                    || otherMinX - buffer > this.maxX + buffer
                    || otherMaxZ + buffer < this.minZ - buffer
                    || otherMinZ - buffer > this.maxZ + buffer);
        }

        public boolean contains(int x, int z) {
            return x >= this.minX && x <= this.maxX && z >= this.minZ && z <= this.maxZ;
        }
    }

    public static List<BlueprintEntry> createBlueprint(StructureType type) {
        List<BlueprintEntry> entries = new ArrayList<>();
        switch (type) {
            case CAMPFIRE_BASE -> buildCampfireBase(entries);
            case TENT -> buildTent(entries);
            case FARM -> buildFarm(entries);
            case STOREHOUSE -> buildStorehouse(entries);
            case WATCHTOWER -> buildWatchtower(entries);
        }
        return entries;
    }

    public static Item getRequiredItemForBlock(BlockState state) {
        if (state == null || state.isAir()) return Items.AIR;
        if (state.is(Blocks.FARMLAND) || state.is(Blocks.DIRT_PATH)) {
            return Items.DIRT;
        }
        if (state.is(Blocks.WATER)) {
            return Items.WATER_BUCKET;
        }
        if (state.is(Blocks.WHEAT)) {
            return Items.WHEAT_SEEDS;
        }
        if (state.is(Blocks.CARROTS)) {
            return Items.CARROT;
        }
        if (state.is(Blocks.POTATOES)) {
            return Items.POTATO;
        }
        if (state.getBlock() instanceof BedBlock && state.hasProperty(BedBlock.PART)) {
            if (state.getValue(BedBlock.PART) == BedPart.HEAD) {
                return Items.AIR; // La cama se consume completamente al colocar la parte FOOT
            }
        }
        Item item = state.getBlock().asItem();
        return item != null ? item : Items.AIR;
    }

    private static void buildCampfireBase(List<BlueprintEntry> entries) {
        // Suelo: camino de tierra y piedra
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                if (x == 2 && z == 2) {
                    // Fogata en el centro
                    entries.add(new BlueprintEntry(x, 1, z, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true), false, false));
                    entries.add(new BlueprintEntry(x, 0, z, Blocks.COBBLESTONE.defaultBlockState(), false, false));
                } else if ((x == 1 || x == 3) && z == 2) {
                    // Troncos para sentarse horizontalmente (X axis)
                    entries.add(new BlueprintEntry(x, 1, z, Blocks.OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X), false, false));
                    entries.add(new BlueprintEntry(x, 0, z, Blocks.DIRT_PATH.defaultBlockState(), false, false));
                } else if (x == 2 && (z == 1 || z == 3)) {
                    // Troncos para sentarse horizontalmente (Z axis)
                    entries.add(new BlueprintEntry(x, 1, z, Blocks.OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z), false, false));
                    entries.add(new BlueprintEntry(x, 0, z, Blocks.DIRT_PATH.defaultBlockState(), false, false));
                } else {
                    entries.add(new BlueprintEntry(x, 0, z, Blocks.DIRT_PATH.defaultBlockState(), false, false));
                }
            }
        }
        // Antorchas en las 4 esquinas
        entries.add(new BlueprintEntry(0, 1, 0, Blocks.OAK_FENCE.defaultBlockState(), false, false));
        entries.add(new BlueprintEntry(0, 2, 0, Blocks.TORCH.defaultBlockState(), false, false));
        entries.add(new BlueprintEntry(4, 1, 0, Blocks.OAK_FENCE.defaultBlockState(), false, false));
        entries.add(new BlueprintEntry(4, 2, 0, Blocks.TORCH.defaultBlockState(), false, false));
        entries.add(new BlueprintEntry(0, 1, 4, Blocks.OAK_FENCE.defaultBlockState(), false, false));
        entries.add(new BlueprintEntry(0, 2, 4, Blocks.TORCH.defaultBlockState(), false, false));
        entries.add(new BlueprintEntry(4, 1, 4, Blocks.OAK_FENCE.defaultBlockState(), false, false));
        entries.add(new BlueprintEntry(4, 2, 4, Blocks.TORCH.defaultBlockState(), false, false));
    }

    private static void buildTent(List<BlueprintEntry> entries) {
        // Suelo de madera de roble
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                entries.add(new BlueprintEntry(x, 0, z, Blocks.OAK_PLANKS.defaultBlockState(), false, false));
            }
        }

        // Estructura en A de lana (Tienda acogedora estilo nómada)
        // Paredes laterales de lana roja a Y=1 y Y=2
        for (int z = 0; z < 5; z++) {
            entries.add(new BlueprintEntry(0, 1, z, Blocks.WOOL.red().defaultBlockState(), false, false));
            entries.add(new BlueprintEntry(4, 1, z, Blocks.WOOL.red().defaultBlockState(), false, false));

            entries.add(new BlueprintEntry(1, 2, z, Blocks.WOOL.yellow().defaultBlockState(), false, false));
            entries.add(new BlueprintEntry(3, 2, z, Blocks.WOOL.yellow().defaultBlockState(), false, false));

            // Techo de la tienda a Y=3
            entries.add(new BlueprintEntry(2, 3, z, Blocks.WOOL.white().defaultBlockState(), false, false));
        }

        // Pared trasera de lana (z = 4)
        entries.add(new BlueprintEntry(1, 1, 4, Blocks.WOOL.red().defaultBlockState(), false, false));
        entries.add(new BlueprintEntry(2, 1, 4, Blocks.WOOL.red().defaultBlockState(), false, false));
        entries.add(new BlueprintEntry(3, 1, 4, Blocks.WOOL.red().defaultBlockState(), false, false));
        entries.add(new BlueprintEntry(2, 2, 4, Blocks.WOOL.yellow().defaultBlockState(), false, false));

        // Columnas frontales de valla de roble (z = 0)
        entries.add(new BlueprintEntry(1, 1, 0, Blocks.OAK_FENCE.defaultBlockState(), false, false));
        entries.add(new BlueprintEntry(3, 1, 0, Blocks.OAK_FENCE.defaultBlockState(), false, false));

        // Cama dentro de la tienda (x=2, z=2 pie y z=3 cabecera)
        entries.add(new BlueprintEntry(2, 1, 2, Blocks.BED.red().defaultBlockState()
                .setValue(BedBlock.FACING, Direction.NORTH)
                .setValue(BedBlock.PART, BedPart.FOOT), true, false));
        entries.add(new BlueprintEntry(2, 1, 3, Blocks.BED.red().defaultBlockState()
                .setValue(BedBlock.FACING, Direction.NORTH)
                .setValue(BedBlock.PART, BedPart.HEAD), true, false));

        // Linterna colgada en el techo frontal
        entries.add(new BlueprintEntry(2, 2, 1, Blocks.LANTERN.defaultBlockState(), false, false));
    }

    private static void buildFarm(List<BlueprintEntry> entries) {
        // Perímetro de vallas de roble a Y=1 con puerta en el frente (z=0, x=2)
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                boolean isBorder = (x == 0 || x == 4 || z == 0 || z == 4);
                if (isBorder) {
                    entries.add(new BlueprintEntry(x, 0, z, Blocks.OAK_PLANKS.defaultBlockState(), false, false));
                    if (x == 2 && z == 0) {
                        entries.add(new BlueprintEntry(x, 1, z, Blocks.OAK_FENCE_GATE.defaultBlockState()
                                .setValue(FenceGateBlock.FACING, Direction.SOUTH), false, false));
                    } else {
                        entries.add(new BlueprintEntry(x, 1, z, Blocks.OAK_FENCE.defaultBlockState(), false, false));
                    }
                } else {
                    // Centro (x=2, z=2): Agua para regar con losa de roble encima
                    if (x == 2 && z == 2) {
                        entries.add(new BlueprintEntry(x, 0, z, Blocks.WATER.defaultBlockState(), false, false));
                        entries.add(new BlueprintEntry(x, 1, z, Blocks.OAK_SLAB.defaultBlockState()
                                .setValue(SlabBlock.TYPE, SlabType.BOTTOM), false, false));
                    } else {
                        // Tierra arada con cultivos de trigo/zanahorias
                        entries.add(new BlueprintEntry(x, 0, z, Blocks.FARMLAND.defaultBlockState(), false, false));
                        // Cultivo maduro o intermedio
                        BlockState crop = (x % 2 == 0) ? Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, 5)
                                : Blocks.CARROTS.defaultBlockState().setValue(CropBlock.AGE, 5);
                        entries.add(new BlueprintEntry(x, 1, z, crop, false, false));
                    }
                }
            }
        }
        // Antorchas sobre las vallas de las esquinas
        entries.add(new BlueprintEntry(0, 2, 0, Blocks.TORCH.defaultBlockState(), false, false));
        entries.add(new BlueprintEntry(4, 2, 0, Blocks.TORCH.defaultBlockState(), false, false));
        entries.add(new BlueprintEntry(0, 2, 4, Blocks.TORCH.defaultBlockState(), false, false));
        entries.add(new BlueprintEntry(4, 2, 4, Blocks.TORCH.defaultBlockState(), false, false));
    }

    private static void buildStorehouse(List<BlueprintEntry> entries) {
        // Suelo de tablones de roble
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                entries.add(new BlueprintEntry(x, 0, z, Blocks.OAK_PLANKS.defaultBlockState(), false, false));
            }
        }

        // Columnas de tronco de roble en las 4 esquinas hasta Y=3
        int[][] corners = new int[][]{{0, 0}, {4, 0}, {0, 4}, {4, 4}};
        for (int[] c : corners) {
            for (int y = 1; y <= 3; y++) {
                entries.add(new BlueprintEntry(c[0], y, c[1], Blocks.OAK_LOG.defaultBlockState(), false, false));
            }
        }

        // Vigas superiores conectando las columnas a Y=3
        for (int x = 1; x <= 3; x++) {
            entries.add(new BlueprintEntry(x, 3, 0, Blocks.OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X), false, false));
            entries.add(new BlueprintEntry(x, 3, 4, Blocks.OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X), false, false));
        }
        for (int z = 1; z <= 3; z++) {
            entries.add(new BlueprintEntry(0, 3, z, Blocks.OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z), false, false));
            entries.add(new BlueprintEntry(4, 3, z, Blocks.OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z), false, false));
        }

        // Techo de losas de roble a Y=4
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                entries.add(new BlueprintEntry(x, 4, z, Blocks.OAK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM), false, false));
            }
        }

        // Paredes traseras y laterales de valla para ventilación a Y=1 y Y=2
        for (int x = 1; x <= 3; x++) {
            entries.add(new BlueprintEntry(x, 1, 4, Blocks.OAK_PLANKS.defaultBlockState(), false, false));
            entries.add(new BlueprintEntry(x, 2, 4, Blocks.OAK_FENCE.defaultBlockState(), false, false));
        }
        entries.add(new BlueprintEntry(0, 1, 1, Blocks.OAK_PLANKS.defaultBlockState(), false, false));
        entries.add(new BlueprintEntry(0, 2, 1, Blocks.OAK_FENCE.defaultBlockState(), false, false));
        entries.add(new BlueprintEntry(0, 1, 2, Blocks.OAK_PLANKS.defaultBlockState(), false, false));
        entries.add(new BlueprintEntry(0, 2, 2, Blocks.OAK_FENCE.defaultBlockState(), false, false));
        entries.add(new BlueprintEntry(0, 1, 3, Blocks.OAK_PLANKS.defaultBlockState(), false, false));
        entries.add(new BlueprintEntry(0, 2, 3, Blocks.OAK_FENCE.defaultBlockState(), false, false));

        entries.add(new BlueprintEntry(4, 1, 1, Blocks.OAK_PLANKS.defaultBlockState(), false, false));
        entries.add(new BlueprintEntry(4, 2, 1, Blocks.OAK_FENCE.defaultBlockState(), false, false));
        entries.add(new BlueprintEntry(4, 1, 2, Blocks.OAK_PLANKS.defaultBlockState(), false, false));
        entries.add(new BlueprintEntry(4, 2, 2, Blocks.OAK_FENCE.defaultBlockState(), false, false));
        entries.add(new BlueprintEntry(4, 1, 3, Blocks.OAK_PLANKS.defaultBlockState(), false, false));
        entries.add(new BlueprintEntry(4, 2, 3, Blocks.OAK_FENCE.defaultBlockState(), false, false));

        // Interior: Cofres organizados y barriles
        entries.add(new BlueprintEntry(1, 1, 3, Blocks.CHEST.defaultBlockState(), false, true));
        entries.add(new BlueprintEntry(2, 1, 3, Blocks.BARREL.defaultBlockState(), false, false));
        entries.add(new BlueprintEntry(3, 1, 3, Blocks.CHEST.defaultBlockState(), false, true));
        entries.add(new BlueprintEntry(1, 1, 1, Blocks.CRAFTING_TABLE.defaultBlockState(), false, false));

        // Linterna colgada del techo
        entries.add(new BlueprintEntry(2, 2, 2, Blocks.LANTERN.defaultBlockState(), false, false));
    }

    private static void buildWatchtower(List<BlueprintEntry> entries) {
        // Base de adoquín (Cobblestone) reforzado
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                entries.add(new BlueprintEntry(x, 0, z, Blocks.COBBLESTONE.defaultBlockState(), false, false));
            }
        }

        // Columnas de tronco de roble en las 4 esquinas subiendo 4 bloques (Y=1 a Y=4)
        int[][] corners = new int[][]{{0, 0}, {4, 0}, {0, 4}, {4, 4}};
        for (int[] c : corners) {
            for (int y = 1; y <= 4; y++) {
                entries.add(new BlueprintEntry(c[0], y, c[1], Blocks.OAK_LOG.defaultBlockState(), false, false));
            }
        }

        // Muros de piedra a nivel Y=1 entre columnas
        for (int x = 1; x <= 3; x++) {
            if (x != 2) { // Dejar entrada abierta a x=2, z=0
                entries.add(new BlueprintEntry(x, 1, 0, Blocks.COBBLESTONE.defaultBlockState(), false, false));
            }
            entries.add(new BlueprintEntry(x, 1, 4, Blocks.COBBLESTONE.defaultBlockState(), false, false));
        }
        for (int z = 1; z <= 3; z++) {
            entries.add(new BlueprintEntry(0, 1, z, Blocks.COBBLESTONE.defaultBlockState(), false, false));
            entries.add(new BlueprintEntry(4, 1, z, Blocks.COBBLESTONE.defaultBlockState(), false, false));
        }

        // Plataforma superior de vigilancia a Y=4
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                entries.add(new BlueprintEntry(x, 4, z, Blocks.OAK_PLANKS.defaultBlockState(), false, false));
            }
        }

        // Parapeto / Barandilla de vallas en el perímetro superior a Y=5
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                if (x == 0 || x == 4 || z == 0 || z == 4) {
                    entries.add(new BlueprintEntry(x, 5, z, Blocks.OAK_FENCE.defaultBlockState(), false, false));
                }
            }
        }

        // Linternas en las esquinas superiores sobre las vallas
        entries.add(new BlueprintEntry(0, 6, 0, Blocks.LANTERN.defaultBlockState(), false, false));
        entries.add(new BlueprintEntry(4, 6, 0, Blocks.LANTERN.defaultBlockState(), false, false));
        entries.add(new BlueprintEntry(0, 6, 4, Blocks.LANTERN.defaultBlockState(), false, false));
        entries.add(new BlueprintEntry(4, 6, 4, Blocks.LANTERN.defaultBlockState(), false, false));

        // Cofre de munición / flechas en la torre
        entries.add(new BlueprintEntry(2, 5, 2, Blocks.CHEST.defaultBlockState(), false, true));
    }
}
