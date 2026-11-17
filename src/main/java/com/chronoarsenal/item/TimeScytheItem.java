package com.chronoarsenal.item;

import com.chronoarsenal.core.ChronoUtil;
import com.chronoarsenal.core.ChronoWeapon;
import com.chronoarsenal.core.DomainManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.Vec3;

/**
 * Time Scythe
 *  I : open / close the Time Domain (white 10x10x10 room, nobody inside can leave, teleport or break blocks)
 *  R : aerial cross-shaped slash, 30 blocks range, 14 damage (7 hearts)
 */
public class TimeScytheItem extends SwordItem implements ChronoWeapon {

    private static final double RANGE = 30.0;
    private static final float SLASH_DAMAGE = 14.0f;

    public TimeScytheItem(Properties properties) {
        super(net.minecraft.world.item.Tiers.NETHERITE, properties);
    }

    @Override
    public boolean ignoresCooldown(ServerPlayer player, int slot) {
        // the player can always close an open domain, even while the key is "cooling down"
        return slot == KEY_I && DomainManager.isOpen(player.getUUID());
    }

    @Override
    public int activate(ServerPlayer player, ItemStack stack, int slot) {
        if (slot == KEY_I) {
            if (DomainManager.isOpen(player.getUUID())) {
                DomainManager.close(player.getUUID());
                return 0; // the cooldown started when it was opened keeps running
            }
            return DomainManager.open(player) ? 20 * 75 : 0;
        }
        return slash(player);
    }

    private int slash(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();

        Vec3 right = look.cross(new Vec3(0, 1, 0));
        right = right.lengthSqr() < 1.0E-4 ? new Vec3(1, 0, 0) : right.normalize();
        Vec3 up = right.cross(look).normalize();

        // damage: everything inside a "+" shaped cross-section swept forward for RANGE blocks
        double box = RANGE + 6;
        for (LivingEntity target : ChronoUtil.targetsInRadius(player, eye, box)) {
            Vec3 rel = target.getBoundingBox().getCenter().subtract(eye);
            double f = rel.dot(look);
            double l = Math.abs(rel.dot(right));
            double v = Math.abs(rel.dot(up));
            double pad = target.getBbWidth() / 2.0;
            boolean inCross = (l <= 1.5 + pad && v <= 6.0 + pad) || (v <= 1.5 + pad && l <= 6.0 + pad);
            if (f > 1.0 && f <= RANGE && inCross) {
                ChronoUtil.hurt(player, target, SLASH_DAMAGE);
            }
        }

        // visuals: the spine of the slash plus three "+" crosses along it
        ChronoUtil.particleLine(level, eye.add(look), eye.add(look.scale(RANGE)), ParticleTypes.END_ROD, 0.75);
        for (double dist : new double[] {10, 20, 30}) {
            Vec3 c = eye.add(look.scale(dist));
            ChronoUtil.particleLine(level, c.subtract(right.scale(6)), c.add(right.scale(6)), ParticleTypes.CRIT, 0.6);
            ChronoUtil.particleLine(level, c.subtract(up.scale(6)), c.add(up.scale(6)), ParticleTypes.CRIT, 0.6);
        }
        player.swing(InteractionHand.MAIN_HAND, true);
        ChronoUtil.play(player, SoundEvents.PLAYER_ATTACK_SWEEP, 1.5f, 0.6f);
        return 20 * 8;
    }
}
