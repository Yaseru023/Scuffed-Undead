package net.yaseru.scuffedundead.classes.classevents;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.yaseru.scuffedundead.ScuffedUndead;
import net.yaseru.scuffedundead.classes.ClassData;
import net.yaseru.scuffedundead.classes.PlayerClass;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * LARPer passives:
 *  - Iron Will: the more armor you wear, the less damage you take from attacks.
 *  - Medieval Might: holding a weapon from the tkvm_weapons mod in your main hand gives Resistance I.
 */
@Mod.EventBusSubscriber(modid = ScuffedUndead.MOD_ID)
public class LarperEvents {

    // ---- Iron Will
    private static final float REDUCTION_PER_ARMOR_POINT = 0.01f; // 1% less damage per armor point
    private static final float MAX_REDUCTION = 0.25f;             // never more than 25% less

    // ---- Medieval Might
    private static final String WEAPON_NAMESPACE = "tkvm_weapons"; // any item from this mod counts...
    // ...except these (crafting parts and the shield), matched by item name without the mod prefix
    private static final Set<String> NOT_WEAPONS = Set.of(
            "pole",
            "handle",
            "leather_strip",
            "starite_upgrade_smithing_template",
            "roundshield"
    );
    private static final int EFFECT_LEVEL = 0;                   // 0 = Resistance I
    private static final int GRANT_TICKS = 30;                     // short on purpose: it fades quickly after switching

    private static final Set<UUID> granted = new HashSet<>();

    // ---------------------------------------------------------------- Iron Will
    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (ClassData.get(player).orElse(null) != PlayerClass.LARPER) return;

        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof LivingEntity)) return; // only attacks, not fall damage, fire, etc.

        float reduction = Math.min(player.getArmorValue() * REDUCTION_PER_ARMOR_POINT, MAX_REDUCTION);
        if (reduction > 0f) {
            event.setAmount(event.getAmount() * (1f - reduction));
        }
    }

    // ---------------------------------------------------------------- Medieval Might
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % 5 != 0) return;

        UUID id = player.getUUID();
        boolean holding = ClassData.get(player).orElse(null) == PlayerClass.LARPER
                && isModWeapon(player.getMainHandItem());

        if (holding) {
            // showIcon=true so the player can see the buff; particles hidden
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, GRANT_TICKS, EFFECT_LEVEL, false, false, true));
            granted.add(id);
        } else if (granted.remove(id)) {
            // Stopped holding it: remove OUR short Resistance right away, but leave real potions alone.
            MobEffectInstance current = player.getEffect(MobEffects.DAMAGE_RESISTANCE);
            if (current != null && current.getAmplifier() == EFFECT_LEVEL && current.getDuration() <= GRANT_TICKS) {
                player.removeEffect(MobEffects.DAMAGE_RESISTANCE);
            }
        }
    }

    private static boolean isModWeapon(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id != null
                && id.getNamespace().equals(WEAPON_NAMESPACE)
                && !NOT_WEAPONS.contains(id.getPath());
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        granted.remove(event.getEntity().getUUID());
    }
}