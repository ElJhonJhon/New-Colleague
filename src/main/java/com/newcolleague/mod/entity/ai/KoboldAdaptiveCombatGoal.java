package com.newcolleague.mod.entity.ai;

import com.newcolleague.mod.entity.KoboldGirlEntity;
import com.newcolleague.mod.entity.AbstractTribalEntity.KoboldProfession;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

/**
 * Meta: Combate Adaptable según el arma equipada (o Magia Pura para la Reina)
 */
public class KoboldAdaptiveCombatGoal extends Goal {
    private final KoboldGirlEntity mob;
    private int attackCooldown = 0;
    private int leapCooldown = 0;
    private boolean isLeapingForSmash = false;
    private int strafeChangeTimer = 0;
    private boolean strafeClockwise = false;
    private int rangedChargeTicks = 0;
    private int obstructedAngleTimer = 0;
    private int heldBowObstructedTicks = 0;

    // Variables de combate mágico exclusivo de la Reina
    private int queenSpellCooldown = 0;
    private int queenBeamChannelTicks = 0;
    private int queenAbsorptionCooldown = 0;
    private int queenRegenCooldown = 0;
    private int queenFireballBurstRemaining = 0;
    private int queenFireballBurstInterval = 0;

    public KoboldAdaptiveCombatGoal(KoboldGirlEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.mob.isSleeping()) {
            return false;
        }
        LivingEntity target = this.mob.getTarget();
        if (target == null || !target.isAlive()) return false;
        if (target instanceof KoboldGirlEntity otherKobold) {
            return this.mob.canFightKobold(otherKobold);
        }
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.mob.isSleeping()) {
            return false;
        }
        LivingEntity target = this.mob.getTarget();
        if (target == null || !target.isAlive()) return false;
        if (target instanceof KoboldGirlEntity otherKobold) {
            return this.mob.canFightKobold(otherKobold);
        }
        return true;
    }

    @Override
    public void start() {
        this.attackCooldown = 0;
        this.leapCooldown = 0;
        this.rangedChargeTicks = 0;
        this.obstructedAngleTimer = 0;
        this.heldBowObstructedTicks = 0;
        this.isLeapingForSmash = false;
        this.queenSpellCooldown = 0;
        if (this.queenBeamChannelTicks > 0) {
            this.mob.stopTriggeredAnim("attack", "magic_laser");
        }
        this.queenBeamChannelTicks = 0;
        this.queenFireballBurstRemaining = 0;
        this.queenFireballBurstInterval = 0;

        LivingEntity target = this.mob.getTarget();
        if (this.mob.isAnyQueen()) {
            this.mob.unequipHands(); // La reina combate exclusivamente con magia pura
        } else if (target instanceof Animal) {
            this.mob.equipForCombat(false); // Caza de animales: solo arma, sin escudo
        } else {
            this.mob.equipForCombat(true);  // Amenazas hostiles: arma y escudo
        }
    }

    @Override
    public void stop() {
        this.isLeapingForSmash = false;
        if (this.queenBeamChannelTicks > 0) {
            this.mob.stopTriggeredAnim("attack", "magic_laser");
        }
        this.queenBeamChannelTicks = 0;
        this.queenFireballBurstRemaining = 0;
        this.rangedChargeTicks = 0;
        this.obstructedAngleTimer = 0;
        this.heldBowObstructedTicks = 0;
        if (this.mob.isUsingItem()) {
            this.mob.stopUsingItem();
            this.mob.stopTriggeredAnim("attack", "bowattack");
        }
        this.mob.unequipHands();
        this.mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target == null || !target.isAlive() || target.getHealth() <= 0.0F) {
            this.rangedChargeTicks = 0;
            this.obstructedAngleTimer = 0;
            this.heldBowObstructedTicks = 0;
            if (this.mob.isUsingItem()) {
                this.mob.stopUsingItem();
                this.mob.stopTriggeredAnim("attack", "bowattack");
            }
            if (this.queenBeamChannelTicks > 0) {
                this.mob.stopTriggeredAnim("attack", "magic_laser");
                this.queenBeamChannelTicks = 0;
            }
            if (this.mob.level() instanceof ServerLevel serverLevel) {
                this.mob.onSlainTargetDrops(serverLevel, target);
            }
            return;
        }

        this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (this.mob.isMagicUser() || (isRangedWeapon(this.mob.getMainHandItem()) && this.mob.hasArrows())) {
            this.faceTargetDirectly(target);
        }

        double dist = Math.sqrt(this.mob.distanceToSqr(target));

        if (this.attackCooldown > 0) this.attackCooldown--;
        if (this.leapCooldown > 0) this.leapCooldown--;

        // Si es la Reina o Hechicera: Táctica de combate puramente mágica (sin armas cuerpo a cuerpo ni a distancia)
        if (this.mob.isMagicUser()) {
            this.tickQueenMagicTactic(target, dist);
            return;
        }

        // Uso defensivo del escudo si está amenazada por un enemigo no animal
        if (!(target instanceof Animal) && KoboldGirlEntity.isShield(this.mob.getOffhandItem())) {
            if (dist <= 3.5D && this.attackCooldown > 4) {
                if (!this.mob.isUsingItem()) {
                    this.mob.startUsingItem(InteractionHand.OFF_HAND);
                }
            } else if (this.mob.isUsingItem() && this.mob.getUsedItemHand() == InteractionHand.OFF_HAND) {
                this.mob.stopUsingItem();
            }
        } else if (this.mob.isUsingItem() && this.mob.getUsedItemHand() == InteractionHand.OFF_HAND) {
            this.mob.stopUsingItem();
        }

        ItemStack held = this.mob.getMainHandItem();

        // Comportamiento táctico según tipo de arma
        if (isRangedWeapon(held) && this.mob.hasArrows()) {
            this.tickRangedTactic(target, dist, held);
        } else {
            if (this.rangedChargeTicks > 0) {
                this.rangedChargeTicks = 0;
                if (this.mob.isUsingItem() && this.mob.getUsedItemHand() == InteractionHand.MAIN_HAND) {
                    this.mob.stopUsingItem();
                }
                this.mob.stopTriggeredAnim("attack", "bowattack");
            }
            if (isMace(held)) {
                this.tickMaceTactic(target, dist);
            } else if (isSpear(held)) {
                this.tickSpearTactic(target, dist);
            } else {
                this.tickDefaultMeleeTactic(target, dist, held);
            }
        }
    }

    // --- Táctica de Arco / Ballesta (Rango y distancia) ---
    private void tickRangedTactic(LivingEntity target, double dist, ItemStack held) {
        this.faceTargetDirectly(target);

        boolean canSee = this.mob.hasLineOfSight(target);
        boolean obstructed = this.isLineOfFireObstructed(target);
        boolean isCrossbow = held.getItem() instanceof CrossbowItem;
        // Sincronización exacta con la animación bowattack (duración 1.29s = 26 ticks; tensión máxima a 0.96s = 19 ticks)
        int releaseTick = isCrossbow ? 22 : 19;
        int animEndTick = isCrossbow ? 28 : 26;

        // Manejo de movimiento táctico: retroceso encarando, strafing y búsqueda activa de ángulo despejado
        if (obstructed) {
            this.obstructedAngleTimer++;
            if (this.obstructedAngleTimer > 12 && this.obstructedAngleTimer % 12 == 0) {
                this.strafeClockwise = !this.strafeClockwise;
            }
            // Si hay un aliado, jugador u otro mob intermedio, detener navegación frontal y
            // maniobrar lateralmente a velocidad rápida para despejar la línea de tiro
            this.mob.getNavigation().stop();
            this.faceTargetDirectly(target);
            this.mob.getMoveControl().strafe(dist < 5.0D ? -0.6F : (dist > 12.0D ? 0.35F : 0.0F), this.strafeClockwise ? 0.85F : -0.85F);
        } else {
            this.obstructedAngleTimer = 0;
            if (dist < 6.0D) {
                // Retroceder de espaldas al movimiento pero de frente al enemigo (backpedal sin darle la espalda)
                this.mob.getNavigation().stop();
                this.faceTargetDirectly(target);
                this.mob.getMoveControl().strafe(-0.75F, this.strafeClockwise ? 0.35F : -0.35F);
            } else if (dist > 14.0D) {
                // Avanzar hacia el objetivo encarando
                this.mob.getNavigation().moveTo(target, 1.25D);
                this.faceTargetDirectly(target);
            } else {
                // Distancia óptima (6 a 14 bloques): Strafing evasivo
                this.mob.getNavigation().stop();
                this.faceTargetDirectly(target);
                this.strafeChangeTimer++;
                if (this.strafeChangeTimer > 25) {
                    this.strafeClockwise = !this.strafeClockwise;
                    this.strafeChangeTimer = 0;
                }
                this.mob.getMoveControl().strafe(0.0F, this.strafeClockwise ? 0.55F : -0.55F);
            }
        }

        // Manejo de tensión del arco / ballesta y disparo sincronizado
        if (this.rangedChargeTicks > 0) {
            this.faceTargetDirectly(target);

            // Si la línea de tiro está obstruida justo al llegar al momento de disparar
            if (this.rangedChargeTicks == releaseTick && obstructed) {
                this.heldBowObstructedTicks++;
                // Mantener el arco tensado al máximo mientras se maniobra buscando el ángulo (hasta 40 ticks / 2s)
                if (this.heldBowObstructedTicks < 40) {
                    if (!this.mob.isUsingItem()) {
                        this.mob.startUsingItem(InteractionHand.MAIN_HAND);
                    }
                    return; // Esperar al siguiente tick mientras se strafea buscando el ángulo despejado
                } else {
                    // Tiempo agotado sin lograr ángulo limpio: abortar disparo y reubicarse
                    if (this.mob.isUsingItem()) {
                        this.mob.stopUsingItem();
                    }
                    this.mob.stopTriggeredAnim("attack", "bowattack");
                    this.rangedChargeTicks = 0;
                    this.heldBowObstructedTicks = 0;
                    this.attackCooldown = 15;
                    return;
                }
            }

            this.rangedChargeTicks++;

            // Mantener item en uso durante la fase de tensado previa al disparo
            if (this.rangedChargeTicks < releaseTick && !this.mob.isUsingItem()) {
                this.mob.startUsingItem(InteractionHand.MAIN_HAND);
            }

            if (!canSee && this.rangedChargeTicks > 60) {
                if (this.mob.isUsingItem()) {
                    this.mob.stopUsingItem();
                }
                this.mob.stopTriggeredAnim("attack", "bowattack");
                this.rangedChargeTicks = 0;
                this.heldBowObstructedTicks = 0;
            } else if (this.rangedChargeTicks == releaseTick) {
                this.heldBowObstructedTicks = 0;
                this.faceTargetDirectly(target);

                // Momento exacto de tensión máxima (0.96s / tick 19) -> Salida de la flecha
                ItemStack arrowStack = this.mob.consumeOneArrow();
                if (arrowStack.isEmpty()) {
                    arrowStack = new ItemStack(Items.ARROW);
                }
                Arrow arrow = new Arrow(this.mob.level(), this.mob, arrowStack, held);
                if (this.mob.getProfession() == KoboldProfession.VIGILANT) {
                    arrow.setBaseDamage(2.0D * 1.05D);
                    arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
                }
                double dx = target.getX() - this.mob.getX();
                double dy = target.getY(0.33D) - arrow.getY();
                double dz = target.getZ() - this.mob.getZ();
                double hDist = Math.sqrt(dx * dx + dz * dz);
                float velocity = isCrossbow ? 3.15F : 1.6F;
                double arcCompensation = isCrossbow ? (hDist * 0.08D) : (hDist * 0.18D);
                arrow.shoot(dx, dy + arcCompensation, dz, velocity, 1.2F);
                SoundEvent shootSound = isCrossbow ? SoundEvents.CROSSBOW_SHOOT : SoundEvents.ARROW_SHOOT;
                this.mob.playSound(shootSound, 1.0F, 1.0F / (this.mob.getRandom().nextFloat() * 0.4F + 1.2F) + 0.5F);
                this.mob.level().addFreshEntity(arrow);

                if (this.mob.isUsingItem()) {
                    this.mob.stopUsingItem();
                }
                // La animación continúa reproduciendo los frames de soltado y recuperación (de 0.96s a 1.29s)
            } else if (this.rangedChargeTicks >= animEndTick) {
                // Fin completo de la animación (1.29s / tick 26) -> Reinicio del ciclo y enfriamiento
                this.rangedChargeTicks = 0;
                this.heldBowObstructedTicks = 0;
                this.attackCooldown = isCrossbow ? 25 : 18;

                // Si se quedó sin flechas, cambiar de arma de inmediato
                if (!this.mob.hasArrows()) {
                    this.mob.equipBestWeapon();
                }
            }
        } else if (this.attackCooldown <= 0 && canSee && !obstructed) {
            // Iniciar la tensión del arco / ballesta solo cuando la línea de tiro esté despejada
            this.faceTargetDirectly(target);
            this.rangedChargeTicks = 1;
            this.heldBowObstructedTicks = 0;
            this.mob.startUsingItem(InteractionHand.MAIN_HAND);
            this.mob.triggerAnim("attack", "bowattack");
        }
    }

    // --- Táctica de Mazo (Salto aéreo y Smash Attack de impacto) ---
    private void tickMaceTactic(LivingEntity target, double dist) {
        // Salto acrobático de Mazo desde 4 a 7.5 bloques en el suelo
        if (dist >= 4.0D && dist <= 7.5D && this.mob.onGround() && this.leapCooldown <= 0) {
            double dx = target.getX() - this.mob.getX();
            double dz = target.getZ() - this.mob.getZ();
            double horiz = Math.max(0.1D, Math.sqrt(dx * dx + dz * dz));
            this.mob.setDeltaMovement((dx / horiz) * 0.48D, 0.82D, (dz / horiz) * 0.48D);
            this.mob.hurtMarked = true;
            this.isLeapingForSmash = true;
            this.leapCooldown = 80;
            this.mob.playSound(SoundEvents.WIND_CHARGE_BURST.value(), 1.0F, 1.1F);
        }

        if (this.isLeapingForSmash) {
            this.mob.getNavigation().moveTo(target, 1.4D);
            // Impacto de caída contra el objetivo
            if (dist <= 2.8D && (this.mob.getDeltaMovement().y < 0 || this.mob.onGround())) {
                if (this.mob.level() instanceof ServerLevel serverLevel) {
                    if (this.mob.isUsingItem()) {
                        this.mob.stopUsingItem();
                    }
                    float baseDmg = (float) this.mob.getAttributeValue(Attributes.ATTACK_DAMAGE);
                    if (this.mob.getProfession() == KoboldProfession.GUARD) {
                        baseDmg *= 1.05F;
                    }
                    float fallBonus = (float) Math.max(5.0D, this.mob.fallDistance * 3.5D);
                    boolean wasAlive = target.isAlive();
                    target.hurtServer(serverLevel, this.mob.damageSources().mace(this.mob), baseDmg + fallBonus);
                    if (wasAlive && (!target.isAlive() || target.getHealth() <= 0.0F)) {
                        this.mob.onSlainTargetDrops(serverLevel, target);
                    }

                    // Sonido y partículas de Smash de suelo
                    serverLevel.playSound(null, this.mob.getX(), this.mob.getY(), this.mob.getZ(),
                            SoundEvents.MACE_SMASH_GROUND, this.mob.getSoundSource(), 1.5F, 1.0F);
                    serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                            target.getX(), target.getY() + 0.5D, target.getZ(), 2, 0.2D, 0.2D, 0.2D, 0.0D);
                    serverLevel.sendParticles(ParticleTypes.CRIT,
                            target.getX(), target.getY() + 1.0D, target.getZ(), 16, 0.4D, 0.4D, 0.4D, 0.2D);

                    // Empuje en área alrededor del impacto
                    List<LivingEntity> nearby = serverLevel.getEntitiesOfClass(LivingEntity.class,
                            this.mob.getBoundingBox().inflate(3.5D),
                            e -> e != this.mob && !(e instanceof KoboldGirlEntity) && e.isAlive());
                    for (LivingEntity other : nearby) {
                        double kx = other.getX() - this.mob.getX();
                        double kz = other.getZ() - this.mob.getZ();
                        other.knockback(0.7D, -kx, -kz, this.mob.damageSources().mace(this.mob), 1.0F);
                    }

                    this.mob.resetFallDistance();
                    this.isLeapingForSmash = false;
                    this.attackCooldown = 22;
                }
            }
            if (this.mob.onGround()) {
                this.isLeapingForSmash = false;
            }
        } else {
            this.mob.getNavigation().moveTo(target, 1.35D);
            if (dist <= 2.2D && this.attackCooldown <= 0) {
                if (this.mob.level() instanceof ServerLevel serverLevel) {
                    if (this.mob.isUsingItem()) {
                        this.mob.stopUsingItem();
                    }
                    boolean wasAlive = target.isAlive();
                    this.mob.swing(InteractionHand.MAIN_HAND);
                    this.mob.doHurtTarget(serverLevel, target);
                    if (wasAlive && (!target.isAlive() || target.getHealth() <= 0.0F)) {
                        this.mob.onSlainTargetDrops(serverLevel, target);
                    }
                    this.attackCooldown = 20;
                }
            }
        }
    }

    // --- Táctica de Lanza / Arma Cinética / Tridente (Rango extendido y Spacing) ---
    private void tickSpearTactic(LivingEntity target, double dist) {
        this.mob.getNavigation().moveTo(target, 1.35D);

        // Ataque con rango extendido de hasta 3.8 bloques
        if (dist <= 3.8D && this.attackCooldown <= 0) {
            if (this.mob.level() instanceof ServerLevel serverLevel) {
                if (this.mob.isUsingItem()) {
                    this.mob.stopUsingItem();
                }
                boolean wasAlive = target.isAlive();
                this.mob.swing(InteractionHand.MAIN_HAND);
                this.mob.doHurtTarget(serverLevel, target);
                if (wasAlive && (!target.isAlive() || target.getHealth() <= 0.0F)) {
                    this.mob.onSlainTargetDrops(serverLevel, target);
                }
                serverLevel.playSound(null, this.mob.getX(), this.mob.getY(), this.mob.getZ(),
                        SoundEvents.TRIDENT_THROW.value(), this.mob.getSoundSource(), 1.0F, 1.3F);
                serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK,
                        target.getX(), target.getY() + 1.0D, target.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);

                // Maniobra de Spacing: Pequeño salto evasivo hacia atrás para mantener la ventaja de alcance
                double dx = target.getX() - this.mob.getX();
                double dz = target.getZ() - this.mob.getZ();
                double hDist = Math.max(0.1D, Math.sqrt(dx * dx + dz * dz));
                this.mob.setDeltaMovement((-dx / hDist) * 0.35D, 0.22D, (-dz / hDist) * 0.35D);
                this.mob.hurtMarked = true;
                this.attackCooldown = 22;
            }
        }
    }

    // --- Táctica Cuerpo a Cuerpo por defecto (Espadas, Hachas, Puños) ---
    private void tickDefaultMeleeTactic(LivingEntity target, double dist, ItemStack held) {
        this.mob.getNavigation().moveTo(target, 1.35D);

        if (dist <= 2.2D && this.attackCooldown <= 0) {
            if (this.mob.level() instanceof ServerLevel serverLevel) {
                if (this.mob.isUsingItem()) {
                    this.mob.stopUsingItem();
                }
                boolean wasAlive = target.isAlive();
                this.mob.swing(InteractionHand.MAIN_HAND);
                this.mob.doHurtTarget(serverLevel, target);
                if (wasAlive && (!target.isAlive() || target.getHealth() <= 0.0F)) {
                    this.mob.onSlainTargetDrops(serverLevel, target);
                }
                if (held.is(ItemTags.AXES)) {
                    this.attackCooldown = 28;
                    double kx = target.getX() - this.mob.getX();
                    double kz = target.getZ() - this.mob.getZ();
                    target.knockback(0.4D, -kx, -kz, this.mob.damageSources().mobAttack(this.mob), 1.0F);
                } else {
                    this.attackCooldown = 18;
                    serverLevel.playSound(null, this.mob.getX(), this.mob.getY(), this.mob.getZ(),
                            SoundEvents.PLAYER_ATTACK_SWEEP, this.mob.getSoundSource(), 0.8F, 1.0F);
                }
            }
        }
    }

    // --- Táctica de Magia Pura de la Reina (Exhalar Fuego Blaze, Colmillos de Evocador, Rayos de Guardián, Absorción III y Regeneración II) ---
    private void tickQueenMagicTactic(LivingEntity target, double dist) {
        Level level = this.mob.level();
        if (!(level instanceof ServerLevel serverLevel)) return;

        // 1. GESTIÓN DE BUFFS PROPIOS: Absorción III y Regeneración II
        if (this.queenAbsorptionCooldown > 0) this.queenAbsorptionCooldown--;
        if (this.queenRegenCooldown > 0) this.queenRegenCooldown--;

        // Absorción III (+12 HP dorados / 6 corazones dorados)
        if (this.queenAbsorptionCooldown <= 0 && !this.mob.hasEffect(MobEffects.ABSORPTION)) {
            this.mob.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 2, false, true)); // 30s, Nivel 3 (amp 2)
            this.queenAbsorptionCooldown = 400; // 20s cooldown
            serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                    this.mob.getX(), this.mob.getY() + 1.0D, this.mob.getZ(),
                    24, 0.4D, 0.5D, 0.4D, 0.15D);
            serverLevel.playSound(null, this.mob.getX(), this.mob.getY(), this.mob.getZ(),
                    SoundEvents.EVOKER_CAST_SPELL, this.mob.getSoundSource(), 1.0F, 1.2F);
        }

        // Regeneración II (Cura continua cuando su vida baja del 85%)
        if (this.queenRegenCooldown <= 0 && this.mob.getHealth() < this.mob.getMaxHealth() * 0.85F && !this.mob.hasEffect(MobEffects.REGENERATION)) {
            this.mob.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 240, 1, false, true)); // 12s, Nivel 2 (amp 1)
            this.queenRegenCooldown = 360; // 18s cooldown
            serverLevel.sendParticles(ParticleTypes.HEART,
                    this.mob.getX(), this.mob.getY() + 1.2D, this.mob.getZ(),
                    8, 0.3D, 0.4D, 0.3D, 0.05D);
            serverLevel.playSound(null, this.mob.getX(), this.mob.getY(), this.mob.getZ(),
                    SoundEvents.BEACON_POWER_SELECT, this.mob.getSoundSource(), 1.0F, 1.4F);
        }

        // 2. POSICIONAMIENTO Y MANIOBRA TÁCTICA DE MAGA
        this.faceTargetDirectly(target);

        boolean canSee = this.mob.hasLineOfSight(target);
        boolean obstructed = this.isLineOfFireObstructed(target);

        if (obstructed) {
            this.obstructedAngleTimer++;
            if (this.obstructedAngleTimer > 12 && this.obstructedAngleTimer % 12 == 0) {
                this.strafeClockwise = !this.strafeClockwise;
            }
        } else {
            this.obstructedAngleTimer = 0;
        }

        if (this.queenBeamChannelTicks > 0) {
            // Durante la canalización del rayo de guardián se queda concentrada mirando fijamente al objetivo
            this.mob.getNavigation().stop();
            this.faceTargetDirectly(target);
        } else if (this.mob.isInQueenDuel() || this.mob.isInPlayerDuel()) {
            // Posicionamiento táctico en duelo: Mantener distancia suficiente (5.0 a 7.5 bloques)
            // para permitir el uso dinámico y constante de toda la variedad de ataques mágicos
            Vec3 arenaCenter = this.mob.getDuelArenaCenter();
            double barrierRadius = this.mob.getDuelArenaBarrierRadius();
            double myDistFromCenter = arenaCenter != null ? Math.sqrt(Math.pow(this.mob.getX() - arenaCenter.x, 2) + Math.pow(this.mob.getZ() - arenaCenter.z, 2)) : 0.0D;

            if (obstructed) {
                // En duelo: buscar un ángulo de ataque despejado strafeando en arco circular encarando al objetivo
                this.mob.getNavigation().stop();
                this.faceTargetDirectly(target);
                this.mob.getMoveControl().strafe(0.0F, this.strafeClockwise ? 0.8F : -0.8F);
            } else if (dist < 5.0D) {
                // Demasiado cerca: Retroceder activamente encarando al oponente (backpedal con strafe sin darle la espalda)
                this.mob.getNavigation().stop();
                this.faceTargetDirectly(target);
                if (arenaCenter != null && myDistFromCenter > barrierRadius - 1.2D) {
                    this.mob.getMoveControl().strafe(0.0F, this.strafeClockwise ? 0.75F : -0.75F);
                } else {
                    this.mob.getMoveControl().strafe(-0.75F, this.strafeClockwise ? 0.35F : -0.35F);
                }
            } else if (dist > 8.0D) {
                // Si está más lejos del rango ideal de la arena, avanzar suavemente encarando
                this.mob.getNavigation().moveTo(target, 1.15D);
                this.faceTargetDirectly(target);
            } else {
                // Distancia ideal de combate (5.0 a 8.0 bloques): Detener navegación frontal y realizar evasión lateral encarando
                this.mob.getNavigation().stop();
                this.faceTargetDirectly(target);
                this.strafeChangeTimer++;
                if (this.strafeChangeTimer > 25) {
                    this.strafeClockwise = !this.strafeClockwise;
                    this.strafeChangeTimer = 0;
                }
                this.mob.getMoveControl().strafe(0.0F, this.strafeClockwise ? 0.45F : -0.45F);
            }
        } else {
            // Distancia óptima de combate mágico (fuera de duelo en campo abierto: 5.5 a 14 bloques)
            if (obstructed) {
                this.mob.getNavigation().stop();
                this.faceTargetDirectly(target);
                this.mob.getMoveControl().strafe(dist < 5.0D ? -0.5F : (dist > 12.0D ? 0.35F : 0.0F), this.strafeClockwise ? 0.8F : -0.8F);
            } else if (dist < 5.5D) {
                this.mob.getNavigation().stop();
                this.faceTargetDirectly(target);
                this.mob.getMoveControl().strafe(-0.75F, this.strafeClockwise ? 0.4F : -0.4F);
            } else if (dist > 15.0D) {
                this.mob.getNavigation().moveTo(target, 1.25D);
                this.faceTargetDirectly(target);
            } else {
                this.mob.getNavigation().stop();
                this.faceTargetDirectly(target);
                this.strafeChangeTimer++;
                if (this.strafeChangeTimer > 25) {
                    this.strafeClockwise = !this.strafeClockwise;
                    this.strafeChangeTimer = 0;
                }
                this.mob.getMoveControl().strafe(0.0F, this.strafeClockwise ? 0.45F : -0.45F);
            }
        }

        // 3. CONTINUACIÓN DE RÁFAGA DE FUEGO (SmallFireball burst)
        if (this.queenFireballBurstRemaining > 0) {
            this.faceTargetDirectly(target);
            this.queenFireballBurstInterval--;
            if (this.queenFireballBurstInterval <= 0) {
                if (!obstructed) {
                    this.queenFireballBurstInterval = 6;
                    this.queenFireballBurstRemaining--;
                    this.castSingleBlazeFireball(serverLevel, target);
                } else {
                    // Si la línea se obstruyó durante la ráfaga, strafea para despejar el tiro antes de soltar la siguiente bola
                    this.mob.getNavigation().stop();
                    this.faceTargetDirectly(target);
                    this.mob.getMoveControl().strafe(0.0F, this.strafeClockwise ? 0.8F : -0.8F);
                }
            }
            return;
        }

        // 4. CANALIZACIÓN DEL RAYO DEL GUARDIÁN DEL OCÉANO
        if (this.queenBeamChannelTicks > 0) {
            this.queenBeamChannelTicks++;
            this.faceTargetDirectly(target);

            // Efecto visual y sonoro denso del rayo del guardián
            Vec3 from = this.mob.getEyePosition().add(0.0D, -0.2D, 0.0D);
            Vec3 to = target.getBoundingBox().getCenter();
            Vec3 beamVec = to.subtract(from);
            double beamLen = beamVec.length();
            Vec3 beamDir = beamVec.normalize();

            int steps = (int) (beamLen * 3.5D);
            for (int i = 0; i <= steps; i++) {
                double progress = (double) i / (double) steps;
                double px = from.x + beamDir.x * (progress * beamLen);
                double py = from.y + beamDir.y * (progress * beamLen);
                double pz = from.z + beamDir.z * (progress * beamLen);
                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, px, py, pz, 1, 0.02D, 0.02D, 0.02D, 0.0D);
                if (i % 3 == 0) {
                    serverLevel.sendParticles(ParticleTypes.GLOW, px, py, pz, 1, 0.03D, 0.03D, 0.03D, 0.0D);
                }
                if (i % 5 == 0) {
                    serverLevel.sendParticles(ParticleTypes.NAUTILUS, px, py, pz, 1, 0.05D, 0.05D, 0.05D, 0.0D);
                }
            }

            if (this.queenBeamChannelTicks % 5 == 0) {
                float pitch = 0.85F + (this.queenBeamChannelTicks / 20.0F) * 0.75F;
                serverLevel.playSound(null, this.mob.getX(), this.mob.getY(), this.mob.getZ(),
                        SoundEvents.GUARDIAN_ATTACK, this.mob.getSoundSource(), 1.0F, pitch);
            }

            // Culminación del rayo: Impacto y explosión mágica sincronizada al finalizar la animación (20 ticks / 1.0 segundo)
            if (this.queenBeamChannelTicks >= 20) {
                this.mob.stopTriggeredAnim("attack", "magic_laser");
                this.queenBeamChannelTicks = 0;
                this.queenSpellCooldown = 25; // Cooldown post-rayo más dinámico

                boolean wasAlive = target.isAlive();
                target.hurtServer(serverLevel, this.mob.damageSources().indirectMagic(this.mob, this.mob), 9.0F);
                if (wasAlive && (!target.isAlive() || target.getHealth() <= 0.0F)) {
                    this.mob.onSlainTargetDrops(serverLevel, target);
                }

                serverLevel.playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.ELDER_GUARDIAN_CURSE, this.mob.getSoundSource(), 1.2F, 1.3F);
                serverLevel.playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.TRIDENT_THUNDER.value(), this.mob.getSoundSource(), 1.0F, 1.4F);
                serverLevel.sendParticles(ParticleTypes.EXPLOSION, target.getX(), target.getY() + 1.0D, target.getZ(), 2, 0.2D, 0.2D, 0.2D, 0.0D);
                serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, target.getX(), target.getY() + 1.0D, target.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
            return;
        }

        // 5. SELECCIÓN Y LANZAMIENTO DE HECHIZOS OFENSIVOS
        if (this.queenSpellCooldown > 0) {
            this.queenSpellCooldown--;
            return;
        }

        if (!canSee) {
            this.mob.getNavigation().moveTo(target, 1.25D);
            this.faceTargetDirectly(target);
            return;
        }

        // A. Evoker Fangs si el objetivo está muy cerca (< 4.0 bloques): Anillo defensivo de colmillos en 360 grados
        if (dist <= 4.0D) {
            this.faceTargetDirectly(target);
            this.castEvokerFangsRing(serverLevel);
            this.queenSpellCooldown = 28;
            return;
        }

        // Si la línea de tiro está obstruida, no lanzar hechizos direccionales; buscar ángulo despejado primero
        if (obstructed) {
            return;
        }

        // B. Rango de combate medio/largo con ángulo despejado: Variedad completa de ataques
        this.faceTargetDirectly(target);
        float roll = this.mob.getRandom().nextFloat();
        if (roll < 0.35F && dist >= 4.0D && dist <= 20.0D) {
            // Hechizo 1: Iniciar canalización del Rayo del Guardián del Océano
            this.queenBeamChannelTicks = 1;
            this.mob.triggerMagicLaserAnimation();
            serverLevel.playSound(null, this.mob.getX(), this.mob.getY(), this.mob.getZ(),
                    SoundEvents.GUARDIAN_ATTACK, this.mob.getSoundSource(), 1.0F, 0.8F);
        } else if (roll < 0.70F && dist <= 16.0D) {
            // Hechizo 2: Colmillos de Evocador en hilera hacia el objetivo
            this.castEvokerFangsLine(serverLevel, target, dist);
            this.queenSpellCooldown = 26;
        } else {
            // Hechizo 3: Exhalar Fuego similar a un Blaze (ráfaga de bolas de fuego + llamarada)
            this.castBlazeFireAttack(serverLevel, target, dist);
            this.queenSpellCooldown = 28;
        }
    }

    private void castEvokerFangsRing(ServerLevel serverLevel) {
        LivingEntity target = this.mob.getTarget();
        if (target != null && target.isAlive()) {
            this.faceTargetDirectly(target);
        }
        this.mob.triggerMagicFangCirculoAnimation();
        serverLevel.playSound(null, this.mob.getX(), this.mob.getY(), this.mob.getZ(),
                SoundEvents.EVOKER_PREPARE_ATTACK, this.mob.getSoundSource(), 1.0F, 1.2F);
        serverLevel.sendParticles(ParticleTypes.ENCHANT,
                this.mob.getX(), this.mob.getY() + 0.8D, this.mob.getZ(),
                16, 0.4D, 0.4D, 0.4D, 0.2D);

        for (int i = 0; i < 8; i++) {
            float angle = (float) (i * Math.PI / 4.0D);
            double fx = this.mob.getX() + Math.cos(angle) * 2.2D;
            double fz = this.mob.getZ() + Math.sin(angle) * 2.2D;
            BlockPos targetGround = serverLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BlockPos.containing(fx, this.mob.getY(), fz));
            double fy = targetGround.getY();
            EvokerFangs fangs = new EvokerFangs(serverLevel, fx, fy, fz, (float) Math.toDegrees(angle), 1, this.mob);
            serverLevel.addFreshEntity(fangs);
        }
    }

    private void castEvokerFangsLine(ServerLevel serverLevel, LivingEntity target, double dist) {
        this.faceTargetDirectly(target);
        this.mob.triggerMagicFangLineaAnimation();
        serverLevel.playSound(null, this.mob.getX(), this.mob.getY(), this.mob.getZ(),
                SoundEvents.EVOKER_PREPARE_ATTACK, this.mob.getSoundSource(), 1.0F, 1.0F);
        serverLevel.sendParticles(ParticleTypes.WITCH,
                this.mob.getX(), this.mob.getY() + 1.2D, this.mob.getZ(),
                14, 0.3D, 0.4D, 0.3D, 0.1D);

        double dx = target.getX() - this.mob.getX();
        double dz = target.getZ() - this.mob.getZ();
        double length = Math.max(0.1D, Math.sqrt(dx * dx + dz * dz));
        double nx = dx / length;
        double nz = dz / length;
        float yaw = (float) (Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;

        int fangCount = Math.min(12, Math.max(4, (int) dist + 2));
        for (int i = 1; i <= fangCount; i++) {
            double fx = this.mob.getX() + nx * (i * 1.35D);
            double fz = this.mob.getZ() + nz * (i * 1.35D);
            BlockPos targetGround = serverLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BlockPos.containing(fx, this.mob.getY(), fz));
            double fy = targetGround.getY();
            EvokerFangs fangs = new EvokerFangs(serverLevel, fx, fy, fz, yaw, i, this.mob);
            serverLevel.addFreshEntity(fangs);
        }
    }

    private void castBlazeFireAttack(ServerLevel serverLevel, LivingEntity target, double dist) {
        this.faceTargetDirectly(target);
        this.mob.triggerMagicFireAnimation();
        // Exhalación de llamarada frontal con sonido de Blaze
        serverLevel.playSound(null, this.mob.getX(), this.mob.getY(), this.mob.getZ(),
                SoundEvents.BLAZE_SHOOT, this.mob.getSoundSource(), 1.0F, 1.0F / (this.mob.getRandom().nextFloat() * 0.4F + 0.8F));

        Vec3 eye = this.mob.getEyePosition().add(0.0D, -0.1D, 0.0D);
        Vec3 dir = target.getBoundingBox().getCenter().subtract(eye).normalize();

        // Partículas cónicas de fuego exhalado
        for (int i = 0; i < 15; i++) {
            double spreadX = (this.mob.getRandom().nextDouble() - 0.5D) * 0.3D;
            double spreadY = (this.mob.getRandom().nextDouble() - 0.5D) * 0.3D;
            double spreadZ = (this.mob.getRandom().nextDouble() - 0.5D) * 0.3D;
            serverLevel.sendParticles(ParticleTypes.FLAME,
                    eye.x + dir.x * 0.5D, eye.y + dir.y * 0.5D, eye.z + dir.z * 0.5D,
                    1, dir.x * 0.4D + spreadX, dir.y * 0.4D + spreadY, dir.z * 0.4D + spreadZ, 0.05D);
        }

        // Si está a corta distancia (< 6 bloques), la llamarada prende fuego al enemigo y daña inmediatamente
        if (dist < 6.0D) {
            target.igniteForSeconds(4.0F);
            boolean wasAlive = target.isAlive();
            target.hurtServer(serverLevel, this.mob.damageSources().inFire(), 4.0F);
            if (wasAlive && (!target.isAlive() || target.getHealth() <= 0.0F)) {
                this.mob.onSlainTargetDrops(serverLevel, target);
            }
        }

        // Iniciar ráfaga de 3 SmallFireball (estilo Blaze)
        this.queenFireballBurstRemaining = 3;
        this.queenFireballBurstInterval = 0; // El primero sale de inmediato
    }

    private void castSingleBlazeFireball(ServerLevel serverLevel, LivingEntity target) {
        this.faceTargetDirectly(target);
        Vec3 eye = this.mob.getEyePosition().add(0.0D, -0.15D, 0.0D);
        Vec3 targetPos = target.getBoundingBox().getCenter();
        Vec3 dir = targetPos.subtract(eye).normalize().scale(0.75D);

        // Pequeña dispersión para naturalidad
        double sx = (this.mob.getRandom().nextDouble() - 0.5D) * 0.06D;
        double sy = (this.mob.getRandom().nextDouble() - 0.5D) * 0.06D;
        double sz = (this.mob.getRandom().nextDouble() - 0.5D) * 0.06D;
        Vec3 motion = dir.add(sx, sy, sz);

        SmallFireball fireball = new SmallFireball(serverLevel, this.mob, motion);
        fireball.setPos(eye.x + dir.x * 0.5D, eye.y, eye.z + dir.z * 0.5D);
        serverLevel.addFreshEntity(fireball);

        serverLevel.playSound(null, this.mob.getX(), this.mob.getY(), this.mob.getZ(),
                SoundEvents.BLAZE_SHOOT, this.mob.getSoundSource(), 0.9F, 1.1F);
        serverLevel.sendParticles(ParticleTypes.FLAME, eye.x, eye.y, eye.z, 6, 0.1D, 0.1D, 0.1D, 0.05D);
    }

    private void faceTargetDirectly(LivingEntity target) {
        double dx = target.getX() - this.mob.getX();
        double dy = target.getEyeY() - this.mob.getEyeY();
        double dz = target.getZ() - this.mob.getZ();
        double horizDist = Math.sqrt(dx * dx + dz * dz);
        if (horizDist > 0.0001D) {
            float targetYaw = (float) (Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
            float targetPitch = (float) (-(Mth.atan2(dy, horizDist) * (180.0D / Math.PI)));
            this.mob.setYRot(targetYaw);
            this.mob.setYBodyRot(targetYaw);
            this.mob.setYHeadRot(targetYaw);
            this.mob.setXRot(targetPitch);
        }
        this.mob.getLookControl().setLookAt(target, 100.0F, 100.0F);
    }

    private boolean isLineOfFireObstructed(LivingEntity target) {
        Vec3 eyePos = this.mob.getEyePosition();
        Vec3 targetCenter = target.getBoundingBox().getCenter();
        Vec3 ray = targetCenter.subtract(eyePos);
        double rayDist = ray.length();
        if (rayDist < 1.2D) {
            return false;
        }

        double minX = Math.min(eyePos.x, targetCenter.x) - 1.2D;
        double minY = Math.min(eyePos.y, targetCenter.y) - 1.2D;
        double minZ = Math.min(eyePos.z, targetCenter.z) - 1.2D;
        double maxX = Math.max(eyePos.x, targetCenter.x) + 1.2D;
        double maxY = Math.max(eyePos.y, targetCenter.y) + 1.2D;
        double maxZ = Math.max(eyePos.z, targetCenter.z) + 1.2D;
        AABB searchBox = new AABB(minX, minY, minZ, maxX, maxY, maxZ);

        List<LivingEntity> potentialBlockers = this.mob.level().getEntitiesOfClass(LivingEntity.class, searchBox,
                e -> e != this.mob && e != target && e.isAlive() && e.getHealth() > 0.0F && !e.isSpectator() && !e.isPassengerOfSameVehicle(this.mob));

        double rayDistSq = rayDist * rayDist;

        for (LivingEntity blocker : potentialBlockers) {
            AABB blockerBox = blocker.getBoundingBox().inflate(0.25D);
            if (blockerBox.clip(eyePos, targetCenter).isPresent()) {
                return true;
            }

            Vec3 bCenter = blocker.getBoundingBox().getCenter();
            Vec3 toBlocker = bCenter.subtract(eyePos);
            double dot = toBlocker.x * ray.x + toBlocker.y * ray.y + toBlocker.z * ray.z;
            double t = dot / rayDistSq;
            if (t > 0.06D && t < 0.94D) {
                Vec3 projection = eyePos.add(ray.scale(t));
                double distSq = bCenter.distanceToSqr(projection);
                double halfW = blocker.getBbWidth() * 0.5D + 0.35D;
                if (distSq <= halfW * halfW) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isRangedWeapon(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem;
    }

    private static boolean isMace(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.getItem() instanceof MaceItem || stack.is(ItemTags.MACE_ENCHANTABLE);
    }

    private static boolean isSpear(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.has(DataComponents.KINETIC_WEAPON) || stack.is(ItemTags.SPEARS) || stack.getItem() instanceof TridentItem) {
            return true;
        }
        String id = stack.getItem().getDescriptionId().toLowerCase();
        return id.contains("spear") || id.contains("trident") || id.contains("lance") || id.contains("lanza");
    }
}
