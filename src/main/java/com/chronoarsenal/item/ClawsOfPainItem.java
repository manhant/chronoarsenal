package com.chronoarsenal.item;

import com.chronoarsenal.core.ChronoUtil;
import com.chronoarsenal.core.ChronoWeapon;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.phys.Vec3;

/**
 * Claws of Pain (very fast attack speed)
 *  Every hit applies Wither (bleeding)
 *  I : Shredding Frenzy - 6 rapid slashes on the nearest enemy in front of you
 *  R : Pounce - dash to the target in sight and hit it, with Slowness on it
 */
public class ClawsOfPainItem extends SwordItem implements ChronoWeapon {

    public ClawsOfPainItem(Properties properties) {
        super(Tiers.NETHERITE, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.addEffect(new MobEffectInstance(MobEffects.WITHER, 20 * 3, 0), attacker);
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public int activate(ServerPlayer player, ItemStack stack, int slot) {
        LivingEntity target = ChronoUtil.entityInSight(player, slot == KEY_I ? 6.0 : 16.0);
        if (target == null) {
            ChronoUtil.msg(player, net.minecraft.network.chat.Component.translatable("message.chronoarsenal.no_target"));
            return 0;
        }
        if (slot == KEY_I) {
            for (int i = 0; i < 6; i++) {
                final int n = i;
                com.chronoarsenal.core.ServerScheduler.schedule(1 + i * 2, () -> {
                    if (target.isAlive() && player.isAlive()) {
                        target.invulnerableTime = 0; // lets every slash count
                        ChronoUtil.hurt(player, target, 4.0f);
                        player.serverLevel().sendParticles(ParticleTypes.CRIT,
                                target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                                10, 0.4, 0.4, 0.4, 0.3);
                    }
                });
            }
            ChronoUtil.play(player, SoundEvents.PLAYER_ATTACK_CRIT, 1.0f, 1.5f);
            return 20 * 12;
        }

        Vec3 back = player.getLookAngle().scale(-1.5);
        Vec3 dest = target.position().add(back.x, 0, back.z);
        player.teleportTo(dest.x, target.getY(), dest.z);
        target.invulnerableTime = 0;
        ChronoUtil.hurt(player, target, 10.0f);
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 4, 2), player);
        player.serverLevel().sendParticles(ParticleTypes.SWEEP_ATTACK,
                target.getX(), target.getY() + 1, target.getZ(), 3, 0.3, 0.3, 0.3, 0);
        ChronoUtil.play(player, SoundEvents.PLAYER_ATTACK_STRONG, 1.0f, 1.2f);
        return 20 * 10;
    }
}
