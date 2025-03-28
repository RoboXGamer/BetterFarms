package net.roboxgamer.betterfarms.base.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.NotNull;

/**
 * A RecipeInput implementation representing the two core inputs
 * for certain farm types (e.g., Stone Farm, Ore Farm, Mob Farm).
 * Slot 0: Tool
 * Slot 1: Primary Ingredient
 */
public record TwoSlotFarmInput(
    @NotNull ItemStack tool,
    @NotNull ItemStack primary
) implements RecipeInput {
  
  /**
   * Gets the item at the specified index.
   * 0 = Tool, 1 = Primary.
   *
   * @param index The slot index (0 or 1).
   * @return The ItemStack in the specified slot.
   * @throws IllegalArgumentException if the index is out of bounds (not 0 or 1).
   */
  @Override
  public @NotNull ItemStack getItem(int index) {
    return switch (index) {
      case 0 -> this.tool;    // Tool input
      case 1 -> this.primary;  // Primary ingredient input
      default -> throw new IllegalArgumentException("Invalid index " + index + " for TwoSlotFarmInput. Must be 0 or 1.");
    };
  }
  
  /**
   * Returns the number of input slots represented by this record.
   *
   * @return Always 2 for TwoSlotFarmInput.
   */
  @Override
  public int size() {
    return 2;
  }
}
