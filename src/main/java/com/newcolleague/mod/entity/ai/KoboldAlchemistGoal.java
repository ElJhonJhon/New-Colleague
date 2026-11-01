package com.newcolleague.mod.entity.ai;

import com.newcolleague.mod.entity.KoboldGirlEntity;
import com.newcolleague.mod.entity.AbstractTribalEntity.CommandStance;
import com.newcolleague.mod.entity.AbstractTribalEntity.KoboldProfession;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

/**
 * Meta de Alquimia y Apoyo Sanador:
 * Exclusiva para la profesión ALCHEMIST (Alquimista).
 * - Recolecta flores silvestres, hongos e ingredientes alquímicos del entorno.
 * - Elabora insumos y brebajes medicinales.
 * - Brinda auxilio y curación activa a aliadas heridas o envenenadas de la tribu.
 * - Almacena excedentes de pociones e insumos en los cofres comunales.
 */
public class KoboldAlchemistGoal extends Goal {

    private final KoboldGirlEntity kobold;
    private final double speedModifier;

    private @Nullable BlockPos currentForageTarget = null;
    private @Nullable KoboldGirlEntity healTarget = null;
    private int actionTicks = 0;
    private int searchCooldown = 0;
    private int healCooldown = 0;

    public KoboldAlchemistGoal(KoboldGirlEntity kobold, double speedModifier) {
        this.kobold = kobold;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.kobold.isSleeping() || this.kobold.isInPowerStruggle()) {
            return false;
        }
        if (this.kobold.getProfession() != KoboldProfession.ALCHEMIST) {
            return false;
        }
        if (this.kobold.isOwned() && this.kobold.getCommandStance() != CommandStance.WORK) {
            return false;
        }
        if (!this.kobold.isWorkScheduleTime() || this.kobold.isSleepTime()) {
            return false;
        }

        if (--this.searchCooldown > 0) {
            return false;
        }
        this.searchCooldown = 20 + this.kobold.getRandom().nextInt(20);

        // 1. Prioridad: Asistir y curar aliadas de la tribu con baja salud
        if (this.healCooldown <= 0) {
            this.healTarget = this.findNearbyInjuredAlly();
            if (this.healTarget != null) {
                return true;
            }
        }

