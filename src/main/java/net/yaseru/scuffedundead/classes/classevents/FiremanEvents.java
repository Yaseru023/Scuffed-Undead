package net.yaseru.scuffedundead.classes.classevents;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.yaseru.scuffedundead.ScuffedUndead;
import net.yaseru.scuffedundead.classes.ClassData;
import net.yaseru.scuffedundead.classes.PlayerClass;

/** Fireman passive ("Fireproof"): takes much less damage from fire and explosions. */
@Mod.EventBusSubscriber(modid = ScuffedUndead.MOD_ID)
public class FiremanEvents {

    private static final float FIRE_REDUCTION = 0.60f;      // 60% less fire/lava damage
    private static final float EXPLOSION_REDUCTION = 0.30f; // 30% less explosion damage

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (ClassData.get(player).orElse(null) != PlayerClass.FIREMAN) return;

        DamageSource source = event.getSource();
        float reduction = 0f;
        if (source.is(DamageTypeTags.IS_FIRE)) reduction = FIRE_REDUCTION;           // fire, burning, lava, fireballs
        else if (source.is(DamageTypeTags.IS_EXPLOSION)) reduction = EXPLOSION_REDUCTION;

        if (reduction > 0f) {
            event.setAmount(event.getAmount() * (1f - reduction));
        }
    }
}