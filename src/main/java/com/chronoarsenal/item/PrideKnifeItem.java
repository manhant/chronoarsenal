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
 * Pride Knife (small, fast knife of an assassin)
 *  Attacking from behind (target looking away from you) does double damage
 *  I : Shadow Step - vanish (invisibility) for 8 seconds and speed up
 *  R : Backstab - teleport behind the target in sight and stab it for 16 damage
 */
public class PrideKnifeItem extends SwordItem implements ChronoWeapon {

    public PrideKnifeItem(Properties properties) {
        super(Tiers.NETHERITE, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        Vec3 targetLook = target.getLookAngle();
        Vec3 toAttacker = attacker.position().subtract(target.position());
        // the attacker is behind the target when the target looks away from them
        if (!attacker.level().isClientSide && targetLook.dot(toAttacker.normalize()) < -0.3) {
            target.invulnerableTime = 0;
            target.hurt(attacker.damageSources().generic(), 6.0f);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public int activate(ServerPlayer player, ItemStack stack, int slot) {
        if (slot == KEY_I) {
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 20 * 8, 0, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 8, 2));
            player.serverLevel().sendParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + 1, player.getZ(),
                    40, 0.4, 0.8, 0.4, 0.05);
            ChronoUtil.play(player, SoundEvents.ENDERMAN_TELEPORT, 0.8f, 1.5f);
            return 20 * 20;
        }

        LivingEntity target = ChronoUtil.entityInSight(player, 14.0);
        if (target == null) {
            ChronoUtil.msg(player, net.minecraft.network.chat.Component.translatable("message.chronoarsenal.no_target"));
            return 0;
        }
        Vec3 behind = target.getLookAngle().multiply(1, 0, 1);
        behind = behind.lengthSqr() < 1.0E-4 ? Vec3.ZERO : behind.normalize().scale(-1.3);
        Vec3 dest = target.position().add(behind);
        player.teleportTo(dest.x, target.getY(), dest.z);
        player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, target.getEyePosition());
        target.invulnerableTime = 0;
        ChronoUtil.hurt(player, target, 16.0f);
        player.serverLevel().sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1, target.getZ(),
                25, 0.4, 0.5, 0.4, 0.3);
        ChronoUtil.play(player, SoundEvents.PLAYER_ATTACK_CRIT, 1.2f, 1.3f);
        return 20 * 12;
    }
}
