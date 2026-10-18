package net.yaseru.scuffedundead.classes.classevents;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.yaseru.scuffedundead.ScuffedUndead;
import net.yaseru.scuffedundead.classes.ClassData;
import net.yaseru.scuffedundead.classes.PlayerClass;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = ScuffedUndead.MOD_ID)
public class VeteranEvents {

    private static final float SAVE_CHANCE = 0.20f;
    private static final int MAX_SHOTS_PER_TICK = 3; // a bigger drop is something else (unloading), not shooting

    private static final String GUN_ID_KEY = "GunId";
    private static final String AMMO_KEY = "GunCurrentAmmoCount";
    private static final String BARREL_KEY = "HasBulletInBarrel";

    private record Snapshot(int slot, String gunId, int ammo, boolean barrel) {
        int total() { return ammo + (barrel ? 1 : 0); }
    }

    private static final Map<UUID, Snapshot> last = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        UUID id = player.getUUID();

        if (ClassData.get(player).orElse(null) != PlayerClass.VETERAN) {
            last.remove(id);
            return;
        }

        ItemStack held = player.getMainHandItem();
        CompoundTag tag = held.getTag();
        if (tag == null || !tag.contains(GUN_ID_KEY)) { // not a TaCZ gun
            last.remove(id);
            return;
        }

        int slot = player.getInventory().selected;
        Snapshot now = new Snapshot(slot, tag.getString(GUN_ID_KEY), tag.getInt(AMMO_KEY), tag.getBoolean(BARREL_KEY));
        Snapshot prev = last.get(id);

        if (prev != null && prev.slot() == now.slot() && prev.gunId().equals(now.gunId())) {
            int fired = prev.total() - now.total();
            if (fired > 0 && fired <= MAX_SHOTS_PER_TICK) {
                int refund = 0;
                for (int i = 0; i < fired; i++) {
                    if (player.getRandom().nextFloat() < SAVE_CHANCE) refund++;
                }

                if (refund > 0) {
                    int ammo = now.ammo();
                    boolean barrel = now.barrel();
                    // if the shot used the chambered round and the magazine was empty, re-chamber first
                    if (!barrel && prev.barrel() && refund > 0) {
                        barrel = true;
                        refund--;
                    }
                    ammo += refund;

                    tag.putBoolean(BARREL_KEY, barrel);
                    tag.putInt(AMMO_KEY, ammo);
                    now = new Snapshot(slot, now.gunId(), ammo, barrel);
                }
            }
        }
        last.put(id, now);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        last.remove(event.getEntity().getUUID());
    }
}