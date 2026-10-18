package net.yaseru.scuffedundead.classes;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;

import java.util.List;
import java.util.function.Supplier;

public enum PlayerClass {


    POLICE("Police",
            "You've just got accepted by the precinct, what a way to start the day!\n\n§6Suspect Tagging: §7Hostile mobs you hit glow and are briefly slowed.",
            "%s is now a Police.",
            () -> List.of(
                    gun("tacz:glock_17", "SEMI", 18),
                    ammoBox("tacz:9mm", 144),
                    itemNbt("vfl:flashlight", 1, "{Flashlight:{Charge:8206,Active:0b}}"),
                    itemNbt("minecraft:leather_chestplate", 1,
                            "{Damage:0,display:{color:3949738},Coat:\"thermal3\"}"),
                    itemNbt("minecraft:leather_leggings", 1,
                            "{Damage:0,display:{color:1908001}"),
                    itemNbt("minecraft:leather_boots", 1,
                            "{Damage:0,display:{color:1908001}"),
                    itemNbt("legendarysurvivaloverhaul:canteen", 1,
                            "{'legendarysurvivaloverhaul:HydrationCapacity':9,'legendarysurvivaloverhaul:HydrationPurity':'purified'}"),
                    item("farmersdelight:chicken_sandwich", 32),
                    item("marbledsmeeles:police_baton", 1))),

    FARMER("Farmer",
            "Has seeds, a hoe, and a lever-action. Mostly the lever-action.\n\n§6Bountiful Harvest: §720% chance of extra drops from fully grown crops.",
            "%s is now a Farmer.",
            () -> List.of(
                    gun("tacz:m870", "SEMI", 6),                      // real gun, check mag size
                    ammoBox("tacz:12g", 144),
                    itemNbt("minecraft:leather_chestplate", 1,
                            "{Damage:0,Coat:\"thermal3\"}"),
                    item("farm_and_charm:dungarees", 1),
                    item("minecraft:leather_boots", 1),
                    itemNbt("legendarysurvivaloverhaul:canteen", 1,
                            "{'legendarysurvivaloverhaul:HydrationCapacity':9,'legendarysurvivaloverhaul:HydrationPurity':'purified'}"),
                    item("farmersdelight:chicken_sandwich", 32),
                    item("zombiekit:rake", 1))),

    FIREMAN("Fireman",
            "Runs toward the burning building, which is also where the zombies are.\n\n§6Fireproof: §7Takes much less damage from fire and explosions.",
            "%s is now a Fireman.",
            () -> List.of(
                    gun("tacz:sawedoff", "SEMI", 4),                      // real gun, check mag size
                    ammoBox("tacz:12g", 144),
                    itemNbt("vfl:flashlight", 1, "{Flashlight:{Charge:8206,Active:0b}}"),
                    itemNbt("minecraft:leather_chestplate", 1,
                            "{Damage:0,display:{color:16351261},Coat:\"thermal3\"}"),
                    itemNbt("minecraft:leather_leggings", 1,
                            "{Damage:0,display:{color:16351261}"),
                    itemNbt("minecraft:leather_boots", 1,
                            "{Damage:0,display:{color:16351261}"),
                    itemNbt("legendarysurvivaloverhaul:canteen", 1,
                            "{'legendarysurvivaloverhaul:HydrationCapacity':9,'legendarysurvivaloverhaul:HydrationPurity':'purified'}"),
                    item("farmersdelight:chicken_sandwich", 32),
                    item("marbledsmeeles:fire_axe", 1))),

    COOK("Cook",
            "Fights with a frying pan and a sawn-off. Gordon Ramsay would be proud.\n\n§6Chef's Touch: §720% chance to double food you craft or cook.",
            "%s is now a Cook.",
            () -> List.of(
                    gun("tacz:db_short", "SEMI", 2),                  // PLACEHOLDER gun
                    ammoBox("tacz:12g", 144),
                    itemNbt("minecraft:leather_chestplate", 1,
                            "{Damage:0,display:{color:16383998},Coat:\"thermal3\"}"),
                    itemNbt("minecraft:leather_leggings", 1,
                            "{Damage:0,display:{color:1908001}"),
                    itemNbt("minecraft:leather_boots", 1,
                            "{Damage:0,display:{color:1908001}"),
                    itemNbt("legendarysurvivaloverhaul:canteen", 1,
                            "{'legendarysurvivaloverhaul:HydrationCapacity':9,'legendarysurvivaloverhaul:HydrationPurity':'purified'}"),
                    item("farmersdelight:chicken_sandwich", 32),
                    item("farmersdelight:skillet", 1))),

