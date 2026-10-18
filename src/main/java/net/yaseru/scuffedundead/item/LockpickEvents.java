package net.yaseru.scuffedundead.item;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.yaseru.scuffedundead.ScuffedUndead;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = ScuffedUndead.MOD_ID)
public class LockpickEvents {

    private static final String LOCK_FLAG = "LockFlag"; // inside the block entity's ForgeData
    private static final String LOCK = "Lock";          // vanilla lock string
    private static final int WATCH_TICKS = 40;          // how long to wait for the lock to open (2s)

    private record Watch(ServerLevel level, BlockPos pos, UUID player, long start) {}

    private static final List<Watch> WATCHES = new ArrayList<>();

    // HIGHEST: we need to see the lock state BEFORE Sona (or anything else) opens it.
    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void onUse(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide || event.getHand() != InteractionHand.MAIN_HAND) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(event.getItemStack().getItem() instanceof LockpickItem)) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        BlockPos pos = event.getPos();
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null || !isLocked(be)) return;

        LogUtils.getLogger().info("[Lockpick] locked container clicked at {}: {}", pos, describe(be)); // DEBUG
        WATCHES.add(new Watch(level, pos, player.getUUID(), level.getGameTime()));
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) return;
        if (WATCHES.isEmpty()) return;

        Iterator<Watch> it = WATCHES.iterator();
        while (it.hasNext()) {
            Watch watch = it.next();
            if (watch.level() != level) continue;

            BlockEntity be = level.getBlockEntity(watch.pos());
            long elapsed = level.getGameTime() - watch.start();

            if (be != null && !isLocked(be)) {          // it opened
                it.remove();
                LogUtils.getLogger().info("[Lockpick] container opened at {}: {}", watch.pos(), describe(be)); // DEBUG
                wearDown(level, watch);
            } else if (be == null || elapsed > WATCH_TICKS) { // gone, or it never opened
                it.remove();
            }
        }
    }

    private static void wearDown(ServerLevel level, Watch watch) {
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(watch.player());
        if (player == null) return;

        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof LockpickItem)) return; // switched items or it was already used up
        stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND));
    }

    /** LockFlag decides if the container has it; otherwise fall back to the vanilla Lock string. */
    private static boolean isLocked(BlockEntity be) {
        CompoundTag forgeData = be.getPersistentData();
        if (forgeData.contains(LOCK_FLAG)) return forgeData.getBoolean(LOCK_FLAG);

        CompoundTag tag = be.saveWithoutMetadata();
        return tag.contains(LOCK, Tag.TAG_STRING) && !tag.getString(LOCK).isEmpty();
    }

    private static String describe(BlockEntity be) {
        CompoundTag tag = be.saveWithoutMetadata();
        return "LockFlag=" + (be.getPersistentData().contains(LOCK_FLAG) ? be.getPersistentData().getBoolean(LOCK_FLAG) : "none")
                + ", Lock=" + (tag.contains(LOCK) ? tag.getString(LOCK) : "none");
    }
}