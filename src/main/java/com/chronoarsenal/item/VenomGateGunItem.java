package com.chronoarsenal.item;

import com.chronoarsenal.core.ChronoUtil;
import com.chronoarsenal.core.ChronoWeapon;
import com.chronoarsenal.core.ServerScheduler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Venom Gate Gun
 *  Right click : poison shot (instant hit, 40 blocks)
 *  I : Venom Gate - a big poison cloud where you aim
 *  R : burst of 5 shots
 */
public class VenomGateGunItem extends Item implements ChronoWeapon {

    private static final double RANGE = 40.0;

    public VenomGateGunItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            shoot(sp);
            sp.getCooldowns().addCooldown(this, 8);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public int activate(ServerPlayer player, ItemStack stack, int slot) {
        if (slot == KEY_I) {
            return gate(player);
        }
        for (int i = 0; i < 5; i++) {
            ServerScheduler.schedule(1 + i * 2, () -> {
                if (!player.isRemoved() && player.isAlive()) {
                    shoot(player);
                }
            });
        }
        return 20 * 6;
    }

    private void shoot(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 eye = player.getEyePosition();
        Vec3 end = ChronoUtil.aimPoint(player, RANGE);
        LivingEntity target = ChronoUtil.entityInSight(player, eye.distanceTo(end));

        Vec3 muzzle = eye.add(player.getLookAngle().scale(0.8));
        if (target != null) {
            end = target.getBoundingBox().getCenter();
            ChronoUtil.hurt(player, target, 6.0f);
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 20 * 8, 1), player);
        }
        ChronoUtil.particleLine(level, muzzle, end, ParticleTypes.HAPPY_VILLAGER, 0.8);
        ChronoUtil.play(player, SoundEvents.CROSSBOW_SHOOT, 1.0f, 1.4f);
    }

    private int gate(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 point = ChronoUtil.aimPoint(player, 30.0);

        AreaEffectCloud cloud = new AreaEffectCloud(level, point.x, point.y, point.z);
        cloud.setOwner(player);
        cloud.setRadius(4.0f);
        cloud.setDuration(20 * 10);
        cloud.setWaitTime(0);
        cloud.setRadiusPerTick(-4.0f / (20 * 10));
        cloud.setParticle(ParticleTypes.ENTITY_EFFECT);
        cloud.setFixedColor(0x4CAF2E);
        cloud.addEffect(new MobEffectInstance(MobEffects.POISON, 20 * 5, 1));
        level.addFreshEntity(cloud);

        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, point.x, point.y + 1, point.z, 40, 2, 1, 2, 0.02);
        ChronoUtil.play(player, SoundEvents.BREWING_STAND_BREW, 1.5f, 0.5f);
        return 20 * 20;
    }
}
