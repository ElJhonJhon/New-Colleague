package com.newcolleague.mod.entity.boss;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.util.GeckoLibUtil;
import com.newcolleague.mod.entity.ModItems;
import com.newcolleague.mod.item.BaculoItem;
import com.newcolleague.mod.item.BaculoTier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * Base colosal para jefes tipo bestia.
 * Incluye barra de jefe, estado de aturdimiento al ser derrotado,
 * mecanica de domesticacion con el Baculo segun su nivel y estado de furia.
 */
public abstract class AbstractBossBeastEntity extends TamableAnimal implements GeoEntity {

    public enum BeastState {
        COMBAT(0, "En Combate"),
        STUNNED(1, "Aturdido"),
        ENRAGED(2, "Enfurecido"),
        TAMED(3, "Domesticado");

        private final int id;
        private final String displayName;

        BeastState(int id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }

        public int getId() {
            return this.id;
        }

        public String getDisplayName() {
            return this.displayName;
        }

        public static BeastState byId(int id) {
            for (BeastState state : values()) {
                if (state.id == id) return state;
            }
            return COMBAT;
        }
    }

    private static final EntityDataAccessor<Integer> DATA_BEAST_STATE =
            SynchedEntityData.defineId(AbstractBossBeastEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_STUN_TICKS =
            SynchedEntityData.defineId(AbstractBossBeastEntity.class, EntityDataSerializers.INT);

    protected final ServerBossEvent bossEvent;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // Animaciones base estándar para bestias
    protected static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    protected static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    protected static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenPlay("attack");
    protected static final RawAnimation STUNNED_ANIM = RawAnimation.begin().thenLoop("stunned");

    public AbstractBossBeastEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.bossEvent = (ServerBossEvent) new ServerBossEvent(
                UUID.randomUUID(),
                this.getDisplayName(),
                BossEvent.BossBarColor.RED,
                BossEvent.BossBarOverlay.PROGRESS
        ).setDarkenScreen(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_BEAST_STATE, BeastState.COMBAT.getId());
        builder.define(DATA_STUN_TICKS, 0);
    }

    public BeastState getBeastState() {
        return BeastState.byId(this.entityData.get(DATA_BEAST_STATE));
    }

    public void setBeastState(BeastState state) {
        this.entityData.set(DATA_BEAST_STATE, state.getId());
        this.updateBossBarState();
    }

    public int getStunTicks() {
        return this.entityData.get(DATA_STUN_TICKS);
    }

    public void setStunTicks(int ticks) {
        this.entityData.set(DATA_STUN_TICKS, Math.max(0, ticks));
    }

    public boolean isStunned() {
        return this.getBeastState() == BeastState.STUNNED;
    }

    public boolean isEnraged() {
        return this.getBeastState() == BeastState.ENRAGED;
    }

    @Override
    public boolean isTame() {
        return this.getBeastState() == BeastState.TAMED || super.isTame();
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    /**
     * Probabilidad de domesticación según el nivel del Báculo (1 a 7).
     */
    public float getTameChanceForStaffLevel(int staffLevel) {
        return switch (staffLevel) {
            case 1 -> 0.05F; // Adoquín: 5%
            case 2 -> 0.15F; // Pizarra Profunda: 15%
            case 3 -> 0.25F; // Cobre: 25%
            case 4 -> 0.40F; // Hierro: 40%
            case 5 -> 0.60F; // Oro: 60%
            case 6 -> 0.80F; // Diamante: 80%
            case 7 -> 1.00F; // Netherita: 100%
            default -> staffLevel > 7 ? 1.00F : 0.05F;
        };
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource source, float amount) {
        // Si no está domesticada y no está aturdida, interceptar golpe letal para pasar a fase ATURDIDO
        if (!this.isTame() && this.getBeastState() != BeastState.STUNNED) {
            if (this.getHealth() - amount <= 0.0F) {
                this.enterStunnedState();
                return true;
            }
        }
        return super.hurtServer(serverLevel, source, amount);
    }

    /**
     * Entra en estado de aturdimiento tras ser derrotado en combate.
     */
    protected void enterStunnedState() {
        this.setBeastState(BeastState.STUNNED);
        this.setStunTicks(1200); // 60 segundos de aturdimiento
        this.setHealth(Math.min(20.0F, this.getMaxHealth() * 0.1F));
        this.setTarget(null);
        this.getNavigation().stop();

        if (this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.RAVAGER_STUNNED, SoundSource.HOSTILE, 1.5F, 0.8F);
            sl.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY() + 1.5D, this.getZ(),
                    40, 0.8D, 0.5D, 0.8D, 0.2D);

            Component alert = Component.literal("⚔ ¡")
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                    .append(this.getDisplayName().copy().withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                    .append(Component.literal(" ha colapsado aturdido! Acércate con tu Báculo de Mando para someterlo.")
                            .withStyle(ChatFormatting.GOLD));

            for (ServerPlayer player : sl.getPlayers(p -> p.distanceToSqr(this) <= 2500.0D)) {
                player.sendSystemMessage(alert);
            }
        }
    }

