package com.newcolleague.mod.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import com.newcolleague.mod.item.BaculoEventHandler;
import com.newcolleague.mod.item.BaculoItem;
import com.newcolleague.mod.network.QueenDialogueActionPayload;
import com.newcolleague.mod.network.OpenQueenDialoguePayload;
import com.newcolleague.mod.village.VillageTier;
import com.newcolleague.mod.village.VillageProject;
import com.newcolleague.mod.village.StructureBlueprint;
import com.newcolleague.mod.village.QueenRoleManager;
import com.newcolleague.mod.village.VillageStorage;
import com.newcolleague.mod.entity.ai.KoboldCraftItemGoal;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.NameTagItem;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jspecify.annotations.Nullable;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.component.ItemLore;
import java.util.Objects;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.*;
import com.newcolleague.mod.entity.ai.*;

/**
 * Clase base abstracta universal para todas las especies que forman tribus y aldeas.
 * Maneja identidad tribal, jerarqua, aldea, prosperidad, horarios circadianos,
 * asignacin de camas y soportes de armadura, duelos de reinas, almacenamiento comunal y comercio.
 */
public abstract class AbstractTribalEntity extends PathfinderMob implements Merchant {

    // Constantes de etiquetas de tribu y lenguaje compartidas
    public static final String TAG_UNDERSTANDS_LANGUAGE = "understands_kobold_language";
    public static final String TAG_TEACHER_TRIBE_PREFIX = "kobold_teacher_tribe_";
    public static final String TAG_MET_TRIBE_PREFIX = "kobold_met_tribe_";

    public AbstractTribalEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setCanPickUpLoot(true);
        this.setPathfindingMalus(PathType.LAVA, -1.0F);
        this.setPathfindingMalus(PathType.FIRE, -1.0F);
        this.setPathfindingMalus(PathType.FIRE_IN_NEIGHBOR, 16.0F);
        this.setPathfindingMalus(PathType.DAMAGING, -1.0F);
        this.setPathfindingMalus(PathType.DAMAGING_IN_NEIGHBOR, 16.0F);
        this.setPathfindingMalus(PathType.DAMAGE_CAUTIOUS, 20.0F);
        this.setPathfindingMalus(PathType.POWDER_SNOW, -1.0F);
        this.setPathfindingMalus(PathType.WATER, 16.0F);
        this.setPathfindingMalus(PathType.WATER_BORDER, 8.0F);
    }

    /**
     * Crea una sbdata de la especie concreta durante el desove de la tribu.
     */
    protected abstract AbstractTribalEntity createSubdita(ServerLevelAccessor level);

    // Atributos base compartidos
    public static AttributeSupplier.Builder createTribalAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.ARMOR, 2.0D)
                .add(Attributes.STEP_HEIGHT, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    // Hooks de animacin sobreescribibles por subclases con GeckoLib
    public void triggerAttackAnimation(ItemStack weapon) {}
    public void triggerSpearAttackAnimation() {}
    public void triggerMagicFireAnimation() {}
    public void triggerMagicFangLineaAnimation() {}
    public void triggerMagicFangCirculoAnimation() {}
    public void triggerMagicLaserAnimation() {}
    public void triggerMagicIdiomaAnimation() {}

    // Hooks de lenguaje y dilogo sobreescribibles por la especie
    public boolean understandsLanguage(Player player) { return true; }
    public void speakToPlayer(ServerPlayer player) {}
    public void speakRebelToPlayer(ServerPlayer player) {}
    public void interactWithBaculo(ServerPlayer player) {}
    public void updatePacifistOfferForPlayer(@Nullable Player player) {}

    public MerchantOffers createPacifistOffers() {
        return new MerchantOffers();
    }

    public boolean canFightKobold(AbstractTribalEntity other) {
        return canFightTribal(other);
    }

    // Mecanica de Hambre y Saturación
    private static final EntityDataAccessor<Integer> DATA_FOOD_LEVEL =
            SynchedEntityData.defineId(AbstractTribalEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_SATURATION_LEVEL =
            SynchedEntityData.defineId(AbstractTribalEntity.class, EntityDataSerializers.FLOAT);

    // Sincronización para la variación de Reina (osea true) o Súbdita (osea false)
    private static final EntityDataAccessor<Boolean> DATA_IS_QUEEN =
            SynchedEntityData.defineId(AbstractTribalEntity.class, EntityDataSerializers.BOOLEAN);

    // Nombre de la Tribu
    private static final EntityDataAccessor<String> DATA_TRIBE_NAME =
            SynchedEntityData.defineId(AbstractTribalEntity.class, EntityDataSerializers.STRING);

    // El estado de Rebelde (resistencia tras perder a su Reina en duelo a muerte con cuchillo proximamente magia)
    private static final EntityDataAccessor<Boolean> DATA_IS_REBEL =
            SynchedEntityData.defineId(AbstractTribalEntity.class, EntityDataSerializers.BOOLEAN);

    // Guerra civil por el poder (si la tribu se queda sin reina y solo sobrevivirá la más fuerte)
    private static final EntityDataAccessor<Boolean> DATA_IS_IN_POWER_STRUGGLE =
            SynchedEntityData.defineId(AbstractTribalEntity.class, EntityDataSerializers.BOOLEAN);

    // Reina Sobreviviente (ganadora de la guerra civil, pq todas las demás en su tribu murieron)
    private static final EntityDataAccessor<Boolean> DATA_IS_SURVIVOR_QUEEN =
            SynchedEntityData.defineId(AbstractTribalEntity.class, EntityDataSerializers.BOOLEAN);

    // Aquí se sincronizan las profesiones (Granjera, Guardia, Vigilante y Cazadora por ahora)
    private static final EntityDataAccessor<String> DATA_PROFESSION =
            SynchedEntityData.defineId(AbstractTribalEntity.class, EntityDataSerializers.STRING);

    // Mecanica de volverse rey de la tribu Kobold... para nada es esclavitud (UUID en formato String o vacío si es salvaje)
    private static final EntityDataAccessor<String> DATA_OWNER_UUID =
            SynchedEntityData.defineId(AbstractTribalEntity.class, EntityDataSerializers.STRING);

    // Postura de mando del gobernante (WORK, FOLLOW, GUARD)
    private static final EntityDataAccessor<String> DATA_COMMAND_STANCE =
            SynchedEntityData.defineId(AbstractTribalEntity.class, EntityDataSerializers.STRING);

    // Nombre de pila personalizado asignado por el gobernante con etiqueta de nombre (ej. "Kira")
    private static final EntityDataAccessor<String> DATA_CUSTOM_GIVEN_NAME =
            SynchedEntityData.defineId(AbstractTribalEntity.class, EntityDataSerializers.STRING);

    // Nivel / Rango de maestría de profesión (1 = básico, 2 = veterana/mejorada)
    private static final EntityDataAccessor<Integer> DATA_PROFESSION_TIER =
            SynchedEntityData.defineId(AbstractTribalEntity.class, EntityDataSerializers.INT);

    // Nivel de prosperidad y desarrollo de la aldea tribal (0 a 300+)
    private static final EntityDataAccessor<Integer> DATA_PROSPERITY =
            SynchedEntityData.defineId(AbstractTribalEntity.class, EntityDataSerializers.INT);

    // Proceso de reencarnación del alma de la reina mediante el báculo
    private static final EntityDataAccessor<Boolean> DATA_IS_REINCARNATING =
            SynchedEntityData.defineId(AbstractTribalEntity.class, EntityDataSerializers.BOOLEAN);

    // Estado de Reina Derrotada a 1 HP tras duelo contra jugador gobernante (inmóvil e invulnerable a la espera de juicio)
    private static final EntityDataAccessor<Boolean> DATA_IS_DOWNED_QUEEN =
            SynchedEntityData.defineId(AbstractTribalEntity.class, EntityDataSerializers.BOOLEAN);

    // Marca de sentencia a muerte autorizada por el jugador gobernante tras juicio
    private static final EntityDataAccessor<Boolean> DATA_IS_MARKED_FOR_EXECUTION =
            SynchedEntityData.defineId(AbstractTribalEntity.class, EntityDataSerializers.BOOLEAN);

    // Sistema de Aldea, Expansión, Nivelación de Terreno y Construcción
    private @Nullable VillageProject activeVillageProject = null;
    private int builtStructuresCount = 0;
    private @Nullable BlockPos campCenterPos = null;
    private final List<StructureBlueprint.VillagePlot> occupiedVillagePlots = new ArrayList<>();
    private @Nullable Item villageProjectMissingItem = null;

    public List<StructureBlueprint.VillagePlot> getOccupiedVillagePlots() {
        return this.occupiedVillagePlots;
    }

    public void addOccupiedVillagePlot(StructureBlueprint.VillagePlot plot) {
        this.occupiedVillagePlots.add(plot);
    }

    public @Nullable Item getVillageProjectMissingItem() {
        return this.villageProjectMissingItem;
    }

    public void setVillageProjectMissingItem(@Nullable Item item) {
        this.villageProjectMissingItem = item;
    }

    // Posturas de mando disponibles para el gobernante
    public enum CommandStance {
        WORK("Rutina de Tribu", "🌾"),
        FOLLOW("Siguiendo", "🚶"),
        GUARD("En Guardia", "🛡");

        private final String displayName;
        private final String icon;

        CommandStance(String displayName, String icon) {
            this.displayName = displayName;
            this.icon = icon;
        }

        public String getDisplayName() {
            return this.displayName;
        }

        public String getIcon() {
            return this.icon;
        }

        public static CommandStance fromString(String name) {
            if (name == null || name.isEmpty()) return WORK;
            for (CommandStance s : values()) {
                if (s.name().equalsIgnoreCase(name) || s.displayName.equalsIgnoreCase(name)) {
                    return s;
                }
            }
            return WORK;
        }
    }

    // Posición asignada para montar guardia
    private @Nullable BlockPos guardPosition = null;

    // Marca de tiempo del último tributo recaudado por el gobernante
    private long lastTributeTime = 0L;

    // Profesiones de Kobold Girls súbditas
    public enum KoboldProfession {
        NONE("Súbdita"),
        FARMER("Granjera"),
        GUARD("Guardia"),
        VIGILANT("Vigilante"),
        HUNTER("Cazadora"),
        BUILDER("Constructora"),
        ALCHEMIST("Alquimista"),
        SCOUT("Exploradora"),
        MINER("Minera"),
        SORCERESS("Hechicera");

        private final String displayName;

        KoboldProfession(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return this.displayName;
        }

        public static KoboldProfession fromString(String name) {
            if (name == null || name.isEmpty()) return NONE;
            for (KoboldProfession p : values()) {
                if (p.name().equalsIgnoreCase(name) || p.displayName.equalsIgnoreCase(name)) {
                    return p;
                }
            }
            return NONE;
        }

        public static KoboldProfession getRandomProfession(RandomSource random) {
            KoboldProfession[] professions = new KoboldProfession[]{FARMER, GUARD, VIGILANT, HUNTER};
            return professions[random.nextInt(professions.length)];
        }
    }

    // Inventario de las Kobold
    private final SimpleContainer inventory = new SimpleContainer(27);

    // Registro global de camas asignadas en el servidor (Posición de cabecera -> UUID)
    private static final Map<BlockPos, UUID> CLAIMED_BEDS = new ConcurrentHashMap<>();

    // Categorías de almacenamiento para organización de cofres
    public enum ChestCategory {
        FOOD,
        SEEDS,
        TERRAIN,
        BUILDING,
        MINERALS,
        MEDICAL,
        MISC
    }

    // Registro global de cofres categorizados en el servidor (Posición canónica -> Categoría)
    private static final Map<BlockPos, ChestCategory> CLAIMED_CHEST_PURPOSES = new ConcurrentHashMap<>();

    // Control de enfriamiento para el diálogo de advertencia de aura de la reina al aproximarse un jugador gobernante (UUID -> GameTime)
    private static final Map<UUID, Long> LAST_AURA_GREETING_TICK = new ConcurrentHashMap<>();

    // Cama asignada exclusivamente a esta Kobold
    private BlockPos assignedBedPos = null;

    // Registro global de soportes de armadura asignados en el servidor (UUID de soporte -> UUID de Kobold)
    private static final Map<UUID, UUID> CLAIMED_ARMOR_STANDS = new ConcurrentHashMap<>();

    // Soporte de armadura asignado exclusivamente a esta Kobold
    private UUID assignedArmorStandUUID = null;

    // Estado para recuperar armaduras del soporte tras despertar
    private boolean needsArmorRetrieval = false;

    // Slots de armadura
    public static final EquipmentSlot[] ARMOR_SLOTS = new EquipmentSlot[]{
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    // Estado de paseo matutino por caminos al despertar
    private boolean needsMorningWalk = false;
    private boolean wasSleepingTime = false;

    // Mecánica de hambre, saturación y agotamiento
    private float exhaustionLevel = 0.0F;
    private int hungerTickTimer = 0;
    private int healthRegenTimer = 0;
    private int starvationTimer = 0;

    // Cooldown en ticks para volver a la cama tras ser despertada por daño o interacción
    private int sleepCooldown = 0;

    // Estado activo de cosecha de cultivos
    private boolean isHarvesting = false;

    // Estado de interacción con el portador del Báculo
    private boolean isApproachingBaculo = false;
    private boolean isStaringAtBaculo = false;

    // Cooldown de reprimenda de la Reina leal hacia súbditas rebeldes
    private int reprimandCooldown = 0;

    // Cooldown de sumisión y huida de la súbdita rebelde tras recibir el golpe de la Reina
    private int reprimandedCooldown = 0;

    // --- Escuadrones de Patrulla para Guardianas y Vigilantes ---
    private UUID patrolSquadLeaderUUID = null;
    private int patrolSquadId = 0;
    private int patrolSquadTotalGroups = 1;
    private int patrolSquadIndexInGroup = 0;
    private int patrolSquadSize = 1;
    public int patrolSquadUpdateCooldown = 0;
    private boolean isInspectingSentry = false;

    // Venganza y transición a guerra civil al morir la Reina
    protected @Nullable UUID avengeQueenKillerUUID = null;
    protected int powerStruggleDelayTicks = 0;
    protected @Nullable String pendingPowerStruggleTribe = null;
    protected @Nullable Vec3 pendingPowerStruggleCenter = null;

    // Reencarnación de la Reina mediante el Báculo
    private int reincarnationTicksRemaining = 0;
    private @Nullable String pendingReincarnatedQueenName = null;
    private @Nullable UUID reincarnationInitiatorPlayerUUID = null;

    public int getReprimandCooldown() {
        return this.reprimandCooldown;
    }

    public void setReprimandCooldown(int cd) {
        this.reprimandCooldown = cd;
    }

    public int getReprimandedCooldown() {
        return this.reprimandedCooldown;
    }

    public void setReprimandedCooldown(int cd) {
        this.reprimandedCooldown = cd;
    }

    private @Nullable AbstractTribalEntity forcedReprimandTarget = null;

    public @Nullable AbstractTribalEntity getForcedReprimandTarget() {
        return this.forcedReprimandTarget;
    }

    public void setForcedReprimandTarget(@Nullable AbstractTribalEntity target) {
        this.forcedReprimandTarget = target;
    }


    @Override
    public void stopSleeping() {
        super.stopSleeping();
        // Al levantarse de la cama, activar la prioridad de caminar por caminos hacia otra zona
        if (this.hasAssignedBed()) {
            this.needsMorningWalk = true;
        }
        if (this.doesAssignedArmorStandHaveArmor()) {
            this.needsArmorRetrieval = true;
        }
    }

    public void setNeedsMorningWalk(boolean needs) {
        this.needsMorningWalk = needs;
    }

    public boolean isNeedsMorningWalk() {
        return this.needsMorningWalk;
    }

    public int getSleepCooldown() {
        return this.sleepCooldown;
    }

    public void setSleepCooldown(int sleepCooldown) {
        this.sleepCooldown = sleepCooldown;
    }

    public boolean isHarvesting() {
        return this.isHarvesting;
    }

    public void setHarvesting(boolean harvesting) {
        this.isHarvesting = harvesting;
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new KoboldPathNavigation(this, level);
    }

    @Override
    public int getMaxFallDistance() {
        // No descender voluntariamente por desniveles de más de 2 bloques
        return 2;
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        // Si está cerca de un desnivel de más de 2 bloques, penalizar al máximo
        if (isNearDangerousDrop(level, pos, 2)) {
            return -1000.0F;
        }
        // Si está cerca de peligros (lava, fuego, cactus, bayas, etc.), penalizar al máximo para que las pendejas no pases por ahí
        if (isNearHazard(level, pos, 2)) {
            return -1000.0F;
        }
        // Evitar agua al deambular y que no sean arrastradas por corrientes
        if (level.getFluidState(pos).is(FluidTags.WATER) || level.getFluidState(pos.below()).is(FluidTags.WATER)) {
            return -500.0F;
        }
        // Si es o está sobre un camino de tierra, darle máxima prioridad para que si caminen por ahí las pendejas
        if (isDirtPath(level, pos)) {
            return 25.0F;
        }
        return 10.0F;
    }

    @Override
    protected Vec3 maybeBackOffFromEdge(Vec3 delta, MoverType moverType) {
        if (!this.level().isClientSide()
                && this.onGround()
                && !(delta.y > 0.0D)
                && (moverType == MoverType.SELF || moverType == MoverType.PLAYER)) {
            double maxSafeDrop = 2.05D;
            double deltaX = delta.x;
            double deltaZ = delta.z;
            double stepX = Math.signum(deltaX) * 0.05D;
            double stepZ = Math.signum(deltaZ) * 0.05D;

            while (deltaX != 0.0D && this.canFallAtLeast(deltaX, 0.0D, maxSafeDrop)) {
                if (Math.abs(deltaX) <= 0.05D) {
                    deltaX = 0.0D;
                    break;
                }
                deltaX -= stepX;
            }

            while (deltaZ != 0.0D && this.canFallAtLeast(0.0D, deltaZ, maxSafeDrop)) {
                if (Math.abs(deltaZ) <= 0.05D) {
                    deltaZ = 0.0D;
                    break;
                }
                deltaZ -= stepZ;
            }

            while (deltaX != 0.0D && deltaZ != 0.0D && this.canFallAtLeast(deltaX, deltaZ, maxSafeDrop)) {
                if (Math.abs(deltaX) <= 0.05D) {
                    deltaX = 0.0D;
                } else {
                    deltaX -= stepX;
                }

                if (Math.abs(deltaZ) <= 0.05D) {
                    deltaZ = 0.0D;
                } else {
                    deltaZ -= stepZ;
                }
            }

            return new Vec3(deltaX, delta.y, deltaZ);
        }
        return delta;
    }

    private boolean canFallAtLeast(double deltaX, double deltaZ, double minHeight) {
        AABB boundingBox = this.getBoundingBox();
        return this.level().noCollision(
                this,
                new AABB(
                        boundingBox.minX + 1.0E-7D + deltaX,
                        boundingBox.minY - minHeight - 1.0E-7D,
                        boundingBox.minZ + 1.0E-7D + deltaZ,
                        boundingBox.maxX - 1.0E-7D + deltaX,
                        boundingBox.minY,
                        boundingBox.maxZ - 1.0E-7D + deltaZ
                )
        );
    }

    @Override
    public boolean isPushable() {
        return !this.isSleeping() && !this.isDownedQueen() && !this.isSpectatingDuel() && super.isPushable();
    }

    @Override
    public void push(Entity entity) {
        if (!this.isSleeping() && !this.isDownedQueen() && !this.isSpectatingDuel()) {
            super.push(entity);
        }
    }

    @Override
    public void push(double xa, double ya, double za) {
        if (!this.isSleeping() && !this.isDownedQueen() && !this.isSpectatingDuel()) {
            super.push(xa, ya, za);
        }
    }

    @Override
    public void push(Vec3 vec) {
        if (!this.isSleeping() && !this.isDownedQueen() && !this.isSpectatingDuel()) {
            super.push(vec);
        }
    }

    @Override
    public void knockback(double power, double xd, double zd, DamageSource source, float damage, boolean comesFromEffect) {
        if (!this.isSleeping() && !this.isDownedQueen() && !this.isSpectatingDuel()) {
            super.knockback(power, xd, zd, source, damage, comesFromEffect);
        }
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effectInstance) {
        if (this.isSpectatingDuel()) {
            return false;
        }
        return super.canBeAffected(effectInstance);
    }

    @Override
    public void aiStep() {
        if (this.isSleeping() || this.isReincarnating()) {
            this.setDeltaMovement(Vec3.ZERO);
        }
        super.aiStep();
        if (this.isSleeping() || this.isReincarnating()) {
            this.setDeltaMovement(Vec3.ZERO);
        }
        if (this.level().isClientSide() && this.isReincarnating()) {
            this.yRotO += (this.random.nextFloat() - 0.5F) * 10.0F;
            this.setYRot(this.getYRot() + (this.random.nextFloat() - 0.5F) * 10.0F);
            this.setYHeadRot(this.getYHeadRot() + (this.random.nextFloat() - 0.5F) * 15.0F);
            this.setYBodyRot(this.yBodyRot + (this.random.nextFloat() - 0.5F) * 12.0F);
        }
        if (!this.level().isClientSide() && this.isAnyQueen() && this.tickCount % 20 == 0) {
            this.serverVillageTick();
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FOOD_LEVEL, 20);
        builder.define(DATA_SATURATION_LEVEL, 5.0F);
        builder.define(DATA_IS_QUEEN, false);
        builder.define(DATA_TRIBE_NAME, "");
        builder.define(DATA_IS_REBEL, false);
        builder.define(DATA_IS_IN_POWER_STRUGGLE, false);
        builder.define(DATA_IS_SURVIVOR_QUEEN, false);
        builder.define(DATA_PROFESSION, KoboldProfession.NONE.name());
        builder.define(DATA_OWNER_UUID, "");
        builder.define(DATA_COMMAND_STANCE, CommandStance.WORK.name());
        builder.define(DATA_CUSTOM_GIVEN_NAME, "");
        builder.define(DATA_PROFESSION_TIER, 1);
        builder.define(DATA_PROSPERITY, 0);
        builder.define(DATA_IS_REINCARNATING, false);
        builder.define(DATA_IS_DOWNED_QUEEN, false);
        builder.define(DATA_IS_MARKED_FOR_EXECUTION, false);
    }

    // Toda esta parte es para la generación de la tribu y su nombre

    private static final String[] FEMININE_TRIBE_ROOTS = {
        "Garras", "Escamas", "Colas", "Lanzas", "Sombras",
        "Rocas", "Espinas", "Grutas", "Cavernas", "Llamas",
        "Furias", "Sendas", "Crestas", "Alas", "Fosas"
    };

    private static final String[] MASCULINE_TRIBE_ROOTS = {
        "Colmillos", "Ojos", "Huesos", "Cuernos", "Minerales",
        "Túneles", "Cráneos", "Vástagos", "Picos", "Gusanos",
        "Ecos", "Montes", "Fangos", "Hierros", "Fuegos"
    };

    private static final String[] COMMON_TRIBE_DESCRIPTORS = {
        "de Fuego", "de Hierro", "de Piedra", "de Obsidiana",
        "del Trueno", "del Abismo", "de Cristal", "de Oro",
        "de Ceniza", "del Pantano", "del Volcán", "de Sangre",
        "del Desierto", "de la Noche", "del Sol", "de Magma"
    };

    private static final String[] FEMININE_TRIBE_ADJECTIVES = {
        "Doradas", "Plateadas", "Rojas", "Negras", "Nocturnas",
        "Feroces", "Ancestrales", "Profundas", "Carmesíes", "Silenciosas",
        "Salvajes", "Brillantes", "Oscuras", "Gélidas", "Ocultas"
    };

    private static final String[] MASCULINE_TRIBE_ADJECTIVES = {
        "Dorados", "Plateados", "Rojos", "Negros", "Nocturnos",
        "Feroces", "Ancestrales", "Profundos", "Carmesíes", "Silenciosos",
        "Salvajes", "Brillantes", "Oscuros", "Gélidos", "Ocultos"
    };

    public static String generateRandomTribeName(RandomSource random) {
        boolean feminine = random.nextBoolean();
        String root = feminine
                ? FEMININE_TRIBE_ROOTS[random.nextInt(FEMININE_TRIBE_ROOTS.length)]
                : MASCULINE_TRIBE_ROOTS[random.nextInt(MASCULINE_TRIBE_ROOTS.length)];

        boolean usePrepositional = random.nextBoolean();
        String complement;
        if (usePrepositional) {
            complement = COMMON_TRIBE_DESCRIPTORS[random.nextInt(COMMON_TRIBE_DESCRIPTORS.length)];
        } else {
            complement = feminine
                    ? FEMININE_TRIBE_ADJECTIVES[random.nextInt(FEMININE_TRIBE_ADJECTIVES.length)]
                    : MASCULINE_TRIBE_ADJECTIVES[random.nextInt(MASCULINE_TRIBE_ADJECTIVES.length)];
        }
        return root + " " + complement;
    }

    public static class KoboldGroupData implements SpawnGroupData {
        public final boolean isQueenAssigned;
        public final String tribeName;

        public KoboldGroupData(boolean isQueenAssigned, String tribeName) {
            this.isQueenAssigned = isQueenAssigned;
            this.tribeName = tribeName;
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData spawnData) {
        if (reason == EntitySpawnReason.SPAWN_ITEM_USE || reason == EntitySpawnReason.DISPENSER) {
            if (!(spawnData instanceof KoboldGroupData)) {
                // Entidad líder del grupo generado por el spawn egg: Reina (máximo 1 Reina por grupo)
                this.setQueen(true);
                this.setProfession(KoboldProfession.NONE);
                String tribe = generateRandomTribeName(level.getRandom());
                this.setTribeName(tribe);
                KoboldGroupData groupData = new KoboldGroupData(true, tribe);

                // Tamaño de grupo aleatorio: mínimo 5 y máximo 10 Kobold Girls
                int groupSize = 5 + level.getRandom().nextInt(6); // 5 a 10 inclusive
                KoboldProfession[] baseRoles = new KoboldProfession[]{
                        KoboldProfession.FARMER,
                        KoboldProfession.GUARD,
                        KoboldProfession.VIGILANT,
                        KoboldProfession.HUNTER
                };

                for (int i = 1; i < groupSize; i++) {
                    AbstractTribalEntity subdita = this.createSubdita(level);
                    subdita.setQueen(false);
                    subdita.setTribeName(tribe);
                    KoboldProfession prof = (i - 1 < baseRoles.length)
                            ? baseRoles[i - 1]
                            : KoboldProfession.getRandomProfession(level.getRandom());
                    subdita.setProfession(prof);

                    double angle = (2.0D * Math.PI * i) / (groupSize - 1);
                    double distance = 1.0D + level.getRandom().nextDouble() * 1.5D;
                    double targetX = this.getX() + Math.cos(angle) * distance;
                    double targetZ = this.getZ() + Math.sin(angle) * distance;
                    double targetY = this.getY();

                    BlockPos basePos = BlockPos.containing(targetX, targetY, targetZ);
                    BlockPos safePos = null;
                    for (int dy = 0; dy <= 2; dy++) {
                        for (int sign : new int[]{1, -1}) {
                            BlockPos checkPos = basePos.above(dy * sign);
                            if (!level.getBlockState(checkPos).blocksMotion()
                                    && !level.getBlockState(checkPos.above()).blocksMotion()
                                    && (level.getBlockState(checkPos.below()).blocksMotion() || level.getFluidState(checkPos).is(FluidTags.WATER))) {
                                safePos = checkPos;
                                break;
                            }
                        }
                        if (safePos != null) break;
                    }

                    if (safePos != null) {
                        targetX = safePos.getX() + 0.5D;
                        targetY = safePos.getY();
                        targetZ = safePos.getZ() + 0.5D;
                    } else {
                        targetX = this.getX() + (level.getRandom().nextDouble() - 0.5D) * 0.5D;
                        targetY = this.getY();
                        targetZ = this.getZ() + (level.getRandom().nextDouble() - 0.5D) * 0.5D;
                    }

                    subdita.setPos(targetX, targetY, targetZ);
                    float rot = level.getRandom().nextFloat() * 360.0F;
                    subdita.setYRot(rot);
                    subdita.setYHeadRot(rot);
                    subdita.finalizeSpawn(level, difficulty, reason, groupData);
                    subdita.initProfessionEquipment();
                    level.addFreshEntity(subdita);
                }

                if (this.isAnyQueen()) {
                    this.setProfession(KoboldProfession.NONE);
                    this.inventory.clearContent();
                    this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                    this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                }
                return super.finalizeSpawn(level, difficulty, reason, groupData);
            } else {
                // Entidad secundaria del grupo (osea la Súbdita)
                this.setQueen(false);
                KoboldGroupData groupData = (KoboldGroupData) spawnData;
                if (groupData.tribeName != null && !groupData.tribeName.isEmpty()) {
                    this.setTribeName(groupData.tribeName);
                }
                if (this.getProfession() == KoboldProfession.NONE) {
                    this.setProfession(KoboldProfession.getRandomProfession(level.getRandom()));
                    this.initProfessionEquipment();
                }
                return super.finalizeSpawn(level, difficulty, reason, spawnData);
            }
        }
        if (this.isQueen() && this.getTribeName().isEmpty()) {
            this.setTribeName(generateRandomTribeName(level.getRandom()));
            this.setProfession(KoboldProfession.NONE);
        } else if (!this.isQueen() && this.getProfession() == KoboldProfession.NONE) {
            this.setProfession(KoboldProfession.getRandomProfession(level.getRandom()));
            this.initProfessionEquipment();
        }
        if (this.isAnyQueen()) {
            this.setProfession(KoboldProfession.NONE);
            this.inventory.clearContent();
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        }
        return super.finalizeSpawn(level, difficulty, reason, spawnData);
    }

    public void initProfessionEquipment() {
        if (this.isAnyQueen()) return;
        if (!this.inventory.isEmpty()) return;
        switch (this.getProfession()) {
            case FARMER -> {
                this.inventory.addItem(new ItemStack(Items.STONE_HOE));
                this.inventory.addItem(new ItemStack(Items.WHEAT_SEEDS, 8));
            }
            case GUARD -> {
                this.inventory.addItem(new ItemStack(Items.STONE_SWORD));
                this.inventory.addItem(new ItemStack(Items.SHIELD));
            }
            case VIGILANT -> {
                if (this.random.nextFloat() < 0.25F) {
                    this.inventory.addItem(new ItemStack(Items.CROSSBOW));
                } else {
                    this.inventory.addItem(new ItemStack(Items.BOW));
                }
                this.inventory.addItem(new ItemStack(Items.ARROW, 32));
            }
            case HUNTER -> {
                this.inventory.addItem(new ItemStack(Items.STONE_AXE));
            }
            default -> {}
        }
    }

    public KoboldProfession getProfession() {
        return KoboldProfession.fromString(this.entityData.get(DATA_PROFESSION));
    }

    public void setProfession(KoboldProfession profession) {
        this.entityData.set(DATA_PROFESSION, profession != null ? profession.name() : KoboldProfession.NONE.name());
        this.updateRulerCustomName();
    }

    public CommandStance getCommandStance() {
        return CommandStance.fromString(this.entityData.get(DATA_COMMAND_STANCE));
    }

    public void setCommandStance(CommandStance stance) {
        this.entityData.set(DATA_COMMAND_STANCE, (stance != null ? stance : CommandStance.WORK).name());
    }

    public String getCustomGivenName() {
        return this.entityData.get(DATA_CUSTOM_GIVEN_NAME);
    }

    public void setCustomGivenName(String name) {
        this.entityData.set(DATA_CUSTOM_GIVEN_NAME, name != null ? name : "");
        this.updateRulerCustomName();
    }

    public int getProfessionTier() {
        return this.entityData.get(DATA_PROFESSION_TIER);
    }

    public void setProfessionTier(int tier) {
        this.entityData.set(DATA_PROFESSION_TIER, Math.max(1, tier));
        this.updateRulerCustomName();
    }

    public int getProsperity() {
        return this.entityData.get(DATA_PROSPERITY);
    }

    public void setProsperity(int prosperity) {
        int oldTierLevel = this.getVillageTier().getLevel();
        this.entityData.set(DATA_PROSPERITY, Math.max(0, prosperity));
        int newTierLevel = this.getVillageTier().getLevel();
        if (newTierLevel > oldTierLevel && !this.level().isClientSide()) {
            this.onVillageTierUpgraded(this.getVillageTier());
        }
    }

    public void addProsperity(int amount) {
        this.setProsperity(this.getProsperity() + amount);
    }

    public VillageTier getVillageTier() {
        int p = this.getProsperity();
        if (p >= VillageTier.DIAMOND_FORTRESS.getRequiredProsperity()) {
            return VillageTier.DIAMOND_FORTRESS;
        } else if (p >= VillageTier.ESTABLISHED.getRequiredProsperity()) {
            return VillageTier.ESTABLISHED;
        } else {
            return VillageTier.NOMAD;
        }
    }

    public void onVillageTierUpgraded(VillageTier newTier) {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.NEUTRAL, 1.2F, 1.0F);
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 1.0F, 1.2F);
        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.5D, this.getZ(),
                25, 0.8D, 0.8D, 0.8D, 0.1D);

        String tribe = this.getTribeName().isEmpty() ? "Tribu Kobold" : this.getTribeName();
        Component speaker = Component.literal("<" + this.getQueenTitle() + "> ").withStyle(ChatFormatting.GOLD);
        Component announcement = Component.literal("¡Nuestra tribu ha prosperado! Hemos alcanzado el rango de ")
                .withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(newTier.getIcon() + " " + newTier.getDisplayName()).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .append(Component.literal("!").withStyle(ChatFormatting.YELLOW));

        Player owner = this.getOwner();
        if (owner instanceof ServerPlayer sp) {
            sp.sendSystemMessage(speaker.copy().append(announcement));
        } else {
            for (ServerPlayer nearby : serverLevel.getPlayers(p -> p.distanceToSqr(this) <= 2304.0D)) {
                nearby.sendSystemMessage(speaker.copy().append(announcement));
            }
        }

        this.updateEquipmentForVillageTier(newTier);
    }

    public void updateEquipmentForVillageTier(VillageTier tier) {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        if (this.isAnyQueen()) {
            switch (tier) {
                case NOMAD -> {}
                case ESTABLISHED -> {
                    if (this.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
                        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_HELMET));
                    }
                    if (this.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) {
                        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
                    }
                    if (this.getItemBySlot(EquipmentSlot.LEGS).isEmpty()) {
                        this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS));
                    }
                    if (this.getItemBySlot(EquipmentSlot.FEET).isEmpty()) {
                        this.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));
                    }
                    if (this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
                        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
                    }
                }
                case DIAMOND_FORTRESS -> {
                    this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
                    this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_CHESTPLATE));
                    this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.DIAMOND_LEGGINGS));
                    this.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.DIAMOND_BOOTS));
                    this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
                    this.playSound(SoundEvents.ARMOR_EQUIP_DIAMOND.value(), 1.0F, 1.0F);
                }
            }
        }

        List<AbstractTribalEntity> tribeMembers = serverLevel.getEntitiesOfClass(
                AbstractTribalEntity.class,
                this.getBoundingBox().inflate(64.0D),
                k -> k.isAlive() && Objects.equals(k.getTribeName(), this.getTribeName())
        );

        for (AbstractTribalEntity member : tribeMembers) {
            if (member.isAnyQueen()) continue;
            if (member.isDefender() || member.getProfession() == KoboldProfession.GUARD || member.getProfession() == KoboldProfession.VIGILANT) {
                if (tier == VillageTier.DIAMOND_FORTRESS) {
                    member.setProfessionTier(2);
                    member.equipEliteGuardEquipment(true);
                } else if (tier == VillageTier.ESTABLISHED) {
                    if (member.getProfessionTier() >= 2) {
                        member.equipEliteGuardEquipment(false);
                    } else {
                        member.equipStandardGuardEquipment();
                    }
                }
            }
        }
    }

    public void equipEliteGuardEquipment(boolean isDiamondFortress) {
        int variant = Math.abs(this.getUUID().hashCode()) % 4;
        switch (variant) {
            case 0 -> {
                this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_CHESTPLATE));
                this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
                this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS));
                this.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.CHAINMAIL_BOOTS));
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
                this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
            }
            case 1 -> {
                this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
                this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
                this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS));
                this.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.DIAMOND_BOOTS));
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(isDiamondFortress ? Items.DIAMOND_SWORD : Items.IRON_SWORD));
                this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
            }
            case 2 -> {
                this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.DIAMOND_LEGGINGS));
                this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
                this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
                this.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
            }
            case 3 -> {
                this.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.DIAMOND_BOOTS));
                this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
                this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.CHAINMAIL_HELMET));
                this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS));
                if (this.getProfession() == KoboldProfession.VIGILANT) {
                    this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
                } else {
                    this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
                    this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
                }
            }
        }
        this.playSound(SoundEvents.ARMOR_EQUIP_DIAMOND.value(), 1.0F, 1.0F);
    }

    public void equipStandardGuardEquipment() {
        if (this.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
            this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        }
        if (this.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) {
            this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
        }
        if (this.getItemBySlot(EquipmentSlot.LEGS).isEmpty()) {
            this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.CHAINMAIL_LEGGINGS));
        }
        if (this.getItemBySlot(EquipmentSlot.FEET).isEmpty()) {
            this.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));
        }
        if (this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        }
    }

    public @Nullable VillageProject getActiveVillageProject() {
        return this.activeVillageProject;
    }

    public void setActiveVillageProject(@Nullable VillageProject project) {
        this.activeVillageProject = project;
    }

    public @Nullable BlockPos getCampCenterPos() {
        return this.campCenterPos != null ? this.campCenterPos : this.blockPosition();
    }

    public void setCampCenterPos(@Nullable BlockPos pos) {
        this.campCenterPos = pos;
    }

    public @Nullable BlockPos findNonOverlappingSite(StructureBlueprint.StructureType type) {
        BlockPos center = this.getCampCenterPos();
        int width = type.getWidth();
        int length = type.getLength();

        // 1. Si es el Campamento Central (fogata base), se ubica centrado en la posición de la aldea
        if (type == StructureBlueprint.StructureType.CAMPFIRE_BASE) {
            int ox = -width / 2;
            int oz = -length / 2;
            int candMinX = center.getX() + ox;
            int candMinZ = center.getZ() + oz;
            int candMaxX = candMinX + width - 1;
            int candMaxZ = candMinZ + length - 1;
            int surfaceY = this.level().getHeight(Heightmap.Types.WORLD_SURFACE, center.getX(), center.getZ());
            BlockPos surfacePos = new BlockPos(center.getX(), surfaceY, center.getZ());
            while (surfacePos.getY() > this.level().getMinY() && (this.level().getBlockState(surfacePos).isAir() || this.level().getBlockState(surfacePos).is(BlockTags.LEAVES))) {
                surfacePos = surfacePos.below();
            }
            int targetY = Math.max(this.level().getMinY() + 1, surfacePos.getY());
            StructureBlueprint.VillagePlot plot = new StructureBlueprint.VillagePlot(candMinX, candMinZ, candMaxX, candMaxZ);
            this.occupiedVillagePlots.add(plot);
            return new BlockPos(candMinX, targetY, candMinZ);
        }

        // Si la lista de parcelas está vacía pero ya existe el campamento base, registrar el área central
        if (this.occupiedVillagePlots.isEmpty()) {
            this.occupiedVillagePlots.add(new StructureBlueprint.VillagePlot(center.getX() - 3, center.getZ() - 3, center.getX() + 3, center.getZ() + 3));
        }

        // 2. Búsqueda en anillos concéntricos alrededor del centro comunal (distancias de 8 a 42 bloques)
        for (int dist = 8; dist <= 42; dist += 3) {
            for (int angleDeg = 0; angleDeg < 360; angleDeg += 30) {
                double rad = Math.toRadians(angleDeg);
                int ox = (int) Math.round(dist * Math.cos(rad));
                int oz = (int) Math.round(dist * Math.sin(rad));

                int candMinX = center.getX() + ox;
                int candMinZ = center.getZ() + oz;
                int candMaxX = candMinX + width - 1;
                int candMaxZ = candMinZ + length - 1;

                // Verificación A: No colisionar con ninguna parcela ya construida o reservada (con margen de 2 bloques)
                boolean collidesWithPlot = false;
                for (StructureBlueprint.VillagePlot plot : this.occupiedVillagePlots) {
                    if (plot.intersects(candMinX, candMinZ, candMaxX, candMaxZ, 2)) {
                        collidesWithPlot = true;
                        break;
                    }
                }
                if (collidesWithPlot) continue;

                // Verificación B: No superponerse con el campamento central
                if (Math.abs((candMinX + width / 2) - center.getX()) <= 4 && Math.abs((candMinZ + length / 2) - center.getZ()) <= 4) {
                    continue;
                }

                // Verificación C: Inspección en el mundo del terreno y estructuras existentes del jugador
                int minY = Integer.MAX_VALUE;
                int maxY = Integer.MIN_VALUE;
                boolean invalidTerrain = false;

                for (int x = candMinX; x <= candMaxX && !invalidTerrain; x++) {
                    for (int z = candMinZ; z <= candMaxZ; z++) {
                        int surfaceY = this.level().getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
                        BlockPos testPos = new BlockPos(x, surfaceY, z);
                        while (testPos.getY() > this.level().getMinY() && (this.level().getBlockState(testPos).isAir() || this.level().getBlockState(testPos).is(BlockTags.LEAVES))) {
                            testPos = testPos.below();
                        }
                        int y = testPos.getY();
                        BlockState stateAt = this.level().getBlockState(testPos);
                        BlockState stateAbove = this.level().getBlockState(testPos.above());

                        // No construir en agua o lava
                        if (stateAt.getFluidState().is(FluidTags.WATER) || stateAt.getFluidState().is(FluidTags.LAVA)
                                || stateAbove.getFluidState().is(FluidTags.WATER) || stateAbove.getFluidState().is(FluidTags.LAVA)) {
                            invalidTerrain = true;
                            break;
                        }

                        // Proteger estructuras y pertenencias de jugadores (cofres, camas, hornos, entidades con bloque)
                        if (stateAt.hasBlockEntity() || stateAbove.hasBlockEntity()
                                || stateAt.is(Blocks.CHEST) || stateAt.is(Blocks.BARREL)
                                || stateAt.getBlock() instanceof BedBlock) {
                            invalidTerrain = true;
                            break;
                        }

                        if (y < minY) minY = y;
                        if (y > maxY) maxY = y;
                    }
                }

                if (invalidTerrain) continue;

                // Verificación D: Desnivel del terreno <= 3 bloques
                if (maxY - minY > 3) continue;

                // ¡Sitio perfecto encontrado!
                int chosenY = (minY + maxY) / 2;
                StructureBlueprint.VillagePlot chosenPlot = new StructureBlueprint.VillagePlot(candMinX, candMinZ, candMaxX, candMaxZ);
                this.occupiedVillagePlots.add(chosenPlot);
                return new BlockPos(candMinX, chosenY, candMinZ);
            }
        }

        return null;
    }

    public boolean startNewVillageProject(StructureBlueprint.StructureType type) {
        if (this.activeVillageProject != null && !this.activeVillageProject.isCompleted()) {
            return false;
        }
        BlockPos origin = this.findNonOverlappingSite(type);
        if (origin == null) {
            Component speaker = Component.literal("<" + this.getQueenTitle() + "> ").withStyle(ChatFormatting.GOLD);
            Component msg = Component.literal("No encontramos terreno adecuado y libre para: ")
                    .withStyle(ChatFormatting.RED)
                    .append(Component.literal(type.getDisplayName()).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                    .append(Component.literal(". ¡Se necesita un área plana libre de obstáculos y agua!").withStyle(ChatFormatting.RED));
            Player owner = this.getOwner();
            if (owner instanceof ServerPlayer sp) {
                sp.sendSystemMessage(speaker.copy().append(msg));
            }
            return false;
        }

        this.villageProjectMissingItem = null;
        this.activeVillageProject = new VillageProject(type, origin);

        if (this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BELL_BLOCK, SoundSource.NEUTRAL, 1.0F, 0.9F);
            sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, origin.getX() + 2.5D, origin.getY() + 1.0D, origin.getZ() + 2.5D, 16, 1.5D, 0.5D, 1.5D, 0.05D);
        }

        Component speaker = Component.literal("<" + this.getQueenTitle() + "> ").withStyle(ChatFormatting.GOLD);
        Component msg = Component.literal("¡Iniciando la construcción de: ")
                .withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(type.getDisplayName()).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .append(Component.literal("! Nuestras constructoras aplanarán el terreno y buscarán los materiales necesarios.").withStyle(ChatFormatting.YELLOW));

        Player owner = this.getOwner();
        if (owner instanceof ServerPlayer sp) {
            sp.sendSystemMessage(speaker.copy().append(msg));
        }
        return true;
    }

    public void onStructureCompleted(VillageProject project) {
        if (!(this.level() instanceof ServerLevel sl)) return;

        this.villageProjectMissingItem = null;
        this.builtStructuresCount++;
        this.addProsperity(35);

        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.NEUTRAL, 1.2F, 1.0F);
        sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.5D, this.getZ(),
                30, 1.0D, 1.0D, 1.0D, 0.1D);

        String structName = project.getStructureType().getDisplayName();
        Component speaker = Component.literal("<" + this.getQueenTitle() + "> ").withStyle(ChatFormatting.GOLD);
        Component msg = Component.literal("¡Nuestra tribu ha completado la construcción de: ")
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal(structName).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .append(Component.literal("! La prosperidad de la aldea aumenta a §e" + this.getProsperity() + "§a pts."));

        Player owner = this.getOwner();
        if (owner instanceof ServerPlayer sp) {
            sp.sendSystemMessage(speaker.copy().append(msg));
        } else {
            for (ServerPlayer nearby : sl.getPlayers(p -> p.distanceToSqr(this) <= 2304.0D)) {
                nearby.sendSystemMessage(speaker.copy().append(msg));
            }
        }

        this.activeVillageProject = null;
    }

    public void serverVillageTick() {
        if (!this.isAnyQueen() || this.level().isClientSide()) return;

        // 1. Ganancia de prosperidad pasiva cada 600 ticks (~30 segundos)
        if (this.tickCount % 600 == 0) {
            int bonus = 1;
            List<AbstractTribalEntity> tribe = this.level().getEntitiesOfClass(
                    AbstractTribalEntity.class,
                    this.getBoundingBox().inflate(64.0D),
                    k -> k.isAlive() && Objects.equals(k.getTribeName(), this.getTribeName())
            );
            boolean hasFarmer = false, hasHunter = false, hasGuard = false;
            for (AbstractTribalEntity k : tribe) {
                if (k.getProfession() == KoboldProfession.FARMER) hasFarmer = true;
                if (k.getProfession() == KoboldProfession.HUNTER) hasHunter = true;
                if (k.isDefender()) hasGuard = true;
            }
            if (hasFarmer) bonus++;
            if (hasHunter) bonus++;
            if (hasGuard) bonus++;
            this.addProsperity(bonus);
        }

        // 2. Comprobar inicio o avance de proyectos de expansión y reasignación dinámica de roles cada 200 ticks (~10s)
        if (this.tickCount % 200 == 0) {
            this.evaluateAndReassignTribeRoles();

            if (this.activeVillageProject == null) {
                VillageTier tier = this.getVillageTier();
                StructureBlueprint.StructureType nextType = null;
                if (this.builtStructuresCount == 0) {
                    nextType = StructureBlueprint.StructureType.CAMPFIRE_BASE;
                } else if (this.builtStructuresCount == 1) {
                    nextType = StructureBlueprint.StructureType.TENT;
                } else if (this.builtStructuresCount == 2) {
                    nextType = StructureBlueprint.StructureType.TENT;
                } else if (tier.getLevel() >= 2 && this.builtStructuresCount == 3) {
                    nextType = StructureBlueprint.StructureType.FARM;
                } else if (tier.getLevel() >= 2 && this.builtStructuresCount == 4) {
                    nextType = StructureBlueprint.StructureType.STOREHOUSE;
                } else if (tier.getLevel() >= 2 && this.builtStructuresCount == 5) {
                    nextType = StructureBlueprint.StructureType.TENT;
                } else if (tier.getLevel() >= 3 && this.builtStructuresCount == 6) {
                    nextType = StructureBlueprint.StructureType.WATCHTOWER;
                } else if (tier.getLevel() >= 3 && this.builtStructuresCount == 7) {
                    nextType = StructureBlueprint.StructureType.WATCHTOWER;
                } else if (tier.getLevel() >= 3 && this.builtStructuresCount >= 8 && this.builtStructuresCount < 12) {
                    nextType = (this.builtStructuresCount % 2 == 0) ? StructureBlueprint.StructureType.WATCHTOWER : StructureBlueprint.StructureType.STOREHOUSE;
                }

                if (nextType != null) {
                    this.startNewVillageProject(nextType);
                }
            } else if (this.activeVillageProject.isCompleted()) {
                this.onStructureCompleted(this.activeVillageProject);
            }
        }
    }

    public boolean hasActiveVillageProject() {
        return this.activeVillageProject != null && !this.activeVillageProject.isCompleted();
    }

    public void evaluateAndReassignTribeRoles() {
        if (!this.isAnyQueen() || !(this.level() instanceof ServerLevel sl)) return;

        List<AbstractTribalEntity> tribe = sl.getEntitiesOfClass(
                AbstractTribalEntity.class,
                this.getBoundingBox().inflate(64.0D),
                k -> k.isAlive() && Objects.equals(k.getTribeName(), this.getTribeName())
        );
        if (tribe.isEmpty()) return;

        QueenRoleManager.evaluateAndAssignRoles(this, tribe);
    }

    public int countCommunityFood(ServerLevel level, int radius) {
        int food = 0;
        List<BlockPos> chests = findNearbyUniqueChests(level, this.blockPosition(), radius, 6);
        for (BlockPos chestPos : chests) {
            Container c = getChestContainer(level, chestPos);
            if (c != null) {
                for (int i = 0; i < c.getContainerSize(); i++) {
                    ItemStack stack = c.getItem(i);
                    if (stack.has(DataComponents.FOOD) || stack.is(Items.WHEAT)) {
                        food += stack.getCount();
                    }
                }
            }
        }
        return food;
    }

    public boolean hasNearbyHostiles(ServerLevel level, double radius) {
        List<LivingEntity> hostiles = level.getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(radius),
                e -> e.isAlive() && (e instanceof Monster || e instanceof Enemy)
        );
        return !hostiles.isEmpty();
    }

    public @Nullable BlockPos getGuardPosition() {
        return this.guardPosition;
    }

    public void setGuardPosition(@Nullable BlockPos pos) {
        this.guardPosition = pos;
    }

    public void updateRulerCustomName() {
        if (!this.isOwned()) return;

        String given = this.getCustomGivenName();
        boolean hasGiven = !given.isEmpty();
        ChatFormatting color = this.isAnyQueen() ? ChatFormatting.GOLD : (this.getProfession() == KoboldProfession.SORCERESS ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.GREEN);

        if (this.isRebel()) {
            String rebelRole = this.getProfession() != KoboldProfession.NONE
                    ? this.getProfession().getDisplayName() + " Rebelde"
                    : "Rebelde";
            if (this.getProfessionTier() >= 2 && this.getProfession() != KoboldProfession.NONE) {
                rebelRole = "⭐ " + rebelRole;
            }
            Component nameComp = Component.literal(hasGiven ? given + " [" + rebelRole + "]" : rebelRole)
                    .withStyle(ChatFormatting.RED);
            this.setCustomName(nameComp);
            this.setCustomNameVisible(true);
            return;
        }

        // Para integrantes pertenecientes al jugador:
        // Se eliminan por completo las palabras "Leal" y "Reina" de las etiquetas para evitar nombres largos innecesarios.
        // Con solo el nombre (si lo tiene) y su profesión es suficiente.
        if (this.getProfession() != KoboldProfession.NONE) {
            String profName = this.getProfession().getDisplayName();
            if (this.getProfessionTier() >= 2) {
                profName = "⭐ " + profName;
            }
            Component nameComp = Component.literal(hasGiven ? given + " [" + profName + "]" : profName)
                    .withStyle(color);
            this.setCustomName(nameComp);
        } else {
            // Sin profesión (como la Reina o una súbdita común sin oficio asignado)
            String displayName = hasGiven ? given : "Kobold Girl";
            this.setCustomName(Component.literal(displayName).withStyle(color));
        }

        this.setCustomNameVisible(true);
    }

    public boolean isDefender() {
        KoboldProfession prof = this.getProfession();
        return prof == KoboldProfession.GUARD || prof == KoboldProfession.VIGILANT || prof == KoboldProfession.SORCERESS;
    }

    public boolean isDownedQueen() {
        return this.entityData.get(DATA_IS_DOWNED_QUEEN);
    }

    public void setDownedQueen(boolean downed) {
        this.entityData.set(DATA_IS_DOWNED_QUEEN, downed);
    }

    public boolean isMarkedForExecution() {
        return this.entityData.get(DATA_IS_MARKED_FOR_EXECUTION);
    }

    public void setMarkedForExecution(boolean marked) {
        this.entityData.set(DATA_IS_MARKED_FOR_EXECUTION, marked);
    }

    public boolean isMagicUser() {
        return this.isAnyQueen() || this.getProfession() == KoboldProfession.SORCERESS;
    }

    public boolean isSorceress() {
        return this.getProfession() == KoboldProfession.SORCERESS;
    }

    public static String getPlayerPrimaryTribe(Player player) {
        if (player == null) return "";
        String stored = player.getPersistentData().getString("PlayerOwnedTribe").orElse("");
        if (!stored.isEmpty()) return stored;
        String pending = player.getPersistentData().getString("PendingQueenSoulTribe").orElse("");
        if (!pending.isEmpty()) return pending;
        if (player.level() instanceof ServerLevel sl) {
            for (Entity e : sl.getAllEntities()) {
                if (e instanceof AbstractTribalEntity k && k.isAlive() && k.isOwnedBy(player) && k.hasTribe()) {
                    String t = k.getTribeName();
                    player.getPersistentData().putString("PlayerOwnedTribe", t);
                    return t;
                }
            }
        }
        return "";
    }

    public static boolean doesPlayerOwnAnyTribe(Player player) {
        return !getPlayerPrimaryTribe(player).isEmpty();
    }

    public static void annexTribeToPlayer(ServerLevel serverLevel, Player player, String oldTribeName, String newTribeName, @Nullable AbstractTribalEntity excludedEntity) {
        if (serverLevel == null || player == null) return;
        List<AbstractTribalEntity> members = serverLevel.getEntitiesOfClass(
                AbstractTribalEntity.class,
                player.getBoundingBox().inflate(128.0D),
                k -> k != excludedEntity && k.isAlive() && (oldTribeName.isEmpty() || oldTribeName.equals(k.getTribeName()))
        );

        for (AbstractTribalEntity member : members) {
            member.setTribeName(newTribeName);
            member.setOwnerUUID(player.getUUID());
            member.setRebel(false);
            member.setPersistenceRequired();
            member.setTarget(null);
            member.unequipHands();
            member.updateRulerCustomName();
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, member.getX(), member.getY() + 1.2D, member.getZ(), 8, 0.3D, 0.3D, 0.3D, 0.05D);
            serverLevel.sendParticles(ParticleTypes.HEART, member.getX(), member.getY() + 1.0D, member.getZ(), 4, 0.2D, 0.2D, 0.2D, 0.05D);
        }

        serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);
        serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    public void enterDownedQueenState(Player player) {
        this.setHealth(1.0F);
        this.setDownedQueen(true);
        this.setMarkedForExecution(false);
        this.playerDuelTargetUUID = null;
        this.duelArenaCenter = null;
        this.setTarget(null);
        this.unequipHands();
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);

        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.ENCHANTED_HIT, this.getX(), this.getY() + 0.8D, this.getZ(), 25, 0.4D, 0.5D, 0.4D, 0.1D);
            sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, this.getX(), this.getY() + 1.0D, this.getZ(), 20, 0.4D, 0.4D, 0.4D, 0.15D);
            sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SHIELD_BREAK, SoundSource.HOSTILE, 1.0F, 0.8F);
            sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 0.9F, 1.2F);
        }

        player.sendSystemMessage(Component.literal("⚔ ¡La Reina ha caído derrotada! Usa tu Báculo de Mando con ella para decidir su destino.")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
    }

    public void executeJudgment(ServerPlayer player, boolean spare) {
        if (player == null || !(this.level() instanceof ServerLevel sl)) return;

        if (spare) {
            String primaryTribe = getPlayerPrimaryTribe(player);
            String oldTribe = this.getTribeName();

            this.setDownedQueen(false);
            this.setMarkedForExecution(false);
            this.setQueen(false);
            this.setSurvivorQueen(false);
            this.setProfession(KoboldProfession.SORCERESS);
            this.setTribeName(primaryTribe);
            this.setOwnerUUID(player.getUUID());
            this.setPersistenceRequired();
            this.setHealth(this.getMaxHealth());
            this.updateRulerCustomName();

            Component speaker = Component.literal("<" + (this.getCustomGivenName().isEmpty() ? "Hechicera" : this.getCustomGivenName()) + "> ").withStyle(ChatFormatting.LIGHT_PURPLE);
            Component msg = Component.literal("Me has perdonado la vida aun cuando la victoria era tuya... Desde hoy pongo mis artes mágicas a tu servicio como Hechicera. Mi pueblo ahora pertenece a la tribu " + primaryTribe + ".")
                    .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD);
            player.sendSystemMessage(speaker.copy().append(msg));

            annexTribeToPlayer(sl, player, oldTribe, primaryTribe, this);

            sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, this.getX(), this.getY() + 1.2D, this.getZ(), 40, 0.5D, 0.5D, 0.5D, 0.15D);
            sl.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 1.2D, this.getZ(), 20, 0.4D, 0.4D, 0.4D, 0.1D);
            sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.TOTEM_USE, SoundSource.NEUTRAL, 1.0F, 1.0F);
            sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);

            int curLevel = player.getPersistentData().getInt("UnlockedBaculoLevel").orElse(1);
            if (curLevel < 7) {
                BaculoItem.upgradePlayerStaff(player, curLevel + 1);
            }
        } else {
            this.setMarkedForExecution(true);

            Component speaker = Component.literal("<" + this.getQueenTitle() + "> ").withStyle(ChatFormatting.GOLD);
            Component msg = Component.literal("Así que este es mi final... Que los ancestros me reciban.")
                    .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC);
            player.sendSystemMessage(speaker.copy().append(msg));

            player.sendSystemMessage(Component.literal("🗡 La Reina ha sido sentenciada a muerte. Un último golpe sellará su destino.")
                    .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));

            sl.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.8D, this.getZ(), 15, 0.3D, 0.4D, 0.3D, 0.05D);
            sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WITHER_DEATH, SoundSource.HOSTILE, 0.6F, 1.6F);
        }
    }

    public boolean isCivilian() {
        return this.getProfession() == KoboldProfession.NONE;
    }

    public boolean isFarmer() {
        return this.getProfession() == KoboldProfession.FARMER;
    }

    public boolean isHunter() {
        return this.getProfession() == KoboldProfession.HUNTER;
    }

    public boolean isBuilder() {
        return this.getProfession() == KoboldProfession.BUILDER;
    }

    public boolean isAlchemist() {
        return this.getProfession() == KoboldProfession.ALCHEMIST;
    }

    public boolean isScout() {
        return this.getProfession() == KoboldProfession.SCOUT;
    }

    public boolean isMiner() {
        return this.getProfession() == KoboldProfession.MINER;
    }

    public boolean isLogger() {
        return this.isBuilder() || this.isCivilian();
    }

    public boolean isWorkStance() {
        return this.getCommandStance() == CommandStance.WORK;
    }

    public void setKoboldRole(KoboldRole role) {
        this.setProfession(role != null ? role.toProfession() : KoboldProfession.NONE);
    }

    public KoboldRole getKoboldRole() {
        return KoboldRole.fromProfession(this.getProfession());
    }

    public boolean hasAxe() {
        if (this.getMainHandItem().is(ItemTags.AXES) || this.getOffhandItem().is(ItemTags.AXES)) return true;
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            if (this.inventory.getItem(i).is(ItemTags.AXES)) return true;
        }
        return false;
    }

    public boolean hasShovel() {
        if (this.getMainHandItem().is(ItemTags.SHOVELS) || this.getOffhandItem().is(ItemTags.SHOVELS)) return true;
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            if (this.inventory.getItem(i).is(ItemTags.SHOVELS)) return true;
        }
        return false;
    }

    public boolean hasHoe() {
        return this.hasHoeInInventory();
    }

    public int countItemInInventory(net.minecraft.world.item.Item item) {
        int count = 0;
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.inventory.getItem(i);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public int countPlanksInInventory() {
        int count = 0;
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.inventory.getItem(i);
            if (stack.is(ItemTags.PLANKS)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public int countLogsInInventory() {
        int count = 0;
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.inventory.getItem(i);
            if (stack.is(ItemTags.LOGS)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public boolean consumePlank(int count) {
        int remaining = count;
        for (int i = 0; i < this.inventory.getContainerSize() && remaining > 0; i++) {
            ItemStack stack = this.inventory.getItem(i);
            if (stack.is(ItemTags.PLANKS)) {
                int toShrink = Math.min(stack.getCount(), remaining);
                stack.shrink(toShrink);
                remaining -= toShrink;
            }
        }
        return remaining == 0;
    }

    public boolean consumeLog(int count) {
        int remaining = count;
        for (int i = 0; i < this.inventory.getContainerSize() && remaining > 0; i++) {
            ItemStack stack = this.inventory.getItem(i);
            if (stack.is(ItemTags.LOGS)) {
                int toShrink = Math.min(stack.getCount(), remaining);
                stack.shrink(toShrink);
                remaining -= toShrink;
            }
        }
        return remaining == 0;
    }

    public boolean hasInInventory(net.minecraft.world.item.Item item, int count) {
        return this.countItemInInventory(item) >= count;
    }

    public boolean hasInInventory(net.minecraft.world.item.Item item) {
        return this.hasItemInInventory(item);
    }

    public boolean consumeItemFromInventory(net.minecraft.world.item.Item item, int count) {
        return this.consumeItem(item, count);
    }

    public boolean isPatrolSquadLeader() {
        if (this.isAnyQueen()) {
            return true;
        }
        return this.patrolSquadIndexInGroup == 0 || this.getUUID().equals(this.patrolSquadLeaderUUID);
    }

    public UUID getPatrolSquadLeaderUUID() {
        return this.patrolSquadLeaderUUID;
    }

    public AbstractTribalEntity getPatrolSquadLeaderEntity() {
        if (this.isPatrolSquadLeader()) {
            return this;
        }
        if (this.patrolSquadLeaderUUID != null && this.level() instanceof ServerLevel serverLevel) {
            Entity ent = serverLevel.getEntity(this.patrolSquadLeaderUUID);
            if (ent instanceof AbstractTribalEntity k && k.isAlive() && k.isDefender()) {
                return k;
            }
        }
        return null;
    }

    public int getPatrolSquadId() {
        return this.patrolSquadId;
    }

    public int getPatrolSquadTotalGroups() {
        return this.patrolSquadTotalGroups;
    }

    public int getPatrolSquadIndexInGroup() {
        return this.patrolSquadIndexInGroup;
    }

    public int getPatrolSquadSize() {
        return this.patrolSquadSize;
    }

    public boolean isInspectingSentry() {
        return this.isInspectingSentry;
    }

    public void setInspectingSentry(boolean inspecting) {
        this.isInspectingSentry = inspecting;
    }

    public void updatePatrolSquad() {
        if (!this.isDefender() || this.getProfession() == KoboldProfession.SORCERESS || this.level().isClientSide()) {
            this.patrolSquadLeaderUUID = null;
            this.patrolSquadId = 0;
            this.patrolSquadTotalGroups = 1;
            this.patrolSquadIndexInGroup = 0;
            this.patrolSquadSize = 1;
            return;
        }

        // Buscar todos los defensores vivos de la misma tribu en un radio de 48 bloques
        List<AbstractTribalEntity> defenders = this.level().getEntitiesOfClass(
                AbstractTribalEntity.class,
                this.getBoundingBox().inflate(48.0D, 16.0D, 48.0D),
                k -> k.isAlive() && k.isDefender() && Objects.equals(k.getTribeName(), this.getTribeName())
        );

        if (defenders.isEmpty()) {
            this.patrolSquadLeaderUUID = this.getUUID();
            this.patrolSquadId = 0;
            this.patrolSquadTotalGroups = 1;
            this.patrolSquadIndexInGroup = 0;
            this.patrolSquadSize = 1;
            return;
        }

        // Ordenamiento determinista por UUID para consistencia mutua
        defenders.sort(Comparator.comparing(Entity::getUUID));

        List<AbstractTribalEntity> guards = new ArrayList<>();
        List<AbstractTribalEntity> vigilantes = new ArrayList<>();

        for (AbstractTribalEntity d : defenders) {
            if (d.getProfession() == KoboldProfession.GUARD) {
                guards.add(d);
            } else if (d.getProfession() == KoboldProfession.VIGILANT) {
                vigilantes.add(d);
            }
        }

        int totalDefenders = defenders.size();
        // Determinar número de grupos K (cada grupo de 2 a 4 miembros)
        int k = Math.max(1, totalDefenders / 2);
        while (k > 1 && (totalDefenders + k - 1) / k > 4) {
            k++;
        }
        while (k > 1 && totalDefenders / k < 2) {
            k--;
        }

        List<List<AbstractTribalEntity>> squads = new ArrayList<>(k);
        for (int i = 0; i < k; i++) {
            squads.add(new ArrayList<>());
        }

        // 1. Repartir Guardianas equitativamente (round-robin)
        for (int i = 0; i < guards.size(); i++) {
            squads.get(i % k).add(guards.get(i));
        }

        // 2. Repartir Vigilantes alternando orden inverso para emparejar equilibradamente con Guardianas
        for (int j = 0; j < vigilantes.size(); j++) {
            int targetSquad = (k - 1 - (j % k)) % k;
            squads.get(targetSquad).add(vigilantes.get(j));
        }

        // Para cada escuadrón, ordenar miembros: Guardiana primero (vanguardia con escudo), luego Vigilante
        for (int s = 0; s < k; s++) {
            List<AbstractTribalEntity> squad = squads.get(s);
            squad.sort((a, b) -> {
                if (a.getProfession() == KoboldProfession.GUARD && b.getProfession() != KoboldProfession.GUARD) return -1;
                if (a.getProfession() != KoboldProfession.GUARD && b.getProfession() == KoboldProfession.GUARD) return 1;
                return a.getUUID().compareTo(b.getUUID());
            });

            for (int idx = 0; idx < squad.size(); idx++) {
                AbstractTribalEntity member = squad.get(idx);
                if (member.getUUID().equals(this.getUUID())) {
                    this.patrolSquadId = s;
                    this.patrolSquadTotalGroups = k;
                    this.patrolSquadIndexInGroup = idx;
                    this.patrolSquadSize = squad.size();
                    this.patrolSquadLeaderUUID = squad.isEmpty() ? this.getUUID() : squad.get(0).getUUID();
                    return;
                }
            }
        }

        // Fallback
        this.patrolSquadLeaderUUID = this.getUUID();
        this.patrolSquadId = 0;
        this.patrolSquadTotalGroups = 1;
        this.patrolSquadIndexInGroup = 0;
        this.patrolSquadSize = 1;
    }

    public boolean isQueen() {
        return this.entityData.get(DATA_IS_QUEEN);
    }

    public void setQueen(boolean queen) {
        this.entityData.set(DATA_IS_QUEEN, queen);
        if (queen) {
            this.setPersistenceRequired();
        }
    }

    public boolean isSubdita() {
        return !this.isAnyQueen();
    }

    public boolean isAnyQueen() {
        return this.isQueen() || this.isSurvivorQueen();
    }

    public boolean isReincarnating() {
        return this.entityData.get(DATA_IS_REINCARNATING);
    }

    public void setReincarnating(boolean val) {
        this.entityData.set(DATA_IS_REINCARNATING, val);
    }

    public void startQueenReincarnation(String soulName, String soulTribe, ServerPlayer player) {
        if (this.isSleeping()) {
            this.stopSleeping();
        }
        this.setReincarnating(true);
        this.reincarnationTicksRemaining = 200; // 10 segundos
        this.pendingReincarnatedQueenName = soulName;
        this.reincarnationInitiatorPlayerUUID = player.getUUID();
        this.setTribeName(soulTribe);
        this.setOwnerUUID(player.getUUID());
        this.setRebel(false);
        this.setInPowerStruggle(false);
        this.setSurvivorQueen(false);
        this.setTarget(null);
        this.getNavigation().stop();
        this.unequipHands();

        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + 1.2D, this.getZ(), 30, 0.4D, 0.5D, 0.4D, 0.08D);
            sl.sendParticles(ParticleTypes.SOUL, this.getX(), this.getY() + 1.0D, this.getZ(), 20, 0.3D, 0.4D, 0.3D, 0.04D);
            sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ZOMBIE_VILLAGER_CURE, SoundSource.NEUTRAL, 1.0F, 1.0F);
            sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.NEUTRAL, 1.0F, 0.8F);
        }
    }

    public void finishQueenReincarnation() {
        this.setReincarnating(false);
        this.reincarnationTicksRemaining = 0;
        this.setQueen(true);
        this.setSurvivorQueen(false);
        this.setRebel(false);
        this.setInPowerStruggle(false);
        this.setProfession(KoboldProfession.NONE);
        this.setPersistenceRequired();

        if (this.pendingReincarnatedQueenName != null && !this.pendingReincarnatedQueenName.isEmpty()) {
            if (!this.pendingReincarnatedQueenName.startsWith("Reina")) {
                this.setCustomGivenName(this.pendingReincarnatedQueenName);
            }
        }
        this.updateRulerCustomName();

        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, this.getX(), this.getY() + 1.2D, this.getZ(), 50, 0.5D, 0.6D, 0.5D, 0.15D);
            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + 1.0D, this.getZ(), 30, 0.4D, 0.5D, 0.4D, 0.08D);
            sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.2D, this.getZ(), 20, 0.4D, 0.4D, 0.4D, 0.05D);
            sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.NEUTRAL, 1.0F, 1.0F);
            sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 1.0F, 1.2F);
        }

        if (this.reincarnationInitiatorPlayerUUID != null) {
            Player player = this.level().getPlayerByUUID(this.reincarnationInitiatorPlayerUUID);
            if (player != null) {
                player.sendSystemMessage(Component.literal("👑 ¡La reencarnación ha concluido! ")
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                        .append(Component.literal("La reina " + this.getQueenTitle() + " ha renacido para gobernar la tribu " + this.getTribeName() + ".")
                                .withStyle(ChatFormatting.YELLOW)));
            }
        }
    }

    public void renameTribe(ServerPlayer player, String newTribeName) {
        if (!this.isAnyQueen() || !this.isOwnedBy(player)) return;
        String oldTribeName = this.getTribeName();
        this.setTribeName(newTribeName);
        this.updateRulerCustomName();
        player.getPersistentData().putString("PlayerOwnedTribe", newTribeName);

        if (this.level() instanceof ServerLevel serverLevel) {
            // Obtener todas las integrantes (leales y rebeldes) que pertenecían a la tribu o están vinculadas al jugador
            List<AbstractTribalEntity> members = serverLevel.getEntitiesOfClass(
                    AbstractTribalEntity.class,
                    this.getBoundingBox().inflate(128.0D),
                    k -> k != this && k.isAlive() && (oldTribeName.isEmpty() ? k.isOwnedBy(player) : (oldTribeName.equals(k.getTribeName()) || k.isOwnedBy(player)))
            );

            List<AbstractTribalEntity> agitatedRebels = new ArrayList<>();

            for (AbstractTribalEntity member : members) {
                member.setTribeName(newTribeName);
                member.updateRulerCustomName();
                member.setPersistenceRequired();

                if (member.isRebel()) {
                    agitatedRebels.add(member);
                } else {
                    serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, member.getX(), member.getY() + 1.2D, member.getZ(), 6, 0.2D, 0.2D, 0.2D, 0.05D);
                }
            }

            serverLevel.sendParticles(ParticleTypes.FIREWORK, this.getX(), this.getY() + 1.5D, this.getZ(), 25, 0.4D, 0.5D, 0.4D, 0.1D);
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.1F);

            // Reacción de súbditas rebeldes ante el cambio de nombre:
            // Se oponen al cambio atacando al jugador en señal de protesta (NO atacan a la Reina).
            // La Reina interviene inmediatamente con una reprimenda para someterlas y cesar la hostilidad.
            if (!agitatedRebels.isEmpty()) {
                this.setReprimandCooldown(0);
                this.setForcedReprimandTarget(agitatedRebels.get(0));

                for (AbstractTribalEntity rebel : agitatedRebels) {
                    rebel.setTarget(player);
                    rebel.equipForCombat(true);
                    rebel.setReprimandedCooldown(0);

                    serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER, rebel.getX(), rebel.getY() + 1.2D, rebel.getZ(), 14, 0.3D, 0.3D, 0.3D, 0.05D);
                    serverLevel.playSound(null, rebel.getX(), rebel.getY(), rebel.getZ(), SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, 1.0F, 0.9F);

                    String rebelName = rebel.getCustomGivenName().isEmpty()
                            ? rebel.getProfession().getDisplayName() + " Rebelde"
                            : rebel.getCustomGivenName() + " [" + rebel.getProfession().getDisplayName() + " Rebelde]";

                    player.sendSystemMessage(Component.literal("<" + rebelName + "> ").withStyle(ChatFormatting.RED)
                            .append(Component.literal("¡Jamás aceptaremos que borres el nombre de nuestra tribu! ¡La tribu " + oldTribeName + " no se inclinará ante tus caprichos!")
                                    .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC)));
                }

                Component queenWarningSpeaker = Component.literal("<" + this.getQueenTitle() + "> ").withStyle(ChatFormatting.GOLD);
                player.sendSystemMessage(queenWarningSpeaker.copy().append(Component.literal("¡Insolentes! ¡No toleraré que ataquen a nuestro gobernante por este cambio sagrado!")
                        .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)));
            }
        }

        Component speaker = Component.literal("<" + this.getQueenTitle() + "> ").withStyle(ChatFormatting.GOLD);
        player.sendSystemMessage(speaker.copy().append(Component.literal("Por la voluntad de nuestro gobernante, nuestra tribu ahora será conocida formalmente como ")
                .withStyle(ChatFormatting.GREEN))
                .append(Component.literal(newTribeName).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .append(Component.literal(".")));
    }

    public boolean isApproachingBaculo() {
        return this.isApproachingBaculo;
    }

    public void setApproachingBaculo(boolean approaching) {
        this.isApproachingBaculo = approaching;
    }

    public boolean isStaringAtBaculo() {
        return this.isStaringAtBaculo;
    }

    public void setStaringAtBaculo(boolean staring) {
        this.isStaringAtBaculo = staring;
    }

    public static boolean isHoldingBaculo(LivingEntity entity) {
        if (entity == null) return false;
        return entity.getMainHandItem().is(ModItems.BACULO.get()) || entity.getOffhandItem().is(ModItems.BACULO.get());
    }

    public boolean isReveringBaculoHolder(Player player) {
        if (player == null || !player.isAlive() || player.isSpectator()) return false;
        if (!isHoldingBaculo(player)) return false;
        if (this.isInPlayerDuelWith(player)) return false;
        if (this.isRebel()) return false;
        if (this.getLastHurtByMob() == player && (this.tickCount - this.getLastHurtByMobTimestamp() < 100)) {
            return false;
        }
        return true;
    }

    public @Nullable UUID getOwnerUUID() {
        String s = this.entityData.get(DATA_OWNER_UUID);
        if (s == null || s.isEmpty()) return null;
        try {
            return UUID.fromString(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public void setOwnerUUID(@Nullable UUID ownerUUID) {
        this.entityData.set(DATA_OWNER_UUID, ownerUUID != null ? ownerUUID.toString() : "");
        if (ownerUUID != null) {
            this.setPersistenceRequired();
        }
    }

    public @Nullable AbstractTribalEntity getTribeQueen() {
        String myTribe = this.getTribeName();
        UUID myOwner = this.getOwnerUUID();
        List<AbstractTribalEntity> queens = this.level().getEntitiesOfClass(
                AbstractTribalEntity.class,
                this.getBoundingBox().inflate(128.0D),
                k -> k != this && k.isAnyQueen() && k.isAlive() && (
                        (!myTribe.isEmpty() && myTribe.equals(k.getTribeName()))
                        || (myOwner != null && myOwner.equals(k.getOwnerUUID()))
                )
        );
        if (!queens.isEmpty()) {
            AbstractTribalEntity q = queens.get(0);
            if (myTribe.isEmpty() && !q.getTribeName().isEmpty()) {
                this.setTribeName(q.getTribeName());
            }
            return q;
        }
        return null;
    }

    public @Nullable AbstractTribalEntity getTribeQueen(ServerLevel level) {
        return this.getTribeQueen();
    }

    public boolean isOwned() {
        if (this.isRebel()) return false;
        if (!this.entityData.get(DATA_OWNER_UUID).isEmpty()) return true;
        if (this.hasTribe() && this.level() instanceof ServerLevel serverLevel) {
            AbstractTribalEntity queen = this.getTribeQueen(serverLevel);
            if (queen != null && queen != this && queen.isOwned()) {
                this.setOwnerUUID(queen.getOwnerUUID());
                return true;
            }
        }
        return false;
    }

    public boolean isOwnedBy(LivingEntity entity) {
        if (entity == null) return false;
        if (this.isRebel()) return false;
        UUID owner = this.getOwnerUUID();
        if (owner != null && owner.equals(entity.getUUID())) {
            return true;
        }
        if (this.hasTribe() && this.level() instanceof ServerLevel serverLevel) {
            AbstractTribalEntity queen = this.getTribeQueen(serverLevel);
            if (queen != null && queen != this) {
                UUID queenOwner = queen.getOwnerUUID();
                if (queenOwner != null && queenOwner.equals(entity.getUUID())) {
                    this.setOwnerUUID(entity.getUUID());
                    return true;
                }
            }
        }
        return false;
    }

    public @Nullable Player getOwner() {
        UUID uuid = this.getOwnerUUID();
        if (uuid == null && this.hasTribe() && this.level() instanceof ServerLevel serverLevel) {
            AbstractTribalEntity queen = this.getTribeQueen(serverLevel);
            if (queen != null && queen != this) {
                uuid = queen.getOwnerUUID();
                if (uuid != null) {
                    this.setOwnerUUID(uuid);
                }
            }
        }
        if (uuid == null) return null;
        return this.level().getPlayerByUUID(uuid);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        if (this.isOwned() || this.isAnyQueen() || this.hasCustomName()) {
            return false;
        }
        return super.removeWhenFarAway(distanceToClosestPlayer);
    }

    @Override
    public boolean requiresCustomPersistence() {
        return super.requiresCustomPersistence() || this.isOwned() || this.isAnyQueen();
    }

    private @Nullable UUID playerDuelTargetUUID = null;

    public boolean isInPlayerDuel() {
        return this.playerDuelTargetUUID != null;
    }

    public boolean isInPlayerDuelWith(Player player) {
        return player != null && player.getUUID().equals(this.playerDuelTargetUUID);
    }

    public @Nullable UUID getPlayerDuelTargetUUID() {
        return this.playerDuelTargetUUID;
    }

    public @Nullable Player getPlayerDuelTarget() {
        if (this.playerDuelTargetUUID == null) return null;
        return this.level().getPlayerByUUID(this.playerDuelTargetUUID);
    }

    public void setPlayerDuelTargetUUID(@Nullable UUID targetUUID) {
        this.playerDuelTargetUUID = targetUUID;
        if (targetUUID == null) {
            this.duelArenaCenter = null;
        }
    }

    private @Nullable Vec3 duelArenaCenter = null;

    public @Nullable Vec3 getDuelArenaCenter() {
        if (!this.isInQueenDuel() && !this.isInPlayerDuel()) {
            this.duelArenaCenter = null;
            return null;
        }
        if (this.duelArenaCenter == null) {
            if (this.isInQueenDuel() && this.getTarget() instanceof AbstractTribalEntity rival) {
                Vec3 rivalCenter = rival.duelArenaCenter;
                if (rivalCenter != null) {
                    this.duelArenaCenter = rivalCenter;
                } else {
                    Vec3 midpoint = this.position().add(rival.position()).scale(0.5D);
                    this.duelArenaCenter = midpoint;
                    rival.duelArenaCenter = midpoint;
                }
            } else if (this.isInPlayerDuel()) {
                Player p = this.getPlayerDuelTarget();
                if (p != null) {
                    this.duelArenaCenter = this.position().add(p.position()).scale(0.5D);
                } else {
                    this.duelArenaCenter = this.position();
                }
            }
        }
        return this.duelArenaCenter;
    }

    public void setDuelArenaCenter(@Nullable Vec3 center) {
        this.duelArenaCenter = center;
    }

    public double getDuelArenaBarrierRadius() {
        Vec3 center = this.getDuelArenaCenter();
        if (center == null) return 5.5D;
        Entity other = this.isInPlayerDuel() ? this.getPlayerDuelTarget() : this.getTarget();
        double specRadius = getDuelSpectatorRadius(this.level(), center, this, other);
        return Math.max(4.5D, specRadius - 0.75D);
    }

    public static @Nullable AbstractTribalEntity getActivePlayerDuelQueen(Player player) {
        if (player == null || !(player.level() instanceof ServerLevel serverLevel)) return null;
        List<AbstractTribalEntity> queens = serverLevel.getEntitiesOfClass(
                AbstractTribalEntity.class,
                player.getBoundingBox().inflate(48.0D),
                q -> q.isAnyQueen() && q.isAlive() && q.isInPlayerDuelWith(player)
        );
        return queens.isEmpty() ? null : queens.get(0);
    }

    private boolean isSpectatingDuel = false;

    public boolean isSpectatingDuel() {
        if (this.isAnyQueen() || this.isRebel() || this.isInPowerStruggle() || !this.isAlive()) {
            this.isSpectatingDuel = false;
            return false;
        }
        return this.isSpectatingDuel;
    }

    public void setSpectatingDuel(boolean spectating) {
        this.isSpectatingDuel = spectating;
    }

    //Ruta pacifista en caso de que el jugador no quiera hacer duelo a muerte con cuchillos contra la reina por el dominio de la tribu

    private boolean pacifistRouteChosen = false;
    protected @Nullable MerchantOffers pacifistOffers = null;
    private int completedPacifistTrades = 0;
    private @Nullable Player tradingPlayer = null;

    public boolean isPacifistRouteChosen() {
        return this.pacifistRouteChosen;
    }

    public void setPacifistRouteChosen(boolean chosen) {
        this.pacifistRouteChosen = chosen;
    }

    @Override
    public void setTradingPlayer(@Nullable Player player) {
        this.tradingPlayer = player;
    }

    @Override
    public @Nullable Player getTradingPlayer() {
        return this.tradingPlayer;
    }

    @Override
    public MerchantOffers getOffers() {
        if (this.pacifistOffers == null) {
            this.pacifistOffers = createPacifistOffers();
        } else {
            // Migrar ofertas previas si contenían la antigua pepita de oro
            for (MerchantOffer offer : this.pacifistOffers) {
                if (offer.getResult().is(Items.GOLD_NUGGET)) {
                    this.pacifistOffers = createPacifistOffers();
                    break;
                }
            }
        }
        return this.pacifistOffers;
    }

    @Override
    public void overrideOffers(MerchantOffers offers) {
        this.pacifistOffers = offers;
    }

    @Override
    public void notifyTrade(MerchantOffer offer) {
        this.ambientSoundTime = -this.getAmbientSoundInterval();
        this.playSound(this.getNotifyTradeSound(), this.getSoundVolume(), this.getVoicePitch());
        this.completedPacifistTrades++;

        if (this.tradingPlayer instanceof ServerPlayer serverPlayer) {
            // Recompensa de 5 niveles de experiencia por cada misión de tributo completada
            serverPlayer.giveExperienceLevels(5);
            serverPlayer.playSound(SoundEvents.PLAYER_LEVELUP, 0.8F, 1.2F);

            int completed = countCompletedOffers();
            int total = this.getOffers().size();

            // Actualizar la pantalla de comercio del cliente para que la barra de confianza avance en tiempo real
            if (serverPlayer.containerMenu != null && serverPlayer.containerMenu.containerId != 0) {
                serverPlayer.sendMerchantOffers(
                        serverPlayer.containerMenu.containerId,
                        this.getOffers(),
                        1,
                        this.getVillagerXp(),
                        this.showProgressBar(),
                        this.canRestock()
                );
            }

            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.2D, this.getZ(), 10, 0.4D, 0.4D, 0.4D, 0.05D);
                serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, serverPlayer.getX(), serverPlayer.getY() + 1.0D, serverPlayer.getZ(), 16, 0.35D, 0.5D, 0.35D, 0.08D);
            }

            Component speaker = Component.literal("<" + this.getQueenTitle() + "> ").withStyle(ChatFormatting.GOLD);
            if (completed < total) {
                int percent = completed * 10;
                Component progressMsg = Component.literal("¡Gran ofrenda para nuestro pueblo! Confianza: " + percent + "% (" + completed + "/" + total + " tributos). ¡Has recibido +5 niveles de experiencia!")
                        .withStyle(ChatFormatting.GREEN);
                serverPlayer.sendSystemMessage(speaker.copy().append(progressMsg));
            } else {
                serverPlayer.sendSystemMessage(speaker.copy().append(Component.literal("¡Tributo final recibido! ¡Has obtenido +5 niveles de experiencia!").withStyle(ChatFormatting.GREEN)));
                this.resolvePlayerPacifistVictory(serverPlayer);
            }
        }
    }

    @Override
    public void notifyTradeUpdated(ItemStack stack) {
    }

    @Override
    public int getVillagerXp() {
        return countCompletedOffers();
    }

    @Override
    public void overrideXp(int xp) {
    }

    @Override
    public boolean showProgressBar() {
        return true;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.VILLAGER_YES;
    }

    @Override
    public boolean canRestock() {
        return false;
    }

    @Override
    public boolean isClientSide() {
        return this.level().isClientSide();
    }

    @Override
    public boolean stillValid(Player player) {
        return this.isAlive() && this.distanceToSqr(player) <= 64.0D;
    }

    public int countCompletedOffers() {
        if (this.pacifistOffers == null) return 0;
        int count = 0;
        for (MerchantOffer offer : this.pacifistOffers) {
            if (offer.getUses() >= offer.getMaxUses() || offer.isOutOfStock()) {
                count++;
            }
        }
        return count;
    }

    public boolean areAllPacifistOffersCompleted() {
        if (this.pacifistOffers == null || this.pacifistOffers.isEmpty()) return false;
        for (MerchantOffer offer : this.pacifistOffers) {
            if (!offer.isOutOfStock() && offer.getUses() < offer.getMaxUses()) {
                return false;
            }
        }
        return true;
    }

    public void resetPacifistTrades() {
        this.pacifistOffers = createPacifistOffers();
        this.completedPacifistTrades = 0;
    }

    public void resolvePlayerPacifistVictory(Player player) {
        if (player == null || !(this.level() instanceof ServerLevel level)) return;
        if (this.isOwnedBy(player)) return;

        String primaryTribe = getPlayerPrimaryTribe(player);
        boolean isSecondaryTribe = !primaryTribe.isEmpty() && !primaryTribe.equals(this.getTribeName());

        if (isSecondaryTribe) {
            String oldTribe = this.getTribeName();
            // Abdica del trono y adquiere la profesión de Hechicera manteniendo sus poderes mágicos
            this.setQueen(false);
            this.setSurvivorQueen(false);
            this.setProfession(KoboldProfession.SORCERESS);
            this.setOwnerUUID(player.getUUID());
            this.setTribeName(primaryTribe);
            this.setRebel(false);
            this.setHealth(this.getMaxHealth());
            this.setPersistenceRequired();
            this.updateRulerCustomName();

            Component speaker = Component.literal("<" + (this.getCustomGivenName().isEmpty() ? "Hechicera" : this.getCustomGivenName()) + "> ").withStyle(ChatFormatting.LIGHT_PURPLE);
            Component peacefulRulerMsg = Component.literal("Has demostrado tu sabiduría y bondad hacia mi pueblo. Cedo mi corona con honor y me pongo a tu servicio como Hechicera. Desde hoy nos unimos a la tribu " + primaryTribe + ".")
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
            player.sendSystemMessage(speaker.copy().append(peacefulRulerMsg));

            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.5D, this.getZ(), 50, 0.8D, 0.8D, 0.8D, 0.1D);
            level.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 1.2D, this.getZ(), 25, 0.6D, 0.6D, 0.6D, 0.1D);
            level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.NEUTRAL, 1.0F, 1.0F);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);

            // Anexar toda la tribu secundaria a la tribu del jugador
            annexTribeToPlayer(level, player, oldTribe, primaryTribe, this);

            player.sendSystemMessage(Component.literal("🎉 ¡Alianza pacífica completada! La tribu " + oldTribe + " ha sido anexada a tu tribu " + primaryTribe + ". La antigua reina ahora te sirve como Hechicera.")
                    .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));

            int curLevel = player.getPersistentData().getInt("UnlockedBaculoLevel").orElse(1);
            if (curLevel < 7) {
                BaculoItem.upgradePlayerStaff(player, curLevel + 1);
            }
            return;
        }

        player.getPersistentData().putString("PlayerOwnedTribe", this.getTribeName());
        this.setOwnerUUID(player.getUUID());
        this.setRebel(false);
        this.setHealth(this.getMaxHealth());
        this.setPersistenceRequired();
        this.updateRulerCustomName();

        // Diálogos de agradecimiento
        Component speaker = Component.literal("<" + this.getQueenTitle() + "> ").withStyle(ChatFormatting.GOLD);
        Component peacefulRulerMsg = Component.literal("¡Tu generosidad y liderazgo han transformado a nuestra tribu! Has demostrado ser un verdadero protector y guía. ¡Toda la tribu te acepta con honor como nuestro legítimo gobernante!")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
        player.sendSystemMessage(speaker.copy().append(peacefulRulerMsg));

        // Efectos de celebracion por esclavitud pacifica
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.5D, this.getZ(), 50, 0.8D, 0.8D, 0.8D, 0.1D);
        level.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 1.2D, this.getZ(), 25, 0.6D, 0.6D, 0.6D, 0.1D);
        level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.NEUTRAL, 1.0F, 1.0F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);

        // Buscar a las súbditas de su tribu en un radio de 128 bloques
        String tribeName = this.getTribeName();
        List<AbstractTribalEntity> subditas = level.getEntitiesOfClass(
                AbstractTribalEntity.class,
                this.getBoundingBox().inflate(128.0D),
                k -> !k.isAnyQueen() && k.isAlive() && (tribeName.isEmpty() || k.getTribeName().equals(tribeName))
        );

        int loyalCount = 0;
        for (AbstractTribalEntity subdita : subditas) {
            subdita.setOwnerUUID(player.getUUID());
            subdita.setRebel(false);
            subdita.setPersistenceRequired();
            subdita.setTarget(null);
            subdita.unequipHands();
            subdita.updateRulerCustomName();
            level.sendParticles(ParticleTypes.HEART, subdita.getX(), subdita.getY() + 1.0D, subdita.getZ(), 5, 0.3D, 0.3D, 0.3D, 0.05D);
            loyalCount++;
        }

        player.sendSystemMessage(Component.literal("🎉 ¡Alianza pacífica completada! " + loyalCount + " Kobold Girl(s) de la tribu se han unido a ti con total devoción.")
                .withStyle(ChatFormatting.GREEN));

        // Ascender el Báculo de Mando del jugador al Nivel 2 (Baldosa de Pizarra Profunda)
        BaculoItem.upgradePlayerStaff(player, 2);
    }

    public String getQueenTitle() {
        String title = this.isSurvivorQueen() ? "Reina Sobreviviente" : "Reina";
        if (this.hasTribe()) {
            title += " (" + this.getTribeName() + ")";
        }
        return title;
    }

    public String getDisplayNameWithTribe() {
        String name = this.isAnyQueen() ? this.getQueenTitle() : this.getProfession().getDisplayName();
        if (name.isEmpty() || name.equals("Ninguna")) {
            name = "Kobold Girl";
        }
        if (!this.isAnyQueen() && this.hasTribe()) {
            name += " (" + this.getTribeName() + ")";
        }
        return name;
    }

    public String getTribeName() {
        return this.entityData.get(DATA_TRIBE_NAME);
    }

    public void setTribeName(String tribeName) {
        this.entityData.set(DATA_TRIBE_NAME, tribeName != null ? tribeName : "");
    }

    public boolean hasTribe() {
        String tribe = this.getTribeName();
        return tribe != null && !tribe.isEmpty();
    }

    public static final Set<String> WITNESSED_BACULO_TRIBES = Collections.synchronizedSet(new HashSet<>());
    protected boolean tribeWitnessedBaculo = false;
    private int baculoStareCooldown = 0;

    public boolean hasTribeWitnessedBaculo() {
        if (this.hasTribe() && WITNESSED_BACULO_TRIBES.contains(this.getTribeName())) {
            this.tribeWitnessedBaculo = true;
            return true;
        }
        return this.tribeWitnessedBaculo;
    }

    public void markTribeWitnessedBaculo() {
        this.tribeWitnessedBaculo = true;
        if (this.hasTribe()) {
            WITNESSED_BACULO_TRIBES.add(this.getTribeName());
            if (this.level() instanceof ServerLevel serverLevel) {
                List<AbstractTribalEntity> tribeMates = serverLevel.getEntitiesOfClass(
                        AbstractTribalEntity.class,
                        this.getBoundingBox().inflate(64.0D),
                        k -> Objects.equals(k.getTribeName(), this.getTribeName())
                );
                for (AbstractTribalEntity mate : tribeMates) {
                    mate.tribeWitnessedBaculo = true;
                }
            }
        }
    }

    public int getBaculoStareCooldown() {
        return this.baculoStareCooldown;
    }

    public void setBaculoStareCooldown(int cooldown) {
        this.baculoStareCooldown = cooldown;
    }

    public boolean isRebel() {
        return this.entityData.get(DATA_IS_REBEL);
    }

    public void setRebel(boolean rebel) {
        this.entityData.set(DATA_IS_REBEL, rebel);
    }

    public boolean isInPowerStruggle() {
        return this.entityData.get(DATA_IS_IN_POWER_STRUGGLE);
    }

    public void setInPowerStruggle(boolean inPowerStruggle) {
        this.entityData.set(DATA_IS_IN_POWER_STRUGGLE, inPowerStruggle);
    }

    public boolean isSurvivorQueen() {
        return this.entityData.get(DATA_IS_SURVIVOR_QUEEN);
    }

    public void setSurvivorQueen(boolean survivorQueen) {
        this.entityData.set(DATA_IS_SURVIVOR_QUEEN, survivorQueen);
        if (survivorQueen) {
            this.setPersistenceRequired();
        }
    }

    public boolean isInQueenDuel() {
        if (!this.isAnyQueen() || !this.hasTribe()) return false;
        LivingEntity target = this.getTarget();
        return target instanceof AbstractTribalEntity rival
                && rival.isAnyQueen()
                && rival.isAlive()
                && rival.hasTribe()
                && !this.getTribeName().equals(rival.getTribeName());
    }
        //Aquí pasito a pasito que después me enredo
    public boolean canFightTribal(AbstractTribalEntity other) {
        if (other == null || !other.isAlive()) return false;
        // Si cualquiera de las dos está espectando un duelo activo, no combaten entre sí
        if (this.isSpectatingDuel() || other.isSpectatingDuel()) {
            return false;
        }
        // 0. Guerra civil por el trono de la tribu
        if (this.isInPowerStruggle() && other.isInPowerStruggle()) {
            return true;
        }
        // 1. Duelo a muerte entre Reinas de diferentes tribus
        if (this.isQueen() && other.isQueen()) {
            return this.hasTribe() && other.hasTribe() && !this.getTribeName().equals(other.getTribeName());
        }
        // 2. Conflicto entre Reina y Súbdita Rebelde:
        // En tribus pertenecientes al jugador, la Reina leal y la súbdita rebelde de la misma tribu NUNCA combaten a muerte
        // (la reina solo aplica disciplina no letal con puñetazos de reprimenda y la rebelde nunca toma a la reina como rival).
        // En tribus salvajes (sin dueño), la reina y las rebeldes sí pueden combatir entre sí.
        boolean thisIsQueen = this.isAnyQueen();
        boolean otherIsQueen = other.isAnyQueen();
        if ((thisIsQueen && other.isRebel()) || (this.isRebel() && otherIsQueen)) {
            AbstractTribalEntity queen = thisIsQueen ? this : other;
            AbstractTribalEntity rebel = thisIsQueen ? other : this;
            if (queen.isOwned()) {
                if (rebel.getTribeName().equals(queen.getTribeName()) || rebel.distanceToSqr(queen) <= 64.0D * 64.0D) {
                    return false;
                }
            }
            if (!this.isOwned() && !other.isOwned()) {
                return true;
            }
        }
        // 3. Súbditas leales al jugador NUNCA atacan a rebeldes de su pueblo (la Reina se encarga con un solo coñazo disciplinario)
        if ((this.isOwned() && !this.isAnyQueen() && other.isRebel()) || (this.isRebel() && other.isOwned() && !other.isAnyQueen())) {
            return false;
        }
        // 4. Si esta Kobold es rebelde, solo ataca a kobolds de tribus rivales (y jamás a una reina perteneciente al jugador)
        if (this.isRebel()) {
            if (other.isRebel()) return false;
            if (other.isAnyQueen() && other.isOwned()) return false;
            return !this.getTribeName().equals(other.getTribeName());
        }
        // 5. Si la otra Kobold es rebelde pero esta no es leal al jugador osea es de tribu rival salvaje
        if (other.isRebel()) {
            if (this.isAnyQueen() && this.isOwned()) return false;
            return !this.isRebel() && !this.isOwned();
        }
        return false;
    }

    // Aqui la comida y vida de las kobolds

    public int getFoodLevel() {
        return this.entityData.get(DATA_FOOD_LEVEL);
    }

    public void setFoodLevel(int food) {
        this.entityData.set(DATA_FOOD_LEVEL, Math.clamp(food, 0, 20));
    }

    public float getSaturationLevel() {
        return this.entityData.get(DATA_SATURATION_LEVEL);
    }

    public void setSaturationLevel(float saturation) {
        this.entityData.set(DATA_SATURATION_LEVEL, Math.clamp(saturation, 0.0F, (float) this.getFoodLevel()));
    }

    public boolean needsFood() {
        return this.getFoodLevel() < 20;
    }

    public boolean hasFoodInInventory() {
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.inventory.getItem(i);
            if (stack.has(DataComponents.FOOD)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);

        // Si está en estado Downed (derrotada a 1 HP esperando juicio): se mantiene totalmente inmóvil
        if (this.isDownedQueen()) {
            this.getNavigation().stop();
            this.setDeltaMovement(Vec3.ZERO);
            this.setTarget(null);
            this.unequipHands();
            return;
        }

        // Gestión de reencarnación del alma de la reina mediante el Báculo
        if (this.isReincarnating()) {
            this.getNavigation().stop();
            this.setTarget(null);
            this.unequipHands();

            if (this.reincarnationTicksRemaining > 0) {
                this.reincarnationTicksRemaining--;

                // Partículas de alma desprendiéndose continuamente
                level.sendParticles(ParticleTypes.SOUL, this.getX(), this.getY() + 0.8D, this.getZ(), 4, 0.25D, 0.35D, 0.25D, 0.03D);
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + 0.6D, this.getZ(), 2, 0.2D, 0.3D, 0.2D, 0.02D);
                if (this.random.nextFloat() < 0.25F) {
                    level.sendParticles(ParticleTypes.SCULK_SOUL, this.getX(), this.getY() + 0.5D, this.getZ(), 1, 0.1D, 0.1D, 0.1D, 0.02D);
                }

                if (this.reincarnationTicksRemaining % 30 == 0) {
                    level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.NEUTRAL, 0.5F, 1.4F);
                }

                if (this.reincarnationTicksRemaining <= 0) {
                    this.finishQueenReincarnation();
                }
            } else {
                this.finishQueenReincarnation();
            }
            return;
        }

        // Si hay una amenaza viva como objetivo, mantener el tiempo de espera en 5 segundos
        if (this.getTarget() != null && this.getTarget().isAlive()) {
            this.sleepCooldown = 100;
        } else if (this.sleepCooldown > 0) {
            this.sleepCooldown--;
        }

        // Ticks de cooldown de la Reina jodiendo a la reebelde y la retirada de rebeldes
        if (this.reprimandCooldown > 0) {
            this.reprimandCooldown--;
        }
        if (this.reprimandedCooldown > 0) {
            this.reprimandedCooldown--;
        }
        if (this.baculoStareCooldown > 0) {
            this.baculoStareCooldown--;
        }

        // Detección de proximidad: Reina de otra tribu advirtiendo al jugador que ya gobierna una tribu
        if (this.isAnyQueen() && !this.isOwned() && !this.isDownedQueen() && !this.isInPlayerDuel() && !this.isInQueenDuel() && this.tickCount % 20 == 0) {
            Player nearbyPlayer = this.level().getNearestPlayer(this, 9.0D);
            if (nearbyPlayer instanceof ServerPlayer serverPlayer && !serverPlayer.isSpectator() && !serverPlayer.isCreative()) {
                if (doesPlayerOwnAnyTribe(serverPlayer)) {
                    long now = this.level().getGameTime();
                    Long lastGreet = LAST_AURA_GREETING_TICK.get(serverPlayer.getUUID());
                    if (lastGreet == null || now - lastGreet > 600L) { // 30 segundos de enfriamiento
                        LAST_AURA_GREETING_TICK.put(serverPlayer.getUUID(), now);
                        this.getLookControl().setLookAt(serverPlayer, 30.0F, 30.0F);
                        this.setYBodyRot(this.getYHeadRot());

                        Component speaker = Component.literal("<" + this.getQueenTitle() + "> ").withStyle(ChatFormatting.GOLD);
                        Component auraMsg = Component.literal("Esa aura es inconfundible... pero me sorprende que sea un humano quien la porta. Dime ¿acaso vienes a someter a mi pueblo?")
                                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC);
                        serverPlayer.sendSystemMessage(speaker.copy().append(auraMsg));

                        level.sendParticles(ParticleTypes.ENCHANT, this.getX(), this.getY() + 1.2D, this.getZ(), 14, 0.4D, 0.4D, 0.4D, 0.05D);
                        level.sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 1.0D, this.getZ(), 8, 0.3D, 0.3D, 0.3D, 0.02D);
                        level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ILLUSIONER_PREPARE_BLINDNESS, SoundSource.NEUTRAL, 0.8F, 1.2F);
                    }
                }
            }
        }

        // Monitoreo del duelo a casi muerte contra el jugador
        if (this.playerDuelTargetUUID != null) {
            Player duelPlayer = this.level().getPlayerByUUID(this.playerDuelTargetUUID);
            if (duelPlayer == null || !duelPlayer.isAlive() || this.distanceToSqr(duelPlayer) > 48.0D * 48.0D) {
                if (duelPlayer != null && !duelPlayer.isAlive()) {
                    Component speaker = Component.literal("<" + this.getQueenTitle() + "> ").withStyle(ChatFormatting.GOLD);
                    duelPlayer.sendSystemMessage(speaker.copy().append(Component.literal("Tu soberbia te ha costado caro, forastero. Nadie desafía el dominio de mi tribu en vano.")
                            .withStyle(ChatFormatting.RED)));
                }
                this.playerDuelTargetUUID = null;
                this.duelArenaCenter = null;
                this.setTarget(null);
                this.unequipHands();
                this.heal(10.0F);
            }
        }

        // Barrera mágica infranqueable para duelos de Reinas (Reina vs Reina o Reina vs Jugador):
        // Las reinas nunca pueden cruzar o salir del círculo que forman las Kobold Girl espectadoras.
        if (this.isAnyQueen() && (this.isInQueenDuel() || this.isInPlayerDuel())) {
            Vec3 arenaCenter = this.getDuelArenaCenter();
            if (arenaCenter != null) {
                double barrierRadius = this.getDuelArenaBarrierRadius();

                // Perímetro visual mágico y runas en alfabeto galáctico formando el círculo del duelo
                if (this.tickCount % 2 == 0) {
                    boolean isPrimaryDuelEntity = true;
                    if (this.isInQueenDuel() && this.getTarget() instanceof AbstractTribalEntity rivalQueen) {
                        if (this.getId() > rivalQueen.getId()) {
                            isPrimaryDuelEntity = false;
                        }
                    }
                    if (isPrimaryDuelEntity) {
                        this.spawnDuelPerimeterEffects(level, arenaCenter, barrierRadius);
                    }
                }

                double dx = this.getX() - arenaCenter.x;
                double dz = this.getZ() - arenaCenter.z;
                double horizDist = Math.sqrt(dx * dx + dz * dz);

                if (horizDist > barrierRadius && horizDist > 1.0E-4D) {
                    double nx = dx / horizDist;
                    double nz = dz / horizDist;
                    double clampedX = arenaCenter.x + nx * barrierRadius;
                    double clampedZ = arenaCenter.z + nz * barrierRadius;

                    this.setPos(clampedX, this.getY(), clampedZ);

                    // Anular componente de velocidad horizontal que intente salir del círculo
                    Vec3 vel = this.getDeltaMovement();
                    double outwardSpeed = vel.x * nx + vel.z * nz;
                    if (outwardSpeed > 0.0D) {
                        this.setDeltaMovement(vel.x - outwardSpeed * nx, vel.y, vel.z - outwardSpeed * nz);
                    }

                    // Efectos mágicos de confinamiento al chocar contra la barrera del círculo
                    if (this.tickCount % 4 == 0) {
                        level.sendParticles(ParticleTypes.ENCHANTED_HIT, this.getX(), this.getY() + 0.8D, this.getZ(), 6, 0.2D, 0.3D, 0.2D, 0.05D);
                        level.sendParticles(ParticleTypes.WITCH, this.getX(), this.getY() + 0.5D, this.getZ(), 4, 0.15D, 0.2D, 0.15D, 0.02D);
                    }
                    if (this.tickCount % 12 == 0) {
                        level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.NEUTRAL, 0.45F, 1.6F);
                    }
                }

                // Confinamiento del Jugador retador en el duelo contra la Reina
                if (this.isInPlayerDuel()) {
                    Player duelPlayer = this.getPlayerDuelTarget();
                    if (duelPlayer != null && duelPlayer.isAlive() && duelPlayer.level() == this.level()) {
                        double pdx = duelPlayer.getX() - arenaCenter.x;
                        double pdz = duelPlayer.getZ() - arenaCenter.z;
                        double pDist = Math.sqrt(pdx * pdx + pdz * pdz);

                        if (pDist > barrierRadius && pDist > 1.0E-4D) {
                            double pnx = pdx / pDist;
                            double pnz = pdz / pDist;
                            double pClampedX = arenaCenter.x + pnx * barrierRadius;
                            double pClampedZ = arenaCenter.z + pnz * barrierRadius;

                            duelPlayer.teleportTo(pClampedX, duelPlayer.getY(), pClampedZ);

                            Vec3 pVel = duelPlayer.getDeltaMovement();
                            double pOutward = pVel.x * pnx + pVel.z * pnz;
                            if (pOutward > 0.0D) {
                                duelPlayer.setDeltaMovement(pVel.x - pOutward * pnx, pVel.y, pVel.z - pOutward * pnz);
                                duelPlayer.hurtMarked = true;
                            }

                            if (this.tickCount % 4 == 0) {
                                level.sendParticles(ParticleTypes.ENCHANTED_HIT, duelPlayer.getX(), duelPlayer.getY() + 1.0D, duelPlayer.getZ(), 8, 0.25D, 0.4D, 0.25D, 0.05D);
                                level.sendParticles(ParticleTypes.WITCH, duelPlayer.getX(), duelPlayer.getY() + 0.5D, duelPlayer.getZ(), 4, 0.2D, 0.2D, 0.2D, 0.02D);
                            }
                            if (this.tickCount % 12 == 0) {
                                level.playSound(null, duelPlayer.getX(), duelPlayer.getY(), duelPlayer.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.5F, 1.5F);
                                duelPlayer.sendSystemMessage(Component.literal("¡No puedes salir del círculo del duelo!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
                            }
                        }
                    }
                }
            }
        }

        // Anclar posición en la cama si está durmiendo para evitar cualquier desplazamiento físico (pasan cosas raras si esto no está)
        if (this.isSleeping()) {
            this.setDeltaMovement(Vec3.ZERO);
            if (this.getSleepingPos().isPresent()) {
                BlockPos bedPos = this.getSleepingPos().get();
                double targetX = bedPos.getX() + 0.5D;
                double targetY = bedPos.getY() + 0.6875D;
                double targetZ = bedPos.getZ() + 0.5D;
                if (this.distanceToSqr(targetX, targetY, targetZ) > 0.001D) {
                    this.setPos(targetX, targetY, targetZ);
                }
            }
        }

        // Gestión dinámica de equipamiento según necesidades actuales
        if (this.isThreatened()) {
            if (!isWeapon(this.getMainHandItem())) {
                this.equipBestWeapon();
            }
            if (this.getProfession() != KoboldProfession.VIGILANT && !isRangedWeapon(this.getMainHandItem()) && this.getOffhandItem().isEmpty() && this.hasShieldInInventory()) {
                this.equipShieldFromInventory();
            }
        } else if (this.isHunting()) {
            if (!isWeapon(this.getMainHandItem())) {
                this.equipBestWeapon();
            }
            if (!this.getOffhandItem().isEmpty()) {
                this.unequipShield(); // Sin escudo para cazar animales
            }
        } else if (this.isHarvesting) {
            if (this.hasHoeInInventory()) {
                if (!isHoe(this.getMainHandItem())) {
                    this.equipHoeForHarvesting();
                }
            } else if (!this.getMainHandItem().isEmpty()) {
                this.unequipWeapon();
            }
            if (!this.getOffhandItem().isEmpty()) {
                this.unequipShield();
            }
        } else if (this.isWorkScheduleTime()) {
            // En horario de trabajo: mantener siempre equipadas sus armas y herramientas de trabajo
            if (this.tickCount % 20 == 0 || this.getMainHandItem().isEmpty()) {
                this.autoEquipForWorkOrDefense();
            }
        } else {
            // Fuera de horario de trabajo (durmiendo en cama o descansando): manos libres
            if (this.isSleeping() || this.isSleepTime()) {
                if (!this.getMainHandItem().isEmpty() || !this.getOffhandItem().isEmpty()) {
                    this.unequipHands();
                }
            }
        }

        this.tickQueenDeathAndPowerStruggle(level);

        this.tickHunger(level);
        this.tickBedBehavior(level);

        // Si está espectando un duelo activo, mantenerla libre de fuego
        if (this.isSpectatingDuel()) {
            this.clearFire();
        }

        // Hornear pan, alquimia y auto-equipar armaduras periódicamente cada 1 segundo
        if (this.tickCount % 20 == 0) {
            this.tryCraftBread();
            this.tryCraftAlchemySupplies();
            this.autoEquipArmor();
        }
    }

    private void spawnDuelPerimeterEffects(ServerLevel level, Vec3 center, double radius) {
        double rotation = (this.tickCount * 0.03D) % (2.0D * Math.PI);
        int points = 36;
        for (int i = 0; i < points; i++) {
            double angle = rotation + (2.0D * Math.PI * i) / points;
            double px = center.x + Math.cos(angle) * radius;
            double pz = center.z + Math.sin(angle) * radius;
            double py = findPerimeterGroundY(level, px, center.y, pz);

            // Letras en alfabeto galáctico flotando suavemente hacia arriba formando el perímetro
            level.sendParticles(ParticleTypes.ENCHANT, px, py + 0.25D, pz, 1, 0.0D, 0.12D, 0.0D, 0.45D);

            // Partículas mágicas: alternancia de chispas místicas moradas y estrellas encantadas celestes
            if (i % 2 == 0) {
                level.sendParticles(ParticleTypes.WITCH, px, py + 0.1D, pz, 1, 0.02D, 0.04D, 0.02D, 0.01D);
            } else {
                level.sendParticles(ParticleTypes.ENCHANTED_HIT, px, py + 0.35D, pz, 1, 0.02D, 0.05D, 0.02D, 0.02D);
            }

            // Destellos de vórtice dimensional en cuartos de cuadrante
            if (i % 9 == 0) {
                level.sendParticles(ParticleTypes.PORTAL, px, py + 0.2D, pz, 1, 0.0D, 0.05D, 0.0D, 0.02D);
            }
        }

        // Sonido ambiental de barrera mágica cada ~2 segundos (40 ticks)
        if (this.tickCount % 40 == 0) {
            level.playSound(null, center.x, center.y, center.z, SoundEvents.BEACON_AMBIENT, SoundSource.AMBIENT, 0.35F, 1.8F);
        }
    }

    public static double findPerimeterGroundY(Level level, double x, double approxY, double z) {
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos(x, approxY + 2.0D, z);
        for (int dy = 2; dy >= -4; dy--) {
            mpos.setY((int) Math.floor(approxY + dy));
            if (level.getBlockState(mpos).blocksMotion() && !level.getBlockState(mpos.above()).blocksMotion()) {
                return mpos.getY() + 1.0D;
            }
        }
        return approxY;
    }

    // Asignación de camas a cada miembro de la tribu

    public static BlockPos getBedHeadPos(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof BedBlock) {
            if (state.getValue(BedBlock.PART) == BedPart.FOOT) {
                Direction facing = state.getValue(BedBlock.FACING);
                return pos.relative(facing);
            }
            return pos;
        }
        return null;
    }

    public BlockPos getAssignedBedPos() {
        return this.assignedBedPos;
    }

    public boolean hasAssignedBed() {
        return this.assignedBedPos != null;
    }

    public boolean isAssignedBedValid() {
        if (this.assignedBedPos == null) return false;
        if (!this.level().isLoaded(this.assignedBedPos)) {
            return true; // Preservar la cama mientras el chunk esté descargado
        }
        BlockState state = this.level().getBlockState(this.assignedBedPos);
        return state.getBlock() instanceof BedBlock;
    }

    /** Atardecer / Sunset (Ticks 12000 a 13800, 6:00 PM a 7:48 PM): Duración del atardecer en Minecraft.
     *  Periodo exclusivo de descanso para Guardias y Vigilantes antes de iniciar su patrulla nocturna. */
    public boolean isDuskSunsetTime() {
        int t = this.getTimeOfDay();
        return t >= 12000 && t < 13800;
    }

    public boolean isSleepTime() {
        // Guardias y Vigilantes: Solo duermen durante lo que dura el atardecer (sunset: ticks 12000 a 13800).
        // Durante toda la noche y todo el día se mantienen despiertas patrullando los caminos de tierra y sus alrededores.
        if (this.isDefender()) {
            return this.isDuskSunsetTime();
        }
        // Reina de la tribu: Se duerme tarde a las 3:00 AM (tick 21000) hasta las 6:00 AM (tick 24000 / 0)
        // para participar activamente en el patrullaje y defensa nocturna durante gran parte de la noche.
        if (this.isAnyQueen()) {
            int t = this.getTimeOfDay();
            return (t >= 21000 && t < 24000) || (this.level().isThundering() && (t >= 21000 || t < 500));
        }
        // Demás miembros de la tribu (Granjeras, Cazadoras, Súbditas comunes):
        // Duermen durante la noche habitual o tormentas
        return this.level().isThundering() || BedRule.CAN_SLEEP_WHEN_DARK.canSleep(this.level());
    }

    //Horarios de trabajo para las kobolds para que no se bugueen y quieran hacer todo al mismo tiempo

    public int getTimeOfDay() {
        long time = this.level().getDefaultClockTime();
        if (time == 0L) {
            time = this.level().getOverworldClockTime();
        }
        if (time == 0L && this.level().getLevelData() != null) {
            time = this.level().getLevelData().getGameTime();
        }
        long dayTime = time % 24000L;
        if (dayTime < 0) dayTime += 24000L;
        return (int) dayTime;
    }

    public boolean isNightTime() {
        int t = this.getTimeOfDay();
        return t >= 13000 && t <= 23000;
    }

    /** 7:00 AM a 1:00 PM (Ticks 1000 a 7000): Granjeras cosechan/cultivan y Cazadoras cazan */
    public boolean isMorningWorkTime() {
        int t = this.getTimeOfDay();
        return t >= 1000 && t < 7000;
    }

    /** 1:00 PM a 3:30 PM (Ticks 7000 a 9500): Granjeras hornean pan y Cazadoras cocinan toda la carne */
    public boolean isCookingAndCraftingTime() {
        int t = this.getTimeOfDay();
        return t >= 7000 && t < 9500;
    }

    /** 3:30 PM a 6:00 PM (Ticks 9500 a 12000): Almacenamiento, reabastecimiento y organización comunitaria de cofres */
    public boolean isStorageAndOrganizationTime() {
        int t = this.getTimeOfDay();
        return t >= 9500 && t < 12000;
    }

    /** Horario general de trabajo y servicio activo de la tribu:
     *  - Guardias y Vigilantes: Servicio continuo activo (día y noche), excepto durante el atardecer que descansan.
     *  - Reina: Activa todo el día y noche (excepto de 3:00 a 6:00 AM que duerme).
     *  - Granjeras, Cazadoras, Constructoras y Súbditas comunes: Horario laboral diurno (ticks 0 a 12000, 6:00 AM a 6:00 PM) mientras no duerman. */
    public boolean isWorkScheduleTime() {
        if (this.isSleeping()) return false;
        if (this.isDefender()) {
            return !this.isDuskSunsetTime();
        }
        if (this.isAnyQueen()) {
            return !this.isSleepTime();
        }
        int t = this.getTimeOfDay();
        return t >= 0 && t < 12000;
    }

    public static boolean isBedSideStandable(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof BedBlock) {
            return false;
        }
        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);
        if (belowState.getBlock() instanceof BedBlock) {
            return false;
        }
        if (!belowState.isFaceSturdy(level, below, Direction.UP) && !belowState.isSolid()) {
            return false;
        }
        if (state.blocksMotion() || level.getBlockState(pos.above()).blocksMotion()) {
            return false;
        }
        if (level.getFluidState(pos).is(FluidTags.LAVA) || level.getFluidState(pos).is(FluidTags.WATER)) {
            return false;
        }
        return true;
    }

    public BlockPos findBedStandPosition(BlockPos headPos) {
        BlockState state = this.level().getBlockState(headPos);
        if (!(state.getBlock() instanceof BedBlock)) {
            return null;
        }
        Direction facing = state.getValue(BedBlock.FACING);
        BlockPos footPos = headPos.relative(facing.getOpposite());

        Direction clockWise = facing.getClockWise();
        Direction counterClockWise = facing.getCounterClockWise();
        Direction opposite = facing.getOpposite();

        BlockPos[] candidates = new BlockPos[]{
                headPos.relative(clockWise),
                headPos.relative(counterClockWise),
                footPos.relative(clockWise),
                footPos.relative(counterClockWise),
                footPos.relative(opposite),
                headPos.relative(facing)
        };

        BlockPos best = null;
        double bestDistSqr = Double.MAX_VALUE;

        for (BlockPos candidate : candidates) {
            if (isBedSideStandable(this.level(), candidate)) {
                double d = this.distanceToSqr(Vec3.atBottomCenterOf(candidate));
                if (d < bestDistSqr) {
                    bestDistSqr = d;
                    best = candidate;
                }
            }
        }
        return best;
    }

    public void setAssignedBed(BlockPos bedHeadPos) {
        if (this.assignedBedPos != null && !this.assignedBedPos.equals(bedHeadPos)) {
            CLAIMED_BEDS.remove(this.assignedBedPos);
        }
        this.assignedBedPos = bedHeadPos;
        if (bedHeadPos != null) {
            CLAIMED_BEDS.put(bedHeadPos, this.getUUID());
            this.setHomeTo(bedHeadPos, 100);
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        bedHeadPos.getX() + 0.5D, bedHeadPos.getY() + 0.6D, bedHeadPos.getZ() + 0.5D,
                        5, 0.3D, 0.3D, 0.3D, 0.05D);
            }
            // Asignar automáticamente un soporte para armaduras cercano a la cama asignada si no tiene uno para que no duerman tiesas con armaduras puestas
            if (this.assignedArmorStandUUID == null) {
                ArmorStand stand = this.findNearestUnclaimedArmorStand(8.0D);
                if (stand != null) {
                    this.setAssignedArmorStand(stand);
                }
            }
        }
    }

    public void clearAssignedBed() {
        if (this.assignedBedPos != null) {
            CLAIMED_BEDS.remove(this.assignedBedPos);
            this.assignedBedPos = null;
        }
        this.clearHome();
        if (this.isSleeping()) {
            this.stopSleeping();
        }
        if (!this.doesAssignedArmorStandHaveArmor()) {
            this.clearAssignedArmorStand();
        }
    }

    //Aqui se asignan los soportes de armadura

    public UUID getAssignedArmorStandUUID() {
        return this.assignedArmorStandUUID;
    }

    public boolean hasAssignedArmorStand() {
        return this.assignedArmorStandUUID != null;
    }

    public ArmorStand getAssignedArmorStandEntity() {
        if (this.assignedArmorStandUUID == null || !(this.level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        Entity entity = serverLevel.getEntity(this.assignedArmorStandUUID);
        if (entity instanceof ArmorStand stand && stand.isAlive()) {
            return stand;
        }
        if (entity != null) {
            this.clearAssignedArmorStand();
        }
        return null;
    }

    public void setAssignedArmorStand(ArmorStand stand) {
        if (this.assignedArmorStandUUID != null && (stand == null || !this.assignedArmorStandUUID.equals(stand.getUUID()))) {
            CLAIMED_ARMOR_STANDS.remove(this.assignedArmorStandUUID);
        }
        if (stand != null) {
            this.assignedArmorStandUUID = stand.getUUID();
            CLAIMED_ARMOR_STANDS.put(stand.getUUID(), this.getUUID());
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        stand.getX(), stand.getY() + 1.2D, stand.getZ(),
                        5, 0.2D, 0.2D, 0.2D, 0.05D);
            }
        } else {
            this.assignedArmorStandUUID = null;
        }
    }

    public void clearAssignedArmorStand() {
        if (this.assignedArmorStandUUID != null) {
            CLAIMED_ARMOR_STANDS.remove(this.assignedArmorStandUUID);
            this.assignedArmorStandUUID = null;
        }
    }

    public boolean isArmorStandClaimedByOther(ArmorStand stand) {
        if (stand == null || !stand.isAlive()) return true;
        UUID claimedBy = CLAIMED_ARMOR_STANDS.get(stand.getUUID());
        if (claimedBy != null && !claimedBy.equals(this.getUUID())) {
            return true;
        }
        List<AbstractTribalEntity> nearbyKobolds = this.level().getEntitiesOfClass(
                AbstractTribalEntity.class,
                stand.getBoundingBox().inflate(64.0D),
                k -> k != this && k.isAlive()
        );
        for (AbstractTribalEntity k : nearbyKobolds) {
            if (stand.getUUID().equals(k.assignedArmorStandUUID)) {
                CLAIMED_ARMOR_STANDS.put(stand.getUUID(), k.getUUID());
                return true;
            }
        }
        return false;
    }

    public ArmorStand findNearestUnclaimedArmorStand(double range) {
        if (!(this.level() instanceof ServerLevel serverLevel)) return null;

        // Si tiene cama asignada, buscar preferentemente alrededor de la cama asignada (radio de 8 bloques)
        if (this.hasAssignedBed()) {
            AABB bedBox = new AABB(this.assignedBedPos).inflate(8.0D, 4.0D, 8.0D);
            List<ArmorStand> nearBed = serverLevel.getEntitiesOfClass(ArmorStand.class, bedBox,
                    stand -> stand.isAlive() && !isArmorStandClaimedByOther(stand));
            if (!nearBed.isEmpty()) {
                nearBed.sort(Comparator.comparingDouble(s -> s.distanceToSqr(Vec3.atCenterOf(this.assignedBedPos))));
                return nearBed.get(0);
            }
        }

        // Búsqueda alternativa en el área alrededor de la Kobold Girl
        AABB box = this.getBoundingBox().inflate(range, 6.0D, range);
        List<ArmorStand> stands = serverLevel.getEntitiesOfClass(ArmorStand.class, box,
                stand -> stand.isAlive() && !isArmorStandClaimedByOther(stand));
        if (stands.isEmpty()) return null;

        stands.sort(Comparator.comparingDouble(this::distanceToSqr));
        return stands.get(0);
    }

    public boolean hasAnyArmorEquipped() {
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            if (!this.getItemBySlot(slot).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public boolean doesAssignedArmorStandHaveArmor() {
        ArmorStand stand = this.getAssignedArmorStandEntity();
        if (stand != null) {
            for (EquipmentSlot slot : ARMOR_SLOTS) {
                if (!stand.getItemBySlot(slot).isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isNeedsArmorRetrieval() {
        return this.needsArmorRetrieval;
    }

    public void setNeedsArmorRetrieval(boolean needs) {
        this.needsArmorRetrieval = needs;
    }

    public boolean isBedClaimedByOther(BlockPos bedHeadPos) {
        if (bedHeadPos == null) return true;
        UUID claimedBy = CLAIMED_BEDS.get(bedHeadPos);
        if (claimedBy != null && !claimedBy.equals(this.getUUID())) {
            return true;
        }

        // Revisar también si otra Kobold Girl viva en el área local tiene asignada esta misma cama
        List<AbstractTribalEntity> nearbyKobolds = this.level().getEntitiesOfClass(
                AbstractTribalEntity.class,
                new AABB(bedHeadPos).inflate(32.0D, 8.0D, 32.0D),
                k -> k != this && k.isAlive()
        );
        for (AbstractTribalEntity k : nearbyKobolds) {
            if (bedHeadPos.equals(k.getAssignedBedPos())) {
                CLAIMED_BEDS.put(bedHeadPos, k.getUUID());
                return true;
            }
        }

        return false;
    }

    public BlockPos findNearestUnclaimedBed(int range) {
        BlockPos currentPos = this.blockPosition();
        BlockPos bestBed = null;
        double bestDistSqr = Double.MAX_VALUE;

        for (BlockPos testPos : BlockPos.betweenClosed(
                currentPos.offset(-range, -4, -range),
                currentPos.offset(range, 4, range))) {
            BlockState state = this.level().getBlockState(testPos);
            if (state.getBlock() instanceof BedBlock) {
                BlockPos headPos = getBedHeadPos(this.level(), testPos);
                if (headPos != null && !isBedClaimedByOther(headPos)) {
                    BlockState headState = this.level().getBlockState(headPos);
                    // Solo si no está ocupada físicamente por otra entidad durmiendo
                    if (headState.hasProperty(BedBlock.OCCUPIED) && !headState.getValue(BedBlock.OCCUPIED)) {
                        double distSqr = this.distanceToSqr(Vec3.atCenterOf(headPos));
                        if (distSqr < bestDistSqr) {
                            bestDistSqr = distSqr;
                            bestBed = headPos.immutable();
                        }
                    }
                }
            }
        }
        return bestBed;
    }

    private void tickBedBehavior(ServerLevel level) {
        boolean isSleepTimeNow = this.isSleepTime();
        if (this.wasSleepingTime && !isSleepTimeNow) {
            // Amanecer: si está cerca de su cama asignada, activar la caminata matutina por el camino
            if (this.hasAssignedBed() && this.distanceToSqr(Vec3.atCenterOf(this.assignedBedPos)) < 64.0D) {
                this.needsMorningWalk = true;
            }
        }
        this.wasSleepingTime = isSleepTimeNow;

        if (this.hasAssignedBed()) {
            double distSqr = this.distanceToSqr(Vec3.atCenterOf(this.assignedBedPos));

            // Si no tiene soporte de armadura asignado o fue destruido, buscar uno cercano a la cama cada 40 ticks
            if (this.assignedArmorStandUUID == null && this.tickCount % 40 == 0) {
                ArmorStand stand = this.findNearestUnclaimedArmorStand(8.0D);
                if (stand != null) {
                    this.setAssignedArmorStand(stand);
                }
            }

            // Si es alejada a más de 300 bloques, pierde la asignación de su cama
            if (distSqr > 300.0D * 300.0D) {
                this.clearAssignedBed();
                return;
            }

            // Cada 40 ticks verificar si la cama asignada sigue existiendo en el mundo (no fue destruida)
            if (this.tickCount % 40 == 0) {
                if (!this.isAssignedBedValid()) {
                    this.clearAssignedBed();
                    return;
                }
            }

            // Restricción de 100 bloques: si está fuera del radio de 100 bloques y no está en combate, volver a su zona
            if (distSqr > 100.0D * 100.0D && this.getTarget() == null && !this.isSleeping()) {
                if (this.getNavigation().isDone() || this.tickCount % 40 == 0) {
                    this.getNavigation().moveTo(
                            this.assignedBedPos.getX() + 0.5D,
                            this.assignedBedPos.getY(),
                            this.assignedBedPos.getZ() + 0.5D,
                            1.25D
                    );
                }
            }
        } else {
            // Si no tiene cama asignada, buscar periódicamente una cama no reclamada cercana
            if (this.tickCount % 40 == 0) {
                BlockPos unclaimed = this.findNearestUnclaimedBed(this.isSleepTime() ? 32 : 16);
                if (unclaimed != null) {
                    this.setAssignedBed(unclaimed);
                }
            }
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        this.clearAssignedBed();
        this.clearAssignedArmorStand();

        if (this.level() instanceof ServerLevel serverLevel) {
            if (this.isQueen() && this.hasTribe()) {
                this.handleQueenDeath(serverLevel, damageSource);
            } else if (this.isInPowerStruggle()) {
                this.setInPowerStruggle(false);
                this.checkPowerStruggleWinner(serverLevel);
            }
        }
    }

    private void handleQueenDeath(ServerLevel level, DamageSource damageSource) {
        // Caso de Ejecución formal tras juicio:
        if (this.isMarkedForExecution()) {
            Player killerPlayer = null;
            if (damageSource.getEntity() instanceof Player p) {
                killerPlayer = p;
            } else if (this.getLastHurtByMob() instanceof Player p) {
                killerPlayer = p;
            } else {
                killerPlayer = level.getNearestPlayer(this, 32.0D);
            }
            if (killerPlayer != null) {
                String primaryTribe = getPlayerPrimaryTribe(killerPlayer);
                String oldTribe = this.getTribeName();
                String queenName = this.getCustomGivenName().isEmpty() ? this.getQueenTitle() : this.getCustomGivenName();
                // Liberar el alma de la reina para ser recolectada en el báculo
                QueenSoulTracker.spawnSoul(killerPlayer, queenName, oldTribe, this.position().add(0, 1.0D, 0));
                // Anexar la tribu entera a la tribu del jugador
                annexTribeToPlayer(level, killerPlayer, oldTribe, primaryTribe, this);
                killerPlayer.sendSystemMessage(Component.literal("⚔ Has ejecutado a la Reina de " + oldTribe + ". Toda su tribu ha sido anexada a tu tribu " + primaryTribe + ".")
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            }
            return;
        }

        AbstractTribalEntity winningQueen = null;
        if (this.getTarget() instanceof AbstractTribalEntity rival && rival.isQueen() && rival.isAlive() && !rival.getTribeName().equals(this.getTribeName())) {
            winningQueen = rival;
        } else if (damageSource.getEntity() instanceof AbstractTribalEntity attacker && attacker.isQueen() && attacker.isAlive() && !attacker.getTribeName().equals(this.getTribeName())) {
            winningQueen = attacker;
        } else {
            List<AbstractTribalEntity> nearbyQueens = level.getEntitiesOfClass(
                    AbstractTribalEntity.class,
                    this.getBoundingBox().inflate(32.0D),
                    q -> q != this && q.isQueen() && q.isAlive() && q.hasTribe() && !q.getTribeName().equals(this.getTribeName())
            );
            if (!nearbyQueens.isEmpty()) {
                winningQueen = nearbyQueens.get(0);
            }
        }

        if (winningQueen != null) {
            this.resolveQueenDuelDefeat(level, winningQueen);
        } else {
            // Caso 1: La tribu pertenece al jugador. El jugador es el gobernante, por lo que NO hay guerra civil.
            if (this.isOwned()) {
                UUID ownerUUID = this.getOwnerUUID();
                Player ownerPlayer = ownerUUID != null ? level.getPlayerByUUID(ownerUUID) : null;
                String queenName = this.getCustomGivenName().isEmpty() ? this.getQueenTitle() : this.getCustomGivenName();
                String tribeName = this.getTribeName();

                // Generar partículas de alma que se acercan lentamente al jugador
                if (ownerPlayer != null && ownerPlayer.isAlive()) {
                    QueenSoulTracker.spawnSoul(ownerPlayer, queenName, tribeName, this.position().add(0, 1.0D, 0));
                }

                // Si un agresor externo la mató, las súbditas neutralizan al agresor pero NUNCA inician guerra civil
                LivingEntity killer = null;
                if (damageSource.getEntity() instanceof LivingEntity le && le.isAlive() && !le.isSpectator()) {
                    killer = le;
                } else if (this.getLastHurtByMob() != null && this.getLastHurtByMob().isAlive() && !this.getLastHurtByMob().isSpectator()) {
                    killer = this.getLastHurtByMob();
                } else if (this.getTarget() != null && this.getTarget().isAlive() && !this.getTarget().isSpectator()) {
                    killer = this.getTarget();
                }

                if (killer instanceof AbstractTribalEntity kg && this.getTribeName().equals(kg.getTribeName())) {
                    killer = null;
                }
                if (killer instanceof Player p && this.isOwnedBy(p)) {
                    killer = null;
                }

                if (killer != null && killer.isAlive()) {
                    List<AbstractTribalEntity> loyalCandidates = level.getEntitiesOfClass(
                            AbstractTribalEntity.class,
                            this.getBoundingBox().inflate(48.0D),
                            k -> k != this && k.isAlive() && !k.isQueen() && Objects.equals(k.getOwnerUUID(), ownerUUID)
                    );
                    for (AbstractTribalEntity candidate : loyalCandidates) {
                        if (candidate.isSleeping()) {
                            candidate.stopSleeping();
                        }
                        candidate.startAvengeQueen(killer, tribeName, this.position());
                    }
                }
                return; // IMPORTANTE: Las tribus del jugador nunca inician guerra civil.
            }

            // Caso 2: Tribus salvajes (no pertenecientes al jugador)
            LivingEntity killer = null;
            if (damageSource.getEntity() instanceof LivingEntity le && le.isAlive() && !le.isSpectator()) {
                killer = le;
            } else if (this.getLastHurtByMob() != null && this.getLastHurtByMob().isAlive() && !this.getLastHurtByMob().isSpectator()) {
                killer = this.getLastHurtByMob();
            } else if (this.getTarget() != null && this.getTarget().isAlive() && !this.getTarget().isSpectator()) {
                killer = this.getTarget();
            }

            // Si el asesino pertenece a la misma tribu, no tomarlo como enemigo
            if (killer instanceof AbstractTribalEntity kg && this.getTribeName().equals(kg.getTribeName())) {
                killer = null;
            }

            String tribeName = this.getTribeName();
            Vec3 center = this.position();

            List<AbstractTribalEntity> candidates = level.getEntitiesOfClass(
                    AbstractTribalEntity.class,
                    new AABB(center.x - 48.0D, center.y - 24.0D, center.z - 48.0D, center.x + 48.0D, center.y + 24.0D, center.z + 48.0D),
                    k -> k != this && k.isAlive() && !k.isQueen() && (tribeName.equals(k.getTribeName()) || k.isRebel())
            );

            if (candidates.isEmpty()) {
                return;
            }

            if (killer != null && killer.isAlive()) {
                // Primero: Neutralizar a aquello que mató a su reina antes de cualquier guerra civil
                for (AbstractTribalEntity candidate : candidates) {
                    if (candidate.isSleeping()) {
                        candidate.stopSleeping();
                    }
                    candidate.startAvengeQueen(killer, tribeName, center);
                }
            } else {
                // Sin asesino vivo: pausa de unos segundos (5 seg / 100 ticks) antes de que inicie la guerra civil
                for (AbstractTribalEntity candidate : candidates) {
                    if (candidate.isSleeping()) {
                        candidate.stopSleeping();
                    }
                    candidate.startPendingPowerStruggle(tribeName, center, 100);
                }
            }
        }
    }

    public void startAvengeQueen(LivingEntity killer, String tribeName, Vec3 center) {
        this.avengeQueenKillerUUID = killer.getUUID();
        this.pendingPowerStruggleTribe = tribeName;
        this.pendingPowerStruggleCenter = center;
        this.powerStruggleDelayTicks = -1;
        this.setInPowerStruggle(false);
        this.setTarget(killer);
        this.equipForCombat(true);

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER, this.getX(), this.getY() + 1.2D, this.getZ(), 10, 0.3D, 0.3D, 0.3D, 0.05D);
            this.playSound(SoundEvents.VILLAGER_NO, 1.0F, 0.8F);
        }
    }

    public void startPendingPowerStruggle(String tribeName, Vec3 center, int delayTicks) {
        this.avengeQueenKillerUUID = null;
        this.pendingPowerStruggleTribe = tribeName;
        this.pendingPowerStruggleCenter = center;
        this.powerStruggleDelayTicks = delayTicks;
        this.setInPowerStruggle(false);
        this.setTarget(null);
        this.equipForCombat(true);
    }

    public void tickQueenDeathAndPowerStruggle(ServerLevel level) {
        if (this.isInPowerStruggle() || !this.isAlive() || this.isAnyQueen()) {
            return;
        }

        // 1. Fase de venganza: neutralizar a lo que mató a su reina
        if (this.avengeQueenKillerUUID != null) {
            Entity killerEntity = level.getEntity(this.avengeQueenKillerUUID);
            boolean killerNeutralized = (killerEntity == null
                    || !killerEntity.isAlive()
                    || killerEntity.isRemoved()
                    || this.distanceToSqr(killerEntity) > 64.0D * 64.0D);

            if (!killerNeutralized && killerEntity instanceof LivingEntity livingKiller) {
                if (this.getTarget() != livingKiller) {
                    this.setTarget(livingKiller);
                    this.equipForCombat(true);
                }
            } else {
                // ¡El asesino ha sido neutralizado o eliminado!
                this.avengeQueenKillerUUID = null;
                this.setTarget(null);

                // Si la tribu pertenece al jugador, el gobernante es el jugador -> NUNCA guerra civil
                if (this.isOwned()) {
                    this.powerStruggleDelayTicks = 0;
                    this.pendingPowerStruggleTribe = null;
                    this.pendingPowerStruggleCenter = null;
                    level.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.2D, this.getZ(), 8, 0.3D, 0.3D, 0.3D, 0.05D);
                    level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 0.8F, 1.2F);
                    return;
                }

                // Pausa de 5 segundos (100 ticks) antes de la guerra civil para tribus salvajes
                this.powerStruggleDelayTicks = 100;
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.2D, this.getZ(), 8, 0.3D, 0.3D, 0.3D, 0.05D);
                level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 0.8F, 1.2F);
            }
            return;
        }

        // 2. Conteo regresivo tras neutralizar al asesino antes de que empiece la guerra civil
        if (this.powerStruggleDelayTicks > 0) {
            if (this.isOwned()) {
                this.powerStruggleDelayTicks = 0;
                this.pendingPowerStruggleTribe = null;
                this.pendingPowerStruggleCenter = null;
                return;
            }
            this.powerStruggleDelayTicks--;
            if (this.powerStruggleDelayTicks == 0) {
                String tribe = this.pendingPowerStruggleTribe != null ? this.pendingPowerStruggleTribe : this.getTribeName();
                Vec3 center = this.pendingPowerStruggleCenter != null ? this.pendingPowerStruggleCenter : this.position();
                this.pendingPowerStruggleTribe = null;
                this.pendingPowerStruggleCenter = null;
                this.triggerTribalPowerStruggle(level, tribe, center);
            }
        }
    }

    public void triggerTribalPowerStruggle(ServerLevel level, String tribeName, Vec3 center) {
        if (tribeName == null || tribeName.isEmpty()) return;
        if (this.isOwned()) return; // Garantía de paz interna para tribus del jugador

        List<AbstractTribalEntity> candidates = level.getEntitiesOfClass(
                AbstractTribalEntity.class,
                new AABB(center.x - 48.0D, center.y - 24.0D, center.z - 48.0D, center.x + 48.0D, center.y + 24.0D, center.z + 48.0D),
                k -> k != this && k.isAlive() && !k.isQueen() && (tribeName.equals(k.getTribeName()) || k.isRebel())
        );

        if (candidates.isEmpty()) {
            return;
        }

        if (candidates.size() == 1) {
            coronateSurvivorQueen(level, candidates.get(0), tribeName);
            return;
        }

        for (AbstractTribalEntity candidate : candidates) {
            if (candidate.isSleeping()) {
                candidate.stopSleeping();
            }
            candidate.avengeQueenKillerUUID = null;
            candidate.powerStruggleDelayTicks = 0;
            candidate.pendingPowerStruggleTribe = null;
            candidate.pendingPowerStruggleCenter = null;
            candidate.setInPowerStruggle(true);
            candidate.setRebel(false);
            candidate.setTribeName(tribeName);
            candidate.setTarget(null);
            candidate.equipForCombat(true);

            level.sendParticles(ParticleTypes.ANGRY_VILLAGER, candidate.getX(), candidate.getY() + 1.2D, candidate.getZ(), 12, 0.4D, 0.4D, 0.4D, 0.05D);
            level.playSound(null, candidate.getX(), candidate.getY(), candidate.getZ(), SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, 1.0F, 0.8F);
        }
    }

    private void checkPowerStruggleWinner(ServerLevel level) {
        String tribe = this.getTribeName();
        List<AbstractTribalEntity> remaining = level.getEntitiesOfClass(
                AbstractTribalEntity.class,
                this.getBoundingBox().inflate(64.0D),
                k -> k != this && k.isAlive() && k.isInPowerStruggle() && (tribe.isEmpty() || k.getTribeName().equals(tribe))
        );

        if (remaining.size() == 1) {
            coronateSurvivorQueen(level, remaining.get(0), tribe);
        }
    }

    public static void coronateSurvivorQueen(ServerLevel level, AbstractTribalEntity winner, String tribeName) {
        if (winner == null || !winner.isAlive()) return;

        winner.setInPowerStruggle(false);
        winner.setRebel(false);
        winner.setQueen(true);
        winner.setSurvivorQueen(true);
        winner.setProfession(KoboldProfession.NONE);
        if (tribeName != null && !tribeName.isEmpty()) {
            winner.setTribeName(tribeName);
        }
        winner.setTarget(null);
        winner.heal(winner.getMaxHealth());
        winner.unequipHands();

        // Efectos festivos de coronación: Tótem de la inmortalidad + aldeano feliz
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, winner.getX(), winner.getY() + 1.2D, winner.getZ(), 40, 0.5D, 0.5D, 0.5D, 0.2D);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, winner.getX(), winner.getY() + 1.5D, winner.getZ(), 30, 0.6D, 0.6D, 0.6D, 0.1D);

        // Sonidos de triunfo épico
        level.playSound(null, winner.getX(), winner.getY(), winner.getZ(), SoundEvents.TOTEM_USE, SoundSource.NEUTRAL, 1.0F, 1.0F);
        level.playSound(null, winner.getX(), winner.getY(), winner.getZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.NEUTRAL, 1.0F, 1.0F);
    }

    private void resolveQueenDuelDefeat(ServerLevel level, AbstractTribalEntity winningQueen) {
        if (winningQueen == null) return;

        String winningTribe = winningQueen.getTribeName();
        String defeatedTribe = this.getTribeName();

        // Celebración y curación total de la Reina Victoriosa
        winningQueen.setTarget(null);
        winningQueen.setDuelArenaCenter(null);
        this.duelArenaCenter = null;
        winningQueen.clearFire();
        winningQueen.setHealth(winningQueen.getMaxHealth());
        winningQueen.heal(winningQueen.getMaxHealth());
        level.sendParticles(ParticleTypes.HEART, winningQueen.getX(), winningQueen.getY() + 1.2D, winningQueen.getZ(), 16, 0.4D, 0.5D, 0.4D, 0.05D);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, winningQueen.getX(), winningQueen.getY() + 1.5D, winningQueen.getZ(), 30, 0.6D, 0.6D, 0.6D, 0.1D);
        level.playSound(null, winningQueen.getX(), winningQueen.getY(), winningQueen.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 1.0F, 1.0F);

        // Buscar súbditas de la tribu derrotada en un radio de 48 bloques
        List<AbstractTribalEntity> defeatedSubditas = level.getEntitiesOfClass(
                AbstractTribalEntity.class,
                this.getBoundingBox().inflate(48.0D),
                k -> !k.isQueen() && k.isAlive() && k.getTribeName().equals(defeatedTribe)
        );

        if (defeatedSubditas.isEmpty()) return;

        List<AbstractTribalEntity> shuffled = new ArrayList<>(defeatedSubditas);
        Collections.shuffle(shuffled, new java.util.Random(level.getRandom().nextLong()));

        int total = shuffled.size();
        int numRebels;
        if (total == 1) {
            numRebels = level.getRandom().nextFloat() < 0.3F ? 1 : 0;
        } else if (total <= 3) {
            numRebels = 1;
        } else {
            numRebels = Math.max(1, (int) Math.round(total * 0.25D));
        }

        for (int i = 0; i < total; i++) {
            AbstractTribalEntity subdita = shuffled.get(i);
            if (i < numRebels) {
                // Súbditas que muestran resistencia (Rebeldes) y se vuelven agresivas con la tribu ganadora
                subdita.setRebel(true);
                subdita.setTarget(winningQueen);
                subdita.equipForCombat(true);

                level.sendParticles(ParticleTypes.ANGRY_VILLAGER, subdita.getX(), subdita.getY() + 1.2D, subdita.getZ(), 12, 0.4D, 0.4D, 0.4D, 0.05D);
                level.playSound(null, subdita.getX(), subdita.getY(), subdita.getZ(), SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, 1.0F, 0.9F);
            } else {
                // Súbditas que se unen pacíficamente a la tribu de la reina ganadora
                subdita.setRebel(false);
                subdita.setTribeName(winningTribe);
                subdita.setTarget(null);
                subdita.unequipHands();

                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, subdita.getX(), subdita.getY() + 1.2D, subdita.getZ(), 15, 0.4D, 0.4D, 0.4D, 0.05D);
                level.playSound(null, subdita.getX(), subdita.getY(), subdita.getZ(), SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 0.9F, 1.1F);
            }
        }
    }

    /* =========================================================================
       SISTEMA DE DUELO DE REINAS Y ESPECTACIÓN DE SÚBDITAS
       ========================================================================= */

    public static class QueenDuel {
        public final AbstractTribalEntity queen1;
        public final LivingEntity queen2;

        public QueenDuel(AbstractTribalEntity queen1, LivingEntity queen2) {
            this.queen1 = queen1;
            this.queen2 = queen2;
        }

        public Vec3 getCenter() {
            Vec3 c1 = this.queen1.getDuelArenaCenter();
            if (c1 != null) return c1;
            if (this.queen2 instanceof AbstractTribalEntity q2) {
                Vec3 c2 = q2.getDuelArenaCenter();
                if (c2 != null) return c2;
            }
            return this.queen1.position().add(this.queen2.position()).scale(0.5D);
        }

        public boolean isActive() {
            if (!this.queen1.isAlive() || !this.queen2.isAlive()) return false;
            if (this.queen2 instanceof AbstractTribalEntity rivalQueen) {
                return this.queen1.isInQueenDuel() && rivalQueen.isInQueenDuel();
            } else if (this.queen2 instanceof Player player) {
                return this.queen1.isInPlayerDuelWith(player);
            }
            return false;
        }
    }

    public static QueenDuel getNearbyQueenDuel(Level level, Vec3 pos, double radius) {
        List<AbstractTribalEntity> nearbyQueens = level.getEntitiesOfClass(
                AbstractTribalEntity.class,
                new AABB(pos.x - radius, pos.y - radius, pos.z - radius, pos.x + radius, pos.y + radius, pos.z + radius),
                q -> q.isAnyQueen() && q.isAlive() && (q.isInQueenDuel() || q.isInPlayerDuel())
        );
        for (AbstractTribalEntity q1 : nearbyQueens) {
            if (q1.isInPlayerDuel() && q1.getPlayerDuelTargetUUID() != null) {
                Player player = level.getPlayerByUUID(q1.getPlayerDuelTargetUUID());
                if (player != null && player.isAlive()) {
                    return new QueenDuel(q1, player);
                }
            } else if (q1.getTarget() instanceof AbstractTribalEntity q2 && q2.isAnyQueen() && q2.isAlive() && q2.isInQueenDuel()) {
                return new QueenDuel(q1, q2);
            }
        }
        return null;
    }

    public static BlockPos findStandablePosNear(Level level, BlockPos basePos) {
        for (int dy = 0; dy <= 2; dy++) {
            for (int sign : new int[]{0, 1, -1}) {
                BlockPos checkPos = basePos.above(dy * sign);
                if (!level.getBlockState(checkPos).blocksMotion()
                        && !level.getBlockState(checkPos.above()).blocksMotion()
                        && level.getBlockState(checkPos.below()).blocksMotion()) {
                    return checkPos;
                }
            }
        }
        return basePos;
    }

    public static double getDuelSpectatorRadius(Level level, Vec3 center, @Nullable Entity entity1, @Nullable Entity entity2) {
        List<AbstractTribalEntity> spectators = level.getEntitiesOfClass(
                AbstractTribalEntity.class,
                new AABB(center.x - 24.0D, center.y - 12.0D, center.z - 24.0D, center.x + 24.0D, center.y + 12.0D, center.z + 24.0D),
                k -> !k.isQueen() && !k.isRebel() && k.isAlive()
                        && (entity1 == null || k.hasLineOfSight(entity1) || (entity2 != null && k.hasLineOfSight(entity2)))
        );
        int total = Math.max(1, spectators.size());
        return 6.0D + Math.min(2.0D, total * 0.15D);
    }

    public @Nullable Vec3 findPosAwayFrom(Vec3 dangerPos, double distance) {
        Vec3 myPos = this.position();
        Vec3 awayDir = myPos.subtract(dangerPos);
        if (awayDir.lengthSqr() < 1.0E-4D) {
            awayDir = new Vec3(this.getRandom().nextGaussian(), 0.0D, this.getRandom().nextGaussian());
        }
        awayDir = awayDir.normalize();

        for (int i = 0; i < 8; i++) {
            double angle = (this.getRandom().nextDouble() - 0.5D) * Math.PI * 0.5D;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            double rx = awayDir.x * cos - awayDir.z * sin;
            double rz = awayDir.x * sin + awayDir.z * cos;
            double dist = distance * (0.8D + this.getRandom().nextDouble() * 0.4D);

            double targetX = myPos.x + rx * dist;
            double targetZ = myPos.z + rz * dist;

            BlockPos ground = this.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BlockPos.containing(targetX, myPos.y, targetZ));
            if (Math.abs(ground.getY() - myPos.y) <= 3.0D) {
                if (!isNearDangerousDrop(this.level(), ground, 2) && !isNearHazard(this.level(), ground, 2)) {
                    return Vec3.atBottomCenterOf(ground);
                }
            }
        }
        return null;
    }

    public @Nullable Vec3 findPosAwayFromWithAnchor(Vec3 dangerPos, double distance, @Nullable Vec3 anchorPos, double maxDistFromAnchor) {
        if (anchorPos == null || maxDistFromAnchor <= 0) {
            return findPosAwayFrom(dangerPos, distance);
        }

        Vec3 myPos = this.position();
        double currentDistToDanger = myPos.distanceTo(dangerPos);
        double maxAnchorSq = maxDistFromAnchor * maxDistFromAnchor;

        Vec3 bestPos = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        // Evaluar 16 ángulos alrededor de la Kobold (360 grados) para encontrar la mejor posición que:
        // 1. Se aleje del peligro (jugador)
        // 2. NO se separe más de maxDistFromAnchor (15 bloques) de la Reina (ancla)
        // 3. Esté sobre suelo seguro libre de caídas y peligros
        for (int i = 0; i < 16; i++) {
            double angle = (i * (2.0D * Math.PI / 16.0D)) + ((this.getRandom().nextDouble() - 0.5D) * (Math.PI / 16.0D));
            double rx = Math.cos(angle);
            double rz = Math.sin(angle);
            double dist = distance * (0.8D + this.getRandom().nextDouble() * 0.4D);

            double targetX = myPos.x + rx * dist;
            double targetZ = myPos.z + rz * dist;

            // Verificar restricción del ancla (Reina)
            double dxAnchor = targetX - anchorPos.x;
            double dzAnchor = targetZ - anchorPos.z;
            if (dxAnchor * dxAnchor + dzAnchor * dzAnchor > maxAnchorSq) {
                continue;
            }

            BlockPos ground = this.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BlockPos.containing(targetX, myPos.y, targetZ));
            if (Math.abs(ground.getY() - myPos.y) <= 3.0D) {
                if (!isNearDangerousDrop(this.level(), ground, 2) && !isNearHazard(this.level(), ground, 2)) {
                    Vec3 candidate = Vec3.atBottomCenterOf(ground);
                    double candDistToDanger = candidate.distanceTo(dangerPos);

                    // Puntuación:
                    // Cuanto más lejos del peligro mejor (+10 por bloque)
                    // Penalizar severamente si el movimiento acerca a la Kobold al peligro
                    double score = candDistToDanger * 10.0D;
                    if (candDistToDanger <= currentDistToDanger) {
                        score -= 50.0D;
                    }
                    // Penalización suave si se acerca demasiado al borde límite del radio del ancla
                    double distToAnchor = Math.sqrt(dxAnchor * dxAnchor + dzAnchor * dzAnchor);
                    if (distToAnchor > maxDistFromAnchor - 2.0D) {
                        score -= 15.0D;
                    }

                    if (score > bestScore) {
                        bestScore = score;
                        bestPos = candidate;
                    }
                }
            }
        }

        if (bestPos != null) {
            return bestPos;
        }

        // Si todas las posiciones radiales fallaron o el jugador la acorraló contra el borde del radio de 15m:
        // Intentar dar un paso hacia la reina rodeando el peligro si es posible
        if (myPos.distanceTo(anchorPos) > 4.0D) {
            Vec3 dirToAnchor = anchorPos.subtract(myPos).normalize();
            double targetX = myPos.x + dirToAnchor.x * Math.min(distance, 4.0D);
            double targetZ = myPos.z + dirToAnchor.z * Math.min(distance, 4.0D);
            BlockPos ground = this.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BlockPos.containing(targetX, myPos.y, targetZ));
            if (Math.abs(ground.getY() - myPos.y) <= 3.0D) {
                if (!isNearDangerousDrop(this.level(), ground, 2) && !isNearHazard(this.level(), ground, 2)) {
                    return Vec3.atBottomCenterOf(ground);
                }
            }
        }

        return null;
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        super.remove(reason);
        if (reason.shouldDestroy()) {
            if (this.assignedBedPos != null) {
                CLAIMED_BEDS.remove(this.assignedBedPos);
            }
            if (this.assignedArmorStandUUID != null) {
                CLAIMED_ARMOR_STANDS.remove(this.assignedArmorStandUUID);
            }
        }
    }

    /* =========================================================================
       DETECCIÓN DE SEGURIDAD DEL TERRENO: PRECIPICIOS Y PELIGROS
       ========================================================================= */

    public static boolean isNearDangerousDrop(BlockGetter level, BlockPos pos, int radius) {
        BlockPos.MutableBlockPos testPos = new BlockPos.MutableBlockPos();
        int originX = pos.getX();
        int originY = pos.getY();
        int originZ = pos.getZ();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx == 0 && dz == 0) continue;

                int checkX = originX + dx;
                int checkZ = originZ + dz;

                // Si hay un bloque sólido a nivel de los pies (originY), es una pared/escalón hacia arriba, no un precipicio
                testPos.set(checkX, originY, checkZ);
                if (!level.getBlockState(testPos).getCollisionShape(level, testPos).isEmpty()) {
                    continue;
                }

                // Comprobar si los 3 niveles debajo (y-1, y-2, y-3) carecen de colisión sólida (caída de más de 2 bloques)
                testPos.set(checkX, originY - 1, checkZ);
                boolean empty1 = level.getBlockState(testPos).getCollisionShape(level, testPos).isEmpty();

                testPos.set(checkX, originY - 2, checkZ);
                boolean empty2 = level.getBlockState(testPos).getCollisionShape(level, testPos).isEmpty();

                testPos.set(checkX, originY - 3, checkZ);
                boolean empty3 = level.getBlockState(testPos).getCollisionShape(level, testPos).isEmpty();

                if (empty1 && empty2 && empty3) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isNearHazard(BlockGetter level, BlockPos pos, int radius) {
        BlockPos.MutableBlockPos testPos = new BlockPos.MutableBlockPos();
        int originX = pos.getX();
        int originY = pos.getY();
        int originZ = pos.getZ();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -1; dy <= 2; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    testPos.set(originX + dx, originY + dy, originZ + dz);
                    BlockState state = level.getBlockState(testPos);

                    // Líquidos peligrosos (Lava)
                    if (state.getFluidState().is(FluidTags.LAVA) || state.is(Blocks.LAVA)) {
                        return true;
                    }

                    // Fuego, hogueras
                    if (state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE)
                            || state.is(Blocks.CAMPFIRE) || state.is(Blocks.SOUL_CAMPFIRE)) {
                        return true;
                    }

                    // Bloques dañinos al tacto o paso (cactus, arbustos de bayas dulces, etc.)
                    if (state.is(Blocks.MAGMA_BLOCK)
                            || state.is(Blocks.CACTUS)
                            || state.is(Blocks.SWEET_BERRY_BUSH)
                            || state.is(Blocks.WITHER_ROSE)
                            || state.is(Blocks.POWDER_SNOW)
                            || state.is(Blocks.POINTED_DRIPSTONE)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isDirtPath(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos).is(Blocks.DIRT_PATH) || level.getBlockState(pos.below()).is(Blocks.DIRT_PATH);
    }

    public boolean isStepSafe(BlockPos nextPos) {
        if (isNearHazard(this.level(), nextPos, 1)) {
            return false;
        }
        if (isNearDangerousDrop(this.level(), nextPos, 1)) {
            return false;
        }
        if (this.level().getBlockState(nextPos).isAir()) {
            for (int i = 1; i <= 3; i++) {
                BlockPos below = nextPos.below(i);
                BlockState state = this.level().getBlockState(below);
                if (state.is(Blocks.LAVA) || (i == 3 && state.isAir())) {
                    return false;
                }
            }
        }
        return true;
    }

    public BlockPos findMorningWalkDestination() {
        if (this.assignedBedPos == null) {
            return null;
        }

        BlockPos currentPos = this.blockPosition();
        BlockPos bestPos = null;
        double bestDistToBedSqr = 0.0D;

        int searchRadius = 24;
        BlockPos.MutableBlockPos testPos = new BlockPos.MutableBlockPos();

        for (int dx = -searchRadius; dx <= searchRadius; dx++) {
            for (int dz = -searchRadius; dz <= searchRadius; dz++) {
                for (int dy = -3; dy <= 3; dy++) {
                    testPos.set(currentPos.getX() + dx, currentPos.getY() + dy, currentPos.getZ() + dz);

                    if (isDirtPath(this.level(), testPos)) {
                        BlockPos standPos = this.level().getBlockState(testPos).is(Blocks.DIRT_PATH) ? testPos.above() : testPos.immutable();

                        if (!isNearDangerousDrop(this.level(), standPos, 2) && !isNearHazard(this.level(), standPos, 2)) {
                            double distToBedSqr = standPos.distSqr(this.assignedBedPos);
                            // Buscar un camino que se aleje de la cama (entre 8 y 35 bloques)
                            if (distToBedSqr >= 64.0D && distToBedSqr <= 1225.0D) {
                                if (distToBedSqr > bestDistToBedSqr) {
                                    bestDistToBedSqr = distToBedSqr;
                                    bestPos = standPos.immutable();
                                }
                            }
                        }
                    }
                }
            }
        }

        return bestPos;
    }

    private void tickHunger(ServerLevel level) {
        this.hungerTickTimer++;

        // Desgaste natural pasivo constante (para que el hambre baje de forma visible y realista)
        this.addExhaustion(0.008F);

        // Desgaste adicional por movimiento físico (caminar/correr)
        if (this.getDeltaMovement().horizontalDistanceSqr() > 0.0005D) {
            this.addExhaustion(0.015F);
        }

        // Si tiene hambre o salud crítica, consumir alimento o raciones de emergencia
        if (!this.isSleeping() && this.hungerTickTimer % 30 == 0) {
            this.tryEmergencyHeal();
            if (this.needsFood()) {
                this.tryEatFromInventory();
            }
        }

        // Regeneración gradual de salud ("poco a poco") similar a la del jugador
        if (this.getHealth() < this.getMaxHealth()) {
            if (this.getSaturationLevel() > 0.0F && this.getFoodLevel() >= 20) {
                this.healthRegenTimer++;
                if (this.healthRegenTimer >= 40) { // cada 2 segundos con saturación alta
                    this.heal(1.0F);
                    this.addExhaustion(2.0F);
                    this.healthRegenTimer = 0;
                    level.sendParticles(ParticleTypes.HEART,
                            this.getX(), this.getY() + 1.2D, this.getZ(),
                            1, 0.2D, 0.2D, 0.2D, 0.02D);
                }
            } else if (this.getFoodLevel() >= 18) {
                this.healthRegenTimer++;
                if (this.healthRegenTimer >= 60) { // cada 3 segundos bien alimentada
                    this.heal(1.0F);
                    this.addExhaustion(2.5F);
                    this.healthRegenTimer = 0;
                    level.sendParticles(ParticleTypes.HEART,
                            this.getX(), this.getY() + 1.2D, this.getZ(),
                            1, 0.2D, 0.2D, 0.2D, 0.02D);
                }
            } else {
                this.healthRegenTimer = 0;
            }
        } else {
            this.healthRegenTimer = 0;
        }

        // Inanición si el hambre llega a 0
        if (this.getFoodLevel() <= 0) {
            this.starvationTimer++;
            if (this.starvationTimer >= 80) {
                if (this.getHealth() > 1.0F) {
                    this.hurtServer(level, this.damageSources().starve(), 1.0F);
                }
                this.starvationTimer = 0;
            }
        } else {
            this.starvationTimer = 0;
        }
    }

    public void addExhaustion(float amount) {
        this.exhaustionLevel += amount;
        while (this.exhaustionLevel >= 4.0F) {
            this.exhaustionLevel -= 4.0F;
            if (this.getSaturationLevel() > 0.0F) {
                this.setSaturationLevel(Math.max(0.0F, this.getSaturationLevel() - 1.0F));
            } else if (this.getFoodLevel() > 0) {
                this.setFoodLevel(Math.max(0, this.getFoodLevel() - 1));
            }
        }
    }

    public boolean tryEatFromInventory() {
        if (this.isSleeping() || !this.needsFood()) {
            return false;
        }
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.inventory.getItem(i);
            FoodProperties food = stack.get(DataComponents.FOOD);
            if (food != null) {
                // Raciones de élite (Manzanas doradas, zanahorias doradas):
                // Reservadas exclusivamente para salud crítica (<= 30%), duelos o guerra/combate
                if (stack.is(Items.GOLDEN_APPLE) || stack.is(Items.ENCHANTED_GOLDEN_APPLE) || stack.is(Items.GOLDEN_CARROT)) {
                    boolean isCritical = (this.getHealth() / this.getMaxHealth()) <= 0.30F;
                    boolean inDuelOrWar = this.isInPlayerDuel() || this.isInQueenDuel() || this.getTarget() != null || this.isInPowerStruggle();
                    if (!isCritical && !inDuelOrWar) {
                        continue;
                    }
                }
                this.eat(stack, food);
                return true;
            }
        }
        return false;
    }

    public boolean tryEmergencyHeal() {
        if (this.isSleeping() || !this.isAlive()) {
            return false;
        }
        boolean isCritical = (this.getHealth() / this.getMaxHealth()) <= 0.30F;
        boolean inDuelOrWar = this.isInPlayerDuel() || this.isInQueenDuel() || this.getTarget() != null || this.isInPowerStruggle();
        if (isCritical || (inDuelOrWar && this.getHealth() < this.getMaxHealth())) {
            for (int i = 0; i < this.inventory.getContainerSize(); i++) {
                ItemStack stack = this.inventory.getItem(i);
                if (stack.is(Items.GOLDEN_APPLE) || stack.is(Items.ENCHANTED_GOLDEN_APPLE) || stack.is(Items.GOLDEN_CARROT)) {
                    FoodProperties food = stack.get(DataComponents.FOOD);
                    if (food != null) {
                        this.eat(stack, food);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void eat(ItemStack stack, FoodProperties food) {
        if (this.isSleeping()) {
            return;
        }
        int nutrition = food.nutrition();
        float saturationModifier = food.saturation();
        float saturationAdded = nutrition * saturationModifier * 2.0F;

        int newFood = Math.min(20, this.getFoodLevel() + nutrition);
        float newSat = Math.min((float) newFood, this.getSaturationLevel() + saturationAdded);

        this.setFoodLevel(newFood);
        this.setSaturationLevel(newSat);

        // Efectos medicinales especiales de raciones de élite
        if (stack.is(Items.GOLDEN_APPLE)) {
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));
            this.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 0));
        } else if (stack.is(Items.ENCHANTED_GOLDEN_APPLE)) {
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 400, 1));
            this.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 6000, 0));
            this.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 6000, 0));
            this.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 3));
        } else if (stack.is(Items.GOLDEN_CARROT)) {
            this.heal(6.0F);
        }

        // Efectos de comer (sonido y partículas)
        this.playSound(SoundEvents.GENERIC_EAT.value(), 0.7F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, stack.getItem()),
                    this.getX(), this.getY() + 1.2D, this.getZ(),
                    8, 0.2D, 0.2D, 0.2D, 0.05D);
        }

        stack.shrink(1);
    }

    /* =========================================================================
       AGRICULTURA Y CRAFTEO DE PAN (Trigo -> Pan y Cultivo)
       ========================================================================= */

    public void tryCraftBread() {
        if (this.isSleeping() || (this.isSubdita() && this.getProfession() != KoboldProfession.FARMER)) {
            return;
        }
        // Hornear pan en el horario asignado (1:00 PM a 3:30 PM: ticks 7000 a 9500), o si el inventario está totalmente lleno
        if (!this.isCookingAndCraftingTime() && this.hasFreeInventorySlot()) {
            return;
        }
        int totalWheat = 0;
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.inventory.getItem(i);
            if (stack.is(Items.WHEAT)) {
                totalWheat += stack.getCount();
            }
        }

        if (totalWheat >= 3) {
            int breadToCraft = totalWheat / 3;
            int wheatToRemove = breadToCraft * 3;

            int removed = 0;
            for (int i = 0; i < this.inventory.getContainerSize() && removed < wheatToRemove; i++) {
                ItemStack stack = this.inventory.getItem(i);
                if (stack.is(Items.WHEAT)) {
                    int canRemove = Math.min(stack.getCount(), wheatToRemove - removed);
                    stack.shrink(canRemove);
                    removed += canRemove;
                }
            }

            int outputBread = this.getProfessionTier() >= 2 ? breadToCraft * 2 : breadToCraft;
            ItemStack breadStack = new ItemStack(Items.BREAD, outputBread);
            ItemStack leftover = this.inventory.addItem(breadStack);
            if (!leftover.isEmpty() && this.level() instanceof ServerLevel serverLevel) {
                this.spawnAtLocation(serverLevel, leftover);
            }

            this.playSound(SoundEvents.VILLAGER_WORK_FARMER, 0.8F, 1.0F);
            this.playSound(SoundEvents.ITEM_PICKUP, 0.4F, 1.2F);
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        this.getX(), this.getY() + 1.2D, this.getZ(),
                        6, 0.25D, 0.25D, 0.25D, 0.05D);
            }

            if (this.needsFood()) {
                this.tryEatFromInventory();
            }
        }
    }

    public void tryCraftAlchemySupplies() {
        if (this.isSleeping() || (this.isSubdita() && this.getProfession() != KoboldProfession.ALCHEMIST)) {
            return;
        }

        int totalGoldIngots = 0;
        int totalGoldNuggets = 0;
        int totalApples = 0;
        int totalCarrots = 0;

        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.inventory.getItem(i);
            if (stack.is(Items.GOLD_INGOT)) totalGoldIngots += stack.getCount();
            else if (stack.is(Items.GOLD_NUGGET)) totalGoldNuggets += stack.getCount();
            else if (stack.is(Items.APPLE)) totalApples += stack.getCount();
            else if (stack.is(Items.CARROT)) totalCarrots += stack.getCount();
        }

        // Si tiene lingotes pero pocas pepitas y tiene zanahorias, convertir 1 lingote en 9 pepitas
        if (totalGoldIngots > 0 && totalGoldNuggets < 8 && totalCarrots > 0) {
            shrinkItemInInventory(Items.GOLD_INGOT, 1);
            totalGoldIngots--;
            ItemStack nuggets = new ItemStack(Items.GOLD_NUGGET, 9);
            ItemStack leftover = this.inventory.addItem(nuggets);
            if (!leftover.isEmpty() && this.level() instanceof ServerLevel sl) {
                this.spawnAtLocation(sl, leftover);
            }
            totalGoldNuggets += 9;
        }

        // A. Craftear Manzana Dorada (8 lingotes de oro + 1 manzana)
        if (totalGoldIngots >= 8 && totalApples >= 1) {
            shrinkItemInInventory(Items.GOLD_INGOT, 8);
            shrinkItemInInventory(Items.APPLE, 1);
            ItemStack goldenApple = new ItemStack(Items.GOLDEN_APPLE, 1);
            ItemStack leftover = this.inventory.addItem(goldenApple);
            if (!leftover.isEmpty() && this.level() instanceof ServerLevel sl) {
                this.spawnAtLocation(sl, leftover);
            }
            this.playSound(SoundEvents.BREWING_STAND_BREW, 0.9F, 1.2F);
            if (this.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.2D, this.getZ(), 8, 0.3D, 0.3D, 0.3D, 0.05D);
            }
        }

        // B. Craftear Zanahoria Dorada (8 pepitas de oro + 1 zanahoria)
        if (totalGoldNuggets >= 8 && totalCarrots >= 1) {
            shrinkItemInInventory(Items.GOLD_NUGGET, 8);
            shrinkItemInInventory(Items.CARROT, 1);
            ItemStack goldenCarrot = new ItemStack(Items.GOLDEN_CARROT, 1);
            ItemStack leftover = this.inventory.addItem(goldenCarrot);
            if (!leftover.isEmpty() && this.level() instanceof ServerLevel sl) {
                this.spawnAtLocation(sl, leftover);
            }
            this.playSound(SoundEvents.BREWING_STAND_BREW, 0.9F, 1.2F);
            if (this.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.2D, this.getZ(), 8, 0.3D, 0.3D, 0.3D, 0.05D);
            }
        }
    }

    public void shrinkItemInInventory(net.minecraft.world.item.Item item, int count) {
        int remaining = count;
        for (int i = 0; i < this.inventory.getContainerSize() && remaining > 0; i++) {
            ItemStack stack = this.inventory.getItem(i);
            if (stack.is(item)) {
                int toShrink = Math.min(stack.getCount(), remaining);
                stack.shrink(toShrink);
                remaining -= toShrink;
            }
        }
    }

    public boolean hasPlantableSeeds() {
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.inventory.getItem(i);
            if (isPlantableSeed(stack)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPlantableSeed(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.is(Items.WHEAT_SEEDS)
                || stack.is(Items.CARROT)
                || stack.is(Items.POTATO)
                || stack.is(Items.BEETROOT_SEEDS);
    }

    public BlockState getCropForSeed(ItemStack stack) {
        if (stack.is(Items.WHEAT_SEEDS)) return Blocks.WHEAT.defaultBlockState();
        if (stack.is(Items.CARROT)) return Blocks.CARROTS.defaultBlockState();
        if (stack.is(Items.POTATO)) return Blocks.POTATOES.defaultBlockState();
        if (stack.is(Items.BEETROOT_SEEDS)) return Blocks.BEETROOTS.defaultBlockState();
        return null;
    }

    public ItemStack takeOneSeed() {
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.inventory.getItem(i);
            if (isPlantableSeed(stack)) {
                return this.inventory.removeItem(i, 1);
            }
        }
        return ItemStack.EMPTY;
    }

    public static Container getChestContainer(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof ChestBlock chestBlock) {
            // Permitir acceso ignorando bloqueo superior para que cofres con bloques o marcos de ítem encima sean accesibles
            Container c = ChestBlock.getContainer(chestBlock, state, level, pos, true);
            if (c != null) {
                return c;
            }
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof Container container) {
            return container;
        }
        return null;
    }

    public static BlockPos getCanonicalChestPos(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof ChestBlock) {
            if (state.hasProperty(ChestBlock.TYPE)) {
                ChestType type = state.getValue(ChestBlock.TYPE);
                if (type != ChestType.SINGLE) {
                    Direction connectedDir = ChestBlock.getConnectedDirection(state);
                    BlockPos other = pos.relative(connectedDir);
                    if (pos.compareTo(other) < 0) {
                        return pos.immutable();
                    } else {
                        return other.immutable();
                    }
                }
            }
        }
        return pos.immutable();
    }

    public static ItemStack getFramedItemStack(Level level, BlockPos pos) {
        // Expandir área vertical y horizontal para detectar marcos de ítem colocados encima o en la pared sobre el cofre
        AABB box = new AABB(pos).inflate(1.75D, 2.5D, 1.75D);
        List<ItemFrame> frames = level.getEntitiesOfClass(ItemFrame.class, box,
                f -> !f.getItem().isEmpty() && f.isAlive());
        if (!frames.isEmpty()) {
            frames.sort(Comparator.comparingDouble(f -> f.distanceToSqr(Vec3.atCenterOf(pos))));
            return frames.get(0).getItem();
        }
        return ItemStack.EMPTY;
    }

    public static ChestCategory getCategoryFromItemFrame(Level level, BlockPos pos) {
        ItemStack framed = getFramedItemStack(level, pos);
        if (!framed.isEmpty()) {
            return getCategoryForItem(framed);
        }
        return null;
    }

    public static ChestCategory getChestPurpose(Level level, BlockPos pos) {
        BlockPos canonical = getCanonicalChestPos(level, pos);

        // 1. Marco de ítem sobre el cofre o en la pared adyacente (revisando ambas mitades si es cofre doble)
        ChestCategory fromFrame = getCategoryFromItemFrame(level, pos);
        if (fromFrame == null && !canonical.equals(pos)) {
            fromFrame = getCategoryFromItemFrame(level, canonical);
        }
        if (fromFrame == null) {
            BlockState state = level.getBlockState(canonical);
            if (state.getBlock() instanceof ChestBlock && state.hasProperty(ChestBlock.TYPE)) {
                ChestType type = state.getValue(ChestBlock.TYPE);
                if (type != ChestType.SINGLE) {
                    Direction dir = ChestBlock.getConnectedDirection(state);
                    BlockPos other = canonical.relative(dir);
                    fromFrame = getCategoryFromItemFrame(level, other);
                }
            }
        }
        if (fromFrame != null) {
            CLAIMED_CHEST_PURPOSES.put(canonical, fromFrame);
            return fromFrame;
        }

        // 2. Reclamación explícita previa en memoria (para cofres comunitarios asignados)
        if (CLAIMED_CHEST_PURPOSES.containsKey(canonical)) {
            return CLAIMED_CHEST_PURPOSES.get(canonical);
        }

        // Si no tiene marco de ítem explícito, el cofre permanece como cofre general (sin propósito exclusivo)
        // para garantizar que las Kobolds jamás vacíen ni alteren cofres personales o mixtos del jugador.
        return null;
    }

    public static void setChestPurpose(Level level, BlockPos pos, ChestCategory category) {
        BlockPos canonical = getCanonicalChestPos(level, pos);
        if (category != null) {
            CLAIMED_CHEST_PURPOSES.put(canonical, category);
        }
    }

    public static boolean isMisplacedItemForCategory(ItemStack stack, ChestCategory chestCat) {
        if (stack.isEmpty() || chestCat == null || chestCat == ChestCategory.MISC) {
            return false;
        }
        switch (chestCat) {
            case FOOD -> {
                return !stack.has(DataComponents.FOOD) && !stack.is(Items.WHEAT);
            }
            case SEEDS -> {
                // Semillas puras o zanahorias y papas (compatibilidad como alimento y semillas)
                if (isPureSeed(stack)) return false;
                if (stack.is(Items.CARROT) || stack.is(Items.POTATO)) return false;
                return true;
            }
            case MINERALS -> {
                return !isMineralOrResource(stack);
            }
            case TERRAIN -> {
                if (stack.getItem() instanceof BlockItem bi) {
                    return !isTerrainBlock(bi.getBlock());
                }
                return true;
            }
            case BUILDING -> {
                return getCategoryForItem(stack) != ChestCategory.BUILDING;
            }
            case MEDICAL -> {
                return getCategoryForItem(stack) != ChestCategory.MEDICAL;
            }
            default -> {
                return false;
            }
        }
    }

    public static boolean isPureSeed(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.is(Items.WHEAT_SEEDS)
                || stack.is(Items.BEETROOT_SEEDS)
                || stack.is(Items.MELON_SEEDS)
                || stack.is(Items.PUMPKIN_SEEDS)
                || stack.is(Items.TORCHFLOWER_SEEDS)
                || stack.is(Items.PITCHER_POD);
    }

    public boolean hasPureSeedsInInventory() {
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.inventory.getItem(i);
            if (isPureSeed(stack)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasEmptyFarmlandNearby(int range) {
        BlockPos current = this.blockPosition();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        Level level = this.level();
        for (int dx = -range; dx <= range; dx++) {
            for (int dy = -3; dy <= 3; dy++) {
                for (int dz = -range; dz <= range; dz++) {
                    pos.set(current.getX() + dx, current.getY() + dy, current.getZ() + dz);
                    if (level.getBlockState(pos).is(Blocks.FARMLAND) && level.getBlockState(pos.above()).isAir()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isMineralOrResource(ItemStack stack) {
        if (stack.isEmpty()) return false;

        // Carbón
        if (stack.is(Items.COAL) || stack.is(Items.CHARCOAL) || stack.is(Items.COAL_BLOCK)) return true;
        // Hierro
        if (stack.is(Items.RAW_IRON) || stack.is(Items.IRON_INGOT) || stack.is(Items.IRON_NUGGET)
                || stack.is(Items.RAW_IRON_BLOCK) || stack.is(Items.IRON_BLOCK)) return true;
        // Oro
        if (stack.is(Items.RAW_GOLD) || stack.is(Items.GOLD_INGOT) || stack.is(Items.GOLD_NUGGET)
                || stack.is(Items.RAW_GOLD_BLOCK) || stack.is(Items.GOLD_BLOCK)) return true;
        // Cobre
        if (stack.is(Items.RAW_COPPER) || stack.is(Items.COPPER_INGOT)
                || stack.is(Items.RAW_COPPER_BLOCK)) return true;
        // Diamante y Esmeralda
        if (stack.is(Items.DIAMOND) || stack.is(Items.DIAMOND_BLOCK)) return true;
        if (stack.is(Items.EMERALD) || stack.is(Items.EMERALD_BLOCK)) return true;
        // Lapis y Redstone
        if (stack.is(Items.LAPIS_LAZULI) || stack.is(Items.LAPIS_BLOCK)) return true;
        if (stack.is(Items.REDSTONE) || stack.is(Items.REDSTONE_BLOCK)) return true;
        // Netherite y Cuarzo
        if (stack.is(Items.NETHERITE_INGOT) || stack.is(Items.NETHERITE_SCRAP) || stack.is(Items.NETHERITE_BLOCK)
                || stack.is(Items.ANCIENT_DEBRIS)) return true;
        if (stack.is(Items.QUARTZ) || stack.is(Items.QUARTZ_BLOCK)) return true;
        if (stack.is(Items.AMETHYST_SHARD) || stack.is(Items.AMETHYST_BLOCK)) return true;

        if (stack.is(ItemTags.COALS)) return true;

        // Bloques de menas
        if (stack.getItem() instanceof BlockItem blockItem) {
            Block b = blockItem.getBlock();
            if (b == Blocks.COAL_ORE || b == Blocks.DEEPSLATE_COAL_ORE
                    || b == Blocks.IRON_ORE || b == Blocks.DEEPSLATE_IRON_ORE
                    || b == Blocks.COPPER_ORE || b == Blocks.DEEPSLATE_COPPER_ORE
                    || b == Blocks.GOLD_ORE || b == Blocks.DEEPSLATE_GOLD_ORE
                    || b == Blocks.REDSTONE_ORE || b == Blocks.DEEPSLATE_REDSTONE_ORE
                    || b == Blocks.EMERALD_ORE || b == Blocks.DEEPSLATE_EMERALD_ORE
                    || b == Blocks.LAPIS_ORE || b == Blocks.DEEPSLATE_LAPIS_ORE
                    || b == Blocks.DIAMOND_ORE || b == Blocks.DEEPSLATE_DIAMOND_ORE
                    || b == Blocks.NETHER_GOLD_ORE || b == Blocks.NETHER_QUARTZ_ORE
                    || b == Blocks.ANCIENT_DEBRIS) {
                return true;
            }
        }

        return false;
    }

    public static boolean isTerrainBlock(Block block) {
        // Tierra, césped, lodo, arcilla, micelio, podzol
        if (block == Blocks.DIRT || block == Blocks.GRASS_BLOCK || block == Blocks.COARSE_DIRT
                || block == Blocks.ROOTED_DIRT || block == Blocks.MUD || block == Blocks.CLAY
                || block == Blocks.PODZOL || block == Blocks.MYCELIUM || block == Blocks.FARMLAND
                || block == Blocks.DIRT_PATH || block == Blocks.MUDDY_MANGROVE_ROOTS) {
            return true;
        }
        // Arena y grava
        if (block == Blocks.SAND || block == Blocks.RED_SAND || block == Blocks.GRAVEL
                || block == Blocks.SUSPICIOUS_SAND || block == Blocks.SUSPICIOUS_GRAVEL) {
            return true;
        }
        // Rocas naturales de generación de mundo
        if (block == Blocks.STONE || block == Blocks.DEEPSLATE || block == Blocks.ANDESITE
                || block == Blocks.DIORITE || block == Blocks.GRANITE || block == Blocks.TUFF
                || block == Blocks.CALCITE || block == Blocks.DRIPSTONE_BLOCK) {
            return true;
        }
        // Terrenos del Nether y End
        if (block == Blocks.NETHERRACK || block == Blocks.SOUL_SAND || block == Blocks.SOUL_SOIL
                || block == Blocks.BASALT || block == Blocks.SMOOTH_BASALT || block == Blocks.BLACKSTONE
                || block == Blocks.END_STONE) {
            return true;
        }
        // Hielo y nieve
        if (block == Blocks.SNOW_BLOCK || block == Blocks.SNOW || block == Blocks.ICE
                || block == Blocks.PACKED_ICE || block == Blocks.BLUE_ICE) {
            return true;
        }
        // Areniscas naturales
        if (block == Blocks.SANDSTONE || block == Blocks.RED_SANDSTONE) {
            return true;
        }

        return false;
    }

    public static ChestCategory getCategoryForItem(ItemStack stack) {
        if (stack.isEmpty()) return ChestCategory.MISC;

        // 0. SUMINISTROS MÉDICOS Y ALQUIMIA
        if (stack.is(Items.GOLDEN_APPLE) || stack.is(Items.ENCHANTED_GOLDEN_APPLE)
                || stack.is(Items.GOLDEN_CARROT) || stack.is(Items.POTION)
                || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION)) {
            return ChestCategory.MEDICAL;
        }

        // 1. COMIDA
        if (stack.has(DataComponents.FOOD) || stack.is(Items.WHEAT)) {
            return ChestCategory.FOOD;
        }

        // 2. SEMILLAS
        if (isPureSeed(stack)) {
            return ChestCategory.SEEDS;
        }

        // 3. MINERALES Y RECURSOS
        if (isMineralOrResource(stack)) {
            return ChestCategory.MINERALS;
        }

        // 4 y 5. BLOQUES (Terreno natural vs Construcción)
        if (stack.getItem() instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            if (isTerrainBlock(block)) {
                return ChestCategory.TERRAIN;
            }
            return ChestCategory.BUILDING;
        }

        return ChestCategory.MISC;
    }

    public boolean hasTerrainBlocksToStore() {
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.inventory.getItem(i);
            if (getCategoryForItem(stack) == ChestCategory.TERRAIN) {
                return true;
            }
        }
        return false;
    }

    public boolean hasBuildingBlocksToStore() {
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.inventory.getItem(i);
            if (getCategoryForItem(stack) == ChestCategory.BUILDING) {
                return true;
            }
        }
        return false;
    }

    public boolean hasMineralsToStore() {
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.inventory.getItem(i);
            if (getCategoryForItem(stack) == ChestCategory.MINERALS) {
                return true;
            }
        }
        return false;
    }

    public boolean hasMedicalItemsToStore() {
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.inventory.getItem(i);
            if (getCategoryForItem(stack) == ChestCategory.MEDICAL) {
                int toKeep = (stack.is(Items.GOLDEN_APPLE) || stack.is(Items.ENCHANTED_GOLDEN_APPLE)) ? 1 : 2;
                if (stack.getCount() > toKeep) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasBlocksToStore() {
        return this.hasTerrainBlocksToStore() || this.hasBuildingBlocksToStore();
    }

    public static float getFoodSaturationScore(ItemStack stack) {
        FoodProperties food = stack.get(DataComponents.FOOD);
        if (food == null) return 0.0F;
        return food.nutrition() * food.saturation() * 2.0F;
    }

    public int getTotalFoodCount() {
        int count = 0;
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.inventory.getItem(i);
            if ((stack.has(DataComponents.FOOD) || stack.is(Items.WHEAT)) && !isRawMeat(stack)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public int getWheatCount() {
        int count = 0;
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.inventory.getItem(i);
            if (stack.is(Items.WHEAT)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public static class FoodTypeEntry {
        public final net.minecraft.world.item.Item item;
        public int count;
        public final float saturationScore;

        public FoodTypeEntry(net.minecraft.world.item.Item item, int count, float saturationScore) {
            this.item = item;
            this.count = count;
            this.saturationScore = saturationScore;
        }
    }

    public List<FoodTypeEntry> getFoodTypesInInventory() {
        Map<net.minecraft.world.item.Item, FoodTypeEntry> map = new LinkedHashMap<>();
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.inventory.getItem(i);
            if (stack.has(DataComponents.FOOD) && !isRawMeat(stack)) {
                net.minecraft.world.item.Item item = stack.getItem();
                FoodTypeEntry entry = map.get(item);
                if (entry == null) {
                    entry = new FoodTypeEntry(item, stack.getCount(), getFoodSaturationScore(stack));
                    map.put(item, entry);
                } else {
                    entry.count += stack.getCount();
                }
            }
        }
        List<FoodTypeEntry> list = new ArrayList<>(map.values());
        list.sort((a, b) -> Float.compare(b.saturationScore, a.saturationScore));
        return list;
    }

    public int getSeedCount() {
        int count = 0;
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.inventory.getItem(i);
            if (isPureSeed(stack) || isPlantableSeed(stack)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public int getInventoryArrowCount() {
        int count = 0;
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.inventory.getItem(i);
            if (stack.is(ItemTags.ARROWS)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public boolean hasExcessFoodToStore() {
        if (this.isStorageAndOrganizationTime()) {
            return this.getTotalFoodCount() > 2 || this.getWheatCount() > 0 || (this.hasRawMeatInInventory() && this.getProfession() == KoboldProfession.HUNTER);
        }
        return this.getTotalFoodCount() > 8;
    }

    public boolean hasSurplusSeeds() {
        if (this.getProfession() == KoboldProfession.FARMER) {
            int keep = this.isStorageAndOrganizationTime() ? 16 : 32;
            return this.getSeedCount() > keep;
        }
        return this.getSeedCount() > 0;
    }

    public boolean needsSeedsRestock() {
        if (this.getProfession() != KoboldProfession.FARMER) {
            return false;
        }
        return !this.hasPlantableSeeds() && this.hasEmptyFarmlandNearby(16);
    }

    public boolean isEssentialPersonalItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.is(ModItems.BACULO.get())) return true;
        if (stack.has(DataComponents.EQUIPPABLE) || stack.is(ItemTags.HEAD_ARMOR) || stack.is(ItemTags.CHEST_ARMOR) || stack.is(ItemTags.LEG_ARMOR) || stack.is(ItemTags.FOOT_ARMOR)) return true;
        if (isWeapon(stack) || stack.is(ItemTags.PICKAXES) || stack.is(ItemTags.AXES) || stack.is(ItemTags.SHOVELS) || stack.is(ItemTags.HOES)) return true;
        if (stack.has(DataComponents.FOOD) || stack.is(Items.WHEAT)) return true;
        if (isPureSeed(stack) || isPlantableSeed(stack)) return true;
        if (stack.is(ItemTags.ARROWS)) return true;
        return false;
    }

    public boolean hasMiscItemsToStore() {
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.inventory.getItem(i);
            if (!stack.isEmpty() && !this.isEssentialPersonalItem(stack)) {
                ChestCategory cat = getCategoryForItem(stack);
                if (cat == ChestCategory.MISC) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasFreeInventorySlot() {
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            if (this.inventory.getItem(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public enum ChestAction {
        RESTOCK_FOOD,
        RESTOCK_SEEDS,
        RESTOCK_ARROWS,
        STORE,
        ORGANIZE
    }

    public static class ChestTask {
        public final BlockPos chestPos;
        public final ChestCategory category;
        public final ChestAction action;

        public ChestTask(BlockPos pos, ChestCategory cat, ChestAction action) {
            this.chestPos = pos;
            this.category = cat;
            this.action = action;
        }
    }

    public static List<BlockPos> findNearbyUniqueChests(Level level, BlockPos center, int radius, int yRadius) {
        Set<BlockPos> unique = new LinkedHashSet<>();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        for (int dy = -yRadius; dy <= yRadius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    pos.set(cx + dx, cy + dy, cz + dz);
                    BlockState state = level.getBlockState(pos);
                    if (state.getBlock() instanceof ChestBlock || state.is(Blocks.BARREL)) {
                        BlockPos canonical = getCanonicalChestPos(level, pos);
                        Container c = getChestContainer(level, canonical);
                        if (c != null) {
                            unique.add(canonical.immutable());
                        }
                    }
                }
            }
        }
        List<BlockPos> list = new ArrayList<>(unique);
        list.sort(Comparator.comparingDouble(p -> p.distSqr(center)));
        return list;
    }

    public @Nullable BlockPos findBestChestForSeeds(List<BlockPos> chests) {
        BlockPos fallbackChest = null;
        for (BlockPos pos : chests) {
            Container c = getChestContainer(this.level(), pos);
            if (c == null || c.isEmpty()) continue;

            boolean hasSeeds = false;
            for (int i = 0; i < c.getContainerSize(); i++) {
                ItemStack stack = c.getItem(i);
                if (isPlantableSeed(stack)) {
                    hasSeeds = true;
                    break;
                }
            }
            if (!hasSeeds) continue;

            ChestCategory purpose = getChestPurpose(this.level(), pos);
            // 1. Prioriza buscar en cofres designados como SEEDS (o sin asignar) que contengan semillas plantables
            if (purpose == ChestCategory.SEEDS || purpose == null) {
                return pos;
            }

            // 2. Respaldo: cualquier otro cofre cercano con semillas plantables (ej. zanahorias o papas en FOOD)
            if (fallbackChest == null) {
                fallbackChest = pos;
            }
        }
        return fallbackChest;
    }

    public @Nullable ChestTask findBestChestTask() {
        if (this.isSleeping() || this.isThreatened() || this.isHunting()) {
            return null;
        }

        boolean isOwnedMob = this.isOwned();

        boolean isStorageTime = this.isStorageAndOrganizationTime();
        boolean isCriticalFood = this.needsFoodRestock();
        boolean isInventoryFull = !this.hasFreeInventorySlot();

        if (!isStorageTime && !isCriticalFood && !isInventoryFull) {
            return null;
        }

        List<BlockPos> chests = findNearbyUniqueChests(this.level(), this.blockPosition(), 16, 6);
        if (chests.isEmpty()) {
            return null;
        }

        // Si la Kobold no tiene dueño, solo puede interactuar con cofres que tengan marco de ítem explícito
        // (los cofres sin marco son personales del jugador y las salvajes no los tocan)
        if (!isOwnedMob) {
            chests.removeIf(p -> getCategoryFromItemFrame(this.level(), p) == null);
            if (chests.isEmpty()) return null;
        }

        AbstractTribalEntity queen = this.isRebel() ? this.getTribeQueen() : null;
        Vec3 queenPos = (queen != null && queen.isAlive()) ? queen.position() : null;

        // Filtrar cofres para rebeldes: deben estar a <= 14 bloques de la Reina
        if (queenPos != null) {
            chests.removeIf(p -> Vec3.atCenterOf(p).distanceTo(queenPos) > 14.0D);
            if (chests.isEmpty()) return null;
        }

        // 1. REABASTECIMIENTO DE COMIDA: Cuando su comida baja a 1 o 0 ítems, acuden exclusivamente al cofre de FOOD
        if (this.needsFoodRestock()) {
            for (BlockPos pos : chests) {
                if (getChestPurpose(this.level(), pos) == ChestCategory.FOOD) {
                    Container c = getChestContainer(this.level(), pos);
                    if (c != null && !c.isEmpty()) {
                        for (int i = 0; i < c.getContainerSize(); i++) {
                            ItemStack stack = c.getItem(i);
                            if (stack.has(DataComponents.FOOD) || stack.is(Items.WHEAT)) {
                                return new ChestTask(pos, ChestCategory.FOOD, ChestAction.RESTOCK_FOOD);
                            }
                        }
                    }
                }
            }
        }

        // Si estamos fuera del horario de almacenamiento y el inventario no está lleno, solo se atiende comida crítica
        if (!isStorageTime && !isInventoryFull) {
            return null;
        }

        // 2. REABASTECIMIENTO DE SEMILLAS: Granjeras sin semillas y con tierra arada cercana
        if (this.needsSeedsRestock()) {
            BlockPos seedChest = this.findBestChestForSeeds(chests);
            if (seedChest != null) {
                return new ChestTask(seedChest, ChestCategory.SEEDS, ChestAction.RESTOCK_SEEDS);
            }
        }

        // 3. REABASTECIMIENTO DE FLECHAS: arqueras con < 8 flechas
        if (this.hasRangedWeapon() && this.getInventoryArrowCount() < 8) {
            for (BlockPos pos : chests) {
                Container c = getChestContainer(this.level(), pos);
                if (c != null && !c.isEmpty()) {
                    for (int i = 0; i < c.getContainerSize(); i++) {
                        ItemStack stack = c.getItem(i);
                        if (stack.is(ItemTags.ARROWS)) {
                            return new ChestTask(pos, ChestCategory.MISC, ChestAction.RESTOCK_ARROWS);
                        }
                    }
                }
            }
        }

        // 4. ALMACENAR EXCEDENTES (Almacenamiento estricto por categoría)
        ChestCategory storeCategory = null;
        if (this.hasMedicalItemsToStore()) {
            storeCategory = ChestCategory.MEDICAL;
        } else if (this.hasExcessFoodToStore()) {
            storeCategory = ChestCategory.FOOD;
        } else if (this.hasSurplusSeeds()) {
            storeCategory = ChestCategory.SEEDS;
        } else if (this.hasMineralsToStore()) {
            storeCategory = ChestCategory.MINERALS;
        } else if (this.hasBuildingBlocksToStore()) {
            storeCategory = ChestCategory.BUILDING;
        } else if (this.hasTerrainBlocksToStore()) {
            storeCategory = ChestCategory.TERRAIN;
        } else if (this.hasMiscItemsToStore()) {
            storeCategory = ChestCategory.MISC;
        }

        if (storeCategory != null) {
            BlockPos targetChest = this.findBestChestForCategory(chests, storeCategory);
            if (targetChest != null) {
                return new ChestTask(targetChest, storeCategory, ChestAction.STORE);
            }
        }

        // 5. ORGANIZAR COFRES (Limpieza activa de ítems extraviados): solo en horario de almacenamiento
        // y EXCLUSIVAMENTE en cofres que tengan un marco de ítem explícito
        if (isStorageTime && this.hasFreeInventorySlot()) {
            for (BlockPos pos : chests) {
                ChestCategory framedCat = getCategoryFromItemFrame(this.level(), pos);
                if (framedCat != null) {
                    Container c = getChestContainer(this.level(), pos);
                    if (c != null && !c.isEmpty()) {
                        for (int i = 0; i < c.getContainerSize(); i++) {
                            ItemStack stack = c.getItem(i);
                            if (isMisplacedItemForCategory(stack, framedCat)) {
                                return new ChestTask(pos, framedCat, ChestAction.ORGANIZE);
                            }
                        }
                    }
                }
            }
        }

        // 6. INSPECCIONAR Y CONSOLIDAR COFRES MARCADOS: durante horario de almacenamiento
        if (isStorageTime) {
            for (BlockPos pos : chests) {
                ChestCategory framedCat = getCategoryFromItemFrame(this.level(), pos);
                if (framedCat != null) {
                    Container c = getChestContainer(this.level(), pos);
                    if (c != null) {
                        return new ChestTask(pos, framedCat, ChestAction.ORGANIZE);
                    }
                }
            }
        }

        return null;
    }

    public @Nullable BlockPos findBestChestForCategory(List<BlockPos> chests, ChestCategory cat) {
        BlockPos bestDesignated = null;
        BlockPos bestMatchingContent = null;
        BlockPos bestEmpty = null;

        for (BlockPos pos : chests) {
            Container c = getChestContainer(this.level(), pos);
            if (c == null) continue;

            boolean hasSpace = false;
            boolean hasMatchingItems = false;
            for (int i = 0; i < c.getContainerSize(); i++) {
                ItemStack slot = c.getItem(i);
                if (slot.isEmpty()) {
                    hasSpace = true;
                } else {
                    if (getCategoryForItem(slot) == cat) {
                        hasMatchingItems = true;
                    }
                    if (slot.getCount() < slot.getMaxStackSize()) {
                        hasSpace = true;
                    }
                }
            }
            if (!hasSpace) continue;

            ChestCategory purpose = getChestPurpose(this.level(), pos);
            if (purpose == cat) {
                if (bestDesignated == null) bestDesignated = pos;
            } else if (purpose == null) {
                if (c.isEmpty()) {
                    if (bestEmpty == null) bestEmpty = pos;
                } else if (hasMatchingItems) {
                    if (bestMatchingContent == null) bestMatchingContent = pos;
                }
            }
        }

        if (bestDesignated != null) return bestDesignated;
        if (bestMatchingContent != null) return bestMatchingContent;
        if (bestEmpty != null) return bestEmpty;
        if (cat == ChestCategory.MISC) {
            for (BlockPos pos : chests) {
                ChestCategory purpose = getChestPurpose(this.level(), pos);
                if (purpose == null || purpose == ChestCategory.MISC) {
                    Container c = getChestContainer(this.level(), pos);
                    if (c != null) return pos;
                }
            }
        }
        return null;
    }

    public static void consolidateChestStacks(Container chest) {
        int size = chest.getContainerSize();
        for (int i = 0; i < size; i++) {
            ItemStack stack = chest.getItem(i);
            if (stack.isEmpty() || stack.getCount() >= stack.getMaxStackSize()) continue;
            for (int j = i + 1; j < size; j++) {
                ItemStack other = chest.getItem(j);
                if (!other.isEmpty() && ItemStack.isSameItemSameComponents(stack, other)) {
                    int canAdd = stack.getMaxStackSize() - stack.getCount();
                    int toMove = Math.min(other.getCount(), canAdd);
                    stack.grow(toMove);
                    other.shrink(toMove);
                    if (other.isEmpty()) {
                        chest.setItem(j, ItemStack.EMPTY);
                    }
                    if (stack.getCount() >= stack.getMaxStackSize()) break;
                }
            }
        }
        chest.setChanged();
    }

    public void relocateMisplacedItems(Container chest, BlockPos chestPos, ChestCategory chestCat) {
        if (chestCat == null || chestCat == ChestCategory.MISC) return;
        // Solo reubicar si el cofre tiene un marco de ítem explícito asignado por el jugador
        if (getCategoryFromItemFrame(this.level(), chestPos) == null) return;

        int freeSlots = 0;
        SimpleContainer inv = this.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (inv.getItem(i).isEmpty()) freeSlots++;
        }
        if (freeSlots <= 0) return;

        List<BlockPos> nearbyChests = findNearbyUniqueChests(this.level(), this.blockPosition(), 16, 6);
        int count = 0;
        for (int i = 0; i < chest.getContainerSize() && count < 4 && freeSlots > 0; i++) {
            ItemStack chestStack = chest.getItem(i);
            if (!chestStack.isEmpty() && isMisplacedItemForCategory(chestStack, chestCat)) {
                ChestCategory targetCat = getCategoryForItem(chestStack);
                // Solo extraer el ítem si existe un cofre destino válido con espacio cerca
                if (targetCat == ChestCategory.MISC || this.findBestChestForCategory(nearbyChests, targetCat) == null) {
                    continue;
                }
                ItemStack toMove = chest.removeItem(i, chestStack.getCount());
                ItemStack leftover = inv.addItem(toMove);
                if (!leftover.isEmpty()) {
                    addItemToContainer(chest, leftover);
                } else {
                    count++;
                    freeSlots--;
                }
            }
        }
        chest.setChanged();
    }

    public void storeItemsOfCategory(Container chest, ChestCategory category) {
        SimpleContainer inv = this.getInventory();
        switch (category) {
            case FOOD -> {
                List<FoodTypeEntry> foodTypes = this.getFoodTypesInInventory();
                int reserved = 0;
                int keepLimit = this.isStorageAndOrganizationTime() ? 2 : 6;
                for (FoodTypeEntry entry : foodTypes) {
                    for (int i = 0; i < inv.getContainerSize(); i++) {
                        ItemStack stack = inv.getItem(i);
                        if (stack.is(entry.item)) {
                            if (reserved < keepLimit) {
                                int toKeep = Math.min(stack.getCount(), keepLimit - reserved);
                                reserved += toKeep;
                                int toStore = stack.getCount() - toKeep;
                                if (toStore > 0) {
                                    ItemStack storeStack = inv.removeItem(i, toStore);
                                    ItemStack leftover = addItemToContainer(chest, storeStack);
                                    if (!leftover.isEmpty()) {
                                        inv.addItem(leftover);
                                        return;
                                    }
                                }
                            } else {
                                ItemStack storeStack = inv.removeItem(i, stack.getCount());
                                ItemStack leftover = addItemToContainer(chest, storeStack);
                                if (!leftover.isEmpty()) {
                                    inv.addItem(leftover);
                                    return;
                                }
                            }
                        }
                    }
                }
                // Guardar también trigo excedente y carne cruda no cocinada
                for (int i = 0; i < inv.getContainerSize(); i++) {
                    ItemStack stack = inv.getItem(i);
                    if (stack.is(Items.WHEAT) || (this.isStorageAndOrganizationTime() && isRawMeat(stack))) {
                        ItemStack storeStack = inv.removeItem(i, stack.getCount());
                        ItemStack leftover = addItemToContainer(chest, storeStack);
                        if (!leftover.isEmpty()) {
                            inv.addItem(leftover);
                            return;
                        }
                    }
                }
            }
            case SEEDS -> {
                int toKeep = (this.getProfession() == KoboldProfession.FARMER) ? (this.isStorageAndOrganizationTime() ? 16 : 32) : 0;
                int kept = 0;
                for (int i = 0; i < inv.getContainerSize(); i++) {
                    ItemStack stack = inv.getItem(i);
                    if (isPureSeed(stack)) {
                        if (kept < toKeep) {
                            int keepAmount = Math.min(stack.getCount(), toKeep - kept);
                            kept += keepAmount;
                            int storeAmount = stack.getCount() - keepAmount;
                            if (storeAmount > 0) {
                                ItemStack storeStack = inv.removeItem(i, storeAmount);
                                ItemStack leftover = addItemToContainer(chest, storeStack);
                                if (!leftover.isEmpty()) {
                                    inv.addItem(leftover);
                                    return;
                                }
                            }
                        } else {
                            ItemStack storeStack = inv.removeItem(i, stack.getCount());
                            ItemStack leftover = addItemToContainer(chest, storeStack);
                            if (!leftover.isEmpty()) {
                                inv.addItem(leftover);
                                return;
                            }
                        }
                    }
                }
            }
            case MEDICAL -> {
                for (int i = 0; i < inv.getContainerSize(); i++) {
                    ItemStack stack = inv.getItem(i);
                    if (getCategoryForItem(stack) == ChestCategory.MEDICAL) {
                        int toKeep = (stack.is(Items.GOLDEN_APPLE) || stack.is(Items.ENCHANTED_GOLDEN_APPLE)) ? 1 : 2;
                        if (stack.getCount() > toKeep) {
                            ItemStack storeStack = inv.removeItem(i, stack.getCount() - toKeep);
                            ItemStack leftover = addItemToContainer(chest, storeStack);
                            if (!leftover.isEmpty()) {
                                inv.addItem(leftover);
                                return;
                            }
                        }
                    }
                }
            }
            case MINERALS -> {
                for (int i = 0; i < inv.getContainerSize(); i++) {
                    ItemStack stack = inv.getItem(i);
                    if (getCategoryForItem(stack) == ChestCategory.MINERALS) {
                        ItemStack storeStack = inv.removeItem(i, stack.getCount());
                        ItemStack leftover = addItemToContainer(chest, storeStack);
                        if (!leftover.isEmpty()) {
                            inv.addItem(leftover);
                            return;
                        }
                    }
                }
            }
            case BUILDING -> {
                for (int i = 0; i < inv.getContainerSize(); i++) {
                    ItemStack stack = inv.getItem(i);
                    if (getCategoryForItem(stack) == ChestCategory.BUILDING) {
                        ItemStack storeStack = inv.removeItem(i, stack.getCount());
                        ItemStack leftover = addItemToContainer(chest, storeStack);
                        if (!leftover.isEmpty()) {
                            inv.addItem(leftover);
                            return;
                        }
                    }
                }
            }
            case TERRAIN -> {
                for (int i = 0; i < inv.getContainerSize(); i++) {
                    ItemStack stack = inv.getItem(i);
                    if (getCategoryForItem(stack) == ChestCategory.TERRAIN) {
                        ItemStack storeStack = inv.removeItem(i, stack.getCount());
                        ItemStack leftover = addItemToContainer(chest, storeStack);
                        if (!leftover.isEmpty()) {
                            inv.addItem(leftover);
                            return;
                        }
                    }
                }
            }
            case MISC -> {
                for (int i = 0; i < inv.getContainerSize(); i++) {
                    ItemStack stack = inv.getItem(i);
                    if (!stack.isEmpty() && !this.isEssentialPersonalItem(stack)) {
                        ItemStack storeStack = inv.removeItem(i, stack.getCount());
                        ItemStack leftover = addItemToContainer(chest, storeStack);
                        if (!leftover.isEmpty()) {
                            inv.addItem(leftover);
                            return;
                        }
                    }
                }
            }
        }
        chest.setChanged();
    }

    public void performCategorizedChestOperations(Container container, ChestTask task) {
        switch (task.action) {
            case RESTOCK_FOOD -> {
                int targetTotal = 8;
                int current = this.getTotalFoodCount();
                for (int i = 0; i < container.getContainerSize() && current < targetTotal; i++) {
                    ItemStack stack = container.getItem(i);
                    if (stack.has(DataComponents.FOOD) || stack.is(Items.WHEAT)) {
                        int needed = targetTotal - current;
                        int take = Math.min(stack.getCount(), needed);
                        ItemStack extracted = container.removeItem(i, take);
                        current += take;
                        ItemStack leftover = this.inventory.addItem(extracted);
                        if (!leftover.isEmpty()) {
                            addItemToContainer(container, leftover);
                            break;
                        }
                    }
                }
                consolidateChestStacks(container);
            }
            case RESTOCK_SEEDS -> {
                int targetTotal = 16;
                int current = this.getSeedCount();
                for (int i = 0; i < container.getContainerSize() && current < targetTotal; i++) {
                    ItemStack stack = container.getItem(i);
                    if (isPlantableSeed(stack)) {
                        int needed = targetTotal - current;
                        int take = Math.min(stack.getCount(), needed);
                        ItemStack extracted = container.removeItem(i, take);
                        current += take;
                        ItemStack leftover = this.inventory.addItem(extracted);
                        if (!leftover.isEmpty()) {
                            addItemToContainer(container, leftover);
                            break;
                        }
                    }
                }
                consolidateChestStacks(container);
            }
            case RESTOCK_ARROWS -> {
                int targetTotal = 16;
                int current = this.getInventoryArrowCount();
                for (int i = 0; i < container.getContainerSize() && current < targetTotal; i++) {
                    ItemStack stack = container.getItem(i);
                    if (stack.is(ItemTags.ARROWS)) {
                        int needed = targetTotal - current;
                        int take = Math.min(stack.getCount(), needed);
                        ItemStack extracted = container.removeItem(i, take);
                        current += take;
                        ItemStack leftover = this.inventory.addItem(extracted);
                        if (!leftover.isEmpty()) {
                            addItemToContainer(container, leftover);
                            break;
                        }
                    }
                }
                consolidateChestStacks(container);
            }
            case STORE -> {
                if (container.isEmpty() && getChestPurpose(this.level(), task.chestPos) == null) {
                    setChestPurpose(this.level(), task.chestPos, task.category);
                }
                this.storeItemsOfCategory(container, task.category);
                consolidateChestStacks(container);
                this.relocateMisplacedItems(container, task.chestPos, task.category);
            }
            case ORGANIZE -> {
                consolidateChestStacks(container);
                ChestCategory framedPurpose = getCategoryFromItemFrame(this.level(), task.chestPos);
                if (framedPurpose != null) {
                    this.relocateMisplacedItems(container, task.chestPos, framedPurpose);
                }
            }
        }
        container.setChanged();
    }

    public boolean needsFoodRestock() {
        return this.getTotalFoodCount() <= 1;
    }

    public ChestCategory getNextChestCategoryNeeded() {
        ChestTask task = this.findBestChestTask();
        return task != null ? task.category : null;
    }

    public boolean needsChestInteraction() {
        return this.findBestChestTask() != null;
    }

    public static ItemStack addItemToContainer(Container target, ItemStack stack) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        int size = target.getContainerSize();

        // 1er paso: fusionar en pilas existentes compatibles
        for (int i = 0; i < size; i++) {
            ItemStack existing = target.getItem(i);
            if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(stack, existing)) {
                int max = Math.min(target.getMaxStackSize(), existing.getMaxStackSize());
                int canAdd = max - existing.getCount();
                if (canAdd > 0) {
                    int toAdd = Math.min(stack.getCount(), canAdd);
                    existing.grow(toAdd);
                    stack.shrink(toAdd);
                    target.setChanged();
                    if (stack.isEmpty()) {
                        return ItemStack.EMPTY;
                    }
                }
            }
        }

        // 2do paso: colocar en ranuras vacías
        for (int i = 0; i < size; i++) {
            ItemStack existing = target.getItem(i);
            if (existing.isEmpty() && target.canPlaceItem(i, stack)) {
                int max = Math.min(target.getMaxStackSize(), stack.getMaxStackSize());
                int toAdd = Math.min(stack.getCount(), max);
                ItemStack newStack = stack.copy();
                newStack.setCount(toAdd);
                stack.shrink(toAdd);
                target.setItem(i, newStack);
                target.setChanged();
                if (stack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }

        return stack;
    }

    /* =========================================================================
       EQUIPAMIENTO AUTOMÁTICO DE ARMADURAS Y ARMAS
       ========================================================================= */

    public void autoEquipArmorAndWeapons() {
        if (this.level().isClientSide() || this.isSleeping()) return;
        this.autoEquipArmor();
    }

    public void autoEquipArmor() {
        if (this.level().isClientSide() || this.isSleeping() || this.needsArmorRetrieval) return;
        if (this.isSleepTime() && this.hasAssignedBed()) return;

        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack current = this.getItemBySlot(slot);
            double currentRating = this.getArmorRating(current, slot);
            int bestInvIndex = -1;
            double bestRating = currentRating;

            for (int i = 0; i < this.inventory.getContainerSize(); i++) {
                ItemStack candidate = this.inventory.getItem(i);
                if (candidate.isEmpty()) continue;

                if (this.fitsArmorSlot(candidate, slot)) {
                    double rating = this.getArmorRating(candidate, slot);
                    if (rating > bestRating) {
                        bestRating = rating;
                        bestInvIndex = i;
                    }
                }
            }

            if (bestInvIndex != -1) {
                ItemStack newArmor = this.inventory.removeItem(bestInvIndex, 1);
                if (!current.isEmpty()) {
                    ItemStack leftover = this.inventory.addItem(current);
                    if (!leftover.isEmpty() && this.level() instanceof ServerLevel serverLevel) {
                        this.spawnAtLocation(serverLevel, leftover);
                    }
                }
                this.setItemSlot(slot, newArmor);

                Equippable eq = newArmor.get(DataComponents.EQUIPPABLE);
                if (eq != null && eq.equipSound() != null) {
                    this.playSound(eq.equipSound().value(), 1.0F, 1.0F);
                }
            }
        }
    }

    public boolean fitsArmorSlot(ItemStack stack, EquipmentSlot slot) {
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable != null && equippable.slot() == slot) {
            return true;
        }
        if (slot == EquipmentSlot.HEAD && stack.is(ItemTags.HEAD_ARMOR)) return true;
        if (slot == EquipmentSlot.CHEST && stack.is(ItemTags.CHEST_ARMOR)) return true;
        if (slot == EquipmentSlot.LEGS && stack.is(ItemTags.LEG_ARMOR)) return true;
        if (slot == EquipmentSlot.FEET && stack.is(ItemTags.FOOT_ARMOR)) return true;
        return false;
    }

    public double getArmorRating(ItemStack stack, EquipmentSlot slot) {
        if (stack.isEmpty()) return -1.0D;
        double[] rating = new double[]{0.0D};
        stack.forEachModifier(slot, (attrHolder, modifier) -> {
            if (attrHolder.equals(Attributes.ARMOR)) {
                rating[0] += modifier.amount();
            } else if (attrHolder.equals(Attributes.ARMOR_TOUGHNESS)) {
                rating[0] += modifier.amount() * 0.5D;
            }
        });
        return rating[0];
    }

    /* =========================================================================
       EQUIPAMIENTO DINÁMICO POR DEMANDA (Armas, Escudo, Azada, Puños)
       ========================================================================= */

    public void unequipWeapon() {
        ItemStack current = this.getMainHandItem();
        if (!current.isEmpty()) {
            ItemStack leftover = this.inventory.addItem(current);
            if (leftover.isEmpty()) {
                this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            } else {
                this.setItemSlot(EquipmentSlot.MAINHAND, leftover);
            }
        }
    }

    public void unequipShield() {
        ItemStack current = this.getOffhandItem();
        if (!current.isEmpty()) {
            ItemStack leftover = this.inventory.addItem(current);
            if (leftover.isEmpty()) {
                this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
            } else {
                this.setItemSlot(EquipmentSlot.OFFHAND, leftover);
            }
        }
    }

    public void unequipHands() {
        if (this.isUsingItem()) {
            this.stopUsingItem();
        }
        this.unequipWeapon();
        this.unequipShield();
    }

    public void equipBestWeapon() {
        if (this.isMagicUser()) return;
        ItemStack current = this.getMainHandItem();
        double currentRating = isWeapon(current) ? this.evaluateWeaponRating(current) : -1.0D;
        int bestInvIndex = -1;
        double bestRating = currentRating;

        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack candidate = this.inventory.getItem(i);
            if (candidate.isEmpty() || !isWeapon(candidate)) continue;
            double rating = this.evaluateWeaponRating(candidate);
            if (rating > bestRating) {
                bestRating = rating;
                bestInvIndex = i;
            }
        }

        if (bestInvIndex != -1) {
            ItemStack newWeapon = this.inventory.removeItem(bestInvIndex, 1);
            if (!current.isEmpty()) {
                ItemStack leftover = this.inventory.addItem(current);
                if (!leftover.isEmpty() && this.level() instanceof ServerLevel serverLevel) {
                    this.spawnAtLocation(serverLevel, leftover);
                }
            }
            this.setItemSlot(EquipmentSlot.MAINHAND, newWeapon);
            this.playSound(SoundEvents.ARMOR_EQUIP_GENERIC.value(), 1.0F, 1.0F);
        } else if (currentRating < 0.0D && !current.isEmpty()) {
            this.unequipWeapon();
        }
    }

    public void equipShieldFromInventory() {
        if (isShield(this.getOffhandItem())) {
            return;
        }
        if (!this.getOffhandItem().isEmpty()) {
            this.unequipShield();
        }
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack candidate = this.inventory.getItem(i);
            if (isShield(candidate)) {
                ItemStack shield = this.inventory.removeItem(i, 1);
                this.setItemSlot(EquipmentSlot.OFFHAND, shield);
                this.playSound(SoundEvents.ARMOR_EQUIP_GENERIC.value(), 1.0F, 1.0F);
                return;
            }
        }
    }

    public void equipHoeForHarvesting() {
        this.unequipShield();

        if (isHoe(this.getMainHandItem())) {
            return;
        }

        int bestIndex = -1;
        double bestRating = -1.0D;
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack candidate = this.inventory.getItem(i);
            if (isHoe(candidate)) {
                double rating = this.evaluateHoeRating(candidate);
                if (rating > bestRating) {
                    bestRating = rating;
                    bestIndex = i;
                }
            }
        }

        if (bestIndex != -1) {
            this.unequipWeapon();
            ItemStack hoe = this.inventory.removeItem(bestIndex, 1);
            this.setItemSlot(EquipmentSlot.MAINHAND, hoe);
            this.playSound(SoundEvents.ARMOR_EQUIP_GENERIC.value(), 1.0F, 1.0F);
        } else {
            // Sin azada en inventario: usar puños (desequipar mainhand)
            this.unequipWeapon();
        }
    }

    private double evaluateHoeRating(ItemStack stack) {
        if (stack.isEmpty()) return 0.0D;
        if (stack.is(Items.NETHERITE_HOE)) return 6.0D;
        if (stack.is(Items.DIAMOND_HOE)) return 5.0D;
        if (stack.is(Items.IRON_HOE)) return 4.0D;
        if (stack.is(Items.GOLDEN_HOE)) return 3.0D;
        if (stack.is(Items.STONE_HOE)) return 2.0D;
        if (stack.is(Items.WOODEN_HOE)) return 1.0D;
        return 1.0D;
    }

    public void equipForCombat(boolean allowShield) {
        if (this.isAnyQueen()) {
            this.unequipHands();
            return;
        }
        this.equipBestWeapon();
        if (this.getProfession() == KoboldProfession.GUARD) {
            this.equipShieldFromInventory();
        } else if (this.getProfession() == KoboldProfession.VIGILANT) {
            this.unequipShield();
        } else if (allowShield) {
            this.equipShieldFromInventory();
        } else {
            this.unequipShield();
        }
    }

    public static boolean isShield(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.getItem() instanceof ShieldItem || stack.is(Items.SHIELD);
    }

    public static boolean isHoe(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.is(ItemTags.HOES);
    }

    public boolean hasWeaponInInventory() {
        if (isWeapon(this.getMainHandItem())) return true;
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            if (isWeapon(this.inventory.getItem(i))) {
                return true;
            }
        }
        return false;
    }

    public boolean hasShieldInInventory() {
        if (isShield(this.getOffhandItem())) return true;
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            if (isShield(this.inventory.getItem(i))) {
                return true;
            }
        }
        return false;
    }

    public boolean hasHoeInInventory() {
        if (isHoe(this.getMainHandItem())) return true;
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            if (isHoe(this.inventory.getItem(i))) {
                return true;
            }
        }
        return false;
    }

    public static boolean isFoodAnimal(LivingEntity entity) {
        return entity instanceof Cow || entity instanceof Pig || entity instanceof Chicken
                || entity instanceof Sheep || entity instanceof Rabbit;
    }

    public boolean isHunting() {
        LivingEntity target = this.getTarget();
        return target != null && target.isAlive() && target instanceof Animal && isFoodAnimal(target) && this.getLastHurtByMob() != target;
    }

    public boolean isThreatened() {
        if (this.isStaringAtBaculo || this.isApproachingBaculo) {
            return false;
        }
        if (this.isInPowerStruggle()) {
            return true;
        }
        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive()) {
            if (target instanceof AbstractTribalEntity otherKobold) {
                if (this.canFightKobold(otherKobold)) {
                    return true;
                }
            } else if (!this.isHunting()) {
                return true;
            }
        }
        LivingEntity hurtBy = this.getLastHurtByMob();
        if (hurtBy != null && hurtBy.isAlive()) {
            if (hurtBy instanceof AbstractTribalEntity otherKobold) {
                if (this.canFightKobold(otherKobold)) {
                    return true;
                }
            } else if (this.tickCount - this.getLastHurtByMobTimestamp() < 100) {
                return true;
            }
        }
        return false;
    }

    public double evaluateWeaponRating(ItemStack stack) {
        if (stack.isEmpty()) return 0.0D;

        KoboldProfession prof = this.getProfession();

        // 1. Guardias: SOLO armas cuerpo a cuerpo
        if (prof == KoboldProfession.GUARD && isRangedWeapon(stack)) {
            return -100.0D;
        }

        // 2. Vigilantes: SOLO armas a distancia que requieran munición
        if (prof == KoboldProfession.VIGILANT) {
            if (!isRangedWeapon(stack)) {
                return -100.0D;
            }
            if (this.hasArrows()) {
                return 10.0D;
            }
            return this.getTarget() != null ? -1.0D : 3.0D;
        }

        // 3. Cazadoras: Especializadas en hachas o armas a distancia
        if (prof == KoboldProfession.HUNTER) {
            if (isAxe(stack)) {
                return 10.0D + this.getWeaponDamage(stack);
            }
            if (isRangedWeapon(stack)) {
                if (this.hasArrows()) {
                    return 9.5D;
                }
                return this.getTarget() != null ? -1.0D : 2.0D;
            }
            // Otras armas tienen menor prioridad
            return 1.0D + this.getWeaponDamage(stack);
        }

        // Armas a distancia: solo son viables si tiene flechas en el inventario
        if (isRangedWeapon(stack)) {
            if (this.hasArrows()) {
                return 8.5D;
            }
            // En combate activo sin flechas: -1.0D para cambiar a arma melee o cuerpo a cuerpo
            // Fuera de combate sin flechas: 2.0D para no desequiparlo a mano vacía y permitir que sus compañeras detecten que necesita flechas
            return this.getTarget() != null ? -1.0D : 2.0D;
        }
        // Mazo: ataque aéreo devastador
        if (isMace(stack)) {
            return 8.0D + this.getWeaponDamage(stack);
        }
        // Lanza / Arma cinética / Tridente: ventaja de rango y retroceso
        if (isSpear(stack)) {
            return 7.5D + this.getWeaponDamage(stack);
        }
        // Espadas y Hachas
        if (stack.is(ItemTags.SWORDS) || stack.is(ItemTags.AXES)) {
            return 5.0D + this.getWeaponDamage(stack);
        }
        // Otras herramientas o ítems con daño
        double dmg = this.getWeaponDamage(stack);
        if (dmg > 1.0D) {
            return dmg;
        }
        return 0.0D;
    }

    public static boolean isSword(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.is(ItemTags.SWORDS);
    }

    public static boolean isAxe(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.is(ItemTags.AXES);
    }

    public static boolean isMeleeWeapon(ItemStack stack) {
        if (stack.isEmpty()) return true;
        return !isRangedWeapon(stack) && (isWeapon(stack) || stack.is(ItemTags.HOES));
    }

    public static ItemStack getCookedResult(ItemStack raw) {
        if (raw.is(Items.BEEF)) return new ItemStack(Items.COOKED_BEEF);
        if (raw.is(Items.PORKCHOP)) return new ItemStack(Items.COOKED_PORKCHOP);
        if (raw.is(Items.CHICKEN)) return new ItemStack(Items.COOKED_CHICKEN);
        if (raw.is(Items.MUTTON)) return new ItemStack(Items.COOKED_MUTTON);
        if (raw.is(Items.RABBIT)) return new ItemStack(Items.COOKED_RABBIT);
        if (raw.is(Items.COD)) return new ItemStack(Items.COOKED_COD);
        if (raw.is(Items.SALMON)) return new ItemStack(Items.COOKED_SALMON);
        return ItemStack.EMPTY;
    }

    public static boolean isRawMeat(ItemStack stack) {
        return !getCookedResult(stack).isEmpty();
    }

    public boolean hasRawMeatInInventory() {
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            if (isRawMeat(this.inventory.getItem(i))) {
                return true;
            }
        }
        return false;
    }

    public boolean isInventoryFullOfMeat() {
        int rawMeatCount = 0;
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.inventory.getItem(i);
            if (isRawMeat(stack)) {
                rawMeatCount += stack.getCount();
            }
        }
        return rawMeatCount >= 16;
    }

    public boolean cookOneRawMeat() {
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.inventory.getItem(i);
            if (!stack.isEmpty() && isRawMeat(stack)) {
                ItemStack cooked = getCookedResult(stack);
                stack.shrink(1);
                ItemStack remaining = this.inventory.addItem(cooked);
                if (!remaining.isEmpty() && this.level() instanceof ServerLevel serverLevel) {
                    this.spawnAtLocation(serverLevel, remaining);
                }
                return true;
            }
        }
        return false;
    }

    public static boolean isWeapon(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return isRangedWeapon(stack)
                || isMace(stack)
                || isSpear(stack)
                || stack.is(ItemTags.SWORDS)
                || stack.is(ItemTags.AXES);
    }

    public static boolean isRangedWeapon(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem;
    }

    public static boolean isMace(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.getItem() instanceof MaceItem || stack.is(ItemTags.MACE_ENCHANTABLE);
    }

    public static boolean isSpear(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.has(DataComponents.KINETIC_WEAPON) || stack.is(ItemTags.SPEARS) || stack.getItem() instanceof TridentItem) {
            return true;
        }
        String id = stack.getItem().getDescriptionId().toLowerCase();
        return id.contains("spear") || id.contains("trident") || id.contains("lance") || id.contains("lanza");
    }

    public double getWeaponDamage(ItemStack stack) {
        if (stack.isEmpty()) return 0.0D;
        double[] dmg = new double[]{0.0D};
        stack.forEachModifier(EquipmentSlot.MAINHAND, (attrHolder, modifier) -> {
            if (attrHolder.equals(Attributes.ATTACK_DAMAGE)) {
                dmg[0] += modifier.amount();
            }
        });
        return dmg[0];
    }

    public boolean hasArrows() {
        if (this.getProfession() == KoboldProfession.VIGILANT) return true;
        return this.hasPhysicalArrows();
    }

    public boolean hasPhysicalArrows() {
        if (this.getOffhandItem().is(ItemTags.ARROWS)) return true;
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.inventory.getItem(i);
            if (stack.is(ItemTags.ARROWS)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasRangedWeapon() {
        if (isRangedWeapon(this.getMainHandItem())) return true;
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            if (isRangedWeapon(this.inventory.getItem(i))) {
                return true;
            }
        }
        return false;
    }

    public ItemStack consumeOneArrow() {
        if (this.getOffhandItem().is(ItemTags.ARROWS)) {
            ItemStack off = this.getOffhandItem();
            ItemStack res = off.split(1);
            if (off.isEmpty()) {
                this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
            }
            return res;
        }
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.inventory.getItem(i);
            if (stack.is(ItemTags.ARROWS)) {
                return this.inventory.removeItem(i, 1);
            }
        }
        if (this.getProfession() == KoboldProfession.VIGILANT) {
            return new ItemStack(Items.ARROW);
        }
        return ItemStack.EMPTY;
    }

    /* =========================================================================
       INVENTARIO Y RECOLECCIÓN DE ITEMS SEGÚN PROFESIÓN
       ========================================================================= */

    public SimpleContainer getInventory() {
        return this.inventory;
    }

    public static boolean isCookedMeat(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.is(Items.COOKED_BEEF)
                || stack.is(Items.COOKED_PORKCHOP)
                || stack.is(Items.COOKED_CHICKEN)
                || stack.is(Items.COOKED_MUTTON)
                || stack.is(Items.COOKED_RABBIT)
                || stack.is(Items.COOKED_COD)
                || stack.is(Items.COOKED_SALMON);
    }

    public static boolean isAnimalProduct(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.is(ItemTags.WOOL)
                || stack.is(Items.LEATHER)
                || stack.is(Items.FEATHER)
                || stack.is(Items.EGG)
                || stack.is(Items.RABBIT_HIDE)
                || stack.is(Items.RABBIT_FOOT);
    }

    public static boolean isHarvestCropItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.is(Items.WHEAT)
                || stack.is(Items.CARROT)
                || stack.is(Items.POTATO)
                || stack.is(Items.BEETROOT)
                || stack.is(Items.SWEET_BERRIES)
                || stack.is(Items.GLOW_BERRIES)
                || stack.is(Items.MELON_SLICE)
                || stack.is(Items.MELON)
                || stack.is(Items.PUMPKIN)
                || stack.is(Items.COCOA_BEANS)
                || stack.is(Items.NETHER_WART);
    }

    public static boolean isSeedItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return isPureSeed(stack)
                || isPlantableSeed(stack)
                || stack.is(Items.TORCHFLOWER_SEEDS)
                || stack.is(Items.PITCHER_POD);
    }

    public static boolean isArrowItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.is(ItemTags.ARROWS);
    }

    public static boolean isReadyToEatFood(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.has(DataComponents.FOOD)
                && !isRawMeat(stack)
                && !stack.is(Items.ROTTEN_FLESH)
                && !stack.is(Items.SPIDER_EYE)
                && !stack.is(Items.POISONOUS_POTATO)
                && !stack.is(Items.PUFFERFISH);
    }

    public static boolean isJunkItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        // 1. Bloques de terreno (tierra, césped, piedra, grava, arena, etc.) y flores de bloques
        if (stack.getItem() instanceof BlockItem bi) {
            Block b = bi.getBlock();
            if (isTerrainBlock(b) || b.defaultBlockState().is(BlockTags.FLOWERS)) {
                return true;
            }
        }
        // 2. Vegetación/flores/hojas/retoños sueltos
        if (stack.is(ItemTags.SAPLINGS) || stack.is(ItemTags.LEAVES)
                || stack.is(Items.SEAGRASS) || stack.is(Items.KELP) || stack.is(Items.SHORT_GRASS)
                || stack.is(Items.TALL_GRASS) || stack.is(Items.FERN) || stack.is(Items.LARGE_FERN)
                || stack.is(Items.DEAD_BUSH) || stack.is(Items.VINE)) {
            return true;
        }
        // 3. Comida venenosa o dañina que nadie debe consumir
        if (stack.is(Items.POISONOUS_POTATO) || stack.is(Items.PUFFERFISH)) {
            return true;
        }
        return false;
    }

    public static boolean isEnemyLoot(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.is(Items.ROTTEN_FLESH)
                || stack.is(Items.BONE)
                || stack.is(Items.GUNPOWDER)
                || stack.is(Items.SPIDER_EYE)
                || stack.is(Items.STRING)
                || stack.is(Items.SLIME_BALL)
                || stack.is(Items.MAGMA_CREAM)
                || stack.is(Items.ENDER_PEARL)
                || stack.is(Items.BLAZE_ROD)
                || stack.is(Items.GHAST_TEAR)
                || stack.is(Items.PHANTOM_MEMBRANE)
                || stack.is(Items.SHULKER_SHELL)
                || stack.is(Items.TOTEM_OF_UNDYING)
                || stack.is(Items.ARROW)
                || stack.is(Items.SPECTRAL_ARROW)
                || stack.is(Items.TIPPED_ARROW)
                || stack.is(Items.GLOWSTONE_DUST);
    }

    public boolean isArmorUpgrade(ItemStack stack) {
        if (stack.isEmpty()) return false;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            if (this.fitsArmorSlot(stack, slot)) {
                double candidateRating = this.getArmorRating(stack, slot);
                double currentRating = this.getArmorRating(this.getItemBySlot(slot), slot);
                if (candidateRating > currentRating) {
                    return true;
                }
            }
        }
        return false;
    }

    public int countRawMeatInInventory() {
        int count = 0;
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.inventory.getItem(i);
            if (isRawMeat(stack)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public int countReadyFoodInInventory() {
        int count = 0;
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.inventory.getItem(i);
            if (isReadyToEatFood(stack)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public static boolean isWorkTool(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.is(ItemTags.PICKAXES) || stack.is(ItemTags.AXES) || stack.is(ItemTags.SHOVELS) || stack.is(ItemTags.HOES);
    }

    public boolean equipBestWorkTool() {
        if (isWorkTool(this.getMainHandItem())) {
            return true;
        }
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.inventory.getItem(i);
            if (!stack.isEmpty() && isWorkTool(stack)) {
                ItemStack tool = this.inventory.removeItem(i, 1);
                ItemStack current = this.getMainHandItem();
                if (!current.isEmpty()) {
                    ItemStack leftover = this.inventory.addItem(current);
                    if (!leftover.isEmpty() && this.level() instanceof ServerLevel sl) {
                        this.spawnAtLocation(sl, leftover);
                    }
                }
                this.setItemSlot(EquipmentSlot.MAINHAND, tool);
                this.playSound(SoundEvents.ARMOR_EQUIP_GENERIC.value(), 1.0F, 1.0F);
                return true;
            }
        }
        return false;
    }

    public void autoEquipForWorkOrDefense() {
        if (this.level().isClientSide() || this.isSleeping()) return;
        if (this.isAnyQueen()) {
            if (!this.isInQueenDuel() && !this.isInPlayerDuel() && !this.isThreatened()) {
                if (!this.getMainHandItem().isEmpty() || !this.getOffhandItem().isEmpty()) {
                    this.unequipHands();
                }
            }
            return;
        }

        KoboldProfession prof = this.getProfession();
        if (prof == KoboldProfession.GUARD) {
            if (!isWeapon(this.getMainHandItem()) || isRangedWeapon(this.getMainHandItem())) {
                this.equipBestWeapon();
            }
            if (!isShield(this.getOffhandItem()) && this.hasShieldInInventory()) {
                this.equipShieldFromInventory();
            }
        } else if (prof == KoboldProfession.VIGILANT) {
            if (!isRangedWeapon(this.getMainHandItem())) {
                this.equipBestWeapon();
            }
            if (!this.getOffhandItem().isEmpty() && isShield(this.getOffhandItem())) {
                this.unequipShield();
            }
        } else if (prof == KoboldProfession.HUNTER) {
            if (!isWeapon(this.getMainHandItem())) {
                this.equipBestWeapon();
            }
            if (!this.getOffhandItem().isEmpty() && isShield(this.getOffhandItem())) {
                this.unequipShield();
            }
        } else if (prof == KoboldProfession.FARMER) {
            if (this.hasHoeInInventory()) {
                if (!isHoe(this.getMainHandItem())) {
                    this.equipHoeForHarvesting();
                }
            } else if (!isWeapon(this.getMainHandItem())) {
                this.equipBestWeapon();
            }
            if (!this.getOffhandItem().isEmpty()) {
                this.unequipShield();
            }
        } else {
            // Súbditas comunes / constructoras: equipan su herramienta de trabajo (pico, hacha, pala) o su mejor arma
            if (!this.equipBestWorkTool()) {
                if (!isWeapon(this.getMainHandItem())) {
                    this.equipBestWeapon();
                }
            }
            if (!isRangedWeapon(this.getMainHandItem()) && !isShield(this.getOffhandItem()) && this.hasShieldInInventory()) {
                this.equipShieldFromInventory();
            }
        }
    }

    public void autoEquipWeaponsAndShield() {
        this.autoEquipForWorkOrDefense();
    }

    public void collectNearbyHuntDrops(ServerLevel level, Vec3 pos) {
        if (this.getProfession() != KoboldProfession.HUNTER) return;
        List<ItemEntity> drops = level.getEntitiesOfClass(
                ItemEntity.class,
                new AABB(pos.x - 3.5D, pos.y - 2.0D, pos.z - 3.5D, pos.x + 3.5D, pos.y + 2.0D, pos.z + 3.5D),
                item -> item.isAlive() && (isRawMeat(item.getItem()) || isAnimalProduct(item.getItem()) || isCookedMeat(item.getItem()))
        );
        for (ItemEntity drop : drops) {
            this.pickUpItem(level, drop);
        }
    }

    public void collectNearbyEnemyDrops(ServerLevel level, Vec3 pos) {
        if (!this.isDefender()) return;
        List<ItemEntity> drops = level.getEntitiesOfClass(
                ItemEntity.class,
                new AABB(pos.x - 4.5D, pos.y - 2.5D, pos.z - 4.5D, pos.x + 4.5D, pos.y + 2.5D, pos.z + 4.5D),
                item -> item.isAlive() && !item.hasPickUpDelay() && this.wantsToPickUpItem(item.getItem())
        );
        for (ItemEntity drop : drops) {
            this.pickUpItem(level, drop);
        }
    }

    public void onSlainTargetDrops(ServerLevel serverLevel, LivingEntity target) {
        if (target == null) return;
        if (target instanceof Animal && (this.getProfession() == KoboldProfession.HUNTER || this.isAnyQueen())) {
            this.collectNearbyHuntDrops(serverLevel, target.position());
        } else if (this.isDefender() && (target instanceof Enemy || target instanceof Monster)) {
            this.collectNearbyEnemyDrops(serverLevel, target.position());
        }
    }

    public boolean wantsToPickUpItem(ItemStack stack) {
        if (stack.isEmpty()) return false;

        // 1. Descartar basura absoluta (bloques de terreno, césped, flores, comida venenosa, etc.)
        if (isJunkItem(stack)) {
            return false;
        }

        // 2. Loot de enemigos caídos: Exclusivo para Guardianas y Vigilantes
        if (isEnemyLoot(stack)) {
            return this.isDefender();
        }

        // 3. Mejoras de armadura: Cualquier Kobold siempre querrá mejorar su equipo protector
        if (this.isArmorUpgrade(stack)) {
            return true;
        }

        // 4. Comida lista para comer si tiene hambre o pocas raciones de reserva (< 4)
        if (isReadyToEatFood(stack) && (this.needsFood() || this.countReadyFoodInInventory() < 4)) {
            return true;
        }

        // 4. Reina (o Reina Sobreviviente): Comportamiento de líder y hechicera de tribu
        if (this.isAnyQueen()) {
            // Minerales valiosos (diamantes, esmeraldas, oro, hierro)
            if (isMineralOrResource(stack)) {
                return true;
            }
            // Si colabora en tareas agrícolas, recoger cosechas y semillas para replantar o almacenar
            if (isHarvestCropItem(stack) || isSeedItem(stack)) {
                return true;
            }
            // Si ayuda en la cacería de alimentos, recoger carne
            if (isRawMeat(stack) || isCookedMeat(stack)) {
                return this.countRawMeatInInventory() < 16;
            }
            // La reina es una hechicera pura: no recoge armas ni escudos convencionales del suelo
            return false;
        }

        KoboldProfession prof = this.getProfession();

        // 5. Filtrado estricto por profesión para súbditas
        switch (prof) {
            case FARMER -> {
                // Herramientas agrícolas: Azada (si no tiene o es mejor)
                if (isHoe(stack)) {
                    return !this.hasHoeInInventory() || this.evaluateHoeRating(stack) > this.evaluateHoeRating(this.getMainHandItem());
                }
                // Cosechas y cultivos
                if (isHarvestCropItem(stack)) {
                    return true;
                }
                // Semillas (hasta un tope prudente de 64 para no saturar la mochila)
                if (isSeedItem(stack)) {
                    return this.getSeedCount() < 64;
                }
                // Polvo de hueso para fertilizar
                if (stack.is(Items.BONE_MEAL)) {
                    return true;
                }
                // Rechaza armas, escudos, flechas, carne cruda y productos de caza
                return false;
            }

            case HUNTER -> {
                // Carnes crudas (para cocinar en fogatas) y carnes cocinadas
                if (isRawMeat(stack) || isCookedMeat(stack)) {
                    return this.countRawMeatInInventory() < 32;
                }
                // Subproductos de animales cazados (cuero, plumas, lana, huesos, hilo, etc.)
                if (isAnimalProduct(stack)) {
                    return true;
                }
                // Armas de cazadora: Hachas y Armas a distancia (Arcos / Ballestas)
                if (isAxe(stack) || isRangedWeapon(stack)) {
                    if (this.getMainHandItem().isEmpty()) return true;
                    return this.evaluateWeaponRating(stack) > this.evaluateWeaponRating(this.getMainHandItem());
                }
                // Flechas para sus armas a distancia (hasta 64)
                if (isArrowItem(stack)) {
                    return this.getInventoryArrowCount() < 64;
                }
                // Rechaza cosechas, semillas, azadas, espadas, mazas, escudos
                return false;
            }

            case GUARD -> {
                // Armas cuerpo a cuerpo exclusivamente (Espadas, Mazas, Lanzas/Tridentes)
                if (isWeapon(stack) && !isRangedWeapon(stack) && !isHoe(stack)) {
                    if (this.getMainHandItem().isEmpty()) return true;
                    return this.evaluateWeaponRating(stack) > this.evaluateWeaponRating(this.getMainHandItem());
                }
                // Escudos defensivos
                if (isShield(stack)) {
                    return !isShield(this.getOffhandItem()) && !this.hasShieldInInventory();
                }
                // Rechaza arcos, ballestas, flechas, azadas, cosechas, semillas, carne cruda, productos de animales
                return false;
            }

            case VIGILANT -> {
                // Armas a distancia exclusivamente (Arcos, Ballestas)
                if (isRangedWeapon(stack)) {
                    if (this.getMainHandItem().isEmpty()) return true;
                    return this.evaluateWeaponRating(stack) > this.evaluateWeaponRating(this.getMainHandItem());
                }
                // Flechas para combate a distancia (hasta 128 / 2 stacks)
                if (isArrowItem(stack)) {
                    return this.getInventoryArrowCount() < 128;
                }
                // Rechaza armas cuerpo a cuerpo, escudos, azadas, cosechas, semillas, carne cruda, productos de animales
                return false;
            }

            default -> { // Súbdita común (NONE)
                // Arma básica si está desarmada
                if (isWeapon(stack) && !isHoe(stack) && this.getMainHandItem().isEmpty()) {
                    return true;
                }
                // Escudo básico si no tiene
                if (isShield(stack) && !isShield(this.getOffhandItem()) && !this.hasShieldInInventory()) {
                    return true;
                }
                // Minerales valiosos
                if (isMineralOrResource(stack)) {
                    return true;
                }
                // Rechaza cosechas, semillas, azadas, carne cruda, productos de caza
                return false;
            }
        }
    }

    @Override
    public boolean wantsToPickUp(ServerLevel level, ItemStack stack) {
        return this.wantsToPickUpItem(stack) && this.inventory.canAddItem(stack);
    }

    @Override
    public void pickUpItem(ServerLevel level, ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getItem();
        if (this.wantsToPickUpItem(stack) && this.inventory.canAddItem(stack)) {
            ItemStack remaining = this.inventory.addItem(stack);
            this.playSound(SoundEvents.ITEM_PICKUP, 0.25F, 1.2F);
            this.take(itemEntity, stack.getCount() - remaining.getCount());

            if (remaining.isEmpty()) {
                itemEntity.discard();
            } else {
                itemEntity.setItem(remaining);
            }

            // Si recogió trigo y es granjera, intentar hornear pan de inmediato
            if (this.getProfession() == KoboldProfession.FARMER) {
                this.tryCraftBread();
            }

            // Si recogió comida y tiene hambre, comerla de inmediato
            if (this.needsFood()) {
                this.tryEatFromInventory();
            }

            // Auto-equipar armadura (solo slots de armadura) si recogió una mejor
            this.autoEquipArmor();

            // Auto-equipar arma o escudo si recogió uno mejor
            this.autoEquipWeaponsAndShield();
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
        // Soltar todo el inventario
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.inventory.getItem(i);
            if (!stack.isEmpty()) {
                this.spawnAtLocation(level, stack);
                this.inventory.setItem(i, ItemStack.EMPTY);
            }
        }
        // Soltar equipo portado (armaduras y armas)
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack equipped = this.getItemBySlot(slot);
            if (!equipped.isEmpty()) {
                this.spawnAtLocation(level, equipped);
                this.setItemSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        // 0. Si es Reina Derrotada a 1 HP tras duelo contra jugador gobernante:
        if (this.isDownedQueen()) {
            if (isHoldingBaculo(player)) {
                if (!this.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
                    if (this.isMarkedForExecution()) {
                        serverPlayer.sendSystemMessage(Component.literal("§cLa Reina ya ha sido sentenciada a muerte. Asesta el golpe de gracia para concluir.")
                                .withStyle(ChatFormatting.RED));
                    } else {
                        PacketDistributor.sendToPlayer(serverPlayer, new OpenQueenDialoguePayload(
                                this.getId(),
                                this.getQueenTitle(),
                                this.getTribeName(),
                                false,
                                false,
                                true,
                                true
                        ));
                    }
                }
                return InteractionResult.SUCCESS;
            } else {
                if (!this.level().isClientSide()) {
                    player.sendSystemMessage(Component.literal("§eLa Reina está inmóvil e indefensa. Usa el Báculo de Mando con ella para decidir su destino.")
                            .withStyle(ChatFormatting.YELLOW));
                }
                return InteractionResult.SUCCESS;
            }
        }

        // 0.1. Si está durmiendo, clic derecho la despierta
        if (this.isSleeping()) {
            if (!this.level().isClientSide()) {
                this.stopSleeping();
                this.sleepCooldown = 100;
                if (this.doesAssignedArmorStandHaveArmor()) {
                    this.needsArmorRetrieval = true;
                }
            }
            return InteractionResult.SUCCESS;
        }

        // 1. Clic derecho sobre la Reina (o Reina Sobreviviente) portando el Báculo: Conversación mágica / ritual
        if (this.isAnyQueen() && isHoldingBaculo(player)) {
            if (!this.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
                this.interactWithBaculo(serverPlayer);
            }
            return InteractionResult.SUCCESS;
        }

        // 2. Si está en duelo activo, no se puede interactuar
        if (this.isInPlayerDuel() || this.isInQueenDuel()) {
            return InteractionResult.FAIL;
        }

        // 3. Si es una súbdita rebelde, rechaza cualquier intento de interacción
        if (this.isRebel()) {
            if (!this.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
                this.speakRebelToPlayer(serverPlayer);
            }
            return InteractionResult.SUCCESS;
        }

        // 4. Si el jugador NO entiende el idioma o esta Kobold NO es aliada suya (no pertenece a la tribu del jugador):
        // No se puede abrir su inventario, ni alimentarla, ni darle equipo. Solo responden con diálogo.
        boolean understands = this.understandsLanguage(player);
        boolean isAlly = this.isOwnedBy(player);

        if (!understands || !isAlly) {
            // Si es una reina con la ruta pacifista activa y el jugador entiende el idioma:
            // Abrir directamente la interfaz de tradeo para entregar ofrendas sin necesidad de usar el báculo
            if (this.isAnyQueen() && this.isPacifistRouteChosen() && understands && !isHoldingBaculo(player)) {
                if (!this.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
                    this.updatePacifistOfferForPlayer(serverPlayer);
                    this.setTradingPlayer(serverPlayer);
                    this.openTradingScreen(serverPlayer, Component.literal("Desarrollo: " + this.getTribeName()), 1);
                }
                return InteractionResult.SUCCESS;
            }

            if (!this.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
                this.speakToPlayer(serverPlayer);
            }
            return InteractionResult.SUCCESS;
        }

        // 5. A partir de aquí, el jugador ENTIENDE el idioma Y es DUEÑO/ALIADO de esta Kobold Girl:
        ItemStack itemInHand = player.getItemInHand(hand);

        // 5.1. Shift + Clic derecho: Abrir la nueva interfaz de inventario completa con armaduras, armas y vista 3D
        if (player.isShiftKeyDown()) {
            if (!this.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(
                        new SimpleMenuProvider(
                                (id, playerInv, p) -> new com.newcolleague.mod.menu.KoboldGirlMenu(id, playerInv, this),
                                Component.translatable("entity.newcolleaguemod.kobold_girl.inventory")
                        ),
                        buf -> buf.writeInt(this.getId())
                );
            }
            return InteractionResult.SUCCESS;
        }

        // 5.2. Asignación de Nombre personalizado con Etiqueta (Name Tag)
        if (itemInHand.is(Items.NAME_TAG) && itemInHand.has(DataComponents.CUSTOM_NAME)) {
            if (!this.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
                Component customNameComp = itemInHand.get(DataComponents.CUSTOM_NAME);
                if (customNameComp != null) {
                    String rawName = customNameComp.getString();
                    this.setCustomGivenName(rawName);
                    if (!player.getAbilities().instabuild) {
                        itemInHand.shrink(1);
                    }
                    this.playSound(SoundEvents.VILLAGER_YES, 1.0F, 1.2F);
                    if (this.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.2D, this.getZ(), 8, 0.3D, 0.3D, 0.3D, 0.05D);
                    }
                    serverPlayer.sendSystemMessage(Component.literal("§aHas nombrado a esta Kobold como: §e" + rawName));
                }
            }
            return InteractionResult.SUCCESS;
        }

        // 5.3. Mejora / Ascenso de Profesión a Rango Veterana (Tier 2 ⭐) o donación a la Reina
        if (itemInHand.is(Items.EMERALD) || itemInHand.is(Items.DIAMOND)) {
            if (this.isAnyQueen()) {
                if (!this.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
                    int boost = itemInHand.is(Items.DIAMOND) ? 50 : 15;
                    this.addProsperity(boost);
                    if (!player.getAbilities().instabuild) {
                        itemInHand.shrink(1);
                    }
                    this.playSound(SoundEvents.PLAYER_LEVELUP, 1.0F, 1.2F);
                    if (this.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.2D, this.getZ(), 16, 0.4D, 0.5D, 0.4D, 0.1D);
                    }
                    Component speaker = Component.literal("<" + this.getQueenTitle() + "> ").withStyle(ChatFormatting.GOLD);
                    serverPlayer.sendSystemMessage(speaker.copy().append(Component.literal("Agradezco tu generosa ofrenda (+§e" + boost + "§a prosperidad para la aldea). Prosperidad actual: §e" + this.getProsperity() + "§a pts (§b" + this.getVillageTier().getDisplayName() + "§a).")));
                }
                return InteractionResult.SUCCESS;
            } else if (this.getProfession() != KoboldProfession.NONE) {
                if (!this.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
                    if (this.getProfessionTier() < 2) {
                        this.setProfessionTier(2);
                        if (!player.getAbilities().instabuild) {
                            itemInHand.shrink(1);
                        }
                        this.playSound(SoundEvents.PLAYER_LEVELUP, 1.0F, 1.2F);
                        if (this.level() instanceof ServerLevel sl) {
                            sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, this.getX(), this.getY() + 1.0D, this.getZ(), 16, 0.4D, 0.5D, 0.4D, 0.1D);
                        }
                        String name = this.getCustomGivenName().isEmpty() ? this.getProfession().getDisplayName() : this.getCustomGivenName();
                        if (this.getProfession() == KoboldProfession.GUARD || this.getProfession() == KoboldProfession.VIGILANT) {
                            this.equipEliteGuardEquipment(this.getVillageTier() == VillageTier.DIAMOND_FORTRESS);
                            serverPlayer.sendSystemMessage(Component.literal("§6¡" + name + " ha ascendido a §bGuardia de Élite (Tier 2 ⭐)§6! Se ha equipado con armadura reforzada con piezas de diamante. 💎"));
                        } else {
                            serverPlayer.sendSystemMessage(Component.literal("§6¡" + name + " ha ascendido a Rango Veterana (Tier 2 ⭐)! Ahora desempeñará sus labores con maestría y eficiencia mejorada."));
                        }
                    } else {
                        serverPlayer.sendSystemMessage(Component.literal("§eEsta súbdita ya ostenta el máximo rango de veteranía."));
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }

        // 5.4. Reasignación dinámica de profesiones a súbditas entregando herramientas de trabajo
        if (!this.isAnyQueen()) {
            if (isHoe(itemInHand) && this.getProfession() != KoboldProfession.FARMER) {
                if (!this.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
                    this.setProfession(KoboldProfession.FARMER);
                    ItemStack toStore = itemInHand.copy();
                    toStore.setCount(1);
                    this.inventory.addItem(toStore);
                    if (!player.getAbilities().instabuild) {
                        itemInHand.shrink(1);
                    }
                    this.playSound(SoundEvents.VILLAGER_WORK_FARMER, 1.0F, 1.0F);
                    if (this.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.2D, this.getZ(), 8, 0.3D, 0.3D, 0.3D, 0.05D);
                    }
                    String name = this.getCustomGivenName().isEmpty() ? "Esta súbdita" : this.getCustomGivenName();
                    serverPlayer.sendSystemMessage(Component.literal("§a" + name + " ahora es §eGranjera§a de la tribu."));
                }
                return InteractionResult.SUCCESS;
            } else if (isRangedWeapon(itemInHand) && this.getProfession() != KoboldProfession.HUNTER) {
                if (!this.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
                    this.setProfession(KoboldProfession.HUNTER);
                    ItemStack toStore = itemInHand.copy();
                    toStore.setCount(1);
                    this.inventory.addItem(toStore);
                    if (!player.getAbilities().instabuild) {
                        itemInHand.shrink(1);
                    }
                    this.playSound(SoundEvents.ARMOR_EQUIP_GENERIC.value(), 1.0F, 1.0F);
                    if (this.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.2D, this.getZ(), 8, 0.3D, 0.3D, 0.3D, 0.05D);
                    }
                    String name = this.getCustomGivenName().isEmpty() ? "Esta súbdita" : this.getCustomGivenName();
                    serverPlayer.sendSystemMessage(Component.literal("§a" + name + " ahora es §eCazadora§a de la tribu."));
                }
                return InteractionResult.SUCCESS;
            } else if ((itemInHand.is(ItemTags.SWORDS) || itemInHand.is(Items.MACE) || itemInHand.is(Items.TRIDENT) || isShield(itemInHand)) && this.getProfession() != KoboldProfession.GUARD) {
                if (!this.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
                    this.setProfession(KoboldProfession.GUARD);
                    ItemStack toStore = itemInHand.copy();
                    toStore.setCount(1);
                    this.inventory.addItem(toStore);
                    if (!player.getAbilities().instabuild) {
                        itemInHand.shrink(1);
                    }
                    this.playSound(SoundEvents.ARMOR_EQUIP_GENERIC.value(), 1.0F, 1.0F);
                    if (this.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.2D, this.getZ(), 8, 0.3D, 0.3D, 0.3D, 0.05D);
                    }
                    String name = this.getCustomGivenName().isEmpty() ? "Esta súbdita" : this.getCustomGivenName();
                    serverPlayer.sendSystemMessage(Component.literal("§a" + name + " ahora es §eGuardia§a defensora de la tribu."));
                }
                return InteractionResult.SUCCESS;
            } else if ((itemInHand.is(Items.TORCH) || itemInHand.is(Items.SPYGLASS)) && this.getProfession() != KoboldProfession.VIGILANT) {
                if (!this.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
                    this.setProfession(KoboldProfession.VIGILANT);
                    ItemStack toStore = itemInHand.copy();
                    toStore.setCount(1);
                    this.inventory.addItem(toStore);
                    if (!player.getAbilities().instabuild) {
                        itemInHand.shrink(1);
                    }
                    this.playSound(SoundEvents.ARMOR_EQUIP_GENERIC.value(), 1.0F, 1.0F);
                    if (this.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.2D, this.getZ(), 8, 0.3D, 0.3D, 0.3D, 0.05D);
                    }
                    String name = this.getCustomGivenName().isEmpty() ? "Esta súbdita" : this.getCustomGivenName();
                    serverPlayer.sendSystemMessage(Component.literal("§a" + name + " ahora es §eVigilante§a de la tribu."));
                }
                return InteractionResult.SUCCESS;
            } else if (itemInHand.is(ItemTags.SHOVELS) && this.getProfession() != KoboldProfession.BUILDER) {
                if (!this.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
                    this.setProfession(KoboldProfession.BUILDER);
                    ItemStack toStore = itemInHand.copy();
                    toStore.setCount(1);
                    this.inventory.addItem(toStore);
                    if (!player.getAbilities().instabuild) {
                        itemInHand.shrink(1);
                    }
                    this.playSound(SoundEvents.STONE_PLACE, 1.0F, 1.0F);
                    if (this.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.2D, this.getZ(), 8, 0.3D, 0.3D, 0.3D, 0.05D);
                    }
                    String name = this.getCustomGivenName().isEmpty() ? "Esta súbdita" : this.getCustomGivenName();
                    serverPlayer.sendSystemMessage(Component.literal("§a" + name + " ahora es §eConstructora§a de la tribu. Aplanará el terreno y construirá estructuras."));
                }
                return InteractionResult.SUCCESS;
            } else if (itemInHand.is(ItemTags.PICKAXES) && this.getProfession() != KoboldProfession.MINER) {
                if (!this.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
                    this.setProfession(KoboldProfession.MINER);
                    ItemStack toStore = itemInHand.copy();
                    toStore.setCount(1);
                    this.inventory.addItem(toStore);
                    if (!player.getAbilities().instabuild) {
                        itemInHand.shrink(1);
                    }
                    this.playSound(SoundEvents.STONE_BREAK, 1.0F, 1.0F);
                    if (this.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.2D, this.getZ(), 8, 0.3D, 0.3D, 0.3D, 0.05D);
                    }
                    String name = this.getCustomGivenName().isEmpty() ? "Esta súbdita" : this.getCustomGivenName();
                    serverPlayer.sendSystemMessage(Component.literal("§a" + name + " ahora es §eMinera§a de la tribu. Extraerá recursos y minerales de las profundidades."));
                }
                return InteractionResult.SUCCESS;
            } else if ((itemInHand.is(Items.BREWING_STAND) || itemInHand.is(Items.GLASS_BOTTLE) || itemInHand.is(Items.GOLDEN_CARROT) || itemInHand.is(Items.GLISTERING_MELON_SLICE)) && this.getProfession() != KoboldProfession.ALCHEMIST) {
                if (!this.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
                    this.setProfession(KoboldProfession.ALCHEMIST);
                    ItemStack toStore = itemInHand.copy();
                    toStore.setCount(1);
                    this.inventory.addItem(toStore);
                    if (!player.getAbilities().instabuild) {
                        itemInHand.shrink(1);
                    }
                    this.playSound(SoundEvents.BREWING_STAND_BREW, 1.0F, 1.0F);
                    if (this.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.2D, this.getZ(), 8, 0.3D, 0.3D, 0.3D, 0.05D);
                    }
                    String name = this.getCustomGivenName().isEmpty() ? "Esta súbdita" : this.getCustomGivenName();
                    serverPlayer.sendSystemMessage(Component.literal("§a" + name + " ahora es §eAlquimista§a de la tribu. Elaborará raciones doradas y medicinas para la tribu."));
                }
                return InteractionResult.SUCCESS;
            } else if ((itemInHand.is(Items.COMPASS) || itemInHand.is(Items.RECOVERY_COMPASS) || itemInHand.is(Items.MAP) || itemInHand.is(Items.FILLED_MAP)) && this.getProfession() != KoboldProfession.SCOUT) {
                if (!this.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
                    this.setProfession(KoboldProfession.SCOUT);
                    ItemStack toStore = itemInHand.copy();
                    toStore.setCount(1);
                    this.inventory.addItem(toStore);
                    if (!player.getAbilities().instabuild) {
                        itemInHand.shrink(1);
                    }
                    this.playSound(SoundEvents.SPYGLASS_USE, 1.0F, 1.0F);
                    if (this.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.2D, this.getZ(), 8, 0.3D, 0.3D, 0.3D, 0.05D);
                    }
                    String name = this.getCustomGivenName().isEmpty() ? "Esta súbdita" : this.getCustomGivenName();
                    serverPlayer.sendSystemMessage(Component.literal("§a" + name + " ahora es §eExploradora§a de la tribu. Rastreará territorio y reportará a la Reina."));
                }
                return InteractionResult.SUCCESS;
            }
        }

        // 5.5. Clic derecho con mano vacía:
        // Si es la Reina -> Abrir Menú de Gobernación Tribal
        // Si es una súbdita -> Alternar Postura de Mando (RUTINA -> SEGUIR -> GUARDIA)
        if (itemInHand.isEmpty()) {
            if (this.isAnyQueen()) {
                if (!this.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
                    PacketDistributor.sendToPlayer(serverPlayer, new OpenQueenDialoguePayload(
                            this.getId(),
                            this.getQueenTitle(),
                            this.getTribeName(),
                            this.isPacifistRouteChosen(),
                            true
                    ));
                }
                return InteractionResult.SUCCESS;
            } else {
                if (!this.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
                    CommandStance current = this.getCommandStance();
                    CommandStance next = switch (current) {
                        case WORK -> CommandStance.FOLLOW;
                        case FOLLOW -> CommandStance.GUARD;
                        case GUARD -> CommandStance.WORK;
                    };
                    this.setCommandStance(next);
                    if (next == CommandStance.GUARD) {
                        this.setGuardPosition(this.blockPosition());
                    }
                    String name = this.getCustomGivenName().isEmpty() ? this.getProfession().getDisplayName() : this.getCustomGivenName();
                    serverPlayer.sendSystemMessage(Component.literal("§e" + name + " §7ahora está en postura: §f" + next.getIcon() + " §6" + next.getDisplayName()), true);
                    this.playSound(next == CommandStance.FOLLOW ? SoundEvents.BELL_BLOCK : (next == CommandStance.GUARD ? SoundEvents.ARMOR_EQUIP_GENERIC.value() : SoundEvents.VILLAGER_YES), 1.0F, 1.2F);
                    if (this.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.2D, this.getZ(), 6, 0.25D, 0.25D, 0.25D, 0.05D);
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }

        // 5.6. Clic derecho con comida: Alimentar si tiene hambre o guardar en mochila si está llena
        FoodProperties food = itemInHand.get(DataComponents.FOOD);
        if (food != null) {
            if (this.needsFood()) {
                if (!this.level().isClientSide()) {
                    this.eat(itemInHand, food);
                }
                return InteractionResult.SUCCESS;
            } else {
                if (!this.level().isClientSide()) {
                    ItemStack toStore = itemInHand.copy();
                    toStore.setCount(1);
                    ItemStack remaining = this.inventory.addItem(toStore);
                    if (remaining.isEmpty()) {
                        if (!player.getAbilities().instabuild) {
                            itemInHand.shrink(1);
                        }
                        this.playSound(SoundEvents.ITEM_PICKUP, 0.3F, 1.2F);
                        if (this.level() instanceof ServerLevel serverLevel) {
                            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                                    this.getX(), this.getY() + 1.2D, this.getZ(),
                                    4, 0.2D, 0.2D, 0.2D, 0.05D);
                        }
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }

        // 5.7. Clic derecho con armadura: Equipar directamente en su slot de armadura
        Equippable equippable = itemInHand.get(DataComponents.EQUIPPABLE);
        if (equippable != null && equippable.slot().isArmor()) {
            EquipmentSlot slot = equippable.slot();
            if (!this.level().isClientSide()) {
                ItemStack current = this.getItemBySlot(slot);
                ItemStack toEquip = itemInHand.copy();
                toEquip.setCount(1);
                this.setItemSlot(slot, toEquip);
                if (!player.getAbilities().instabuild) {
                    itemInHand.shrink(1);
                }
                if (!current.isEmpty()) {
                    if (!player.getInventory().add(current) && this.level() instanceof ServerLevel serverLevel) {
                        this.spawnAtLocation(serverLevel, current);
                    }
                }
                if (equippable.equipSound() != null) {
                    this.playSound(equippable.equipSound().value(), 1.0F, 1.0F);
                }
            }
            return InteractionResult.SUCCESS;
        }

        // 5.8. Clic derecho con arma, herramienta (azada) o escudo: Guardar en el inventario/mochila sin desgastar
        if (isWeapon(itemInHand) || isHoe(itemInHand) || isShield(itemInHand)) {
            if (!this.level().isClientSide()) {
                ItemStack toStore = itemInHand.copy();
                toStore.setCount(1);
                if (this.inventory.canAddItem(toStore)) {
                    this.inventory.addItem(toStore);
                    if (!player.getAbilities().instabuild) {
                        itemInHand.shrink(1);
                    }
                    this.playSound(SoundEvents.ARMOR_EQUIP_GENERIC.value(), 1.0F, 1.0F);
                    if (this.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                                this.getX(), this.getY() + 1.2D, this.getZ(),
                                4, 0.2D, 0.2D, 0.2D, 0.05D);
                    }
                    if (this.isThreatened()) {
                        this.equipForCombat(true);
                    } else if (this.isHunting()) {
                        this.equipForCombat(false);
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }

        // 5.9. Clic derecho con mano vacía u otros ítems: Diálogo de lealtad
        if (!this.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
            this.speakToPlayer(serverPlayer);
        }

        return InteractionResult.SUCCESS;
    }
    public void handleDialogueAction(ServerPlayer player, int actionId) {
        if (player == null || !this.isAlive() || this.distanceToSqr(player) > 64.0D) {
            return;
        }

        switch (actionId) {
            case QueenDialogueActionPayload.ACTION_JUST_PASSING -> {
                this.getLookControl().setLookAt(player, 30.0F, 30.0F);
                this.setYBodyRot(this.getYHeadRot());

                Component speaker = Component.literal("<" + this.getQueenTitle() + "> ").withStyle(ChatFormatting.GOLD);
                Component msg = Component.literal("Puedes hablar conmigo cuando quieras.")
                        .withStyle(ChatFormatting.AQUA);
                player.sendSystemMessage(speaker.copy().append(msg));

                this.playSound(SoundEvents.VILLAGER_YES, 0.9F, 1.1F);
                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.2D, this.getZ(), 8, 0.3D, 0.3D, 0.3D, 0.05D);
                }
            }
            case QueenDialogueActionPayload.ACTION_CHALLENGE_DUEL -> {
                if (this.pacifistRouteChosen) {
                    Component speaker = Component.literal("<" + this.getQueenTitle() + "> ").withStyle(ChatFormatting.GOLD);
                    Component msg = Component.literal("Has elegido el camino de la paz y el progreso para mi pueblo. El camino de la violencia ha quedado atrás.")
                            .withStyle(ChatFormatting.RED);
                    player.sendSystemMessage(speaker.copy().append(msg));
                    return;
                }
                this.startPlayerDominanceDuel(player);
            }
            case QueenDialogueActionPayload.ACTION_PASSIVE_PROGRESS -> {
                this.getLookControl().setLookAt(player, 30.0F, 30.0F);
                this.setYBodyRot(this.getYHeadRot());

                boolean wasChosen = this.pacifistRouteChosen;
                this.pacifistRouteChosen = true;

                Component speaker = Component.literal("<" + this.getQueenTitle() + "> ").withStyle(ChatFormatting.GOLD);
                if (!wasChosen) {
                    Component msg = doesPlayerOwnAnyTribe(player)
                            ? Component.literal("Si tus palabras son sinceras y vienes a ayudarnos como a tu pueblo, muéstralo con estas ofrendas.").withStyle(ChatFormatting.GREEN)
                            : Component.literal("Si deseas ganarte la lealtad de mi tribu con sabiduría, ayúdanos con estas ofrendas.").withStyle(ChatFormatting.GREEN);
                    player.sendSystemMessage(speaker.copy().append(msg));
                }

                this.playSound(SoundEvents.VILLAGER_YES, 0.9F, 1.0F);
                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.2D, this.getZ(), 8, 0.3D, 0.3D, 0.3D, 0.05D);
                }

                this.updatePacifistOfferForPlayer(player);
                this.setTradingPlayer(player);
                this.openTradingScreen(player, Component.literal("Desarrollo: " + this.getTribeName()), 1);
            }
            case QueenDialogueActionPayload.ACTION_RULER_CALL_ALL_FOLLOW -> {
                this.broadcastCommandStance(player, CommandStance.FOLLOW);
            }
            case QueenDialogueActionPayload.ACTION_RULER_CALL_ALL_WORK -> {
                this.broadcastCommandStance(player, CommandStance.WORK);
            }
            case QueenDialogueActionPayload.ACTION_RULER_CALL_ALL_GUARD -> {
                this.broadcastCommandStance(player, CommandStance.GUARD);
            }
            case QueenDialogueActionPayload.ACTION_RULER_COLLECT_TRIBUTES -> {
                this.collectTributesForRuler(player);
            }
            case QueenDialogueActionPayload.ACTION_RULER_TRIBE_REPORT -> {
                this.sendTribeReportToRuler(player);
            }
            case QueenDialogueActionPayload.ACTION_RULER_EXPANSION_MENU -> {
                Component speaker = Component.literal("<" + this.getQueenTitle() + "> ").withStyle(ChatFormatting.GOLD);
                VillageTier tier = this.getVillageTier();
                player.sendSystemMessage(speaker.copy().append(Component.literal("Estado del Desarrollo y Expansión Tribal:").withStyle(ChatFormatting.YELLOW)));
                player.sendSystemMessage(Component.literal("§7Rango de la aldea: §b" + tier.getIcon() + " " + tier.getDisplayName() + " §7(Prosperidad: §e" + this.getProsperity() + "§7/300)"));
                if (this.activeVillageProject != null) {
                    String stateStr = !this.activeVillageProject.isTerraformed()
                            ? "§eAplanando y nivelando terreno para cimientos..."
                            : "§aEdificando estructura (" + (this.activeVillageProject.getBuildIndex() * 100 / Math.max(1, this.activeVillageProject.getTotalBlocks())) + "%)";
                    player.sendSystemMessage(Component.literal("§d🏗 Proyecto activo: §f" + this.activeVillageProject.getStructureType().getDisplayName() + " §7- " + stateStr));
                    if (this.villageProjectMissingItem != null) {
                        player.sendSystemMessage(Component.literal("§c⚠ Obras pausadas: §eFalta 1x " + new ItemStack(this.villageProjectMissingItem).getHoverName().getString() + " §7en los cofres comunales."));
                        player.sendSystemMessage(Component.literal("§7💡 Coloca el ítem en cualquier cofre o barril de la aldea y las constructoras irán a buscarlo."));
                    }
                } else {
                    player.sendSystemMessage(Component.literal("§aNo hay obras activas en este instante. Nuestras súbditas constructoras están listas."));
                    StructureBlueprint.StructureType nextType = null;
                    if (this.builtStructuresCount == 0) nextType = StructureBlueprint.StructureType.CAMPFIRE_BASE;
                    else if (this.builtStructuresCount <= 2) nextType = StructureBlueprint.StructureType.TENT;
                    else if (tier.getLevel() >= 2 && this.builtStructuresCount == 3) nextType = StructureBlueprint.StructureType.FARM;
                    else if (tier.getLevel() >= 2 && this.builtStructuresCount == 4) nextType = StructureBlueprint.StructureType.STOREHOUSE;
                    else if (tier.getLevel() >= 2 && this.builtStructuresCount == 5) nextType = StructureBlueprint.StructureType.TENT;
                    else if (tier.getLevel() >= 3 && this.builtStructuresCount <= 7) nextType = StructureBlueprint.StructureType.WATCHTOWER;

                    if (nextType != null) {
                        this.startNewVillageProject(nextType);
                    } else {
                        player.sendSystemMessage(Component.literal("§7Entrega más ofrendas o asigna más súbditas a recolectar para aumentar la prosperidad y desbloquear más planos."));
                    }
                }
                this.playSound(SoundEvents.VILLAGER_YES, 1.0F, 1.0F);
            }
            case QueenDialogueActionPayload.ACTION_JUDGMENT_EXECUTE -> {
                if (this.isDownedQueen()) {
                    this.executeJudgment(player, false);
                }
            }
            case QueenDialogueActionPayload.ACTION_JUDGMENT_SPARE -> {
                if (this.isDownedQueen()) {
                    this.executeJudgment(player, true);
                }
            }
        }
    }

    public void broadcastCommandStance(ServerPlayer player, CommandStance stance) {
        if (player == null || !(this.level() instanceof ServerLevel serverLevel)) return;
        List<AbstractTribalEntity> tribe = serverLevel.getEntitiesOfClass(
                AbstractTribalEntity.class,
                this.getBoundingBox().inflate(64.0D),
                k -> k.isAlive() && k.isOwnedBy(player) && Objects.equals(k.getTribeName(), this.getTribeName())
        );
        int count = 0;
        for (AbstractTribalEntity girl : tribe) {
            if (girl.isAnyQueen()) continue;
            girl.setCommandStance(stance);
            if (stance == CommandStance.GUARD) {
                girl.setGuardPosition(girl.blockPosition());
            }
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, girl.getX(), girl.getY() + 1.2D, girl.getZ(), 4, 0.2D, 0.2D, 0.2D, 0.05D);
            count++;
        }
        String orderText = switch (stance) {
            case FOLLOW -> "¡Toda la tribu, sigan a nuestro gobernante!";
            case WORK -> "¡Todas a sus puestos! Continúen con sus labores cotidianas.";
            case GUARD -> "¡Toda la tribu en guardia! Defiendan el territorio de la tribu.";
        };
        Component speaker = Component.literal("<" + this.getQueenTitle() + "> ").withStyle(ChatFormatting.GOLD);
        player.sendSystemMessage(speaker.copy().append(Component.literal(orderText + " (§e" + count + " súbditas recibieron la orden§f)").withStyle(ChatFormatting.YELLOW)));
        this.playSound(SoundEvents.BELL_BLOCK, 1.0F, 1.0F);
    }

    public void collectTributesForRuler(ServerPlayer player) {
        if (player == null || !(this.level() instanceof ServerLevel serverLevel)) return;
        long now = serverLevel.getGameTime();
        if (this.lastTributeTime != 0 && (now - this.lastTributeTime) < 24000L) {
            long remainingTicks = 24000L - (now - this.lastTributeTime);
            int minutes = (int) Math.ceil(remainingTicks / 1200.0D);
            Component speaker = Component.literal("<" + this.getQueenTitle() + "> ").withStyle(ChatFormatting.GOLD);
            Component msg = Component.literal("Mis súbditas aún están reuniendo las cosechas y recursos del día. Vuelve en un rato (~" + minutes + " min restantes).")
                    .withStyle(ChatFormatting.AQUA);
            player.sendSystemMessage(speaker.copy().append(msg));
            this.playSound(SoundEvents.VILLAGER_NO, 1.0F, 1.0F);
            return;
        }

        List<AbstractTribalEntity> tribe = serverLevel.getEntitiesOfClass(
                AbstractTribalEntity.class,
                this.getBoundingBox().inflate(64.0D),
                k -> k.isAlive() && k.isOwnedBy(player) && Objects.equals(k.getTribeName(), this.getTribeName())
        );

        int farmers = 0, hunters = 0, guards = 0, vigilants = 0, tier2Count = 0;
        for (AbstractTribalEntity k : tribe) {
            if (k.isAnyQueen()) continue;
            switch (k.getProfession()) {
                case FARMER -> farmers++;
                case HUNTER -> hunters++;
                case GUARD -> guards++;
                case VIGILANT -> vigilants++;
                default -> {}
            }
            if (k.getProfessionTier() >= 2) tier2Count++;
        }

        this.lastTributeTime = now;
        List<ItemStack> tributes = new ArrayList<>();

        // Granjeras aportan pan, trigo y zanahorias
        int breadCount = Math.max(2, farmers * 3 + (tier2Count * 2));
        tributes.add(new ItemStack(Items.BREAD, Math.min(64, breadCount)));
        tributes.add(new ItemStack(Items.WHEAT, Math.min(64, Math.max(4, farmers * 4))));
        if (farmers > 0) {
            tributes.add(new ItemStack(Items.CARROT, Math.min(64, Math.max(2, farmers * 3))));
        }

        // Cazadoras aportan carne cocinada y cuero
        int meatCount = Math.max(2, hunters * 3 + (tier2Count * 2));
        tributes.add(new ItemStack(Items.COOKED_BEEF, Math.min(64, meatCount)));
        if (hunters > 0) {
            tributes.add(new ItemStack(Items.LEATHER, Math.min(64, Math.max(2, hunters * 2))));
        }

        // Guardias y Vigilantes aportan flechas, hierro y esmeraldas
        int defenders = guards + vigilants;
        int arrowCount = Math.max(4, defenders * 4);
        tributes.add(new ItemStack(Items.ARROW, Math.min(64, arrowCount)));
        int ironCount = Math.max(1, defenders * 2 + tier2Count);
        tributes.add(new ItemStack(Items.IRON_INGOT, Math.min(64, ironCount)));
        if (tier2Count > 0 || tribe.size() >= 3) {
            tributes.add(new ItemStack(Items.EMERALD, Math.min(16, Math.max(1, tier2Count + tribe.size() / 3))));
        }

        for (ItemStack stack : tributes) {
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
        }

        Component speaker = Component.literal("<" + this.getQueenTitle() + "> ").withStyle(ChatFormatting.GOLD);
        Component msg = Component.literal("Aquí tienes el tributo diario y las riquezas producidas por nuestra tribu, honorable gobernante.")
                .withStyle(ChatFormatting.GREEN);
        player.sendSystemMessage(speaker.copy().append(msg));
        this.playSound(SoundEvents.PLAYER_LEVELUP, 1.0F, 1.0F);
        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.2D, this.getZ(), 16, 0.4D, 0.4D, 0.4D, 0.05D);
    }

    public void sendTribeReportToRuler(ServerPlayer player) {
        if (player == null || !(this.level() instanceof ServerLevel serverLevel)) return;
        List<AbstractTribalEntity> tribe = serverLevel.getEntitiesOfClass(
                AbstractTribalEntity.class,
                this.getBoundingBox().inflate(64.0D),
                k -> k.isAlive() && k.isOwnedBy(player) && Objects.equals(k.getTribeName(), this.getTribeName())
        );

        int farmers = 0, hunters = 0, guards = 0, vigilants = 0, builders = 0, sorceresses = 0, unassigned = 0, tier2Count = 0;
        int following = 0, guarding = 0, working = 0;

        for (AbstractTribalEntity k : tribe) {
            if (k.isAnyQueen()) continue;
            switch (k.getProfession()) {
                case FARMER -> farmers++;
                case HUNTER -> hunters++;
                case GUARD -> guards++;
                case VIGILANT -> vigilants++;
                case BUILDER -> builders++;
                case SORCERESS -> sorceresses++;
                case NONE -> unassigned++;
            }
            if (k.getProfessionTier() >= 2) tier2Count++;
            switch (k.getCommandStance()) {
                case FOLLOW -> following++;
                case GUARD -> guarding++;
                case WORK -> working++;
            }
        }

        player.sendSystemMessage(Component.literal("§6========================================"));
        player.sendSystemMessage(Component.literal("§e📜 CENSO Y ESTADO DE LA TRIBU: §f" + this.getTribeName()));
        player.sendSystemMessage(Component.literal("§7Reina regente: §6" + this.getQueenTitle()));
        player.sendSystemMessage(Component.literal("§7Rango de la aldea: §b" + this.getVillageTier().getIcon() + " " + this.getVillageTier().getDisplayName() + " §7(Prosperidad: §e" + this.getProsperity() + "§7/300)"));
        if (this.activeVillageProject != null) {
            String stateStr = !this.activeVillageProject.isTerraformed() ? "§eAplanando y nivelando terreno..." : "§aConstruyendo (" + (this.activeVillageProject.getBuildIndex() * 100 / Math.max(1, this.activeVillageProject.getTotalBlocks())) + "%)";
            player.sendSystemMessage(Component.literal("§d🏗 Proyecto en curso: §f" + this.activeVillageProject.getStructureType().getDisplayName() + " - " + stateStr));
            if (this.villageProjectMissingItem != null) {
                player.sendSystemMessage(Component.literal("§c  ⚠ Pausado: §eFalta 1x " + new ItemStack(this.villageProjectMissingItem).getHoverName().getString() + " §7en los cofres de la aldea."));
            }
        } else {
            player.sendSystemMessage(Component.literal("§7🏗 Proyecto en curso: §fNinguno (Aldea en paz)"));
        }
        player.sendSystemMessage(Component.literal("§7Población leal: §a" + tribe.size() + " Kobolds"));
        player.sendSystemMessage(Component.literal("§a  🌾 Granjeras: " + farmers + " | 🏹 Cazadoras: " + hunters + " | 🔨 Constructoras: " + builders));
        player.sendSystemMessage(Component.literal("§9  🛡 Guardias: " + guards + " | 👁 Vigilantes: " + vigilants));
        if (sorceresses > 0) {
            player.sendSystemMessage(Component.literal("§d  ✨ Hechiceras arcanas: " + sorceresses));
        }
        if (unassigned > 0) {
            player.sendSystemMessage(Component.literal("§8  ❔ Súbditas sin asignar: " + unassigned));
        }
        player.sendSystemMessage(Component.literal("§d⭐ Súbditas Veteranas (Tier 2): " + tier2Count));
        player.sendSystemMessage(Component.literal("§bPosturas actuales: §f🚶 Siguiendo: " + following + " | 🛡 En Guardia: " + guarding + " | 🌾 En Labores: " + working));
        player.sendSystemMessage(Component.literal("§6========================================"));
        this.playSound(SoundEvents.VILLAGER_YES, 1.0F, 1.0F);
    }

    public void startPlayerDominanceDuel(Player player) {
        if (player == null || !(this.level() instanceof ServerLevel level)) return;
        this.playerDuelTargetUUID = player.getUUID();
        this.duelArenaCenter = this.position().add(player.position()).scale(0.5D);
        this.unequipHands();
        this.setTarget(player);

        Component speaker = Component.literal("<" + this.getQueenTitle() + "> ").withStyle(ChatFormatting.GOLD);
        Component challengeMsg = Component.literal("¡¿Te atreves a desafiar a la Reina en su propio territorio?! ¡Que los ancestros juzguen quién es digno de gobernar!")
                .withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
        player.sendSystemMessage(speaker.copy().append(challengeMsg));

        level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 1.0F, 1.5F);
        level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BELL_BLOCK, SoundSource.NEUTRAL, 1.0F, 0.8F);
        level.sendParticles(ParticleTypes.ANGRY_VILLAGER, this.getX(), this.getY() + 1.2D, this.getZ(), 20, 0.4D, 0.4D, 0.4D, 0.05D);
    }

    public void resolvePlayerDominanceVictory(Player player) {
        if (player == null || !(this.level() instanceof ServerLevel level)) return;

        this.playerDuelTargetUUID = null;
        this.duelArenaCenter = null;
        this.setTarget(null);
        this.unequipHands();
        this.setHealth(Math.max(2.0F, this.getHealth()));
        this.heal(8.0F);

        // Otorgar propiedad de la reina al jugador manteniendo su estatus y dignidad real
        if (getPlayerPrimaryTribe(player).isEmpty()) {
            player.getPersistentData().putString("PlayerOwnedTribe", this.getTribeName());
        }
        this.setOwnerUUID(player.getUUID());
        this.setRebel(false);
        this.setQueen(true);
        this.setProfession(KoboldProfession.NONE);
        this.setPersistenceRequired();
        this.updateRulerCustomName();

        // Diálogo de sumisión y reconocimiento
        Component speaker = Component.literal("<" + this.getQueenTitle() + "> ").withStyle(ChatFormatting.GOLD);
        Component surrenderMsg = Component.literal("Has demostrado tu fuerza... mi pueblo te reconoce como gobernante.")
                .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD);
        player.sendSystemMessage(speaker.copy().append(surrenderMsg));

        // Efectos de victoria
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.5D, this.getZ(), 35, 0.6D, 0.6D, 0.6D, 0.1D);
        level.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 1.2D, this.getZ(), 15, 0.5D, 0.5D, 0.5D, 0.1D);
        level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.NEUTRAL, 1.0F, 1.0F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);

        // Buscar a las súbditas de su tribu en un radio amplio (128 bloques)
        String tribeName = this.getTribeName();
        List<AbstractTribalEntity> subditas = level.getEntitiesOfClass(
                AbstractTribalEntity.class,
                this.getBoundingBox().inflate(128.0D),
                k -> !k.isAnyQueen() && k.isAlive() && (tribeName.isEmpty() || k.getTribeName().isEmpty() || k.getTribeName().equals(tribeName))
        );

        if (!subditas.isEmpty()) {
            List<AbstractTribalEntity> rebelCandidates = new ArrayList<>();
            List<AbstractTribalEntity> loyalWorkers = new ArrayList<>();
            for (AbstractTribalEntity subdita : subditas) {
                // Sincronizar nombre de tribu si estaba vacío
                if (subdita.getTribeName().isEmpty() && !tribeName.isEmpty()) {
                    subdita.setTribeName(tribeName);
                }
                // Constructoras y Granjeras son trabajadoras civiles pacíficas y NUNCA se rebelan
                if (subdita.getProfession() == KoboldProfession.BUILDER || subdita.getProfession() == KoboldProfession.FARMER) {
                    loyalWorkers.add(subdita);
                } else {
                    rebelCandidates.add(subdita);
                }
            }

            // Procesar trabajadoras civiles: siempre leales y en postura WORK
            for (AbstractTribalEntity worker : loyalWorkers) {
                worker.setRebel(false);
                worker.setOwnerUUID(player.getUUID());
                worker.setTarget(null);
                worker.setLastHurtByMob(null);
                worker.unequipHands();
                worker.setCommandStance(CommandStance.WORK);
                worker.updateRulerCustomName();

                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, worker.getX(), worker.getY() + 1.2D, worker.getZ(), 16, 0.4D, 0.4D, 0.4D, 0.05D);
                level.sendParticles(ParticleTypes.HEART, worker.getX(), worker.getY() + 1.2D, worker.getZ(), 6, 0.3D, 0.3D, 0.3D, 0.05D);
                level.playSound(null, worker.getX(), worker.getY(), worker.getZ(), SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 0.9F, 1.1F);
            }

            int actualRebels = 0;
            if (!rebelCandidates.isEmpty()) {
                List<AbstractTribalEntity> shuffled = new ArrayList<>(rebelCandidates);
                Collections.shuffle(shuffled, new java.util.Random(level.getRandom().nextLong()));

                int totalCandidates = shuffled.size();
                int numRebels;
                if (totalCandidates <= 2) {
                    numRebels = level.getRandom().nextFloat() < 0.25F ? 1 : 0;
                } else if (totalCandidates <= 4) {
                    numRebels = 1;
                } else {
                    numRebels = Math.max(1, (int) Math.round(totalCandidates * 0.20D));
                }

                for (int i = 0; i < totalCandidates; i++) {
                    AbstractTribalEntity subdita = shuffled.get(i);
                    if (i < numRebels) {
                        // Rebelde: se opone al nuevo gobernante pero no ataca de inmediato; se aleja
                        subdita.setRebel(true);
                        subdita.setOwnerUUID(null);
                        subdita.setTarget(null);
                        subdita.setLastHurtByMob(null);
                        subdita.unequipHands();
                        subdita.setCustomName(Component.literal(subdita.getProfession().getDisplayName() + " Rebelde").withStyle(ChatFormatting.RED));
                        subdita.setCustomNameVisible(true);
                        actualRebels++;

                        level.sendParticles(ParticleTypes.ANGRY_VILLAGER, subdita.getX(), subdita.getY() + 1.2D, subdita.getZ(), 14, 0.4D, 0.4D, 0.4D, 0.05D);
                        level.playSound(null, subdita.getX(), subdita.getY(), subdita.getZ(), SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, 1.0F, 0.9F);
                    } else {
                        // Aliada leal: ahora es propiedad del jugador
                        subdita.setRebel(false);
                        subdita.setOwnerUUID(player.getUUID());
                        subdita.setTarget(null);
                        subdita.setLastHurtByMob(null);
                        subdita.unequipHands();
                        subdita.updateRulerCustomName();

                        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, subdita.getX(), subdita.getY() + 1.2D, subdita.getZ(), 16, 0.4D, 0.4D, 0.4D, 0.05D);
                        level.sendParticles(ParticleTypes.HEART, subdita.getX(), subdita.getY() + 1.2D, subdita.getZ(), 6, 0.3D, 0.3D, 0.3D, 0.05D);
                        level.playSound(null, subdita.getX(), subdita.getY(), subdita.getZ(), SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 0.9F, 1.1F);
                    }
                }
            }

            if (actualRebels > 0) {
                player.sendSystemMessage(Component.literal("⚠ ¡" + actualRebels + " combatiente(s) kobold se niegan a someterse y se han rebelado contra ti!")
                        .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
            } else {
                player.sendSystemMessage(Component.literal("✨ Todas las súbditas de la tribu han jurado lealtad ante ti.")
                        .withStyle(ChatFormatting.GREEN));
            }
        }

        // Ascender el Báculo de Mando del jugador al Nivel 2 (Baldosa de Pizarra Profunda)
        BaculoItem.upgradePlayerStaff(player, 2);
    }

    public static int resetTribeOwnership(ServerPlayer player, double radius) {
        if (player == null || !(player.level() instanceof ServerLevel level)) return 0;
        AABB box = player.getBoundingBox().inflate(radius);
        List<AbstractTribalEntity> nearbyKobolds = level.getEntitiesOfClass(
                AbstractTribalEntity.class,
                box,
                AbstractTribalEntity::isAlive
        );
        if (nearbyKobolds.isEmpty()) return 0;

        Set<String> affectedTribes = new HashSet<>();
        int count = 0;

        for (AbstractTribalEntity k : nearbyKobolds) {
            k.setOwnerUUID(null);
            k.setRebel(false);
            k.setTarget(null);
            k.unequipHands();
            k.setCustomName(null);
            k.setCustomNameVisible(false);
            if (k.hasTribe()) {
                affectedTribes.add(k.getTribeName());
            }
            if (k.isAnyQueen()) {
                k.setPlayerDuelTargetUUID(null);
                k.setHealth(k.getMaxHealth());
                k.setPacifistRouteChosen(false);
                k.resetPacifistTrades();
            }
            count++;
        }

        if (!affectedTribes.isEmpty()) {
            List<AbstractTribalEntity> allTribeMembers = level.getEntitiesOfClass(
                    AbstractTribalEntity.class,
                    player.getBoundingBox().inflate(256.0D),
                    k -> k.isAlive() && affectedTribes.contains(k.getTribeName())
            );
            for (AbstractTribalEntity k : allTribeMembers) {
                k.setOwnerUUID(null);
                k.setRebel(false);
                k.setTarget(null);
                k.unequipHands();
                k.setCustomName(null);
                k.setCustomNameVisible(false);
                if (k.isAnyQueen()) {
                    k.setPlayerDuelTargetUUID(null);
                    k.setHealth(k.getMaxHealth());
                    k.setPacifistRouteChosen(false);
                    k.resetPacifistTrades();
                }
            }
        }

        for (String tribe : affectedTribes) {
            String norm = tribe.toLowerCase().replace(" ", "_");
            player.removeTag(TAG_MET_TRIBE_PREFIX + norm);
            player.removeTag(TAG_TEACHER_TRIBE_PREFIX + norm);
        }

        return count;
    }

    /* =========================================================================
       METAS DE IA PERSONALIZADAS
       ========================================================================= */

    public static @Nullable Player findNearbyBaculoPlayer(AbstractTribalEntity mob, double radius) {
        if (mob.level().isClientSide()) return null;
        List<Player> players = mob.level().getEntitiesOfClass(
                Player.class,
                mob.getBoundingBox().inflate(radius),
                p -> p.isAlive() && !p.isSpectator() && mob.isReveringBaculoHolder(p)
        );
        if (players.isEmpty()) return null;

        Player best = null;
        double bestDistSq = Double.MAX_VALUE;
        for (Player p : players) {
            double dSq = mob.distanceToSqr(p);
            if (mob.hasLineOfSight(p)) {
                if (dSq < bestDistSq) {
                    bestDistSq = dSq;
                    best = p;
                }
            }
        }
        return best;
    }


    public boolean hasItemInInventory(net.minecraft.world.item.Item item) {
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.inventory.getItem(i);
            if (stack.is(item)) {
                return true;
            }
        }
        return false;
    }

    public static boolean canBreedAnimal(AbstractTribalEntity kobold, Animal animal) {
        if (animal instanceof Cow || animal instanceof Sheep) {
            return kobold.getWheatCount() > 0;
        } else if (animal instanceof Pig) {
            return kobold.hasItemInInventory(Items.CARROT) || kobold.hasItemInInventory(Items.POTATO) || kobold.hasItemInInventory(Items.BEETROOT);
        } else if (animal instanceof Chicken) {
            return kobold.hasPureSeedsInInventory();
        } else if (animal instanceof Rabbit) {
            return kobold.hasItemInInventory(Items.CARROT) || kobold.hasItemInInventory(Items.GOLDEN_CARROT) || kobold.hasItemInInventory(Items.DANDELION);
        }
        return false;
    }

    public static boolean tryFeedAnimal(AbstractTribalEntity kobold, Animal animal) {
        net.minecraft.world.item.Item feedItem = null;
        if (animal instanceof Cow || animal instanceof Sheep) {
            if (kobold.getWheatCount() > 0) feedItem = Items.WHEAT;
        } else if (animal instanceof Pig) {
            if (kobold.hasItemInInventory(Items.CARROT)) feedItem = Items.CARROT;
            else if (kobold.hasItemInInventory(Items.POTATO)) feedItem = Items.POTATO;
            else if (kobold.hasItemInInventory(Items.BEETROOT)) feedItem = Items.BEETROOT;
        } else if (animal instanceof Chicken) {
            if (kobold.hasItemInInventory(Items.WHEAT_SEEDS)) feedItem = Items.WHEAT_SEEDS;
            else if (kobold.hasItemInInventory(Items.BEETROOT_SEEDS)) feedItem = Items.BEETROOT_SEEDS;
        } else if (animal instanceof Rabbit) {
            if (kobold.hasItemInInventory(Items.CARROT)) feedItem = Items.CARROT;
            else if (kobold.hasItemInInventory(Items.GOLDEN_CARROT)) feedItem = Items.GOLDEN_CARROT;
        }

        if (feedItem != null) {
            kobold.shrinkItemInInventory(feedItem, 1);
            animal.setInLove(null);
            kobold.playSound(SoundEvents.GENERIC_EAT.value(), 0.8F, 1.2F);
            if (kobold.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.HEART, animal.getX(), animal.getY() + 0.8D, animal.getZ(), 6, 0.3D, 0.3D, 0.3D, 0.05D);
            }
            return true;
        }
        return false;
    }

    public boolean consumeItem(net.minecraft.world.item.Item item, int count) {
        if (!this.hasItemInInventory(item)) return false;
        this.shrinkItemInInventory(item, count);
        return true;
    }

    public boolean hasPickaxe() {
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.inventory.getItem(i);
            if (stack.is(ItemTags.PICKAXES)) {
                return true;
            }
        }
        return this.getMainHandItem().is(ItemTags.PICKAXES) || this.getOffhandItem().is(ItemTags.PICKAXES);
    }

    public void damagePickaxe(int amount) {
        if (this.level() instanceof ServerLevel sl) {
            for (int i = 0; i < this.inventory.getContainerSize(); i++) {
                ItemStack stack = this.inventory.getItem(i);
                if (stack.is(ItemTags.PICKAXES)) {
                    stack.hurtAndBreak(amount, sl, null, item -> {});
                    return;
                }
            }
        }
    }

    /* =========================================================================
       PROTECCIÓN CONTRA FUEGO AMIGO: No se dañan entre sí
       ========================================================================= */

    @Override
    public boolean canAttack(LivingEntity target) {
        if (this.isSpectatingDuel()) return false;
        if (target instanceof AbstractTribalEntity targetKobold && targetKobold.isSpectatingDuel()) return false;
        if (target instanceof Player player) {
            if (this.isInPlayerDuelWith(player)) {
                return !player.isSpectator();
            }
            if (this.isOwnedBy(player) && !this.isRebel()) {
                return false;
            }
            if (this.isRebel()) {
                if (player.isSpectator() || player.isCreative()) return false;
                if (isHoldingBaculo(player)) return false;
                if (this.reprimandedCooldown > 0) return false;
                // Ataca al jugador si se acerca a <= 3.5 bloques, o si es su objetivo actual, o si el jugador la atacó
                return this.distanceToSqr(player) <= 3.5D * 3.5D || this.getTarget() == player || this.getLastHurtByMob() == player;
            }
            if (this.isReveringBaculoHolder(player)) {
                return false;
            }
        }
        if (target instanceof AbstractTribalEntity otherKobold) {
            return this.canFightKobold(otherKobold);
        }
        return super.canAttack(target);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (this.isSpectatingDuel()) {
            super.setTarget(null);
            return;
        }
        if (target instanceof AbstractTribalEntity targetKobold && targetKobold.isSpectatingDuel()) {
            super.setTarget(null);
            return;
        }
        if (target instanceof Player player) {
            if (this.isInPlayerDuelWith(player)) {
                super.setTarget(player.isSpectator() ? null : target);
                return;
            }
            if (this.isOwnedBy(player) && !this.isRebel()) {
                super.setTarget(null);
                return;
            }
            if (this.isRebel()) {
                if (player.isSpectator() || player.isCreative() || isHoldingBaculo(player) || this.reprimandedCooldown > 0) {
                    super.setTarget(null);
                    return;
                }
                super.setTarget(target);
                return;
            }
            if (this.isReveringBaculoHolder(player)) {
                super.setTarget(null);
                return;
            }
        }
        if (target instanceof AbstractTribalEntity otherKobold) {
            if (!this.canFightKobold(otherKobold)) {
                super.setTarget(null);
                return;
            }
        }
        super.setTarget(target);
    }

    @Override
    protected boolean considersEntityAsAlly(Entity other) {
        if (other instanceof Player player) {
            if (this.isOwnedBy(player) && !this.isRebel()) {
                return true;
            }
        }
        if (other instanceof AbstractTribalEntity otherKobold) {
            if (this.canFightKobold(otherKobold)) {
                return false;
            }
            return true;
        }
        return super.considersEntityAsAlly(other);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        if (target instanceof AbstractTribalEntity otherKobold) {
            if (!this.canFightKobold(otherKobold)) {
                return false;
            }
        }
        this.swing(InteractionHand.MAIN_HAND);
        this.addExhaustion(0.3F);

        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        ItemStack weapon = this.getMainHandItem();
        if (this.getProfession() == KoboldProfession.GUARD && isMeleeWeapon(weapon)) {
            damage *= 1.05F;
        }
        if (this.getProfessionTier() >= 2) {
            damage *= 1.25F;
        }

        DamageSource damageSource = this.damageSources().mobAttack(this);
        damage = EnchantmentHelper.modifyDamage(level, this.getWeaponItem(), target, damageSource, damage);
        boolean flag = target.hurtServer(level, damageSource, damage);
        if (flag) {
            float knockback = this.getKnockback(target, damageSource);
            if (knockback > 0.0F && target instanceof LivingEntity livingentity) {
                livingentity.knockback(
                        (double) (knockback * 0.5F),
                        (double) Mth.sin(this.getYRot() * ((float) Math.PI / 180F)),
                        (double) (-Mth.cos(this.getYRot() * ((float) Math.PI / 180F))),
                        damageSource,
                        damage
                );
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
            }

            EnchantmentHelper.doPostAttackEffects(level, target, damageSource);
            this.setLastHurtMob(target);
            this.playAttackSound();
        }

        return flag;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource damageSource, float amount) {
        // Reducción de daño para súbditas veteranas Tier 2
        if (this.getProfessionTier() >= 2) {
            amount *= 0.8F;
        }
        // Las Kobold Girl espectadoras son totalmente invulnerables mientras el duelo está activo
        if (this.isSpectatingDuel()) {
            level.sendParticles(ParticleTypes.ENCHANTED_HIT, this.getX(), this.getY() + 0.8D, this.getZ(), 6, 0.2D, 0.3D, 0.2D, 0.05D);
            level.sendParticles(ParticleTypes.WITCH, this.getX(), this.getY() + 0.5D, this.getZ(), 4, 0.15D, 0.2D, 0.15D, 0.02D);
            level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.NEUTRAL, 0.4F, 1.8F);
            return false;
        }

        // Si la Reina está en estado Downed (derrotada a 1 HP tras duelo):
        if (this.isDownedQueen()) {
            if (!this.isMarkedForExecution()) {
                // Invulnerable hasta que el jugador decida ejecutarla formalmente con el Báculo
                level.sendParticles(ParticleTypes.ENCHANTED_HIT, this.getX(), this.getY() + 0.8D, this.getZ(), 8, 0.25D, 0.3D, 0.25D, 0.05D);
                level.sendParticles(ParticleTypes.WITCH, this.getX(), this.getY() + 0.5D, this.getZ(), 5, 0.15D, 0.2D, 0.15D, 0.02D);
                level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.NEUTRAL, 0.5F, 1.6F);
                return false;
            }
            // Si ya está marcada para ejecución, permitir que el golpe la remate
            return super.hurtServer(level, damageSource, amount);
        }

        if (damageSource.getEntity() instanceof AbstractTribalEntity koboldAttacker) {
            // Fuego amigo y rivalidad entre Kobolds:
            if (!this.canFightKobold(koboldAttacker)) {
                // Excepción de disciplina: permitir exclusivamente el puñetazo no letal de la Reina leal (manos vacías y daño <= 1.0F)
                if (koboldAttacker.isAnyQueen() && koboldAttacker.isOwned() && this.isRebel()
                        && (this.getTribeName().equals(koboldAttacker.getTribeName()) || this.distanceToSqr(koboldAttacker) <= 64.0D * 64.0D)
                        && koboldAttacker.getMainHandItem().isEmpty() && amount <= 1.0F) {
                    boolean hurt = super.hurtServer(level, damageSource, amount);
                    this.setTarget(null);
                    return hurt;
                }
                // Si una rebelde intenta agredir o daña accidentalmente a su Reina leal:
                if (this.isAnyQueen() && this.isOwned() && koboldAttacker.isRebel()) {
                    this.setReprimandCooldown(0);
                    this.setForcedReprimandTarget(koboldAttacker);
                }
                return false;
            }
        }

        // Fuego amigo con el dueño: Si la kobold pertenece al jugador y no es rebelde, el dueño no le hace daño accidental
        if (damageSource.getEntity() instanceof Player player) {
            if (this.isOwnedBy(player) && !this.isRebel()) {
                return false;
            }
        }

        // Duelo de Dominancia entre Reina y Jugador:
        // Si el jugador ya posee una tribu, la reina derrotada queda a 1 HP e invulnerable (Downed) para decidir su destino
        if (this.isInPlayerDuel()) {
            Player duelPlayer = this.getPlayerDuelTarget();
            if (this.getHealth() <= 2.0F || this.getHealth() - amount <= 2.0F) {
                Player winner = duelPlayer != null ? duelPlayer : (damageSource.getEntity() instanceof Player p ? p : null);
                if (winner != null && doesPlayerOwnAnyTribe(winner)) {
                    this.enterDownedQueenState(winner);
                    return false;
                }
                this.setHealth(2.0F);
                if (winner != null) {
                    this.resolvePlayerDominanceVictory(winner);
                }
                return false;
            }
        }

        this.addExhaustion(0.2F);

        boolean wasSleeping = this.isSleeping();
        if (wasSleeping) {
            this.stopSleeping();
            this.sleepCooldown = 100;
            if (this.doesAssignedArmorStandHaveArmor()) {
                this.needsArmorRetrieval = true;
            }
            if (damageSource.getEntity() instanceof LivingEntity attacker && attacker.isAlive()) {
                if (!this.isInQueenDuel() && !this.isInPlayerDuel()) {
                    if (attacker instanceof AbstractTribalEntity koboldAttacker) {
                        if (this.canFightKobold(koboldAttacker)) {
                            this.setTarget(koboldAttacker);
                        }
                    } else if (this.canAttack(attacker)) {
                        this.setTarget(attacker);
                    }
                }
            }
        } else if (!this.isInQueenDuel() && !this.isInPlayerDuel()) {
            if (damageSource.getEntity() instanceof AbstractTribalEntity koboldAttacker && koboldAttacker.isAlive()) {
                if (this.canFightKobold(koboldAttacker)) {
                    this.setTarget(koboldAttacker);
                }
            }
        }

        boolean hurt = super.hurtServer(level, damageSource, amount);
        if (wasSleeping || this.sleepCooldown > 0) {
            this.sleepCooldown = 100;
        }
        return hurt;
    }

    /* =========================================================================
       GUARDADO Y CARGA EN DISCO (NBT)
       ========================================================================= */

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("IsQueen", this.isQueen());
        output.putString("TribeName", this.getTribeName());
        output.putBoolean("IsRebel", this.isRebel());
        output.putBoolean("IsInPowerStruggle", this.isInPowerStruggle());
        output.putBoolean("IsSurvivorQueen", this.isSurvivorQueen());
        output.putBoolean("IsDownedQueen", this.isDownedQueen());
        output.putBoolean("IsMarkedForExecution", this.isMarkedForExecution());
        output.putString("Profession", this.getProfession().name());
        output.putInt("FoodLevel", this.getFoodLevel());
        output.putFloat("Saturation", this.getSaturationLevel());
        output.putFloat("Exhaustion", this.exhaustionLevel);
        if (this.getOwnerUUID() != null) {
            output.putString("OwnerUUID", this.getOwnerUUID().toString());
        }
        this.inventory.storeAsItemList(output.list("Inventory", ItemStack.CODEC));
        output.putBoolean("NeedsMorningWalk", this.needsMorningWalk);
        output.putInt("SleepCooldown", this.sleepCooldown);
        if (this.assignedBedPos != null) {
            output.putBoolean("HasAssignedBed", true);
            output.putInt("BedX", this.assignedBedPos.getX());
            output.putInt("BedY", this.assignedBedPos.getY());
            output.putInt("BedZ", this.assignedBedPos.getZ());
        } else {
            output.putBoolean("HasAssignedBed", false);
        }
        if (this.assignedArmorStandUUID != null) {
            output.putBoolean("HasAssignedArmorStand", true);
            output.putString("ArmorStandUUID", this.assignedArmorStandUUID.toString());
        } else {
            output.putBoolean("HasAssignedArmorStand", false);
        }
        output.putBoolean("NeedsArmorRetrieval", this.needsArmorRetrieval);
        output.putInt("ReprimandCooldown", this.reprimandCooldown);
        output.putInt("ReprimandedCooldown", this.reprimandedCooldown);
        output.putBoolean("WitnessedBaculo", this.hasTribeWitnessedBaculo());
        output.putInt("BaculoStareCooldown", this.baculoStareCooldown);
        output.putBoolean("PacifistRouteChosen", this.pacifistRouteChosen);
        output.putInt("CompletedPacifistTrades", this.completedPacifistTrades);
        if (this.pacifistOffers != null) {
            output.store("Offers", MerchantOffers.CODEC, this.pacifistOffers);
        }
        output.putString("CommandStance", this.getCommandStance().name());
        output.putString("CustomGivenName", this.getCustomGivenName());
        output.putInt("ProfessionTier", this.getProfessionTier());
        output.putLong("LastTributeTime", this.lastTributeTime);
        if (this.guardPosition != null) {
            output.putBoolean("HasGuardPos", true);
            output.putInt("GuardX", this.guardPosition.getX());
            output.putInt("GuardY", this.guardPosition.getY());
            output.putInt("GuardZ", this.guardPosition.getZ());
        } else {
            output.putBoolean("HasGuardPos", false);
        }
        output.putInt("Prosperity", this.getProsperity());
        output.putInt("BuiltStructuresCount", this.builtStructuresCount);
        output.putInt("OccupiedPlotsCount", this.occupiedVillagePlots.size());
        for (int i = 0; i < this.occupiedVillagePlots.size(); i++) {
            StructureBlueprint.VillagePlot plot = this.occupiedVillagePlots.get(i);
            output.putInt("Plot_" + i + "_minX", plot.minX());
            output.putInt("Plot_" + i + "_minZ", plot.minZ());
            output.putInt("Plot_" + i + "_maxX", plot.maxX());
            output.putInt("Plot_" + i + "_maxZ", plot.maxZ());
        }
        if (this.campCenterPos != null) {
            output.putBoolean("HasCampCenter", true);
            output.putInt("CampCenterX", this.campCenterPos.getX());
            output.putInt("CampCenterY", this.campCenterPos.getY());
            output.putInt("CampCenterZ", this.campCenterPos.getZ());
        } else {
            output.putBoolean("HasCampCenter", false);
        }
        if (this.activeVillageProject != null) {
            output.putBoolean("HasActiveProject", true);
            output.putString("ProjectType", this.activeVillageProject.getStructureType().name());
            output.putInt("ProjectOriginX", this.activeVillageProject.getOrigin().getX());
            output.putInt("ProjectOriginY", this.activeVillageProject.getOrigin().getY());
            output.putInt("ProjectOriginZ", this.activeVillageProject.getOrigin().getZ());
            output.putBoolean("ProjectTerraformed", this.activeVillageProject.isTerraformed());
            output.putInt("ProjectBuildIndex", this.activeVillageProject.getBuildIndex());
        } else {
            output.putBoolean("HasActiveProject", false);
        }
        if (this.avengeQueenKillerUUID != null) {
            output.putString("AvengeQueenKillerUUID", this.avengeQueenKillerUUID.toString());
        }
        output.putInt("PowerStruggleDelayTicks", this.powerStruggleDelayTicks);
        if (this.pendingPowerStruggleTribe != null) {
            output.putString("PendingPowerStruggleTribe", this.pendingPowerStruggleTribe);
        }
        if (this.pendingPowerStruggleCenter != null) {
            output.putBoolean("HasPendingPowerStruggleCenter", true);
            output.putDouble("PendingCenterX", this.pendingPowerStruggleCenter.x);
            output.putDouble("PendingCenterY", this.pendingPowerStruggleCenter.y);
            output.putDouble("PendingCenterZ", this.pendingPowerStruggleCenter.z);
        } else {
            output.putBoolean("HasPendingPowerStruggleCenter", false);
        }
        output.putBoolean("IsReincarnating", this.isReincarnating());
        output.putInt("ReincarnationTicks", this.reincarnationTicksRemaining);
        if (this.pendingReincarnatedQueenName != null) {
            output.putString("PendingReincarnatedQueenName", this.pendingReincarnatedQueenName);
        }
        if (this.reincarnationInitiatorPlayerUUID != null) {
            output.putString("ReincarnationInitiatorUUID", this.reincarnationInitiatorPlayerUUID.toString());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setQueen(input.getBooleanOr("IsQueen", false));
        this.setTribeName(input.getStringOr("TribeName", ""));
        this.setRebel(input.getBooleanOr("IsRebel", false));
        this.setInPowerStruggle(input.getBooleanOr("IsInPowerStruggle", false));
        this.setSurvivorQueen(input.getBooleanOr("IsSurvivorQueen", false));
        this.setDownedQueen(input.getBooleanOr("IsDownedQueen", false));
        this.setMarkedForExecution(input.getBooleanOr("IsMarkedForExecution", false));
        this.setProfession(KoboldProfession.fromString(input.getStringOr("Profession", "NONE")));
        String ownerStr = input.getStringOr("OwnerUUID", "");
        if (!ownerStr.isEmpty()) {
            try {
                this.setOwnerUUID(UUID.fromString(ownerStr));
            } catch (IllegalArgumentException ignored) {
            }
        } else {
            this.setOwnerUUID(null);
        }
        this.setFoodLevel(input.getIntOr("FoodLevel", 20));
        this.setSaturationLevel(input.getFloatOr("Saturation", 5.0F));
        this.exhaustionLevel = input.getFloatOr("Exhaustion", 0.0F);
        this.inventory.fromItemList(input.listOrEmpty("Inventory", ItemStack.CODEC));
        this.needsMorningWalk = input.getBooleanOr("NeedsMorningWalk", false);
        this.sleepCooldown = input.getIntOr("SleepCooldown", 0);
        this.reprimandCooldown = input.getIntOr("ReprimandCooldown", 0);
        this.reprimandedCooldown = input.getIntOr("ReprimandedCooldown", 0);
        this.tribeWitnessedBaculo = input.getBooleanOr("WitnessedBaculo", false);
        if (this.tribeWitnessedBaculo && this.hasTribe()) {
            WITNESSED_BACULO_TRIBES.add(this.getTribeName());
        }
        this.baculoStareCooldown = input.getIntOr("BaculoStareCooldown", 0);
        if (input.getBooleanOr("HasAssignedArmorStand", false)) {
            String uuidStr = input.getStringOr("ArmorStandUUID", "");
            if (!uuidStr.isEmpty()) {
                try {
                    UUID standUUID = UUID.fromString(uuidStr);
                    this.assignedArmorStandUUID = standUUID;
                    CLAIMED_ARMOR_STANDS.put(standUUID, this.getUUID());
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        if (input.getBooleanOr("HasAssignedBed", false)) {
            int bx = input.getIntOr("BedX", 0);
            int by = input.getIntOr("BedY", 0);
            int bz = input.getIntOr("BedZ", 0);
            BlockPos bPos = new BlockPos(bx, by, bz);
            this.assignedBedPos = bPos;
            CLAIMED_BEDS.put(bPos, this.getUUID());
            this.setHomeTo(bPos, 100);
        }
        this.needsArmorRetrieval = input.getBooleanOr("NeedsArmorRetrieval", false);
        this.pacifistRouteChosen = input.getBooleanOr("PacifistRouteChosen", false);
        this.completedPacifistTrades = input.getIntOr("CompletedPacifistTrades", 0);
        this.pacifistOffers = input.read("Offers", MerchantOffers.CODEC).orElse(null);

        this.setCommandStance(CommandStance.fromString(input.getStringOr("CommandStance", "WORK")));
        this.setCustomGivenName(input.getStringOr("CustomGivenName", ""));
        this.setProfessionTier(input.getIntOr("ProfessionTier", 1));
        this.lastTributeTime = input.getLongOr("LastTributeTime", 0L);
        if (input.getBooleanOr("HasGuardPos", false)) {
            int gx = input.getIntOr("GuardX", 0);
            int gy = input.getIntOr("GuardY", 0);
            int gz = input.getIntOr("GuardZ", 0);
            this.setGuardPosition(new BlockPos(gx, gy, gz));
        } else {
            this.setGuardPosition(null);
        }
        this.setProsperity(input.getIntOr("Prosperity", 0));
        this.builtStructuresCount = input.getIntOr("BuiltStructuresCount", 0);
        int plotCount = input.getIntOr("OccupiedPlotsCount", 0);
        this.occupiedVillagePlots.clear();
        for (int i = 0; i < plotCount; i++) {
            int minX = input.getIntOr("Plot_" + i + "_minX", 0);
            int minZ = input.getIntOr("Plot_" + i + "_minZ", 0);
            int maxX = input.getIntOr("Plot_" + i + "_maxX", 0);
            int maxZ = input.getIntOr("Plot_" + i + "_maxZ", 0);
            this.occupiedVillagePlots.add(new StructureBlueprint.VillagePlot(minX, minZ, maxX, maxZ));
        }
        if (input.getBooleanOr("HasCampCenter", false)) {
            int cx = input.getIntOr("CampCenterX", 0);
            int cy = input.getIntOr("CampCenterY", 0);
            int cz = input.getIntOr("CampCenterZ", 0);
            this.campCenterPos = new BlockPos(cx, cy, cz);
        } else {
            this.campCenterPos = null;
        }
        if (input.getBooleanOr("HasActiveProject", false)) {
            String typeName = input.getStringOr("ProjectType", "");
            if (!typeName.isEmpty()) {
                try {
                    StructureBlueprint.StructureType type = StructureBlueprint.StructureType.valueOf(typeName);
                    BlockPos origin = new BlockPos(input.getIntOr("ProjectOriginX", 0), input.getIntOr("ProjectOriginY", 0), input.getIntOr("ProjectOriginZ", 0));
                    this.activeVillageProject = new VillageProject(type, origin);
                    this.activeVillageProject.setTerraformed(input.getBooleanOr("ProjectTerraformed", false));
                    int bIndex = input.getIntOr("ProjectBuildIndex", 0);
                    for (int i = 0; i < bIndex && !this.activeVillageProject.isCompleted(); i++) {
                        this.activeVillageProject.advanceBuild();
                    }
                } catch (IllegalArgumentException ignored) {}
            }
        } else {
            this.activeVillageProject = null;
        }
        String avengeKillerStr = input.getStringOr("AvengeQueenKillerUUID", "");
        if (!avengeKillerStr.isEmpty()) {
            try {
                this.avengeQueenKillerUUID = UUID.fromString(avengeKillerStr);
            } catch (IllegalArgumentException ignored) {}
        } else {
            this.avengeQueenKillerUUID = null;
        }
        this.powerStruggleDelayTicks = input.getIntOr("PowerStruggleDelayTicks", 0);
        String pendingTribe = input.getStringOr("PendingPowerStruggleTribe", "");
        this.pendingPowerStruggleTribe = pendingTribe.isEmpty() ? null : pendingTribe;
        if (input.getBooleanOr("HasPendingPowerStruggleCenter", false)) {
            double cx = input.getDoubleOr("PendingCenterX", 0.0);
            double cy = input.getDoubleOr("PendingCenterY", 0.0);
            double cz = input.getDoubleOr("PendingCenterZ", 0.0);
            this.pendingPowerStruggleCenter = new Vec3(cx, cy, cz);
        } else {
            this.pendingPowerStruggleCenter = null;
        }
        this.setReincarnating(input.getBooleanOr("IsReincarnating", false));
        this.reincarnationTicksRemaining = input.getIntOr("ReincarnationTicks", 0);
        this.pendingReincarnatedQueenName = input.getStringOr("PendingReincarnatedQueenName", "");
        String initStr = input.getStringOr("ReincarnationInitiatorUUID", "");
        if (!initStr.isEmpty()) {
            try {
                this.reincarnationInitiatorPlayerUUID = UUID.fromString(initStr);
            } catch (IllegalArgumentException ignored) {}
        } else {
            this.reincarnationInitiatorPlayerUUID = null;
        }

        if (this.isOwned() || this.isAnyQueen() || this.hasCustomName()) {
            this.setPersistenceRequired();
        }
    }

}
