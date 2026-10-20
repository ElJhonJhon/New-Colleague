package com.newcolleague.mod.menu;

import com.newcolleague.mod.entity.KoboldGirlEntity;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class KoboldEquipmentContainer implements Container {
    private final KoboldGirlEntity kobold;

    public KoboldEquipmentContainer(KoboldGirlEntity kobold) {
        this.kobold = kobold;
    }

    @Override
    public int getContainerSize() {
        return 6;
    }

    @Override
    public boolean isEmpty() {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!this.kobold.getItemBySlot(slot).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public EquipmentSlot getSlotForIndex(int index) {
        return switch (index) {
            case 0 -> EquipmentSlot.HEAD;
            case 1 -> EquipmentSlot.CHEST;
            case 2 -> EquipmentSlot.LEGS;
            case 3 -> EquipmentSlot.FEET;
            case 4 -> EquipmentSlot.MAINHAND;
            case 5 -> EquipmentSlot.OFFHAND;
            default -> EquipmentSlot.MAINHAND;
        };
    }

    @Override
    public ItemStack getItem(int index) {
        return this.kobold.getItemBySlot(getSlotForIndex(index));
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        EquipmentSlot slot = getSlotForIndex(index);
        ItemStack item = this.kobold.getItemBySlot(slot);
        if (item.isEmpty()) return ItemStack.EMPTY;
        ItemStack split = item.split(count);
        if (item.isEmpty()) {
            this.kobold.setItemSlot(slot, ItemStack.EMPTY);
        }
        this.setChanged();
        return split;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        EquipmentSlot slot = getSlotForIndex(index);
        ItemStack item = this.kobold.getItemBySlot(slot);
        if (item.isEmpty()) return ItemStack.EMPTY;
        this.kobold.setItemSlot(slot, ItemStack.EMPTY);
        return item;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        EquipmentSlot slot = getSlotForIndex(index);
        this.kobold.setItemSlot(slot, stack);
        this.setChanged();
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return this.kobold.isAlive() && this.kobold.distanceToSqr(player) < 64.0D;
    }

    @Override
    public void clearContent() {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            this.kobold.setItemSlot(slot, ItemStack.EMPTY);
        }
    }
}
