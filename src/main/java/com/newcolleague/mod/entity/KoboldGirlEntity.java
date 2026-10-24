package com.newcolleague.mod.entity;
//todo esto da pase a trabajar mejor con futuras especies y no buscar a lo loco acciones en un solo documento
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.util.GeckoLibUtil;
import com.newcolleague.mod.entity.ai.*;
import com.newcolleague.mod.item.BaculoEventHandler;
import com.newcolleague.mod.item.BaculoItem;
import com.newcolleague.mod.network.OpenQueenDialoguePayload;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Entidad Kobold Girl: Especie kobold con animaciones GeckoLib, dilogos en rnico/kobold,
 * comercio pacifista basado en carne, y metas de comportamiento tribal heredadas de AbstractTribalEntity.
 */
public class KoboldGirlEntity extends AbstractTribalEntity implements GeoEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation SLEEP_ANIM = RawAnimation.begin().thenLoop("sleep");
    private static final RawAnimation SWORD_ATTACK_ANIM = RawAnimation.begin().thenPlay("swordattack");
    private static final RawAnimation AXE_ATTACK_ANIM = RawAnimation.begin().thenPlay("axeattack");
    private static final RawAnimation BOW_ATTACK_ANIM = RawAnimation.begin().thenPlay("bowattack");
    private static final RawAnimation SPEAR_ATTACK_ANIM = RawAnimation.begin().thenPlay("spear_attack");
    private static final RawAnimation MAGIC_FIRE_ANIM = RawAnimation.begin().thenPlay("magic_fire");
    private static final RawAnimation MAGIC_FANG_LINEA_ANIM = RawAnimation.begin().thenPlay("magic_fang_linea");
    private static final RawAnimation MAGIC_FANG_CIRCULO_ANIM = RawAnimation.begin().thenPlay("magic_fang_circulo");
    private static final RawAnimation MAGIC_LASER_ANIM = RawAnimation.begin().thenPlay("magic_laser");
    private static final RawAnimation MAGIC_IDIOMA_ANIM = RawAnimation.begin().thenPlay("magic_idioma");


    public KoboldGirlEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected AbstractTribalEntity createSubdita(ServerLevelAccessor level) {
        return new KoboldGirlEntity(ModEntities.KOBOLD_GIRL.get(), level.getLevel());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createTribalAttributes();
    }

    @Override
    public @Nullable KoboldGirlEntity getTribeQueen() {
        AbstractTribalEntity q = super.getTribeQueen();
        return q instanceof KoboldGirlEntity k ? k : null;
    }

    @Override
    public @Nullable KoboldGirlEntity getTribeQueen(ServerLevel serverLevel) {
        AbstractTribalEntity q = super.getTribeQueen(serverLevel);
        return q instanceof KoboldGirlEntity k ? k : null;
    }

    @Override
    public @Nullable KoboldGirlEntity getForcedReprimandTarget() {
        AbstractTribalEntity t = super.getForcedReprimandTarget();
        return t instanceof KoboldGirlEntity k ? k : null;
    }

    @Override
    public @Nullable KoboldGirlEntity getPatrolSquadLeaderEntity() {
        AbstractTribalEntity l = super.getPatrolSquadLeaderEntity();
        return l instanceof KoboldGirlEntity k ? k : null;
    }

    public static @Nullable KoboldGirlEntity getActivePlayerDuelQueen(Player player) {
        AbstractTribalEntity q = AbstractTribalEntity.getActivePlayerDuelQueen(player);
        return q instanceof KoboldGirlEntity k ? k : null;
    }

    /* =========================================================================
       COMERCIO PACIFISTA Y OFRENDAS DE CARNE (ESPECFICO DE KOBOLDS)
       ========================================================================= */
    public static boolean isAllowedRawMeat(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return (stack.is(Items.BEEF) || stack.is(Items.PORKCHOP) || stack.is(Items.MUTTON) || stack.is(Items.CHICKEN))
                && !stack.is(Items.RABBIT);
    }

    public void updateRawMeatOfferForPlayer(@Nullable Player player) {
        if (this.pacifistOffers == null || player == null) return;

        int meatOfferIndex = -1;
        for (int i = 0; i < this.pacifistOffers.size(); i++) {
            MerchantOffer o = this.pacifistOffers.get(i);
            if (isAllowedRawMeat(o.getBaseCostA()) && o.getUses() == 0) {
                meatOfferIndex = i;
                break;
            }
        }
        if (meatOfferIndex == -1) return;

        ItemLike bestMeat = Items.BEEF;
        int maxFound = 0;
        Item[] meats = new Item[]{Items.BEEF, Items.PORKCHOP, Items.MUTTON, Items.CHICKEN};
        for (Item meat : meats) {
            int count = 0;
            for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                ItemStack stack = player.getInventory().getItem(slot);
                if (stack.is(meat)) {
                    count += stack.getCount();
                }
            }
            if (count >= 16) {
                bestMeat = meat;
                break;
            } else if (count > maxFound) {
                maxFound = count;
                bestMeat = meat;
            }
        }

        MerchantOffer newOffer = createSingleTributeOffer(
                bestMeat, 16,
                null, 0,
                "Provisiones de Caza y Alimento",
                "Carne fresca (res, cerdo, cordero o pollo) para alimentar y nutrir a la tribu"
        );
        this.pacifistOffers.set(meatOfferIndex, newOffer);
    }

    public MerchantOffers createPacifistOffers() {
        MerchantOffers offers = new MerchantOffers();

        // 1. Armas para la Guardia: 2 Espadas de Hierro (1 en cada ranura de tributo)
        offers.add(createSingleTributeOffer(
                Items.IRON_SWORD, 1,
                Items.IRON_SWORD, 1,
                "Espadas para la Guardia",
                "Armas forjadas para defender a la tribu contra criaturas hostiles"
        ));

        // 2. Armamento a Distancia: 2 Ballestas (1 en cada ranura de tributo)
        offers.add(createSingleTributeOffer(
                Items.CROSSBOW, 1,
                Items.CROSSBOW, 1,
                "Ballestas para la Guardia",
                "Armas a distancia para que las centinelas y cazadoras defiendan el campamento"
        ));

        // 3. Provisiones de Caza y Alimento: 16 Carne Cruda (por alguna razon solo aparece la carne de vaca y no otras... aunque la carne de conejo vale vrg)
        offers.add(createSingleTributeOffer(
                Items.BEEF, 16,
                null, 0,
                "Provisiones de Caza y Alimento",
                "Carne fresca (res, cerdo, cordero o pollo) para alimentar y nutrir a la tribu"
        ));

        // 4. Herramientas de Silvicultura: 2 Hachas de Hierro (1 en cada ranura de tributo)
        offers.add(createSingleTributeOffer(
                Items.IRON_AXE, 1,
                Items.IRON_AXE, 1,
                "Hachas de Silvicultura",
                "Herramientas para talar madera y construir refugios para el pueblo"
        ));

        // 5. Armaduras de Protección: 2 Petos de Hierro (1 en cada ranura de tributo)
        offers.add(createSingleTributeOffer(
                Items.IRON_CHESTPLATE, 1,
                Items.IRON_CHESTPLATE, 1,
                "Armaduras de Hierro",
                "Protección de hierro macizo para las guerreras de la vanguardia"
        ));

        // 6. Variedad Agrícola (Semillas y Zanahorias): 16 Semillas de Trigo + 16 Zanahorias
        offers.add(createSingleTributeOffer(
                Items.WHEAT_SEEDS, 16,
                Items.CARROT, 16,
                "Semillas y Huerta Básica",
                "Variedad agrícola esencial para asegurar el alimento de la tribu"
        ));

        // 7. Variedad Agrícola (Papas y Remolacha): 16 Papas + 16 Semillas de Remolacha
        offers.add(createSingleTributeOffer(
                Items.POTATO, 16,
                Items.BEETROOT_SEEDS, 16,
                "Tubérculos y Vegetales Nutritivos",
                "Cultivos diversos para enriquecer la despensa comunitaria"
        ));

        // 8. Almacenamiento Comunitario: 8 Cofres
        offers.add(createSingleTributeOffer(
                Items.CHEST, 8,
                null, 0,
                "Cofres de Almacenamiento",
                "Estructuras de almacenaje para organizar las riquezas del campamento"
        ));

        // 9. Calor y Cocina Tribal: 4 Fogatas
        offers.add(createSingleTributeOffer(
                Items.CAMPFIRE, 4,
                null, 0,
                "Fogatas de Campamento",
                "Hogares de fuego para cocinar provisiones y brindar calor al pueblo"
        ));

        // 10. Minerales de Desarrollo y Forja: 16 Lingotes de Hierro + 8 Lingotes de Oro (tengo que ver si hago algo ocn esto más adelante)
        offers.add(createSingleTributeOffer(
                Items.IRON_INGOT, 16,
                Items.GOLD_INGOT, 8,
                "Minerales y Metales Nobles",
                "Lingotes de hierro y oro para la forja, orfebrería y progreso del reino"
        ));

        return offers;
    }

    private static MerchantOffer createSingleTributeOffer(
            net.minecraft.world.level.ItemLike itemA, int countA,
            net.minecraft.world.level.ItemLike itemB, int countB,
            String tributeName, String description) {

        ItemStack reward = new ItemStack(Items.EXPERIENCE_BOTTLE, 1);
        reward.set(DataComponents.CUSTOM_NAME, Component.literal("Bendición de Sabiduría: " + tributeName)
                .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        reward.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal(description).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
                Component.literal("⭐ Recompensa: +5 Niveles de Experiencia").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD),
                Component.literal("+10% Confianza y Alianza con la Reina Kobold").withStyle(ChatFormatting.YELLOW),
                Component.literal("Sello de gratitud y respeto tribal").withStyle(ChatFormatting.DARK_GREEN)
        )));

        ItemCost costA = new ItemCost(itemA, countA);
        Optional<ItemCost> costB = (itemB != null && countB > 0)
                ? Optional.of(new ItemCost(itemB, countB))
                : Optional.empty();

        return new MerchantOffer(costA, costB, reward, 0, 1, 5, 0.0F);
    }


    @Override
    public void updatePacifistOfferForPlayer(@Nullable Player player) {
        this.updateRawMeatOfferForPlayer(player);
    }

    /* =========================================================================
       REGISTRO DE METAS DE IA
       ========================================================================= */
    @Override
    protected void registerGoals() {
        // Metas de supervivencia y combate
        this.goalSelector.addGoal(0, new FloatGoal(this));

        // 0. Meta de la Reina leal: reprender con un solo puñetazo a las súbditas rebeldes que ataquen al gobernante
        this.goalSelector.addGoal(1, new QueenReprimandRebelGoal(this));

        // 1. Meta de súbditas rebeldes: evitar al jugador sin atacarlo, mantenerse a <= 15 bloques de la Reina, y permitir cercanía solo con el báculo
        this.goalSelector.addGoal(1, new KoboldRebelAvoidAndDefendPlayerGoal(this));

        // 1. Espectar en círculo el Duelo entre Reinas (prioridad máxima sobre tareas cotidianas)
        this.goalSelector.addGoal(1, new KoboldSpectateQueenDuelGoal(this));

        // 2. Al despertar por cualquier causa, tomar inmediatamente su armadura del soporte asignado
        this.goalSelector.addGoal(2, new KoboldRetrieveArmorGoal(this));

        // 2. Metas de gobernante: Seguir al dueño o mantener posición asignada
        this.goalSelector.addGoal(2, new KoboldFollowOwnerGoal(this, 1.25D));
        this.goalSelector.addGoal(2, new KoboldGuardPositionGoal(this, 1.05D));

        // 3. Dormir en su cama asignada durante la noche (suspendido durante duelos de reinas)
        this.goalSelector.addGoal(3, new KoboldSleepInBedGoal(this));

        // 4. Combate táctico adaptable según el arma equipada (Mazo, Lanza, Arco, Espada/Hacha)
        this.goalSelector.addGoal(4, new KoboldAdaptiveCombatGoal(this));

        // 4. Auto-Crafteo de Emergencia: Fabricar o reponer herramientas rotas en mesa comunal o in-situ
        this.goalSelector.addGoal(4, new KoboldCraftItemGoal(this, 1.15D));

        // 5. Interacción con el Báculo: La Reina (o Reina Sobreviviente) se acerca al jugador portador del Báculo
        this.goalSelector.addGoal(5, new KoboldQueenApproachBaculoGoal(this));

        // 5. Interacción con el Báculo: Todas las Kobold Girls súbditas se quedan inmóviles mirando al jugador
        this.goalSelector.addGoal(5, new KoboldStareAtBaculoGoal(this));

        // 6. Comportamiento social: Compartir comida y flechas con Kobold Girls necesitadas de su tribu
        this.goalSelector.addGoal(6, new ShareResourcesGoal(this));

        // 7. Al levantarse de la cama por la mañana, caminar por Dirt Path hacia otra zona
        this.goalSelector.addGoal(7, new KoboldMorningWalkGoal(this));

        // 8. Recoger ítems del suelo (comida, armas, armaduras, flechas)
        this.goalSelector.addGoal(8, new PickupGroundItemGoal(this));

        // 9. Patrullar caminos de tierra y sus alrededores día y noche (Exclusivo Guardias y Vigilantes)
        this.goalSelector.addGoal(9, new KoboldPatrolPathsGoal(this, 1.05D));

        // 9. Cosechar activamente cultivos 100% maduros y sembrar tierra arada (Exclusivo Granjeras)
        this.goalSelector.addGoal(9, new HarvestCropGoal(this, 1.15D));

        // 9. Ganadería y reproducción autónoma de fauna con baja población (<= 2 ejemplares)
        this.goalSelector.addGoal(9, new KoboldHerdingGoal(this, 1.15D));

        // 10. Aplanar terreno, nivelar y construir estructuras tribales (Constructoras y súbditas en trabajo)
        this.goalSelector.addGoal(10, new KoboldVillageBuildGoal(this, 1.15D));

        // 10. Cocinar carne cruda en fogatas (Exclusivo Cazadoras)
        this.goalSelector.addGoal(10, new KoboldCookMeatAtCampfireGoal(this, 1.15D));

        // 10. Exploración e inteligencia táctica (Exclusivo Exploradoras)
        this.goalSelector.addGoal(10, new KoboldScoutGoal(this, 1.2D));

        // 10. Minería subterránea en escuadrón e iluminación (Exclusivo Mineras)
        this.goalSelector.addGoal(10, new KoboldMiningSquadGoal(this, 1.15D));

        // 11. Usar cofres para almacenar excedentes (bloques, semillas, comida) y reabastecerse de comida
        this.goalSelector.addGoal(11, new KoboldChestGoal(this));

        // 12. Paseo y vida cotidiana seguro (prioriza Dirt Path y evita precipicios/peligros)
        this.goalSelector.addGoal(12, new SafeKoboldStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(13, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(14, new RandomLookAroundGoal(this));

        // Selección de Objetivos
        // 0. Duelo a casi muerte con cuchillos contra el jugador (máxima prioridad para la Reina desafiada)
        this.targetSelector.addGoal(0, new QueenPlayerDuelTargetGoal(this));

        // 0. Duelo a muerte con cuchillos entre Reinas de diferentes tribus (máxima prioridad)
        this.targetSelector.addGoal(0, new QueenDuelTargetGoal(this));

        // 1. Kobolds aliadas defienden activamente a su dueño / gobernante (estilo lobo domesticado UwU)
        this.targetSelector.addGoal(1, new KoboldDefendOwnerGoal(this));

        // 2. Guerra civil por el poder osea, luchar entre todas las súbditas hasta que quede una Reina Sobreviviente
        this.targetSelector.addGoal(2, new TribalPowerStruggleTargetGoal(this));

        // 3. Guardias y Vigilantes defienden a cualquier aliada de la tribu que sea atacada o esté en combate
        this.targetSelector.addGoal(3, new KoboldDefendAlliesGoal(this));

        // 4. Súbditas rebeldes: atacar activamente a la reina y tribu ganadora o al gobernante
        this.targetSelector.addGoal(4, new RebelTargetGoal(this));

        // 5. Defender a la reina y miembros de la tribu contra ataques de rebeldes
        this.targetSelector.addGoal(5, new DefendTribeFromRebelsGoal(this));

        // 6. Represalia en grupo: Atacar a quien la dañe y alertar a aliadas cercanas (sin atravesar muros)
        this.targetSelector.addGoal(6, new KoboldHurtByTargetGoal(this).setAlertOthers());

        // 7. Agresividad preventiva contra monstruos hostiles a 10 bloques (con línea de visión)
        this.targetSelector.addGoal(7, new HostileDetectorGoal(this));

        // 8. Caza de presas alimenticias (Exclusivo Cazadoras con hachas o arcos)
        this.targetSelector.addGoal(8, new HuntFoodGoal(this));
    }

    /* =========================================================================
       DILOGOS Y VOCES DE KOBOLD GIRLS
       ========================================================================= */

    // Frases de extrañeza / intriga de las Kobold Girls hacia un forastero
    private static final String[] KOBOLD_STRANGER_DIALOGUES = {
        "¿Que hace un forastero aquí?",
        "Un humano llegando a nuestro territorio, eso es nuevo.",
        "¿Por qué nos miras tanto? Aléjate...",
        "Nuestra reina vigila atentamente a los intrusos.",
        "No confío en los humanos... ¿qué buscas aquí?",
        "¿Nos entiendes? Claro que no, aun si no lo haces solo eres un extraño.",
        "Cuidado por dónde pisas, forastero.",
        "Nuestras guerreras te están vigilando así que cuida tus acciones.",
        "¿Has venido a perturbar la paz de nuestra tribu?"
    };

    // Frases de la Reina leal hacia su gobernante reconociendo su mando sin perder su dignidad y estatus real
    private static final String[] KOBOLD_QUEEN_ALLY_DIALOGUES = {
        "Mi corona y mi tribu reconocen tu mandato, honorable gobernante.",
        "Gobernamos juntos por el bienestar y honor de nuestro pueblo.",
        "Mis súbditas y yo respetamos tu fuerza. La tribu está en buenas manos.",
        "Has demostrado ser digno de guiarnos. Cuenta con mi lealtad.",
        "La tribu prosperará bajo tu sabiduría y poder, mi señor."
    };

    // Frases de súbditas rebeldes
    private static final String[] KOBOLD_REBEL_DIALOGUES = {
        "¡Jamás me someteré a un humano!",
        "¡Nuestra reina habrá perdido contra ti, pero mi espíritu no se doblegará!",
        "¡Fuera de nuestras tierras, usurpador!",
        "¡Prefiero el exilio antes que servir a un forastero!"
    };

    // Frases de súbditas rebeldes hacia un jugador portador del báculo (reconocen el artefacto místico pero rechazan someterse)
    private static final String[] KOBOLD_REBEL_BACULO_DIALOGUES = {
        "¡Ese báculo emana un poder antiguo... pero aún así no te obedeceré!",
        "Reconozco el poder de ese artefacto sagrado, pero no a ti como gobernante.",
        "El báculo te protegerá de mi ira, forastero... pero jamás seré tu súbdita.",
        "¡No intentes darme órdenes ni con ese báculo en la mano!",
        "Ese artefacto te da derecho a pisar estas tierras, no a gobernarme."
    };

    // Frases de Kobold Girls aliadas hacia su nuevo gobernante
    private static final String[] KOBOLD_ALLY_DIALOGUES = {
        "¡A tus órdenes, gobernante!",
        "La tribu prospera bajo tu liderazgo.",
        "Mis armas están listas si las necesitas.",
        "¿Qué deseas que haga hoy, líder?",
        "Siempre leal a ti, mi señor.",
        "Es un honor proteger nuestras tierras a tu lado."
    };

    private static final Map<UUID, Long> LAST_KOBOLD_TALK_TICK = new ConcurrentHashMap<>();

    public void speakToPlayer(ServerPlayer player) {
        long gameTime = player.level().getGameTime();
        Long lastTick = LAST_KOBOLD_TALK_TICK.get(player.getUUID());
        if (lastTick != null && gameTime - lastTick < 15L) {
            return;
        }
        LAST_KOBOLD_TALK_TICK.put(player.getUUID(), gameTime);

        this.getLookControl().setLookAt(player, 30.0F, 30.0F);
        this.setYBodyRot(this.getYHeadRot());

        String speakerName = this.getDisplayNameWithTribe();
        Component speaker = Component.literal("<" + speakerName + "> ").withStyle(ChatFormatting.GOLD);

        if (!understandsKoboldLanguage(player)) {
            // El jugador no entiende el idioma: responder en Alfabeto Galáctico
            String phrase = KOBOLD_STRANGER_DIALOGUES[this.getRandom().nextInt(KOBOLD_STRANGER_DIALOGUES.length)];
            Component galacticText = Component.literal(phrase)
                    .withStyle(style -> style
                            .withFont(new FontDescription.Resource(Identifier.withDefaultNamespace("alt")))
                            .withColor(ChatFormatting.LIGHT_PURPLE)
                            .withHoverEvent(new HoverEvent.ShowText(Component.literal("??? (Lengua desconocida)").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC)))
                    );
            player.sendSystemMessage(speaker.copy().append(galacticText));
            this.playSound(SoundEvents.VILLAGER_NO, 0.8F, 1.1F);
        } else if (!this.isOwnedBy(player)) {
            // Entiende el idioma pero la tribu no es aliada suya: responde en español
            String phrase = KOBOLD_STRANGER_DIALOGUES[this.getRandom().nextInt(KOBOLD_STRANGER_DIALOGUES.length)];
            Component message = Component.literal(phrase).withStyle(ChatFormatting.AQUA);
            player.sendSystemMessage(speaker.copy().append(message));
            this.playSound(SoundEvents.VILLAGER_NO, 0.8F, 1.0F);
        } else if (this.isAnyQueen()) {
            // Es la Reina aliada y leal al jugador: responde con dignidad y lealtad real
            String phrase = KOBOLD_QUEEN_ALLY_DIALOGUES[this.getRandom().nextInt(KOBOLD_QUEEN_ALLY_DIALOGUES.length)];
            Component message = Component.literal(phrase).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
            player.sendSystemMessage(speaker.copy().append(message));
            this.playSound(SoundEvents.VILLAGER_YES, 0.8F, 1.2F);
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.2D, this.getZ(), 6, 0.3D, 0.3D, 0.3D, 0.05D);
            }
        } else {
            // Es aliada y súbdita del jugador: responde con lealtad
            String phrase = KOBOLD_ALLY_DIALOGUES[this.getRandom().nextInt(KOBOLD_ALLY_DIALOGUES.length)];
            Component message = Component.literal(phrase).withStyle(ChatFormatting.GREEN);
            player.sendSystemMessage(speaker.copy().append(message));
            this.playSound(SoundEvents.VILLAGER_YES, 0.8F, 1.2F);
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.2D, this.getZ(), 4, 0.2D, 0.2D, 0.2D, 0.05D);
            }
        }
    }

    public void speakRebelToPlayer(ServerPlayer player) {
        long gameTime = player.level().getGameTime();
        Long lastTick = LAST_KOBOLD_TALK_TICK.get(player.getUUID());
        if (lastTick != null && gameTime - lastTick < 15L) {
            return;
        }
        LAST_KOBOLD_TALK_TICK.put(player.getUUID(), gameTime);

        this.getLookControl().setLookAt(player, 30.0F, 30.0F);
        String speakerName = "Rebelde: " + (this.hasTribe() ? this.getTribeName() : "Kobold");
        Component speaker = Component.literal("<" + speakerName + "> ").withStyle(ChatFormatting.RED);

        boolean holdingBaculo = isHoldingBaculo(player);
        String[] pool = holdingBaculo ? KOBOLD_REBEL_BACULO_DIALOGUES : KOBOLD_REBEL_DIALOGUES;
        String phrase = pool[this.getRandom().nextInt(pool.length)];

        if (!understandsKoboldLanguage(player)) {
            Component galacticText = Component.literal(phrase)
                    .withStyle(style -> style
                            .withFont(new FontDescription.Resource(Identifier.withDefaultNamespace("alt")))
                            .withColor(ChatFormatting.RED)
                            .withHoverEvent(new HoverEvent.ShowText(Component.literal("??? (Lengua desconocida)").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC)))
                    );
            player.sendSystemMessage(speaker.copy().append(galacticText));
        } else {
            player.sendSystemMessage(speaker.copy().append(Component.literal(phrase).withStyle(ChatFormatting.RED, ChatFormatting.BOLD)));
        }
        this.playSound(SoundEvents.VILLAGER_NO, 1.0F, 0.8F);
    }

    /* =========================================================================
       SISTEMA DE LENGUAJE KOBOLD
       ========================================================================= */
    /* =========================================================================
       SISTEMA DE LENGUAJE Y CONVERSACIÓN CON LA REINA
       ========================================================================= */

    public static final String TAG_UNDERSTANDS_LANGUAGE = "understands_kobold_language";
    public static final String TAG_TEACHER_TRIBE_PREFIX = "kobold_teacher_tribe_";
    public static final String TAG_MET_TRIBE_PREFIX = "kobold_met_tribe_";

    // Diálogos de primer encuentro con una Reina de OTRA tribu cuando ya entiendes el idioma (sorpresa / intriga / incredulidad)
    private static final String[] FOREIGN_QUEEN_FIRST_MEET_DIALOGUES = {
        "¿Un Forastero que entiende nuestra lengua? ... Eso es interesante ... ¿Donde lo aprendiste o ... quien te lo enseñó?",
        "¡Por los ancestros! ¿Comprendes lo que digo...? Ningún forastero debería conocer nuestras palabras sagradas... ¿Quién bendijo tu mente?",
        "¿Tú... puedes entenderme? Esto es inaudito. El secreto de nuestra lengua no se comparte a la ligera con forasteros. ¿Quién tuvo la osadía de concederte tal regalo?",
        "Esa mirada... ¡Entiendes cada una de mis palabras! Jamás creí ver a un humano dominar nuestro dialecto. ¿Quién te enseñó a escuchar nuestra voz?",
        "¿Comprendes nuestra lengua ancestral...? Fascinante. Muy pocas tribus tendrían la osadía de enseñarle nuestros secretos a un forastero como tú."
    };

    // Diálogos de preguntas genéricas e intrigadas para interacciones posteriores con otras tribus
    private static final String[] FOREIGN_QUEEN_GENERIC_DIALOGUES = {
        "¿Hiciste un largo viaje para llegar hasta aquí? ¿Por qué?",
        "¿Cuales son tus intenciones, forastero?",
        "¿Qué energías te han traído a los dominios de nuestra tribu?",
        "Ese báculo que portas guarda un inmenso poder... ¿lo usas para proteger o para conquistar?",
        "Nuestras protectoras vigilan cada uno de tus pasos, forastero. No sabemos si eres una amenaza.",
        "¿Vienes de tierras lejanas buscando refugio o persigues los misterios de nuestra gente?",
        "La mirada de mi tribu está sobre ti. Ten cuidado con las decisiones que tomas en nuestro territorio.",
        "Nuestras tierras son peligrosas para quienes no conocen sus secretos. ¿Qué buscas encontrar aquí?"
    };

    // Diálogos para la Reina de la tribu que le enseñó el idioma (tribu aliada / mentora)
    private static final String[] TEACHER_QUEEN_DIALOGUES = {
        "Nuestras mentes ya están conectadas, portador del báculo. Tu presencia intriga a nuestra tribu. ¿Cuáles son tus intenciones?",
        "El báculo que portas resuena en armonía con nuestra magia ancestral. Siempre serás bienvenido entre nuestra gente.",
        "Veo la luz del báculo reflejada en tus ojos. Nuestra tribu no olvidará a alguien como tú.",
        "¿Sientes la energía de la tierra a través de tu báculo? Nuestra lengua es solo el primer paso para comprender nuestros secretos.",
        "Mi tribu reconocen tu aura, forastero. Mientras portes ese artefacto con respeto, caminarás seguro en nuestras tierras."
    };

    private static final Map<UUID, Long> LAST_INTERACTION_TICK = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> LAST_FOREIGN_SURPRISE_INDEX = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> LAST_FOREIGN_GENERIC_INDEX = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> LAST_TEACHER_DIALOG_INDEX = new ConcurrentHashMap<>();

    public String getTribeOrQueenKey() {
        if (this.hasTribe()) {
            return this.getTribeName().trim().toLowerCase().replaceAll("[^a-z0-9_]", "_");
        }
        return "queen_" + this.getUUID().toString().replace("-", "_");
    }

    public static boolean understandsKoboldLanguage(Player player) {
        if (player == null) return false;
        return player.entityTags().contains(TAG_UNDERSTANDS_LANGUAGE);
    }

    public static boolean hasAnyTeacherTribe(Player player) {
        if (player == null) return false;
        for (String tag : player.entityTags()) {
            if (tag.startsWith(TAG_TEACHER_TRIBE_PREFIX)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isTeacherTribe(Player player, KoboldGirlEntity queen) {
        if (player == null || queen == null) return false;
        String tribeKey = queen.getTribeOrQueenKey();
        Set<String> tags = player.entityTags();
        if (tags.contains(TAG_TEACHER_TRIBE_PREFIX + tribeKey)) {
            return true;
        }
        String queenKey = "queen_" + queen.getUUID().toString().replace("-", "_");
        return tags.contains(TAG_TEACHER_TRIBE_PREFIX + queenKey);
    }

    public static boolean hasMetTribe(Player player, KoboldGirlEntity queen) {
        if (player == null || queen == null) return false;
        String tribeKey = queen.getTribeOrQueenKey();
        Set<String> tags = player.entityTags();
        if (tags.contains(TAG_MET_TRIBE_PREFIX + tribeKey)) {
            return true;
        }
        String queenKey = "queen_" + queen.getUUID().toString().replace("-", "_");
        return tags.contains(TAG_MET_TRIBE_PREFIX + queenKey);
    }

    public static void markTribeAsMet(Player player, KoboldGirlEntity queen) {
        if (player == null || queen == null) return;
        String tribeKey = queen.getTribeOrQueenKey();
        player.addTag(TAG_MET_TRIBE_PREFIX + tribeKey);
        String queenKey = "queen_" + queen.getUUID().toString().replace("-", "_");
        player.addTag(TAG_MET_TRIBE_PREFIX + queenKey);
    }

    public static void grantKoboldLanguage(Player player) {
        grantKoboldLanguage(player, "unknown", null);
    }

    public static void grantKoboldLanguage(Player player, String tribeKey, @Nullable UUID queenUUID) {
        if (player == null) return;
        player.addTag(TAG_UNDERSTANDS_LANGUAGE);
        if (tribeKey != null && !tribeKey.isEmpty()) {
            String sanitizedTribe = tribeKey.trim().toLowerCase().replaceAll("[^a-z0-9_]", "_");
            player.addTag(TAG_TEACHER_TRIBE_PREFIX + sanitizedTribe);
            player.addTag(TAG_MET_TRIBE_PREFIX + sanitizedTribe);
        }
        if (queenUUID != null) {
            String queenKey = "queen_" + queenUUID.toString().replace("-", "_");
            player.addTag(TAG_TEACHER_TRIBE_PREFIX + queenKey);
            player.addTag(TAG_MET_TRIBE_PREFIX + queenKey);
            if (player.level() instanceof ServerLevel serverLevel) {
                Entity qEnt = serverLevel.getEntity(queenUUID);
                if (qEnt instanceof KoboldGirlEntity queen && queen.isAlive()) {
                    queen.triggerMagicIdiomaAnimation();
                }
            }
        }
    }

    public static void revokeKoboldLanguage(Player player) {
        if (player == null) return;
        player.removeTag(TAG_UNDERSTANDS_LANGUAGE);
        player.removeTag("UnderstandsKoboldLanguage");

        // Eliminar todas las etiquetas asociadas a tribus maestras y tribus conocidas
        List<String> toRemove = new ArrayList<>();
        for (String tag : player.entityTags()) {
            if (tag.startsWith(TAG_TEACHER_TRIBE_PREFIX)
                    || tag.startsWith("KoboldTeacherTribe:")
                    || tag.startsWith(TAG_MET_TRIBE_PREFIX)
                    || tag.startsWith("KoboldMetTribe:")) {
                toRemove.add(tag);
            }
        }
        for (String tag : toRemove) {
            player.removeTag(tag);
        }

        // Limpiar registros en memoria de este jugador
        LAST_INTERACTION_TICK.remove(player.getUUID());
        LAST_FOREIGN_SURPRISE_INDEX.remove(player.getUUID());
        LAST_FOREIGN_GENERIC_INDEX.remove(player.getUUID());
        LAST_TEACHER_DIALOG_INDEX.remove(player.getUUID());

        // Cancelar sesión de hechizo activa si existiera
        BaculoEventHandler.cancelLanguageSpell(player.getUUID());

        // Si tiene náuseas activas causadas por el hechizo, retirarlas
        if (player.hasEffect(MobEffects.NAUSEA)) {
            player.removeEffect(MobEffects.NAUSEA);
        }
    }

    private static String getNextCyclingDialogue(UUID playerUUID, String[] dialogues, Map<UUID, Integer> indexTracker) {
        if (dialogues.length == 0) return "";
        if (dialogues.length == 1) return dialogues[0];
        int lastIndex = indexTracker.getOrDefault(playerUUID, -1);
        int nextIndex = (lastIndex + 1) % dialogues.length;
        indexTracker.put(playerUUID, nextIndex);
        return dialogues[nextIndex];
    }

    private static String getRandomDialogue(UUID playerUUID, String[] dialogues, Map<UUID, Integer> indexTracker, RandomSource random) {
        if (dialogues.length == 0) return "";
        if (dialogues.length == 1) return dialogues[0];
        int lastIndex = indexTracker.getOrDefault(playerUUID, -1);
        int nextIndex;
        if (lastIndex >= 0 && lastIndex < dialogues.length) {
            int offset = 1 + random.nextInt(dialogues.length - 1);
            nextIndex = (lastIndex + offset) % dialogues.length;
        } else {
            nextIndex = random.nextInt(dialogues.length);
        }
        indexTracker.put(playerUUID, nextIndex);
        return dialogues[nextIndex];
    }

    public void interactWithBaculo(ServerPlayer player) {
        // Mirar fijamente al jugador
        this.getLookControl().setLookAt(player, 30.0F, 30.0F);
        this.setYBodyRot(this.getYHeadRot());

        // Control anti-spam de clics rápidos (mínimo 15 ticks = 0.75 segundos entre interacciones)
        long gameTime = player.level().getGameTime();
        Long lastTick = LAST_INTERACTION_TICK.get(player.getUUID());
        if (lastTick != null && gameTime - lastTick < 15L) {
            return;
        }
        LAST_INTERACTION_TICK.put(player.getUUID(), gameTime);

        String queenTitle = this.isSurvivorQueen() ? "Reina Sobreviviente" : "Reina";
        if (this.hasTribe()) {
            queenTitle += " (" + this.getTribeName() + ")";
        }

        // 1. Si el jugador ya entiende el idioma de las Kobold Girls
        if (understandsKoboldLanguage(player)) {
            // Compatibilidad hacia atrás: si aprendió el idioma sin registrar tribu tutora, registrar esta
            if (!hasAnyTeacherTribe(player)) {
                grantKoboldLanguage(player, this.getTribeOrQueenKey(), this.getUUID());
            }

            Component speaker = Component.literal("<" + queenTitle + "> ").withStyle(ChatFormatting.GOLD);

            if (isTeacherTribe(player, this)) {
                // Diálogo con la Reina de la tribu tutora (alianza, respeto, ciclo no repetitivo)
                String line = getNextCyclingDialogue(player.getUUID(), TEACHER_QUEEN_DIALOGUES, LAST_TEACHER_DIALOG_INDEX);
                player.sendSystemMessage(speaker.copy().append(Component.literal(line).withStyle(ChatFormatting.GREEN)));

                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.2D, this.getZ(), 8, 0.3D, 0.3D, 0.3D, 0.05D);
                }
                this.playSound(SoundEvents.VILLAGER_YES, 0.8F, 1.1F);
            } else if (!hasMetTribe(player, this)) {
                // Primer encuentro con la Reina de OTRA tribu: Sorpresa, intriga e incredulidad elegida de forma aleatoria
                markTribeAsMet(player, this);
                String line = getRandomDialogue(player.getUUID(), FOREIGN_QUEEN_FIRST_MEET_DIALOGUES, LAST_FOREIGN_SURPRISE_INDEX, this.getRandom());
                player.sendSystemMessage(speaker.copy().append(Component.literal(line).withStyle(ChatFormatting.LIGHT_PURPLE)));

                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.ENCHANT, this.getX(), this.getY() + 1.2D, this.getZ(), 14, 0.4D, 0.4D, 0.4D, 0.1D);
                    serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.2D, this.getZ(), 6, 0.3D, 0.3D, 0.3D, 0.05D);
                }
                this.playSound(SoundEvents.VILLAGER_YES, 0.9F, 1.3F);
            } else {
                // Interacciones posteriores con la Reina de otra tribu: Preguntas genéricas / curiosas que van rotando
                String line = getNextCyclingDialogue(player.getUUID(), FOREIGN_QUEEN_GENERIC_DIALOGUES, LAST_FOREIGN_GENERIC_INDEX);
                player.sendSystemMessage(speaker.copy().append(Component.literal(line).withStyle(ChatFormatting.AQUA)));

                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.2D, this.getZ(), 6, 0.3D, 0.3D, 0.3D, 0.03D);
                }
                this.playSound(SoundEvents.VILLAGER_YES, 0.8F, 1.0F);
            }
            return;
        }

        // 2. Si el jugador ya tiene el hechizo activo en transición
        if (BaculoEventHandler.hasActiveSpell(player.getUUID())) {
            Component spellWarning = Component.literal("* El hechizo de la Reina aún está alterando tu mente... *").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC);
            player.sendSystemMessage(spellWarning);
            return;
        }

        // 3. Primer contacto: Mensaje en Alfabeto Galáctico (mesa de encantamientos)
        Component speaker = Component.literal("<" + queenTitle + "> ").withStyle(ChatFormatting.GOLD);
        Component galacticText = Component.literal("Un forastero con un artefacto tan único y místico")
                .withStyle(style -> style
                        .withFont(new FontDescription.Resource(Identifier.withDefaultNamespace("alt")))
                        .withColor(ChatFormatting.LIGHT_PURPLE)
                        .withHoverEvent(new HoverEvent.ShowText(Component.literal("??? (Lengua desconocida)").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC)))
                );

        player.sendSystemMessage(speaker.copy().append(galacticText));

        // Efectos mágicos de lanzamiento de hechizo sobre el jugador
        if (this.level() instanceof ServerLevel serverLevel) {
            this.triggerMagicIdiomaAnimation();
            serverLevel.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.2D, player.getZ(), 45, 0.5D, 0.5D, 0.5D, 0.5D);
            serverLevel.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0D, player.getZ(), 30, 0.4D, 0.4D, 0.4D, 0.1D);
            serverLevel.sendParticles(ParticleTypes.WITCH, player.getX(), player.getY() + 1.0D, player.getZ(), 20, 0.3D, 0.3D, 0.3D, 0.05D);

            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.NEUTRAL, 1.0F, 0.9F);
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.NEUTRAL, 1.0F, 1.0F);
        }

        // Aplicar Náuseas al jugador
        player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 0, false, true, true));

        // Iniciar la sesión de transición del hechizo en BaculoEventHandler
        BaculoEventHandler.startLanguageSpell(player, this, queenTitle, 200);
    }


    @Override
    public boolean understandsLanguage(Player player) {
        return understandsKoboldLanguage(player);
    }

    /* =========================================================================
       ANIMACIONES GECKOLIB
       ========================================================================= */
    /* =========================================================================
       ANIMACIONES GECKOLIB
       ========================================================================= */

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.LEFT;
    }

    private int lastAttackAnimTick = -1;

    @Override
    public void swing(InteractionHand hand, boolean updateSelf) {
        super.swing(hand, updateSelf);
        if (hand == InteractionHand.MAIN_HAND) {
            this.triggerAttackAnimation();
        }
    }

    public void triggerAttackAnimation() {
        if (this.isSleeping() || this.level().isClientSide()) {
            return;
        }
        if (this.tickCount == this.lastAttackAnimTick) {
            return;
        }
        this.lastAttackAnimTick = this.tickCount;

        ItemStack mainHand = this.getMainHandItem();
        String anim;
        if (isRangedWeapon(mainHand)) {
            return; // El ataque a distancia se gestiona y sincroniza exclusivamente en KoboldAdaptiveCombatGoal
        } else if (isSpear(mainHand)) {
            anim = "spear_attack";
        } else if (isAxe(mainHand)) {
            anim = "axeattack";
        } else {
            anim = "swordattack";
        }
        this.triggerAnim("attack", anim);
    }

    public void triggerSpearAttackAnimation() {
        if (!this.level().isClientSide() && !this.isSleeping()) {
            this.triggerAnim("attack", "spear_attack");
        }
    }

    public void triggerMagicFireAnimation() {
        if (!this.level().isClientSide() && !this.isSleeping()) {
            this.triggerAnim("attack", "magic_fire");
        }
    }

    public void triggerMagicFangLineaAnimation() {
        if (!this.level().isClientSide() && !this.isSleeping()) {
            this.triggerAnim("attack", "magic_fang_linea");
        }
    }

    public void triggerMagicFangCirculoAnimation() {
        if (!this.level().isClientSide() && !this.isSleeping()) {
            this.triggerAnim("attack", "magic_fang_circulo");
        }
    }

    public void triggerMagicLaserAnimation() {
        if (!this.level().isClientSide() && !this.isSleeping()) {
            this.triggerAnim("attack", "magic_laser");
        }
    }

    public void triggerMagicIdiomaAnimation() {
        if (!this.level().isClientSide() && !this.isSleeping()) {
            this.triggerAnim("attack", "magic_idioma");
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("movement", 5, this::movementPredicate));
        controllers.add(new AnimationController<>("attack", 2, state -> PlayState.STOP)
                .triggerableAnim("swordattack", SWORD_ATTACK_ANIM)
                .triggerableAnim("axeattack", AXE_ATTACK_ANIM)
                .triggerableAnim("bowattack", BOW_ATTACK_ANIM)
                .triggerableAnim("spear_attack", SPEAR_ATTACK_ANIM)
                .triggerableAnim("magic_fire", MAGIC_FIRE_ANIM)
                .triggerableAnim("magic_fang_linea", MAGIC_FANG_LINEA_ANIM)
                .triggerableAnim("magic_fang_circulo", MAGIC_FANG_CIRCULO_ANIM)
                .triggerableAnim("magic_laser", MAGIC_LASER_ANIM)
                .triggerableAnim("magic_idioma", MAGIC_IDIOMA_ANIM));
    }

    private PlayState movementPredicate(AnimationTest<KoboldGirlEntity> state) {
        if (this.isSleeping() || this.isDownedQueen()) {
            return state.setAndContinue(SLEEP_ANIM);
        }
        if (state.isMoving()) {
            return state.setAndContinue(WALK_ANIM);
        }
        return state.setAndContinue(IDLE_ANIM);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
