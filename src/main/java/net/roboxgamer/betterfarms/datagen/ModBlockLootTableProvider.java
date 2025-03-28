package net.roboxgamer.betterfarms.datagen;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.roboxgamer.betterfarms.ModRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ModBlockLootTableProvider extends BlockLootSubProvider {
  protected ModBlockLootTableProvider(HolderLookup.Provider provider) {
    super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
  }

  @Override
  protected void generate() {
    ModRegistry.BLOCKS.getEntries().forEach(
        holder -> dropSelf(holder.get())
    );
  }

  protected LootTable.Builder createMultipleOreDrops(Block pBlock, Item item, float minDrops, float maxDrops) {
    HolderLookup.RegistryLookup<Enchantment> registrylookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
    return this.createSilkTouchDispatchTable(pBlock,
        this.applyExplosionDecay(pBlock,
                                 LootItem.lootTableItem(item).apply(
                                     SetItemCountFunction.setCount(
                                         UniformGenerator.between(minDrops,maxDrops)))
                                                .apply(ApplyBonusCount.
                                                           addOreBonusCount(
                                                               registrylookup.getOrThrow(Enchantments.FORTUNE)))));
  }

  @Override
  protected @NotNull Iterable<Block> getKnownBlocks() {
    var entries = new java.util.ArrayList<>(ModRegistry.BLOCKS.getEntries());
    return entries.stream().map(Holder::value)::iterator;
  }
}