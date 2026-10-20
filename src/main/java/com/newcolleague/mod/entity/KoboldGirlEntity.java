package com.newcolleague.mod.entity;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.util.GeckoLibUtil;
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
import com.newcolleague.mod.network.QueenDialogueActionPayload;
import com.newcolleague.mod.network.OpenQueenDialoguePayload;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.SwordItem;
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

public class KoboldGirlEntity extends PathfinderMob implements GeoEntity, Merchant {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // Aquí las animaciones
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");

    // Mecanica de Hambre y Saturación
    private static final EntityDataAccessor<Integer> DATA_FOOD_LEVEL =
            SynchedEntityData.defineId(KoboldGirlEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_SATURATION_LEVEL =
            SynchedEntityData.defineId(KoboldGirlEntity.class, EntityDataSerializers.FLOAT);

    // Sincronización para la variación de Reina (osea true) o Súbdita (osea false)
    private static final EntityDataAccessor<Boolean> DATA_IS_QUEEN =
            SynchedEntityData.defineId(KoboldGirlEntity.class, EntityDataSerializers.BOOLEAN);

    // Nombre de la Tribu
    private static final EntityDataAccessor<String> DATA_TRIBE_NAME =
            SynchedEntityData.defineId(KoboldGirlEntity.class, EntityDataSerializers.STRING);

    // El estado de Rebelde (resistencia tras perder a su Reina en duelo a muerte con cuchillo proximamente magia)
    private static final EntityDataAccessor<Boolean> DATA_IS_REBEL =
            SynchedEntityData.defineId(KoboldGirlEntity.class, EntityDataSerializers.BOOLEAN);

    // Guerra civil por el poder (si la tribu se queda sin reina y solo sobrevivirá la más fuerte)
    private static final EntityDataAccessor<Boolean> DATA_IS_IN_POWER_STRUGGLE =
            SynchedEntityData.defineId(KoboldGirlEntity.class, EntityDataSerializers.BOOLEAN);

    // Reina Sobreviviente (ganadora de la guerra civil, pq todas las demás en su tribu murieron)
    private static final EntityDataAccessor<Boolean> DATA_IS_SURVIVOR_QUEEN =
            SynchedEntityData.defineId(KoboldGirlEntity.class, EntityDataSerializers.BOOLEAN);

    // Aquí se sincronizan las profesiones (Granjera, Guardia, Vigilante y Cazadora por ahora)
    private static final EntityDataAccessor<String> DATA_PROFESSION =
            SynchedEntityData.defineId(KoboldGirlEntity.class, EntityDataSerializers.STRING);

    // Mecanica de volverse rey de la tribu Kobold... para nada es esclavitud (UUID en formato String o vacío si es salvaje)
    private static final EntityDataAccessor<String> DATA_OWNER_UUID =
            SynchedEntityData.defineId(KoboldGirlEntity.class, EntityDataSerializers.STRING);

    // Postura de mando del gobernante (WORK, FOLLOW, GUARD)
    private static final EntityDataAccessor<String> DATA_COMMAND_STANCE =
            SynchedEntityData.defineId(KoboldGirlEntity.class, EntityDataSerializers.STRING);

    // Nombre de pila personalizado asignado por el gobernante con etiqueta de nombre (ej. "Kira")
    private static final EntityDataAccessor<String> DATA_CUSTOM_GIVEN_NAME =
            SynchedEntityData.defineId(KoboldGirlEntity.class, EntityDataSerializers.STRING);

    // Nivel / Rango de maestría de profesión (1 = básico, 2 = veterana/mejorada)
    private static final EntityDataAccessor<Integer> DATA_PROFESSION_TIER =
            SynchedEntityData.defineId(KoboldGirlEntity.class, EntityDataSerializers.INT);

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
        HUNTER("Cazadora");

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
        MISC
    }

    // Registro global de cofres categorizados en el servidor (Posición canónica -> Categoría)
    private static final Map<BlockPos, ChestCategory> CLAIMED_CHEST_PURPOSES = new ConcurrentHashMap<>();

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

    public KoboldGirlEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setCanPickUpLoot(true);

