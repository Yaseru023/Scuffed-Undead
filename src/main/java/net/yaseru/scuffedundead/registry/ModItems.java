package net.yaseru.scuffedundead.registry;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.yaseru.scuffedundead.ScuffedUndead;
import net.yaseru.scuffedundead.item.LockpickItem;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ScuffedUndead.MOD_ID);

    public static final RegistryObject<Item> IRON_LOCKPICK = ITEMS.register("iron_lockpick",
            () -> new LockpickItem(new Item.Properties().durability(10)));

    public static final RegistryObject<Item> IRON_LOCKPICK_BLADE = ITEMS.register("iron_lockpick_blade",
            () -> new Item(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> STEEL_LOCKPICK = ITEMS.register("steel_lockpick",
            () -> new LockpickItem(new Item.Properties().durability(20)));

    public static final RegistryObject<Item> STEEL_LOCKPICK_BLADE = ITEMS.register("steel_lockpick_blade",
            () -> new Item(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> DIAMOND_LOCKPICK = ITEMS.register("diamond_lockpick",
            () -> new LockpickItem(new Item.Properties().durability(40)));

    public static final RegistryObject<Item> DIAMOND_LOCKPICK_BLADE = ITEMS.register("diamond_lockpick_blade",
            () -> new Item(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> NETHERITE_LOCKPICK = ITEMS.register("netherite_lockpick",
            () -> new LockpickItem(new Item.Properties().durability(100).fireResistant()));

    public static final RegistryObject<Item> NETHERITE_LOCKPICK_BLADE = ITEMS.register("netherite_lockpick_blade",
            () -> new Item(new Item.Properties().stacksTo(1)));
}