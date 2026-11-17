package com.chronoarsenal.core;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;

/** Per player + per weapon + per key cooldowns, stored in memory (reset when the server stops). */
public final class ChronoCooldowns {

    private static final Map<String, Long> UNTIL = new HashMap<>();

    private ChronoCooldowns() {}

    private static String key(ServerPlayer player, Item item, int slot) {
        return player.getUUID() + "|" + BuiltInRegistries.ITEM.getKey(item) + "|" + slot;
    }

    public static int remaining(ServerPlayer player, Item item, int slot) {
        String k = key(player, item, slot);
        Long until = UNTIL.get(k);
        if (until == null) {
            return 0;
        }
        long left = until - player.serverLevel().getGameTime();
        if (left <= 0) {
            UNTIL.remove(k);
            return 0;
        }
        return (int) left;
    }

    public static void start(ServerPlayer player, Item item, int slot, int ticks) {
        UNTIL.put(key(player, item, slot), player.serverLevel().getGameTime() + ticks);
    }

    public static void clear() {
        UNTIL.clear();
    }
}
