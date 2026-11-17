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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Tra God Bow (needs no arrows, arrows it fires cannot be picked up)
 *  Right click : fast powerful arrow
 *  I : Arrow Rain - a rain of arrows falling around the aim point
 *  R : Triple Volley - three arrows with a spread, homing is not used
 */
public class TraGodBowItem extends Item implements ChronoWeapon {

    public TraGodBowItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            fire(sp, 0, 3.5f, 10.0);
            sp.getCooldowns().addCooldown(this, 10);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public int activate(ServerPlayer player, ItemStack stack, int slot) {
        return slot == KEY_I ? rain(player) : volley(player);
    }

    private void fire(ServerPlayer player, float yawOffset, float speed, double damage) {
        ServerLevel level = player.serverLevel();
        Arrow arrow = new Arrow(level, player, new ItemStack(net.minecraft.world.item.Items.ARROW), null);
        arrow.shootFromRotation(player, player.getXRot(), player.getYRot() + yawOffset, 0.0f, speed, 0.5f);
        arrow.setBaseDamage(damage);
        arrow.setCritArrow(true);
        arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
        level.addFreshEntity(arrow);
        ChronoUtil.play(player, SoundEvents.ARROW_SHOOT, 1.0f, 1.0f);
    }

    private int volley(ServerPlayer player) {
        fire(player, -8, 3.5f, 8.0);
        fire(player, 0, 3.5f, 8.0);
        fire(player, 8, 3.5f, 8.0);
        return 20 * 6;
    }

    private int rain(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 center = ChronoUtil.aimPoint(player, 50.0);
        ChronoUtil.play(player, SoundEvents.EVOKER_PREPARE_SUMMON, 1.2f, 1.5f);
        for (int i = 0; i < 20; i++) {
            ServerScheduler.schedule(3 + i * 2, () -> {
                for (int n = 0; n < 4; n++) {
                    double a = level.random.nextDouble() * Math.PI * 2;
                    double r = Math.sqrt(level.random.nextDouble()) * 8.0;
                    Arrow arrow = new Arrow(level, player, new ItemStack(net.minecraft.world.item.Items.ARROW), null);
                    arrow.setPos(center.x + Math.cos(a) * r, center.y + 22, center.z + Math.sin(a) * r);
                    arrow.setDeltaMovement(0, -2.5, 0);
                    arrow.setBaseDamage(6.0);
                    arrow.setCritArrow(true);
                    arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
                    level.addFreshEntity(arrow);
                }
            });
        }
        level.sendParticles(ParticleTypes.CLOUD, center.x, center.y + 20, center.z, 40, 6, 0.5, 6, 0.02);
        return 20 * 25;
    }
}
