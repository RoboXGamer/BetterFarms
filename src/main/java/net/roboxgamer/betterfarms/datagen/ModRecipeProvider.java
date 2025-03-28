package net.roboxgamer.betterfarms.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import net.roboxgamer.betterfarms.BetterFarms;
import net.roboxgamer.betterfarms.datagen.recipe.CropFarmRecipeBuilder;
import net.roboxgamer.betterfarms.datagen.recipe.TreeFarmRecipeBuilder;
import net.roboxgamer.betterfarms.util.ChanceResult;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
  public ModRecipeProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pRegistries) {
    super(pOutput, pRegistries);
  }
  
  @Override
  protected void buildRecipes(@NotNull RecipeOutput recipeOutput) {
    // --- Tree Farm Recipes ---
    // Map: Sapling Item -> Log Item
    Map<Item, Item> treeRecipes = new LinkedHashMap<>();
    treeRecipes.put(Items.OAK_SAPLING, Items.OAK_LOG);
    treeRecipes.put(Items.SPRUCE_SAPLING, Items.SPRUCE_LOG);
    treeRecipes.put(Items.BIRCH_SAPLING, Items.BIRCH_LOG);
    treeRecipes.put(Items.JUNGLE_SAPLING, Items.JUNGLE_LOG);
    treeRecipes.put(Items.ACACIA_SAPLING, Items.ACACIA_LOG);
    treeRecipes.put(Items.DARK_OAK_SAPLING, Items.DARK_OAK_LOG);
    // Add other saplings like CHERRY_SAPLING -> CHERRY_LOG, MANGROVE_PROPAGULE -> MANGROVE_LOG etc. if TreeFarmRecipeBuilder exists
    
    treeRecipes.forEach((sapling, log) -> {
      TreeFarmRecipeBuilder builder = TreeFarmRecipeBuilder.create(
          RecipeCategory.MISC,
          ItemTags.AXES,
          Ingredient.of(sapling),
          Ingredient.of(Items.DIRT),
          new ChanceResult(new ItemStack(log), 1f)
      );
      
      builder.addOutput(new ChanceResult(new ItemStack(sapling), 0.5f));
      
      if (sapling == Items.OAK_SAPLING) {
        builder.addOutput(new ChanceResult(new ItemStack(Items.APPLE), 0.05f));
      }
      builder.save(recipeOutput, BetterFarms.location(getItemName(log) + "_from_tree_farm"));
    });
    
    
    // --- Crop Farm Recipes ---
    
    // Map: Seed Item -> Crop Item
    Map<Item, Item> seedCropRecipes = new LinkedHashMap<>();
    seedCropRecipes.put(Items.WHEAT_SEEDS, Items.WHEAT);
    seedCropRecipes.put(Items.BEETROOT_SEEDS, Items.BEETROOT);
    seedCropRecipes.put(Items.PUMPKIN_SEEDS, Items.PUMPKIN);
    seedCropRecipes.put(Items.MELON_SEEDS, Items.MELON_SLICE);
    
    // List: Crop Item that acts as its own seed
    List<Item> selfPlantingCrops = List.of(
        Items.CARROT,
        Items.POTATO
    );
    
    // Process Seed -> Crop recipes
    seedCropRecipes.forEach((seed, crop) -> {
      CropFarmRecipeBuilder.create(
              RecipeCategory.MISC,
              ItemTags.HOES,
              Ingredient.of(seed),
              Ingredient.of(Items.DIRT),
              new ChanceResult(new ItemStack(crop), 1f)
          )
          .addOutput(new ChanceResult(new ItemStack(seed), 0.75f))
          .save(recipeOutput, BetterFarms.location(getItemName(crop) + "_from_crop_farm"));
    });
    
    // Process Crop -> Crop recipes
    selfPlantingCrops.forEach(crop -> {
      CropFarmRecipeBuilder.create(
              RecipeCategory.MISC,
              ItemTags.HOES,
              Ingredient.of(crop),
              Ingredient.of(Items.DIRT),
              new ChanceResult(new ItemStack(crop), 1.2f)
          )
          .save(recipeOutput, BetterFarms.location(getItemName(crop) + "_from_crop_farm"));
    });
    
    // --- Special Crop Farm Cases ---
    
    // Nether Wart
    CropFarmRecipeBuilder.create(
            RecipeCategory.MISC,
            ItemTags.HOES,
            Ingredient.of(Items.NETHER_WART),
            Ingredient.of(Items.SOUL_SAND),
            new ChanceResult(new ItemStack(Items.NETHER_WART), 1.5f)
        )
        .save(recipeOutput, BetterFarms.location("nether_wart_from_crop_farm"));
    
    CropFarmRecipeBuilder.create(
            RecipeCategory.MISC,
            ItemTags.HOES,
            Ingredient.of(Items.SUGAR_CANE),
            Ingredient.of(Items.WATER_BUCKET),
            new ChanceResult(new ItemStack(Items.SUGAR_CANE), 1.5f)
        )
        .save(recipeOutput, BetterFarms.location("sugar_cane_from_crop_farm"));
  }
  
  protected static void oreSmelting(RecipeOutput pRecipeOutput, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTIme, String pGroup) {
    oreCooking(pRecipeOutput, RecipeSerializer.SMELTING_RECIPE, SmeltingRecipe::new, pIngredients, pCategory, pResult,
               pExperience, pCookingTIme, pGroup, "_from_smelting");
  }
  
  protected static void oreBlasting(RecipeOutput pRecipeOutput, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, String pGroup) {
    oreCooking(pRecipeOutput, RecipeSerializer.BLASTING_RECIPE, BlastingRecipe::new, pIngredients, pCategory, pResult,
               pExperience, pCookingTime, pGroup, "_from_blasting");
  }
  
  protected static <T extends AbstractCookingRecipe> void oreCooking(RecipeOutput pRecipeOutput, RecipeSerializer<T> pCookingSerializer, AbstractCookingRecipe.Factory<T> factory, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, String pGroup, String pRecipeName) {
    for (ItemLike itemlike : pIngredients) {
      SimpleCookingRecipeBuilder.generic(Ingredient.of(itemlike), pCategory, pResult, pExperience, pCookingTime,
                                         pCookingSerializer, factory).group(pGroup).unlockedBy(getHasName(itemlike),
                                                                                               has(itemlike)).save(
          pRecipeOutput, BetterFarms.MODID + ":" + getItemName(pResult) + pRecipeName + "_" + getItemName(itemlike));
    }
  }
}