    /**
     * Se recupera del aturdimiento y entra en estado enfurecido si no fue domesticado a tiempo.
     */
    protected void enterEnragedState() {
        this.setBeastState(BeastState.ENRAGED);
        this.setHealth(this.getMaxHealth() * 0.4F);

        if (this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 2.0F, 0.7F);
            sl.sendParticles(ParticleTypes.ANGRY_VILLAGER, this.getX(), this.getY() + 2.0D, this.getZ(),
                    25, 1.0D, 0.5D, 1.0D, 0.1D);

            Component alert = Component.literal("⚠ ¡")
                    .withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
                    .append(this.getDisplayName().copy().withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
                    .append(Component.literal(" se ha recuperado y entra en un frenesí de furia!")
                            .withStyle(ChatFormatting.RED));

            for (ServerPlayer player : sl.getPlayers(p -> p.distanceToSqr(this) <= 2500.0D)) {
                player.sendSystemMessage(alert);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            if (this.isStunned()) {
                this.getNavigation().stop();
                int ticks = this.getStunTicks();
                if (ticks > 0) {
                    this.setStunTicks(ticks - 1);
                    if (ticks % 25 == 0 && this.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 1.2D, this.getZ(),
                                4, 0.4D, 0.3D, 0.4D, 0.02D);
                    }
                } else {
                    this.enterEnragedState();
                }
            }

            this.updateBossBarProgress();
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);

        // Fase 1: Intentar domesticar con el Báculo mientras está aturdido
        if (this.isStunned()) {
            if (held.is(ModItems.BACULO.get())) {
                if (!this.level().isClientSide() && this.level() instanceof ServerLevel sl) {
                    int staffLevel = BaculoItem.getStaffLevel(held);
                    BaculoTier tier = BaculoTier.byLevel(staffLevel);
                    float chance = this.getTameChanceForStaffLevel(staffLevel);

                    float roll = this.random.nextFloat();
                    if (roll <= chance) {
                        // ¡Domesticación Exitosa!
                        this.tame(player);
                        this.setBeastState(BeastState.TAMED);
                        this.setStunTicks(0);
                        this.setHealth(this.getMaxHealth() * 0.5F);

                        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                                SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.NEUTRAL, 1.2F, 1.0F);
                        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);
                        sl.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 2.0D, this.getZ(),
                                30, 1.0D, 0.8D, 1.0D, 0.1D);
                        sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, this.getX(), this.getY() + 2.0D, this.getZ(),
                                40, 1.2D, 1.0D, 1.2D, 0.15D);

                        Component msg = Component.literal("✨ ¡Has sometido la voluntad de la bestia! ")
                                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                                .append(Component.literal("Ahora te reconoce como su amo supremo gracias a tu Báculo de ")
                                        .withStyle(ChatFormatting.YELLOW))
                                .append(Component.literal(tier.getDisplayName()).withStyle(tier.getFormatting(), ChatFormatting.BOLD))
                                .append(Component.literal("."));
                        player.sendSystemMessage(msg);
                    } else {
                        // Intento Fallido: Resistencia y retroceso
                        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                                SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 1.0F, 1.2F);
                        sl.sendParticles(ParticleTypes.ANGRY_VILLAGER, this.getX(), this.getY() + 1.8D, this.getZ(),
                                12, 0.6D, 0.4D, 0.6D, 0.05D);

                        // Reducir tiempo de aturdimiento en 150 ticks (~7.5s) por la conmoción
                        this.setStunTicks(this.getStunTicks() - 150);

                        // Empujar al jugador hacia atrás
                        Vec3 push = player.position().subtract(this.position()).normalize().scale(0.8D);
                        player.setDeltaMovement(push.x, 0.35D, push.z);
                        player.hurtMarked = true;

