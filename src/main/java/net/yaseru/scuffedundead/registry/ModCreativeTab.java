package net.yaseru.scuffedundead.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.yaseru.scuffedundead.ScuffedUndead;

public class ModCreativeTab {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ScuffedUndead.MOD_ID);

    public static final RegistryObject<CreativeModeTab> SCUFFED_TAB = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.scuffedundead.main"))
                    .icon(() -> new ItemStack(ModItems.IRON_LOCKPICK.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.IRON_LOCKPICK.get());
                        output.accept(ModItems.IRON_LOCKPICK_BLADE.get());
                        output.accept(ModItems.STEEL_LOCKPICK.get());
                        output.accept(ModItems.STEEL_LOCKPICK_BLADE.get());
                        output.accept(ModItems.DIAMOND_LOCKPICK.get());
                        output.accept(ModItems.DIAMOND_LOCKPICK_BLADE.get());
                        output.accept(ModItems.NETHERITE_LOCKPICK.get());
                        output.accept(ModItems.NETHERITE_LOCKPICK_BLADE.get());
                    })
                    .build());
}