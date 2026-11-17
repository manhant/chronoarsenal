package com.chronoarsenal.item;

import com.chronoarsenal.core.ChronoUtil;
import com.chronoarsenal.core.ChronoWeapon;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
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
 * Blood Rage Sword
 *  Every hit heals you a little (lifesteal)
 *  I : Blood Rage - 12 seconds of Strength, Speed and Regeneration (costs 3 hearts)
 *  R : Crimson Sweep - hits everything around you and heals you per enemy hit
 */
public class BloodRageSwordItem extends SwordItem implements ChronoWeapon {

    public BloodRageSwordItem(Properties properties) {
        super(Tiers.NETHERITE, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        attacker.heal(2.0f);
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public int activate(ServerPlayer player, ItemStack stack, int slot) {
        if (slot == KEY_I) {
            if (player.getHealth() <= 6.5f) {
                ChronoUtil.msg(player, Component.translatable("message.chronoarsenal.low_health"));
                return 0;
            }
            player.setHealth(player.getHealth() - 6.0f);
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 12, 2));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 12, 1));
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 12, 1));
            player.serverLevel().sendParticles(ParticleTypes.DAMAGE_INDICATOR,
                    player.getX(), player.getY() + 1, player.getZ(), 30, 0.5, 0.8, 0.5, 0.1);
            ChronoUtil.play(player, SoundEvents.WITHER_SPAWN, 0.6f, 1.6f);
            return 20 * 30;
        }

        Vec3 pos = player.position();
        int hits = 0;
        for (LivingEntity e : ChronoUtil.targetsInRadius(player, pos, 5.0)) {
            ChronoUtil.hurt(player, e, 9.0f);
            hits++;
        }
        player.heal(hits * 3.0f);
        player.serverLevel().sendParticles(ParticleTypes.SWEEP_ATTACK, pos.x, pos.y + 1, pos.z, 8, 2.5, 0.3, 2.5, 0);
        player.serverLevel().sendParticles(ParticleTypes.DAMAGE_INDICATOR, pos.x, pos.y + 1, pos.z, 25, 2.5, 0.5, 2.5, 0.1);
        ChronoUtil.play(player, SoundEvents.PLAYER_ATTACK_SWEEP, 1.2f, 0.7f);
        return hits > 0 ? 20 * 10 : 20 * 2;
    }
}