    MEDIC("Medic",
            "Googled the symptoms. Everything is fine. Probably.\n\n§6Stockpile: §720% chance to double healing items you craft.\n\n§6Distress Ping: §7Get alerted when a teammate is badly hurt.",
            "%s is now a Medic.",
            () -> List.of(
                    gun("tacz:sten", "AUTO", 32),                  // PLACEHOLDER gun
                    ammoBox("tacz:9mm", 144),
                    itemNbt("minecraft:leather_chestplate", 1,
                            "{Damage:0,display:{color:16383998},Coat:\"thermal3\"}"),
                    itemNbt("minecraft:leather_leggings", 1,
                            "{Damage:0,display:{color:16383998}"),
                    itemNbt("minecraft:leather_boots", 1,
                            "{Damage:0,display:{color:16383998}"),
                    itemNbt("legendarysurvivaloverhaul:canteen", 1,
                            "{'legendarysurvivaloverhaul:HydrationCapacity':9,'legendarysurvivaloverhaul:HydrationPurity':'purified'}"),
                    item("farmersdelight:chicken_sandwich", 32),
                    item("marbledsmelees:baseball_bat", 1))),

    HUNTER("Hunter",
            "Has seen things in the dark. Turns anything with four legs into dinner.\n\n§6Butcher: §7Knives and cleavers gain Looting while you hold them.",
            "%s is now a Hunter.",
            () -> List.of(
                    gun("tacz:springfield1873", "SEMI", 1),                  // PLACEHOLDER gun
                    ammoBox("tacz:45_70", 144),
                    itemNbt("minecraft:leather_chestplate", 1,
                            "{Damage:0,Coat:\"thermal3\"}"),
                    item("minecraft:leather_leggings", 1),
                    item("minecraft:leather_boots", 1),
                    itemNbt("legendarysurvivaloverhaul:canteen", 1,
                            "{'legendarysurvivaloverhaul:HydrationCapacity':9,'legendarysurvivaloverhaul:HydrationPurity':'purified'}"),
                    item("farmersdelight:chicken_sandwich", 32),
                    item("farmersdelight:iron_knife", 1))),

    LUMBERJACK("Lumberjack",
            "Has an axe and strong opinions about trees. Always leaves one behind.\n\n§6Replanter: §7A sapling is planted automatically when you fell a tree.",
            "%s is now a Lumberjack.",
            () -> List.of(
                    gun("tacz:mag10", "SEMI", 3),                  // PLACEHOLDER gun
                    ammoBox("ea:10g", 144),
                    itemNbt("minecraft:leather_chestplate", 1,
                            "{Damage:0,Coat:\"thermal3\"}"),
                    item("minecraft:leather_leggings", 1),
                    item("minecraft:leather_boots", 1),
                    itemNbt("legendarysurvivaloverhaul:canteen", 1,
                            "{'legendarysurvivaloverhaul:HydrationCapacity':9,'legendarysurvivaloverhaul:HydrationPurity':'purified'}"),
                    item("farmersdelight:chicken_sandwich", 32),
                    item("minecraft:iron_axe", 1))),

