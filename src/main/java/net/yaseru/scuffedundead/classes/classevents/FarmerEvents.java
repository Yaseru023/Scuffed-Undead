package net.yaseru.scuffedundead.classes.classevents;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.yaseru.scuffedundead.ScuffedUndead;
import net.yaseru.scuffedundead.classes.ClassData;
import net.yaseru.scuffedundead.classes.PlayerClass;

import java.util.List;

/** Farmer passive ("Bountiful Harvest"): fully grown crops have a chance to drop a second batch. */
@Mod.EventBusSubscriber(modid = ScuffedUndead.MOD_ID)
public class FarmerEvents {

    private static final float BONUS_CHANCE = 0.20f; // 1.0f to test

    @SubscribeEvent
    public static void onCropBroken(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player) || player.isCreative()) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (ClassData.get(player).orElse(null) != PlayerClass.FARMER) return;

        BlockState state = event.getState();
        // only fully grown crops (wheat, carrots, potatoes, beetroots, and modded crops built on CropBlock)
        if (!(state.getBlock() instanceof CropBlock crop) || !crop.isMaxAge(state)) return;
        if (player.getRandom().nextFloat() >= BONUS_CHANCE) return;

        BlockPos pos = event.getPos();
        List<ItemStack> drops = Block.getDrops(state, level, pos, level.getBlockEntity(pos), player, player.getMainHandItem());
        for (ItemStack stack : drops) {
            Block.popResource(level, pos, stack);
        }
    }
}