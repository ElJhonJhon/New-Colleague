package com.newcolleague.mod.entity.ai;

import com.newcolleague.mod.entity.KoboldGirlEntity;
import com.newcolleague.mod.entity.AbstractTribalEntity.CommandStance;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/**
 * Meta: Deambular seguro (prioriza Dirt Path, evita desniveles >2 bloques y bloques o líquidos peligrosos)
 */
public class SafeKoboldStrollGoal extends WaterAvoidingRandomStrollGoal {
    private final KoboldGirlEntity kobold;

    public SafeKoboldStrollGoal(KoboldGirlEntity mob, double speedModifier) {
        super(mob, speedModifier);
        this.kobold = mob;
    }

    @Override
    public boolean canUse() {
        if (this.kobold.isOwned() && this.kobold.getCommandStance() != CommandStance.WORK) {
            return false;
        }
        // Guardias y Vigilantes patrullan de forma activa y continua los caminos con KoboldPatrolPathsGoal
        if (this.kobold.isDefender()) {
            return false;
        }
        return super.canUse();
    }

    @Override
    protected Vec3 getPosition() {
        Vec3 bestCandidate = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        KoboldGirlEntity queen = this.kobold.isRebel() ? this.kobold.getTribeQueen() : null;
        Vec3 queenPos = (queen != null && queen.isAlive()) ? queen.position() : null;

        // Intentar hasta 8 veces encontrar una posición de deambulación segura, priorizando Dirt Path
        for (int attempt = 0; attempt < 8; attempt++) {
            Vec3 candidate = super.getPosition();
            if (candidate != null) {
                // Si es rebelde y la reina está viva, no deambular a más de 15 bloques de la Reina
                if (queenPos != null && candidate.distanceTo(queenPos) > 100.0D) {
                    continue;
                }
                BlockPos targetPos = BlockPos.containing(candidate);
                // Evitar elegir posiciones cercanas a precipicios (>2 bloques) o peligros
                if (!KoboldGirlEntity.isNearDangerousDrop(this.kobold.level(), targetPos, 2)
                        && !KoboldGirlEntity.isNearHazard(this.kobold.level(), targetPos, 2)) {
                    double score = KoboldGirlEntity.isDirtPath(this.kobold.level(), targetPos) ? 100.0D : 10.0D;
                    if (score > bestScore) {
                        bestScore = score;
                        bestCandidate = candidate;
                        // Si encontramos una sobre Dirt Path, seleccionarla de inmediato
                        if (score >= 100.0D) {
                            break;
                        }
                    }
                }
            }
        }
        if (bestCandidate == null && queen != null && this.kobold.distanceTo(queen) > 10.0D) {
            Vec3 dir = queenPos.subtract(this.kobold.position()).normalize().scale(6.0D);
            BlockPos ground = this.kobold.level().getHeightmapPos(
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    BlockPos.containing(this.kobold.position().add(dir))
            );
            if (!KoboldGirlEntity.isNearDangerousDrop(this.kobold.level(), ground, 2) && !KoboldGirlEntity.isNearHazard(this.kobold.level(), ground, 2)) {
                return Vec3.atBottomCenterOf(ground);
            }
        }
        return bestCandidate;
    }
}
