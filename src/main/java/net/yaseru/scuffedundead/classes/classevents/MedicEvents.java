package net.yaseru.scuffedundead.classes.classevents;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.yaseru.scuffedundead.ScuffedUndead;
import net.yaseru.scuffedundead.classes.ClassData;
import net.yaseru.scuffedundead.classes.PlayerClass;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Medic passives:
 *  1. Supplies bonus: chance to get an extra copy of healing items they craft.
 *  2. Distress ping: action bar alert when a teammate is badly hurt nearby.
 */
@Mod.EventBusSubscriber(modid = ScuffedUndead.MOD_ID)
public class MedicEvents {

    // ---------------------------------------------------------------- settings
    private static final float BONUS_CHANCE = 0.20f;      // chance per crafted item (1.0f to test)
    private static final float DISTRESS_THRESHOLD = 0.25f; // alert at or below 25% health
    private static final double DISTRESS_RANGE = 48.0;    // blocks
    private static final int DISTRESS_COOLDOWN = 200;     // ticks (10s) between alerts per player

    /**
     * Healing items the Medic can double-craft. Full IDs only: "modid:item".
     * Items from mods that aren't installed simply never match, so nothing breaks.
     */
    private static final String[] BONUS_ITEMS = {
            "legendarysurvivaloverhaul:bandage",
            "legendarysurvivaloverhaul:healing_herbs",
            "legendarysurvivaloverhaul:medkit",
            "legendarysurvivaloverhaul:morphine",
            "legendarysurvivaloverhaul:plaster",
            "legendarysurvivaloverhaul:tonic",
            "marbledsfirstaid:bandages",
            "marbledsfirstaid:medkit",
            "marbledsfirstaid:medical_bag",
            "marbledsfirstaid:first_aid_kit",
            "marbledsfirstaid:bandaid_box",
            "marbledsfirstaid:bandaid",
            "marbledsfirstaid:pill_bottle",
            "marbledsfirstaid:pain_pills",
            "marbledsfirstaid:curative_pills",
            "marbledsfirstaid:panacea_pills",
            "medieval_healing:bandage",
            "medieval_healing:healing_wrap",
            "medieval_healing:leather_binding",
            "medieval_healing:raw_herbal_mix",
            "medieval_healing:herbal_medicine",
            "medieval_healing:herbal_tonic",
            "medieval_healing:suture_kit",
            "zombiekit:bandage",
            "zombiekit:painkiller",
            "zombiekit:medical_kit"
    };

    private static final Set<ResourceLocation> BONUS_SET = Arrays.stream(BONUS_ITEMS)
            .map(ResourceLocation::tryParse)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

    private static final Map<UUID, Long> lastAlert = new HashMap<>();

    // ---------------------------------------------------------------- supplies bonus
    // Crafting table / inventory grid only (all the healing recipes are crafted there).
    @SubscribeEvent
    public static void onCrafted(PlayerEvent.ItemCraftedEvent event) {
        Player player = event.getEntity();
        ItemStack result = event.getCrafting();
        if (player.level().isClientSide || result.isEmpty()) return;
        if (ClassData.get(player).orElse(null) != PlayerClass.MEDIC) return;
        if (!isBonusItem(result)) return;

        int extra = 0;
        for (int i = 0; i < result.getCount(); i++) {
            if (player.getRandom().nextFloat() < BONUS_CHANCE) extra++;
        }
        if (extra == 0) return;

        ItemStack bonus = result.copy();
        bonus.setCount(extra);
        if (!player.getInventory().add(bonus)) player.drop(bonus, false);

        player.displayClientMessage(Component.literal("§aMedic's stash! +" + extra), true);
        if (player instanceof ServerPlayer sp) {
            sp.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.7f, 1.4f);
        }
    }

    private static boolean isBonusItem(ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id != null && BONUS_SET.contains(id);
    }

    // ---------------------------------------------------------------- distress ping
    @SubscribeEvent
    public static void onDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer victim)) return;

        float after = victim.getHealth() - event.getAmount();
        if (after <= 0 || after > victim.getMaxHealth() * DISTRESS_THRESHOLD) return; // fine, or dead

        long now = victim.level().getGameTime();
        Long last = lastAlert.get(victim.getUUID());
        if (last != null && now < last + DISTRESS_COOLDOWN) return;
        lastAlert.put(victim.getUUID(), now);

        for (ServerPlayer medic : victim.server.getPlayerList().getPlayers()) {
            if (medic == victim || medic.level() != victim.level()) continue;
            if (ClassData.get(medic).orElse(null) != PlayerClass.MEDIC) continue;
            if (medic.distanceToSqr(victim) > DISTRESS_RANGE * DISTRESS_RANGE) continue;

            int meters = (int) medic.distanceTo(victim);
            medic.displayClientMessage(Component.literal(
                    "§c⚠ " + victim.getGameProfile().getName() + " is hurt! (" + meters + "m)"), true);
            medic.playNotifySound(SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 0.8f, 1.0f);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        lastAlert.remove(event.getEntity().getUUID());
    }
}