package net.yaseru.scuffedundead.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.yaseru.scuffedundead.classes.ClassData;
import net.yaseru.scuffedundead.classes.PlayerClass;

import java.util.function.Supplier;

/** Client -> Server: "I choose this one." Server validates everything. */
public record SelectClassPacket(int classIndex) {

    public static void encode(SelectClassPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.classIndex);
    }

    public static SelectClassPacket decode(FriendlyByteBuf buf) {
        return new SelectClassPacket(buf.readVarInt());
    }

    public static void handle(SelectClassPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        c.enqueueWork(() -> {
            ServerPlayer player = c.getSender();
            PlayerClass[] all = PlayerClass.values();
            if (player == null || msg.classIndex < 0 || msg.classIndex >= all.length) return;
            if (ClassData.get(player).isPresent()) return; // already chose, no take-backs
            ClassData.assign(player, all[msg.classIndex]);
        });
        c.setPacketHandled(true);
    }
}
