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
import com.newcolleague.mod.entity.KoboldGirlEntity;
import com.newcolleague.mod.entity.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class BaculoItem extends Item implements GeoItem {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");

    public BaculoItem(Properties properties) {
        super(properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    public static int getStaffLevel(ItemStack stack) {
        if (stack.isEmpty()) return 1;
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            return Math.max(1, tag.getInt("BaculoLevel").orElse(1));
        }
        return 1;
    }

    public static void setStaffLevel(ItemStack stack, int level) {
        if (stack.isEmpty()) return;
        int clamped = Math.max(1, Math.min(7, level));
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putInt("BaculoLevel", clamped);
        });
    }

    public static BaculoTier getStaffTier(ItemStack stack) {
        return BaculoTier.byLevel(getStaffLevel(stack));
    }

    public static boolean hasQueenSoul(ItemStack stack) {
        if (stack.isEmpty()) return false;
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            return tag.getBoolean("HasQueenSoul").orElse(false);
        }
        return false;
    }

    public static String getQueenSoulName(ItemStack stack) {
        if (stack.isEmpty()) return "";
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            return tag.getString("QueenSoulName").orElse("");
        }
        return "";
    }

    public static String getQueenSoulTribe(ItemStack stack) {
        if (stack.isEmpty()) return "";
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            return tag.getString("QueenSoulTribe").orElse("");
        }
        return "";
    }

    public static void setStaffQueenSoul(ItemStack stack, String queenName, String tribeName) {
        if (stack.isEmpty()) return;
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putBoolean("HasQueenSoul", true);
            tag.putString("QueenSoulName", queenName != null ? queenName : "");
            tag.putString("QueenSoulTribe", tribeName != null ? tribeName : "");
        });
    }

    public static void clearStaffQueenSoul(ItemStack stack) {
        if (stack.isEmpty()) return;
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putBoolean("HasQueenSoul", false);
            tag.putString("QueenSoulName", "");
            tag.putString("QueenSoulTribe", "");
        });
    }

    public static boolean upgradePlayerStaff(Player player, int targetLevel) {
        boolean upgraded = false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(ModItems.BACULO.get())) {
                int currentLevel = getStaffLevel(stack);
                if (currentLevel < targetLevel) {
                    setStaffLevel(stack, targetLevel);
                    upgraded = true;
                }
            }
        }

        // Registrar progreso persistente en el jugador para auto-sincronizar báculos futuros
        CompoundTag persistent = player.getPersistentData();
        int currentUnlocked = persistent.getInt("UnlockedBaculoLevel").orElse(1);
        boolean unlockedNew = targetLevel > currentUnlocked;
        if (unlockedNew) {
            persistent.putInt("UnlockedBaculoLevel", targetLevel);
        }

        if (upgraded || unlockedNew) {
            BaculoTier tier = BaculoTier.byLevel(targetLevel);
            player.sendSystemMessage(Component.literal("✨ ¡Tu Báculo de Mando ha ascendido de nivel! ")
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                    .append(Component.literal("El orbe ahora es de ")
                            .withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal(tier.getDisplayName())
                            .withStyle(tier.getFormatting(), ChatFormatting.BOLD))
                    .append(Component.literal(" (Nivel " + targetLevel + ").")
                            .withStyle(ChatFormatting.GOLD)));

            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.2D, player.getZ(), 40, 0.5D, 0.5D, 0.5D, 0.15D);
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.4D, player.getZ(), 20, 0.4D, 0.4D, 0.4D, 0.05D);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 0.9F, 1.2F);
            }
        }
        return upgraded || unlockedNew;
    }

    public static boolean isShield(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ShieldItem;
    }

    @Override
    public Component getName(ItemStack stack) {
        BaculoTier tier = getStaffTier(stack);
        if (tier.getLevel() > 1) {
            return super.getName(stack).copy()
                    .append(Component.literal(" - " + tier.getDisplayName()).withStyle(tier.getFormatting()));
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipOutput, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltipOutput, flag);
        BaculoTier tier = getStaffTier(stack);

        tooltipOutput.accept(Component.literal("Nivel: " + tier.getLevel() + " - " + tier.getDisplayName())
                .withStyle(tier.getFormatting(), ChatFormatting.BOLD));

        if (tier.getLevel() >= 2) {
            tooltipOutput.accept(Component.literal("✦ Bendición Tribal: Regeneración III para ti y toda tu tribu aliada.")
                    .withStyle(ChatFormatting.GREEN));
        } else {
            tooltipOutput.accept(Component.literal("✦ Curación Básica: Regeneración II personal.")
                    .withStyle(ChatFormatting.GRAY));
            tooltipOutput.accept(Component.literal("Resuena con un poder digno de respetar por un lider")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }

        if (hasQueenSoul(stack)) {
            String qName = getQueenSoulName(stack);
            String tName = getQueenSoulTribe(stack);
            tooltipOutput.accept(Component.literal("👻 Alma contenida: Reina " + qName + " (" + tName + ")")
                    .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
            tooltipOutput.accept(Component.literal("  [Clic izquierdo en una Kobold de la tribu para reencarnar]")
                    .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC));
        }
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
        BaculoTier tier = getStaffTier(stack);

        if (level.isClientSide()) {
            if (ticksUsed % 4 == 0) {
                level.addParticle(
                        ParticleTypes.HAPPY_VILLAGER,
                        livingEntity.getX() + (level.getRandom().nextDouble() - 0.5) * 0.8,
                        livingEntity.getY() + 0.5 + level.getRandom().nextDouble() * 0.8,
                        livingEntity.getZ() + (level.getRandom().nextDouble() - 0.5) * 0.8,
                        0, 0.05, 0
                );
                if (tier.getLevel() >= 2) {
                    level.addParticle(
                            ParticleTypes.ENCHANT,
                            livingEntity.getX() + (level.getRandom().nextDouble() - 0.5) * 0.9,
                            livingEntity.getY() + 0.6 + level.getRandom().nextDouble() * 0.9,
                            livingEntity.getZ() + (level.getRandom().nextDouble() - 0.5) * 0.9,
                            0, 0.1, 0
                    );
                }
            }
        }

        if (ticksUsed == 30) {
            if (!level.isClientSide()) {
                if (tier.getLevel() >= 2) {
                    // Nivel 2+: Regeneración 3 (200 ticks = 10 segundos, amplificador 2)
                    int amplifier = tier.getRegenAmplifier();
                    livingEntity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, amplifier));

                    // Capacidad de darle el efecto de regeneración 3 a todas las integrantes cercanas de la tribu cuyo propietario es el jugador
                    List<KoboldGirlEntity> nearbyKobolds = level.getEntitiesOfClass(
                            KoboldGirlEntity.class,
                            livingEntity.getBoundingBox().inflate(14.0D),
                            k -> k.isAlive() && k.isOwnedBy(livingEntity)
                    );
                    for (KoboldGirlEntity kobold : nearbyKobolds) {
                        kobold.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, amplifier));
                        if (level instanceof ServerLevel serverLevel) {
                            serverLevel.sendParticles(
                                    ParticleTypes.HEART,
                                    kobold.getX(), kobold.getY() + 1.2D, kobold.getZ(),
                                    8, 0.3D, 0.3D, 0.3D, 0.05D
                            );
                            serverLevel.sendParticles(
                                    ParticleTypes.HAPPY_VILLAGER,
                                    kobold.getX(), kobold.getY() + 1.2D, kobold.getZ(),
                                    6, 0.3D, 0.3D, 0.3D, 0.05D
                            );
                        }
                    }
                } else {
                    // Nivel 1 (Adoquín): Regeneración básica personal (Regeneración II)
                    livingEntity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
                }

                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                            ParticleTypes.HEART,
                            livingEntity.getX(), livingEntity.getY() + 1.2, livingEntity.getZ(),
                            9, 0.4, 0.4, 0.4, 0.1
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
