package com.newcolleague.mod.entity.ai;

import com.newcolleague.mod.entity.KoboldGirlEntity;
import com.newcolleague.mod.entity.AbstractTribalEntity.CommandStance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

/**
 * Meta: Patrullar caminos de tierra y sus alrededores en escuadrones coordinados (Exclusivo Guardias y Vigilantes)
 */
public class KoboldPatrolPathsGoal extends Goal {
    private final KoboldGirlEntity mob;
    private final double speed;
    private BlockPos patrolTarget;
    private BlockPos lastPatrolPos;
    private int sentryInspectionTicks = 0;
    private int lookCycleTimer = 0;
    private float scanYawOffset = 0.0F;
    private int pathRecomputeTimer = 0;
    private int squadCheckTimer = 0;

    public KoboldPatrolPathsGoal(KoboldGirlEntity mob, double speed) {
        this.mob = mob;
        this.speed = speed;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.mob.isOwned() && this.mob.getCommandStance() != CommandStance.WORK) {
            return false;
        }
        // Solo para Guardias, Vigilantes y la Reina de la tribu
        if (!this.mob.isDefender() && !this.mob.isAnyQueen()) {
            return false;
        }
        // Si está durmiendo o es hora de dormir (durante el atardecer para defensoras o de 3:00 AM a 6:00 AM para la reina), no patrulla
        if (this.mob.isSleeping() || this.mob.isSleepTime()) {
            return false;
        }
        // Si está en combate o amenazada, las metas de combate tienen prioridad
        if (this.mob.getTarget() != null || this.mob.isThreatened()) {
            return false;
        }
        // Si tiene que recuperar su armadura del soporte, hacerlo primero
        if (this.mob.isNeedsArmorRetrieval()) {
            return false;
        }
        // Si está en duelo de reinas, lucha por el poder o mirando el báculo
        if (this.mob.isInQueenDuel() || this.mob.isInPowerStruggle() || this.mob.isStaringAtBaculo()) {
            return false;
        }
        // Durante la jornada de almacenamiento, si tiene exceso de comida o mochila llena, ceder el paso para almacenar en cofres
        if (this.mob.isStorageAndOrganizationTime() && (this.mob.hasExcessFoodToStore() || !this.mob.hasFreeInventorySlot())) {
            return false;
        }
        // Si la reina está cazando presas durante la mañana, no patrulla simultáneamente
        if (this.mob.isAnyQueen() && this.mob.isMorningWorkTime() && this.mob.isHunting()) {
            return false;
        }

        // Actualizar o sincronizar el escuadrón de patrulla periódicamente
        this.squadCheckTimer++;
        if (this.squadCheckTimer >= 60 || this.mob.getPatrolSquadLeaderUUID() == null) {
            this.squadCheckTimer = 0;
            this.mob.updatePatrolSquad();
        }

        // Comportamiento según rol en el escuadrón
        if (this.mob.isPatrolSquadLeader()) {
            this.patrolTarget = this.findPatrolWaypoint();
            return this.patrolTarget != null;
        } else {
            KoboldGirlEntity leader = this.mob.getPatrolSquadLeaderEntity();
            return leader != null && leader.isAlive();
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.mob.isDefender() && !this.mob.isAnyQueen()) {
            return false;
        }
        if (this.mob.isSleeping() || this.mob.isSleepTime()) {
            return false;
        }
        if (this.mob.getTarget() != null || this.mob.isThreatened()) {
            return false;
        }
        if (this.mob.isNeedsArmorRetrieval()) {
            return false;
        }
        if (this.mob.isInQueenDuel() || this.mob.isInPowerStruggle() || this.mob.isStaringAtBaculo()) {
            return false;
        }
        if (this.mob.isStorageAndOrganizationTime() && (this.mob.hasExcessFoodToStore() || !this.mob.hasFreeInventorySlot())) {
            return false;
        }
        if (this.mob.isAnyQueen() && this.mob.isMorningWorkTime() && this.mob.isHunting()) {
            return false;
        }

