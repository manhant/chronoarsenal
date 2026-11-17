package com.chronoarsenal.item;

import com.chronoarsenal.core.ChronoUtil;
import com.chronoarsenal.core.ChronoWeapon;
import com.chronoarsenal.core.ServerScheduler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Staff of Mikulak (mage staff)
 *  I : Fang Wall - two lines of evoker fangs toward where you look
 *  R : Arcane Storm - a meteor-like rain of explosions around the aim point (no block damage)
 */
public class StaffOfMikulakItem extends Item implements ChronoWeapon {

    public StaffOfMikulakItem(Properties properties) {
        super(properties);
    }

    @Override
    public int activate(ServerPlayer player, ItemStack stack, int slot) {
        return slot == KEY_I ? fangs(player) : storm(player);
    }

    private int fangs(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 look = player.getLookAngle();
        Vec3 dir = new Vec3(look.x, 0, look.z);
        if (dir.lengthSqr() < 1.0E-4) {
            return 0;
        }
        dir = dir.normalize();
        Vec3 side = new Vec3(-dir.z, 0, dir.x);
        for (int i = 1; i <= 14; i++) {
            final int step = i;
            final Vec3 d = dir;
            ServerScheduler.schedule(i, () -> {
                for (int lane = -1; lane <= 1; lane += 2) {
                    Vec3 p = player.position().add(d.scale(step * 1.5)).add(side.scale(lane * 1.2));
                    EvokerFangs fang = new EvokerFangs(level, p.x, p.y, p.z, (float) Math.atan2(d.z, d.x), 0, player);
                    level.addFreshEntity(fang);
                }
            });
        }
        ChronoUtil.play(player, SoundEvents.EVOKER_CAST_SPELL, 1.5f, 0.9f);
        return 20 * 12;
    }

    private int storm(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 center = ChronoUtil.aimPoint(player, 35.0);
        ChronoUtil.play(player, SoundEvents.EVOKER_PREPARE_ATTACK, 1.5f, 0.7f);
        for (int i = 0; i < 24; i++) {
            ServerScheduler.schedule(5 + i * 2, () -> {
                double a = level.random.nextDouble() * Math.PI * 2;
                double r = Math.sqrt(level.random.nextDouble()) * 9.0;
                Vec3 p = center.add(Math.cos(a) * r, 0, Math.sin(a) * r);
                level.sendParticles(ParticleTypes.EXPLOSION, p.x, p.y + 0.5, p.z, 1, 0, 0, 0, 0);
                level.sendParticles(ParticleTypes.WITCH, p.x, p.y + 1, p.z, 15, 0.6, 0.6, 0.6, 0.1);
                ChronoUtil.play(player, SoundEvents.GENERIC_EXPLODE.value(), 0.5f, 1.4f);
                for (LivingEntity e : ChronoUtil.targetsInRadius(player, p, 2.5)) {
                    e.invulnerableTime = 0;
                    ChronoUtil.hurt(player, e, 6.0f);
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20 * 4, 0), player);
                }
            });
        }
        return 20 * 30;
    }
}
