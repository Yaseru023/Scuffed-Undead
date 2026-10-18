package net.yaseru.scuffedundead.classes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

/** Stores the chosen class in the player's persistent NBT (survives death & relogs). */
public final class ClassData {
    private static final String KEY = "scuffedundead_class";

    private ClassData() {}

    public static Optional<PlayerClass> get(Player player) {
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        if (!persisted.contains(KEY)) return Optional.empty();
        try {
            return Optional.of(PlayerClass.valueOf(persisted.getString(KEY)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public static void set(Player player, PlayerClass playerClass) {
        CompoundTag root = player.getPersistentData();
        CompoundTag persisted = root.getCompound(Player.PERSISTED_NBT_TAG);
        persisted.putString(KEY, playerClass.name());
        root.put(Player.PERSISTED_NBT_TAG, persisted);
    }

    public static void clear(Player player) {
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        persisted.remove(KEY);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
    }

    /** Server-side: lock in the class, hand out the kit, tell everyone. */
    public static void assign(ServerPlayer player, PlayerClass playerClass) {
        set(player, playerClass);
        playerClass.giveKit(player);
        player.server.getPlayerList().broadcastSystemMessage(
                playerClass.announcement(player.getGameProfile().getName()), false);
    }
}