    MINER("Miner",
            "Knows every cave and is deeply uncomfortable above ground. Digs first, asks questions never.\n\n§6Prospector's Luck: §720% chance of extra drops from ores.\n\n§6Tunnel Rat: §7Mine faster while underground.",
            "%s is now a Miner.",
            () -> List.of(
                    item("minecraft:iron_pickaxe", 1),
                    item("minecraft:torch", 32),
                    itemNbt("vfl:helmet_miners", 1,
                            "{Damage:0,Flashlight:{Charge:8326,Active:0b}}"),
                    itemNbt("bludgeons_arc:service_fabric_chestplate", 1,
                            "{Damage:0,Coat:\"thermal3\"}"),
                    item("bludgeons_arc:service_fabric_leggings", 1),
                    item("bludgeons_arc:primary_fabric_boots", 1),
                    itemNbt("legendarysurvivaloverhaul:canteen", 1,
                            "{'legendarysurvivaloverhaul:HydrationCapacity':9,'legendarysurvivaloverhaul:HydrationPurity':'purified'}"),
                    item("farmersdelight:chicken_sandwich", 32))),

    VETERAN("Veteran",
            "Has seen worse than zombies and will tell you about it.\n\n§6Frugal Shooter: §720% chance a shot doesn't use ammo.",
            "%s is now a Veteran.",
            () -> List.of(
                    gun("maxstuff:m1_garand", "SEMI", 8),                  // PLACEHOLDER gun
                    ammoBox("tacz:30_06", 144),
                    itemNbt("minecraft:leather_chestplate", 1,
                            "{Damage:0,display:{color:6192150},Coat:\"thermal3\"}"),
                    itemNbt("minecraft:leather_leggings", 1,
                            "{Damage:0,display:{color:6192150}}"),
                    itemNbt("minecraft:leather_boots", 1,
                            "{Damage:0,display:{color:6192150}}"),
                    itemNbt("legendarysurvivaloverhaul:canteen", 1,
                            "{'legendarysurvivaloverhaul:HydrationCapacity':9,'legendarysurvivaloverhaul:HydrationPurity':'purified'}"),
                    item("farmersdelight:chicken_sandwich", 32),
                    item("marbledsmelees:machete", 1))),

    OTAKU("Otaku",
            "Learned everything from anime and nothing from practice. Swings a sword with far too much confidence.\n\n§6Bucket List: §7Melee kills in a row stack Speed.\n\n§6Opening Strike: §7Your first hit after a pause deals bonus damage.\n\n§6Nihon Enthusiast: §7Has read every volume. Japanese blades hit harder in your hands.",
            "%s is now an Otaku.",
            () -> List.of(
                    itemNbt("oyoroi:iron_suji_kabuto_crescent", 1,
                            "{Damage:0,display:{color:11546150}}"),
                    itemNbt("minecraft:leather_chestplate", 1,
                            "{Damage:0,display:{color:11546150},Coat:\"thermal3\"}"),
                    itemNbt("minecraft:leather_leggings", 1,
                            "{Damage:0,display:{color:11546150}}"),
                    itemNbt("minecraft:leather_boots", 1,
                            "{Damage:0,display:{color:11546150}}"),
                    item("marbledsmelees:katana", 1),
                    itemNbt("legendarysurvivaloverhaul:canteen", 1,
                            "{'legendarysurvivaloverhaul:HydrationCapacity':9,'legendarysurvivaloverhaul:HydrationPurity':'purified'}"),
                    item("farmersdelight:chicken_sandwich", 32))),

    LARPER("LARPer",
            "Showed up to the apocalypse in full plate armor. Thankfully he was in a museum when the outbreak happened.\n\n§6Iron Will: §7More armor means less damage taken.\n\n§6Medieval Might: §7Holding a medieval weapon grants Resistance.",
            "%s is now a LARPer.",
            () -> List.of(
                    item("tkvm_weapons:iron_zweihander", 1),
                    item("bludgeons_arc:primary_knight_helmet", 1),
                    itemNbt("bludgeons_arc:primary_knight_chestplate", 1,
                            "{Damage:0,Coat:\"thermal3\"}"),
                    itemNbt("bludgeons_arc:primary_knight_leggings", 1,
                            "{Damage:0,Coat:\"thermal3\"}"),
                    item("bludgeons_arc:primary_knight_boots", 1),
                    item("farmersdelight:chicken_sandwich", 32),
                    itemNbt("legendarysurvivaloverhaul:canteen", 1,
                            "{'legendarysurvivaloverhaul:HydrationCapacity':9,'legendarysurvivaloverhaul:HydrationPurity':'purified'}"),
                    item("farmersdelight:chicken_sandwich", 32)));               // PLACEHOLDER medieval weapon


