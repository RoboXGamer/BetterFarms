package net.roboxgamer.betterfarms.TreeFarmDir;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;
import net.roboxgamer.betterfarms.ModRegistry;
import net.roboxgamer.betterfarms.base.AbstractFarmBlockEntity;
import net.roboxgamer.betterfarms.base.recipe.ThreeSlotFarmInput;
import net.roboxgamer.betterfarms.util.CustomItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TreeFarmBlockEntity extends AbstractFarmBlockEntity {
  private final CustomItemStackHandler inputSlots;
  
  public static final int INPUT_SLOTS_COUNT = 3;
  public static final int TOTAL_SLOT_COUNT = INPUT_SLOTS_COUNT + UPGRADE_SLOTS_COUNT + OUTPUT_SLOTS_COUNT;
  
  public static final int TOOL_INPUT_SLOT = 0;
  public static final int INGREDIENT_INPUT_SLOT = 1;
  public static final int BASE_INPUT_SLOT = 2;
  
  private static final Component TITLE = ModRegistry.TREE_FARM_BLOCK.get().getName();
  
  public TreeFarmBlockEntity(BlockPos pos, BlockState blockState) {
    super(ModRegistry.TREE_FARM_BLOCK_ENTITY.get(), pos, blockState);
    this.inputSlots = new CustomItemStackHandler(INPUT_SLOTS_COUNT, this);
    
    this.combinedInvWrapper = createCombinedWrapper();
  }
  
  @Override
  protected RecipeType<? extends Recipe<? extends RecipeInput>> getRecipeType() {
    return ModRegistry.TREE_FARM_TYPE.get();
  }
  
  @Override
  protected RecipeInput createRecipeInput() {
    return new ThreeSlotFarmInput(
        this.inputSlots.getStackInSlot(TOOL_INPUT_SLOT),
        this.inputSlots.getStackInSlot(INGREDIENT_INPUT_SLOT),
        this.inputSlots.getStackInSlot(BASE_INPUT_SLOT)
    );
  }
  
  @Override
  protected CustomItemStackHandler getInputSlotsHandler() {
    return this.inputSlots;
  }
  
  @Override
  public @NotNull Component getDisplayName() {
    return TITLE;
  }
  
  @Override
  public @Nullable AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInv, @NotNull Player player) {
    return new TreeFarmMenu(containerId, playerInv, this, this.data);
  }
  
  @Override
  protected void craftItem() {
    if (!(level instanceof ServerLevel serverLevel)) return;
    // --- Crop Farm SPECIFIC logic ---
    
    // Damage the Hoe in the tool slot
    ItemStack tool = this.inputSlots.getStackInSlot(TOOL_INPUT_SLOT);
    if (!tool.isEmpty() && tool.isDamageableItem() && level != null && !level.isClientSide()) {
      tool.hurtAndBreak(1, serverLevel, null, (item) -> {
         level.playSound(null, worldPosition, SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);
      });
      this.setChanged();
    }
    // --- Call common logic AFTER specific actions ---
    // This inserts the recipe results into the output slots
    super.craftItem();
  }
}
