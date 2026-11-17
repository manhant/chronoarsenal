package com.chronoarsenal.core;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/** Your original dash, now run on the server (so it works in multiplayer) and always horizontal. */
public final class DashUtil {

    private DashUtil() {}

    public static void dash(Player player, double strength) {
        Vec3 look = player.getLookAngle();
        Vec3 flat = new Vec3(look.x, 0, look.z);
        if (flat.lengthSqr() < 1.0E-4) {
            return;
        }
        flat = flat.normalize();
        player.setDeltaMovement(flat.x * strength, 0.15, flat.z * strength);
        player.hurtMarked = true; // makes the server send the new velocity to the client
        player.fallDistance = 0;
    }
}