    public final String displayName;
    public final String description;
    private final String quip;
    private final Supplier<List<ItemStack>> kit;

    PlayerClass(String displayName, String description, String quip,
                Supplier<List<ItemStack>> kit) {
        this.displayName = displayName;
        this.description = description;
        this.quip = quip;
        this.kit = kit;
    }

    public Component announcement(String playerName) {
        return Component.literal("§e" + String.format(quip, playerName));
    }

    public void giveKit(ServerPlayer player) {
        for (ItemStack stack : kit.get()) {
            if (stack.isEmpty()) continue; // e.g. TaCZ not installed

            // auto-equip armor if that slot is free
            if (stack.getItem() instanceof ArmorItem armor) {
                EquipmentSlot slot = armor.getEquipmentSlot();
                if (player.getItemBySlot(slot).isEmpty()) {
                    player.setItemSlot(slot, stack);
                    continue;
                }
            }

            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
        }
    }

    // ------------------------------------------------- generic item helpers
    // Work with ANY item by full registry ID ("modid:item"). If the mod isn't
    // installed or the ID is wrong, they return EMPTY and giveKit() skips them.

    /** Look up an item by its full ID, "modid:item". Returns null if not found. */
    private static Item getItem(String id) {
        ResourceLocation rl = id.contains(":") ? ResourceLocation.tryParse(id) : null;
        if (rl == null || !ForgeRegistries.ITEMS.containsKey(rl)) {
            LogUtils.getLogger().warn("[ScuffedUndead] Unknown or malformed kit item (use modid:item): {}", id);
            return null;
        }
        return ForgeRegistries.ITEMS.getValue(rl);
    }

    /** item("minecraft:iron_hoe", 1) */
    private static ItemStack item(String id, int count) {
        Item item = getItem(id);
        return item == null ? ItemStack.EMPTY : new ItemStack(item, count);
    }

    /** item("modid:cleaver", 1, "Chef's Cleaver") - with a custom name */
    private static ItemStack item(String id, int count, String name) {
        ItemStack stack = item(id, count);
        if (!stack.isEmpty()) stack.setHoverName(Component.literal(name));
        return stack;
    }

    /** itemNbt("modid:thing", 1, "{SomeTag:1b}") - paste the tag from /data get ... SelectedItem.tag */
    private static ItemStack itemNbt(String id, int count, String snbt) {
        ItemStack stack = item(id, count);
        if (stack.isEmpty()) return stack;
        try {
            stack.setTag(TagParser.parseTag(snbt));
        } catch (CommandSyntaxException e) {
            LogUtils.getLogger().warn("[ScuffedUndead] Bad NBT for kit item {}: {}", id, snbt);
        }
        return stack;
    }

    private static ItemStack named(ItemLike item, int count, String name) {
        ItemStack stack = new ItemStack(item, count);
        stack.setHoverName(Component.literal(name));
        return stack;
    }

    /** tacz:modern_kinetic_gun with GunId etc. Returns EMPTY if TaCZ isn't loaded. */
    private static ItemStack gun(String gunId, String fireMode, int loadedAmmo) {
        Item item = getItem("tacz:modern_kinetic_gun");
        if (item == null) return ItemStack.EMPTY;
        ItemStack stack = new ItemStack(item);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("GunId", gunId);
        tag.putString("GunFireMode", fireMode);
        tag.putInt("GunCurrentAmmoCount", loadedAmmo);
        tag.putBoolean("HasBulletInBarrel", loadedAmmo > 0);
        return stack;
    }

    /** tacz:ammo_box. Level 0 holds up to 144 rounds. */
    private static ItemStack ammoBox(String ammoId, int amount) {
        Item item = getItem("tacz:ammo_box");
        if (item == null) return ItemStack.EMPTY;
        ItemStack stack = new ItemStack(item);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("Level", 0);
        tag.putInt("AmmoCount", Math.min(amount, 144));
        tag.putString("AmmoId", ammoId);
        return stack;
    }
}