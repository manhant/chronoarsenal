package com.chronoarsenal.item;

import com.chronoarsenal.core.ChronoUtil;
import com.chronoarsenal.core.ChronoWeapon;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.phys.Vec3;

/**
 * Lion Sun Axe
 *  I : Solar Roar - fire blast around you, sets enemies on fire and knocks them back
 *  R : Sun Leap - jump forward and slam down, damaging everything where you land
 */
public class LionSunAxeItem extends AxeItem implements ChronoWeapon {

    public LionSunAxeItem(Properties properties) {
        super(Tiers.NETHERITE, properties);
    }

    @Override
    public int activate(ServerPlayer player, ItemStack stack, int slot) {
        return slot == KEY_I ? roar(player) : leap(player);
    }

    private int roar(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 center = player.position();
        for (LivingEntity e : ChronoUtil.targetsInRadius(player, center, 8.0)) {
            ChronoUtil.hurt(player, e, 10.0f);
            e.igniteForSeconds(8);
            Vec3 push = e.position().subtract(center).multiply(1, 0, 1);
            if (push.lengthSqr() > 1.0E-4) {
                push = push.normalize().scale(1.2);
                e.setDeltaMovement(push.x, 0.5, push.z);
                e.hurtMarked = true;
            }
        }
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 6, 1));
        level.sendParticles(ParticleTypes.FLAME, center.x, center.y + 1, center.z, 150, 4, 1, 4, 0.1);
        level.sendParticles(ParticleTypes.LAVA, center.x, center.y + 1, center.z, 30, 3, 1, 3, 0.0);
        ChronoUtil.play(player, SoundEvents.RAVAGER_ROAR, 1.5f, 0.7f);
        return 20 * 25;
    }

    private int leap(ServerPlayer player) {
        Vec3 look = player.getLookAngle();
        Vec3 flat = new Vec3(look.x, 0, look.z);
        flat = flat.lengthSqr() < 1.0E-4 ? Vec3.ZERO : flat.normalize();
        player.setDeltaMovement(flat.x * 1.3, 0.9, flat.z * 1.3);
        player.hurtMarked = true;
        player.fallDistance = 0;
        ChronoUtil.play(player, SoundEvents.FIRECHARGE_USE, 1.0f, 0.8f);

        // landing is detected by polling every 2 ticks (up to 3 seconds)
        pollLanding(player, 0);
        return 20 * 12;
    }

    private void pollLanding(ServerPlayer player, int elapsed) {
        com.chronoarsenal.core.ServerScheduler.schedule(2, () -> {
            if (player.isRemoved() || !player.isAlive()) {
                return;
            }
            boolean landed = elapsed > 4 && player.onGround();
            if (landed || elapsed > 60) {
                slam(player);
            } else {
                player.fallDistance = 0;
                pollLanding(player, elapsed + 2);
            }
        });
    }

    private void slam(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 pos = player.position();
        for (LivingEntity e : ChronoUtil.targetsInRadius(player, pos, 5.0)) {
            ChronoUtil.hurt(player, e, 12.0f);
            e.igniteForSeconds(5);
        }
        level.sendParticles(ParticleTypes.EXPLOSION, pos.x, pos.y + 0.5, pos.z, 6, 2, 0.2, 2, 0.0);
        level.sendParticles(ParticleTypes.FLAME, pos.x, pos.y + 0.5, pos.z, 80, 3, 0.3, 3, 0.1);
        ChronoUtil.play(player, SoundEvents.GENERIC_EXPLODE.value(), 1.0f, 1.2f);
    }
}
