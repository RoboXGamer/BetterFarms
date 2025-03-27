package net.roboxgamer.betterfarms;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.roboxgamer.betterfarms.CropFarmDir.CropFarmBlock;
import net.roboxgamer.betterfarms.CropFarmDir.CropFarmBlockEntity;
import net.roboxgamer.betterfarms.CropFarmDir.CropFarmMenu;
import net.roboxgamer.betterfarms.CropFarmDir.CropFarmRecipe;

import java.util.function.Supplier;

public class ModRegistry {
  public static final DeferredRegister.Blocks BLOCKS =
      DeferredRegister.createBlocks(BetterFarms.MODID);
  
  public static final DeferredRegister.Items ITEMS =
      DeferredRegister.createItems(BetterFarms.MODID);
  
  public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
      DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, BetterFarms.MODID);
  
  public static final DeferredRegister<MenuType<?>> MENUS =
      DeferredRegister.create(BuiltInRegistries.MENU, BetterFarms.MODID);
  
  public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
      DeferredRegister.create(Registries.RECIPE_SERIALIZER, BetterFarms.MODID);
  public static final DeferredRegister<RecipeType<?>> TYPES =
      DeferredRegister.create(Registries.RECIPE_TYPE, BetterFarms.MODID);
  
  private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
    DeferredBlock<T> toReturn = BLOCKS.register(name, block);
    registerBlockItem(name, toReturn);
    return toReturn;
  }
  
  public static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
    ITEMS.register(name, ()-> new BlockItem(block.get(), new Item.Properties()));
  }
  
  public static void register(IEventBus eventBus) {
    BLOCKS.register(eventBus);
    ITEMS.register(eventBus);
    BLOCK_ENTITIES.register(eventBus);
    MENUS.register(eventBus);
    // Custom Recipes
    SERIALIZERS.register(eventBus);
    TYPES.register(eventBus);
  }
  
//  All the registrations
  
  public static final DeferredBlock<CropFarmBlock> CROP_FARM_BLOCK = registerBlock("crop_farm_block",() ->
      new CropFarmBlock(BlockBehaviour.Properties.of()));
  
  public static final Supplier<BlockEntityType<CropFarmBlockEntity>> CROP_FARM_BLOCK_ENTITY =
      BLOCK_ENTITIES.register("crop_farm_block_entity", () -> BlockEntityType.Builder.
          of(CropFarmBlockEntity::new,
             CROP_FARM_BLOCK.get())
          .build(null));
  
  public static final Supplier<MenuType<CropFarmMenu>> CROP_FARM_MENU = MENUS.register(
      "crop_farm_menu", () -> IMenuTypeExtension.create(CropFarmMenu::new));
  
  public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CropFarmRecipe>> CROP_FARM_SERIALIZER =
      SERIALIZERS.register("crop_farm", CropFarmRecipe.Serializer::new);
  public static final DeferredHolder<RecipeType<?>, RecipeType<CropFarmRecipe>> CROP_FARM_TYPE =
      TYPES.register("crop_farm", () -> new RecipeType<CropFarmRecipe>() {
        @Override
        public String toString() {
          return "crop_farm";
        }
      });
}
