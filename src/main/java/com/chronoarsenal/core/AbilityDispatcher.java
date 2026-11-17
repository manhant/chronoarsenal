package com.chronoarsenal.core;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class AbilityDispatcher {

    private AbilityDispatcher() {}

    public static void handle(ServerPlayer player, int slot) {
        if (slot != ChronoWeapon.KEY_I && slot != ChronoWeapon.KEY_R) {
            return;
        }
        if (!player.isAlive() || player.isSpectator()) {
            return;
        }
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof ChronoWeapon weapon)) {
            return;
        }

        int left = ChronoCooldowns.remaining(player, stack.getItem(), slot);
        if (left > 0 && !weapon.ignoresCooldown(player, slot)) {
            ChronoUtil.msg(player, Component.translatable(
                    "message.chronoarsenal.cooldown", String.format("%.1f", left / 20.0)));
            return;
        }

        int cooldown = weapon.activate(player, stack, slot);
        if (cooldown > 0) {
            ChronoCooldowns.start(player, stack.getItem(), slot, cooldown);
        }
    }
}
