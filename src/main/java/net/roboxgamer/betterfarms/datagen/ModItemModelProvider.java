package net.roboxgamer.betterfarms.datagen;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.roboxgamer.betterfarms.BetterFarms;

public class ModItemModelProvider extends ItemModelProvider {
  public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
    super(output, BetterFarms.MODID, existingFileHelper);
  }

  @Override
  protected void registerModels() {

  }
}