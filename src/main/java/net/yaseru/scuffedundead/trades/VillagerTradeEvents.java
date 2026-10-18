package net.yaseru.scuffedundead.trades;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.yaseru.scuffedundead.ScuffedUndead;

/** Swaps emeralds for scrap in every villager (and wandering trader) trade, in both directions. */
@Mod.EventBusSubscriber(modid = ScuffedUndead.MOD_ID)
public class VillagerTradeEvents {

    private static final ResourceLocation CURRENCY = new ResourceLocation("recycler", "scrap");

    /** Prices the player PAYS: each emerald becomes this much scrap. */
    private static final int PRICE_MULTIPLIER = 3;

    /** Payouts the player RECEIVES from villagers: 1 = same number of scrap as the old emeralds. */
    private static final int PAYOUT_MULTIPLIER = 1;

    // LOWEST priority so trades added by other mods' listeners are already in the list.
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onVillagerTrades(VillagerTradesEvent event) {
        event.getTrades().values().forEach(list -> list.replaceAll(VillagerTradeEvents::wrap));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onWandererTrades(WandererTradesEvent event) {
        event.getGenericTrades().replaceAll(VillagerTradeEvents::wrap);
        event.getRareTrades().replaceAll(VillagerTradeEvents::wrap);
    }

    private static VillagerTrades.ItemListing wrap(VillagerTrades.ItemListing original) {
        return (trader, random) -> {
            MerchantOffer offer = original.getOffer(trader, random);
            if (offer == null) return null;

            Item currency = ForgeRegistries.ITEMS.getValue(CURRENCY);
            if (currency == null || currency == Items.AIR) return offer; // recycler not installed

            return new MerchantOffer(
                    swap(offer.getBaseCostA(), currency, PRICE_MULTIPLIER),
                    swap(offer.getCostB(), currency, PRICE_MULTIPLIER),
                    swap(offer.getResult(), currency, PAYOUT_MULTIPLIER),
                    offer.getUses(), offer.getMaxUses(), offer.getXp(),
                    offer.getPriceMultiplier(), offer.getDemand());
        };
    }

    private static ItemStack swap(ItemStack stack, Item currency, int multiplier) {
        if (!stack.is(Items.EMERALD)) return stack;
        // a trade slot can't hold more than one stack, so cap at the item's max stack size
        int count = Math.min(stack.getCount() * multiplier, currency.getMaxStackSize());
        return new ItemStack(currency, count);
    }
}