package com.chronoarsenal.item;

import com.chronoarsenal.core.ChronoUtil;
import com.chronoarsenal.core.ChronoWeapon;
import com.chronoarsenal.core.ServerScheduler;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.phys.Vec3;

/**
 * Ragnarok Spear
 *  I : Ragnarok - 100 lightning strikes, 5 per tick-group (so the server does not lag), around where you aim
 *  R : Thunder Lance - lightning on the target you are looking at plus a chain to nearby enemies
 */
public class RagnarokSpearItem extends SwordItem implements ChronoWeapon {

    private static final int STRIKES = 100;
    private static final int PER_BATCH = 5;
    private static final double RADIUS = 14.0;

    public RagnarokSpearItem(Properties properties) {
        super(Tiers.NETHERITE, properties);
    }

    @Override
    public int activate(ServerPlayer player, ItemStack stack, int slot) {
        return slot == KEY_I ? ragnarok(player) : lance(player);
    }

    private int ragnarok(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 center = ChronoUtil.aimPoint(player, 40.0);
        ChronoUtil.play(player, SoundEvents.LIGHTNING_BOLT_THUNDER, 2.0f, 0.6f);

        int batches = STRIKES / PER_BATCH;
        for (int b = 0; b < batches; b++) {
            ServerScheduler.schedule(1 + b * 2, () -> {
                if (player.isRemoved()) {
                    return;
                }
                for (int i = 0; i < PER_BATCH; i++) {
                    double angle = level.random.nextDouble() * Math.PI * 2;
                    double dist = Math.sqrt(level.random.nextDouble()) * RADIUS;
                    strike(level, player, center.x + Math.cos(angle) * dist,
                            center.y, center.z + Math.sin(angle) * dist);
                }
            });
        }
        return 20 * 90;
    }

    private int lance(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        LivingEntity first = ChronoUtil.entityInSight(player, 40.0);
        if (first == null) {
            ChronoUtil.msg(player, net.minecraft.network.chat.Component.translatable("message.chronoarsenal.no_target"));
            return 0;
        }
        strike(level, player, first.getX(), first.getY(), first.getZ());
        List<LivingEntity> near = ChronoUtil.targetsInRadius(player, first.position(), 8.0);
        int chained = 0;
        for (LivingEntity e : near) {
            if (e != first && chained < 5) {
                strike(level, player, e.getX(), e.getY(), e.getZ());
                chained++;
            }
        }
        return 20 * 10;
    }

    /** Spawns a real lightning bolt. Damage to the owner and allies is blocked in CommonEvents.onIncomingDamage. */
    private void strike(ServerLevel level, ServerPlayer owner, double x, double y, double z) {
        // find the ground under (x, z) starting from a bit above the aim height
        int groundY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,
                (int) Math.floor(x), (int) Math.floor(z));
        double useY = Math.abs(groundY - y) < 20 ? groundY : y;
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt == null) {
            return;
        }
        bolt.moveTo(x, useY, z);
        bolt.setCause(owner);
        level.addFreshEntity(bolt);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, useY + 1, z, 8, 0.5, 1, 0.5, 0.1);
    }
}