        // Configuración de navegación segura y evasión de peligros
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
        return !this.isSleeping() && !this.isSpectatingDuel() && super.isPushable();
    }

    @Override
    public void push(Entity entity) {
        if (!this.isSleeping() && !this.isSpectatingDuel()) {
            super.push(entity);
        }
    }

    @Override
    public void push(double xa, double ya, double za) {
        if (!this.isSleeping() && !this.isSpectatingDuel()) {
            super.push(xa, ya, za);
        }
    }

    @Override
    public void push(Vec3 vec) {
        if (!this.isSleeping() && !this.isSpectatingDuel()) {
            super.push(vec);
        }
    }

    @Override
    public void knockback(double power, double xd, double zd, DamageSource source, float damage, boolean comesFromEffect) {
        if (!this.isSleeping() && !this.isSpectatingDuel()) {
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
        if (this.isSleeping()) {
            this.setDeltaMovement(Vec3.ZERO);
        }
        super.aiStep();
        if (this.isSleeping()) {
            this.setDeltaMovement(Vec3.ZERO);
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
                    KoboldGirlEntity subdita = new KoboldGirlEntity(ModEntities.KOBOLD_GIRL.get(), level.getLevel());
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

    public @Nullable BlockPos getGuardPosition() {
        return this.guardPosition;
    }

    public void setGuardPosition(@Nullable BlockPos pos) {
        this.guardPosition = pos;
    }

    public void updateRulerCustomName() {
        if (!this.isOwned()) return;
        String given = this.getCustomGivenName();
        String profName = this.isAnyQueen() ? this.getQueenTitle() : this.getProfession().getDisplayName();
        if (this.getProfessionTier() >= 2) {
            profName += " ⭐";
        }
        if (this.isRebel()) {
            profName += " Rebelde";
            this.setCustomName(Component.literal(given.isEmpty() ? profName : given + " [" + profName + "]").withStyle(ChatFormatting.RED));
        } else {
            profName += " Leal";
            ChatFormatting color = this.isAnyQueen() ? ChatFormatting.GOLD : ChatFormatting.GREEN;
            this.setCustomName(Component.literal(given.isEmpty() ? profName : given + " [" + profName + "]").withStyle(color));
        }
        this.setCustomNameVisible(true);
    }

    public boolean isDefender() {
        KoboldProfession prof = this.getProfession();
        return prof == KoboldProfession.GUARD || prof == KoboldProfession.VIGILANT;
    }

    public boolean isPatrolSquadLeader() {
        if (this.isAnyQueen()) {
            return true;
        }
        return this.patrolSquadIndexInGroup == 0 || this.getUUID().equals(this.patrolSquadLeaderUUID);
    }

    public KoboldGirlEntity getPatrolSquadLeaderEntity() {
        if (this.isPatrolSquadLeader()) {
            return this;
        }
        if (this.patrolSquadLeaderUUID != null && this.level() instanceof ServerLevel serverLevel) {
            Entity ent = serverLevel.getEntity(this.patrolSquadLeaderUUID);
            if (ent instanceof KoboldGirlEntity k && k.isAlive() && k.isDefender()) {
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
        if (!this.isDefender() || this.level().isClientSide()) {
            this.patrolSquadLeaderUUID = null;
            this.patrolSquadId = 0;
            this.patrolSquadTotalGroups = 1;
            this.patrolSquadIndexInGroup = 0;
            this.patrolSquadSize = 1;
            return;
        }

        // Buscar todos los defensores vivos de la misma tribu en un radio de 48 bloques
        List<KoboldGirlEntity> defenders = this.level().getEntitiesOfClass(
                KoboldGirlEntity.class,
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

        List<KoboldGirlEntity> guards = new ArrayList<>();
        List<KoboldGirlEntity> vigilantes = new ArrayList<>();

        for (KoboldGirlEntity d : defenders) {
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

        List<List<KoboldGirlEntity>> squads = new ArrayList<>(k);
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
            List<KoboldGirlEntity> squad = squads.get(s);
            squad.sort((a, b) -> {
                if (a.getProfession() == KoboldProfession.GUARD && b.getProfession() != KoboldProfession.GUARD) return -1;
                if (a.getProfession() != KoboldProfession.GUARD && b.getProfession() == KoboldProfession.GUARD) return 1;
                return a.getUUID().compareTo(b.getUUID());
            });

            for (int idx = 0; idx < squad.size(); idx++) {
                KoboldGirlEntity member = squad.get(idx);
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
    }

    public boolean isSubdita() {
        return !this.isAnyQueen();
    }

    public boolean isAnyQueen() {
        return this.isQueen() || this.isSurvivorQueen();
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
    }

    public @Nullable KoboldGirlEntity getTribeQueen() {
        if (!this.hasTribe()) return null;
        String myTribe = this.getTribeName();
        List<KoboldGirlEntity> queens = this.level().getEntitiesOfClass(
                KoboldGirlEntity.class,
                this.getBoundingBox().inflate(128.0D),
                k -> k.isAnyQueen() && k.isAlive() && myTribe.equals(k.getTribeName())
        );
        return queens.isEmpty() ? null : queens.get(0);
    }

    public @Nullable KoboldGirlEntity getTribeQueen(ServerLevel level) {
        return this.getTribeQueen();
    }

    public boolean isOwned() {
        if (this.isRebel()) return false;
        if (!this.entityData.get(DATA_OWNER_UUID).isEmpty()) return true;
        if (this.hasTribe() && this.level() instanceof ServerLevel serverLevel) {
            KoboldGirlEntity queen = this.getTribeQueen(serverLevel);
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
            KoboldGirlEntity queen = this.getTribeQueen(serverLevel);
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
            KoboldGirlEntity queen = this.getTribeQueen(serverLevel);
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
            if (this.isInQueenDuel() && this.getTarget() instanceof KoboldGirlEntity rival) {
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

    public static @Nullable KoboldGirlEntity getActivePlayerDuelQueen(Player player) {
        if (player == null || !(player.level() instanceof ServerLevel serverLevel)) return null;
        List<KoboldGirlEntity> queens = serverLevel.getEntitiesOfClass(
                KoboldGirlEntity.class,
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
        if (this.isSpectatingDuel) {
            return true;
        }
        QueenDuel duel = getNearbyQueenDuel(this.level(), this.position(), 28.0D);
        if (duel != null && duel.isActive()) {
            if (this.hasLineOfSight(duel.queen1) || (duel.queen2 != null && this.hasLineOfSight(duel.queen2))) {
                return true;
            }
        }
        return false;
    }

    public void setSpectatingDuel(boolean spectating) {
        this.isSpectatingDuel = spectating;
    }

    //Ruta pacifista en caso de que el jugador no quiera hacer duelo a muerte con cuchillos contra la reina por el dominio de la tribu

    private boolean pacifistRouteChosen = false;
    private @Nullable MerchantOffers pacifistOffers = null;
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
        }
        return this.pacifistOffers;
    }

    @Override
    public void overrideOffers(MerchantOffers offers) {
        this.pacifistOffers = offers;
    }

    @Override
    public void notifyTrade(MerchantOffer offer) {
        offer.increaseUses();
        this.ambientSoundTime = -this.getAmbientSoundInterval();
        this.playSound(this.getNotifyTradeSound(), this.getSoundVolume(), this.getVoicePitch());
        this.completedPacifistTrades++;

        if (this.tradingPlayer instanceof ServerPlayer serverPlayer) {
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
            }

            Component speaker = Component.literal("<" + this.getQueenTitle() + "> ").withStyle(ChatFormatting.GOLD);
            if (completed < total) {
                int percent = completed * 10;
                Component progressMsg = Component.literal("¡Gran ofrenda para nuestro pueblo! Confianza: " + percent + "% (" + completed + "/" + total + " tributos).")
                        .withStyle(ChatFormatting.GREEN);
                serverPlayer.sendSystemMessage(speaker.copy().append(progressMsg));
            } else {
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

        // 1. Armas para la Guardia: 2 Espadas de Hierro (aunque al parecer la reina hace descuento y lo baja a uno todo XD)
        offers.add(createSingleTributeOffer(
                Items.IRON_SWORD, 2,
                null, 0,
                "Espadas para la Guardia",
                "Armas forjadas para defender a la tribu contra criaturas hostiles"
        ));

        // 2. Armamento a Distancia: 2 Ballestas
        offers.add(createSingleTributeOffer(
                Items.CROSSBOW, 2,
                null, 0,
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

        // 4. Herramientas de Silvicultura: 2 Hachas de Hierro
        offers.add(createSingleTributeOffer(
                Items.IRON_AXE, 2,
                null, 0,
                "Hachas de Silvicultura",
                "Herramientas para talar madera y construir refugios para el pueblo"
        ));

        // 5. Armaduras de Protección: 2 Petos de Hierro
        offers.add(createSingleTributeOffer(
                Items.IRON_CHESTPLATE, 2,
                null, 0,
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

        ItemStack reward = new ItemStack(Items.GOLD_NUGGET, 1);
        reward.set(DataComponents.CUSTOM_NAME, Component.literal("Punto de Confianza: " + tributeName)
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        reward.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal(description).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
                Component.literal("+10% Confianza y Alianza con la Reina Kobold").withStyle(ChatFormatting.YELLOW),
                Component.literal("Sello de gratitud y respeto tribal").withStyle(ChatFormatting.DARK_GREEN)
        )));

        ItemCost costA = new ItemCost(itemA, countA);
        Optional<ItemCost> costB = (itemB != null && countB > 0)
                ? Optional.of(new ItemCost(itemB, countB))
                : Optional.empty();

        return new MerchantOffer(costA, costB, reward, 0, 1, 5, 0.0F);
    }

    public void resolvePlayerPacifistVictory(Player player) {
        if (player == null || !(this.level() instanceof ServerLevel level)) return;
        if (this.isOwnedBy(player)) return;

        this.setOwnerUUID(player.getUUID());
        this.setRebel(false);
        this.setHealth(this.getMaxHealth());

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
        List<KoboldGirlEntity> subditas = level.getEntitiesOfClass(
                KoboldGirlEntity.class,
                this.getBoundingBox().inflate(128.0D),
                k -> !k.isAnyQueen() && k.isAlive() && (tribeName.isEmpty() || k.getTribeName().equals(tribeName))
        );

        int loyalCount = 0;
        for (KoboldGirlEntity subdita : subditas) {
            subdita.setOwnerUUID(player.getUUID());
            subdita.setRebel(false);
            subdita.setTarget(null);
            subdita.unequipHands();
            String baseName = subdita.getProfession() != KoboldProfession.NONE
                    ? subdita.getProfession().getDisplayName()
                    : "Kobold Girl";
            subdita.setCustomName(Component.literal("[Leal] " + baseName).withStyle(ChatFormatting.GREEN));
            subdita.setCustomNameVisible(true);
            level.sendParticles(ParticleTypes.HEART, subdita.getX(), subdita.getY() + 1.0D, subdita.getZ(), 5, 0.3D, 0.3D, 0.3D, 0.05D);
            loyalCount++;
        }

        player.sendSystemMessage(Component.literal("🎉 ¡Alianza pacífica completada! " + loyalCount + " Kobold Girl(s) de la tribu se han unido a ti con total devoción.")
                .withStyle(ChatFormatting.GREEN));
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
    private boolean tribeWitnessedBaculo = false;
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
                List<KoboldGirlEntity> tribeMates = serverLevel.getEntitiesOfClass(
                        KoboldGirlEntity.class,
                        this.getBoundingBox().inflate(64.0D),
                        k -> Objects.equals(k.getTribeName(), this.getTribeName())
                );
                for (KoboldGirlEntity mate : tribeMates) {
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
    }

    public boolean isInQueenDuel() {
        if (!this.isAnyQueen() || !this.hasTribe()) return false;
        LivingEntity target = this.getTarget();
        return target instanceof KoboldGirlEntity rival
                && rival.isAnyQueen()
                && rival.isAlive()
                && rival.hasTribe()
                && !this.getTribeName().equals(rival.getTribeName());
    }
        //Aquí pasito a pasito que después me enredo
    public boolean canFightKobold(KoboldGirlEntity other) {
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
        // 2. Si la Reina leal está jodiendo a una súbdita rebelde o la rebelde jode a la Reina leal
        if ((this.isAnyQueen() && this.isOwned() && other.isRebel()) || (this.isRebel() && other.isAnyQueen() && other.isOwned())) {
            return true;
        }
        // 3. Súbditas leales al jugador NUNCA atacan a rebeldes de su pueblo (la Reina se encarga con un solo coñazo disciplinario)
        if ((this.isOwned() && !this.isAnyQueen() && other.isRebel()) || (this.isRebel() && other.isOwned() && !other.isAnyQueen())) {
            return false;
        }
        // 4. Si esta Kobold es rebelde, solo ataca a kobolds de tribus rivales
        if (this.isRebel()) {
            if (other.isRebel()) return false;
            return !this.getTribeName().equals(other.getTribeName());
        }
        // 5. Si la otra Kobold es rebelde pero esta no es leal al jugador osea es de tribu rival salvaje
        if (other.isRebel()) {
            return !this.isRebel() && !this.isOwned();
        }
        return false;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.FOLLOW_RANGE, 128.0D);
    }

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

        // 10. Cocinar carne cruda en fogatas (Exclusivo Cazadoras)
        this.goalSelector.addGoal(10, new KoboldCookMeatAtCampfireGoal(this, 1.15D));

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
                    if (this.isInQueenDuel() && this.getTarget() instanceof KoboldGirlEntity rivalQueen) {
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
            if (this.getOffhandItem().isEmpty() && this.hasShieldInInventory()) {
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
        } else {
            // Fuera de combate, caza y cosecha a manos libres (osea sin herramientas, está usando solo sus manitos o garritas XD)
            if (!this.getMainHandItem().isEmpty() || !this.getOffhandItem().isEmpty()) {
                this.unequipHands();
            }
        }

        this.tickHunger(level);
        this.tickBedBehavior(level);

        // Si está espectando un duelo activo, mantenerla libre de fuego
        if (this.isSpectatingDuel()) {
            this.clearFire();
        }

        // Hornear pan y auto-equipar armaduras (solo en slots de armadura) periódicamente cada 1 segundo
        if (this.tickCount % 20 == 0) {
            this.tryCraftBread();
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
        this.clearAssignedArmorStand();
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
        List<KoboldGirlEntity> nearbyKobolds = this.level().getEntitiesOfClass(
                KoboldGirlEntity.class,
                stand.getBoundingBox().inflate(64.0D),
                k -> k != this && k.isAlive()
        );
        for (KoboldGirlEntity k : nearbyKobolds) {
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

        // Revisar también si otra Kobold Girl viva en el área tiene asignada esta misma cama
        List<KoboldGirlEntity> nearbyKobolds = this.level().getEntitiesOfClass(
                KoboldGirlEntity.class,
                new AABB(bedHeadPos).inflate(320.0D),
                k -> k != this && k.isAlive()
        );
        for (KoboldGirlEntity k : nearbyKobolds) {
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
        KoboldGirlEntity winningQueen = null;
        if (this.getTarget() instanceof KoboldGirlEntity rival && rival.isQueen() && rival.isAlive() && !rival.getTribeName().equals(this.getTribeName())) {
            winningQueen = rival;
        } else if (damageSource.getEntity() instanceof KoboldGirlEntity attacker && attacker.isQueen() && attacker.isAlive() && !attacker.getTribeName().equals(this.getTribeName())) {
            winningQueen = attacker;
        } else {
            List<KoboldGirlEntity> nearbyQueens = level.getEntitiesOfClass(
                    KoboldGirlEntity.class,
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
            this.triggerTribalPowerStruggle(level, this.getTribeName(), this.position());
        }
    }

    public void triggerTribalPowerStruggle(ServerLevel level, String tribeName, Vec3 center) {
        if (tribeName == null || tribeName.isEmpty()) return;

        List<KoboldGirlEntity> candidates = level.getEntitiesOfClass(
                KoboldGirlEntity.class,
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

        for (KoboldGirlEntity candidate : candidates) {
            if (candidate.isSleeping()) {
                candidate.stopSleeping();
            }
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
        List<KoboldGirlEntity> remaining = level.getEntitiesOfClass(
                KoboldGirlEntity.class,
                this.getBoundingBox().inflate(64.0D),
                k -> k != this && k.isAlive() && k.isInPowerStruggle() && (tribe.isEmpty() || k.getTribeName().equals(tribe))
        );

        if (remaining.size() == 1) {
            coronateSurvivorQueen(level, remaining.get(0), tribe);
        }
    }

    public static void coronateSurvivorQueen(ServerLevel level, KoboldGirlEntity winner, String tribeName) {
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

    private void resolveQueenDuelDefeat(ServerLevel level, KoboldGirlEntity winningQueen) {
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
        List<KoboldGirlEntity> defeatedSubditas = level.getEntitiesOfClass(
                KoboldGirlEntity.class,
                this.getBoundingBox().inflate(48.0D),
                k -> !k.isQueen() && k.isAlive() && k.getTribeName().equals(defeatedTribe)
        );

        if (defeatedSubditas.isEmpty()) return;

        List<KoboldGirlEntity> shuffled = new ArrayList<>(defeatedSubditas);
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
            KoboldGirlEntity subdita = shuffled.get(i);
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
        public final KoboldGirlEntity queen1;
        public final LivingEntity queen2;

        public QueenDuel(KoboldGirlEntity queen1, LivingEntity queen2) {
            this.queen1 = queen1;
            this.queen2 = queen2;
        }

        public Vec3 getCenter() {
            Vec3 c1 = this.queen1.getDuelArenaCenter();
            if (c1 != null) return c1;
            if (this.queen2 instanceof KoboldGirlEntity q2) {
                Vec3 c2 = q2.getDuelArenaCenter();
                if (c2 != null) return c2;
            }
            return this.queen1.position().add(this.queen2.position()).scale(0.5D);
        }

        public boolean isActive() {
            if (!this.queen1.isAlive() || !this.queen2.isAlive()) return false;
            if (this.queen2 instanceof KoboldGirlEntity rivalQueen) {
                return this.queen1.isInQueenDuel() && rivalQueen.isInQueenDuel();
            } else if (this.queen2 instanceof Player player) {
                return this.queen1.isInPlayerDuelWith(player);
            }
            return false;
        }
    }

    public static QueenDuel getNearbyQueenDuel(Level level, Vec3 pos, double radius) {
        List<KoboldGirlEntity> nearbyQueens = level.getEntitiesOfClass(
                KoboldGirlEntity.class,
                new AABB(pos.x - radius, pos.y - radius, pos.z - radius, pos.x + radius, pos.y + radius, pos.z + radius),
                q -> q.isAnyQueen() && q.isAlive() && (q.isInQueenDuel() || q.isInPlayerDuel())
        );
        for (KoboldGirlEntity q1 : nearbyQueens) {
            if (q1.isInPlayerDuel() && q1.getPlayerDuelTargetUUID() != null) {
                Player player = level.getPlayerByUUID(q1.getPlayerDuelTargetUUID());
                if (player != null && player.isAlive()) {
                    return new QueenDuel(q1, player);
                }
            } else if (q1.getTarget() instanceof KoboldGirlEntity q2 && q2.isAnyQueen() && q2.isAlive() && q2.isInQueenDuel()) {
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
        List<KoboldGirlEntity> spectators = level.getEntitiesOfClass(
                KoboldGirlEntity.class,
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

        // Si tiene hambre y no está durmiendo, intentar comer de su inventario cada 30 ticks (1.5 segundos)
        if (!this.isSleeping() && this.needsFood() && this.hungerTickTimer % 30 == 0) {
            this.tryEatFromInventory();
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
                this.eat(stack, food);
                return true;
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

        KoboldGirlEntity queen = this.isRebel() ? this.getTribeQueen() : null;
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
        if (this.hasExcessFoodToStore()) {
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
            if (target instanceof KoboldGirlEntity otherKobold) {
                if (this.canFightKobold(otherKobold)) {
                    return true;
                }
            } else if (!this.isHunting()) {
                return true;
            }
        }
        LivingEntity hurtBy = this.getLastHurtByMob();
        if (hurtBy != null && hurtBy.isAlive()) {
            if (hurtBy instanceof KoboldGirlEntity otherKobold) {
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
        return stack.has(DataComponents.KINETIC_WEAPON) || stack.is(ItemTags.SPEARS) || stack.getItem() instanceof TridentItem;
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
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.inventory.getItem(i);
            if (stack.is(ItemTags.ARROWS)) {
                return this.inventory.removeItem(i, 1);
            }
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

    public void autoEquipWeaponsAndShield() {
        if (this.level().isClientSide() || this.isSleeping()) return;
        if (this.isAnyQueen()) {
            this.unequipHands();
            return;
        }
        KoboldProfession prof = this.getProfession();
        if (prof == KoboldProfession.GUARD) {
            this.equipBestWeapon();
            if (!isShield(this.getOffhandItem())) {
                this.equipShieldFromInventory();
            }
        } else if (prof == KoboldProfession.VIGILANT) {
            this.equipBestWeapon();
            this.unequipShield();
        } else if (prof == KoboldProfession.HUNTER) {
            this.equipBestWeapon();
            this.unequipShield();
        } else if (prof == KoboldProfession.FARMER) {
            if (this.isMorningWorkTime()) {
                this.equipHoeForHarvesting();
            } else {
                this.equipBestWeapon();
            }
            this.unequipShield();
        } else {
            if (this.getMainHandItem().isEmpty()) {
                this.equipBestWeapon();
            }
            if (!isShield(this.getOffhandItem())) {
                this.equipShieldFromInventory();
            }
        }
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
        // 0. Si está durmiendo, clic derecho la despierta
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
        boolean understands = understandsKoboldLanguage(player);
        boolean isAlly = this.isOwnedBy(player);

        if (!understands || !isAlly) {
            // Si es una reina con la ruta pacifista activa y el jugador entiende el idioma:
            // Abrir directamente la interfaz de tradeo para entregar ofrendas sin necesidad de usar el báculo
            if (this.isAnyQueen() && this.isPacifistRouteChosen() && understands && !isHoldingBaculo(player)) {
                if (!this.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
                    this.updateRawMeatOfferForPlayer(serverPlayer);
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

        // 5.3. Mejora / Ascenso de Profesión a Rango Veterana (Tier 2 ⭐) entregando Esmeralda o Diamante
        if (!this.isAnyQueen() && (itemInHand.is(Items.EMERALD) || itemInHand.is(Items.DIAMOND))) {
            if (this.getProfession() != KoboldProfession.NONE) {
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
                        serverPlayer.sendSystemMessage(Component.literal("§6¡" + name + " ha ascendido a Rango Veterana (Tier 2 ⭐)! Ahora desempeñará sus labores con maestría y eficiencia mejorada."));
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
            } else if ((itemInHand.getItem() instanceof BowItem || itemInHand.getItem() instanceof CrossbowItem) && this.getProfession() != KoboldProfession.HUNTER) {
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
            } else if ((itemInHand.getItem() instanceof SwordItem || itemInHand.getItem() instanceof MaceItem || itemInHand.getItem() instanceof TridentItem || isShield(itemInHand)) && this.getProfession() != KoboldProfession.GUARD) {
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
            }
        }

        // 5.5. Clic derecho con mano vacía:
        // Si es la Reina -> Abrir Menú de Gobernación Tribal
        // Si es una súbdita -> Alternar Postura de Mando (RUTINA -> SEGUIR -> GUARDIA)
        if (itemInHand.isEmpty()) {
            if (this.isAnyQueen()) {
                if (!this.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
                    PacketDistributor.sendToPlayer(serverPlayer, new OpenQueenDialoguePayload(this.getId(), this.getTribeName(), this.getQueenTitle(), true));
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

    // Frases de extrañeza / intriga de las Kobold Girls hacia un forastero
    private static final String[] KOBOLD_STRANGER_DIALOGUES = {
        "¿Que hace un forastero aquí?",
        "Un humano llegando a nuestro territorio, eso es nuevo.",
        "¿Por qué nos miras tanto? Aléjate...",
        "Nuestra reina vigila atentamente a los intrusos.",
        "No confío en los humanos... ¿qué buscas aquí?",
        "¿Nos entiendes? Claro que no, solo eres un extraño.",
        "Cuidado por dónde pisas, forastero.",
        "Nuestras guerreras te están vigilando desde las sombras.",
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
        "¡Nuestra reina cayó, pero mi espíritu no se doblegará!",
        "¡Fuera de nuestras tierras, usurpador!",
        "¡Prefiero el exilio antes que servir a un forastero!"
    };

    // Frases de súbditas rebeldes hacia un jugador portador del báculo (reconocen el artefacto místico pero rechazan someterse)
    private static final String[] KOBOLD_REBEL_BACULO_DIALOGUES = {
        "¡Ese báculo emana un poder antiguo... pero aún así no te obedeceré!",
        "Reconozco el artefacto sagrado de nuestros ancestros, pero no a ti como gobernante.",
        "El báculo te protegerá de mi ira, forastero... pero jamás seré tu súbdita.",
        "¡No intentes darme órdenes ni con ese báculo en la mano!",
        "La vara de los ancestros te da derecho a pisar estas tierras, no a gobernarme."
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
                    Component msg = Component.literal("Si deseas ganarte la lealtad de mi tribu con sabiduría, ayúdanos con estas ofrendas.")
                            .withStyle(ChatFormatting.GREEN);
                    player.sendSystemMessage(speaker.copy().append(msg));
                }

                this.playSound(SoundEvents.VILLAGER_YES, 0.9F, 1.0F);
                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.2D, this.getZ(), 8, 0.3D, 0.3D, 0.3D, 0.05D);
                }

                this.updateRawMeatOfferForPlayer(player);
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
        }
    }

    public void broadcastCommandStance(ServerPlayer player, CommandStance stance) {
        if (player == null || !(this.level() instanceof ServerLevel serverLevel)) return;
        List<KoboldGirlEntity> tribe = serverLevel.getEntitiesOfClass(
                KoboldGirlEntity.class,
                this.getBoundingBox().inflate(64.0D),
                k -> k.isAlive() && k.isOwnedBy(player) && Objects.equals(k.getTribeName(), this.getTribeName())
        );
        int count = 0;
        for (KoboldGirlEntity girl : tribe) {
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

        List<KoboldGirlEntity> tribe = serverLevel.getEntitiesOfClass(
                KoboldGirlEntity.class,
                this.getBoundingBox().inflate(64.0D),
                k -> k.isAlive() && k.isOwnedBy(player) && Objects.equals(k.getTribeName(), this.getTribeName())
        );

        int farmers = 0, hunters = 0, guards = 0, vigilants = 0, tier2Count = 0;
        for (KoboldGirlEntity k : tribe) {
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
        List<KoboldGirlEntity> tribe = serverLevel.getEntitiesOfClass(
                KoboldGirlEntity.class,
                this.getBoundingBox().inflate(64.0D),
                k -> k.isAlive() && k.isOwnedBy(player) && Objects.equals(k.getTribeName(), this.getTribeName())
        );

        int farmers = 0, hunters = 0, guards = 0, vigilants = 0, unassigned = 0, tier2Count = 0;
        int following = 0, guarding = 0, working = 0;

        for (KoboldGirlEntity k : tribe) {
            if (k.isAnyQueen()) continue;
            switch (k.getProfession()) {
                case FARMER -> farmers++;
                case HUNTER -> hunters++;
                case GUARD -> guards++;
                case VIGILANT -> vigilants++;
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
        player.sendSystemMessage(Component.literal("§7Población leal: §a" + tribe.size() + " Kobolds"));
        player.sendSystemMessage(Component.literal("§a  🌾 Granjeras: " + farmers + " | 🏹 Cazadoras: " + hunters));
        player.sendSystemMessage(Component.literal("§9  🛡 Guardias: " + guards + " | 👁 Vigilantes: " + vigilants));
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
        this.setOwnerUUID(player.getUUID());
        this.setRebel(false);
        this.setQueen(true);
        this.setProfession(KoboldProfession.NONE);
        this.setCustomName(Component.literal(this.getQueenTitle() + " Leal").withStyle(ChatFormatting.GOLD));
        this.setCustomNameVisible(true);

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
        List<KoboldGirlEntity> subditas = level.getEntitiesOfClass(
                KoboldGirlEntity.class,
                this.getBoundingBox().inflate(128.0D),
                k -> !k.isAnyQueen() && k.isAlive() && (tribeName.isEmpty() || k.getTribeName().equals(tribeName))
        );

        if (!subditas.isEmpty()) {
            List<KoboldGirlEntity> shuffled = new ArrayList<>(subditas);
            Collections.shuffle(shuffled, new java.util.Random(level.getRandom().nextLong()));

            int total = shuffled.size();
            int numRebels;
            if (total <= 2) {
                numRebels = level.getRandom().nextFloat() < 0.25F ? 1 : 0;
            } else if (total <= 4) {
                numRebels = 1;
            } else {
                numRebels = Math.max(1, (int) Math.round(total * 0.20D));
            }

            int actualRebels = 0;
            for (int i = 0; i < total; i++) {
                KoboldGirlEntity subdita = shuffled.get(i);
                if (i < numRebels) {
                    // Rebelde: se opone al nuevo gobernante pero no ataca de inmediato; se aleja
                    subdita.setRebel(true);
                    subdita.setOwnerUUID(null);
                    subdita.setTarget(null);
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
                    subdita.unequipHands();
                    subdita.setCustomName(Component.literal(subdita.getProfession().getDisplayName() + " Leal").withStyle(ChatFormatting.GREEN));
                    subdita.setCustomNameVisible(true);

                    level.sendParticles(ParticleTypes.HAPPY_VILLAGER, subdita.getX(), subdita.getY() + 1.2D, subdita.getZ(), 16, 0.4D, 0.4D, 0.4D, 0.05D);
                    level.sendParticles(ParticleTypes.HEART, subdita.getX(), subdita.getY() + 1.2D, subdita.getZ(), 6, 0.3D, 0.3D, 0.3D, 0.05D);
                    level.playSound(null, subdita.getX(), subdita.getY(), subdita.getZ(), SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 0.9F, 1.1F);
                }
            }

            if (actualRebels > 0) {
                player.sendSystemMessage(Component.literal("⚠ ¡" + actualRebels + " kobold(s) se niegan a someterse y se han rebelado contra ti!")
                        .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
            } else {
                player.sendSystemMessage(Component.literal("✨ Todas las súbditas de la tribu han jurado lealtad ante ti.")
                        .withStyle(ChatFormatting.GREEN));
            }
        }
    }

    public static int resetTribeOwnership(ServerPlayer player, double radius) {
        if (player == null || !(player.level() instanceof ServerLevel level)) return 0;
        AABB box = player.getBoundingBox().inflate(radius);
        List<KoboldGirlEntity> nearbyKobolds = level.getEntitiesOfClass(
                KoboldGirlEntity.class,
                box,
                KoboldGirlEntity::isAlive
        );
        if (nearbyKobolds.isEmpty()) return 0;

        Set<String> affectedTribes = new HashSet<>();
        int count = 0;

        for (KoboldGirlEntity k : nearbyKobolds) {
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
            List<KoboldGirlEntity> allTribeMembers = level.getEntitiesOfClass(
                    KoboldGirlEntity.class,
                    player.getBoundingBox().inflate(256.0D),
                    k -> k.isAlive() && affectedTribes.contains(k.getTribeName())
            );
            for (KoboldGirlEntity k : allTribeMembers) {
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

    /* =========================================================================
       METAS DE IA PERSONALIZADAS
       ========================================================================= */

    public static @Nullable Player findNearbyBaculoPlayer(KoboldGirlEntity mob, double radius) {
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

    // Meta: La Reina (o Reina Sobreviviente) es la única que se acerca al jugador que porta el báculo
    public static class KoboldQueenApproachBaculoGoal extends Goal {
        private final KoboldGirlEntity mob;
        private Player targetPlayer;
        private int repathTimer = 0;
        private int particleTimer = 0;
        private boolean hasArrived = false;

        public KoboldQueenApproachBaculoGoal(KoboldGirlEntity mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!this.mob.isAlive() || !this.mob.isAnyQueen() || this.mob.isInQueenDuel() || this.mob.isInPlayerDuel() || this.mob.isSleeping()) {
                return false;
            }
            this.targetPlayer = findNearbyBaculoPlayer(this.mob, 24.0D);
            return this.targetPlayer != null;
        }

        @Override
        public boolean canContinueToUse() {
            if (!this.mob.isAlive() || !this.mob.isAnyQueen() || this.mob.isInQueenDuel() || this.mob.isInPlayerDuel() || this.mob.isSleeping()) {
                return false;
            }
            if (this.targetPlayer == null || !this.targetPlayer.isAlive() || this.targetPlayer.isSpectator()) {
                return false;
            }
            if (!this.mob.isReveringBaculoHolder(this.targetPlayer)) {
                return false;
            }
            return this.mob.distanceToSqr(this.targetPlayer) <= 28.0D * 28.0D
                    && this.mob.hasLineOfSight(this.targetPlayer);
        }

        @Override
        public void start() {
            this.mob.markTribeWitnessedBaculo();
            this.mob.setApproachingBaculo(true);
            if (this.mob.getTarget() != null) {
                this.mob.setTarget(null);
            }
            if (this.mob.isUsingItem()) {
                this.mob.stopUsingItem();
            }
            this.mob.unequipHands();
            this.repathTimer = 0;
            this.particleTimer = 0;
            this.hasArrived = false;
        }

        @Override
        public void stop() {
            this.mob.markTribeWitnessedBaculo();
            this.mob.setApproachingBaculo(false);
            this.mob.getNavigation().stop();
            this.targetPlayer = null;
            this.hasArrived = false;
        }

        @Override
        public void tick() {
            if (this.targetPlayer == null) return;

            // Mantener las manos desarmadas para aproximarse con respeto
            if (!this.mob.getMainHandItem().isEmpty() || !this.mob.getOffhandItem().isEmpty()) {
                this.mob.unequipHands();
            }
            if (this.mob.getTarget() != null) {
                this.mob.setTarget(null);
            }

            // Mirar al jugador
            this.mob.getLookControl().setLookAt(this.targetPlayer, 30.0F, 30.0F);
            this.mob.setYBodyRot(this.mob.getYHeadRot());

            double distSq = this.mob.distanceToSqr(this.targetPlayer);
            double approachDist = 2.8D;
            double stopDistSq = approachDist * approachDist;

            if (distSq > stopDistSq) {
                this.repathTimer--;
                if (this.repathTimer <= 0) {
                    this.repathTimer = 10;
                    this.mob.getNavigation().moveTo(this.targetPlayer, 1.0D);
                }
            } else {
                this.mob.getNavigation().stop();
                if (!this.hasArrived) {
                    this.hasArrived = true;
                    if (this.mob.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                                this.mob.getX(), this.mob.getY() + 1.3D, this.mob.getZ(),
                                8, 0.3D, 0.3D, 0.3D, 0.05D);
                    }
                    this.mob.playSound(SoundEvents.VILLAGER_YES, 0.8F, 1.1F);
                }

                this.particleTimer++;
                if (this.particleTimer >= 50) {
                    this.particleTimer = 0;
                    if (this.mob.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                                this.mob.getX(), this.mob.getY() + 1.2D, this.mob.getZ(),
                                2, 0.25D, 0.25D, 0.25D, 0.02D);
                    }
                }
            }
        }
    }

    // Meta: Todas las Kobold Girls súbditas se quedan inmóviles mirando al jugador con el báculo
    public static class KoboldStareAtBaculoGoal extends Goal {
        private final KoboldGirlEntity mob;
        private Player targetPlayer;
        private int stareTimer = 0;
        private boolean isFirstTimeForTribe = false;

        public KoboldStareAtBaculoGoal(KoboldGirlEntity mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            if (!this.mob.isAlive() || this.mob.isAnyQueen() || this.mob.isSleeping() || this.mob.isRebel()
                    || this.mob.isInQueenDuel() || this.mob.isInPlayerDuel()
                    || getNearbyQueenDuel(this.mob.level(), this.mob.position(), 24.0D) != null) {
                return false;
            }
            // Si la tribu ya presenció el báculo anteriormente y está en cooldown, no interrumpir sus actividades normales
            if (this.mob.hasTribeWitnessedBaculo() && this.mob.getBaculoStareCooldown() > 0) {
                return false;
            }
            this.targetPlayer = findNearbyBaculoPlayer(this.mob, 20.0D);
            return this.targetPlayer != null;
        }

        @Override
        public boolean canContinueToUse() {
            if (!this.mob.isAlive() || this.mob.isAnyQueen() || this.mob.isSleeping() || this.mob.isRebel()
                    || this.mob.isInQueenDuel() || this.mob.isInPlayerDuel()
                    || getNearbyQueenDuel(this.mob.level(), this.mob.position(), 24.0D) != null) {
                return false;
            }
            if (this.targetPlayer == null || !this.targetPlayer.isAlive() || this.targetPlayer.isSpectator()) {
                return false;
            }
            if (!this.mob.isReveringBaculoHolder(this.targetPlayer)) {
                return false;
            }
            if (this.mob.distanceToSqr(this.targetPlayer) > 24.0D * 24.0D || !this.mob.hasLineOfSight(this.targetPlayer)) {
                return false;
            }
            // Si después de la primera vez ya la tribu presenció el báculo:
            // Solo mirar al jugador por unos segundos (70 ticks = 3.5s) y regresar a actividades normales
            if (!this.isFirstTimeForTribe && this.stareTimer >= 70) {
                return false;
            }
            return true;
        }

        @Override
        public void start() {
            this.stareTimer = 0;
            this.isFirstTimeForTribe = !this.mob.hasTribeWitnessedBaculo();
            this.mob.setStaringAtBaculo(true);
            this.mob.getNavigation().stop();
            if (this.mob.getTarget() != null) {
                this.mob.setTarget(null);
            }
            if (this.mob.isUsingItem()) {
                this.mob.stopUsingItem();
            }
            this.mob.setHarvesting(false);
            if (this.isFirstTimeForTribe) {
                this.mob.unequipHands();
            }
        }

        @Override
        public void stop() {
            this.mob.setStaringAtBaculo(false);
            this.mob.getNavigation().stop();
            this.mob.markTribeWitnessedBaculo();
            this.mob.setBaculoStareCooldown(400); // 20 segundos de cooldown antes de volver a mirar fijamente
            this.targetPlayer = null;
            this.mob.autoEquipWeaponsAndShield();
        }

        @Override
        public void tick() {
            if (this.targetPlayer == null) return;
            this.stareTimer++;

            // Si pasa un tiempo considerable en la primera vez (15 segundos), registrar que la tribu ya lo presenció
            if (this.stareTimer >= 300) {
                this.mob.markTribeWitnessedBaculo();
            }

            // Permanecer completamente inmóvil durante la observación
            this.mob.getNavigation().stop();

            if (this.isFirstTimeForTribe && (!this.mob.getMainHandItem().isEmpty() || !this.mob.getOffhandItem().isEmpty())) {
                this.mob.unequipHands();
            }
            if (this.mob.getTarget() != null) {
                this.mob.setTarget(null);
            }

            // Mirar fijamente al jugador portador del báculo
            this.mob.getLookControl().setLookAt(this.targetPlayer.getX(), this.targetPlayer.getEyeY(), this.targetPlayer.getZ(), 30.0F, 30.0F);
            this.mob.setYBodyRot(this.mob.getYHeadRot());
        }
    }

    // 1. Meta: Compartir recursos (comida y munición) según la necesidad entre Kobold Girls
    public static class ShareResourcesGoal extends Goal {
        private final KoboldGirlEntity mob;
        private KoboldGirlEntity targetKobold;
        private int cooldown = 0;
        private int shareTimeout = 0;

        public ShareResourcesGoal(KoboldGirlEntity mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.mob.isSleeping()) {
                return false;
            }
            if (this.cooldown > 0) {
                this.cooldown--;
                return false;
            }

            boolean hasFood = this.mob.hasFoodInInventory();
            boolean hasArrows = this.mob.hasArrows();

            if (!hasFood && !hasArrows) {
                return false;
            }

            // Buscar otra Kobold Girl cercana con línea de visión directa que no esté durmiendo
            List<KoboldGirlEntity> nearbyKobolds = this.mob.level().getEntitiesOfClass(
                    KoboldGirlEntity.class,
                    this.mob.getBoundingBox().inflate(12.0D, 4.0D, 12.0D),
                    k -> k != this.mob && k.isAlive() && !k.isSleeping() && this.mob.hasLineOfSight(k) && this.needsAssistance(k, hasFood, hasArrows)
            );

            if (nearbyKobolds.isEmpty()) {
                return false;
            }

            this.targetKobold = nearbyKobolds.get(0);
            return true;
        }

        private boolean needsAssistance(KoboldGirlEntity k, boolean iHaveFood, boolean iHaveArrows) {
            // No asistir ni compartir recursos con una Kobold Girl que esté durmiendo en la cama o durante duelos/guerra por el poder
            if (k.isSleeping() || this.mob.isInQueenDuel() || k.isInQueenDuel() || this.mob.isInPowerStruggle() || k.isInPowerStruggle()) {
                return false;
            }
            // Solo compartir recursos con compañeras de la misma tribu y que no sean rebeldes enemigas
            if (k.isRebel() != this.mob.isRebel() || (!k.getTribeName().isEmpty() && !this.mob.getTribeName().isEmpty() && !k.getTribeName().equals(this.mob.getTribeName()))) {
                return false;
            }
            // 1. Necesidad de comida: SOLO si la compañera tiene hambre por debajo de 18 Y no tiene nada de comida en su inventario
            if (iHaveFood && k.getFoodLevel() < 18 && !k.hasFoodInInventory()) {
                return true;
            }
            // 2. Necesidad de munición: si la compañera tiene un arma a distancia (en mano o inventario) pero NO tiene flechas
            if (iHaveArrows && k.hasRangedWeapon() && !k.hasArrows()) {
                return true;
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return !this.mob.isSleeping()
                    && this.targetKobold != null
                    && this.targetKobold.isAlive()
                    && !this.targetKobold.isSleeping()
                    && this.mob.hasLineOfSight(this.targetKobold)
                    && this.mob.distanceToSqr(this.targetKobold) < 144.0D
                    && this.needsAssistance(this.targetKobold, this.mob.hasFoodInInventory(), this.mob.hasArrows());
        }

        @Override
        public void start() {
            this.shareTimeout = 0;
            if (this.targetKobold != null) {
                this.mob.getNavigation().moveTo(this.targetKobold, 1.25D);
            }
        }

        @Override
        public void tick() {
            if (this.targetKobold == null || this.targetKobold.isSleeping() || this.mob.isSleeping()) {
                this.targetKobold = null;
                this.cooldown = 40;
                this.mob.getNavigation().stop();
                return;
            }

            this.shareTimeout++;
            if (this.shareTimeout > 120) {
                this.targetKobold = null;
                this.cooldown = 60;
                this.mob.getNavigation().stop();
                return;
            }

            this.mob.getLookControl().setLookAt(this.targetKobold, 30.0F, 30.0F);
            this.mob.getNavigation().moveTo(this.targetKobold, 1.25D);

            if (this.mob.distanceToSqr(this.targetKobold) < 4.0D) {
                boolean shared = false;

                // 1. Compartir comida: SOLO si la compañera tiene hambre < 18 Y no tiene comida en su inventario
                if (this.mob.hasFoodInInventory() && this.targetKobold.getFoodLevel() < 18 && !this.targetKobold.hasFoodInInventory()) {
                    for (int i = 0; i < this.mob.getInventory().getContainerSize(); i++) {
                        ItemStack stack = this.mob.getInventory().getItem(i);
                        if (stack.has(DataComponents.FOOD)) {
                            ItemStack foodToShare = this.mob.getInventory().removeItem(i, 1);
                            ItemStack remaining = this.targetKobold.getInventory().addItem(foodToShare);
                            if (!remaining.isEmpty()) {
                                this.mob.getInventory().addItem(remaining);
                            } else {
                                shared = true;
                                if (!this.targetKobold.isSleeping()) {
                                    this.targetKobold.tryEatFromInventory();
                                }
                            }
                            break;
                        }
                    }
                }

                // 2. Compartir flechas si la compañera tiene arma a distancia pero no tiene flechas, y esta Kobold tiene flechas
                if (this.mob.hasArrows() && this.targetKobold.hasRangedWeapon() && !this.targetKobold.hasArrows()) {
                    for (int i = 0; i < this.mob.getInventory().getContainerSize(); i++) {
                        ItemStack stack = this.mob.getInventory().getItem(i);
                        if (stack.is(ItemTags.ARROWS)) {
                            int count = stack.getCount();
                            int toShare = count > 1 ? Math.min(count / 2, 16) : 1;
                            ItemStack arrowsToShare = this.mob.getInventory().removeItem(i, toShare);
                            ItemStack remaining = this.targetKobold.getInventory().addItem(arrowsToShare);
                            if (!remaining.isEmpty()) {
                                this.mob.getInventory().addItem(remaining);
                            } else {
                                shared = true;
                                if (this.targetKobold.isThreatened() || this.targetKobold.isHunting()) {
                                    this.targetKobold.equipBestWeapon();
                                }
                            }
                            break;
                        }
                    }
                }

                if (shared) {
                    if (this.mob.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                                this.targetKobold.getX(), this.targetKobold.getY() + 1.2D, this.targetKobold.getZ(),
                                8, 0.25D, 0.25D, 0.25D, 0.05D);
                        serverLevel.sendParticles(ParticleTypes.HEART,
                                this.mob.getX(), this.mob.getY() + 1.2D, this.mob.getZ(),
                                3, 0.2D, 0.2D, 0.2D, 0.05D);
                        this.mob.playSound(SoundEvents.ITEM_PICKUP, 0.4F, 1.3F);
                    }
                }

                this.cooldown = 100; // 5 segundos de espera entre intercambios
                this.targetKobold = null;
                this.mob.getNavigation().stop();
            }
        }

        @Override
        public void stop() {
            this.targetKobold = null;
        }
    }

    // 2. Meta: Combate Adaptable según el arma equipada (o Magia Pura para la Reina)
    public static class KoboldAdaptiveCombatGoal extends Goal {
        private final KoboldGirlEntity mob;
        private int attackCooldown = 0;
        private int leapCooldown = 0;
        private boolean isLeapingForSmash = false;
        private int strafeChangeTimer = 0;
        private boolean strafeClockwise = false;

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
            this.isLeapingForSmash = false;
            this.queenSpellCooldown = 0;
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
            this.queenBeamChannelTicks = 0;
            this.queenFireballBurstRemaining = 0;
            if (this.mob.isUsingItem()) {
                this.mob.stopUsingItem();
            }
            this.mob.unequipHands();
            this.mob.getNavigation().stop();
        }

        @Override
        public void tick() {
            LivingEntity target = this.mob.getTarget();
            if (target == null || !target.isAlive() || target.getHealth() <= 0.0F) {
                if (this.mob.level() instanceof ServerLevel serverLevel) {
                    this.mob.onSlainTargetDrops(serverLevel, target);
                }
                return;
            }

            this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

            double dist = Math.sqrt(this.mob.distanceToSqr(target));

            if (this.attackCooldown > 0) this.attackCooldown--;
            if (this.leapCooldown > 0) this.leapCooldown--;

            // Si es la Reina: Táctica de combate puramente mágica (sin armas cuerpo a cuerpo ni a distancia)
            if (this.mob.isAnyQueen()) {
                this.tickQueenMagicTactic(target, dist);
                return;
            }

            // Uso defensivo del escudo si está amenazada por un enemigo no animal
            if (!(target instanceof Animal) && isShield(this.mob.getOffhandItem())) {
                if (dist <= 3.5D && this.attackCooldown > 4) {
                    if (!this.mob.isUsingItem()) {
                        this.mob.startUsingItem(InteractionHand.OFF_HAND);
                    }
                } else if (this.mob.isUsingItem()) {
                    this.mob.stopUsingItem();
                }
            } else if (this.mob.isUsingItem()) {
                this.mob.stopUsingItem();
            }

            ItemStack held = this.mob.getMainHandItem();

            // Comportamiento táctico según tipo de arma
            if (isRangedWeapon(held) && this.mob.hasArrows()) {
                this.tickRangedTactic(target, dist, held);
            } else if (isMace(held)) {
                this.tickMaceTactic(target, dist);
            } else if (isSpear(held)) {
                this.tickSpearTactic(target, dist);
            } else {
                this.tickDefaultMeleeTactic(target, dist, held);
            }
        }

        // --- Táctica de Arco / Ballesta (Rango y distancia) ---
        private void tickRangedTactic(LivingEntity target, double dist, ItemStack held) {
            // Si el objetivo está demasiado cerca (< 6 bloques), retroceder / mantener distancia
            if (dist < 6.0D) {
                Vec3 away = this.mob.position().subtract(target.position()).normalize();
                this.mob.getNavigation().moveTo(this.mob.getX() + away.x * 4.0D, this.mob.getY(), this.mob.getZ() + away.z * 4.0D, 1.4D);
            } else if (dist > 14.0D) {
                this.mob.getNavigation().moveTo(target, 1.25D);
            } else {
                // Distancia óptima (6 a 14 bloques): Strafing evasivo
                this.strafeChangeTimer++;
                if (this.strafeChangeTimer > 25) {
                    this.strafeClockwise = !this.strafeClockwise;
                    this.strafeChangeTimer = 0;
                }
                this.mob.getMoveControl().strafe(0.0F, this.strafeClockwise ? 0.5F : -0.5F);
            }

            // Disparo de flecha
            if (this.attackCooldown <= 0 && this.mob.hasLineOfSight(target)) {
                if (this.mob.isUsingItem()) {
                    this.mob.stopUsingItem();
                }
                ItemStack arrowStack = this.mob.consumeOneArrow();
                if (!arrowStack.isEmpty()) {
                    Arrow arrow = new Arrow(this.mob.level(), this.mob, arrowStack, held);
                    if (this.mob.getProfession() == KoboldProfession.VIGILANT) {
                        arrow.setBaseDamage(2.0D * 1.05D);
                    }
                    double dx = target.getX() - this.mob.getX();
                    double dy = target.getY(0.33D) - arrow.getY();
                    double dz = target.getZ() - this.mob.getZ();
                    double hDist = Math.sqrt(dx * dx + dz * dz);
                    arrow.shoot(dx, dy + hDist * 0.18D, dz, 1.6F, 2.0F);
                    SoundEvent shootSound = held.getItem() instanceof CrossbowItem ? SoundEvents.CROSSBOW_SHOOT : SoundEvents.ARROW_SHOOT;
                    this.mob.playSound(shootSound, 1.0F, 1.0F / (this.mob.getRandom().nextFloat() * 0.4F + 1.2F) + 0.5F);
                    this.mob.level().addFreshEntity(arrow);
                    this.attackCooldown = held.getItem() instanceof CrossbowItem ? 34 : 28;

                    // Si se quedó sin flechas, cambiar de arma de inmediato
                    if (!this.mob.hasArrows()) {
                        this.mob.equipBestWeapon();
                    }
                }
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
                    this.mob.doHurtTarget(serverLevel, target);
                    if (wasAlive && (!target.isAlive() || target.getHealth() <= 0.0F)) {
                        this.mob.onSlainTargetDrops(serverLevel, target);
                    }
                    if (held.is(ItemTags.AXES)) {
                        this.attackCooldown = 24;
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
            if (this.queenBeamChannelTicks > 0) {
                // Durante la canalización del rayo de guardián se queda concentrada mirando fijamente al objetivo
                this.mob.getNavigation().stop();
                this.mob.getLookControl().setLookAt(target, 90.0F, 90.0F);
            } else if (this.mob.isInQueenDuel() || this.mob.isInPlayerDuel()) {
                // Posicionamiento táctico en duelo: Mantener distancia suficiente (5.0 a 7.5 bloques)
                // para permitir el uso dinámico y constante de toda la variedad de ataques mágicos
                Vec3 arenaCenter = this.mob.getDuelArenaCenter();
                double barrierRadius = this.mob.getDuelArenaBarrierRadius();
                double myDistFromCenter = arenaCenter != null ? Math.sqrt(Math.pow(this.mob.getX() - arenaCenter.x, 2) + Math.pow(this.mob.getZ() - arenaCenter.z, 2)) : 0.0D;

                if (dist < 5.0D) {
                    // Demasiado cerca: Retroceder activamente y crear distancia de combate mágico
                    Vec3 away = this.mob.position().subtract(target.position()).normalize();
                    double targetX = this.mob.getX() + away.x * 3.5D;
                    double targetZ = this.mob.getZ() + away.z * 3.5D;
                    if (arenaCenter != null) {
                        double targetDist = Math.sqrt(Math.pow(targetX - arenaCenter.x, 2) + Math.pow(targetZ - arenaCenter.z, 2));
                        if (targetDist > barrierRadius - 0.4D) {
                            double scale = (barrierRadius - 0.4D) / targetDist;
                            targetX = arenaCenter.x + (targetX - arenaCenter.x) * scale;
                            targetZ = arenaCenter.z + (targetZ - arenaCenter.z) * scale;
                        }
                    }
                    this.mob.getNavigation().moveTo(targetX, this.mob.getY(), targetZ, 1.35D);
                    this.mob.getMoveControl().strafe(-0.5F, this.strafeClockwise ? 0.45F : -0.45F);
                } else if (dist > 8.0D) {
                    // Si está más lejos del rango ideal de la arena, avanzar suavemente
                    this.mob.getNavigation().moveTo(target, 1.15D);
                } else {
                    // Distancia ideal de combate (5.0 a 8.0 bloques): Detener navegación frontal y realizar evasión lateral
                    this.mob.getNavigation().stop();
                    this.strafeChangeTimer++;
                    if (this.strafeChangeTimer > 25) {
                        this.strafeClockwise = !this.strafeClockwise;
                        this.strafeChangeTimer = 0;
                    }
                    this.mob.getMoveControl().strafe(0.0F, this.strafeClockwise ? 0.45F : -0.45F);
                }
            } else {
                // Distancia óptima de combate mágico (fuera de duelo en campo abierto: 5.5 a 14 bloques)
                if (dist < 5.5D) {
                    Vec3 away = this.mob.position().subtract(target.position()).normalize();
                    this.mob.getNavigation().moveTo(this.mob.getX() + away.x * 5.0D, this.mob.getY(), this.mob.getZ() + away.z * 5.0D, 1.35D);
                    this.mob.getMoveControl().strafe(-0.5F, this.strafeClockwise ? 0.4F : -0.4F);
                } else if (dist > 15.0D) {
                    this.mob.getNavigation().moveTo(target, 1.25D);
                } else {
                    this.mob.getNavigation().stop();
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
                this.queenFireballBurstInterval--;
                if (this.queenFireballBurstInterval <= 0) {
                    this.queenFireballBurstInterval = 6;
                    this.queenFireballBurstRemaining--;
                    this.castSingleBlazeFireball(serverLevel, target);
                }
                return;
            }

            // 4. CANALIZACIÓN DEL RAYO DEL GUARDIÁN DEL OCÉANO
            if (this.queenBeamChannelTicks > 0) {
                this.queenBeamChannelTicks++;

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

                if (this.queenBeamChannelTicks % 7 == 0) {
                    float pitch = 0.8F + (this.queenBeamChannelTicks / 30.0F) * 0.7F;
                    serverLevel.playSound(null, this.mob.getX(), this.mob.getY(), this.mob.getZ(),
                            SoundEvents.GUARDIAN_ATTACK, this.mob.getSoundSource(), 1.0F, pitch);
                }

                // Culminación del rayo: Impacto y explosión mágica tras 30 ticks (1.5 segundos)
                if (this.queenBeamChannelTicks >= 30) {
                    this.queenBeamChannelTicks = 0;
                    this.queenSpellCooldown = 35; // Cooldown post-rayo

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

            if (!this.mob.hasLineOfSight(target)) {
                this.mob.getNavigation().moveTo(target, 1.25D);
                return;
            }

            // A. Evoker Fangs si el objetivo está muy cerca (< 4.0 bloques): Anillo defensivo de colmillos
            if (dist <= 4.0D) {
                this.castEvokerFangsRing(serverLevel);
                this.queenSpellCooldown = 28;
                return;
            }

            // B. Rango de combate medio/largo: Variedad completa de ataques
            // Alternar equitativamente entre Rayo del Guardián del Océano, Colmillos de Evocador en hilera y Ráfaga de Fuego de Blaze
            float roll = this.mob.getRandom().nextFloat();
            if (roll < 0.35F && dist >= 4.0D && dist <= 20.0D) {
                // Hechizo 1: Iniciar canalización del Rayo del Guardián del Océano
                this.queenBeamChannelTicks = 1;
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
            return stack.has(DataComponents.KINETIC_WEAPON) || stack.is(ItemTags.SPEARS) || stack.getItem() instanceof TridentItem;
        }
    }

    // 3. Meta: Caminar hacia ítems en el suelo y recogerlos
    public static class PickupGroundItemGoal extends Goal {
        private final KoboldGirlEntity mob;
        private ItemEntity targetItem;

        public PickupGroundItemGoal(KoboldGirlEntity mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.mob.isSleeping()) {
                return false;
            }
            List<ItemEntity> items = this.mob.level().getEntitiesOfClass(
                    ItemEntity.class,
                    this.mob.getBoundingBox().inflate(12.0D, 4.0D, 12.0D),
                    item -> !item.hasPickUpDelay() && item.isAlive()
                            && this.mob.hasLineOfSight(item)
                            && this.mob.wantsToPickUpItem(item.getItem())
                            && this.mob.getInventory().canAddItem(item.getItem())
            );
            if (items.isEmpty()) return false;

            this.targetItem = this.getBestTargetItem(items);
            return this.targetItem != null;
        }

        private ItemEntity getBestTargetItem(List<ItemEntity> items) {
            ItemEntity bestItem = null;
            double bestScore = -Double.MAX_VALUE;

            for (ItemEntity item : items) {
                ItemStack stack = item.getItem();
                double priority = 10.0D;

                // Prioridad máxima: Comida si tiene hambre
                if (this.mob.needsFood() && isReadyToEatFood(stack)) {
                    priority = 100.0D;
                } else if (this.mob.isArmorUpgrade(stack)) {
                    priority = 90.0D;
                } else if (this.mob.isDefender() && isEnemyLoot(stack)) {
                    priority = 85.0D;
                } else if (this.mob.getProfession() == KoboldProfession.FARMER && (isHarvestCropItem(stack) || isSeedItem(stack))) {
                    priority = 80.0D;
                } else if (this.mob.getProfession() == KoboldProfession.HUNTER && (isRawMeat(stack) || isCookedMeat(stack) || isAnimalProduct(stack))) {
                    priority = 80.0D;
                } else if (this.mob.getProfession() == KoboldProfession.VIGILANT && isArrowItem(stack)) {
                    priority = 80.0D;
                } else if (this.mob.getProfession() == KoboldProfession.GUARD && (isShield(stack) || isWeapon(stack))) {
                    priority = 80.0D;
                }

                // Descuento por distancia (más cercano = mayor score)
                double distSq = this.mob.distanceToSqr(item);
                double score = priority - (distSq * 0.1D);
                if (score > bestScore) {
                    bestScore = score;
                    bestItem = item;
                }
            }
            return bestItem;
        }

        @Override
        public boolean canContinueToUse() {
            return !this.mob.isSleeping() && this.targetItem != null && this.targetItem.isAlive() && !this.targetItem.hasPickUpDelay()
                    && this.mob.hasLineOfSight(this.targetItem)
                    && this.mob.wantsToPickUpItem(this.targetItem.getItem())
                    && this.mob.getInventory().canAddItem(this.targetItem.getItem())
                    && this.mob.distanceToSqr(this.targetItem) < 144.0D;
        }

        @Override
        public void tick() {
            if (this.targetItem != null) {
                this.mob.getNavigation().moveTo(this.targetItem, 1.25D);
                if (this.mob.distanceToSqr(this.targetItem) < 2.5D) {
                    if (this.mob.level() instanceof ServerLevel serverLevel) {
                        this.mob.pickUpItem(serverLevel, this.targetItem);
                    }
                }
            }
        }
    }

    // 4. Meta: Cosechar cultivos maduros, replantar y sembrar tierra arada
    public static class HarvestCropGoal extends MoveToBlockGoal {
        private final KoboldGirlEntity kobold;

        public HarvestCropGoal(KoboldGirlEntity mob, double speed) {
            super(mob, speed, 16);
            this.kobold = mob;
        }

        @Override
        public boolean canUse() {
            if (this.kobold.isSleeping() || this.kobold.isThreatened() || this.kobold.isHunting()) {
                return false;
            }
            if (this.kobold.isOwned() && this.kobold.getCommandStance() != CommandStance.WORK) {
                return false;
            }
            // Granjeras o la Reina colaborando en labores agrícolas
            if (this.kobold.getProfession() != KoboldProfession.FARMER && !this.kobold.isAnyQueen()) {
                return false;
            }
            // Jornada de trabajo matutina (7:00 AM a 1:00 PM: ticks 1000 a 7000)
            if (!this.kobold.isMorningWorkTime()) {
                return false;
            }
            if (!this.kobold.hasFreeInventorySlot()) {
                return false;
            }
            return super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            if (this.kobold.isSleeping() || this.kobold.isThreatened() || this.kobold.isHunting()) {
                return false;
            }
            if (this.kobold.isOwned() && this.kobold.getCommandStance() != CommandStance.WORK) {
                return false;
            }
            if (this.kobold.getProfession() != KoboldProfession.FARMER && !this.kobold.isAnyQueen()) {
                return false;
            }
            if (!this.kobold.isMorningWorkTime()) {
                return false;
            }
            if (!this.kobold.hasFreeInventorySlot()) {
                return false;
            }
            return super.canContinueToUse();
        }

        @Override
        public void start() {
            super.start();
            this.kobold.setHarvesting(true);
            if (this.kobold.getProfession() == KoboldProfession.FARMER) {
                this.kobold.equipHoeForHarvesting();
            } else {
                this.kobold.unequipHands();
            }
        }

        @Override
        public void stop() {
            super.stop();
            this.kobold.setHarvesting(false);
            this.kobold.unequipHands();
        }

        @Override
        protected int nextStartTick(PathfinderMob mob) {
            return 20 + mob.getRandom().nextInt(20);
        }

        @Override
        public double acceptedDistance() {
            return 2.2D;
        }

        @Override
        protected boolean isValidTarget(LevelReader level, BlockPos pos) {
            // 1. Cultivo maduro para cosechar activamente (100% maduro)
            BlockState aboveState = level.getBlockState(pos.above());
            if (isMatureCropOrBerry(aboveState)) {
                return true;
            }
            BlockState state = level.getBlockState(pos);
            if (isMatureCropOrBerry(state)) {
                return true;
            }

            // 2. Tierra arada vacía para sembrar si tiene semillas
            if (this.kobold.hasPlantableSeeds()) {
                if (state.is(Blocks.FARMLAND) && aboveState.isAir()) {
                    return true;
                }
                if (level.getBlockState(pos.below()).is(Blocks.FARMLAND) && state.isAir()) {
                    return true;
                }
            }

            // 3. Preparar terreno para siembra: arar césped/tierra cerca de agua si tiene semillas
            if (this.kobold.hasPlantableSeeds() && isTillableSoilNearWater(level, pos)) {
                return true;
            }

            return false;
        }

        private static boolean isTillableSoilNearWater(LevelReader level, BlockPos pos) {
            BlockState state = level.getBlockState(pos);
            if (!state.is(Blocks.GRASS_BLOCK) && !state.is(Blocks.DIRT) && !state.is(Blocks.COARSE_DIRT)) {
                return false;
            }
            if (!level.getBlockState(pos.above()).isAir()) {
                return false;
            }
            // Verificar si hay agua a 4 bloques o menos horizontalmente (dx, dz <= 4, dy 0..1) para hidratación
            for (BlockPos checkPos : BlockPos.betweenClosed(pos.offset(-4, 0, -4), pos.offset(4, 1, 4))) {
                if (level.getFluidState(checkPos).is(FluidTags.WATER)) {
                    return true;
                }
            }
            return false;
        }

        private static boolean isMatureCropOrBerry(BlockState state) {
            if (state.getBlock() instanceof CropBlock crop) {
                return crop.isMaxAge(state);
            }
            if (state.getBlock() instanceof SweetBerryBushBlock) {
                return state.getValue(SweetBerryBushBlock.AGE) >= 2;
            }
            return false;
        }

        @Override
        public void tick() {
            super.tick();
            if (this.isReachedTarget()) {
                if (this.kobold.level() instanceof ServerLevel serverLevel) {
                    BlockPos cropPos = this.blockPos.above();
                    if (!isMatureCropOrBerry(serverLevel.getBlockState(cropPos))) {
                        cropPos = this.blockPos;
                    }
                    BlockState cropState = serverLevel.getBlockState(cropPos);

                    // 1. Cosechar y replantar de inmediato el cultivo maduro
                    if (cropState.getBlock() instanceof CropBlock crop && crop.isMaxAge(cropState)) {
                        // Si está usando los puños (mano vacía), destroyBlock no desgasta ninguna herramienta.
                        // Si tiene una azada, se usa la azada.
                        serverLevel.destroyBlock(cropPos, true, this.kobold.getMainHandItem().isEmpty() ? null : this.kobold);
                        this.kobold.playSound(SoundEvents.CROP_BREAK, 1.0F, 1.0F);

                        // Absorción instantánea: Recoger inmediatamente los drops generados por la cosecha antes de que nadie los tome
                        List<ItemEntity> drops = serverLevel.getEntitiesOfClass(
                                ItemEntity.class,
                                new AABB(cropPos).inflate(2.5D),
                                item -> item.isAlive() && this.kobold.wantsToPickUpItem(item.getItem())
                        );
                        for (ItemEntity drop : drops) {
                            this.kobold.pickUpItem(serverLevel, drop);
                        }

                        // Replantar inmediatamente en la tierra arada
                        BlockPos soilPos = cropPos.below();
                        if (serverLevel.getBlockState(soilPos).is(Blocks.FARMLAND) && serverLevel.getBlockState(cropPos).isAir()) {
                            serverLevel.setBlock(cropPos, crop.getStateForAge(0), 3);
                            serverLevel.playSound(null, cropPos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
                        }
                        this.stop();
                    } else if (cropState.getBlock() instanceof SweetBerryBushBlock) {
                        serverLevel.setBlock(cropPos, cropState.setValue(SweetBerryBushBlock.AGE, 1), 3);
                        ItemStack berries = new ItemStack(Items.SWEET_BERRIES, 2 + this.kobold.getRandom().nextInt(2));
                        if (this.kobold.getInventory().canAddItem(berries)) {
                            this.kobold.getInventory().addItem(berries);
                            this.kobold.playSound(SoundEvents.ITEM_PICKUP, 0.25F, 1.2F);
                        } else {
                            Block.popResource(serverLevel, cropPos, berries);
                        }
                        this.kobold.playSound(SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, 1.0F, 1.0F);
                        this.stop();
                    } else if (serverLevel.getBlockState(this.blockPos).is(Blocks.FARMLAND)
                            || serverLevel.getBlockState(this.blockPos.below()).is(Blocks.FARMLAND)) {
                        // 2. Sembrar en tierra arada vacía si tiene semillas
                        BlockPos plantPos = this.blockPos.above();
                        BlockPos soilPos = this.blockPos;
                        if (!serverLevel.getBlockState(soilPos).is(Blocks.FARMLAND)) {
                            soilPos = this.blockPos.below();
                            plantPos = this.blockPos;
                        }

                        if (serverLevel.getBlockState(soilPos).is(Blocks.FARMLAND) && serverLevel.getBlockState(plantPos).isAir()) {
                            ItemStack seed = this.kobold.takeOneSeed();
                            if (!seed.isEmpty()) {
                                BlockState newCrop = this.kobold.getCropForSeed(seed);
                                if (newCrop != null) {
                                    serverLevel.setBlock(plantPos, newCrop, 3);
                                    serverLevel.playSound(null, plantPos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
                                }
                            }
                        }
                        this.stop();
                    } else {
                        // 3. Preparar terreno: arar tierra/césped cerca de agua y sembrar si tiene semillas
                        BlockPos targetPos = this.blockPos;
                        if (!isTillableSoilNearWater(serverLevel, targetPos)) {
                            targetPos = this.blockPos.below();
                        }

                        if (isTillableSoilNearWater(serverLevel, targetPos)) {
                            serverLevel.setBlock(targetPos, Blocks.FARMLAND.defaultBlockState(), 3);
                            serverLevel.playSound(null, targetPos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                                    targetPos.getX() + 0.5D, targetPos.getY() + 1.1D, targetPos.getZ() + 0.5D,
                                    4, 0.2D, 0.1D, 0.2D, 0.0D);

                            ItemStack hoe = this.kobold.getMainHandItem();
                            if (isHoe(hoe)) {
                                hoe.hurtAndBreak(1, serverLevel, this.kobold, item -> this.kobold.onEquippedItemBroken(item, EquipmentSlot.MAINHAND));
                            }

                            // Sembrar inmediatamente si tiene semillas
                            BlockPos plantPos = targetPos.above();
                            if (serverLevel.getBlockState(plantPos).isAir() && this.kobold.hasPlantableSeeds()) {
                                ItemStack seed = this.kobold.takeOneSeed();
                                if (!seed.isEmpty()) {
                                    BlockState newCrop = this.kobold.getCropForSeed(seed);
                                    if (newCrop != null) {
                                        serverLevel.setBlock(plantPos, newCrop, 3);
                                        serverLevel.playSound(null, plantPos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
                                    }
                                }
                            }
                        }
                        this.stop();
                    }
                }
            }
        }
    }

    // 5. Meta: Cazar animales exclusivamente (Cazadoras con hachas o arcos)
    public static class HuntFoodGoal extends NearestAttackableTargetGoal<Animal> {
        private final KoboldGirlEntity kobold;

        public HuntFoodGoal(KoboldGirlEntity mob) {
            super(mob, Animal.class, 0, false, false, (target, level) -> {
                if (!(target instanceof Animal animal)) return false;
                if (animal.isBaby()) return false;
                if (mob.distanceToSqr(target) > 256.0D) return false;
                if (!mob.hasLineOfSight(target)) return false;
                return isFoodAnimal(target);
            });
            this.kobold = mob;
        }

        @Override
        public boolean canUse() {
            if (this.kobold.isSleeping() || this.kobold.isInPowerStruggle()) {
                return false;
            }
            if (this.kobold.isOwned() && this.kobold.getCommandStance() != CommandStance.WORK) {
                return false;
            }
            if (this.kobold.getProfession() != KoboldProfession.HUNTER && !this.kobold.isAnyQueen()) {
                return false;
            }
            // Solo cazar en la jornada de trabajo matutina (7:00 AM a 1:00 PM: ticks 1000 a 7000)
            if (!this.kobold.isMorningWorkTime()) {
                return false;
            }
            if (this.kobold.isInventoryFullOfMeat()) {
                return false;
            }
            // Si la reina ya tiene suficiente carne recolectada (>= 16), cede el paso a las cazadoras
            if (this.kobold.isAnyQueen() && this.kobold.countRawMeatInInventory() >= 16) {
                return false;
            }
            return super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            if (this.kobold.isSleeping() || this.kobold.isInPowerStruggle()) {
                return false;
            }
            if (this.kobold.isOwned() && this.kobold.getCommandStance() != CommandStance.WORK) {
                return false;
            }
            if (this.kobold.getProfession() != KoboldProfession.HUNTER && !this.kobold.isAnyQueen()) {
                return false;
            }
            if (!this.kobold.isMorningWorkTime()) {
                return false;
            }
            if (this.kobold.isInventoryFullOfMeat()) {
                return false;
            }
            if (this.kobold.isAnyQueen() && this.kobold.countRawMeatInInventory() >= 16) {
                return false;
            }
            LivingEntity target = this.mob.getTarget();
            if (target != null && !this.mob.hasLineOfSight(target)) {
                return false;
            }
            return super.canContinueToUse();
        }

        @Override
        public void start() {
            super.start();
            this.kobold.equipForCombat(false); // Caza de animales: solo arma, sin escudo
        }

        @Override
        public void stop() {
            super.stop();
            this.kobold.unequipHands(); // Desequipar manos al terminar la caza
        }
    }

    // 6. Meta: Detectar entidades agresivas (Radio extendido a 16 bloques para patrullas de Guardias y Vigilantes)
    public static class HostileDetectorGoal extends NearestAttackableTargetGoal<LivingEntity> {
        private static final double RANGE = 10.0D;
        private final KoboldGirlEntity kobold;

        public HostileDetectorGoal(KoboldGirlEntity mob) {
            super(mob, LivingEntity.class, 0, true, false, (target, level) -> {
                if (target instanceof KoboldGirlEntity) return false;
                double maxRange = mob.isDefender() ? 16.0D : RANGE;
                if (mob.distanceToSqr(target) > (maxRange * maxRange)) return false;
                if (!mob.hasLineOfSight(target)) return false;
                if (target instanceof Enemy || target instanceof Monster) return true;
                if (target instanceof Mob otherMob && otherMob.getTarget() instanceof Player) return true;
                return false;
            });
            this.kobold = mob;
        }

        @Override
        public boolean canUse() {
            if (this.kobold.isSleeping() || this.kobold.isInPowerStruggle()) {
                return false;
            }
            if (this.kobold.getProfession() == KoboldProfession.FARMER || this.kobold.getProfession() == KoboldProfession.HUNTER) {
                return false;
            }
            return super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            if (this.kobold.isSleeping() || this.kobold.isInPowerStruggle()) {
                return false;
            }
            if (this.kobold.getProfession() == KoboldProfession.FARMER || this.kobold.getProfession() == KoboldProfession.HUNTER) {
                return false;
            }
            LivingEntity target = this.mob.getTarget();
            if (target != null && !this.mob.hasLineOfSight(target)) {
                return false;
            }
            return super.canContinueToUse();
        }

        @Override
        protected AABB getTargetSearchArea(double targetDistance) {
            return this.mob.getBoundingBox().inflate(RANGE, 4.0D, RANGE);
        }
    }

    // Meta: Cazadoras cocinan carne cruda en fogatas cercanas
    public static class KoboldCookMeatAtCampfireGoal extends MoveToBlockGoal {
        private final KoboldGirlEntity kobold;
        private int cookingTicks = 0;

        public KoboldCookMeatAtCampfireGoal(KoboldGirlEntity mob, double speed) {
            super(mob, speed, 24);
            this.kobold = mob;
        }

        @Override
        public boolean canUse() {
            if (this.kobold.isSleeping() || this.kobold.isThreatened() || this.kobold.isInPowerStruggle()) {
                return false;
            }
            if (this.kobold.isOwned() && this.kobold.getCommandStance() != CommandStance.WORK) {
                return false;
            }
            if (this.kobold.getProfession() != KoboldProfession.HUNTER) {
                return false;
            }
            if (!this.kobold.hasRawMeatInInventory()) {
                return false;
            }
            // Cocinar durante la hora de cocina (1:00 PM a 3:30 PM: ticks 7000 a 9500), o si ya llenó su capacidad de carne
            if (!this.kobold.isCookingAndCraftingTime() && !this.kobold.isInventoryFullOfMeat()) {
                return false;
            }
            return super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            if (this.kobold.isSleeping() || this.kobold.isThreatened() || this.kobold.isInPowerStruggle()) {
                return false;
            }
            if (this.kobold.isOwned() && this.kobold.getCommandStance() != CommandStance.WORK) {
                return false;
            }
            if (!this.kobold.hasRawMeatInInventory()) {
                return false;
            }
            return super.canContinueToUse();
        }

        @Override
        public void start() {
            super.start();
            this.cookingTicks = 0;
            this.kobold.unequipHands();
        }

        @Override
        public void stop() {
            super.stop();
            this.cookingTicks = 0;
            this.kobold.unequipHands();
        }

        @Override
        public double acceptedDistance() {
            return 2.5D;
        }

        @Override
        protected boolean isValidTarget(LevelReader level, BlockPos pos) {
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof CampfireBlock) {
                return state.getValue(CampfireBlock.LIT);
            }
            return false;
        }

        @Override
        public void tick() {
            super.tick();
            if (this.isReachedTarget()) {
                this.kobold.getLookControl().setLookAt(this.blockPos.getX() + 0.5D, this.blockPos.getY() + 0.5D, this.blockPos.getZ() + 0.5D);
                this.cookingTicks++;

                if (this.kobold.level() instanceof ServerLevel serverLevel) {
                    if (this.cookingTicks % 5 == 0) {
                        serverLevel.sendParticles(ParticleTypes.SMOKE,
                                this.blockPos.getX() + 0.5D, this.blockPos.getY() + 0.5D, this.blockPos.getZ() + 0.5D,
                                2, 0.1D, 0.2D, 0.1D, 0.02D);
                    }
                    if (this.cookingTicks >= 25) {
                        this.cookingTicks = 0;
                        if (this.kobold.cookOneRawMeat()) {
                            serverLevel.playSound(null, this.blockPos, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F);
                            serverLevel.sendParticles(ParticleTypes.FLAME,
                                    this.blockPos.getX() + 0.5D, this.blockPos.getY() + 0.6D, this.blockPos.getZ() + 0.5D,
                                    4, 0.15D, 0.1D, 0.15D, 0.01D);
                            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                                    this.kobold.getX(), this.kobold.getY() + 1.2D, this.kobold.getZ(),
                                    3, 0.2D, 0.2D, 0.2D, 0.0D);
                        }
                    }
                }
            }
        }
    }

    // Meta: Guardias y Vigilantes defienden activamente a todas las demás Kobold Girls de su tribu
    public static class KoboldDefendAlliesGoal extends Goal {
        private final KoboldGirlEntity mob;
        private LivingEntity threatToTarget;

        public KoboldDefendAlliesGoal(KoboldGirlEntity mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            if (!this.mob.isDefender() || !this.mob.isAlive() || this.mob.isSleeping()) return false;
            if (this.mob.isInQueenDuel() || this.mob.isInPowerStruggle()) return false;
            LivingEntity target = this.mob.getTarget();
            if (target != null && target.isAlive() && this.mob.hasLineOfSight(target)) return false;

            this.threatToTarget = this.findThreatAgainstAllies();
            return this.threatToTarget != null;
        }

        @Override
        public boolean canContinueToUse() {
            if (!this.mob.isDefender() || !this.mob.isAlive() || this.mob.isSleeping()) return false;
            if (this.mob.isInQueenDuel() || this.mob.isInPowerStruggle()) return false;
            return this.threatToTarget != null && this.threatToTarget.isAlive() && this.mob.hasLineOfSight(this.threatToTarget);
        }

        @Override
        public void start() {
            if (this.threatToTarget != null) {
                this.mob.setTarget(this.threatToTarget);
                this.mob.equipForCombat(true);
            }
        }

        @Override
        public void stop() {
            this.threatToTarget = null;
        }

        private LivingEntity findThreatAgainstAllies() {
            List<KoboldGirlEntity> allies = this.mob.level().getEntitiesOfClass(
                    KoboldGirlEntity.class,
                    this.mob.getBoundingBox().inflate(24.0D, 8.0D, 24.0D),
                    k -> k != this.mob && k.isAlive() && !k.isInQueenDuel() && k.getTribeName().equals(this.mob.getTribeName())
            );

            for (KoboldGirlEntity ally : allies) {
                LivingEntity attacker = ally.getLastHurtByMob();
                if (attacker != null && attacker.isAlive() && ally.tickCount - ally.getLastHurtByMobTimestamp() < 100) {
                    if (this.isValidThreat(attacker) && (this.mob.hasLineOfSight(attacker) || (this.mob.hasLineOfSight(ally) && this.mob.distanceToSqr(attacker) <= 16.0D))) {
                        return attacker;
                    }
                }

                LivingEntity allyTarget = ally.getTarget();
                if (allyTarget != null && allyTarget.isAlive() && !(allyTarget instanceof Animal)) {
                    if (this.isValidThreat(allyTarget) && (this.mob.hasLineOfSight(allyTarget) || (this.mob.hasLineOfSight(ally) && this.mob.distanceToSqr(allyTarget) <= 16.0D))) {
                        return allyTarget;
                    }
                }
            }
            return null;
        }

        private boolean isValidThreat(LivingEntity threat) {
            if (threat == this.mob) return false;
            if (threat instanceof KoboldGirlEntity koboldThreat && koboldThreat.isRebel() && this.mob.isOwned()) {
                return false;
            }
            if (threat instanceof KoboldGirlEntity koboldThreat) {
                return this.mob.canFightKobold(koboldThreat);
            }
            return this.mob.canAttack(threat);
        }
    }

    // Meta del gobernante: Sigue fielmente al dueño / rey si está en postura FOLLOW
    public static class KoboldFollowOwnerGoal extends Goal {
        private final KoboldGirlEntity mob;
        private LivingEntity owner;
        private final double speedModifier;
        private int timeToRecalcPath;
        private float oldWaterCost;

        public KoboldFollowOwnerGoal(KoboldGirlEntity mob, double speedModifier) {
            this.mob = mob;
            this.speedModifier = speedModifier;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!this.mob.isOwned() || this.mob.isRebel() || !this.mob.isAlive() || this.mob.isSleeping()) {
                return false;
            }
            if (this.mob.getCommandStance() != CommandStance.FOLLOW) {
                return false;
            }
            if (this.mob.isInQueenDuel() || this.mob.isInPlayerDuel() || this.mob.isInPowerStruggle()) {
                return false;
            }
            Player owner = this.mob.getOwner();
            if (owner == null || owner.isSpectator()) {
                return false;
            }
            if (this.mob.distanceToSqr(owner) < 9.0D) {
                return false;
            }
            if (this.mob.getTarget() != null && this.mob.getTarget().isAlive() && this.mob.distanceToSqr(owner) < 64.0D) {
                return false;
            }
            this.owner = owner;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.mob.getNavigation().isDone()) {
                return false;
            }
            if (this.mob.getCommandStance() != CommandStance.FOLLOW) {
                return false;
            }
            if (!this.mob.isOwned() || this.mob.isRebel() || !this.mob.isAlive() || this.mob.isSleeping()) {
                return false;
            }
            return this.owner != null && this.mob.distanceToSqr(this.owner) > 4.0D;
        }

        @Override
        public void start() {
            this.timeToRecalcPath = 0;
            this.oldWaterCost = this.mob.getPathfindingMalus(PathType.WATER);
            this.mob.setPathfindingMalus(PathType.WATER, 0.0F);
        }

        @Override
        public void stop() {
            this.owner = null;
            this.mob.getNavigation().stop();
            this.mob.setPathfindingMalus(PathType.WATER, this.oldWaterCost);
        }

        @Override
        public void tick() {
            if (this.owner == null) return;
            this.mob.getLookControl().setLookAt(this.owner, 10.0F, (float) this.mob.getMaxHeadXRot());
            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = 10;
                double distSqr = this.mob.distanceToSqr(this.owner);
                if (distSqr >= 256.0D) {
                    this.teleportToOwner();
                } else {
                    this.mob.getNavigation().moveTo(this.owner, this.speedModifier);
                }
            }
        }

        private void teleportToOwner() {
            BlockPos ownerPos = this.owner.blockPosition();
            for (int i = 0; i < 10; i++) {
                int dx = this.mob.getRandom().nextIntBetweenInclusive(-3, 3);
                int dz = this.mob.getRandom().nextIntBetweenInclusive(-3, 3);
                int dy = this.mob.getRandom().nextIntBetweenInclusive(-1, 1);
                if (Math.abs(dx) >= 2 || Math.abs(dz) >= 2) {
                    BlockPos targetPos = ownerPos.offset(dx, dy, dz);
                    if (KoboldGirlEntity.isBedSideStandable(this.mob.level(), targetPos)) {
                        this.mob.moveTo(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D, this.mob.getYRot(), this.mob.getXRot());
                        this.mob.getNavigation().stop();
                        if (this.mob.level() instanceof ServerLevel sl) {
                            sl.sendParticles(ParticleTypes.PORTAL, this.mob.getX(), this.mob.getY() + 0.8D, this.mob.getZ(), 10, 0.3D, 0.4D, 0.3D, 0.05D);
                        }
                        return;
                    }
                }
            }
        }
    }

    // Meta del gobernante: Mantener posición vigilando cuando está en postura GUARD
    public static class KoboldGuardPositionGoal extends Goal {
        private final KoboldGirlEntity mob;
        private final double speedModifier;

        public KoboldGuardPositionGoal(KoboldGirlEntity mob, double speedModifier) {
            this.mob = mob;
            this.speedModifier = speedModifier;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (!this.mob.isOwned() || this.mob.isRebel() || !this.mob.isAlive() || this.mob.isSleeping()) return false;
            if (this.mob.getCommandStance() != CommandStance.GUARD) return false;
            if (this.mob.getTarget() != null && this.mob.getTarget().isAlive()) return false;
            BlockPos guardPos = this.mob.getGuardPosition();
            if (guardPos == null) return false;
            return this.mob.distanceToSqr(Vec3.atBottomCenterOf(guardPos)) > 16.0D;
        }

        @Override
        public void start() {
            BlockPos guardPos = this.mob.getGuardPosition();
            if (guardPos != null) {
                this.mob.getNavigation().moveTo(guardPos.getX() + 0.5D, guardPos.getY(), guardPos.getZ() + 0.5D, this.speedModifier);
            }
        }
    }

    // Meta: Duelo de dominancia de la Reina contra el Jugador
    public static class QueenPlayerDuelTargetGoal extends Goal {
        private final KoboldGirlEntity mob;

        public QueenPlayerDuelTargetGoal(KoboldGirlEntity mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            if (!this.mob.isAnyQueen() || !this.mob.isInPlayerDuel() || !this.mob.isAlive()) {
                return false;
            }
            Player duelPlayer = this.mob.getPlayerDuelTarget();
            return duelPlayer != null && duelPlayer.isAlive() && !duelPlayer.isSpectator();
        }

        @Override
        public void start() {
            Player duelPlayer = this.mob.getPlayerDuelTarget();
            if (duelPlayer != null && duelPlayer.isAlive()) {
                this.mob.setTarget(duelPlayer);
                this.mob.equipForCombat(true);
            }
        }

        @Override
        public boolean canContinueToUse() {
            if (!this.mob.isAnyQueen() || !this.mob.isInPlayerDuel() || !this.mob.isAlive()) {
                return false;
            }
            Player duelPlayer = this.mob.getPlayerDuelTarget();
            return duelPlayer != null && duelPlayer.isAlive() && !duelPlayer.isSpectator();
        }

        @Override
        public void tick() {
            Player duelPlayer = this.mob.getPlayerDuelTarget();
            if (duelPlayer != null && duelPlayer.isAlive()) {
                if (this.mob.getTarget() != duelPlayer) {
                    this.mob.setTarget(duelPlayer);
                }
                if (!isWeapon(this.mob.getMainHandItem())) {
                    this.mob.equipForCombat(true);
                }
            }
        }
    }

    // Meta: Kobolds aliadas defienden activamente a su dueño / gobernante (estilo lobo domesticado)
    public static class KoboldDefendOwnerGoal extends Goal {
        private final KoboldGirlEntity mob;
        private LivingEntity threatToTarget;

        public KoboldDefendOwnerGoal(KoboldGirlEntity mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            if (!this.mob.isOwned() || this.mob.isRebel() || !this.mob.isAlive() || this.mob.isSleeping()) return false;
            if (this.mob.isInQueenDuel() || this.mob.isInPlayerDuel() || this.mob.isInPowerStruggle()) return false;
            if (this.mob.getProfession() == KoboldProfession.FARMER) return false;

            LivingEntity currentTarget = this.mob.getTarget();
            if (currentTarget != null && currentTarget.isAlive() && this.mob.hasLineOfSight(currentTarget)) {
                return false;
            }

            Player owner = this.mob.getOwner();
            if (owner == null || !owner.isAlive() || this.mob.distanceToSqr(owner) > 32.0D * 32.0D) {
                return false;
            }

            // 1. Defender al dueño si fue atacado recientemente
            LivingEntity attacker = owner.getLastHurtByMob();
            if (attacker != null && attacker.isAlive() && (owner.tickCount - owner.getLastHurtByMobTimestamp() < 100)) {
                if (this.isValidThreat(attacker, owner) && (this.mob.hasLineOfSight(attacker) || this.mob.distanceToSqr(attacker) <= 16.0D)) {
                    this.threatToTarget = attacker;
                    return true;
                }
            }

            // 2. Apoyar al dueño si atacó a un objetivo recientemente
            LivingEntity target = owner.getLastHurtMob();
            if (target != null && target.isAlive() && (owner.tickCount - owner.getLastHurtMobTimestamp() < 100)) {
                if (this.isValidThreat(target, owner) && (this.mob.hasLineOfSight(target) || this.mob.distanceToSqr(target) <= 16.0D)) {
                    this.threatToTarget = target;
                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean canContinueToUse() {
            if (!this.mob.isOwned() || this.mob.isRebel() || !this.mob.isAlive() || this.mob.isSleeping()) return false;
            if (this.mob.isInQueenDuel() || this.mob.isInPlayerDuel() || this.mob.isInPowerStruggle()) return false;
            return this.threatToTarget != null && this.threatToTarget.isAlive() && this.mob.hasLineOfSight(this.threatToTarget);
        }

        @Override
        public void start() {
            if (this.threatToTarget != null) {
                this.mob.setTarget(this.threatToTarget);
                this.mob.equipForCombat(true);
            }
        }

        @Override
        public void stop() {
            this.threatToTarget = null;
        }

        private boolean isValidThreat(LivingEntity threat, Player owner) {
            if (threat == null || threat == this.mob || threat == owner) return false;
            // Las súbditas leales NUNCA atacan a rebeldes de su pueblo (la Reina leal se encarga)
            if (threat instanceof KoboldGirlEntity koboldThreat && koboldThreat.isRebel()) {
                return false;
            }
            if (threat instanceof KoboldGirlEntity otherKobold) {
                return this.mob.canFightKobold(otherKobold);
            }
            return this.mob.canAttack(threat);
        }
    }

    // Meta: Duelo a muerte entre Reinas de tribus rivales
    public static class QueenDuelTargetGoal extends Goal {
        private final KoboldGirlEntity mob;
        private KoboldGirlEntity rivalQueen;
        private int unseenTicks = 0;

        public QueenDuelTargetGoal(KoboldGirlEntity mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            if (!this.mob.isQueen() || !this.mob.hasTribe() || !this.mob.isAlive()) {
                return false;
            }
            LivingEntity currentTarget = this.mob.getTarget();
            if (currentTarget instanceof KoboldGirlEntity rival && rival.isQueen() && rival.isAlive() && rival.hasTribe()
                    && !rival.getTribeName().equals(this.mob.getTribeName()) && this.mob.hasLineOfSight(rival)) {
                this.rivalQueen = rival;
                return true;
            }
            // NO detectar a través de muros: Solo reinas rivales vivas con línea de visión directa
            List<KoboldGirlEntity> candidates = this.mob.level().getEntitiesOfClass(
                    KoboldGirlEntity.class,
                    this.mob.getBoundingBox().inflate(24.0D, 8.0D, 24.0D),
                    q -> q != this.mob && q.isQueen() && q.isAlive() && q.hasTribe()
                            && !q.getTribeName().equals(this.mob.getTribeName())
                            && this.mob.hasLineOfSight(q)
            );
            if (!candidates.isEmpty()) {
                candidates.sort(Comparator.comparingDouble(this.mob::distanceToSqr));
                this.rivalQueen = candidates.get(0);
                return true;
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            if (!this.mob.isQueen() || !this.mob.isAlive() || !this.mob.hasTribe()) {
                return false;
            }
            LivingEntity target = this.mob.getTarget();
            if (!(target instanceof KoboldGirlEntity rival) || !rival.isQueen() || !rival.isAlive()
                    || !rival.hasTribe() || rival.getTribeName().equals(this.mob.getTribeName())) {
                return false;
            }
            if (this.mob.distanceToSqr(rival) > 36.0D * 36.0D) {
                return false;
            }
            // Si la reina rival se oculta tras muros por más de 80 ticks (4 seg) y no está en rango cuerpo a cuerpo, de-escalar
            if (this.unseenTicks > 80 && this.mob.distanceToSqr(rival) > 16.0D) {
                return false;
            }
            return true;
        }

        @Override
        public void start() {
            this.unseenTicks = 0;
            if (this.rivalQueen != null && this.rivalQueen.isAlive()) {
                if (this.mob.isSleeping()) this.mob.stopSleeping();
                if (this.rivalQueen.isSleeping() && this.rivalQueen.hasLineOfSight(this.mob)) {
                    this.rivalQueen.stopSleeping();
                }

                Vec3 center = this.mob.position().add(this.rivalQueen.position()).scale(0.5D);
                this.mob.setDuelArenaCenter(center);
                this.rivalQueen.setDuelArenaCenter(center);

                this.mob.setTarget(this.rivalQueen);
                if (this.rivalQueen.hasLineOfSight(this.mob) && this.rivalQueen.getTarget() != this.mob) {
                    this.rivalQueen.setTarget(this.mob);
                    this.rivalQueen.equipForCombat(true);
                }
                this.mob.equipForCombat(true);
            }
        }

        @Override
        public void stop() {
            this.mob.setDuelArenaCenter(null);
            if (this.rivalQueen != null) {
                this.rivalQueen.setDuelArenaCenter(null);
            }
            this.rivalQueen = null;
            this.unseenTicks = 0;
        }

        @Override
        public void tick() {
            LivingEntity target = this.mob.getTarget();
            if (target instanceof KoboldGirlEntity rival && rival.isQueen() && rival.isAlive()) {
                if (!this.mob.hasLineOfSight(rival)) {
                    this.unseenTicks++;
                } else {
                    this.unseenTicks = 0;
                    if (rival.getTarget() != this.mob && rival.hasLineOfSight(this.mob)) {
                        rival.setTarget(this.mob);
                        rival.equipForCombat(true);
                    }
                }
            }
        }
    }

    // Meta: Formar un círculo y espectar el duelo a muerte entre Reinas
    public static class KoboldSpectateQueenDuelGoal extends Goal {
        private final KoboldGirlEntity mob;
        private QueenDuel currentDuel;
        private int repathTimer = 0;

        public KoboldSpectateQueenDuelGoal(KoboldGirlEntity mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.mob.isAnyQueen() || this.mob.isRebel() || this.mob.isInPowerStruggle() || !this.mob.isAlive()) return false;
            this.currentDuel = getNearbyQueenDuel(this.mob.level(), this.mob.position(), 24.0D);
            if (this.currentDuel == null || !this.currentDuel.isActive()) {
                return false;
            }
            // NO detectar a través de muros: Solo espectar si tiene línea de visión directa hacia alguna de las dos reinas
            return this.mob.hasLineOfSight(this.currentDuel.queen1) || this.mob.hasLineOfSight(this.currentDuel.queen2);
        }

        @Override
        public boolean canContinueToUse() {
            if (this.mob.isAnyQueen() || this.mob.isRebel() || this.mob.isInPowerStruggle() || !this.mob.isAlive()) return false;
            if (this.currentDuel == null || !this.currentDuel.isActive()) return false;
            if (this.mob.distanceToSqr(this.currentDuel.getCenter()) > 36.0D * 36.0D) return false;
            // Si ambas reinas quedan fuera de línea de visión tras muros, dejar de espectar
            return this.mob.hasLineOfSight(this.currentDuel.queen1) || this.mob.hasLineOfSight(this.currentDuel.queen2);
        }

        @Override
        public void start() {
            this.mob.setSpectatingDuel(true);
            if (this.mob.isSleeping()) {
                this.mob.stopSleeping();
            }
            if (this.mob.getTarget() != null) {
                this.mob.setTarget(null);
            }
            if (this.mob.isUsingItem()) {
                this.mob.stopUsingItem();
            }
            this.mob.unequipHands();
            this.repathTimer = 0;
        }

        @Override
        public void stop() {
            this.mob.setSpectatingDuel(false);
            this.currentDuel = null;
            this.mob.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (this.currentDuel == null || !this.currentDuel.isActive()) {
                this.mob.setSpectatingDuel(false);
                return;
            }
            this.mob.setSpectatingDuel(true);

            if (!this.mob.getMainHandItem().isEmpty() || !this.mob.getOffhandItem().isEmpty()) {
                this.mob.unequipHands();
            }
            if (this.mob.getTarget() != null) {
                this.mob.setTarget(null);
            }

            // Partículas místicas periódicas sobre las espectadoras canalizando la arena del duelo
            if (this.mob.tickCount % 20 == 0 && this.mob.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.ENCHANT, this.mob.getX(), this.mob.getY() + 1.2D, this.mob.getZ(), 2, 0.2D, 0.2D, 0.2D, 0.05D);
            }

            Vec3 center = this.currentDuel.getCenter();
            this.mob.getLookControl().setLookAt(center.x, center.y + 1.0D, center.z, 30.0F, 30.0F);

            this.repathTimer--;
            if (this.repathTimer <= 0) {
                this.repathTimer = 10 + this.mob.getRandom().nextInt(10);

                // Solo incluir en el círculo a las súbditas que realmente tienen línea de visión hacia el combate
                List<KoboldGirlEntity> spectators = this.mob.level().getEntitiesOfClass(
                        KoboldGirlEntity.class,
                        new AABB(center.x - 24.0D, center.y - 12.0D, center.z - 24.0D, center.x + 24.0D, center.y + 12.0D, center.z + 24.0D),
                        k -> !k.isAnyQueen() && !k.isRebel() && k.isAlive()
                                && (k.hasLineOfSight(this.currentDuel.queen1) || k.hasLineOfSight(this.currentDuel.queen2))
                );

                spectators.sort(Comparator.comparing(Entity::getUUID));
                int index = spectators.indexOf(this.mob);
                if (index < 0) index = 0;
                int total = Math.max(1, spectators.size());

                double angle = (2.0D * Math.PI * index) / total;
                double radius = getDuelSpectatorRadius(this.mob.level(), center, this.currentDuel.queen1, this.currentDuel.queen2);

                double targetX = center.x + Math.cos(angle) * radius;
                double targetZ = center.z + Math.sin(angle) * radius;
                double targetY = center.y;

                BlockPos basePos = BlockPos.containing(targetX, targetY, targetZ);
                BlockPos standPos = findStandablePosNear(this.mob.level(), basePos);
                if (standPos != null) {
                    targetX = standPos.getX() + 0.5D;
                    targetY = standPos.getY();
                    targetZ = standPos.getZ() + 0.5D;
                }

                double distSq = this.mob.distanceToSqr(targetX, targetY, targetZ);
                if (distSq > 2.0D) {
                    this.mob.getNavigation().moveTo(targetX, targetY, targetZ, 1.2D);
                } else {
                    this.mob.getNavigation().stop();
                }
            }
        }
    }

    // Meta: Lucha encarnizada por el trono de la tribu (guerra civil entre todas las súbditas)
    public static class TribalPowerStruggleTargetGoal extends Goal {
        private final KoboldGirlEntity mob;
        private int unseenTicks = 0;

        public TribalPowerStruggleTargetGoal(KoboldGirlEntity mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            if (!this.mob.isInPowerStruggle() || !this.mob.isAlive()) return false;
            LivingEntity target = this.mob.getTarget();
            if (target != null && target.isAlive() && target instanceof KoboldGirlEntity k && k.isInPowerStruggle()) {
                if (this.mob.hasLineOfSight(k)) {
                    return false;
                }
            }

            // NO detectar a través de muros: Solo rivales con línea de visión directa
            List<KoboldGirlEntity> rivals = this.mob.level().getEntitiesOfClass(
                    KoboldGirlEntity.class,
                    this.mob.getBoundingBox().inflate(24.0D, 8.0D, 24.0D),
                    k -> k != this.mob && k.isAlive() && k.isInPowerStruggle() && this.mob.hasLineOfSight(k)
            );
            if (rivals.isEmpty()) return false;

            // Atacar al rival visible más cercano en la guerra por el poder
            rivals.sort(Comparator.comparingDouble(this.mob::distanceToSqr));
            KoboldGirlEntity targetRival = rivals.get(0);

            this.mob.setTarget(targetRival);
            this.mob.equipForCombat(true);
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            if (!this.mob.isInPowerStruggle() || !this.mob.isAlive()) return false;
            LivingEntity target = this.mob.getTarget();
            if (target == null || !target.isAlive() || !(target instanceof KoboldGirlEntity k) || !k.isInPowerStruggle()) {
                return false;
            }
            if (this.mob.distanceToSqr(k) > 36.0D * 36.0D) {
                return false;
            }
            if (this.unseenTicks > 80 && this.mob.distanceToSqr(k) > 16.0D) {
                return false;
            }
            return true;
        }

        @Override
        public void start() {
            this.unseenTicks = 0;
        }

        @Override
        public void stop() {
            this.unseenTicks = 0;
        }

        @Override
        public void tick() {
            LivingEntity target = this.mob.getTarget();
            if (target != null) {
                if (!this.mob.hasLineOfSight(target)) {
                    this.unseenTicks++;
                } else {
                    this.unseenTicks = 0;
                }
            }
        }
    }

    public static class RebelTargetGoal extends Goal {
        private final KoboldGirlEntity mob;
        private int unseenTicks = 0;

        public RebelTargetGoal(KoboldGirlEntity mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            if (!this.mob.isRebel() || !this.mob.isAlive() || this.mob.isInPowerStruggle()) return false;
            LivingEntity target = this.mob.getTarget();
            if (target != null && target.isAlive() && this.mob.hasLineOfSight(target)) return false;

            // 1. La evasión pacífica respecto a jugadores es gestionada por KoboldRebelAvoidAndDefendPlayerGoal
            // (evita al jugador sin atacarlo y se mantiene a <= 15 bloques de la Reina)

            // 2. NO detectar a través de muros: Solo enemigas de TRIBUS RIVALES con línea de visión directa
            // (no ataca a las súbditas sometidas de su misma tribu)
            List<KoboldGirlEntity> enemies = this.mob.level().getEntitiesOfClass(
                    KoboldGirlEntity.class,
                    this.mob.getBoundingBox().inflate(20.0D, 8.0D, 20.0D),
                    k -> k != this.mob && k.isAlive() && !k.isRebel()
                            && !this.mob.getTribeName().isEmpty() && !k.getTribeName().isEmpty()
                            && !k.getTribeName().equals(this.mob.getTribeName())
                            && this.mob.hasLineOfSight(k)
            );
            if (enemies.isEmpty()) return false;

            KoboldGirlEntity targetEnemy = null;
            for (KoboldGirlEntity k : enemies) {
                if (k.isQueen()) {
                    targetEnemy = k;
                    break;
                }
            }
            if (targetEnemy == null) {
                targetEnemy = enemies.get(0);
            }
            this.mob.setTarget(targetEnemy);
            this.mob.equipForCombat(true);
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            if (!this.mob.isRebel() || !this.mob.isAlive() || this.mob.isInPowerStruggle()) return false;
            LivingEntity target = this.mob.getTarget();
            if (target == null || !target.isAlive()) {
                return false;
            }
            if (target instanceof Player player) {
                if (player.isCreative() || player.isSpectator() || isHoldingBaculo(player) || this.mob.getReprimandedCooldown() > 0) return false;
                if (this.mob.distanceTo(player) > 6.0D) return false;
            } else if (target instanceof KoboldGirlEntity other) {
                if (other.isRebel()) return false;
                if (other.getTribeName().equals(this.mob.getTribeName())) return false;
            } else {
                return false;
            }
            if (this.mob.distanceToSqr(target) > 28.0D * 28.0D) {
                return false;
            }
            if (this.unseenTicks > 80 && this.mob.distanceToSqr(target) > 16.0D) {
                return false;
            }
            return true;
        }

        @Override
        public void start() {
            this.unseenTicks = 0;
        }

        @Override
        public void stop() {
            this.unseenTicks = 0;
        }

        @Override
        public void tick() {
            LivingEntity target = this.mob.getTarget();
            if (target != null) {
                if (!this.mob.hasLineOfSight(target)) {
                    this.unseenTicks++;
                } else {
                    this.unseenTicks = 0;
                }
            }
        }
    }

    // Meta: Defender a la tribu contra ataques de súbditas rebeldes
    public static class DefendTribeFromRebelsGoal extends Goal {
        private final KoboldGirlEntity mob;
        private int unseenTicks = 0;

        public DefendTribeFromRebelsGoal(KoboldGirlEntity mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            if (this.mob.isRebel() || !this.mob.isAlive() || this.mob.isInQueenDuel() || this.mob.isInPowerStruggle()) return false;
            // ¡Las súbditas leales al jugador NO atacan a las rebeldes! (La reina se encarga con un golpe disciplinario)
            if (this.mob.isOwned() || this.mob.getOwnerUUID() != null) return false;
            if (this.mob.getProfession() == KoboldProfession.FARMER || this.mob.getProfession() == KoboldProfession.HUNTER) return false;
            LivingEntity target = this.mob.getTarget();
            if (target != null && target.isAlive() && this.mob.hasLineOfSight(target)) return false;

            // NO detectar a través de muros: Solo rebeldes con línea de visión directa
            List<KoboldGirlEntity> rebels = this.mob.level().getEntitiesOfClass(
                    KoboldGirlEntity.class,
                    this.mob.getBoundingBox().inflate(16.0D, 8.0D, 16.0D),
                    k -> k != this.mob && k.isAlive() && k.isRebel()
                            && this.mob.hasLineOfSight(k)
            );
            if (rebels.isEmpty()) return false;

            this.mob.setTarget(rebels.get(0));
            this.mob.equipForCombat(true);
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.mob.isRebel() || !this.mob.isAlive() || this.mob.isInQueenDuel() || this.mob.isInPowerStruggle()) return false;
            if (this.mob.isOwned() || this.mob.getOwnerUUID() != null) return false;
            if (this.mob.getProfession() == KoboldProfession.FARMER || this.mob.getProfession() == KoboldProfession.HUNTER) return false;
            LivingEntity target = this.mob.getTarget();
            if (target == null || !target.isAlive() || !(target instanceof KoboldGirlEntity rebel)
                    || !rebel.isRebel()) {
                return false;
            }
            if (this.mob.distanceToSqr(rebel) > 24.0D * 24.0D) {
                return false;
            }
            if (this.unseenTicks > 80 && this.mob.distanceToSqr(rebel) > 16.0D) {
                return false;
            }
            return true;
        }

        @Override
        public void start() {
            this.unseenTicks = 0;
        }

        @Override
        public void stop() {
            this.unseenTicks = 0;
        }

        @Override
        public void tick() {
            LivingEntity target = this.mob.getTarget();
            if (target != null) {
                if (!this.mob.hasLineOfSight(target)) {
                    this.unseenTicks++;
                } else {
                    this.unseenTicks = 0;
                }
            }
        }
    }

    // Meta: Represalia al ser dañada y alertar a aliadas cercanas SIN atravesar muros
    public static class KoboldHurtByTargetGoal extends HurtByTargetGoal {
        private final KoboldGirlEntity kobold;

        public KoboldHurtByTargetGoal(KoboldGirlEntity mob) {
            super(mob, KoboldGirlEntity.class);
            this.kobold = mob;
        }

        @Override
        public boolean canUse() {
            if (this.kobold.isOwned() && !this.kobold.isAnyQueen()) {
                LivingEntity attacker = this.kobold.getLastHurtByMob();
                if (attacker instanceof KoboldGirlEntity k && k.isRebel()) {
                    return false;
                }
            }
            return super.canUse();
        }

        @Override
        protected void alertOther(Mob other, LivingEntity hurtByMob) {
            if (other instanceof KoboldGirlEntity koboldOther) {
                // Súbditas leales no toman represalia ni se alertan contra rebeldes de su tribu
                if (hurtByMob instanceof KoboldGirlEntity k && k.isRebel() && koboldOther.isOwned()) {
                    return;
                }
                // No alertar si está durmiendo, en duelo de reinas o en guerra por el poder
                if (koboldOther.isSleeping() || koboldOther.isInQueenDuel() || koboldOther.isInPowerStruggle()) {
                    return;
                }
                // Granjeras y Cazadoras son pacíficas y solo se defienden cuando son atacadas directamente
                if (koboldOther.getProfession() == KoboldProfession.FARMER || koboldOther.getProfession() == KoboldProfession.HUNTER) {
                    return;
                }
                // Solo alertar a compañeras de la misma tribu
                if (!this.kobold.getTribeName().isEmpty() && !koboldOther.getTribeName().isEmpty()
                        && !this.kobold.getTribeName().equals(koboldOther.getTribeName())) {
                    return;
                }
                // ¡NO DETECTAR A TRAVÉS DE MUROS! Debe tener línea de visión directa hacia la víctima o hacia el agresor
                if (!koboldOther.hasLineOfSight(this.kobold) && !koboldOther.hasLineOfSight(hurtByMob)) {
                    return;
                }
            }
            super.alertOther(other, hurtByMob);
        }
    }

    // Meta: La Reina leal defiende a su gobernante aplicando un solo puñetazo disciplinario con los puños a súbditas rebeldes
    public static class QueenReprimandRebelGoal extends Goal {
        private final KoboldGirlEntity queen;
        private KoboldGirlEntity targetRebel = null;
        private boolean deliveredHit = false;

        public QueenReprimandRebelGoal(KoboldGirlEntity queen) {
            this.queen = queen;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!this.queen.isAnyQueen() || !this.queen.isOwned() || this.queen.isRebel() || !this.queen.isAlive() || this.queen.isSleeping()) {
                return false;
            }
            if (this.queen.isInPlayerDuel() || this.queen.isInQueenDuel() || this.queen.isInPowerStruggle()) {
                return false;
            }
            if (this.queen.getReprimandCooldown() > 0) {
                return false;
            }
            Player owner = this.queen.getOwner();
            if (owner == null || !owner.isAlive()) {
                return false;
            }

            // Buscar si hay alguna súbdita rebelde de la tribu que esté atacando o teniendo como objetivo al gobernante
            List<KoboldGirlEntity> nearbyRebels = this.queen.level().getEntitiesOfClass(
                    KoboldGirlEntity.class,
                    this.queen.getBoundingBox().inflate(24.0D, 12.0D, 24.0D),
                    k -> k != this.queen && k.isAlive() && k.isRebel()
                            && (k.getTarget() == owner || owner.getLastHurtByMob() == k)
                            && (this.queen.getTribeName().isEmpty() || k.getTribeName().equals(this.queen.getTribeName()))
            );

            if (nearbyRebels.isEmpty()) return false;

            this.targetRebel = nearbyRebels.get(0);
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.deliveredHit) return false;
            if (this.targetRebel == null || !this.targetRebel.isAlive() || !this.targetRebel.isRebel()) return false;
            if (!this.queen.isAlive() || this.queen.isSleeping()) return false;
            return this.queen.distanceToSqr(this.targetRebel) <= 32.0D * 32.0D;
        }

        @Override
        public void start() {
            this.deliveredHit = false;
            // Desequipar armas letales para golpear estrictamente con los puños
            this.queen.unequipHands();
            this.queen.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            this.queen.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            if (this.targetRebel != null) {
                this.queen.getNavigation().moveTo(this.targetRebel, 1.35D);
            }
        }

        @Override
        public void stop() {
            this.targetRebel = null;
            this.deliveredHit = false;
            this.queen.setTarget(null);
        }

        @Override
        public void tick() {
            if (this.targetRebel == null || !this.targetRebel.isAlive() || this.deliveredHit) return;

            // Asegurar que las manos permanezcan desarmadas (golpe con los puños)
            if (!this.queen.getMainHandItem().isEmpty() || !this.queen.getOffhandItem().isEmpty()) {
                this.queen.unequipHands();
            }

            this.queen.getLookControl().setLookAt(this.targetRebel, 30.0F, 30.0F);

            if (this.queen.getNavigation().isDone() || this.queen.tickCount % 10 == 0) {
                this.queen.getNavigation().moveTo(this.targetRebel, 1.35D);
            }

            double distSqr = this.queen.distanceToSqr(this.targetRebel);
            if (distSqr <= 5.5D) { // Alcance cuerpo a cuerpo (aprox 2.3 bloques)
                // Entregar exactamente UN golpe con los puños para reprender
                this.deliveredHit = true;
                this.queen.swing(InteractionHand.MAIN_HAND);

                if (this.queen.level() instanceof ServerLevel serverLevel) {
                    DamageSource punchSource = this.queen.damageSources().mobAttack(this.queen);
                    this.targetRebel.hurtServer(serverLevel, punchSource, 1.0F);

                    double dx = this.targetRebel.getX() - this.queen.getX();
                    double dz = this.targetRebel.getZ() - this.queen.getZ();
                    this.targetRebel.knockback(0.65D, -dx, -dz, punchSource, 1.0F, false);

                    serverLevel.sendParticles(ParticleTypes.CRIT, this.targetRebel.getX(), this.targetRebel.getY() + 1.2D, this.targetRebel.getZ(), 10, 0.3D, 0.3D, 0.3D, 0.05D);
                    serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER, this.targetRebel.getX(), this.targetRebel.getY() + 1.4D, this.targetRebel.getZ(), 8, 0.3D, 0.3D, 0.3D, 0.02D);
                }

                this.queen.playSound(SoundEvents.PLAYER_ATTACK_KNOCKBACK, 1.0F, 1.2F);
                this.targetRebel.playSound(SoundEvents.VILLAGER_NO, 1.0F, 1.2F);

                // La rebelde detiene inmediatamente su ataque contra el jugador
                this.targetRebel.setTarget(null);
                this.targetRebel.unequipHands();
                this.targetRebel.setReprimandedCooldown(300); // 15 segundos sin volver a atacar

                // La rebelde retrocede asustada y avergonzada sin separarse a más de 14 bloques de la Reina
                Vec3 away = this.targetRebel.findPosAwayFromWithAnchor(this.queen.position(), 8.0D, this.queen.position(), 14.0D);
                if (away != null) {
                    this.targetRebel.getNavigation().moveTo(away.x, away.y, away.z, 1.3D);
                }

                // Diálogo de reprimenda de la Reina
                String[] reprimands = {
                    "¡Basta! ¡No permitiré que ataques a nuestro gobernante!",
                    "¡Compórtate! Acepta el mandato o retírate, pero no toleraré tu insubordinación.",
                    "¡Suficiente insolencia! ¡Respeta a quien ahora guía con honor a nuestra tribu!"
                };
                String rebuke = reprimands[this.queen.getRandom().nextInt(reprimands.length)];
                Component speaker = Component.literal("<" + this.queen.getQueenTitle() + "> ").withStyle(ChatFormatting.GOLD);
                Component msg = Component.literal(rebuke).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD);

                Player owner = this.queen.getOwner();
                if (owner != null) {
                    owner.sendSystemMessage(speaker.copy().append(msg));
                }

                // La Reina cesa inmediatamente la agresión
                this.queen.setTarget(null);
                this.queen.setReprimandCooldown(100);
                this.queen.getNavigation().stop();
            }
        }
    }

    // Meta: Súbditas rebeldes se alejan del jugador sin atacarlo (incluso si se acerca a <= 3 bloques),
    // no se separan de la Reina a más de 15 bloques, y solo permiten acercamiento si el jugador sostiene el báculo en mano
    public static class KoboldRebelAvoidAndDefendPlayerGoal extends Goal {
        private final KoboldGirlEntity mob;
        private Player targetPlayer = null;
        private int fleeCooldown = 0;

        public KoboldRebelAvoidAndDefendPlayerGoal(KoboldGirlEntity mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!this.mob.isRebel() || !this.mob.isAlive() || this.mob.isSleeping() || this.mob.isInPowerStruggle()) {
                return false;
            }
            KoboldGirlEntity queen = this.mob.getTribeQueen();
            // Si la reina está viva y la rebelde se alejó a más de 15 bloques de la reina, activar para regresar hacia ella
            if (queen != null && queen.isAlive() && this.mob.distanceTo(queen) > 15.0D) {
                return true;
            }
            // Buscar al jugador más cercano en un radio de 14 bloques
            Player nearest = this.mob.level().getNearestPlayer(this.mob, 14.0D);
            if (nearest == null || !nearest.isAlive() || nearest.isSpectator() || nearest.isCreative()) {
                return false;
            }
            if (!this.mob.hasLineOfSight(nearest)) {
                return false;
            }
            this.targetPlayer = nearest;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            if (!this.mob.isRebel() || !this.mob.isAlive() || this.mob.isSleeping() || this.mob.isInPowerStruggle()) {
                return false;
            }
            KoboldGirlEntity queen = this.mob.getTribeQueen();
            if (queen != null && queen.isAlive() && this.mob.distanceTo(queen) > 15.0D) {
                return true;
            }
            if (this.targetPlayer == null || !this.targetPlayer.isAlive() || this.targetPlayer.isSpectator() || this.targetPlayer.isCreative()) {
                return false;
            }
            return this.mob.distanceToSqr(this.targetPlayer) <= 16.0D * 16.0D;
        }

        @Override
        public void start() {
            this.fleeCooldown = 0;
        }

        @Override
        public void stop() {
            this.targetPlayer = null;
        }

        @Override
        public void tick() {
            if (this.fleeCooldown > 0) this.fleeCooldown--;

            KoboldGirlEntity queen = this.mob.getTribeQueen();

            // 1. REGLA ESTRICTA DE PROXIMIDAD: No separarse a más de 15 bloques de la Reina
            // "esas rebeldes que rechazan AL JUGADOR no se deben separar de la reina a mas de 15 bloques"
            if (queen != null && queen.isAlive()) {
                double distToQueen = this.mob.distanceTo(queen);
                if (distToQueen > 15.0D) {
                    if (this.mob.getTarget() != null && this.mob.getTarget() instanceof Player) {
                        this.mob.setTarget(null);
                        this.mob.unequipHands();
                    }
                    if (this.mob.getNavigation().isDone() || this.mob.tickCount % 20 == 0) {
                        this.mob.getNavigation().moveTo(queen, 1.35D);
                    }
                    this.mob.getLookControl().setLookAt(queen, 30.0F, 30.0F);
                    return;
                }
            }

            // Actualizar o verificar jugador objetivo
            if (this.targetPlayer == null || !this.targetPlayer.isAlive() || this.targetPlayer.isSpectator() || this.targetPlayer.isCreative()) {
                this.targetPlayer = this.mob.level().getNearestPlayer(this.mob, 14.0D);
            }
            if (this.targetPlayer == null) return;

            double distToPlayer = this.mob.distanceTo(this.targetPlayer);

            // 2. Si el jugador tiene el báculo en mano:
            // "la unica manera que le permitan acercarse sea si el jugador tiene el báculo en mano"
            if (isHoldingBaculo(this.targetPlayer)) {
                if (this.mob.getTarget() == this.targetPlayer) {
                    this.mob.setTarget(null);
                    this.mob.unequipHands();
                }
                // Permitir que el jugador se acerque sin huir ni atacar
                this.mob.getLookControl().setLookAt(this.targetPlayer, 30.0F, 30.0F);
                this.mob.getNavigation().stop();
                return;
            }

            // 3. Si el jugador NO tiene el báculo en mano:
            Vec3 queenPos = (queen != null && queen.isAlive()) ? queen.position() : null;

            // Si la rebelde fue recientemente reprendida por la Reina: huir activamente sin volver a atacar
            if (this.mob.getReprimandedCooldown() > 0) {
                if (this.mob.getTarget() == this.targetPlayer) {
                    this.mob.setTarget(null);
                    this.mob.unequipHands();
                }
                this.mob.getLookControl().setLookAt(this.targetPlayer, 30.0F, 30.0F);
                double speed = distToPlayer <= 3.0D ? 1.35D : 1.2D;
                this.fleeFromWithAnchor(this.targetPlayer.position(), 8.0D, queenPos, 15.0D, speed);
                return;
            }

            // Si el jugador está a más de 3 bloques: alejarse de él manteniéndose dentro de los 15 bloques de la Reina
            if (distToPlayer > 3.0D) {
                if (this.mob.getTarget() == this.targetPlayer) {
                    this.mob.setTarget(null);
                    this.mob.unequipHands();
                }
                this.mob.getLookControl().setLookAt(this.targetPlayer, 30.0F, 30.0F);
                this.fleeFromWithAnchor(this.targetPlayer.position(), 8.0D, queenPos, 15.0D, 1.2D);
            } else {
                // Si el jugador se acerca a <= 3 bloques sin el báculo en mano:
                // "las rebeldes son las que tienen que atacar al jugador si se acerca demasiado y es ahí cuando la reina se debe acercar para reprenderlas"
                if (this.mob.getTarget() != this.targetPlayer) {
                    this.mob.setTarget(this.targetPlayer);
                    this.mob.equipForCombat(true);
                }
                this.mob.getLookControl().setLookAt(this.targetPlayer, 30.0F, 30.0F);
            }
        }

        private void fleeFromWithAnchor(Vec3 dangerPos, double distance, @Nullable Vec3 anchorPos, double maxDistFromAnchor, double speedModifier) {
            if (this.mob.getNavigation().isDone() || this.fleeCooldown <= 0) {
                this.fleeCooldown = 15;
                Vec3 awayPos = this.mob.findPosAwayFromWithAnchor(dangerPos, distance, anchorPos, maxDistFromAnchor);
                if (awayPos != null) {
                    this.mob.getNavigation().moveTo(awayPos.x, awayPos.y, awayPos.z, speedModifier);
                } else if (anchorPos != null) {
                    this.mob.getNavigation().moveTo(anchorPos.x, anchorPos.y, anchorPos.z, speedModifier);
                }
            }
        }
    }

    // 0. Meta: Tomar inmediatamente sus piezas de armadura del soporte de armadura al despertar
    public static class KoboldRetrieveArmorGoal extends Goal {
        private final KoboldGirlEntity mob;
        private int timeout = 0;

        public KoboldRetrieveArmorGoal(KoboldGirlEntity mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.mob.isSleeping()) {
                return false;
            }
            if (!this.mob.needsArmorRetrieval && !(!this.mob.isSleepTime() && this.mob.doesAssignedArmorStandHaveArmor())) {
                return false;
            }
            ArmorStand stand = this.mob.getAssignedArmorStandEntity();
            if (stand == null || !stand.isAlive()) {
                this.mob.needsArmorRetrieval = false;
                return false;
            }
            return this.mob.doesAssignedArmorStandHaveArmor();
        }

        @Override
        public boolean canContinueToUse() {
            if (this.mob.isSleeping()) {
                return false;
            }
            if (this.timeout > 120) {
                return false;
            }
            ArmorStand stand = this.mob.getAssignedArmorStandEntity();
            if (stand == null || !stand.isAlive()) {
                return false;
            }
            return this.mob.doesAssignedArmorStandHaveArmor();
        }

        @Override
        public void start() {
            this.timeout = 0;
            ArmorStand stand = this.mob.getAssignedArmorStandEntity();
            if (stand != null) {
                this.mob.getNavigation().moveTo(stand, 1.3D);
            }
        }

        @Override
        public void stop() {
            this.timeout = 0;
            this.mob.needsArmorRetrieval = false;
            this.mob.getNavigation().stop();
        }

        @Override
        public void tick() {
            ArmorStand stand = this.mob.getAssignedArmorStandEntity();
            if (stand == null || !stand.isAlive()) {
                this.mob.needsArmorRetrieval = false;
                return;
            }

            this.timeout++;
            this.mob.getLookControl().setLookAt(stand, 30.0F, 30.0F);

            double distSqr = this.mob.distanceToSqr(stand);
            if (distSqr > 4.0D) {
                if (this.mob.getNavigation().isDone() || this.mob.tickCount % 10 == 0) {
                    this.mob.getNavigation().moveTo(stand, 1.3D);
                }
            } else {
                // Llegó al soporte de armadura: retirar y equipar cada pieza
                this.mob.getNavigation().stop();
                this.mob.setDeltaMovement(Vec3.ZERO);
                boolean retrievedAny = false;
                for (EquipmentSlot slot : ARMOR_SLOTS) {
                    ItemStack standArmor = stand.getItemBySlot(slot);
                    if (!standArmor.isEmpty()) {
                        stand.setItemSlot(slot, ItemStack.EMPTY);
                        ItemStack current = this.mob.getItemBySlot(slot);
                        if (!current.isEmpty()) {
                            ItemStack leftover = this.mob.getInventory().addItem(current);
                            if (!leftover.isEmpty() && this.mob.level() instanceof ServerLevel serverLevel) {
                                this.mob.spawnAtLocation(serverLevel, leftover);
                            }
                        }
                        this.mob.setItemSlot(slot, standArmor);
                        retrievedAny = true;
                    }
                }

                if (retrievedAny) {
                    this.mob.playSound(SoundEvents.ARMOR_EQUIP_GENERIC.value(), 1.0F, 1.0F);
                    if (this.mob.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                                stand.getX(), stand.getY() + 1.2D, stand.getZ(),
                                6, 0.25D, 0.25D, 0.25D, 0.05D);
                    }
                }

                this.mob.needsArmorRetrieval = false;
            }
        }
    }

    // 7. Meta: Dormir en cama asignada durante la noche
    public static class KoboldSleepInBedGoal extends Goal {
        private final KoboldGirlEntity mob;
        private int waitTicks = 0;

        public KoboldSleepInBedGoal(KoboldGirlEntity mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            if (this.mob.sleepCooldown > 0 || this.mob.isInPowerStruggle()) {
                return false;
            }
            if (this.mob.isOwned() && this.mob.getCommandStance() == CommandStance.FOLLOW) {
                return false;
            }
            if (this.mob.isInQueenDuel() || getNearbyQueenDuel(this.mob.level(), this.mob.position(), 24.0D) != null) {
                return false;
            }
            if (!this.mob.isSleepTime()) {
                return false;
            }
            if (this.mob.getTarget() != null) {
                return false;
            }
            if (!this.mob.hasAssignedBed()) {
                BlockPos bed = this.mob.findNearestUnclaimedBed(32);
                if (bed != null) {
                    this.mob.setAssignedBed(bed);
                } else {
                    return false;
                }
            }
            return this.mob.isAssignedBedValid();
        }

        @Override
        public boolean canContinueToUse() {
            if (this.mob.sleepCooldown > 0 || this.mob.isInPowerStruggle()) {
                return false;
            }
            if (this.mob.isOwned() && this.mob.getCommandStance() == CommandStance.FOLLOW) {
                return false;
            }
            if (this.mob.isInQueenDuel() || getNearbyQueenDuel(this.mob.level(), this.mob.position(), 24.0D) != null) {
                return false;
            }
            if (!this.mob.isSleepTime()) {
                return false;
            }
            if (this.mob.getTarget() != null) {
                return false;
            }
            if (!this.mob.hasAssignedBed() || !this.mob.isAssignedBedValid()) {
                return false;
            }
            return true;
        }

        @Override
        public void start() {
            this.waitTicks = 0;
            BlockPos bed = this.mob.getAssignedBedPos();
            if (bed != null) {
                Vec3 center = getBedCenter(bed);
                this.mob.getNavigation().moveTo(center.x, bed.getY(), center.z, 1.15D);
            }
        }

        private Vec3 getBedCenter(BlockPos headPos) {
            BlockState state = this.mob.level().getBlockState(headPos);
            Direction facing = (state.getBlock() instanceof BedBlock) ? state.getValue(BedBlock.FACING) : Direction.NORTH;
            BlockPos footPos = headPos.relative(facing.getOpposite());
            double centerX = (Math.min(headPos.getX(), footPos.getX()) + Math.max(headPos.getX(), footPos.getX()) + 1.0D) / 2.0D;
            double centerZ = (Math.min(headPos.getZ(), footPos.getZ()) + Math.max(headPos.getZ(), footPos.getZ()) + 1.0D) / 2.0D;
            return new Vec3(centerX, headPos.getY(), centerZ);
        }

        public boolean isBesideBed(BlockPos bed) {
            if (this.mob.isSleeping()) {
                return false;
            }

            BlockState state = this.mob.level().getBlockState(bed);
            if (!(state.getBlock() instanceof BedBlock)) {
                return false;
            }
            Direction facing = state.getValue(BedBlock.FACING);
            BlockPos footPos = bed.relative(facing.getOpposite());

            double minX = Math.min(bed.getX(), footPos.getX());
            double maxX = Math.max(bed.getX(), footPos.getX()) + 1.0D;
            double minZ = Math.min(bed.getZ(), footPos.getZ());
            double maxZ = Math.max(bed.getZ(), footPos.getZ()) + 1.0D;

            double clampedX = Math.clamp(this.mob.getX(), minX, maxX);
            double clampedZ = Math.clamp(this.mob.getZ(), minZ, maxZ);
            double dx = this.mob.getX() - clampedX;
            double dz = this.mob.getZ() - clampedZ;
            double horizDistSq = dx * dx + dz * dz;

            // Histéresis: si ya estaba esperando junto a la cama, tolerar pequeños empujes de otras entidades (hasta 1.6 bloques)
            // Si apenas va llegando, requiere estar en el perímetro inmediato adyacente (a 1.25 bloques o menos del borde)
            double maxHorizDistSq = (this.waitTicks > 0) ? 2.56D : 1.5625D;
            if (horizDistSq > maxHorizDistSq) {
                return false;
            }

            // Misma altura aproximada (diferencia vertical menor a 1.0 bloque)
            double vertDist = Math.abs(this.mob.getY() - bed.getY());
            if (vertDist > 1.0D) {
                return false;
            }

            return true;
        }

        @Override
        public void tick() {
            BlockPos bed = this.mob.getAssignedBedPos();
            if (bed == null || !this.mob.isAssignedBedValid()) {
                if (this.mob.isSleeping()) {
                    this.mob.stopSleeping();
                }
                this.waitTicks = 0;
                return;
            }

            // Si ya está durmiendo en la cama, mantenerse inmóvil
            if (this.mob.isSleeping()) {
                this.mob.getNavigation().stop();
                this.waitTicks = 0;
                return;
            }

            // 1. Si lleva armadura equipada y tiene o encuentra un soporte de armadura asignado cerca de la cama,
            // primero depositar las piezas de armadura en el soporte antes de acostarse
            if (this.mob.hasAnyArmorEquipped()) {
                ArmorStand stand = this.mob.getAssignedArmorStandEntity();
                if (stand == null) {
                    stand = this.mob.findNearestUnclaimedArmorStand(8.0D);
                    if (stand != null) {
                        this.mob.setAssignedArmorStand(stand);
                    }
                }

                if (stand != null) {
                    double distToStandSqr = this.mob.distanceToSqr(stand);
                    if (distToStandSqr > 4.0D) {
                        this.waitTicks = 0;
                        this.mob.getLookControl().setLookAt(stand, 30.0F, 30.0F);
                        if (this.mob.getNavigation().isDone() || this.mob.tickCount % 10 == 0) {
                            this.mob.getNavigation().moveTo(stand, 1.15D);
                        }
                        return;
                    } else {
                        // Junto al soporte de armadura: depositar cada pieza
                        this.mob.getNavigation().stop();
                        this.mob.setDeltaMovement(Vec3.ZERO);
                        this.mob.getLookControl().setLookAt(stand, 30.0F, 30.0F);

                        boolean depositedAny = false;
                        for (EquipmentSlot slot : ARMOR_SLOTS) {
                            ItemStack worn = this.mob.getItemBySlot(slot);
                            if (!worn.isEmpty()) {
                                ItemStack standCurrent = stand.getItemBySlot(slot);
                                if (standCurrent.isEmpty()) {
                                    stand.setItemSlot(slot, worn.copy());
                                    this.mob.setItemSlot(slot, ItemStack.EMPTY);
                                    depositedAny = true;
                                } else {
                                    ItemStack leftover = this.mob.getInventory().addItem(worn);
                                    this.mob.setItemSlot(slot, leftover);
                                }
                            }
                        }

                        if (depositedAny) {
                            this.mob.playSound(SoundEvents.ARMOR_STAND_PLACE, 1.0F, 1.0F);
                            if (this.mob.level() instanceof ServerLevel serverLevel) {
                                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                                        stand.getX(), stand.getY() + 1.2D, stand.getZ(),
                                        6, 0.25D, 0.25D, 0.25D, 0.05D);
                            }
                        }
                        return;
                    }
                }
            }

            Vec3 bedCenter = getBedCenter(bed);

            // Verificar si ya está al lado de la cama
            if (this.isBesideBed(bed)) {
                // Detener movimiento y mirar fijamente hacia el centro de la cama
                this.mob.getNavigation().stop();
                this.mob.setDeltaMovement(Vec3.ZERO);
                this.mob.getLookControl().setLookAt(bedCenter.x, bed.getY() + 0.6D, bedCenter.z, 30.0F, 30.0F);

                // Esperar exactamente 2 segundos (40 ticks) antes de acostarse
                this.waitTicks++;
                if (this.waitTicks >= 40) {
                    this.mob.startSleeping(bed);
                    this.waitTicks = 0;
                }
            } else {
                // Si todavía no está junto a la cama, reiniciar la espera y navegar hacia la cama
                this.waitTicks = 0;
                if (this.mob.getNavigation().isDone() || this.mob.tickCount % 10 == 0) {
                    this.mob.getNavigation().moveTo(bedCenter.x, bed.getY(), bedCenter.z, 1.15D);
                }
            }
        }

        @Override
        public void stop() {
            this.waitTicks = 0;
            if (this.mob.isSleeping()) {
                this.mob.stopSleeping();
            }
            this.mob.getNavigation().stop();
        }
    }

    // 8. Meta: Al levantarse de la cama, caminar por Dirt Path hacia otra zona para deambular
    public static class KoboldMorningWalkGoal extends Goal {
        private final KoboldGirlEntity mob;
        private BlockPos destination;
        private int timeout;

        public KoboldMorningWalkGoal(KoboldGirlEntity mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!this.mob.needsMorningWalk) {
                return false;
            }
            // Guardias y Vigilantes patrullan de forma activa y continua con KoboldPatrolPathsGoal
            if (this.mob.isDefender()) {
                this.mob.needsMorningWalk = false;
                return false;
            }
            if (this.mob.isSleepTime()) {
                this.mob.needsMorningWalk = false;
                return false;
            }
            if (this.mob.getTarget() != null) {
                this.mob.needsMorningWalk = false;
                return false;
            }

            this.destination = this.mob.findMorningWalkDestination();
            if (this.destination == null) {
                // No hay caminos de Dirt Path cercanos o seguros que se alejen de la cama
                this.mob.needsMorningWalk = false;
                return false;
            }
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            if (!this.mob.needsMorningWalk || this.mob.isSleepTime() || this.mob.getTarget() != null) {
                return false;
            }
            if (this.destination == null) {
                return false;
            }
            if (this.mob.distanceToSqr(Vec3.atCenterOf(this.destination)) < 4.0D) {
                return false;
            }
            return this.timeout > 0 && !this.mob.getNavigation().isDone();
        }

        @Override
        public void start() {
            this.timeout = 300; // Máximo 15 segundos
            if (this.destination != null) {
                this.mob.getNavigation().moveTo(
                        this.destination.getX() + 0.5D,
                        this.destination.getY(),
                        this.destination.getZ() + 0.5D,
                        1.15D
                );
            }
        }

        @Override
        public void tick() {
            this.timeout--;
            if (this.destination != null && (this.mob.getNavigation().isDone() || this.timeout % 40 == 0)) {
                this.mob.getNavigation().moveTo(
                        this.destination.getX() + 0.5D,
                        this.destination.getY(),
                        this.destination.getZ() + 0.5D,
                        1.15D
                );
            }
        }

        @Override
        public void stop() {
            this.mob.needsMorningWalk = false;
            this.destination = null;
            this.mob.getNavigation().stop();
        }
    }

    // Meta: Usar cofres para categorizar, almacenar, reabastecerse y organizar recursos
    public static class KoboldChestGoal extends Goal {
        private final KoboldGirlEntity kobold;
        private ChestTask currentTask = null;
        private int chestOpenTicks = 0;
        private int searchCooldown = 0;
        private int reachTimeout = 0;

        public KoboldChestGoal(KoboldGirlEntity mob) {
            this.kobold = mob;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.kobold.isSleeping() || this.kobold.isThreatened() || this.kobold.isHunting()) {
                return false;
            }
            if (this.kobold.isOwned() && this.kobold.getCommandStance() != CommandStance.WORK) {
                return false;
            }
            if (this.searchCooldown > 0) {
                this.searchCooldown--;
                return false;
            }

            ChestTask task = this.kobold.findBestChestTask();
            if (task == null) {
                this.searchCooldown = 30; // 1.5s antes de volver a escanear
                return false;
            }

            this.currentTask = task;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.kobold.isSleeping() || this.kobold.isThreatened() || this.kobold.isHunting()) {
                return false;
            }
            if (this.kobold.isOwned() && this.kobold.getCommandStance() != CommandStance.WORK) {
                return false;
            }
            if (this.currentTask == null || this.currentTask.chestPos == null) {
                return false;
            }
            Container container = getChestContainer(this.kobold.level(), this.currentTask.chestPos);
            return container != null;
        }

        private BlockPos getStandablePosNearChest(BlockPos chestPos) {
            Level level = this.kobold.level();
            BlockPos best = null;
            double bestDist = Double.MAX_VALUE;
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos adj = chestPos.relative(dir);
                BlockPos below = adj.below();
                BlockState belowState = level.getBlockState(below);
                BlockState state = level.getBlockState(adj);
                BlockState above = level.getBlockState(adj.above());

                boolean groundOk = belowState.isFaceSturdy(level, below, Direction.UP) || belowState.isSolid();
                boolean spaceOk = !state.blocksMotion() && !above.blocksMotion();
                boolean fluidOk = !state.getFluidState().is(FluidTags.LAVA) && !state.getFluidState().is(FluidTags.WATER);

                if (groundOk && spaceOk && fluidOk) {
                    double d = this.kobold.distanceToSqr(Vec3.atCenterOf(adj));
                    if (d < bestDist) {
                        bestDist = d;
                        best = adj;
                    }
                }
            }
            if (best != null) return best;
            return chestPos;
        }

        @Override
        public void start() {
            this.chestOpenTicks = 0;
            this.reachTimeout = 200; // 10 segundos máximo para alcanzar el cofre
            if (this.currentTask != null) {
                this.kobold.unequipHands();
                BlockPos standPos = this.getStandablePosNearChest(this.currentTask.chestPos);
                this.kobold.getNavigation().moveTo(
                        standPos.getX() + 0.5D,
                        standPos.getY(),
                        standPos.getZ() + 0.5D,
                        1.2D
                );
            }
        }

        @Override
        public void stop() {
            if (this.chestOpenTicks > 0 && this.currentTask != null) {
                BlockState state = this.kobold.level().getBlockState(this.currentTask.chestPos);
                this.kobold.playSound(state.is(Blocks.BARREL) ? SoundEvents.BARREL_CLOSE : SoundEvents.CHEST_CLOSE, 0.5F, 1.0F);
            }
            this.currentTask = null;
            this.chestOpenTicks = 0;
            this.reachTimeout = 0;
            this.searchCooldown = 30;
        }

        @Override
        public void tick() {
            if (this.currentTask == null) return;

            this.kobold.getLookControl().setLookAt(
                    this.currentTask.chestPos.getX() + 0.5D,
                    this.currentTask.chestPos.getY() + 0.5D,
                    this.currentTask.chestPos.getZ() + 0.5D,
                    30.0F, 30.0F
            );

            double distSqr = this.kobold.distanceToSqr(Vec3.atCenterOf(this.currentTask.chestPos));

            if (distSqr > 9.0D) { // A más de 3 bloques
                this.reachTimeout--;
                if (this.reachTimeout <= 0) {
                    this.searchCooldown = 60; // 3s cooldown tras atasco
                    this.currentTask = null;
                    return;
                }
                this.chestOpenTicks = 0;
                if (this.kobold.getNavigation().isDone() || this.kobold.tickCount % 15 == 0) {
                    BlockPos standPos = this.getStandablePosNearChest(this.currentTask.chestPos);
                    this.kobold.getNavigation().moveTo(
                            standPos.getX() + 0.5D,
                            standPos.getY(),
                            standPos.getZ() + 0.5D,
                            1.2D
                    );
                }
            } else {
                // Llegó junto al cofre (a <= 3 bloques)
                this.kobold.getNavigation().stop();
                this.kobold.setDeltaMovement(Vec3.ZERO);

                if (this.chestOpenTicks == 0) {
                    BlockState state = this.kobold.level().getBlockState(this.currentTask.chestPos);
                    this.kobold.playSound(state.is(Blocks.BARREL) ? SoundEvents.BARREL_OPEN : SoundEvents.CHEST_OPEN, 0.5F, 1.0F);
                    this.chestOpenTicks = 1;
                } else if (this.chestOpenTicks < 15) {
                    this.chestOpenTicks++;
                } else {
                    Container container = getChestContainer(this.kobold.level(), this.currentTask.chestPos);
                    if (container != null) {
                        this.kobold.performCategorizedChestOperations(container, this.currentTask);
                    }

                    BlockState state = this.kobold.level().getBlockState(this.currentTask.chestPos);
                    this.kobold.playSound(state.is(Blocks.BARREL) ? SoundEvents.BARREL_CLOSE : SoundEvents.CHEST_CLOSE, 0.5F, 1.0F);

                    if (this.kobold.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                                this.currentTask.chestPos.getX() + 0.5D,
                                this.currentTask.chestPos.getY() + 0.8D,
                                this.currentTask.chestPos.getZ() + 0.5D,
                                6, 0.25D, 0.25D, 0.25D, 0.05D
                        );
                    }

                    this.chestOpenTicks = 0;
                    this.searchCooldown = 40;
                    this.currentTask = null;
                }
            }
        }
    }

    // 9. Meta: Deambular seguro (prioriza Dirt Path, evita desniveles >2 bloques y bloques o líquidos peligrosos)
    public static class SafeKoboldStrollGoal extends WaterAvoidingRandomStrollGoal {
        private final KoboldGirlEntity kobold;

        public SafeKoboldStrollGoal(KoboldGirlEntity mob, double speedModifier) {
            super(mob, speedModifier);
            this.kobold = mob;
        }

        @Override
        public boolean canUse() {
            if (this.kobold.isOwned() && this.kobold.getCommandStance() != CommandStance.WORK) {
                return false;
            }
            // Guardias y Vigilantes patrullan de forma activa y continua los caminos con KoboldPatrolPathsGoal
            if (this.kobold.isDefender()) {
                return false;
            }
            return super.canUse();
        }

        @Override
        protected Vec3 getPosition() {
            Vec3 bestCandidate = null;
            double bestScore = Double.NEGATIVE_INFINITY;
            KoboldGirlEntity queen = this.kobold.isRebel() ? this.kobold.getTribeQueen() : null;
            Vec3 queenPos = (queen != null && queen.isAlive()) ? queen.position() : null;

            // Intentar hasta 8 veces encontrar una posición de deambulación segura, priorizando Dirt Path
            for (int attempt = 0; attempt < 8; attempt++) {
                Vec3 candidate = super.getPosition();
                if (candidate != null) {
                    // Si es rebelde y la reina está viva, no deambular a más de 15 bloques de la Reina
                    if (queenPos != null && candidate.distanceTo(queenPos) > 15.0D) {
                        continue;
                    }
                    BlockPos targetPos = BlockPos.containing(candidate);
                    // Evitar elegir posiciones cercanas a precipicios (>2 bloques) o peligros
                    if (!isNearDangerousDrop(this.kobold.level(), targetPos, 2)
                            && !isNearHazard(this.kobold.level(), targetPos, 2)) {
                        double score = isDirtPath(this.kobold.level(), targetPos) ? 100.0D : 10.0D;
                        if (score > bestScore) {
                            bestScore = score;
                            bestCandidate = candidate;
                            // Si encontramos una sobre Dirt Path, seleccionarla de inmediato
                            if (score >= 100.0D) {
                                break;
                            }
                        }
                    }
                }
            }
            if (bestCandidate == null && queen != null && this.kobold.distanceTo(queen) > 10.0D) {
                Vec3 dir = queenPos.subtract(this.kobold.position()).normalize().scale(6.0D);
                BlockPos ground = this.kobold.level().getHeightmapPos(
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        BlockPos.containing(this.kobold.position().add(dir))
                );
                if (!isNearDangerousDrop(this.kobold.level(), ground, 2) && !isNearHazard(this.kobold.level(), ground, 2)) {
                    return Vec3.atBottomCenterOf(ground);
                }
            }
            return bestCandidate;
        }
    }

    // 10. Meta: Patrullar caminos de tierra y sus alrededores en escuadrones coordinados (Exclusivo Guardias y Vigilantes)
    public static class KoboldPatrolPathsGoal extends Goal {
        private final KoboldGirlEntity mob;
        private final double speed;
        private BlockPos patrolTarget;
        private BlockPos lastPatrolPos;
        private int sentryInspectionTicks = 0;
        private int lookCycleTimer = 0;
        private float scanYawOffset = 0.0F;
        private int pathRecomputeTimer = 0;
        private int squadCheckTimer = 0;

        public KoboldPatrolPathsGoal(KoboldGirlEntity mob, double speed) {
            this.mob = mob;
            this.speed = speed;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.mob.isOwned() && this.mob.getCommandStance() != CommandStance.WORK) {
                return false;
            }
            // Solo para Guardias, Vigilantes y la Reina de la tribu
            if (!this.mob.isDefender() && !this.mob.isAnyQueen()) {
                return false;
            }
            // Si está durmiendo o es hora de dormir (durante el atardecer para defensoras o de 3:00 AM a 6:00 AM para la reina), no patrulla
            if (this.mob.isSleeping() || this.mob.isSleepTime()) {
                return false;
            }
            // Si está en combate o amenazada, las metas de combate tienen prioridad
            if (this.mob.getTarget() != null || this.mob.isThreatened()) {
                return false;
            }
            // Si tiene que recuperar su armadura del soporte, hacerlo primero
            if (this.mob.isNeedsArmorRetrieval()) {
                return false;
            }
            // Si está en duelo de reinas, lucha por el poder o mirando el báculo
            if (this.mob.isInQueenDuel() || this.mob.isInPowerStruggle() || this.mob.isStaringAtBaculo) {
                return false;
            }
            // Durante la jornada de almacenamiento, si tiene exceso de comida o mochila llena, ceder el paso para almacenar en cofres
            if (this.mob.isStorageAndOrganizationTime() && (this.mob.hasExcessFoodToStore() || !this.mob.hasFreeInventorySlot())) {
                return false;
            }
            // Si la reina está cazando presas durante la mañana, no patrulla simultáneamente
            if (this.mob.isAnyQueen() && this.mob.isMorningWorkTime() && this.mob.isHunting()) {
                return false;
            }

            // Actualizar o sincronizar el escuadrón de patrulla periódicamente
            this.squadCheckTimer++;
            if (this.squadCheckTimer >= 60 || this.mob.patrolSquadLeaderUUID == null) {
                this.squadCheckTimer = 0;
                this.mob.updatePatrolSquad();
            }

            // Comportamiento según rol en el escuadrón
            if (this.mob.isPatrolSquadLeader()) {
                this.patrolTarget = this.findPatrolWaypoint();
                return this.patrolTarget != null;
            } else {
                KoboldGirlEntity leader = this.mob.getPatrolSquadLeaderEntity();
                return leader != null && leader.isAlive();
            }
        }

        @Override
        public boolean canContinueToUse() {
            if (!this.mob.isDefender() && !this.mob.isAnyQueen()) {
                return false;
            }
            if (this.mob.isSleeping() || this.mob.isSleepTime()) {
                return false;
            }
            if (this.mob.getTarget() != null || this.mob.isThreatened()) {
                return false;
            }
            if (this.mob.isNeedsArmorRetrieval()) {
                return false;
            }
            if (this.mob.isInQueenDuel() || this.mob.isInPowerStruggle() || this.mob.isStaringAtBaculo) {
                return false;
            }
            if (this.mob.isStorageAndOrganizationTime() && (this.mob.hasExcessFoodToStore() || !this.mob.hasFreeInventorySlot())) {
                return false;
            }
            if (this.mob.isAnyQueen() && this.mob.isMorningWorkTime() && this.mob.isHunting()) {
                return false;
            }

            if (this.mob.isPatrolSquadLeader()) {
                return this.patrolTarget != null;
            } else {
                KoboldGirlEntity leader = this.mob.getPatrolSquadLeaderEntity();
                return leader != null && leader.isAlive() && leader.isDefender() && !leader.isSleeping();
            }
        }

        @Override
        public void start() {
            this.sentryInspectionTicks = 0;
            this.lookCycleTimer = 0;
            this.pathRecomputeTimer = 0;

            // Asegurar que porte su arma y escudo adecuados listos para la guardia
            this.mob.autoEquipWeaponsAndShield();

            if (this.mob.isPatrolSquadLeader()) {
                this.mob.setInspectingSentry(false);
                if (this.patrolTarget != null) {
                    this.mob.getNavigation().moveTo(
                            this.patrolTarget.getX() + 0.5D,
                            this.patrolTarget.getY(),
                            this.patrolTarget.getZ() + 0.5D,
                            this.speed
                    );
                }
            }
        }

        @Override
        public void stop() {
            this.patrolTarget = null;
            this.sentryInspectionTicks = 0;
            this.mob.setInspectingSentry(false);
            this.mob.getNavigation().stop();
        }

        @Override
        public void tick() {
            // Periódicamente verificar consistencia del escuadrón (cada 60 ticks)
            this.squadCheckTimer++;
            if (this.squadCheckTimer >= 60) {
                this.squadCheckTimer = 0;
                this.mob.updatePatrolSquad();
            }

            if (this.mob.isPatrolSquadLeader()) {
                this.tickLeader();
            } else {
                this.tickFollower();
            }
        }

        private void tickLeader() {
            // Si el líder no tiene objetivo pero un seguidor del escuadrón sí lo tiene, acudir a defenderlo
            if (this.mob.getTarget() == null) {
                List<KoboldGirlEntity> followers = this.mob.level().getEntitiesOfClass(
                        KoboldGirlEntity.class,
                        this.mob.getBoundingBox().inflate(16.0D, 8.0D, 16.0D),
                        k -> k.isAlive() && k.isDefender() && this.mob.getUUID().equals(k.patrolSquadLeaderUUID) && k.getTarget() != null && k.getTarget().isAlive()
                );
                if (!followers.isEmpty()) {
                    this.mob.setTarget(followers.get(0).getTarget());
                    return;
                }
            }

            if (this.patrolTarget == null) {
                this.patrolTarget = this.findPatrolWaypoint();
                if (this.patrolTarget == null) return;
            }

            double distSq = this.mob.distanceToSqr(Vec3.atCenterOf(this.patrolTarget));

            // Si llegó al punto de patrulla o el camino se completó
            if (distSq < 3.5D || this.mob.getNavigation().isDone()) {
                this.mob.getNavigation().stop();
                this.mob.setInspectingSentry(true);
                this.sentryInspectionTicks++;

                // Mantener vigilancia activa (girar la vista cada 20 ticks para escanear el perímetro frontal)
                this.lookCycleTimer++;
                if (this.lookCycleTimer >= 20) {
                    this.lookCycleTimer = 0;
                    this.scanYawOffset = (this.mob.getRandom().nextFloat() - 0.5F) * 100.0F;
                }

                float targetYaw = this.mob.getYRot() + this.scanYawOffset;
                double rad = Math.toRadians(targetYaw);
                double lookX = this.mob.getX() - Math.sin(rad) * 8.0D;
                double lookZ = this.mob.getZ() + Math.cos(rad) * 8.0D;
                this.mob.getLookControl().setLookAt(lookX, this.mob.getEyeY(), lookZ, 30.0F, 30.0F);

                // Tras 50 ticks (2.5 segundos) de inspección atenta del área, continuar al siguiente punto de patrulla
                if (this.sentryInspectionTicks >= 50) {
                    this.mob.setInspectingSentry(false);
                    this.lastPatrolPos = this.patrolTarget;
                    this.patrolTarget = this.findPatrolWaypoint();
                    this.sentryInspectionTicks = 0;
                    if (this.patrolTarget != null) {
                        this.mob.getNavigation().moveTo(
                                this.patrolTarget.getX() + 0.5D,
                                this.patrolTarget.getY(),
                                this.patrolTarget.getZ() + 0.5D,
                                this.speed
                        );
                    }
                }
            } else {
                // En marcha hacia el punto de patrulla
                this.mob.setInspectingSentry(false);
                this.pathRecomputeTimer++;
                // Si la navegación terminó pero aún está lejos, o tras 100 ticks (5 segundos), refrescar o buscar nuevo punto
                if (this.mob.getNavigation().isDone() || this.pathRecomputeTimer >= 100) {
                    this.pathRecomputeTimer = 0;
                    this.lastPatrolPos = this.patrolTarget;
                    this.patrolTarget = this.findPatrolWaypoint();
                    if (this.patrolTarget != null) {
                        this.mob.getNavigation().moveTo(
                                this.patrolTarget.getX() + 0.5D,
                                this.patrolTarget.getY(),
                                this.patrolTarget.getZ() + 0.5D,
                                this.speed
                        );
                    }
                }
            }
        }

        private void tickFollower() {
            KoboldGirlEntity leader = this.mob.getPatrolSquadLeaderEntity();
            if (leader == null || !leader.isAlive()) {
                this.mob.updatePatrolSquad();
                return;
            }

            // Si el líder entra en combate, asistirlo de inmediato
            if (leader.getTarget() != null && leader.getTarget().isAlive() && this.mob.getTarget() == null) {
                this.mob.setTarget(leader.getTarget());
                return;
            }

            double distToLeader = this.mob.distanceTo(leader);
            int squadIndex = this.mob.getPatrolSquadIndexInGroup();
            int squadSize = Math.max(2, this.mob.getPatrolSquadSize());

            // 1. Si el líder está en inspección de centinela (en el punto de patrulla)
            if (leader.isInspectingSentry()) {
                // Si está dentro del radio de guardia del líder (<= 3.5 bloques), detenerse y cubrir la retaguardia / ángulos ciegos
                if (distToLeader <= 3.5D) {
                    this.mob.getNavigation().stop();

                    this.lookCycleTimer++;
                    if (this.lookCycleTimer >= 20) {
                        this.lookCycleTimer = 0;
                        this.scanYawOffset = (this.mob.getRandom().nextFloat() - 0.5F) * 60.0F;
                    }

                    // Ángulo de cobertura complementario:
                    // En dúo (squadSize == 2): Retaguardia exacta (180° opuesto al líder)
                    // En trío/cuarteto: Distribución angular equilibrada
                    float coverageAngleOffset = 180.0F;
                    if (squadSize == 3) {
                        coverageAngleOffset = (squadIndex == 1) ? 120.0F : -120.0F;
                    } else if (squadSize >= 4) {
                        coverageAngleOffset = (squadIndex == 1) ? 90.0F : ((squadIndex == 2) ? 180.0F : -90.0F);
                    }

                    float targetYaw = leader.getYRot() + coverageAngleOffset + this.scanYawOffset;
                    double rad = Math.toRadians(targetYaw);
                    double lookX = this.mob.getX() - Math.sin(rad) * 8.0D;
                    double lookZ = this.mob.getZ() + Math.cos(rad) * 8.0D;
                    this.mob.getLookControl().setLookAt(lookX, this.mob.getEyeY(), lookZ, 30.0F, 30.0F);
                    return;
                }
            }

            // 2. En marcha de escolta tras el líder
            this.pathRecomputeTimer++;
            if (this.pathRecomputeTimer >= 10 || this.mob.getNavigation().isDone()) {
                this.pathRecomputeTimer = 0;

                // Calcular posición de formación de escolta (detrás del líder con ligero offset lateral)
                Vec3 look = leader.getLookAngle();
                Vec3 behind = new Vec3(-look.x, 0.0D, -look.z).normalize().scale(2.6D);

                // Desplazamiento lateral perpendicular según el índice en el escuadrón
                double sideSign = (squadIndex % 2 == 1) ? 1.0D : -1.0D;
                double sideMultiplier = 1.0D + ((squadIndex - 1) / 2) * 0.8D;
                Vec3 side = new Vec3(-look.z, 0.0D, look.x).normalize().scale(sideSign * 1.4D * sideMultiplier);

                Vec3 targetPos = leader.position().add(behind).add(side);
                BlockPos targetBlock = BlockPos.containing(targetPos);

                Level level = this.mob.level();
                BlockPos safeStand = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, targetBlock);

                double moveSpeed = this.speed;
                // Si se quedó rezagada (> 6 bloques), acelerar el paso para mantener la formación
                if (distToLeader > 6.0D) {
                    moveSpeed *= 1.35D;
                } else if (distToLeader < 2.0D) {
                    // Si está pegada al líder, esperar para no estorbar el paso
                    this.mob.getNavigation().stop();
                    return;
                }

                if (!isNearDangerousDrop(level, safeStand, 2) && !isNearHazard(level, safeStand, 2)) {
                    this.mob.getNavigation().moveTo(safeStand.getX() + 0.5D, safeStand.getY(), safeStand.getZ() + 0.5D, moveSpeed);
                } else {
                    this.mob.getNavigation().moveTo(leader, moveSpeed);
                }
            }
        }

        private BlockPos findPatrolWaypoint() {
            Level level = this.mob.level();
            BlockPos current = this.mob.blockPosition();
            BlockPos bestPos = null;
            double bestScore = -Double.MAX_VALUE;

            // Determinar ancla central de la aldea / tribu (Reina o Cama)
            BlockPos anchor = this.mob.hasAssignedBed() ? this.mob.getAssignedBedPos() : current;
            KoboldGirlEntity queen = this.mob.getTribeQueen();
            if (queen != null && queen.isAlive()) {
                anchor = queen.blockPosition();
            }

            int squadId = this.mob.getPatrolSquadId();
            int totalSquads = Math.max(1, this.mob.getPatrolSquadTotalGroups());

            // Ángulo objetivo de sector para este escuadrón (reparto radial de 360 grados)
            double targetSectorAngle = (totalSquads > 1) ? ((2.0D * Math.PI * squadId) / totalSquads) : 0.0D;

            // Obtener posiciones de líderes de otros escuadrones para repulsión mutua activa
            List<KoboldGirlEntity> otherLeaders = level.getEntitiesOfClass(
                    KoboldGirlEntity.class,
                    this.mob.getBoundingBox().inflate(48.0D, 16.0D, 48.0D),
                    k -> k.isAlive() && k != this.mob && k.isDefender()
                            && Objects.equals(k.getTribeName(), this.mob.getTribeName())
                            && k.isPatrolSquadLeader()
            );

            int radius = 24;
            BlockPos.MutableBlockPos testPos = new BlockPos.MutableBlockPos();

            // Explorar en un radio de 24 bloques
            for (int dx = -radius; dx <= radius; dx += 2) {
                for (int dz = -radius; dz <= radius; dz += 2) {
                    for (int dy = -3; dy <= 3; dy++) {
                        testPos.set(current.getX() + dx, current.getY() + dy, current.getZ() + dz);

                        // Comprobar si el bloque o el suelo es camino de tierra (Dirt Path)
                        boolean isOnPath = isDirtPath(level, testPos);
                        boolean isNearPath = false;

                        if (!isOnPath) {
                            // Comprobar si está en los alrededores inmediatos de un camino de tierra (a 1-3 bloques)
                            for (Direction dir : Direction.Plane.HORIZONTAL) {
                                BlockPos neighbor = testPos.relative(dir);
                                if (isDirtPath(level, neighbor) || isDirtPath(level, neighbor.relative(dir))) {
                                    isNearPath = true;
                                    break;
                                }
                            }
                        }

                        if (isOnPath || isNearPath) {
                            BlockPos standPos = level.getBlockState(testPos).is(Blocks.DIRT_PATH) ? testPos.above() : testPos.immutable();

                            // Asegurar que la posición para pararse sea segura y transitable
                            if (level.getBlockState(standPos).isAir()
                                    && level.getBlockState(standPos.above()).isAir()
                                    && !isNearDangerousDrop(level, standPos, 2)
                                    && !isNearHazard(level, standPos, 2)) {

                                double distSq = standPos.distSqr(current);
                                // Evitar puntos demasiado cercanos (< 5 bloques) o excesivamente lejanos (> 28 bloques)
                                if (distSq >= 25.0D && distSq <= 784.0D) {
                                    double score = isOnPath ? 100.0D : 70.0D;

                                    // 1. Asignación por sector angular (si hay más de 1 grupo, patrullar áreas distintas)
                                    if (totalSquads > 1) {
                                        double vecX = standPos.getX() - anchor.getX();
                                        double vecZ = standPos.getZ() - anchor.getZ();
                                        double pointAngle = Math.atan2(vecZ, vecX);
                                        if (pointAngle < 0) pointAngle += 2.0D * Math.PI;

                                        double angleDiff = Math.abs(pointAngle - targetSectorAngle);
                                        if (angleDiff > Math.PI) angleDiff = 2.0D * Math.PI - angleDiff;

                                        // Bonificación si el punto está en el sector designado de este escuadrón (+60.0D a -60.0D)
                                        double sectorBonus = (1.0D - (angleDiff / (Math.PI / 2.0D))) * 60.0D;
                                        score += sectorBonus;
                                    }

                                    // 2. Repulsión inter-escuadrones: Evitar coincidir en el mismo sector con otros líderes
                                    for (KoboldGirlEntity otherLeader : otherLeaders) {
                                        double distToOtherLeaderSq = standPos.distSqr(otherLeader.blockPosition());
                                        if (distToOtherLeaderSq < 400.0D) { // < 20 bloques
                                            score -= 80.0D;
                                        } else {
                                            score += 15.0D;
                                        }
                                    }

                                    // 3. Penalizar volver inmediatamente al punto anterior para fomentar patrulla continua en circuito
                                    if (this.lastPatrolPos != null) {
                                        double distToLastSq = standPos.distSqr(this.lastPatrolPos);
                                        if (distToLastSq < 36.0D) {
                                            score -= 40.0D;
                                        }
                                    }

                                    // 4. Si la mob pertenece a una tribu con reina o tiene cama, priorizar no alejarse más de 45 bloques de su base
                                    if (anchor != null) {
                                        double distToAnchorSq = standPos.distSqr(anchor);
                                        if (distToAnchorSq > 45.0D * 45.0D) {
                                            score -= 50.0D;
                                        }
                                    }

                                    // Variación aleatoria para dinamismo natural
                                    score += this.mob.getRandom().nextDouble() * 15.0D;

                                    if (score > bestScore) {
                                        bestScore = score;
                                        bestPos = standPos.immutable();
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Si no se encontraron caminos de tierra cerca, patrullar el sector radial alrededor de la base
            if (bestPos == null) {
                for (int attempt = 0; attempt < 8; attempt++) {
                    double angleVariation = (this.mob.getRandom().nextDouble() - 0.5D) * (Math.PI / 2.0D);
                    double angle = targetSectorAngle + angleVariation;
                    double r = 8.0D + this.mob.getRandom().nextDouble() * 14.0D;
                    int px = anchor.getX() + (int) (Math.cos(angle) * r);
                    int pz = anchor.getZ() + (int) (Math.sin(angle) * r);
                    BlockPos pPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(px, anchor.getY(), pz));

                    if (level.getBlockState(pPos).isAir() && !isNearDangerousDrop(level, pPos, 2) && !isNearHazard(level, pPos, 2)) {
                        bestPos = pPos.immutable();
                        break;
                    }
                }
            }

            return bestPos;
        }
    }

    // Navegación adaptada: descarta destinos inestables o cercanos a precipicios y peligros
    public static class KoboldPathNavigation extends GroundPathNavigation {
        public KoboldPathNavigation(Mob mob, Level level) {
            super(mob, level);
        }

        @Override
        protected PathFinder createPathFinder(int maxVisitedNodes) {
            this.nodeEvaluator = new KoboldNodeEvaluator();
            return new PathFinder(this.nodeEvaluator, maxVisitedNodes);
        }

        @Override
        public boolean isStableDestination(BlockPos pos) {
            if (!super.isStableDestination(pos)) {
                return false;
            }
            if (KoboldGirlEntity.isNearDangerousDrop(this.level, pos, 1)) {
                return false;
            }
            if (KoboldGirlEntity.isNearHazard(this.level, pos, 1)) {
                return false;
            }
            return true;
        }
    }

    // Evaluador de nodos: penaliza fuertemente nodos junto a caídas de más de 2 bloques
    // y prioriza caminar por Dirt Path frente a terrenos normales
    public static class KoboldNodeEvaluator extends WalkNodeEvaluator {
        @Override
        public PathType getPathTypeOfMob(PathfindingContext context, int x, int y, int z, Mob mob) {
            PathType type = super.getPathTypeOfMob(context, x, y, z, mob);
            if (type == PathType.WALKABLE) {
                // Si el nodo está pegado a un desnivel de más de 2 bloques, clasificarlo como DAMAGE_CAUTIOUS
                // para que el pathfinder aplique el malus alto y busque una ruta más alejada del borde
                if (KoboldGirlEntity.isNearDangerousDrop(context.level(), new BlockPos(x, y, z), 1)) {
                    return PathType.DAMAGE_CAUTIOUS;
                }
            }
            return type;
        }

        @Override
        protected Node findAcceptedNode(int x, int y, int z, int jumpSize, double nodeHeight, Direction travelDirection, PathType blockPathTypeCurrent) {
            Node node = super.findAcceptedNode(x, y, z, jumpSize, nodeHeight, travelDirection, blockPathTypeCurrent);
            if (node != null && node.costMalus >= 0.0F) {
                // Si el nodo no es un peligro grave (malus < 16.0F)
                if (node.costMalus < 16.0F && this.currentContext != null) {
                    BlockGetter level = this.currentContext.level();
                    BlockPos nodePos = new BlockPos(node.x, node.y, node.z);
                    BlockPos groundPos = new BlockPos(node.x, node.y - 1, node.z);
                    BlockState nodeBlock = level.getBlockState(nodePos);
                    BlockState groundBlock = level.getBlockState(groundPos);

                    // Evitar caminar o subirse encima de las camas (preferir rodearlas por el suelo)
                    if (nodeBlock.getBlock() instanceof BedBlock || groundBlock.getBlock() instanceof BedBlock) {
                        node.costMalus += 8.0F;
                    }

                    boolean isPath = groundBlock.is(Blocks.DIRT_PATH) || nodeBlock.is(Blocks.DIRT_PATH);

                    if (!isPath) {
                        // Terreno normal fuera del Dirt Path (hierba, tierra, roca):
                        // Añadir coste adicional de +2.0F para que A* priorice fuertemente los Dirt Path
                        node.costMalus += 2.0F;
                    }
                }
            }
            return node;
        }
    }

    /* =========================================================================
       PROTECCIÓN CONTRA FUEGO AMIGO: No se dañan entre sí
       ========================================================================= */

    @Override
    public boolean canAttack(LivingEntity target) {
        if (this.isSpectatingDuel()) return false;
        if (target instanceof KoboldGirlEntity targetKobold && targetKobold.isSpectatingDuel()) return false;
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
        if (target instanceof KoboldGirlEntity otherKobold) {
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
        if (target instanceof KoboldGirlEntity targetKobold && targetKobold.isSpectatingDuel()) {
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
        super.setTarget(target);
    }

    @Override
    protected boolean considersEntityAsAlly(Entity other) {
        if (other instanceof Player player) {
            if (this.isOwnedBy(player) && !this.isRebel()) {
                return true;
            }
        }
        if (other instanceof KoboldGirlEntity otherKobold) {
            if (this.canFightKobold(otherKobold)) {
                return false;
            }
            return true;
        }
        return super.considersEntityAsAlly(other);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        if (target instanceof KoboldGirlEntity otherKobold) {
            if (!this.canFightKobold(otherKobold)) {
                return false;
            }
        }
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

        if (damageSource.getEntity() instanceof KoboldGirlEntity koboldAttacker) {
            // Solo permitir daño mutuo si son rivales válidos (Duelo entre Reinas de diferentes tribus o combate contra rebeldes)
            if (!this.canFightKobold(koboldAttacker)) {
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
        // Una vez que la reina de la tribu llegue a 2 puntos de vida esta no puede recibir más daño
        // y reconoce al jugador como gobernante.
        if (this.isInPlayerDuel()) {
            Player duelPlayer = this.getPlayerDuelTarget();
            if (this.getHealth() <= 2.0F || this.getHealth() - amount <= 2.0F) {
                this.setHealth(2.0F);
                if (duelPlayer != null) {
                    this.resolvePlayerDominanceVictory(duelPlayer);
                } else if (damageSource.getEntity() instanceof Player p) {
                    this.resolvePlayerDominanceVictory(p);
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
                    if (attacker instanceof KoboldGirlEntity koboldAttacker) {
                        if (this.canFightKobold(koboldAttacker)) {
                            this.setTarget(koboldAttacker);
                        }
                    } else if (this.canAttack(attacker)) {
                        this.setTarget(attacker);
                    }
                }
            }
        } else if (!this.isInQueenDuel() && !this.isInPlayerDuel()) {
            if (damageSource.getEntity() instanceof KoboldGirlEntity koboldAttacker && koboldAttacker.isAlive()) {
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
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setQueen(input.getBooleanOr("IsQueen", false));
        this.setTribeName(input.getStringOr("TribeName", ""));
        this.setRebel(input.getBooleanOr("IsRebel", false));
        this.setInPowerStruggle(input.getBooleanOr("IsInPowerStruggle", false));
        this.setSurvivorQueen(input.getBooleanOr("IsSurvivorQueen", false));
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
        if (input.getBooleanOr("HasAssignedBed", false)) {
            int bx = input.getIntOr("BedX", 0);
            int by = input.getIntOr("BedY", 0);
            int bz = input.getIntOr("BedZ", 0);
            this.setAssignedBed(new BlockPos(bx, by, bz));
        }
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
    }

    /* =========================================================================
       ANIMACIONES GECKOLIB
       ========================================================================= */

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("movement", 5, this::movementPredicate));
    }

    private PlayState movementPredicate(AnimationTest<KoboldGirlEntity> state) {
        if (this.isSleeping()) {
            return PlayState.STOP;
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