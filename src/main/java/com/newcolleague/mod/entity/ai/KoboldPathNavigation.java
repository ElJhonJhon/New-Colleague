package com.newcolleague.mod.entity.ai;

import com.newcolleague.mod.entity.KoboldGirlEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathFinder;

/**
 * Navegación adaptada: descarta destinos inestables o cercanos a precipicios y peligros.
 */
public class KoboldPathNavigation extends GroundPathNavigation {
    public KoboldPathNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    @Override
    protected PathFinder createPathFinder(int maxVisitedNodes) {
        this.nodeEvaluator = new KoboldNodeEvaluator();
        return new PathFinder(this.nodeEvaluator, maxVisitedNodes);
    }

    @Override
    public boolean isStableDestination(BlockPos pos) {
        if (!super.isStableDestination(pos)) {
            return false;
        }
        if (KoboldGirlEntity.isNearDangerousDrop(this.level, pos, 1)) {
            return false;
        }
        if (KoboldGirlEntity.isNearHazard(this.level, pos, 1)) {
            return false;
        }
        return true;
    }
}
