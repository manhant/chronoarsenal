package com.chronoarsenal;

import com.chronoarsenal.network.ModNetwork;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(ChronoArsenalMod.MODID)
public class ChronoArsenalMod {

    public static final String MODID = "chronoarsenal";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ChronoArsenalMod(IEventBus modBus) {
        ModItems.ITEMS.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
        modBus.addListener(ModNetwork::register);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
