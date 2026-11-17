package com.chronoarsenal.item;

import com.chronoarsenal.core.ChronoUtil;
import com.chronoarsenal.core.ChronoWeapon;
import com.chronoarsenal.core.ServerScheduler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;

/**
 * Veid Katana
 *  I : Phantom Dash - blink up to 12 blocks forward, cutting everything on the way (stops at walls)
 *  R : Iaido - after a short pause, a lightning-fast slash hits every enemy in front of you
 */
public class VeidKatanaItem extends SwordItem implements ChronoWeapon {

    public VeidKatanaItem(Properties properties) {
        super(Tiers.NETHERITE, properties);
    }

    @Override
    public int activate(ServerPlayer player, ItemStack stack, int slot) {
        return slot == KEY_I ? dash(player) : iaido(player);
    }

    private int dash(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 start = player.position();
        Vec3 eye = player.getEyePosition();
        Vec3 dir = player.getLookAngle();
        Vec3 flat = new Vec3(dir.x, 0, dir.z);
        if (flat.lengthSqr() < 1.0E-4) {
            return 0;
        }
        flat = flat.normalize();

        // stop in front of the first wall (checked at head height)
        Vec3 end = eye.add(flat.scale(12));
        Vec3 hit = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, player)).getLocation();
        double dist = Math.max(0, eye.distanceTo(hit) - 0.8);
        Vec3 dest = start.add(flat.scale(dist));

        for (LivingEntity e : ChronoUtil.targetsInRadius(player, start.add(flat.scale(dist / 2)), dist / 2 + 2)) {
            Vec3 rel = e.position().subtract(start);
            double along = rel.dot(flat);
            double off = rel.subtract(flat.scale(along)).length();
            if (along >= -1 && along <= dist + 1 && off <= 2.0) {
                ChronoUtil.hurt(player, e, 12.0f);
            }
        }
        ChronoUtil.particleLine(level, start.add(0, 1, 0), dest.add(0, 1, 0), ParticleTypes.CLOUD, 0.5);
        player.teleportTo(dest.x, dest.y, dest.z);
        player.fallDistance = 0;
        ChronoUtil.play(player, SoundEvents.PLAYER_ATTACK_SWEEP, 1.2f, 1.6f);
        return 20 * 8;
    }

    private int iaido(ServerPlayer player) {
        ChronoUtil.play(player, SoundEvents.CHAIN_PLACE, 1.0f, 0.5f);
        ServerScheduler.schedule(12, () -> {
            if (player.isRemoved() || !player.isAlive()) {
                return;
            }
            ServerLevel level = player.serverLevel();
            Vec3 eye = player.getEyePosition();
            Vec3 look = player.getLookAngle().normalize();
            for (LivingEntity e : ChronoUtil.targetsInRadius(player, eye.add(look.scale(5)), 8.0)) {
                Vec3 rel = e.getBoundingBox().getCenter().subtract(eye);
                double f = rel.dot(look);
                double off = rel.subtract(look.scale(f)).length();
                if (f > 0 && f <= 10 && off <= 2.5 + f * 0.25) {
                    e.invulnerableTime = 0;
                    ChronoUtil.hurt(player, e, 18.0f);
                }
            }
            ChronoUtil.particleLine(level, eye.add(look.scale(0.8)), eye.add(look.scale(10)), ParticleTypes.SWEEP_ATTACK, 1.2);
            ChronoUtil.particleLine(level, eye.add(look.scale(0.8)), eye.add(look.scale(10)), ParticleTypes.END_ROD, 0.4);
            ChronoUtil.play(player, SoundEvents.PLAYER_ATTACK_CRIT, 1.5f, 0.6f);
        });
        return 20 * 10;
    }
}
