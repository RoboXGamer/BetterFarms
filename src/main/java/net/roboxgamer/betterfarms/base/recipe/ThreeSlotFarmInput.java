package net.roboxgamer.betterfarms.base.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.NotNull;

/**
 * A RecipeInput implementation representing the three core inputs
 * for certain farm types (e.g., Crop Farm, Tree Farm).
 * Slot 0: Tool
 * Slot 1: Primary Ingredient
 * Slot 2: Secondary/Base Ingredient
 */
public record ThreeSlotFarmInput(
    @NotNull ItemStack tool,
    @NotNull ItemStack primary,
    @NotNull ItemStack secondary
) implements RecipeInput {
  
  /**
   * Gets the item at the specified index.
   * 0 = Tool, 1 = Primary, 2 = Secondary.
   *
   * @param index The slot index (0, 1, or 2).
   * @return The ItemStack in the specified slot.
   * @throws IllegalArgumentException if the index is out of bounds (not 0, 1, or 2).
   */
  @Override
  public @NotNull ItemStack getItem(int index) {
    return switch (index) {
      case 0 -> this.tool;    // Tool input
      case 1 -> this.primary;  // Primary ingredient input
      case 2 -> this.secondary; // Secondary/Base input
      default -> throw new IllegalArgumentException("Invalid index " + index + " for ThreeSlotFarmInput. Must be 0, 1, or 2.");
    };
  }
  
  /**
   * Returns the number of input slots represented by this record.
   *
   * @return Always 3 for ThreeSlotFarmInput.
   */
  @Override
  public int size() {
    return 3;
  }
}
