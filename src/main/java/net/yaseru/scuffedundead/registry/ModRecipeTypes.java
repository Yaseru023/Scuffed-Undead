package net.yaseru.scuffedundead.registry;

import net.yaseru.scuffedundead.ScuffedUndead;
import net.yaseru.scuffedundead.recipe.LogStrippingRecipeSerializer;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipeTypes {

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, ScuffedUndead.MOD_ID);

    public static final RegistryObject<net.yaseru.scuffedundead.recipe.LogStrippingRecipeSerializer> LOG_STRIPPING_SERIALIZER =
            SERIALIZERS.register("log_stripping", net.yaseru.scuffedundead.recipe.LogStrippingRecipeSerializer::new);
}
