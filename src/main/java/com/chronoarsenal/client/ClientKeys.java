package com.chronoarsenal.client;

import com.chronoarsenal.ChronoArsenalMod;
import com.chronoarsenal.core.ChronoWeapon;
import com.chronoarsenal.network.AbilityPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public class ClientKeys {

    public static final KeyMapping KEY_I = new KeyMapping(
            "key.chronoarsenal.ability_i", GLFW.GLFW_KEY_I, "key.categories.chronoarsenal");
    public static final KeyMapping KEY_R = new KeyMapping(
            "key.chronoarsenal.ability_r", GLFW.GLFW_KEY_R, "key.categories.chronoarsenal");

    @EventBusSubscriber(modid = ChronoArsenalMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(KEY_I);
            event.register(KEY_R);
        }
    }

    @EventBusSubscriber(modid = ChronoArsenalMod.MODID, value = Dist.CLIENT)
    public static class GameBusEvents {
        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (player == null) {
                return;
            }
            boolean holdingWeapon = player.getMainHandItem().getItem() instanceof ChronoWeapon;

            // consumeClick must always be drained, otherwise presses queue up.
            while (KEY_I.consumeClick()) {
                if (holdingWeapon && mc.screen == null) {
                    PacketDistributor.sendToServer(new AbilityPayload(ChronoWeapon.KEY_I));
                }
            }
            while (KEY_R.consumeClick()) {
                if (holdingWeapon && mc.screen == null) {
                    PacketDistributor.sendToServer(new AbilityPayload(ChronoWeapon.KEY_R));
                }
            }
        }
    }
}
