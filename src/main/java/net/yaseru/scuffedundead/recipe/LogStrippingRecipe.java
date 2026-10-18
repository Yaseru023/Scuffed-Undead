package net.yaseru.scuffedundead.recipe;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.yaseru.scuffedundead.registry.ModRecipeTypes;

public class LogStrippingRecipe implements CraftingRecipe {

    private final ResourceLocation id;
    private final Ingredient axeIngredient;
    private final Ingredient logIngredient;
    private final ItemStack result;

    public LogStrippingRecipe(ResourceLocation id, Ingredient axeIngredient, Ingredient logIngredient, ItemStack result) {
        this.id = id;
        this.axeIngredient = axeIngredient;
        this.logIngredient = logIngredient;
        this.result = result;
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        int width = container.getWidth();
        int height = container.getHeight();

        for (int col = 0; col < width; col++) {
            for (int axeRow = 0; axeRow < height - 1; axeRow++) {
                int logRow = axeRow + 1;

                ItemStack axeStack = container.getItem(axeRow * width + col);
                ItemStack logStack = container.getItem(logRow * width + col);

                if (!axeIngredient.test(axeStack)) continue;
                if (!logIngredient.test(logStack)) continue;

                // Make sure all other slots are empty
                boolean valid = true;
                for (int slot = 0; slot < container.getContainerSize(); slot++) {
                    int slotCol = slot % width;
                    int slotRow = slot / width;
                    if (slotCol == col && (slotRow == axeRow || slotRow == logRow)) continue;
                    if (!container.getItem(slot).isEmpty()) {
                        valid = false;
                        break;
                    }
                }

                if (valid) return true;
            }
        }
        return false;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, net.minecraft.core.RegistryAccess registryAccess) {
        return result.copy();
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }

    /**
     * Called after crafting to damage the axe by 1 durability.
     * We override getRemainingItems to return the axe with 1 durability removed.
     */
    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);
        int width = container.getWidth();
        int height = container.getHeight();

        for (int col = 0; col < width; col++) {
            for (int axeRow = 0; axeRow < height - 1; axeRow++) {
                int logRow = axeRow + 1;

                ItemStack axeStack = container.getItem(axeRow * width + col);
                ItemStack logStack = container.getItem(logRow * width + col);

                if (!axeIngredient.test(axeStack)) continue;
                if (!logIngredient.test(logStack)) continue;

                // Return a copy of the axe with 1 durability removed
                ItemStack damagedAxe = axeStack.copy();
                damagedAxe.setCount(1);
                damagedAxe.setDamageValue(damagedAxe.getDamageValue() + 1);

                // If the axe is broken after damage, return empty
                if (damagedAxe.getDamageValue() >= damagedAxe.getMaxDamage()) {
                    remaining.set(axeRow * width + col, ItemStack.EMPTY);
                } else {
                    remaining.set(axeRow * width + col, damagedAxe);
                }
                return remaining;
            }
        }
        return remaining;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        // Needs at least 1 column and 2 rows (axe above log)
        return width >= 1 && height >= 2;
    }

    @Override
    public ItemStack getResultItem(net.minecraft.core.RegistryAccess registryAccess) {
        return result.copy();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.LOG_STRIPPING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeType.CRAFTING;
    }

    public Ingredient getAxeIngredient() {
        return axeIngredient;
    }

    public Ingredient getLogIngredient() {
        return logIngredient;
    }
}