        if (this.mob.isPatrolSquadLeader()) {
            return this.patrolTarget != null;
        } else {
            KoboldGirlEntity leader = this.mob.getPatrolSquadLeaderEntity();
            return leader != null && leader.isAlive() && leader.isDefender() && !leader.isSleeping();
        }
    }

    @Override
    public void start() {
        this.sentryInspectionTicks = 0;
        this.lookCycleTimer = 0;
        this.pathRecomputeTimer = 0;

        // Asegurar que porte su arma y escudo adecuados listos para la guardia
        this.mob.autoEquipWeaponsAndShield();

        if (this.mob.isPatrolSquadLeader()) {
            this.mob.setInspectingSentry(false);
            if (this.patrolTarget != null) {
                this.mob.getNavigation().moveTo(
                        this.patrolTarget.getX() + 0.5D,
                        this.patrolTarget.getY(),
                        this.patrolTarget.getZ() + 0.5D,
                        this.speed
                );
            }
        }
    }

    @Override
    public void stop() {
        this.patrolTarget = null;
        this.sentryInspectionTicks = 0;
        this.mob.setInspectingSentry(false);
        this.mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        // Periódicamente verificar consistencia del escuadrón (cada 60 ticks)
        this.squadCheckTimer++;
        if (this.squadCheckTimer >= 60) {
            this.squadCheckTimer = 0;
            this.mob.updatePatrolSquad();
        }

        if (this.mob.isPatrolSquadLeader()) {
            this.tickLeader();
        } else {
            this.tickFollower();
        }
    }

    private void tickLeader() {
        // Si el líder no tiene objetivo pero un seguidor del escuadrón sí lo tiene, acudir a defenderlo
        if (this.mob.getTarget() == null) {
            List<KoboldGirlEntity> followers = this.mob.level().getEntitiesOfClass(
                    KoboldGirlEntity.class,
                    this.mob.getBoundingBox().inflate(16.0D, 8.0D, 16.0D),
                    k -> k.isAlive() && k.isDefender() && this.mob.getUUID().equals(k.getPatrolSquadLeaderUUID()) && k.getTarget() != null && k.getTarget().isAlive()
            );
            if (!followers.isEmpty()) {
                this.mob.setTarget(followers.get(0).getTarget());
                return;
            }
        }

        if (this.patrolTarget == null) {
            this.patrolTarget = this.findPatrolWaypoint();
            if (this.patrolTarget == null) return;
        }

        double distSq = this.mob.distanceToSqr(Vec3.atCenterOf(this.patrolTarget));

        // Si llegó al punto de patrulla o el camino se completó
        if (distSq < 3.5D || this.mob.getNavigation().isDone()) {
            this.mob.getNavigation().stop();
            this.mob.setInspectingSentry(true);
            this.sentryInspectionTicks++;

            // Mantener vigilancia activa (girar la vista cada 20 ticks para escanear el perímetro frontal)
            this.lookCycleTimer++;
            if (this.lookCycleTimer >= 20) {
                this.lookCycleTimer = 0;
                this.scanYawOffset = (this.mob.getRandom().nextFloat() - 0.5F) * 100.0F;
            }

            float targetYaw = this.mob.getYRot() + this.scanYawOffset;
            double rad = Math.toRadians(targetYaw);
            double lookX = this.mob.getX() - Math.sin(rad) * 8.0D;
            double lookZ = this.mob.getZ() + Math.cos(rad) * 8.0D;
            this.mob.getLookControl().setLookAt(lookX, this.mob.getEyeY(), lookZ, 30.0F, 30.0F);

            // Tras 50 ticks (2.5 segundos) de inspección atenta del área, continuar al siguiente punto de patrulla
            if (this.sentryInspectionTicks >= 50) {
                this.mob.setInspectingSentry(false);
                this.lastPatrolPos = this.patrolTarget;
                this.patrolTarget = this.findPatrolWaypoint();
                this.sentryInspectionTicks = 0;
                if (this.patrolTarget != null) {
                    this.mob.getNavigation().moveTo(
                            this.patrolTarget.getX() + 0.5D,
                            this.patrolTarget.getY(),
                            this.patrolTarget.getZ() + 0.5D,
                            this.speed
                    );
                }
            }
        } else {
            // En marcha hacia el punto de patrulla
            this.mob.setInspectingSentry(false);
            this.pathRecomputeTimer++;
            // Si la navegación terminó pero aún está lejos, o tras 100 ticks (5 segundos), refrescar o buscar nuevo punto
            if (this.mob.getNavigation().isDone() || this.pathRecomputeTimer >= 100) {
                this.pathRecomputeTimer = 0;
                this.lastPatrolPos = this.patrolTarget;
                this.patrolTarget = this.findPatrolWaypoint();
                if (this.patrolTarget != null) {
                    this.mob.getNavigation().moveTo(
                            this.patrolTarget.getX() + 0.5D,
                            this.patrolTarget.getY(),
                            this.patrolTarget.getZ() + 0.5D,
                            this.speed
                    );
                }
            }
        }
    }

    private void tickFollower() {
        KoboldGirlEntity leader = this.mob.getPatrolSquadLeaderEntity();
        if (leader == null || !leader.isAlive()) {
            this.mob.updatePatrolSquad();
            return;
        }

        // Si el líder entra en combate, asistirlo de inmediato
        if (leader.getTarget() != null && leader.getTarget().isAlive() && this.mob.getTarget() == null) {
            this.mob.setTarget(leader.getTarget());
            return;
        }

        double distToLeader = this.mob.distanceTo(leader);
        int squadIndex = this.mob.getPatrolSquadIndexInGroup();
        int squadSize = Math.max(2, this.mob.getPatrolSquadSize());

        // 1. Si el líder está en inspección de centinela (en el punto de patrulla)
        if (leader.isInspectingSentry()) {
            // Si está dentro del radio de guardia del líder (<= 3.5 bloques), detenerse y cubrir la retaguardia / ángulos ciegos
            if (distToLeader <= 3.5D) {
                this.mob.getNavigation().stop();

                this.lookCycleTimer++;
                if (this.lookCycleTimer >= 20) {
                    this.lookCycleTimer = 0;
                    this.scanYawOffset = (this.mob.getRandom().nextFloat() - 0.5F) * 60.0F;
                }

                // Ángulo de cobertura complementario:
                // En dúo (squadSize == 2): Retaguardia exacta (180° opuesto al líder)
                // En trío/cuarteto: Distribución angular equilibrada
                float coverageAngleOffset = 180.0F;
                if (squadSize == 3) {
                    coverageAngleOffset = (squadIndex == 1) ? 120.0F : -120.0F;
                } else if (squadSize >= 4) {
                    coverageAngleOffset = (squadIndex == 1) ? 90.0F : ((squadIndex == 2) ? 180.0F : -90.0F);
                }

                float targetYaw = leader.getYRot() + coverageAngleOffset + this.scanYawOffset;
                double rad = Math.toRadians(targetYaw);
                double lookX = this.mob.getX() - Math.sin(rad) * 8.0D;
                double lookZ = this.mob.getZ() + Math.cos(rad) * 8.0D;
                this.mob.getLookControl().setLookAt(lookX, this.mob.getEyeY(), lookZ, 30.0F, 30.0F);
                return;
            }
        }

        // 2. En marcha de escolta tras el líder
        this.pathRecomputeTimer++;
        if (this.pathRecomputeTimer >= 10 || this.mob.getNavigation().isDone()) {
            this.pathRecomputeTimer = 0;

            // Calcular posición de formación de escolta (detrás del líder con ligero offset lateral)
            Vec3 look = leader.getLookAngle();
            Vec3 behind = new Vec3(-look.x, 0.0D, -look.z).normalize().scale(2.6D);

            // Desplazamiento lateral perpendicular según el índice en el escuadrón
            double sideSign = (squadIndex % 2 == 1) ? 1.0D : -1.0D;
            double sideMultiplier = 1.0D + ((squadIndex - 1) / 2) * 0.8D;
            Vec3 side = new Vec3(-look.z, 0.0D, look.x).normalize().scale(sideSign * 1.4D * sideMultiplier);

            Vec3 targetPos = leader.position().add(behind).add(side);
            BlockPos targetBlock = BlockPos.containing(targetPos);

            Level level = this.mob.level();
            BlockPos safeStand = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, targetBlock);

            double moveSpeed = this.speed;
            // Si se quedó rezagada (> 6 bloques), acelerar el paso para mantener la formación
            if (distToLeader > 6.0D) {
                moveSpeed *= 1.35D;
            } else if (distToLeader < 2.0D) {
                // Si está pegada al líder, esperar para no estorbar el paso
                this.mob.getNavigation().stop();
                return;
            }

            if (!KoboldGirlEntity.isNearDangerousDrop(level, safeStand, 2) && !KoboldGirlEntity.isNearHazard(level, safeStand, 2)) {
                this.mob.getNavigation().moveTo(safeStand.getX() + 0.5D, safeStand.getY(), safeStand.getZ() + 0.5D, moveSpeed);
            } else {
                this.mob.getNavigation().moveTo(leader, moveSpeed);
            }
        }
    }

    private BlockPos findPatrolWaypoint() {
        Level level = this.mob.level();
        BlockPos current = this.mob.blockPosition();
        BlockPos bestPos = null;
        double bestScore = -Double.MAX_VALUE;

        // Determinar ancla central de la aldea / tribu (Reina o Cama)
        BlockPos anchor = this.mob.hasAssignedBed() ? this.mob.getAssignedBedPos() : current;
        KoboldGirlEntity queen = this.mob.getTribeQueen();
        if (queen != null && queen.isAlive()) {
            anchor = queen.blockPosition();
        }

        int squadId = this.mob.getPatrolSquadId();
        int totalSquads = Math.max(1, this.mob.getPatrolSquadTotalGroups());

        // Ángulo objetivo de sector para este escuadrón (reparto radial de 360 grados)
        double targetSectorAngle = (totalSquads > 1) ? ((2.0D * Math.PI * squadId) / totalSquads) : 0.0D;

        // Obtener posiciones de líderes de otros escuadrones para repulsión mutua activa
        List<KoboldGirlEntity> otherLeaders = level.getEntitiesOfClass(
                KoboldGirlEntity.class,
                this.mob.getBoundingBox().inflate(48.0D, 16.0D, 48.0D),
                k -> k.isAlive() && k != this.mob && k.isDefender()
                        && Objects.equals(k.getTribeName(), this.mob.getTribeName())
                        && k.isPatrolSquadLeader()
        );

        int radius = 24;
        BlockPos.MutableBlockPos testPos = new BlockPos.MutableBlockPos();

        // Explorar en un radio de 24 bloques
        for (int dx = -radius; dx <= radius; dx += 2) {
            for (int dz = -radius; dz <= radius; dz += 2) {
                for (int dy = -3; dy <= 3; dy++) {
                    testPos.set(current.getX() + dx, current.getY() + dy, current.getZ() + dz);

                    // Comprobar si el bloque o el suelo es camino de tierra (Dirt Path)
                    boolean isOnPath = KoboldGirlEntity.isDirtPath(level, testPos);
                    boolean isNearPath = false;

                    if (!isOnPath) {
                        // Comprobar si está en los alrededores inmediatos de un camino de tierra (a 1-3 bloques)
                        for (Direction dir : Direction.Plane.HORIZONTAL) {
                            BlockPos neighbor = testPos.relative(dir);
                            if (KoboldGirlEntity.isDirtPath(level, neighbor) || KoboldGirlEntity.isDirtPath(level, neighbor.relative(dir))) {
                                isNearPath = true;
                                break;
                            }
                        }
                    }

                    if (isOnPath || isNearPath) {
                        BlockPos standPos = level.getBlockState(testPos).is(Blocks.DIRT_PATH) ? testPos.above() : testPos.immutable();

                        // Asegurar que la posición para pararse sea segura y transitable
                        if (level.getBlockState(standPos).isAir()
                                && level.getBlockState(standPos.above()).isAir()
                                && !KoboldGirlEntity.isNearDangerousDrop(level, standPos, 2)
                                && !KoboldGirlEntity.isNearHazard(level, standPos, 2)) {

                            double distSq = standPos.distSqr(current);
                            // Evitar puntos demasiado cercanos (< 5 bloques) o excesivamente lejanos (> 28 bloques)
                            if (distSq >= 25.0D && distSq <= 784.0D) {
                                double score = isOnPath ? 100.0D : 70.0D;

                                // 1. Asignación por sector angular (si hay más de 1 grupo, patrullar áreas distintas)
                                if (totalSquads > 1) {
                                    double vecX = standPos.getX() - anchor.getX();
                                    double vecZ = standPos.getZ() - anchor.getZ();
                                    double pointAngle = Math.atan2(vecZ, vecX);
                                    if (pointAngle < 0) pointAngle += 2.0D * Math.PI;

                                    double angleDiff = Math.abs(pointAngle - targetSectorAngle);
                                    if (angleDiff > Math.PI) angleDiff = 2.0D * Math.PI - angleDiff;

                                    // Bonificación si el punto está en el sector designado de este escuadrón (+60.0D a -60.0D)
                                    double sectorBonus = (1.0D - (angleDiff / (Math.PI / 2.0D))) * 60.0D;
                                    score += sectorBonus;
                                }

                                // 2. Repulsión inter-escuadrones: Evitar coincidir en el mismo sector con otros líderes
                                for (KoboldGirlEntity otherLeader : otherLeaders) {
                                    double distToOtherLeaderSq = standPos.distSqr(otherLeader.blockPosition());
                                    if (distToOtherLeaderSq < 400.0D) { // < 20 bloques
                                        score -= 80.0D;
                                    } else {
                                        score += 15.0D;
                                    }
                                }

                                // 3. Penalizar volver inmediatamente al punto anterior para fomentar patrulla continua en circuito
                                if (this.lastPatrolPos != null) {
                                    double distToLastSq = standPos.distSqr(this.lastPatrolPos);
                                    if (distToLastSq < 36.0D) {
                                        score -= 40.0D;
                                    }
                                }

                                // 4. Si la mob pertenece a una tribu con reina o tiene cama, priorizar no alejarse más de 45 bloques de su base
                                if (anchor != null) {
                                    double distToAnchorSq = standPos.distSqr(anchor);
                                    if (distToAnchorSq > 45.0D * 45.0D) {
                                        score -= 50.0D;
                                    }
                                }

                                // Variación aleatoria para dinamismo natural
                                score += this.mob.getRandom().nextDouble() * 15.0D;

                                if (score > bestScore) {
                                    bestScore = score;
                                    bestPos = standPos.immutable();
                                }
                            }
                        }
                    }
                }
            }
        }

        // Si no se encontraron caminos de tierra cerca, patrullar el sector radial alrededor de la base
        if (bestPos == null) {
            for (int attempt = 0; attempt < 8; attempt++) {
                double angleVariation = (this.mob.getRandom().nextDouble() - 0.5D) * (Math.PI / 2.0D);
                double angle = targetSectorAngle + angleVariation;
                double r = 8.0D + this.mob.getRandom().nextDouble() * 14.0D;
                int px = anchor.getX() + (int) (Math.cos(angle) * r);
                int pz = anchor.getZ() + (int) (Math.sin(angle) * r);
                BlockPos pPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(px, anchor.getY(), pz));

                if (level.getBlockState(pPos).isAir() && !KoboldGirlEntity.isNearDangerousDrop(level, pPos, 2) && !KoboldGirlEntity.isNearHazard(level, pPos, 2)) {
                    bestPos = pPos.immutable();
                    break;
                }
            }
        }

        return bestPos;
    }
}
