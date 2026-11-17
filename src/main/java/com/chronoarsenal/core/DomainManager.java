package com.chronoarsenal.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The Time Scythe "domain": a hollow 10x10x10 room made of white concrete.
 * - Only air / replaceable blocks are turned into wall, so the terrain is never destroyed.
 *   The original blocks are remembered and put back when the domain closes.
 * - Everything alive inside when it opens (including the owner) cannot leave and cannot teleport.
 * - Nobody can break or place blocks inside it.
 * - It closes by itself after MAX_TICKS, when the owner dies / logs out, or when the server stops.
 */
public final class DomainManager {

    public static final int MAX_TICKS = 20 * 30;

    private static final Map<UUID, Domain> DOMAINS = new HashMap<>();

    private DomainManager() {}

    private static final class Domain {
        final UUID owner;
        final ServerLevel level;
        final BlockPos min; // wall corner (inclusive)
        final BlockPos max; // wall corner (inclusive)
        final AABB inner;   // free space inside the walls
        final Map<BlockPos, BlockState> saved = new HashMap<>();
        final Set<UUID> trapped = new HashSet<>();
        int ticksLeft = MAX_TICKS;

        Domain(UUID owner, ServerLevel level, BlockPos min, BlockPos max) {
            this.owner = owner;
            this.level = level;
            this.min = min;
            this.max = max;
            this.inner = new AABB(min.getX() + 1, min.getY() + 1, min.getZ() + 1, max.getX(), max.getY(), max.getZ());
        }

        boolean containsBlock(BlockPos p) {
            return p.getX() >= min.getX() && p.getX() <= max.getX()
                    && p.getY() >= min.getY() && p.getY() <= max.getY()
                    && p.getZ() >= min.getZ() && p.getZ() <= max.getZ();
        }

        boolean overlaps(BlockPos otherMin, BlockPos otherMax) {
            return min.getX() <= otherMax.getX() && max.getX() >= otherMin.getX()
                    && min.getY() <= otherMax.getY() && max.getY() >= otherMin.getY()
                    && min.getZ() <= otherMax.getZ() && max.getZ() >= otherMin.getZ();
        }

        void buildWalls() {
            BlockState wall = Blocks.WHITE_CONCRETE.defaultBlockState();
            for (int x = min.getX(); x <= max.getX(); x++) {
                for (int y = min.getY(); y <= max.getY(); y++) {
                    for (int z = min.getZ(); z <= max.getZ(); z++) {
                        boolean isWall = x == min.getX() || x == max.getX()
                                || y == min.getY() || y == max.getY()
                                || z == min.getZ() || z == max.getZ();
                        if (!isWall) {
                            continue;
                        }
                        BlockPos pos = new BlockPos(x, y, z);
                        if (!level.isInWorldBounds(pos)) {
                            continue;
                        }
                        BlockState old = level.getBlockState(pos);
                        if (old.isAir() || old.canBeReplaced()) {
                            saved.put(pos, old);
                            level.setBlock(pos, wall, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                        }
                    }
                }
            }
        }

        void restoreWalls() {
            for (Map.Entry<BlockPos, BlockState> entry : saved.entrySet()) {
                level.setBlock(entry.getKey(), entry.getValue(), Block.UPDATE_ALL);
            }
            saved.clear();
        }

        void enforce() {
            Iterator<UUID> it = trapped.iterator();
            while (it.hasNext()) {
                Entity e = level.getEntity(it.next());
                if (e == null || !e.isAlive()) {
                    it.remove();
                    continue;
                }
                Vec3 p = e.position();
                double x = Mth.clamp(p.x, inner.minX + 0.5, inner.maxX - 0.5);
                double y = Mth.clamp(p.y, inner.minY, inner.maxY - e.getBbHeight());
                double z = Mth.clamp(p.z, inner.minZ + 0.5, inner.maxZ - 0.5);
                if (x != p.x || y != p.y || z != p.z) {
                    if (e instanceof ServerPlayer sp) {
                        sp.connection.teleport(x, y, z, sp.getYRot(), sp.getXRot());
                    } else {
                        e.moveTo(x, y, z, e.getYRot(), e.getXRot());
                    }
                    e.setDeltaMovement(Vec3.ZERO);
                    e.hurtMarked = true;
                }
            }
        }
    }

    /** @return true if a domain was created for this player */
    public static boolean open(ServerPlayer owner) {
        if (DOMAINS.containsKey(owner.getUUID())) {
            return false;
        }
        ServerLevel level = owner.serverLevel();
        BlockPos c = owner.blockPosition();
        BlockPos min = c.offset(-5, -1, -5);
        BlockPos max = c.offset(6, 10, 6);

        for (Domain other : DOMAINS.values()) {
            if (other.level == level && other.overlaps(min, max)) {
                ChronoUtil.msg(owner, Component.translatable("message.chronoarsenal.domain_overlap"));
                return false;
            }
        }

        Domain domain = new Domain(owner.getUUID(), level, min, max);
        domain.buildWalls();
        List<LivingEntity> inside = level.getEntitiesOfClass(LivingEntity.class, domain.inner);
        for (LivingEntity e : inside) {
            domain.trapped.add(e.getUUID());
        }
        DOMAINS.put(owner.getUUID(), domain);

        level.playSound(null, c, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.5f, 0.6f);
        level.sendParticles(ParticleTypes.END_ROD, owner.getX(), owner.getY() + 1, owner.getZ(),
                60, 3.0, 2.0, 3.0, 0.05);
        ChronoUtil.msg(owner, Component.translatable("message.chronoarsenal.domain_open"));
        return true;
    }

    public static boolean isOpen(UUID owner) {
        return DOMAINS.containsKey(owner);
    }

    /** @return true if a domain existed and was closed */
    public static boolean close(UUID owner) {
        Domain domain = DOMAINS.remove(owner);
        if (domain == null) {
            return false;
        }
        domain.restoreWalls();
        domain.level.playSound(null, domain.min, SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.5f, 0.6f);
        ServerPlayer player = domain.level.getServer().getPlayerList().getPlayer(owner);
        if (player != null) {
            ChronoUtil.msg(player, Component.translatable("message.chronoarsenal.domain_close"));
        }
        return true;
    }

    public static void closeAll() {
        for (UUID owner : List.copyOf(DOMAINS.keySet())) {
            close(owner);
        }
    }

    public static void tick() {
        if (DOMAINS.isEmpty()) {
            return;
        }
        for (UUID owner : List.copyOf(DOMAINS.keySet())) {
            Domain domain = DOMAINS.get(owner);
            if (domain == null) {
                continue;
            }
            if (--domain.ticksLeft <= 0) {
                close(owner);
                continue;
            }
            domain.enforce();
        }
    }

    /** True for entities that were inside a domain when it opened and have not been freed yet. */
    public static boolean isTrapped(Entity entity) {
        for (Domain d : DOMAINS.values()) {
            if (d.level == entity.level() && d.trapped.contains(entity.getUUID())) {
                return true;
            }
        }
        return false;
    }

    /** True if this block position is part of a domain (walls or inside). */
    public static boolean isProtected(Level level, BlockPos pos) {
        for (Domain d : DOMAINS.values()) {
            if (d.level == level && d.containsBlock(pos)) {
                return true;
            }
        }
        return false;
    }
}
