package net.yaseru.scuffedundead;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.yaseru.scuffedundead.registry.ModCreativeTab;
import net.yaseru.scuffedundead.registry.ModItems;
import net.yaseru.scuffedundead.registry.ModNetwork;
import net.yaseru.scuffedundead.registry.ModRecipeTypes;

@Mod(ScuffedUndead.MOD_ID)
public class ScuffedUndead {
    public static final String MOD_ID = "scuffedundead";

    public ScuffedUndead() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModRecipeTypes.SERIALIZERS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTab.TABS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetwork::register);
    }
}
