package net.yaseru.scuffedundead.classes.classevents;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.yaseru.scuffedundead.ScuffedUndead;
import net.yaseru.scuffedundead.classes.ClassData;
import net.yaseru.scuffedundead.classes.PlayerClass;

/**
 * Police passive: anything hostile the Police Officer hurts is "tagged".
 * It glows (visible through walls to everyone) and is briefly slowed.
 */
@Mod.EventBusSubscriber(modid = ScuffedUndead.MOD_ID)
public class PoliceEvents {

    private static final int GLOW_TICKS = 100;      // 5 seconds
    private static final int SLOW_TICKS = 40;       // 2 seconds
    private static final int SLOW_LEVEL = 1;        // 0 = Slowness I, 1 = Slowness II
    private static final int COOLDOWN_TICKS = 100;  // per mob, so fast guns don't keep it slowed forever
    private static final String TAG_KEY = "scuffedundead_police_tag";

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide || !(victim instanceof Enemy)) return;

        // getEntity() is the attacker (the shooter for projectiles), not the bullet itself
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (ClassData.get(player).orElse(null) != PlayerClass.POLICE) return;

        long now = victim.level().getGameTime();
        CompoundTag data = victim.getPersistentData();
        if (now < data.getLong(TAG_KEY)) return; // still on cooldown
        data.putLong(TAG_KEY, now + COOLDOWN_TICKS);

        // ambient=false, visible=false, showIcon=false -> no particles or icon on the mob
        victim.addEffect(new MobEffectInstance(MobEffects.GLOWING, GLOW_TICKS, 0, false, false, false), player);
        victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOW_TICKS, SLOW_LEVEL, false, false, false), player);
    }
}