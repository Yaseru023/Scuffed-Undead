package net.yaseru.scuffedundead.classes.classevents;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.yaseru.scuffedundead.ScuffedUndead;
import net.yaseru.scuffedundead.classes.ClassData;
import net.yaseru.scuffedundead.classes.PlayerClass;

import java.util.ArrayList;
import java.util.List;

/**
 * Locksmith passives:
 *  - Light Fingers: handled in item/LockpickEvents (a lockpick may not wear down).
 *  - Scavenge: killing a hostile mob has a chance to drop one extra item.
 */
@Mod.EventBusSubscriber(modid = ScuffedUndead.MOD_ID)
public class LocksmithEvents {

    private static final float SCAVENGE_CHANCE = 0.20f; // 1.0f to test

    @SubscribeEvent
    public static void onDrops(LivingDropsEvent event) {
        // getEntity() on the damage source is the attacker (the shooter for bullets), so guns count too
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (ClassData.get(player).orElse(null) != PlayerClass.LOCKSMITH) return;

        LivingEntity victim = event.getEntity();
        if (!(victim instanceof Enemy) || event.getDrops().isEmpty()) return;
        if (player.getRandom().nextFloat() >= SCAVENGE_CHANCE) return;

        // duplicate ONE item from a random drop
        List<ItemEntity> drops = new ArrayList<>(event.getDrops());
        ItemEntity picked = drops.get(player.getRandom().nextInt(drops.size()));
        ItemStack copy = picked.getItem().copy();
        copy.setCount(1);

        ItemEntity extra = new ItemEntity(victim.level(), victim.getX(), victim.getY(), victim.getZ(), copy);
        extra.setDefaultPickUpDelay();
        event.getDrops().add(extra);

        player.displayClientMessage(Component.literal("§bScavenged!"), true);
    }
}