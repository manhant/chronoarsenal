package com.chronoarsenal.network;

import com.chronoarsenal.ChronoArsenalMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Sent client -> server when an ability key is pressed. slot: 0 = I, 1 = R. */
public record AbilityPayload(int slot) implements CustomPacketPayload {

    public static final Type<AbilityPayload> TYPE = new Type<>(ChronoArsenalMod.id("ability"));

    public static final StreamCodec<ByteBuf, AbilityPayload> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(AbilityPayload::new, AbilityPayload::slot);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
