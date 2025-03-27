package net.roboxgamer.betterfarms.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.roboxgamer.betterfarms.BetterFarms;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends ItemTagsProvider {
  public ModItemTagProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pLookupProvider,
                            CompletableFuture<TagLookup<Block>> pBlockTags, @Nullable ExistingFileHelper existingFileHelper) {
    super(pOutput, pLookupProvider, pBlockTags, BetterFarms.MODID, existingFileHelper);
  }

  @Override
  protected void addTags(HolderLookup.Provider pProvider) {
//    ModTags.Items.
  }
}
