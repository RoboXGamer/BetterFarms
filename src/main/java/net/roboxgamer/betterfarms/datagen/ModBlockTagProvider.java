package net.roboxgamer.betterfarms.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.roboxgamer.betterfarms.BetterFarms;
import net.roboxgamer.betterfarms.ModRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends BlockTagsProvider {
  public ModBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
    super(output, lookupProvider, BetterFarms.MODID, existingFileHelper);
  }

  @Override
  protected void addTags(HolderLookup.@NotNull Provider pProvider) {
    this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
        .add(ModRegistry.CROP_FARM_BLOCK.get())
        .add(ModRegistry.TREE_FARM_BLOCK.get());
        
  }
}
