package net.roboxgamer.betterfarms.datagen.recipe;

import net.minecraft.advancements.*;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.roboxgamer.betterfarms.BetterFarms;
import net.roboxgamer.betterfarms.CropFarmDir.CropFarmRecipe;
import net.roboxgamer.betterfarms.util.ChanceResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CropFarmRecipeBuilder implements RecipeBuilder {
  
  private final RecipeCategory category;
  private final Ingredient toolInput;
  private final Ingredient ingredientInput;
  private final Ingredient baseInput;
  private final List<ChanceResult> outputItems = new ArrayList<>();
  private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
  
  // Private constructor, use static factory methods
  private CropFarmRecipeBuilder(RecipeCategory category, Ingredient toolInput, Ingredient ingredientInput, Ingredient baseInput, ChanceResult firstOutput) {
    this.category = category;
    this.toolInput = toolInput;
    this.ingredientInput = ingredientInput;
    this.baseInput = baseInput;
    this.outputItems.add(firstOutput); // Add the mandatory first output
  }
  @NotNull
  private String group;
  // --- Static Factory Methods ---
  
  public static CropFarmRecipeBuilder create(RecipeCategory category, Ingredient toolInput, Ingredient ingredientInput, Ingredient baseInput, ChanceResult firstOutput) {
    return new CropFarmRecipeBuilder(category, toolInput, ingredientInput, baseInput, firstOutput);
  }
  
  // Convenience factory methods using ItemLike/Tags if desired
  public static CropFarmRecipeBuilder create(RecipeCategory category, ItemLike toolInput, Ingredient ingredientInput, ItemLike baseInput, ChanceResult firstOutput) {
    return create(category, Ingredient.of(toolInput), ingredientInput, Ingredient.of(baseInput), firstOutput);
  }
  
  public static CropFarmRecipeBuilder create(RecipeCategory category, TagKey<Item> toolInput, Ingredient ingredientInput, TagKey<Item> baseInput, ChanceResult firstOutput) {
    return create(category, Ingredient.of(toolInput), ingredientInput, Ingredient.of(baseInput), firstOutput);
  }
  
  public static CropFarmRecipeBuilder create(RecipeCategory category, TagKey<Item> toolInput, Ingredient ingredientInput, Ingredient baseInput,ChanceResult firstOutput) {
    return create(category, Ingredient.of(toolInput), ingredientInput, baseInput, firstOutput);
  }
  
  
  // --- Builder Methods ---
  
  public CropFarmRecipeBuilder addOutput(ChanceResult output) {
    this.outputItems.add(output);
    return this;
  }
  
  // Convenience method for adding outputs
  public CropFarmRecipeBuilder addOutput(ItemLike itemProvider, int count, float chance) {
    return addOutput(new ChanceResult(new ItemStack(itemProvider, count), chance));
  }
  
  public CropFarmRecipeBuilder addOutput(ItemLike itemProvider, float chance) {
    return addOutput(itemProvider, 1, chance);
  }
  
  // --- Implementing RecipeBuilder ---
  
  @Override
  public @NotNull CropFarmRecipeBuilder unlockedBy(@NotNull String name, @NotNull Criterion<?> criterion) {
    this.criteria.put(name, criterion);
    return this;
  }
  
  @Override
  @NotNull
  public CropFarmRecipeBuilder group(@Nullable String groupName) {
    this.group = groupName;
    return this;
  }
  
  @Override
  @NotNull
  public Item getResult() {
    // The primary result is considered the item from the first output entry
    if (this.outputItems.isEmpty()) {
      throw new IllegalStateException("Cannot get result from a recipe builder with no outputs");
    }
    return this.outputItems.getFirst().stack().getItem();
  }
  
  @Override
  public void save(@NotNull RecipeOutput recipeOutput, @NotNull ResourceLocation id) {
    if (this.outputItems.isEmpty()) {
      throw new IllegalStateException("Cannot save recipe " + id + " with no outputs defined.");
    }
    Advancement.Builder advancementBuilder = recipeOutput.advancement()
        .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
        .rewards(AdvancementRewards.Builder.recipe(id))
        .requirements(AdvancementRequirements.Strategy.OR);
    
    this.criteria.forEach(advancementBuilder::addCriterion);
    
    CropFarmRecipe recipe = new CropFarmRecipe(
        this.toolInput,
        this.ingredientInput,
        this.baseInput,
        List.copyOf(this.outputItems)
    );
    
    AdvancementHolder advancementHolder = advancementBuilder.build(
        id.withPrefix("recipes/" + this.category.getFolderName() + "/"));
    recipeOutput.accept(BetterFarms.location(id.getPath()), recipe, advancementHolder);
  }
}
