package com.chronoarsenal.core;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Implemented by every weapon in the mod. Abilities run on the server only. */
public interface ChronoWeapon {

    int KEY_I = 0;
    int KEY_R = 1;

    /**
     * @param slot KEY_I or KEY_R
     * @return the cooldown in ticks if the ability fired, or 0 if nothing happened (no cooldown is applied)
     */
    int activate(ServerPlayer player, ItemStack stack, int slot);

    /** Lets a weapon allow "toggle off" presses while the key is on cooldown (used by the Time Scythe domain). */
    default boolean ignoresCooldown(ServerPlayer player, int slot) {
        return false;
    }
}