        // 2. Si tiene inventario libre, recolectar flores u hongos cercanos
        if (this.kobold.hasFreeInventorySlot()) {
            this.currentForageTarget = this.findNearbyForageTarget();
            if (this.currentForageTarget != null) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.kobold.isSleeping() || this.kobold.isInPowerStruggle()) return false;
        if (this.healTarget != null && this.healTarget.isAlive() && this.healTarget.getHealth() < this.healTarget.getMaxHealth()) {
            return true;
        }
        return this.currentForageTarget != null && !this.kobold.level().getBlockState(this.currentForageTarget).isAir();
    }

    @Override
    public void start() {
        this.actionTicks = 0;
        if (this.healTarget != null) {
            this.kobold.getNavigation().moveTo(this.healTarget, this.speedModifier);
        } else if (this.currentForageTarget != null) {
            this.kobold.getNavigation().moveTo(
                    this.currentForageTarget.getX() + 0.5D,
                    this.currentForageTarget.getY(),
                    this.currentForageTarget.getZ() + 0.5D,
                    this.speedModifier
            );
        }
    }

    @Override
    public void stop() {
        this.currentForageTarget = null;
        this.healTarget = null;
        this.actionTicks = 0;
        this.kobold.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.healCooldown > 0) this.healCooldown--;

        // Rama A: Auxilio médico a aliada
        if (this.healTarget != null) {
            if (!this.healTarget.isAlive() || this.healTarget.getHealth() >= this.healTarget.getMaxHealth()) {
                this.healTarget = null;
                this.stop();
                return;
            }

            this.kobold.getLookControl().setLookAt(this.healTarget, 30.0F, 30.0F);
            double distSq = this.kobold.distanceToSqr(this.healTarget);

            if (distSq <= 6.25D) {
                this.kobold.getNavigation().stop();
                this.actionTicks++;
                if (this.actionTicks >= 20) {
                    this.actionTicks = 0;
                    this.administerAid(this.healTarget);
                    this.healTarget = null;
                    this.healCooldown = 120; // 6s de enfriamiento entre curaciones
                    this.stop();
                }
            } else if (this.kobold.getNavigation().isDone()) {
                this.kobold.getNavigation().moveTo(this.healTarget, this.speedModifier);
            }
            return;
        }

        // Rama B: Recolección botánica de flores y hongos
        if (this.currentForageTarget != null) {
            Level level = this.kobold.level();
            BlockState state = level.getBlockState(this.currentForageTarget);
            if (state.isAir() || !isForageable(state)) {
                this.currentForageTarget = null;
                this.stop();
                return;
            }

            this.kobold.getLookControl().setLookAt(
                    this.currentForageTarget.getX() + 0.5D,
                    this.currentForageTarget.getY() + 0.5D,
                    this.currentForageTarget.getZ() + 0.5D,
                    30.0F, 30.0F
            );

            double distSq = this.kobold.distanceToSqr(
                    this.currentForageTarget.getX() + 0.5D,
                    this.currentForageTarget.getY() + 0.5D,
                    this.currentForageTarget.getZ() + 0.5D
            );

            if (distSq <= 6.25D) {
                this.kobold.getNavigation().stop();
                this.actionTicks++;
                if (this.actionTicks >= 15) {
                    this.actionTicks = 0;
                    this.harvestForageBlock(level, this.currentForageTarget);
                    this.currentForageTarget = null;
                    this.stop();
                }
            } else if (this.kobold.getNavigation().isDone()) {
                this.kobold.getNavigation().moveTo(
                        this.currentForageTarget.getX() + 0.5D,
                        this.currentForageTarget.getY(),
                        this.currentForageTarget.getZ() + 0.5D,
                        this.speedModifier
                );
            }
        }
    }

    private @Nullable KoboldGirlEntity findNearbyInjuredAlly() {
        List<KoboldGirlEntity> allies = this.kobold.level().getEntitiesOfClass(
                KoboldGirlEntity.class,
                this.kobold.getBoundingBox().inflate(16.0D, 6.0D, 16.0D),
                k -> k != this.kobold && k.isAlive()
                        && (this.kobold.getTribeName().equals(k.getTribeName()) || (this.kobold.isOwned() && k.isOwned()))
                        && (k.getHealth() <= k.getMaxHealth() * 0.65F || k.hasEffect(MobEffects.POISON) || k.hasEffect(MobEffects.WITHER))
        );
        return allies.isEmpty() ? null : allies.get(0);
    }

    private void administerAid(KoboldGirlEntity patient) {
        if (this.kobold.level() instanceof ServerLevel sl) {
            // Curar y otorgar regeneración
            patient.heal(6.0F);
            patient.removeEffect(MobEffects.POISON);
            patient.removeEffect(MobEffects.WITHER);
            patient.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));

            this.kobold.playSound(SoundEvents.BREWING_STAND_BREW, 0.9F, 1.2F);
            patient.playSound(SoundEvents.PLAYER_BURP, 0.8F, 1.0F);

            sl.sendParticles(ParticleTypes.HEART, patient.getX(), patient.getY() + 1.2D, patient.getZ(), 6, 0.3D, 0.3D, 0.3D, 0.05D);
            sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.kobold.getX(), this.kobold.getY() + 1.2D, this.kobold.getZ(), 4, 0.2D, 0.2D, 0.2D, 0.05D);
        }
    }

    private @Nullable BlockPos findNearbyForageTarget() {
        Level level = this.kobold.level();
        BlockPos center = this.kobold.getCampCenterPos();
        if (center == null) center = this.kobold.blockPosition();

        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        int radius = 24;

        for (int dx = -radius; dx <= radius; dx += 2) {
            for (int dz = -radius; dz <= radius; dz += 2) {
                for (int dy = -3; dy <= 4; dy++) {
                    mpos.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    if (this.kobold.isPosInsideAnyOccupiedPlot(mpos.getX(), mpos.getZ())) continue;

                    BlockState state = level.getBlockState(mpos);
                    if (isForageable(state)) {
                        double dist = this.kobold.distanceToSqr(mpos.getX() + 0.5D, mpos.getY(), mpos.getZ() + 0.5D);
                        if (dist < bestDist) {
                            bestDist = dist;
                            best = mpos.immutable();
                        }
                    }
                }
            }
        }
        return best;
    }

    private static boolean isForageable(BlockState state) {
        return state.is(BlockTags.FLOWERS)
                || state.is(Blocks.BROWN_MUSHROOM)
                || state.is(Blocks.RED_MUSHROOM);
    }

    private void harvestForageBlock(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (level instanceof ServerLevel sl) {
            sl.levelEvent(2001, pos, Block.getId(state));
            sl.destroyBlock(pos, true, this.kobold);
            this.kobold.playSound(SoundEvents.GRASS_BREAK, 1.0F, 1.0F);

            // Intentar crafteo alquímico tras recolectar
            this.kobold.tryCraftAlchemySupplies();

            // Almacenar insumos si acumula buena cantidad
            this.tryStoreExcessSupplies(sl);
        }
    }

    private void tryStoreExcessSupplies(ServerLevel sl) {
        int potionOrHerbCount = 0;
        for (int i = 0; i < this.kobold.getInventory().getContainerSize(); i++) {
            ItemStack s = this.kobold.getInventory().getItem(i);
            if (s.is(Items.GLASS_BOTTLE) || s.is(Items.POTION) || s.is(Items.GOLDEN_CARROT)
                    || s.is(Items.GLISTERING_MELON_SLICE) || s.is(Items.GOLDEN_APPLE)
                    || s.is(Items.BROWN_MUSHROOM) || s.is(Items.RED_MUSHROOM)) {
                potionOrHerbCount += s.getCount();
            }
        }
        if (potionOrHerbCount < 12) return;

        BlockPos center = this.kobold.getCampCenterPos();
        BlockPos chestPos = this.kobold.findNearbyUniqueChests(sl, center != null ? center : this.kobold.blockPosition(), 36, 1)
                .stream().findFirst().orElse(null);

        if (chestPos != null) {
            Container chest = KoboldGirlEntity.getChestContainer(sl, chestPos);
            if (chest != null) {
                for (int i = 0; i < this.kobold.getInventory().getContainerSize(); i++) {
                    ItemStack s = this.kobold.getInventory().getItem(i);
                    if (s.is(Items.POTION) || s.is(Items.GOLDEN_CARROT) || s.is(Items.GLISTERING_MELON_SLICE) || s.is(Items.GOLDEN_APPLE)) {
                        ItemStack leftover = KoboldGirlEntity.addItemToContainer(chest, s);
                        this.kobold.getInventory().setItem(i, leftover);
                    }
                }
                this.kobold.playSound(SoundEvents.CHEST_CLOSE, 0.7F, 1.0F);
            }
        }
    }
}
