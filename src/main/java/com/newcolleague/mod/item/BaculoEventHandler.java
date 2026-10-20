package com.newcolleague.mod.item;

import com.newcolleague.mod.NewColleagueMod;
import com.newcolleague.mod.entity.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import com.newcolleague.mod.entity.KoboldGirlEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import com.newcolleague.mod.network.OpenQueenDialoguePayload;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = NewColleagueMod.MODID)
public class BaculoEventHandler {

    // =========================================================================
    // SESIONES DEL HECHIZO DE LENGUAJE DE LA REINA KOBOLD
    // =========================================================================

    public static class KoboldSpellSession {
        private final UUID playerUUID;
        private final UUID queenUUID;
        private final String queenTitle;
        private final String tribeKey;
        private int remainingTicks;

        public KoboldSpellSession(UUID playerUUID, UUID queenUUID, String queenTitle, String tribeKey, int remainingTicks) {
            this.playerUUID = playerUUID;
            this.queenUUID = queenUUID;
            this.queenTitle = queenTitle;
            this.tribeKey = tribeKey;
            this.remainingTicks = remainingTicks;
        }

        public boolean tick() {
            this.remainingTicks--;
            return this.remainingTicks <= 0;
        }

        public int getRemainingTicks() {
            return this.remainingTicks;
        }

        public String getQueenTitle() {
            return this.queenTitle;
        }

        public UUID getQueenUUID() {
            return this.queenUUID;
        }

        public String getTribeKey() {
            return this.tribeKey;
        }
    }

    private static final Map<UUID, KoboldSpellSession> ACTIVE_SPELL_SESSIONS = new ConcurrentHashMap<>();

    public static void startLanguageSpell(Player player, KoboldGirlEntity queen, String queenTitle, int durationTicks) {
        String tribeKey = queen.getTribeOrQueenKey();
        ACTIVE_SPELL_SESSIONS.put(player.getUUID(), new KoboldSpellSession(player.getUUID(), queen.getUUID(), queenTitle, tribeKey, durationTicks));
    }

    public static boolean hasActiveSpell(UUID playerUUID) {
        return ACTIVE_SPELL_SESSIONS.containsKey(playerUUID);
    }

    public static void cancelLanguageSpell(UUID playerUUID) {
        if (playerUUID != null) {
            ACTIVE_SPELL_SESSIONS.remove(playerUUID);
        }
    }

    // 1. Desactivar ataque cuerpo a cuerpo convencional con el báculo e interceptar clic izquierdo en la Reina
    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();

