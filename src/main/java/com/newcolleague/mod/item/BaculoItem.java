package com.newcolleague.mod.item;

import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.SingletonGeoAnimatable;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.util.GeckoLibUtil;
import com.newcolleague.mod.client.BaculoRenderer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import com.newcolleague.mod.entity.KoboldGirlEntity;

public class BaculoItem extends Item implements GeoItem {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");

    public BaculoItem(Properties properties) {
        super(properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    public static boolean isShield(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ShieldItem;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        ItemStack offhand = player.getOffhandItem();
        // Si tiene un escudo en la mano secundaria, dejamos pasar para que se active primero el escudo
        if (isShield(offhand)) {
            return InteractionResult.PASS;
        }

        ItemStack mainHand = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(mainHand)) {
            return InteractionResult.FAIL;
        }

        // Si la mano secundaria tiene cualquier otro ítem (bloques, armas, herramientas),
        // el báculo toma prioridad y comienza a cargarse, bloqueando el uso del ítem secundario.
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null && context.getHand() == InteractionHand.MAIN_HAND) {
            ItemStack offhand = player.getOffhandItem();
            if (isShield(offhand)) {
                return InteractionResult.PASS;
            }
            return this.use(context.getLevel(), player, context.getHand());
        }
        return super.useOn(context);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int count) {
        int ticksUsed = this.getUseDuration(stack, livingEntity) - count;

        if (level.isClientSide()) {
            if (ticksUsed % 4 == 0) {
                level.addParticle(
                        ParticleTypes.HAPPY_VILLAGER,
                        livingEntity.getX() + (level.getRandom().nextDouble() - 0.5) * 0.8,
                        livingEntity.getY() + 0.5 + level.getRandom().nextDouble() * 0.8,
                        livingEntity.getZ() + (level.getRandom().nextDouble() - 0.5) * 0.8,
                        0, 0.05, 0
                );
            }
        }

        if (ticksUsed == 30) {
            if (!level.isClientSide()) {
                // Regeneración nivel 2 por 10 segundos (200 ticks, amplificador 1)
                livingEntity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));

                // Extender la bendición curativa a las Kobold Girls de la tribu cercanas
                List<KoboldGirlEntity> nearbyKobolds = level.getEntitiesOfClass(
                        KoboldGirlEntity.class,
                        livingEntity.getBoundingBox().inflate(12.0D),
                        LivingEntity::isAlive
                );
                for (KoboldGirlEntity kobold : nearbyKobolds) {
                    kobold.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(
                                ParticleTypes.HEART,
                                kobold.getX(), kobold.getY() + 1.2D, kobold.getZ(),
                                6, 0.3D, 0.3D, 0.3D, 0.05D
                        );
                        serverLevel.sendParticles(
                                ParticleTypes.HAPPY_VILLAGER,
                                kobold.getX(), kobold.getY() + 1.2D, kobold.getZ(),
                                6, 0.3D, 0.3D, 0.3D, 0.05D
                        );
                    }
                }

                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                            ParticleTypes.HEART,
                            livingEntity.getX(), livingEntity.getY() + 1.2, livingEntity.getZ(),
                            7, 0.4, 0.4, 0.4, 0.1
                    );
                }

                level.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(),
                        SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.4F);

                stack.hurtAndBreak(1, livingEntity, EquipmentSlot.MAINHAND);

                if (livingEntity instanceof Player player) {
                    player.getCooldowns().addCooldown(stack, 60);
                    player.stopUsingItem();
                }
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("idle", 0, this::idlePredicate));
    }

    private com.geckolib.animation.object.PlayState idlePredicate(com.geckolib.animation.state.AnimationTest<BaculoItem> state) {
        if (com.geckolib.GeckoLibServices.PLATFORM.isPhysicalClient()) {
            state.setControllerSpeed(com.newcolleague.mod.client.BaculoClientHandler.getStaffAnimationSpeed());
        }
        return state.setAndContinue(IDLE_ANIM);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private BaculoRenderer renderer;

            @Override
            public @Nullable GeoItemRenderer<?> getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new BaculoRenderer();
                }
                return this.renderer;
            }
        });
    }
}