                        Component failMsg = Component.literal("✖ ¡La bestia se resiste a la energía del báculo! ")
                                .withStyle(ChatFormatting.RED)
                                .append(Component.literal("(Probabilidad: " + (int)(chance * 100) + "% con " + tier.getDisplayName() + "). ")
                                        .withStyle(ChatFormatting.GRAY))
                                .append(Component.literal("¡El tiempo de aturdimiento disminuye!").withStyle(ChatFormatting.YELLOW));
                        player.sendSystemMessage(failMsg);
                    }
                }
                return InteractionResult.SUCCESS;
            } else {
                if (!this.level().isClientSide()) {
                    player.sendSystemMessage(Component.literal("¡La bestia está aturdida! Usa el Báculo de Mando para someterla.")
                            .withStyle(ChatFormatting.YELLOW));
                }
                return InteractionResult.SUCCESS;
            }
        }

        // Fase 2: Comandos de domesticada (Sentarse / Seguir)
        if (this.isTame() && this.isOwnedBy(player)) {
            if (player.isSecondaryUseActive()) {
                if (!this.level().isClientSide()) {
                    boolean nowSitting = !this.isOrderedToSit();
                    this.setOrderedToSit(nowSitting);
                    Component stanceMsg = nowSitting
                            ? Component.literal("La bestia colosal monta guardia inmóvil aquí.").withStyle(ChatFormatting.AQUA)
                            : Component.literal("La bestia colosal ahora te seguirá lealmente.").withStyle(ChatFormatting.GREEN);
                    player.sendSystemMessage(stanceMsg);
                }
                return InteractionResult.SUCCESS;
            }
        }

        return super.mobInteract(player, hand);
    }

    protected void updateBossBarState() {
        BeastState state = this.getBeastState();
        switch (state) {
            case COMBAT -> {
                this.bossEvent.setName(this.getDisplayName());
                this.bossEvent.setColor(BossEvent.BossBarColor.RED);
                this.bossEvent.setVisible(true);
            }
            case STUNNED -> {
                int secLeft = Math.max(0, this.getStunTicks() / 20);
                this.bossEvent.setName(Component.literal("✦ ")
                        .append(this.getDisplayName())
                        .append(Component.literal(" [¡ATURDIDO! - Somételo: " + secLeft + "s]"))
                        .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
                this.bossEvent.setColor(BossEvent.BossBarColor.YELLOW);
                this.bossEvent.setVisible(true);
            }
            case ENRAGED -> {
                this.bossEvent.setName(Component.literal("☠ ")
                        .append(this.getDisplayName())
                        .append(Component.literal(" [¡FRENESÍ DE FURIA!]"))
                        .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
                this.bossEvent.setColor(BossEvent.BossBarColor.PURPLE);
                this.bossEvent.setVisible(true);
            }
            case TAMED -> {
                this.bossEvent.setVisible(false);
            }
        }
    }

    protected void updateBossBarProgress() {
        if (this.getBeastState() == BeastState.TAMED) {
            if (this.bossEvent.isVisible()) this.bossEvent.setVisible(false);
            return;
        }

        if (this.isStunned()) {
            this.bossEvent.setProgress((float) this.getStunTicks() / 1200.0F);
            int secLeft = Math.max(0, this.getStunTicks() / 20);
            this.bossEvent.setName(Component.literal("✦ ")
                    .append(this.getDisplayName())
                    .append(Component.literal(" [¡ATURDIDO! - Somételo: " + secLeft + "s]"))
                    .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        } else {
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        if (this.getBeastState() != BeastState.TAMED) {
            this.bossEvent.addPlayer(player);
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("BeastState", this.getBeastState().getId());
        output.putInt("StunTicks", this.getStunTicks());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setBeastState(BeastState.byId(input.getIntOr("BeastState", BeastState.COMBAT.getId())));
        this.setStunTicks(input.getIntOr("StunTicks", 0));
        this.updateBossBarState();
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }

    // GeckoLib Implementation
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<AbstractBossBeastEntity>("controller", 5, this::animPredicate));
    }

    protected PlayState animPredicate(AnimationTest<AbstractBossBeastEntity> event) {
        if (this.isStunned()) {
            return event.setAndContinue(STUNNED_ANIM);
        }
        if (event.isMoving()) {
            return event.setAndContinue(WALK_ANIM);
        }
        return event.setAndContinue(IDLE_ANIM);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