        // Si el objetivo es una Kobold Girl espectadora de un duelo activo, es invulnerable e intocable
        if (event.getTarget() instanceof KoboldGirlEntity targetKobold && targetKobold.isSpectatingDuel()) {
            event.setCanceled(true);
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.ENCHANTED_HIT, targetKobold.getX(), targetKobold.getY() + 0.8D, targetKobold.getZ(), 6, 0.2D, 0.3D, 0.2D, 0.05D);
                sl.sendParticles(ParticleTypes.WITCH, targetKobold.getX(), targetKobold.getY() + 0.5D, targetKobold.getZ(), 4, 0.15D, 0.2D, 0.15D, 0.02D);
                sl.playSound(null, targetKobold.getX(), targetKobold.getY(), targetKobold.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.5F, 1.7F);
            }
            if (player.tickCount % 10 == 0) {
                player.sendSystemMessage(Component.literal("¡Las espectadoras del duelo son invulnerables bajo la protección de la tribu!").withStyle(ChatFormatting.GOLD));
            }
            return;
        }

        if (!player.getMainHandItem().is(ModItems.BACULO.get())) {
            return;
        }

        // Siempre cancelar ataque cuerpo a cuerpo convencional con el báculo
        event.setCanceled(true);

        // Si el objetivo atacado con clic izquierdo es una Reina Kobold:
        if (event.getTarget() instanceof KoboldGirlEntity queen && queen.isAnyQueen()) {
            if (player.level().isClientSide()) return;
            if (player instanceof ServerPlayer serverPlayer) {
                // Si la reina está en duelo contra este jugador, no abrir menú
                if (queen.isInPlayerDuelWith(serverPlayer)) {
                    return;
                }

                // Si la tribu ya pertenece al jugador
                if (queen.isOwnedBy(serverPlayer)) {
                    Component speaker = Component.literal("<" + queen.getQueenTitle() + "> ").withStyle(ChatFormatting.GOLD);
                    Component msg = Component.literal("Ya eres el gobernante de nuestra tribu, mi señor. Mi pueblo te es leal.")
                            .withStyle(ChatFormatting.GREEN);
                    serverPlayer.sendSystemMessage(speaker.copy().append(msg));
                    queen.playSound(SoundEvents.VILLAGER_YES, 0.9F, 1.1F);
                    return;
                }

                // Si el jugador aún no comprende el idioma Kobold
                if (!KoboldGirlEntity.understandsKoboldLanguage(serverPlayer)) {
                    Component speaker = Component.literal("<" + queen.getQueenTitle() + "> ").withStyle(ChatFormatting.GOLD);
                    Component galacticText = Component.literal("¿Qué pretendes hacer, forastero?")
                            .withStyle(style -> style
                                    .withFont(new FontDescription.Resource(Identifier.withDefaultNamespace("alt")))
                                    .withColor(ChatFormatting.LIGHT_PURPLE)
                                    .withHoverEvent(new HoverEvent.ShowText(Component.literal("??? (Lengua desconocida)").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC)))
                            );
                    serverPlayer.sendSystemMessage(speaker.copy().append(galacticText));
                    serverPlayer.sendSystemMessage(Component.literal("* Debes hacer clic derecho con el Báculo para comprender su lengua primero. *")
                            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
                    queen.playSound(SoundEvents.VILLAGER_NO, 0.8F, 1.0F);
                    return;
                }

                // Abrir menú de diálogo con la Reina mediante paquete de red
                PacketDistributor.sendToPlayer(serverPlayer, new OpenQueenDialoguePayload(
                        queen.getId(),
                        queen.getQueenTitle(),
                        queen.getTribeName(),
                        queen.isPacifistRouteChosen()
                ));
            }
        }
    }

    // 2. Desactivar minado / rotura de bloques con el clic izquierdo al tener el báculo
    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if (player.getMainHandItem().is(ModItems.BACULO.get())) {
            event.setCanceled(true);
        }
    }

    // 3. Impedir que la mano secundaria coloque bloques o use ítems si el jugador tiene el báculo (excepto escudos)
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (event.getHand() == InteractionHand.OFF_HAND) {
            ItemStack mainHand = player.getMainHandItem();
            if (mainHand.is(ModItems.BACULO.get())) {
                ItemStack offhand = event.getItemStack();
                if (!(offhand.getItem() instanceof ShieldItem)) {
                    event.setCanceled(true);
                }
            }
        }
    }
    // 4. Combinación Escudo + Báculo:
    // Al mantener activo el escudo en la mano secundaria, a los pocos instantes se activa la carga y cura del báculo

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        // Confinamiento en el círculo de duelo si el jugador está retando a una Reina Kobold
        KoboldGirlEntity duelQueen = KoboldGirlEntity.getActivePlayerDuelQueen(player);
        if (duelQueen != null && duelQueen.isAlive() && duelQueen.level() == player.level()) {
            Vec3 arenaCenter = duelQueen.getDuelArenaCenter();
            if (arenaCenter != null) {
                double barrierRadius = duelQueen.getDuelArenaBarrierRadius();
                double pdx = player.getX() - arenaCenter.x;
                double pdz = player.getZ() - arenaCenter.z;
                double pDist = Math.sqrt(pdx * pdx + pdz * pdz);

                if (pDist > barrierRadius && pDist > 1.0E-4D) {
                    double pnx = pdx / pDist;
                    double pnz = pdz / pDist;
                    double pClampedX = arenaCenter.x + pnx * barrierRadius;
                    double pClampedZ = arenaCenter.z + pnz * barrierRadius;

                    player.teleportTo(pClampedX, player.getY(), pClampedZ);

                    Vec3 pVel = player.getDeltaMovement();
                    double pOutward = pVel.x * pnx + pVel.z * pnz;
                    if (pOutward > 0.0D) {
                        player.setDeltaMovement(pVel.x - pOutward * pnx, pVel.y, pVel.z - pOutward * pnz);
                        player.hurtMarked = true;
                    }

                    if (player.tickCount % 4 == 0 && player.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.ENCHANTED_HIT, player.getX(), player.getY() + 1.0D, player.getZ(), 8, 0.25D, 0.4D, 0.25D, 0.05D);
                        serverLevel.sendParticles(ParticleTypes.WITCH, player.getX(), player.getY() + 0.5D, player.getZ(), 4, 0.2D, 0.2D, 0.2D, 0.02D);
                    }
                    if (player.tickCount % 20 == 0) {
                        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.6F, 1.5F);
                        player.sendSystemMessage(Component.literal("¡No puedes salir del círculo del duelo!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
                    }
                }
            }
        }

        // 4. Gestión de la transición del hechizo de comprensión de lengua Kobold
        KoboldSpellSession session = ACTIVE_SPELL_SESSIONS.get(player.getUUID());
        if (session != null) {
            boolean nauseaActive = player.hasEffect(MobEffects.NAUSEA);
            boolean timerFinished = session.tick();

            // Partículas místicas periódicas alrededor del jugador
            if (session.getRemainingTicks() % 20 == 0 && player.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.2D, player.getZ(), 8, 0.3D, 0.3D, 0.3D, 0.1D);
            }

            // Cuando el temporizador finaliza o el efecto de náuseas se disipa:
            if (timerFinished || (!nauseaActive && session.getRemainingTicks() < 130)) {
                ACTIVE_SPELL_SESSIONS.remove(player.getUUID());

                if (nauseaActive) {
                    player.removeEffect(MobEffects.NAUSEA);
                }

                // Otorgar comprensión permanente de la lengua Kobold registrando la tribu tutora
                KoboldGirlEntity.grantKoboldLanguage(player, session.getTribeKey(), session.getQueenUUID());

                // Efectos audiovisuales de despertar y comprensión
                if (player.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.2D, player.getZ(), 25, 0.5D, 0.5D, 0.5D, 0.1D);
                    serverLevel.sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + 1.2D, player.getZ(), 10, 0.4D, 0.4D, 0.4D, 0.1D);
                    serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.9F, 1.2F);

                    // Si la Reina sigue presente en el mundo, hacerla asentir y festejar
                    Entity queenEntity = serverLevel.getEntity(session.getQueenUUID());
                    if (queenEntity instanceof KoboldGirlEntity queen && queen.isAlive()) {
                        queen.playSound(SoundEvents.VILLAGER_YES, 0.9F, 1.1F);
                        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, queen.getX(), queen.getY() + 1.2D, queen.getZ(), 12, 0.4D, 0.4D, 0.4D, 0.05D);
                    }
                }

                // Mensaje en el chat de la Reina revelando el idioma
                Component speaker = Component.literal("<" + session.getQueenTitle() + "> ").withStyle(ChatFormatting.GOLD);
                Component translation = Component.literal("Con este hechizo ahora serás capaz de entender nuestra lengua, consideralo un honor, forastero. ¿Cuales son tus intenciones ahora?. Presiento que el báculo me mostrará tu verdadera naturaleza.")
                        .withStyle(ChatFormatting.YELLOW);

                player.sendSystemMessage(speaker.copy().append(translation));
            }
        }

        // 5. Combinación Escudo + Báculo:
        // Al mantener activo el escudo en la mano secundaria, a los pocos instantes se activa la carga y cura del báculo
        ItemStack mainHand = player.getMainHandItem();
        if (!mainHand.is(ModItems.BACULO.get())) return;

        ItemStack offhand = player.getOffhandItem();
        if (!(offhand.getItem() instanceof ShieldItem)) return;

        // Comprobar si el jugador está bloqueando activamente con el escudo
        if (player.isBlocking() && player.getUsedItemHand() == InteractionHand.OFF_HAND) {
            int ticksUsing = player.getTicksUsingItem();

            // A los 10 ticks (~0.5s) de escudo activo, se empieza a notar la carga mágica
            if (ticksUsing >= 10 && ticksUsing < 30) {
                if (player.level() instanceof ServerLevel serverLevel && ticksUsing % 4 == 0) {
                    serverLevel.sendParticles(
                            ParticleTypes.HAPPY_VILLAGER,
                            player.getX(), player.getY() + 1.0, player.getZ(),
                            2, 0.3, 0.3, 0.3, 0.05
                    );
                }
            }

            // Al mantenerlo 30 ticks (~1.5s), se completa la curación simultánea
            if (ticksUsing == 30) {
                if (!player.getCooldowns().isOnCooldown(mainHand)) {
                    // Otorgar Regeneración II por 10 segundos
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));

                    // Curar también a las Kobold Girls de la tribu cercanas
                    List<KoboldGirlEntity> nearbyKobolds = player.level().getEntitiesOfClass(
                            KoboldGirlEntity.class,
                            player.getBoundingBox().inflate(12.0D),
                            LivingEntity::isAlive
                    );
                    for (KoboldGirlEntity kobold : nearbyKobolds) {
                        kobold.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
                        if (player.level() instanceof ServerLevel serverLevel) {
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

                    if (player.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(
                                ParticleTypes.HEART,
                                player.getX(), player.getY() + 1.2, player.getZ(),
                                8, 0.4, 0.4, 0.4, 0.1
                        );
                    }

                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.4F);

                    mainHand.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);

                    // 5 segundos de cooldown al efecto curativo del báculo (el escudo puede seguir bloqueando)
                    player.getCooldowns().addCooldown(mainHand, 100);
                }
            }
        }
    }

    // 6. Impedir teletransportación fuera del círculo durante un duelo contra una Reina (Perlas de Ender, Chorus Fruit, comandos, etc.)
    @SubscribeEvent
    public static void onEntityTeleport(EntityTeleportEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        // Caso 1: Jugador en duelo contra una Reina Kobold
        if (event.getEntity() instanceof Player player) {
            KoboldGirlEntity duelQueen = KoboldGirlEntity.getActivePlayerDuelQueen(player);
            if (duelQueen != null && duelQueen.isAlive() && duelQueen.level() == player.level()) {
                Vec3 arenaCenter = duelQueen.getDuelArenaCenter();
                if (arenaCenter != null) {
                    double barrierRadius = duelQueen.getDuelArenaBarrierRadius();
                    double targetX = event.getTargetX();
                    double targetZ = event.getTargetZ();
                    double dx = targetX - arenaCenter.x;
                    double dz = targetZ - arenaCenter.z;
                    double targetDist = Math.sqrt(dx * dx + dz * dz);

                    // Si el destino del teletransporte está fuera de la barrera del círculo de espectadoras, cancelarlo
                    if (targetDist > barrierRadius) {
                        event.setCanceled(true);

                        if (player.level() instanceof ServerLevel serverLevel) {
                            serverLevel.sendParticles(ParticleTypes.WITCH, player.getX(), player.getY() + 1.0D, player.getZ(), 20, 0.4D, 0.6D, 0.4D, 0.1D);
                            serverLevel.sendParticles(ParticleTypes.ENCHANTED_HIT, player.getX(), player.getY() + 1.2D, player.getZ(), 25, 0.5D, 0.5D, 0.5D, 0.1D);
                            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.9F, 0.8F);
                            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.6F, 0.5F);
                        }

                        player.sendSystemMessage(Component.literal("¡La barrera mágica del círculo impide escapar del duelo!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
                        return;
                    }
                }
            }
        }

        // Caso 2: Reina Kobold en duelo intentando teletransportarse fuera del círculo
        if (event.getEntity() instanceof KoboldGirlEntity queen && queen.isAnyQueen() && (queen.isInQueenDuel() || queen.isInPlayerDuel())) {
            Vec3 arenaCenter = queen.getDuelArenaCenter();
            if (arenaCenter != null) {
                double barrierRadius = queen.getDuelArenaBarrierRadius();
                double dx = event.getTargetX() - arenaCenter.x;
                double dz = event.getTargetZ() - arenaCenter.z;
                if (Math.sqrt(dx * dx + dz * dz) > barrierRadius) {
                    event.setCanceled(true);
                }
            }
        }
    }
}
