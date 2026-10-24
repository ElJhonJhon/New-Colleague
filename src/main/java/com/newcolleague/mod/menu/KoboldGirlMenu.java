package com.newcolleague.mod.menu;

import com.newcolleague.mod.entity.AbstractTribalEntity;
import com.newcolleague.mod.entity.KoboldGirlEntity;
import com.newcolleague.mod.entity.ModEntities;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.Level;

public class KoboldGirlMenu extends AbstractContainerMenu {

    private final AbstractTribalEntity kobold;
    private final KoboldEquipmentContainer equipContainer;

    // Constructor del lado del cliente llamado por IContainerFactory
    public KoboldGirlMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory, getClientKoboldEntity(playerInventory.player.level(), extraData.readInt()));
    }

    // Constructor del lado del servidor
    public KoboldGirlMenu(int containerId, Inventory playerInventory, AbstractTribalEntity kobold) {
        super(ModMenus.KOBOLD_GIRL_MENU.get(), containerId);
        this.kobold = kobold != null ? kobold : new KoboldGirlEntity(ModEntities.KOBOLD_GIRL.get(), playerInventory.player.level());
        this.equipContainer = new KoboldEquipmentContainer(this.kobold);

        // 1. Ranuras de Armadura de la Kobold Girl (Slots 0..3)
        // Casco
        this.addSlot(new ArmorSlot(this.equipContainer, this.kobold, EquipmentSlot.HEAD, 0, 8, 8, InventoryMenu.EMPTY_ARMOR_SLOT_HELMET));
        // Peto
        this.addSlot(new ArmorSlot(this.equipContainer, this.kobold, EquipmentSlot.CHEST, 1, 8, 26, InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE));
        // Grebas
        this.addSlot(new ArmorSlot(this.equipContainer, this.kobold, EquipmentSlot.LEGS, 2, 8, 44, InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS));
        // Botas
        this.addSlot(new ArmorSlot(this.equipContainer, this.kobold, EquipmentSlot.FEET, 3, 8, 62, InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS));

        // 2. Ranuras de Armas y Escudo de la Kobold Girl (Slots 4 y 5)
        // Mano principal (Arma)
        this.addSlot(new Slot(this.equipContainer, 4, 80, 26) {
            @Override
            public Identifier getNoItemIcon() {
                return Identifier.withDefaultNamespace("container/slot/sword");
            }
        });
        // Mano secundaria (Escudo / Secundario)
        this.addSlot(new Slot(this.equipContainer, 5, 80, 48) {
            @Override
            public Identifier getNoItemIcon() {
                return InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD;
            }
        });

        // 3. Inventario / Mochila de 27 casillas de la Kobold Girl (Slots 6..32)
        Container koboldInv = this.kobold.getInventory();
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(koboldInv, col + row * 9, 8 + col * 18, 81 + row * 18));
            }
        }

        // 4. Inventario principal del Jugador (Slots 33..59)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 149 + row * 18));
            }
        }

        // 5. Barra de acceso rápido (Hotbar) del Jugador (Slots 60..68)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 207));
        }
    }

    private static AbstractTribalEntity getClientKoboldEntity(Level level, int entityId) {
        Entity entity = level.getEntity(entityId);
        if (entity instanceof AbstractTribalEntity tribalEntity) {
            return tribalEntity;
        }
        return null;
    }

    public AbstractTribalEntity getTribalEntity() {
        return this.kobold;
    }

    public KoboldGirlEntity getKoboldGirl() {
        return this.kobold instanceof KoboldGirlEntity k ? k : null;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.equipContainer.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            // Si el ítem proviene de la Kobold Girl (Slots 0..32): Mover al inventario del jugador (33..68)
            if (index < 33) {
                if (!this.moveItemStackTo(itemstack1, 33, 69, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Si el ítem proviene del jugador (Slots 33..68):
                // 1. Intentar equipar en ranuras de armadura (Slots 0..3)
                Equippable equippable = itemstack1.get(DataComponents.EQUIPPABLE);
                boolean placedInArmor = false;
                if (equippable != null && equippable.slot().isArmor()) {
                    int armorSlotIndex = switch (equippable.slot()) {
                        case HEAD -> 0;
                        case CHEST -> 1;
                        case LEGS -> 2;
                        case FEET -> 3;
                        default -> -1;
                    };
                    if (armorSlotIndex != -1 && !this.slots.get(armorSlotIndex).hasItem()) {
                        if (this.moveItemStackTo(itemstack1, armorSlotIndex, armorSlotIndex + 1, false)) {
                            placedInArmor = true;
                        }
                    }
                }

                // 2. Si es escudo, intentar colocar en mano secundaria (Slot 5)
                if (!placedInArmor && (itemstack1.getItem() instanceof ShieldItem || itemstack1.is(ItemTags.CHEST_ARMOR_ENCHANTABLE))) {
                    if (!this.slots.get(5).hasItem()) {
                        if (this.moveItemStackTo(itemstack1, 5, 6, false)) {
                            placedInArmor = true;
                        }
                    }
                }

                // 3. Si es arma, intentar colocar en mano principal (Slot 4) si está libre
                if (!placedInArmor && AbstractTribalEntity.isWeapon(itemstack1)) {
                    if (!this.slots.get(4).hasItem()) {
                        if (this.moveItemStackTo(itemstack1, 4, 5, false)) {
                            placedInArmor = true;
                        }
                    }
                }

                // 4. Si no se equipó, mover a las 27 casillas de la mochila de la Kobold (Slots 6..32)
                if (!placedInArmor && !itemstack1.isEmpty()) {
                    if (!this.moveItemStackTo(itemstack1, 6, 33, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
        }

        return itemstack;
    }

    private static class ArmorSlot extends Slot {
        private final AbstractTribalEntity mob;
        private final EquipmentSlot slot;
        private final Identifier emptyIcon;

        public ArmorSlot(Container container, AbstractTribalEntity mob, EquipmentSlot slot, int index, int x, int y, Identifier emptyIcon) {
            super(container, index, x, y);
            this.mob = mob;
            this.slot = slot;
            this.emptyIcon = emptyIcon;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return this.mob.fitsArmorSlot(stack, this.slot);
        }

        @Override
        public Identifier getNoItemIcon() {
            return this.emptyIcon;
        }
    }
}