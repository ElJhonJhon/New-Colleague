package com.newcolleague.mod.entity;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class QueenSoulTracker {

    public static class ActiveSoul {
        public final UUID playerUUID;
        public final String queenName;
        public final String tribeName;
        public Vec3 currentPos;
        public int ticksAlive = 0;

        public ActiveSoul(UUID playerUUID, String queenName, String tribeName, Vec3 currentPos) {
            this.playerUUID = playerUUID;
            this.queenName = queenName;
            this.tribeName = tribeName;
            this.currentPos = currentPos;
        }
    }

    private static final Map<UUID, ActiveSoul> ACTIVE_SOULS = new ConcurrentHashMap<>();

    public static void spawnSoul(Player player, String queenName, String tribeName, Vec3 startPos) {
        if (player == null) return;
        ACTIVE_SOULS.put(player.getUUID(), new ActiveSoul(player.getUUID(), queenName, tribeName, startPos));
    }

    public static boolean hasSoul(UUID playerUUID) {
        return playerUUID != null && ACTIVE_SOULS.containsKey(playerUUID);
    }

    public static @Nullable ActiveSoul getSoul(UUID playerUUID) {
        return playerUUID != null ? ACTIVE_SOULS.get(playerUUID) : null;
    }

    public static void removeSoul(UUID playerUUID) {
        if (playerUUID != null) {
            ACTIVE_SOULS.remove(playerUUID);
        }
    }
}
