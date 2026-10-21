package net.yaseru.scuffedundead.classes.classevents;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.yaseru.scuffedundead.ScuffedUndead;
import net.yaseru.scuffedundead.classes.ClassData;
import net.yaseru.scuffedundead.classes.PlayerClass;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Otaku passives (melee only, hostile mobs only):
 *  - Opening Strike: the first hit after a pause deals bonus damage.
 *  - Katana Mastery: extra damage on every melee hit while holding a listed blade.
 */
@Mod.EventBusSubscriber(modid = ScuffedUndead.MOD_ID)
public class OtakuEvents {

    // ---- Opening Strike
    private static final int IDLE_TICKS = 60;           // 3s without attacking
    private static final float OPENING_MULTIPLIER = 1.5f; // +50% damage

    // ---- Katana Mastery
    private static final float KATANA_MULTIPLIER = 1.25f; // +25% damage
    private static final Set<ResourceLocation> KATANAS = Set.of(
            new ResourceLocation("nihon_blacksmithing", "claymore"),
            new ResourceLocation("nihon_blacksmithing", "katana"),
            new ResourceLocation("nihon_blacksmithing", "glaive"),
            new ResourceLocation("nihon_blacksmithing", "dagger"),
            new ResourceLocation("marbledsmelees", "katana")
    );

    private static final Map<UUID, Long> lastAttack = new HashMap<>();

    // ---------------------------------------------------------------- Katana Mastery + Opening Strike
    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player) || source.getDirectEntity() != player) return; // melee only
        if (ClassData.get(player).orElse(null) != PlayerClass.OTAKU) return;

        LivingEntity victim = event.getEntity();
        if (!(victim instanceof Enemy)) return;

        float amount = event.getAmount();

        // Katana Mastery: every hit with a katana
        if (isKatana(player.getMainHandItem())) amount *= KATANA_MULTIPLIER;

        // Opening Strike: first hit after a pause
        long now = player.level().getGameTime();
        Long last = lastAttack.put(player.getUUID(), now);
        if (last == null || now - last >= IDLE_TICKS) {
            amount *= OPENING_MULTIPLIER;

            if (victim.level() instanceof ServerLevel level) {
                level.sendParticles(ParticleTypes.CRIT, victim.getX(), victim.getY() + victim.getBbHeight() / 2, victim.getZ(),
                        8, 0.3, 0.3, 0.3, 0.1);
                level.playSound(null, victim.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 0.8f, 1.0f);
            }
        }

        event.setAmount(amount);
    }

    private static boolean isKatana(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id != null && KATANAS.contains(id);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        lastAttack.remove(event.getEntity().getUUID());
    }
}