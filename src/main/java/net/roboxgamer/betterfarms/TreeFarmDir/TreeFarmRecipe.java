package net.roboxgamer.betterfarms.TreeFarmDir;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.roboxgamer.betterfarms.ModRegistry;
import net.roboxgamer.betterfarms.base.AbstractFarmBlockEntity;
import net.roboxgamer.betterfarms.base.recipe.ThreeSlotFarmInput;
import net.roboxgamer.betterfarms.util.ChanceResult;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public record TreeFarmRecipe(
    Ingredient toolInput,
    Ingredient primaryInput,
    Ingredient secondaryInput,
    List<ChanceResult> outputItems
) implements Recipe<ThreeSlotFarmInput>, AbstractFarmBlockEntity.ICraftingFarmRecipe<ThreeSlotFarmInput> {
  
  @Override
  public boolean matches(@NotNull ThreeSlotFarmInput recipeInput, @NotNull Level level) {
    if (level.isClientSide()) return false;
    return this.toolInput.test(recipeInput.tool())
        && this.primaryInput.test(recipeInput.primary())
        && this.secondaryInput.test(recipeInput.secondary());
  }
  
  @Override
  public @NotNull NonNullList<Ingredient> getIngredients() {
    NonNullList<Ingredient> list = NonNullList.create();
    list.add(this.toolInput);
    list.add(this.primaryInput);
    list.add(this.secondaryInput);
    return list;
  }
  
  @Override
  public @NotNull ItemStack assemble(@NotNull ThreeSlotFarmInput recipeInput, HolderLookup.@NotNull Provider provider) {
    return this.getResultItem(provider);
  }
  
  @Override
  public boolean canCraftInDimensions(int i, int i1) {
    return true;
  }
  
  @Override
  public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider provider) {
    return outputItems.getFirst().stack().copy();
  }
  
  public List<ItemStack> getResults() {
    return getRollResults().stream()
        .map(ChanceResult::stack)
        .collect(Collectors.toList());
  }
  
  public List<ChanceResult> getRollResults() {
    return this.outputItems;
  }
  
  public List<ItemStack> rollResults(RandomSource rand) {
    List<ItemStack> results = new ArrayList<>();
    List<ChanceResult> rollResults = getRollResults();
    for (ChanceResult output : rollResults) {
      ItemStack stack = output.rollOutput(rand);
      if (!stack.isEmpty())
        results.add(stack);
    }
    return results;
  }
  
  @Override
  public @NotNull RecipeSerializer<?> getSerializer() {
    return ModRegistry.TREE_FARM_SERIALIZER.get();
  }
  
  @Override
  public @NotNull RecipeType<?> getType() {
    return ModRegistry.TREE_FARM_TYPE.get();
  }
  
  public static class Serializer implements RecipeSerializer<TreeFarmRecipe> {
    public static final MapCodec<TreeFarmRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
        Ingredient.CODEC_NONEMPTY.fieldOf("tool").forGetter(TreeFarmRecipe::toolInput),
        Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(TreeFarmRecipe::primaryInput),
        Ingredient.CODEC_NONEMPTY.fieldOf("base").forGetter(TreeFarmRecipe::secondaryInput),
        Codec.list(ChanceResult.CODEC).fieldOf("output").forGetter(TreeFarmRecipe::outputItems)
    ).apply(inst, TreeFarmRecipe::new));
    
    public static final StreamCodec<RegistryFriendlyByteBuf, TreeFarmRecipe> STREAM_CODEC =
        StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, TreeFarmRecipe::toolInput,
            Ingredient.CONTENTS_STREAM_CODEC, TreeFarmRecipe::primaryInput,
            Ingredient.CONTENTS_STREAM_CODEC, TreeFarmRecipe::secondaryInput,
            ChanceResult.STREAM_CODEC.apply(ByteBufCodecs.list()), TreeFarmRecipe::outputItems,
            TreeFarmRecipe::new);
    
    @Override
    public @NotNull MapCodec<TreeFarmRecipe> codec() {
      return CODEC;
    }
    
    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, TreeFarmRecipe> streamCodec() {
      return STREAM_CODEC;
    }
  }
}
