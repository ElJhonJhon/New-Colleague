package com.newcolleague.mod.entity.ai;

import com.newcolleague.mod.entity.KoboldGirlEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

/**
 * Evaluador de nodos: penaliza fuertemente nodos junto a caídas de más de 2 bloques
 * y prioriza caminar por Dirt Path frente a terrenos normales.
 */
public class KoboldNodeEvaluator extends WalkNodeEvaluator {
    @Override
    public PathType getPathTypeOfMob(PathfindingContext context, int x, int y, int z, Mob mob) {
        PathType type = super.getPathTypeOfMob(context, x, y, z, mob);
        if (type == PathType.WALKABLE) {
            // Si el nodo está pegado a un desnivel de más de 2 bloques, clasificarlo como DAMAGE_CAUTIOUS
            // para que el pathfinder aplique el malus alto y busque una ruta más alejada del borde
            if (KoboldGirlEntity.isNearDangerousDrop(context.level(), new BlockPos(x, y, z), 1)) {
                return PathType.DAMAGE_CAUTIOUS;
            }
        }
        return type;
    }

    @Override
    protected Node findAcceptedNode(int x, int y, int z, int jumpSize, double nodeHeight, Direction travelDirection, PathType blockPathTypeCurrent) {
        Node node = super.findAcceptedNode(x, y, z, jumpSize, nodeHeight, travelDirection, blockPathTypeCurrent);
        if (node != null && node.costMalus >= 0.0F) {
            // Si el nodo no es un peligro grave (malus < 16.0F)
            if (node.costMalus < 16.0F && this.currentContext != null) {
                BlockGetter level = this.currentContext.level();
                BlockPos nodePos = new BlockPos(node.x, node.y, node.z);
                BlockPos groundPos = new BlockPos(node.x, node.y - 1, node.z);
                BlockState nodeBlock = level.getBlockState(nodePos);
                BlockState groundBlock = level.getBlockState(groundPos);

                // Evitar caminar o subirse encima de las camas (preferir rodearlas por el suelo)
                if (nodeBlock.getBlock() instanceof BedBlock || groundBlock.getBlock() instanceof BedBlock) {
                    node.costMalus += 8.0F;
                }

                boolean isPath = groundBlock.is(Blocks.DIRT_PATH) || nodeBlock.is(Blocks.DIRT_PATH);

                if (!isPath) {
                    // Terreno normal fuera del Dirt Path (hierba, tierra, roca):
                    // Añadir coste adicional de +2.0F para que A* priorice fuertemente los Dirt Path
                    node.costMalus += 2.0F;
                }
            }
        }
        return node;
    }
}
