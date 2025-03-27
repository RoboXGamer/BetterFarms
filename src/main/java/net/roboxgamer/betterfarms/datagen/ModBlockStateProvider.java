package net.roboxgamer.betterfarms.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.roboxgamer.betterfarms.BetterFarms;
import net.roboxgamer.betterfarms.ModRegistry;

public class ModBlockStateProvider extends BlockStateProvider {
  public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
    super(output, BetterFarms.MODID, exFileHelper);
  }
  @Override
  protected void registerStatesAndModels() {
    blockWithItem(ModRegistry.CROP_FARM_BLOCK);
  }
  private <T extends Block> void blockWithItem(DeferredBlock<T> block) {
    simpleBlockWithItem(block.get(), cubeAll(block.get()));
  }
  
  private <T extends Block> void blockWithItemWithModel(DeferredBlock<T> block, ModelFile model) {
    simpleBlockWithItem(block.get(), model);
  }
  
  
  // This is an automatic method to register all simple blocks with item that are in ModBlocks.BLOCKS
  //@Override
  //protected void registerStatesAndModels() {
  //  ModBlocks.BLOCKS.getEntries().forEach(block -> simpleBlockWithItem(block.get(), cubeAll(block.get())));
  //}
  
}
