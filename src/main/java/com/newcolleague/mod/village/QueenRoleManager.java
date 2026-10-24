package com.newcolleague.mod.village;

import com.newcolleague.mod.entity.AbstractTribalEntity;
import com.newcolleague.mod.entity.AbstractTribalEntity.KoboldProfession;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;

import java.util.ArrayList;
import java.util.List;

/**
 * El Cerebro de la Reina: Administrador dinámico de reasignación periódica de roles
 * según las emergencias y métricas del asentamiento tribal.
 * Funciona para cualquier entidad tribal (Kobold, Goblin, Orc, etc.).
 */
public class QueenRoleManager {

    public static void evaluateAndAssignRoles(AbstractTribalEntity queen, List<AbstractTribalEntity> tribeMembers, VillageStorage storage) {
        if (!queen.isAnyQueen() || !(queen.level() instanceof ServerLevel sl)) return;

        int foodCount = storage.getTotalFood();
        boolean hasActiveProject = storage.hasActiveProject();
        boolean isNightOrThreatened = queen.isNightTime() || queen.hasNearbyHostiles(sl, 32.0D);

        int builderCount = 0;
        int farmerCount = 0;
        int hunterCount = 0;
        int defenderCount = 0;
        int minerCount = 0;
        List<AbstractTribalEntity> civilians = new ArrayList<>();
        List<AbstractTribalEntity> hunters = new ArrayList<>();
        List<AbstractTribalEntity> defenders = new ArrayList<>();

        for (AbstractTribalEntity member : tribeMembers) {
            if (member == queen) continue;
            switch (member.getProfession()) {
                case BUILDER -> builderCount++;
                case FARMER -> farmerCount++;
                case HUNTER -> {
                    hunterCount++;
                    hunters.add(member);
                }
                case GUARD, VIGILANT -> {
                    defenderCount++;
                    defenders.add(member);
                }
                case MINER -> minerCount++;
                case NONE -> civilians.add(member);
                default -> {}
            }
        }

        // Prioridad 1: Defensa Perimetral
        // Al caer la noche o detectar mobs hostiles en un radio de 32 bloques, promociona temporalmente civiles o cazadoras a GUARD o VIGILANT
        if (isNightOrThreatened && defenderCount < 2) {
            AbstractTribalEntity recruit = null;
            if (!civilians.isEmpty()) {
                recruit = civilians.remove(0);
            } else if (!hunters.isEmpty() && hunterCount > 1) {
                recruit = hunters.remove(0);
            }

            if (recruit != null) {
                KoboldProfession newRole = (defenderCount == 0) ? KoboldProfession.GUARD : KoboldProfession.VIGILANT;
                recruit.setProfession(newRole);
                recruit.equipForCombat(true);
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, recruit.getX(), recruit.getY() + 1.2D, recruit.getZ(), 8, 0.2D, 0.2D, 0.2D, 0.05D);
                recruit.playSound(SoundEvents.ARMOR_EQUIP_GENERIC.value(), 1.0F, 1.0F);
                queen.playSound(SoundEvents.VILLAGER_YES, 1.0F, 1.0F);
                return;
            }
        }

        // Normalización de Guardias en tiempo de paz diurno: Si hay más de 2 guardias y se necesita mano de obra
        if (!isNightOrThreatened && defenderCount > 2 && !defenders.isEmpty()) {
            if (hasActiveProject && builderCount < 2) {
                AbstractTribalEntity worker = defenders.remove(0);
                worker.setProfession(KoboldProfession.BUILDER);
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, worker.getX(), worker.getY() + 1.2D, worker.getZ(), 6, 0.2D, 0.2D, 0.2D, 0.05D);
                worker.playSound(SoundEvents.STONE_PLACE, 1.0F, 1.0F);
                queen.playSound(SoundEvents.VILLAGER_YES, 1.0F, 1.0F);
                return;
            }
        }

        // Prioridad 2: Emergencia de Alimento
        // Si la comida total en los cofres es < 12, convierte a las civiles en FARMER o HUNTER
        if (foodCount < 12 && (farmerCount + hunterCount) < 3 && !civilians.isEmpty()) {
            AbstractTribalEntity recruit = civilians.remove(0);
            KoboldProfession newProf = (farmerCount <= hunterCount) ? KoboldProfession.FARMER : KoboldProfession.HUNTER;
            recruit.setProfession(newProf);
            sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, recruit.getX(), recruit.getY() + 1.2D, recruit.getZ(), 8, 0.2D, 0.2D, 0.2D, 0.05D);
            recruit.playSound(SoundEvents.VILLAGER_WORK_FARMER, 1.0F, 1.0F);
            queen.playSound(SoundEvents.VILLAGER_YES, 1.0F, 1.0F);
            return;
        }

        // Prioridad 3: Emergencia de Infraestructura
        // Si hay un blueprint activo (como carpa o torre) y hay menos de 2 constructoras
        if (hasActiveProject && builderCount < 2 && !civilians.isEmpty()) {
            AbstractTribalEntity recruit = civilians.remove(0);
            recruit.setProfession(KoboldProfession.BUILDER);
            sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, recruit.getX(), recruit.getY() + 1.2D, recruit.getZ(), 8, 0.2D, 0.2D, 0.2D, 0.05D);
            recruit.playSound(SoundEvents.STONE_PLACE, 1.0F, 1.0F);
            queen.playSound(SoundEvents.VILLAGER_YES, 1.0F, 1.0F);
            return;
        }

        // Prioridad 4: Apoyo Minero
        // Si hay proyectos de construcción y falta extracción de piedra o minerales
        if (hasActiveProject && minerCount < 1 && civilians.size() >= 2) {
            AbstractTribalEntity recruit = civilians.remove(0);
            recruit.setProfession(KoboldProfession.MINER);
            sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, recruit.getX(), recruit.getY() + 1.2D, recruit.getZ(), 8, 0.2D, 0.2D, 0.2D, 0.05D);
            recruit.playSound(SoundEvents.STONE_BREAK, 1.0F, 1.0F);
            queen.playSound(SoundEvents.VILLAGER_YES, 1.0F, 1.0F);
            return;
        }
    }

    public static void evaluateAndAssignRoles(AbstractTribalEntity queen, List<AbstractTribalEntity> tribeMembers) {
        evaluateAndAssignRoles(queen, tribeMembers, new VillageStorage(queen, 32));
    }
}
