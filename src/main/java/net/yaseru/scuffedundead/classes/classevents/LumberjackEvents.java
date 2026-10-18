package net.yaseru.scuffedundead.classes.classevents;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.yaseru.scuffedundead.ScuffedUndead;
import net.yaseru.scuffedundead.classes.ClassData;
import net.yaseru.scuffedundead.classes.PlayerClass;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Lumberjack passive: when the base log of a tree is cut, a sapling of the same wood
 * type is planted where the tree stood (or where the player is standing, as a fallback).
 * Planting waits a moment so tree-felling mods can finish clearing the area, then the
 * sapling is checked a few times and replanted if something broke it.
 */
@Mod.EventBusSubscriber(modid = ScuffedUndead.MOD_ID)
public class LumberjackEvents {

    private static final int PLANT_DELAY_TICKS = 40;      // wait before planting (20 ticks = 1 second)
    private static final int VERIFY_INTERVAL_TICKS = 20;  // how often to re-check the sapling
    private static final int VERIFY_TRIES = 3;            // how many re-checks

    // ---------------------------------------------------------------- the trigger
    // receiveCanceled = true: tree-felling mods often cancel the normal break and remove the logs
    // themselves, and by default we would never hear about it.
    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onLogBroken(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (ClassData.get(player).orElse(null) != PlayerClass.LUMBERJACK) return;

        BlockState broken = event.getState();
        if (!broken.is(BlockTags.LOGS)) return;
        LogUtils.getLogger().info("[Lumberjack] log break, canceled={}", event.isCanceled()); // DEBUG

        Block sapling = saplingFor(broken);
        if (sapling == null) {
            LogUtils.getLogger().info("[Lumberjack] no sapling found for {}", broken.getBlock()); // DEBUG
            return;
        }

        BlockPos logPos = event.getPos();
        BlockState saplingState = sapling.defaultBlockState();

        // "Base log" = a sapling could grow where this log stands (valid soil below it).
        // Trunk logs higher up have air or a sapling below them, so they don't count.
        if (!saplingState.canSurvive(level, logPos)) return;

        BlockPos feet = player.blockPosition();

        later(level, PLANT_DELAY_TICKS, () -> {
            // If the log is still standing, the break was really blocked (e.g. by a protection mod).
            if (level.getBlockState(logPos).is(BlockTags.LOGS)) {
                LogUtils.getLogger().info("[Lumberjack] log still standing, nothing planted"); // DEBUG
                return;
            }

            BlockPos target = null;
            if (tryPlant(level, logPos, saplingState)) target = logPos;
            else if (tryPlant(level, feet, saplingState)) target = feet;

            if (target == null) {
                LogUtils.getLogger().info("[Lumberjack] nowhere valid to plant"); // DEBUG
                return;
            }
            LogUtils.getLogger().info("[Lumberjack] planted at {}", target); // DEBUG
            verify(level, target, saplingState, VERIFY_TRIES);
        });
    }

    // ---------------------------------------------------------------- planting
    private static boolean tryPlant(ServerLevel level, BlockPos pos, BlockState sapling) {
        BlockState current = level.getBlockState(pos);
        if (!current.isAir() && !current.canBeReplaced()) return false;     // spot must be free
        if (!current.getFluidState().isEmpty()) return false;               // not in water
        if (!sapling.canSurvive(level, pos)) return false;                  // needs valid soil below
        level.setBlock(pos, sapling, 3);
        return true;
    }

    /** Re-checks the sapling a few times and plants it again if something removed it. */
    private static void verify(ServerLevel level, BlockPos pos, BlockState sapling, int triesLeft) {
        if (triesLeft <= 0) return;
        later(level, VERIFY_INTERVAL_TICKS, () -> {
            BlockState current = level.getBlockState(pos);
            if (!current.is(sapling.getBlock()) && tryPlant(level, pos, sapling)) {
                LogUtils.getLogger().info("[Lumberjack] sapling was removed, replanted at {}", pos); // DEBUG
            }
            verify(level, pos, sapling, triesLeft - 1);
        });
    }

    /** oak_log -> oak_sapling, and the same pattern for modded woods (modid:cedar_log -> modid:cedar_sapling). */
    private static Block saplingFor(BlockState log) {
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(log.getBlock());
        if (id == null) return null;

        String path = id.getPath();
        if (path.equals("mangrove_log")) return Blocks.MANGROVE_PROPAGULE;
        if (!path.endsWith("_log")) return null; // also skips stripped logs: no such sapling exists

        ResourceLocation saplingId = new ResourceLocation(
                id.getNamespace(), path.substring(0, path.length() - 4) + "_sapling");
        if (!ForgeRegistries.BLOCKS.containsKey(saplingId)) return null;

        Block block = ForgeRegistries.BLOCKS.getValue(saplingId);
        return block instanceof SaplingBlock ? block : null;
    }

    // ---------------------------------------------------------------- tiny delay scheduler
    private record Task(ServerLevel level, long due, Runnable action) {}

    private static final List<Task> TASKS = new ArrayList<>();

    private static void later(ServerLevel level, int ticks, Runnable action) {
        TASKS.add(new Task(level, level.getGameTime() + ticks, action));
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) return;
        if (TASKS.isEmpty()) return;

        List<Task> ready = new ArrayList<>();
        Iterator<Task> it = TASKS.iterator();
        while (it.hasNext()) {
            Task task = it.next();
            if (task.level() == level && level.getGameTime() >= task.due()) {
                ready.add(task);
                it.remove();
            }
        }
        // run after the loop, because a task may schedule new tasks
        ready.forEach(task -> task.action().run());
    }
}