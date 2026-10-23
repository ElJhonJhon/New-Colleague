package com.newcolleague.mod.entity.ai;

import com.newcolleague.mod.entity.KoboldGirlEntity;
import com.newcolleague.mod.entity.KoboldGirlEntity.KoboldProfession;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

/**
 * Meta de Auto-Crafteo de Emergencia:
 * Permite a las Kobolds detectar herramientas rotas o faltantes para su profesión,
 * abastecerse de insumos (adoquín, palos, tablones, troncos) desde su inventario o cofres comunales,
 * desplazarse a la mesa de crafteo comunal (o crafteo in-situ de emergencia), equipar la herramienta
 * y reanudar el trabajo sin quedar paralizadas.
 */
public class KoboldCraftItemGoal extends Goal {
    private final KoboldGirlEntity kobold;
    private final double speedModifier;
    private @Nullable BlockPos craftingTablePos = null;
    private @Nullable BlockPos targetChestPos = null;
    private int searchCooldown = 0;
    private int craftTicks = 0;

    public KoboldCraftItemGoal(KoboldGirlEntity kobold, double speedModifier) {
        this.kobold = kobold;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    public KoboldCraftItemGoal(KoboldGirlEntity kobold) {
        this(kobold, 1.15D);
    }

    @Override
    public boolean canUse() {
        if (this.kobold.isSleeping() || this.kobold.isInPowerStruggle() || !this.kobold.isAlive()) return false;
        if (!this.kobold.isWorkStance()) return false;
        return this.needsTool();
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void start() {
        this.craftingTablePos = null;
        this.targetChestPos = null;
        this.searchCooldown = 0;
        this.craftTicks = 0;
    }

    @Override
    public void stop() {
        this.craftingTablePos = null;
        this.targetChestPos = null;
        this.kobold.getNavigation().stop();
    }

    @Override
    public void tick() {
        Item neededTool = this.getNeededTool();
        if (neededTool == Items.AIR) {
            this.stop();
            return;
        }

        Level level = this.kobold.level();
        int reqCobble = this.getRequiredCobblestone(neededTool);
        int reqSticks = this.getRequiredSticks(neededTool);

        // 1. Procesar pre-requisitos de madera en inventario (Troncos -> Tablones -> Palos)
        this.tryCraftPrerequisites();

        // 2. Verificar si cuenta con los materiales en el inventario personal
        boolean hasCobble = this.countCobblestoneInInventory() >= reqCobble;
        boolean hasSticks = this.kobold.countItemInInventory(Items.STICK) >= reqSticks;

        if (!hasCobble || !hasSticks) {
            // Buscar materiales faltantes en cofres comunales en un radio de 24 bloques
            if (this.targetChestPos == null || this.searchCooldown <= 0) {
                this.targetChestPos = this.findNearbyChestWithMaterials(level, !hasCobble, !hasSticks);
                this.searchCooldown = 40;
            }

            if (this.targetChestPos != null) {
                double distChest = this.kobold.distanceToSqr(this.targetChestPos.getX() + 0.5D, this.targetChestPos.getY() + 0.5D, this.targetChestPos.getZ() + 0.5D);
                if (distChest <= 6.25D) {
                    this.kobold.getNavigation().stop();
                    this.extractMaterialsFromChest(level, this.targetChestPos, reqCobble, reqSticks);
                    this.tryCraftPrerequisites();
                    this.targetChestPos = null;
                } else if (this.kobold.getNavigation().isDone()) {
                    this.kobold.getNavigation().moveTo(this.targetChestPos.getX() + 0.5D, this.targetChestPos.getY(), this.targetChestPos.getZ() + 0.5D, this.speedModifier);
                }
                return;
            }
        }

        // 3. Una vez reunidos los materiales (o si ya los tenía), buscar la mesa de crafteo comunal
        if (this.craftingTablePos == null || this.searchCooldown <= 0) {
            this.craftingTablePos = this.findNearestCraftingTable(level, 24);
            this.searchCooldown = 60;
        }

        if (this.craftingTablePos != null) {
            double distTable = this.kobold.distanceToSqr(this.craftingTablePos.getX() + 0.5D, this.craftingTablePos.getY() + 0.5D, this.craftingTablePos.getZ() + 0.5D);
            this.kobold.getLookControl().setLookAt(this.craftingTablePos.getX() + 0.5D, this.craftingTablePos.getY() + 0.5D, this.craftingTablePos.getZ() + 0.5D, 30.0F, 30.0F);

            if (distTable <= 6.25D) {
                this.kobold.getNavigation().stop();
                this.craftTicks++;
                if (this.craftTicks >= 10) {
                    this.executeCraft(neededTool, reqCobble, reqSticks);
                    this.craftTicks = 0;
                }
            } else if (this.kobold.getNavigation().isDone()) {
                this.kobold.getNavigation().moveTo(this.craftingTablePos.getX() + 0.5D, this.craftingTablePos.getY(), this.craftingTablePos.getZ() + 0.5D, this.speedModifier);
            }
        } else {
            // Si no hay mesa comunal en 24 bloques, crafteo de emergencia in-situ para no quedar bloqueada
            this.executeCraft(neededTool, reqCobble, reqSticks);
        }
    }

    private boolean needsTool() {
        return this.getNeededTool() != Items.AIR;
    }

    public Item getNeededTool() {
        KoboldProfession prof = this.kobold.getProfession();
        if (prof == KoboldProfession.MINER) {
            if (!this.kobold.hasPickaxe()) return Items.STONE_PICKAXE;
        } else if (prof == KoboldProfession.FARMER) {
            if (!this.kobold.hasHoe()) return Items.STONE_HOE;
        } else if (prof == KoboldProfession.BUILDER || this.kobold.isLogger()) {
            if (!this.kobold.hasAxe()) return Items.STONE_AXE;
            if (!this.kobold.hasShovel()) return Items.STONE_SHOVEL;
            if (!this.kobold.hasPickaxe()) return Items.STONE_PICKAXE;
        } else if (prof == KoboldProfession.HUNTER) {
            if (!this.kobold.hasAxe() && !this.kobold.hasWeaponInInventory()) return Items.STONE_AXE;
        } else if (prof == KoboldProfession.NONE) {
            if (!this.kobold.hasAxe()) return Items.STONE_AXE;
            if (!this.kobold.hasPickaxe()) return Items.STONE_PICKAXE;
        }
        return Items.AIR;
    }

    private int getRequiredCobblestone(Item tool) {
        if (tool == Items.STONE_AXE || tool == Items.STONE_PICKAXE) return 3;
        if (tool == Items.STONE_HOE) return 2;
        if (tool == Items.STONE_SHOVEL) return 1;
        return 0;
    }

    private int getRequiredSticks(Item tool) {
        return 2;
    }

    private boolean hasMaterialsForStoneAxe() {
        return this.countCobblestoneInInventory() >= 3 && this.kobold.countItemInInventory(Items.STICK) >= 2;
    }

    public void tryCraftPrerequisites() {
        // Tronco a Tablones si faltan tablones o palos
        if (this.kobold.countPlanksInInventory() < 2 && this.kobold.countLogsInInventory() > 0) {
            if (this.kobold.consumeLog(1)) {
                this.kobold.getInventory().addItem(new ItemStack(Items.OAK_PLANKS, 4));
            }
        }
        // Tablones a Palos si faltan palos
        if (this.kobold.countItemInInventory(Items.STICK) < 2 && this.kobold.countPlanksInInventory() >= 2) {
            if (this.kobold.consumePlank(2)) {
                this.kobold.getInventory().addItem(new ItemStack(Items.STICK, 4));
            }
        }
    }

    private int countCobblestoneInInventory() {
        return this.kobold.countItemInInventory(Items.COBBLESTONE)
                + this.kobold.countItemInInventory(Items.COBBLED_DEEPSLATE);
    }

    private void consumeCobblestone(int count) {
        int left = count;
        int cobble = this.kobold.countItemInInventory(Items.COBBLESTONE);
        if (cobble > 0) {
            int take = Math.min(cobble, left);
            this.kobold.consumeItem(Items.COBBLESTONE, take);
            left -= take;
        }
        if (left > 0) {
            this.kobold.consumeItem(Items.COBBLED_DEEPSLATE, left);
        }
    }

    private void executeCraft(Item neededTool, int reqCobble, int reqSticks) {
        // Asegurarse de tener palos listos
        this.tryCraftPrerequisites();

        int cobbleAvailable = this.countCobblestoneInInventory();
        int sticksAvailable = this.kobold.countItemInInventory(Items.STICK);

        // Si faltan insumos incluso tras buscar, pero tiene madera, crear herramienta de madera provisional
        if (cobbleAvailable < reqCobble) {
            if (this.kobold.countPlanksInInventory() >= reqCobble && sticksAvailable >= reqSticks) {
                this.kobold.consumePlank(reqCobble);
                this.kobold.consumeItem(Items.STICK, reqSticks);
                Item woodTool = getWoodenEquivalent(neededTool);
                this.equipTool(woodTool);
                return;
            }
            return;
        }

        if (sticksAvailable < reqSticks) return;

        // Consumir insumos reales
        this.consumeCobblestone(reqCobble);
        this.kobold.consumeItem(Items.STICK, reqSticks);

        // Equipar la herramienta resultante
        this.equipTool(neededTool);
    }

    private static Item getWoodenEquivalent(Item stoneTool) {
        if (stoneTool == Items.STONE_AXE) return Items.WOODEN_AXE;
        if (stoneTool == Items.STONE_PICKAXE) return Items.WOODEN_PICKAXE;
        if (stoneTool == Items.STONE_HOE) return Items.WOODEN_HOE;
        if (stoneTool == Items.STONE_SHOVEL) return Items.WOODEN_SHOVEL;
        return stoneTool;
    }

    private void equipTool(Item toolItem) {
        ItemStack oldMain = this.kobold.getMainHandItem();
        if (!oldMain.isEmpty()) {
            ItemStack leftover = this.kobold.getInventory().addItem(oldMain);
            if (!leftover.isEmpty() && this.kobold.level() instanceof ServerLevel sl) {
                this.kobold.spawnAtLocation(sl, leftover);
            }
        }

        ItemStack toolStack = new ItemStack(toolItem);
        this.kobold.setItemSlot(EquipmentSlot.MAINHAND, toolStack);
        this.kobold.swing(InteractionHand.MAIN_HAND);
        this.kobold.playSound(SoundEvents.VILLAGER_WORK_TOOLSMITH, 1.0F, 1.0F);

        if (this.kobold.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.kobold.getX(), this.kobold.getY() + 1.2D, this.kobold.getZ(), 8, 0.2D, 0.2D, 0.2D, 0.05D);
        }
    }

    private @Nullable BlockPos findNearestCraftingTable(Level level, int radius) {
        BlockPos center = this.kobold.blockPosition();
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;

        for (int dy = -4; dy <= 4; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    mpos.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    if (level.getBlockState(mpos).is(Blocks.CRAFTING_TABLE)) {
                        double d = center.distSqr(mpos);
                        if (d < bestDist) {
                            bestDist = d;
                            best = mpos.immutable();
                        }
                    }
                }
            }
        }
        return best;
    }

    private @Nullable BlockPos findNearbyChestWithMaterials(Level level, boolean needCobble, boolean needWood) {
        List<BlockPos> chests = KoboldGirlEntity.findNearbyUniqueChests(level, this.kobold.blockPosition(), 24, 6);
        for (BlockPos chestPos : chests) {
            Container c = KoboldGirlEntity.getChestContainer(level, chestPos);
            if (c == null || c.isEmpty()) continue;
            for (int i = 0; i < c.getContainerSize(); i++) {
                ItemStack st = c.getItem(i);
                if (st.isEmpty()) continue;
                if (needCobble && (st.is(Items.COBBLESTONE) || st.is(Items.COBBLED_DEEPSLATE))) {
                    return chestPos;
                }
                if (needWood && (st.is(Items.STICK) || st.is(ItemTags.PLANKS) || st.is(ItemTags.LOGS))) {
                    return chestPos;
                }
            }
        }
        return null;
    }

    private void extractMaterialsFromChest(Level level, BlockPos chestPos, int reqCobble, int reqSticks) {
        Container c = KoboldGirlEntity.getChestContainer(level, chestPos);
        if (c == null) return;

        int cobbleNeeded = Math.max(0, reqCobble - this.countCobblestoneInInventory());
        int sticksNeeded = Math.max(0, reqSticks - this.kobold.countItemInInventory(Items.STICK));

        for (int i = 0; i < c.getContainerSize(); i++) {
            ItemStack st = c.getItem(i);
            if (st.isEmpty()) continue;

            if (cobbleNeeded > 0 && (st.is(Items.COBBLESTONE) || st.is(Items.COBBLED_DEEPSLATE))) {
                int take = Math.min(st.getCount(), cobbleNeeded);
                ItemStack extracted = c.removeItem(i, take);
                ItemStack leftover = this.kobold.getInventory().addItem(extracted);
                if (!leftover.isEmpty()) {
                    ItemStack current = c.getItem(i);
                    if (current.isEmpty()) {
                        c.setItem(i, leftover);
                    } else {
                        current.grow(leftover.getCount());
                    }
                }
                cobbleNeeded -= (take - leftover.getCount());
            }

            if (sticksNeeded > 0 && (st.is(Items.STICK) || st.is(ItemTags.PLANKS) || st.is(ItemTags.LOGS))) {
                int take = Math.min(st.getCount(), Math.max(4, sticksNeeded));
                ItemStack extracted = c.removeItem(i, take);
                ItemStack leftover = this.kobold.getInventory().addItem(extracted);
                if (!leftover.isEmpty()) {
                    ItemStack current = c.getItem(i);
                    if (current.isEmpty()) {
                        c.setItem(i, leftover);
                    } else {
                        current.grow(leftover.getCount());
                    }
                }
                sticksNeeded -= (take - leftover.getCount());
            }
        }
        this.kobold.playSound(SoundEvents.CHEST_OPEN, 0.8F, 1.0F);
    }
}
