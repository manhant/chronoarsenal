package com.chronoarsenal.event;

import com.chronoarsenal.ChronoArsenalMod;
import com.chronoarsenal.core.ChronoCooldowns;
import com.chronoarsenal.core.ChronoWeapon;
import com.chronoarsenal.core.DomainManager;
import com.chronoarsenal.core.ServerScheduler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = ChronoArsenalMod.MODID)
public class CommonEvents {

    /** Weapons taken out of a dying player's drops, given back when the player respawns. */
    private static final Map<UUID, List<ItemStack>> SOULBOUND = new HashMap<>();

    // ---------------------------------------------------------------- ticking / cleanup

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        ServerScheduler.tick();
        DomainManager.tick();
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        DomainManager.closeAll(); // puts the terrain back before the world is saved
        ServerScheduler.clear();
        ChronoCooldowns.clear();
        SOULBOUND.clear();
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        DomainManager.close(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            DomainManager.close(player.getUUID());
        }
    }

    // ---------------------------------------------------------------- domain protection

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (DomainManager.isProtected(event.getPlayer().level(), event.getPos())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof Level level && DomainManager.isProtected(level, event.getPos())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEnderEntityTeleport(EntityTeleportEvent.EnderEntity event) {
        if (DomainManager.isTrapped(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEnderPearlTeleport(EntityTeleportEvent.EnderPearl event) {
        if (DomainManager.isTrapped(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onChorusTeleport(EntityTeleportEvent.ChorusFruit event) {
        if (DomainManager.isTrapped(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onCommandTeleport(EntityTeleportEvent.TeleportCommand event) {
        if (DomainManager.isTrapped(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onSpreadPlayers(EntityTeleportEvent.SpreadPlayersCommand event) {
        if (DomainManager.isTrapped(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    // ---------------------------------------------------------------- Ragnarok lightning never hurts its owner / allies

    @SubscribeEvent
    public static void onIncomingDamage(net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent event) {
        if (event.getSource().getDirectEntity() instanceof net.minecraft.world.entity.LightningBolt bolt
                && bolt.getCause() != null) {
            ServerPlayer owner = bolt.getCause();
            if (event.getEntity() == owner || event.getEntity().isAlliedTo(owner)) {
                event.setCanceled(true);
            }
        }
    }

    // ---------------------------------------------------------------- weapons never drop on death

    @SubscribeEvent
    public static void onPlayerDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        List<ItemStack> kept = new ArrayList<>();
        Iterator<ItemEntity> it = event.getDrops().iterator();
        while (it.hasNext()) {
            ItemStack stack = it.next().getItem();
            if (stack.getItem() instanceof ChronoWeapon) {
                kept.add(stack.copy());
                it.remove();
            }
        }
        if (!kept.isEmpty()) {
            SOULBOUND.computeIfAbsent(player.getUUID(), k -> new ArrayList<>()).addAll(kept);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath() || !(event.getEntity() instanceof ServerPlayer newPlayer)) {
            return;
        }
        List<ItemStack> kept = SOULBOUND.remove(event.getOriginal().getUUID());
        if (kept == null) {
            return;
        }
        for (ItemStack stack : kept) {
            if (!newPlayer.getInventory().add(stack)) {
                newPlayer.drop(stack, false); // inventory full: drop at feet (the item itself is safe from lava/fire)
            }
        }
    }
}
