package com.newcolleague.mod.village;

import com.newcolleague.mod.entity.AbstractTribalEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Representa la gestión y métricas unificadas del almacén comunal de la tribu.
 * Funciona para cualquier especie tribal.
 */
public class VillageStorage {
    private final AbstractTribalEntity queen;
    private final int radius;

    public VillageStorage(AbstractTribalEntity queen, int radius) {
        this.queen = queen;
        this.radius = radius;
    }

    public VillageStorage(AbstractTribalEntity queen) {
        this(queen, 32);
    }

    public int getTotalFood() {
        if (!(this.queen.level() instanceof ServerLevel sl)) return 0;
        return this.queen.countCommunityFood(sl, this.radius);
    }

    public boolean hasActiveProject() {
        return this.queen.hasActiveVillageProject();
    }

    public List<BlockPos> getNearbyChests() {
        return AbstractTribalEntity.findNearbyUniqueChests(this.queen.level(), this.queen.blockPosition(), this.radius, 6);
    }

    public int countItemInChests(Item item) {
        Level level = this.queen.level();
        int total = 0;
        List<BlockPos> chests = this.getNearbyChests();
        for (BlockPos pos : chests) {
            Container c = AbstractTribalEntity.getChestContainer(level, pos);
            if (c == null || c.isEmpty()) continue;
            for (int i = 0; i < c.getContainerSize(); i++) {
                ItemStack st = c.getItem(i);
                if (!st.isEmpty() && st.is(item)) {
                    total += st.getCount();
                }
            }
        }
        return total;
    }

    public @Nullable BlockPos findChestWithItem(Item item) {
        Level level = this.queen.level();
        List<BlockPos> chests = this.getNearbyChests();
        for (BlockPos pos : chests) {
            Container c = AbstractTribalEntity.getChestContainer(level, pos);
            if (c == null || c.isEmpty()) continue;
            for (int i = 0; i < c.getContainerSize(); i++) {
                ItemStack st = c.getItem(i);
                if (!st.isEmpty() && st.is(item)) {
                    return pos;
                }
            }
        }
        return null;
    }
}
