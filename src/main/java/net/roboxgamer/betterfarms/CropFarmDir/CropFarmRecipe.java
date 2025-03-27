package net.roboxgamer.betterfarms.CropFarmDir;

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
import net.roboxgamer.betterfarms.util.ChanceResult;
import net.roboxgamer.betterfarms.ModRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public record CropFarmRecipe(
    Ingredient toolInput,
    Ingredient ingredientInput,
    Ingredient baseInput,
    List<ChanceResult> outputItems
) implements Recipe<CropFarmRecipeInput> {
  
  @Override
  public boolean matches(@NotNull CropFarmRecipeInput cropFarmRecipeInput, @NotNull Level level) {
    if (level.isClientSide()) return false;
    return this.toolInput.test(cropFarmRecipeInput.toolInput())
        && this.ingredientInput.test(cropFarmRecipeInput.ingredientInput())
        && this.baseInput.test(cropFarmRecipeInput.baseInput());
  }
  
  @Override
  public @NotNull NonNullList<Ingredient> getIngredients() {
    NonNullList<Ingredient> list = NonNullList.create();
    list.add(this.toolInput);
    list.add(this.ingredientInput);
    list.add(this.baseInput);
    return list;
  }
  
  @Override
  public @NotNull ItemStack assemble(@NotNull CropFarmRecipeInput cropFarmRecipeInput, HolderLookup.@NotNull Provider provider) {
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
    return ModRegistry.CROP_FARM_SERIALIZER.get();
  }
  
  @Override
  public @NotNull RecipeType<?> getType() {
    return ModRegistry.CROP_FARM_TYPE.get();
  }
  
  public static class Serializer implements RecipeSerializer<CropFarmRecipe> {
    public static final MapCodec<CropFarmRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
        Ingredient.CODEC_NONEMPTY.fieldOf("tool").forGetter(CropFarmRecipe::toolInput),
        Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(CropFarmRecipe::ingredientInput),
        Ingredient.CODEC_NONEMPTY.fieldOf("base").forGetter(CropFarmRecipe::baseInput),
        Codec.list(ChanceResult.CODEC).fieldOf("output").forGetter(CropFarmRecipe::outputItems)
    ).apply(inst, CropFarmRecipe::new));
    
    public static final StreamCodec<RegistryFriendlyByteBuf, CropFarmRecipe> STREAM_CODEC =
        StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, CropFarmRecipe::toolInput,
            Ingredient.CONTENTS_STREAM_CODEC, CropFarmRecipe::ingredientInput,
            Ingredient.CONTENTS_STREAM_CODEC, CropFarmRecipe::baseInput,
            ChanceResult.STREAM_CODEC.apply(ByteBufCodecs.list()), CropFarmRecipe::outputItems,
            CropFarmRecipe::new);
    
    @Override
    public @NotNull MapCodec<CropFarmRecipe> codec() {
      return CODEC;
    }
    
    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, CropFarmRecipe> streamCodec() {
      return STREAM_CODEC;
    }
  }
}
