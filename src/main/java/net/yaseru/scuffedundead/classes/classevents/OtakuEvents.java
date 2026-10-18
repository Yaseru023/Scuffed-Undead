package net.yaseru.scuffedundead.classes.classevents;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
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

@Mod.EventBusSubscriber(modid = ScuffedUndead.MOD_ID)
public class OtakuEvents {

    // ---- Bucket List
    private static final int STREAK_WINDOW_TICKS = 100; // 5s to land the next kill
    private static final int MAX_STACKS = 3;            // 3 stacks = Speed III

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

    private record Streak(int count, long lastKill) {}

    private static final Map<UUID, Streak> streaks = new HashMap<>();
    private static final Map<UUID, Long> lastAttack = new HashMap<>();

    // ---------------------------------------------------------------- Bucket List
    @SubscribeEvent
    public static void onKill(LivingDeathEvent event) {
        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player) || source.getDirectEntity() != player) return; // melee only
        if (ClassData.get(player).orElse(null) != PlayerClass.OTAKU) return;
        if (!(event.getEntity() instanceof Enemy)) return;

        long now = player.level().getGameTime();
        Streak prev = streaks.get(player.getUUID());
        int count = (prev != null && now - prev.lastKill() <= STREAK_WINDOW_TICKS) ? prev.count() + 1 : 1;
        count = Math.min(count, MAX_STACKS);
        streaks.put(player.getUUID(), new Streak(count, now));

        // visible=false hides particles, showIcon=true so the player can see the buff
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, STREAK_WINDOW_TICKS, count - 1, false, false, true));
        player.displayClientMessage(Component.literal("§6Bucket List x" + count), true);
    }

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
        UUID id = event.getEntity().getUUID();
        streaks.remove(id);
        lastAttack.remove(id);
    }
}