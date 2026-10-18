package net.yaseru.scuffedundead.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.yaseru.scuffedundead.client.ClientHooks;

import java.util.function.Supplier;

/** Server -> Client: "pick your class, buddy". */
public record OpenClassScreenPacket() {

    public static void encode(OpenClassScreenPacket msg, FriendlyByteBuf buf) {}

    public static OpenClassScreenPacket decode(FriendlyByteBuf buf) {
        return new OpenClassScreenPacket();
    }

    public static void handle(OpenClassScreenPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientHooks::openClassScreen));
        ctx.get().setPacketHandled(true);
    }
}