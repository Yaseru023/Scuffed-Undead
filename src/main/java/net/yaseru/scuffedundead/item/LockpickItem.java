package net.yaseru.scuffedundead.item;

import net.minecraft.world.item.Item;

/**
 * Marker class for lockpicks. LockpickEvents wears one down (1 durability) each time it
 * successfully opens a locked container. The durability itself is set where the item is
 * registered, in ModItems.
 */
public class LockpickItem extends Item {
    public LockpickItem(Properties properties) {
        super(properties);
    }
}