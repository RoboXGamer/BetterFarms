package net.roboxgamer.betterfarms.CropFarmDir;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.NotNull;

public record CropFarmRecipeInput(ItemStack toolInput, ItemStack ingredientInput, ItemStack baseInput) implements RecipeInput {
  @Override
  public @NotNull ItemStack getItem(int i) {
    return switch (i) {
      case 0 -> this.toolInput;
      case 1 -> this.ingredientInput;
      case 2 -> this.baseInput;
      default -> throw new IllegalArgumentException("Recipe does not contain slot " + i);
    };
  }
  
  @Override
  public int size() {
    return 3;
  }
  
  public ItemStack toolInput() {
    return this.toolInput;
  }
  
  public ItemStack ingredientInput() {
    return this.ingredientInput;
  }
  
  public ItemStack baseInput() {
    return this.baseInput;
  }
}
