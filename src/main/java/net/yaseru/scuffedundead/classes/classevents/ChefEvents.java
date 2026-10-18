package net.yaseru.scuffedundead.classes.classevents;

import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.yaseru.scuffedundead.ScuffedUndead;
import net.yaseru.scuffedundead.classes.ClassData;
import net.yaseru.scuffedundead.classes.PlayerClass;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Cook passive: 20% chance per item to get an extra copy of food they make. */
@Mod.EventBusSubscriber(modid = ScuffedUndead.MOD_ID)
public class ChefEvents {

    private static final float BONUS_CHANCE = 0.20f; // 1.0f = always (testing). Set to 0.20f for real play.

    // Cooking GUIs, matched by class name so there's no compile dependency on the mods.
    // menu class name -> index of the output slot in that menu
    private static final Map<String, Integer> STATIONS = Map.of(
            "vectorwing.farmersdelight.common.block.entity.container.CookingPotMenu", 8,
            "net.satisfy.farm_and_charm.client.gui.handler.CookingPotGuiHandler", 7,
            "net.satisfy.farm_and_charm.client.gui.handler.RoasterGuiHandler", 7,
            "net.satisfy.farm_and_charm.client.gui.handler.StoveGuiHandler", 0
    );
    private static final Map<UUID, ItemStack> lastOutput = new HashMap<>();

    // ---------------------------------------------------------------- crafting table / inventory
    @SubscribeEvent
    public static void onCrafted(PlayerEvent.ItemCraftedEvent event) {
        chefBonus(event.getEntity(), event.getCrafting());
    }

    // ---------------------------------------------------------------- furnace / smoker output
    @SubscribeEvent
    public static void onSmelted(PlayerEvent.ItemSmeltedEvent event) {
        chefBonus(event.getEntity(), event.getSmelting());
    }

    private static void chefBonus(Player player, ItemStack result) {
        if (player.level().isClientSide || result.isEmpty() || !result.isEdible()) return;
        if (ClassData.get(player).orElse(null) != PlayerClass.COOK) return;
        // Cooking GUIs are handled by onPlayerTick below; skip here so it can't double up.
        if (STATIONS.containsKey(player.containerMenu.getClass().getName())) return;

        LogUtils.getLogger().info("chefBonus fired for {}", result); // DEBUG

        int extra = rollExtra(player, result.getCount());
        if (extra == 0) return;

        ItemStack bonus = result.copy();
        bonus.setCount(extra);
        give(player, bonus);
    }

    // ---------------------------------------------------------------- cooking GUI output slots
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;

        AbstractContainerMenu menu = player.containerMenu;
        Integer outputSlot = STATIONS.get(menu.getClass().getName());
        if (outputSlot == null) {
            lastOutput.remove(player.getUUID()); // not a cooking GUI, or it was closed
            return;
        }
        if (ClassData.get(player).orElse(null) != PlayerClass.COOK) return;

        ItemStack now = menu.getSlot(outputSlot).getItem();
        ItemStack prev = lastOutput.get(player.getUUID());

        if (prev != null && !prev.isEmpty()) {
            int taken;
            if (now.isEmpty() || !ItemStack.isSameItemSameTags(now, prev)) taken = prev.getCount();
            else taken = prev.getCount() - now.getCount(); // negative if the pot added more food

            if (taken > 0) {
                LogUtils.getLogger().info("Cook took {} from cooking output", taken); // DEBUG
                int extra = rollExtra(player, taken);
                if (extra > 0) {
                    ItemStack bonus = prev.copy();
                    bonus.setCount(extra);
                    give(player, bonus);
                }
            }
        }
        lastOutput.put(player.getUUID(), now.copy());
    }

    // ---------------------------------------------------------------- pot served with a bowl
    @SubscribeEvent
    public static void onUsePot(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide || event.getHand() != InteractionHand.MAIN_HAND) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (ClassData.get(player).orElse(null) != PlayerClass.COOK) return;

        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(
                event.getLevel().getBlockState(event.getPos()).getBlock());
        if (id == null || !id.toString().equals("farmersdelight:cooking_pot")) return;
        LogUtils.getLogger().info("Cook right-clicked a cooking pot"); // DEBUG

        Map<Item, Integer> before = edibleCounts(player);
        player.server.tell(new TickTask(player.server.getTickCount() + 1, () -> {
            Map<Item, Integer> after = edibleCounts(player);
            for (Map.Entry<Item, Integer> e : after.entrySet()) {
                int gained = e.getValue() - before.getOrDefault(e.getKey(), 0);
                int extra = rollExtra(player, gained);
                if (extra > 0) give(player, new ItemStack(e.getKey(), extra));
            }
        }));
    }

    // ---------------------------------------------------------------- helpers
    private static int rollExtra(Player player, int count) {
        int extra = 0;
        for (int i = 0; i < count; i++) {
            if (player.getRandom().nextFloat() < BONUS_CHANCE) extra++;
        }
        return extra;
    }

    private static void give(Player player, ItemStack bonus) {
        int amount = bonus.getCount();
        if (!player.getInventory().add(bonus)) player.drop(bonus, false);

        player.displayClientMessage(Component.literal("§6Chef's bonus! +" + amount), true);
        if (player instanceof ServerPlayer sp) {
            sp.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.4f, 1.0f);
        }
    }

    private static Map<Item, Integer> edibleCounts(Player player) {
        Map<Item, Integer> counts = new HashMap<>();
        for (ItemStack s : player.getInventory().items) {
            if (!s.isEmpty() && s.isEdible()) counts.merge(s.getItem(), s.getCount(), Integer::sum);
        }
        return counts;
    }
}