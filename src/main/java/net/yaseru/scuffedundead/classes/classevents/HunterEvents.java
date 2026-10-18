package net.yaseru.scuffedundead.classes.classevents;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.yaseru.scuffedundead.ScuffedUndead;
import net.yaseru.scuffedundead.classes.ClassData;
import net.yaseru.scuffedundead.classes.PlayerClass;

import java.util.Map;

/**
 * Hunter passive: a knife or cleaver in the Hunter's main hand gets real Looting levels.
 * The enchantment is added while held and removed as soon as it leaves the Hunter's hand,
 * so it works with anything that reads the tool's enchantments (butcher mods, normal kills).
 */
@Mod.EventBusSubscriber(modid = ScuffedUndead.MOD_ID)
public class HunterEvents {

    private static final int BONUS_LEVELS = 2;           // Looting levels added while held
    // Any item whose ID contains one of these words counts as a butchering tool.
    private static final String[] TOOL_KEYWORDS = {"knife", "cleaver"};
    private static final String MARK = "scuffedundead_hunter_looting"; // remembers the knife's original Looting level

    // ---------------------------------------------------------------- apply / remove
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % 2 != 0) return;

        boolean hunter = ClassData.get(player).orElse(null) == PlayerClass.HUNTER;
        ItemStack main = player.getMainHandItem();
        boolean keep = hunter && isButcherTool(main);

        stripAll(player, keep ? main : ItemStack.EMPTY); // remove from everything except the held knife
        if (keep && !hasMark(main)) addBonus(main);
    }

    // ---------------------------------------------------------------- clean-up so it can't leak out
    @SubscribeEvent
    public static void onToss(ItemTossEvent event) {
        removeBonus(event.getEntity().getItem());
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) stripAll(player, ItemStack.EMPTY);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) stripAll(player, ItemStack.EMPTY);
    }

    // ---------------------------------------------------------------- helpers
    private static void stripAll(ServerPlayer player, ItemStack keep) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s != keep) removeBonus(s);
        }
        for (Slot slot : player.containerMenu.slots) { // also covers an open chest
            ItemStack s = slot.getItem();
            if (s != keep) removeBonus(s);
        }
        ItemStack carried = player.containerMenu.getCarried(); // item on the cursor
        if (carried != keep) removeBonus(carried);
    }

    private static boolean isButcherTool(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null) return false;
        for (String keyword : TOOL_KEYWORDS) {
            if (id.getPath().contains(keyword)) return true;
        }
        return false;
    }

    private static boolean hasMark(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains(MARK);
    }

    private static void addBonus(ItemStack stack) {
        Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(stack);
        int original = enchants.getOrDefault(Enchantments.MOB_LOOTING, 0);
        enchants.put(Enchantments.MOB_LOOTING, original + BONUS_LEVELS);
        EnchantmentHelper.setEnchantments(enchants, stack);
        stack.getOrCreateTag().putInt(MARK, original);
    }

    private static void removeBonus(ItemStack stack) {
        if (!hasMark(stack)) return;
        CompoundTag tag = stack.getTag();
        int original = tag.getInt(MARK);

        Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(stack);
        if (original > 0) enchants.put(Enchantments.MOB_LOOTING, original);
        else enchants.remove(Enchantments.MOB_LOOTING);
        EnchantmentHelper.setEnchantments(enchants, stack);

        tag.remove(MARK);
        if (tag.isEmpty()) stack.setTag(null); // keep the knife stackable with clean ones
    }
}