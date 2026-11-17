package com.chronoarsenal.core;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/** Shared helpers for all weapon abilities. */
public final class ChronoUtil {

    private ChronoUtil() {}

    /** Anything that is a living, non-ally entity. The owner, their pets and teammates are never targets. */
    public static boolean isValidTarget(ServerPlayer owner, Entity e) {
        if (!(e instanceof LivingEntity living) || e == owner || !living.isAlive() || living.isSpectator()) {
            return false;
        }
        if (e instanceof ArmorStand) {
            return false;
        }
        if (e instanceof TamableAnimal pet && pet.isOwnedBy(owner)) {
            return false;
        }
        return !living.isAlliedTo(owner);
    }

    public static List<LivingEntity> targetsInRadius(ServerPlayer owner, Vec3 center, double radius) {
        AABB box = new AABB(center.subtract(radius, radius, radius), center.add(radius, radius, radius));
        return owner.serverLevel().getEntitiesOfClass(LivingEntity.class, box,
                e -> isValidTarget(owner, e) && e.distanceToSqr(center) <= radius * radius);
    }

    public static void hurt(ServerPlayer owner, LivingEntity target, float amount) {
        target.hurt(owner.damageSources().playerAttack(owner), amount);
    }

    /** The point the player is looking at (a block surface, or the end of the ray if nothing is hit). */
    public static Vec3 aimPoint(ServerPlayer player, double distance) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(distance));
        return player.level().clip(new ClipContext(
                eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getLocation();
    }

    /** The first valid target along the look direction, or null. */
    @Nullable
    public static LivingEntity entityInSight(ServerPlayer player, double distance) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = eye.add(look.scale(distance));
        AABB box = player.getBoundingBox().expandTowards(look.scale(distance)).inflate(1.0);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(
                player.level(), player, eye, end, box, e -> isValidTarget(player, e));
        if (hit != null && hit.getEntity() instanceof LivingEntity living) {
            return living;
        }
        return null;
    }

    public static void particleLine(ServerLevel level, Vec3 from, Vec3 to, ParticleOptions type, double step) {
        Vec3 delta = to.subtract(from);
        double length = delta.length();
        if (length < 1.0E-3) {
            return;
        }
        Vec3 dir = delta.scale(1.0 / length);
        for (double t = 0; t <= length; t += step) {
            Vec3 p = from.add(dir.scale(t));
            level.sendParticles(type, p.x, p.y, p.z, 1, 0, 0, 0, 0);
        }
    }

    public static void play(ServerPlayer player, SoundEvent sound, float volume, float pitch) {
        player.level().playSound(null, player.blockPosition(), sound, SoundSource.PLAYERS, volume, pitch);
    }

    public static void msg(ServerPlayer player, Component text) {
        player.displayClientMessage(text, true);
    }
}
