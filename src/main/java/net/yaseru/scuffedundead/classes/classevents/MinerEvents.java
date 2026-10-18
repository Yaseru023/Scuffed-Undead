package net.yaseru.scuffedundead.classes.classevents;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.yaseru.scuffedundead.ScuffedUndead;
import net.yaseru.scuffedundead.classes.ClassData;
import net.yaseru.scuffedundead.classes.PlayerClass;

import java.util.List;

/**
 * Miner passives:
 *  - Prospector's Luck: mined ores have a chance to drop a second batch.
 *  - Tunnel Rat: Haste I while underground.
 */
@Mod.EventBusSubscriber(modid = ScuffedUndead.MOD_ID)
public class MinerEvents {

    // ---- Prospector's Luck
    private static final float BONUS_CHANCE = 0.20f; // 1.0f to test

    // ---- Tunnel Rat
    private static final int UNDERGROUND_Y = 50;     // below this height AND with no sky overhead
    private static final int HASTE_LEVEL = 0;        // 0 = Haste I
    private static final int HASTE_TICKS = 40;       // refreshed every few ticks, fades ~2s after leaving

    // ---------------------------------------------------------------- Prospector's Luck
    @SubscribeEvent
    public static void onOreBroken(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player) || player.isCreative()) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (ClassData.get(player).orElse(null) != PlayerClass.MINER) return;

        BlockState state = event.getState();
        if (!state.is(Tags.Blocks.ORES)) return;

        ItemStack tool = player.getMainHandItem();
        // no bonus with Silk Touch (it would duplicate ore blocks) or if the block wouldn't drop anything
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0) return;
        if (!player.hasCorrectToolForDrops(state)) return;

        if (player.getRandom().nextFloat() >= BONUS_CHANCE) return;

        BlockPos pos = event.getPos();
        List<ItemStack> drops = Block.getDrops(state, level, pos, level.getBlockEntity(pos), player, tool);
        for (ItemStack stack : drops) {
            Block.popResource(level, pos, stack);
        }
    }

    // ---------------------------------------------------------------- Tunnel Rat
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % 10 != 0) return;
        if (ClassData.get(player).orElse(null) != PlayerClass.MINER) return;

        BlockPos pos = player.blockPosition();
        boolean underground = pos.getY() < UNDERGROUND_Y && !player.level().canSeeSky(pos);
        if (underground) {
            // showIcon=true so the player can see the buff; particles hidden
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, HASTE_TICKS, HASTE_LEVEL, false, false, true));
        }
    }
}