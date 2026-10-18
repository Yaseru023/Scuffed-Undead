package net.yaseru.scuffedundead.classes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.yaseru.scuffedundead.ScuffedUndead;
import net.yaseru.scuffedundead.network.OpenClassScreenPacket;
import net.yaseru.scuffedundead.registry.ModNetwork;

@Mod.EventBusSubscriber(modid = ScuffedUndead.MOD_ID)
public class ClassEvents {

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && ClassData.get(player).isEmpty()) {
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new OpenClassScreenPacket());
        }
    }

    /** Keep the class after dying / going through the End portal. */
    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        CompoundTag old = event.getOriginal().getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        event.getEntity().getPersistentData().put(Player.PERSISTED_NBT_TAG, old.copy());
    }
}