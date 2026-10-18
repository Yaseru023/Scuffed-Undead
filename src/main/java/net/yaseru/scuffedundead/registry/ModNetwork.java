package net.yaseru.scuffedundead.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.yaseru.scuffedundead.ScuffedUndead;
import net.yaseru.scuffedundead.network.OpenClassScreenPacket;
import net.yaseru.scuffedundead.network.SelectClassPacket;

public class ModNetwork {
    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ScuffedUndead.MOD_ID, "main"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, OpenClassScreenPacket.class,
                OpenClassScreenPacket::encode, OpenClassScreenPacket::decode, OpenClassScreenPacket::handle);
        CHANNEL.registerMessage(id++, SelectClassPacket.class,
                SelectClassPacket::encode, SelectClassPacket::decode, SelectClassPacket::handle);
    }
}
