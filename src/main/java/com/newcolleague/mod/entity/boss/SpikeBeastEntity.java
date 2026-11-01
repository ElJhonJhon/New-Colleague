package com.newcolleague.mod.entity.boss;

import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

/**
 * Bestia con Espinas "Spike":
 * Coloso cuadrúpedo/bípedo recubierto de espinas afiladas (estilo Crash Mind Over Mutant).
 * - Reflejo de daño cuerpo a cuerpo por espinas corporales.
 * - Ataque combo "attack1": Primer golpe a los 0.29s (tick 6), desplazamiento hacia adelante
 *   de 2 bloques entre los 0.29s y 0.58s (ticks 6-12), y segundo golpe pesado a los 0.58s (tick 12).
 */
public class SpikeBeastEntity extends AbstractBossBeastEntity {

    private static final EntityDataAccessor<Boolean> DATA_ATTACKING =
            SynchedEntityData.defineId(SpikeBeastEntity.class, EntityDataSerializers.BOOLEAN);

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ATTACK1_ANIM = RawAnimation.begin().thenPlay("attack1");

    private int attackTickCounter = 0;
    private int attackCooldown = 0;
    private Vec3 dashDirection = Vec3.ZERO;

    public SpikeBeastEntity(EntityType<? extends AbstractBossBeastEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, false);
    }

    public boolean isAttackingWithCombo() {
        return this.entityData.get(DATA_ATTACKING);
    }

    public void setAttackingWithCombo(boolean attacking) {
        this.entityData.set(DATA_ATTACKING, attacking);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createMobAttributes()
                .add(Attributes.MAX_HEALTH, 250.0D)
                .add(Attributes.ATTACK_DAMAGE, 12.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.85D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));

        // Metas de entidad domesticada
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new FollowOwnerGoal(this, 1.2D, 10.0F, 3.0F));

        // Meta de ataque combo especializado para Spike
        this.goalSelector.addGoal(3, new SpikeComboAttackGoal());

        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D) {
            @Override
            public boolean canUse() {
                return !SpikeBeastEntity.this.isStunned() && !SpikeBeastEntity.this.isAttackingWithCombo() && super.canUse();
            }
        });

        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        // Metas de selección de objetivos (Targets)
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this) {
            @Override
            public boolean canUse() {
                return !SpikeBeastEntity.this.isStunned() && super.canUse();
            }
        });

        // Solo ataca jugadores si es salvaje o no está domesticada
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Player.class, true) {
            @Override
            public boolean canUse() {
                return !SpikeBeastEntity.this.isTame() && !SpikeBeastEntity.this.isStunned() && super.canUse();
            }
        });

        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, IronGolem.class, true) {
            @Override
            public boolean canUse() {
                return !SpikeBeastEntity.this.isTame() && !SpikeBeastEntity.this.isStunned() && super.canUse();
            }
        });
    }

    /**
     * Inicia la secuencia de ataque combo con desplazamiento y doble golpe.
     */
    public void startComboAttack(LivingEntity target) {
        if (this.isStunned() || this.isAttackingWithCombo()) return;

        this.setAttackingWithCombo(true);
        this.attackTickCounter = 0;

        // Calcular y fijar la dirección del ataque hacia el objetivo
        if (target != null) {
            Vec3 diff = target.position().subtract(this.position());
            this.dashDirection = new Vec3(diff.x, 0, diff.z).normalize();
        } else {
            Vec3 look = this.getLookAngle();
            this.dashDirection = new Vec3(look.x, 0, look.z).normalize();
        }

        this.getNavigation().stop();
    }

    public void stopComboAttack() {
        this.setAttackingWithCombo(false);
        this.attackTickCounter = 0;
        this.attackCooldown = 25 + this.random.nextInt(15); // 1.25s a 2.0s de cooldown tras terminar el combo
    }

    @Override
    public void tick() {
        super.tick();

        if (this.attackCooldown > 0) {
            this.attackCooldown--;
        }

        // Ejecución sincronizada del combo de ataque en el servidor
        if (!this.level().isClientSide() && this.isAttackingWithCombo()) {
            if (this.isStunned()) {
                this.stopComboAttack();
                return;
            }

            if (this.level() instanceof ServerLevel sl) {
                this.attackTickCounter++;

                // 1. Primer Golpe a los 0.29 segundos (~6 ticks)
                if (this.attackTickCounter == 6) {
                    this.executeFirstHit(sl);
                }

                // 2. Desplazamiento hacia adelante de 2 bloques entre los 0.29s y 0.58s (ticks 6 a 12 = 6 ticks)
                if (this.attackTickCounter >= 6 && this.attackTickCounter <= 12) {
                    this.executeForwardDash(sl);
                }

                // 3. Segundo Golpe a los 0.58 segundos (~12 ticks)
                if (this.attackTickCounter == 12) {
                    this.executeSecondHit(sl);
                }

                // 4. Fin de la animación y recuperación (longitud total: 1.25s = 25 ticks)
                if (this.attackTickCounter >= 25) {
                    this.stopComboAttack();
                }
            }
        }
    }

    /**
     * Primer golpe del combo: barrido inicial que impacta a los objetivos frente a Spike.
     */
    private void executeFirstHit(ServerLevel sl) {
        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 1.4F, 1.1F);
        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.RAVAGER_ATTACK, SoundSource.HOSTILE, 1.0F, 1.2F);

        Vec3 frontPos = this.position().add(this.dashDirection.scale(1.8D)).add(0, 1.2D, 0);
        sl.sendParticles(ParticleTypes.SWEEP_ATTACK, frontPos.x, frontPos.y, frontPos.z, 2, 0.4D, 0.2D, 0.4D, 0.0D);

        AABB hitBox = this.getBoundingBox().inflate(1.2D).move(this.dashDirection.scale(1.2D));
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, hitBox, this::canDamageTarget);

        DamageSource source = this.damageSources().mobAttack(this);
        for (LivingEntity victim : targets) {
            victim.hurtServer(sl, source, 10.0F);
            victim.knockback(0.45D, -this.dashDirection.x, -this.dashDirection.z, source, 10.0F);
            sl.sendParticles(ParticleTypes.CRIT, victim.getX(), victim.getY() + 1.0D, victim.getZ(), 8, 0.3D, 0.3D, 0.3D, 0.1D);
        }
    }

    /**
     * Desplaza a la bestia hacia adelante un total de 2 bloques repartidos en 6 ticks (~0.36 bloques/tick).
     */
    private void executeForwardDash(ServerLevel sl) {
        double speed = 0.36D;
        this.setDeltaMovement(this.dashDirection.x * speed, this.getDeltaMovement().y, this.dashDirection.z * speed);
        this.hurtMarked = true;

        sl.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 0.15D, this.getZ(), 2, 0.4D, 0.05D, 0.4D, 0.02D);
        if (this.attackTickCounter % 3 == 0) {
            sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GENERIC_BIG_FALL, SoundSource.HOSTILE, 0.5F, 1.5F);
        }
    }

    /**
     * Segundo golpe del combo: impacto demoledor que remata el desplazamiento con alto daño y empuje.
     */
    private void executeSecondHit(ServerLevel sl) {
        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.IRON_GOLEM_ATTACK, SoundSource.HOSTILE, 1.5F, 0.85F);
        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.RAVAGER_ATTACK, SoundSource.HOSTILE, 1.3F, 0.9F);

        Vec3 frontPos = this.position().add(this.dashDirection.scale(2.2D)).add(0, 1.2D, 0);
        sl.sendParticles(ParticleTypes.SWEEP_ATTACK, frontPos.x, frontPos.y, frontPos.z, 3, 0.5D, 0.2D, 0.5D, 0.0D);
        sl.sendParticles(ParticleTypes.CRIT, frontPos.x, frontPos.y, frontPos.z, 20, 0.6D, 0.4D, 0.6D, 0.15D);

        AABB hitBox = this.getBoundingBox().inflate(1.5D).move(this.dashDirection.scale(1.5D));
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, hitBox, this::canDamageTarget);

        DamageSource source = this.damageSources().mobAttack(this);
        for (LivingEntity victim : targets) {
            victim.hurtServer(sl, source, 14.0F);
            victim.knockback(0.85D, -this.dashDirection.x, -this.dashDirection.z, source, 14.0F);
            sl.sendParticles(ParticleTypes.EXPLOSION, victim.getX(), victim.getY() + 1.2D, victim.getZ(), 1, 0, 0, 0, 0);
        }
    }

    private boolean canDamageTarget(LivingEntity entity) {
        if (entity == this || !entity.isAlive()) return false;
        if (this.isTame()) {
            return !this.isOwnedBy(entity) && !this.isAlliedTo(entity);
        }
        return true;
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(serverLevel, source, amount);

        // Mecánica pasiva: Reflejo de daño por espinas corporales
        if (hurt && amount > 0.5F) {
            this.handleThornsReaction(serverLevel, source, amount);
        }

        return hurt;
    }

    /**
     * Devuelve daño a atacantes cuerpo a cuerpo directo debido a las espinas que cubren el cuerpo.
     */
    private void handleThornsReaction(ServerLevel serverLevel, DamageSource source, float incomingDamage) {
        // Solo refleja si el daño es directo cuerpo a cuerpo (no proyectiles, ni espinas previas, ni fuego/magia)
        if (source.is(DamageTypeTags.IS_PROJECTILE)
                || source.is(DamageTypes.THORNS)
                || source.is(DamageTypeTags.BYPASSES_ARMOR)) {
            return;
        }

        if (source.getDirectEntity() instanceof LivingEntity attacker && attacker != this) {
            // No dañar al dueño si ya está domesticado
            if (this.isTame() && this.isOwnedBy(attacker)) {
                return;
            }

            // Calcular daño devuelto (35% del daño recibido, mínimo 2.5 de daño)
            float reflectedDamage = Math.max(2.5F, incomingDamage * 0.35F);
            attacker.hurtServer(serverLevel, this.damageSources().thorns(this), reflectedDamage);

            // Efectos visuales y sonoros de espinas desgarrando
            serverLevel.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
                    SoundEvents.THORNS_HIT, SoundSource.HOSTILE, 1.2F, 0.9F);
            serverLevel.sendParticles(ParticleTypes.CRIT, attacker.getX(), attacker.getY() + 1.0D, attacker.getZ(),
                    10, 0.3D, 0.3D, 0.3D, 0.1D);
        }
    }

    // GeckoLib Implementation
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<SpikeBeastEntity>("controller", 4, this::spikeAnimPredicate));
    }

    private PlayState spikeAnimPredicate(AnimationTest<SpikeBeastEntity> event) {
        if (this.isAttackingWithCombo()) {
            return event.setAndContinue(ATTACK1_ANIM);
        }
        if (this.isStunned()) {
            return event.setAndContinue(IDLE_ANIM);
        }
        if (event.isMoving()) {
            return event.setAndContinue(WALK_ANIM);
        }
        return event.setAndContinue(IDLE_ANIM);
    }

    /**
     * Meta de IA para ejecutar el combo "attack1" calculando la distancia y el desplazamiento.
     */
    private class SpikeComboAttackGoal extends Goal {

        public SpikeComboAttackGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (SpikeBeastEntity.this.isStunned() || SpikeBeastEntity.this.isOrderedToSit()) {
                return false;
            }
            LivingEntity target = SpikeBeastEntity.this.getTarget();
            if (target == null || !target.isAlive()) {
                return false;
            }
            if (SpikeBeastEntity.this.isTame() && SpikeBeastEntity.this.isOwnedBy(target)) {
                return false;
            }
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            if (SpikeBeastEntity.this.isStunned()) {
                return false;
            }
            if (SpikeBeastEntity.this.isAttackingWithCombo()) {
                return true;
            }
            LivingEntity target = SpikeBeastEntity.this.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public void stop() {
            if (SpikeBeastEntity.this.isAttackingWithCombo()) {
                SpikeBeastEntity.this.stopComboAttack();
            }
            SpikeBeastEntity.this.getNavigation().stop();
        }

        @Override
        public void tick() {
            LivingEntity target = SpikeBeastEntity.this.getTarget();
            if (target == null) return;

            SpikeBeastEntity.this.getLookControl().setLookAt(target, 30.0F, 30.0F);

            // Durante el combo, la física de desplazamiento manual toma el control
            if (SpikeBeastEntity.this.isAttackingWithCombo()) {
                return;
            }

            double distSq = SpikeBeastEntity.this.distanceToSqr(target);

            // Como Spike avanza 2 bloques hacia adelante con su combo, puede iniciar el ataque hasta a 4.5 bloques
            if (distSq <= 20.25D && SpikeBeastEntity.this.attackCooldown <= 0) {
                SpikeBeastEntity.this.startComboAttack(target);
            } else {
                if (SpikeBeastEntity.this.getNavigation().isDone() || SpikeBeastEntity.this.tickCount % 15 == 0) {
                    SpikeBeastEntity.this.getNavigation().moveTo(target, 1.25D);
                }
            }
        }
    }
}
