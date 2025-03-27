package net.roboxgamer.betterfarms.CropFarmDir;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import net.roboxgamer.betterfarms.BetterFarms;
import net.roboxgamer.betterfarms.CustomItemStackHandler;
import net.roboxgamer.betterfarms.ModRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CropFarmBlockEntity extends BlockEntity implements MenuProvider {
  private static final Component TITLE = ModRegistry.CROP_FARM_BLOCK.get().getName();
  private static final int DEFAULT_MAX_PROGRESS = 100;
  
  private final CustomItemStackHandler inputSlots = new CustomItemStackHandler(3,this);
  private final CustomItemStackHandler extraSlots = new CustomItemStackHandler(3,this);
  private final CustomItemStackHandler upgradeSlots = new CustomItemStackHandler(3,this);
  private final CustomItemStackHandler outputSlots = new CustomItemStackHandler(18, this);
  
  public static final int INPUT_SLOTS_COUNT = 3;
  public static final int EXTRA_SLOTS_COUNT = 3;
  public static final int UPGRADE_SLOTS_COUNT = 3;
  public static final int OUTPUT_SLOTS_COUNT = 18;
  public static final int TOOL_INPUT_SLOT = 0;
  public static final int INGREDIENT_INPUT_SLOT = 1;
  public static final int BASE_INPUT_SLOT = 2;
  
  public static final int TOTAL_SLOT_COUNT = INPUT_SLOTS_COUNT + EXTRA_SLOTS_COUNT + UPGRADE_SLOTS_COUNT + OUTPUT_SLOTS_COUNT;
  
  protected final ContainerData data;
  private int progress = 0;
  private int maxProgress = DEFAULT_MAX_PROGRESS;
  
  private final CombinedInvWrapper combinedInvWrapper = new CombinedInvWrapper(inputSlots, extraSlots, upgradeSlots,
                                                                               outputSlots){
    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
      // Don't allow extracting other than the output slots
      if (slot < 9){
        return ItemStack.EMPTY;
      }
      return super.extractItem(slot, amount, simulate);
    }
    
    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
      // Don't allow inserting into the output slots externally
      if (slot > 9){
        return ItemStack.EMPTY;
      }
      return super.insertItem(slot, stack, simulate);
    }
  };
  
  private List<ItemStack> outputItems = new ArrayList<>();
  
  public CropFarmBlockEntity(BlockPos pos, BlockState blockState) {
    super(ModRegistry.CROP_FARM_BLOCK_ENTITY.get(), pos, blockState);
    data = new ContainerData() {
      @Override
      public int get(int i) {
        return switch (i) {
          case 0 -> CropFarmBlockEntity.this.progress;
          case 1 -> CropFarmBlockEntity.this.maxProgress;
          default -> 0;
        };
      }
      
      @Override
      public void set(int i, int value) {
        switch (i) {
          case 0: CropFarmBlockEntity.this.progress = value;
          case 1: CropFarmBlockEntity.this.maxProgress = value;
        }
      }
      
      @Override
      public int getCount() {
        return 2;
      }
    };
  }
  
  public void tick(Level level, BlockPos blockPos, BlockState blockState) {
    if(hasRecipe()) {
      increaseCraftingProgress();
      setChanged(level, blockPos, blockState);
      
      if(hasCraftingFinished()) {
        craftItem();
        resetProgress();
      }
    } else {
      resetProgress();
    }
  }
  
  private Optional<RecipeHolder<CropFarmRecipe>> getCurrentRecipe() {
    return this.level.getRecipeManager()
        .getRecipeFor(ModRegistry.CROP_FARM_TYPE.get(), new CropFarmRecipeInput(
            this.inputSlots.getStackInSlot(TOOL_INPUT_SLOT),
            this.inputSlots.getStackInSlot(INGREDIENT_INPUT_SLOT),
            this.inputSlots.getStackInSlot(BASE_INPUT_SLOT)
        ), level);
  }
  
  private boolean hasRecipe() {
    Optional<RecipeHolder<CropFarmRecipe>> recipe = getCurrentRecipe();
    if(recipe.isEmpty()) {
      return false;
    }
    
    this.outputItems = recipe.get().value().rollResults(this.level.random);
    return canInsertItemIntoOutputSlots(this.outputItems);
  }
  
  private void increaseCraftingProgress() {
    this.progress++;
  }
  
  private boolean hasCraftingFinished() {
    return this.progress >= this.maxProgress;
  }
  
  private void craftItem() {
    Optional<RecipeHolder<CropFarmRecipe>> recipe = getCurrentRecipe();
    if(recipe.isEmpty()) return;
    
    // --- Core Logic: Insert items into outputSlots ---
    for (ItemStack resultStack : this.outputItems) {
      if (resultStack == null || resultStack.isEmpty()) {
        continue;
      }
      
      ItemStack remainder = ItemHandlerHelper.insertItemStacked(this.outputSlots, resultStack.copy(), false);
      
      if (!remainder.isEmpty()) {
        System.err.println("CRITICAL: Failed to insert recipe output item " + resultStack
                               + " into output slots despite prior checks. Remainder: " + remainder);
      }
    }
  }
  
  private void resetProgress() {
    progress = 0;
    maxProgress = DEFAULT_MAX_PROGRESS;
  }
  
  /**
   * Checks if a list of ItemStacks can potentially be inserted into the output slots.
   * This simulates the insertion without actually changing the inventory.
   *
   * @param itemsToInsert The list of ItemStacks to check for insertion.
   * @return true if all items in the list could theoretically fit, false otherwise.
   */
  private boolean canInsertItemIntoOutputSlots(List<ItemStack> itemsToInsert) {
    if (itemsToInsert == null || itemsToInsert.isEmpty()) {
      return true;
    }
    
    CustomItemStackHandler simulationHandler = new CustomItemStackHandler(this.outputSlots.getSlots(), this);
    for (int i = 0; i < this.outputSlots.getSlots(); i++) {
      simulationHandler.setStackInSlot(i, this.outputSlots.getStackInSlot(i).copy());
    }
    
    for (ItemStack originalStackToInsert : itemsToInsert) {
      if (originalStackToInsert == null || originalStackToInsert.isEmpty()) {
        continue;
      }
      
      ItemStack stackToSimulate = originalStackToInsert.copy();
      
      ItemStack remainder = stackToSimulate;
      
      for (int i = 0; i < simulationHandler.getSlots(); i++) {
        remainder = simulationHandler.insertItem(i, remainder, false);
        if (remainder.isEmpty()) {
          break;
        }
      }
      
      if (!remainder.isEmpty()) {
        return false;
      }
    }
    
    return true;
  }
  
  @Override
  public @NotNull Component getDisplayName() {
    return TITLE;
  }
  
  @Override
  public @Nullable AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInv, @NotNull Player player) {
    return new CropFarmMenu(containerId, playerInv, this,this.data);
  }
  
  CompoundTag getBEData(HolderLookup.Provider registries) {
    CompoundTag beData = new CompoundTag();
    // Serialize the inventory
    beData.put("inputSlots", this.inputSlots.serializeNBT(registries));
    beData.put("extraSlots", this.extraSlots.serializeNBT(registries));
    beData.put("upgradeSlots", this.upgradeSlots.serializeNBT(registries));
    beData.put("outputInv", this.outputSlots.serializeNBT(registries));
    
    // Save the progress
    beData.putInt("progress", this.progress);
    beData.putInt("maxProgress", this.maxProgress);
    return beData;
  }
  
  private void deserializeFromTag(CompoundTag tag, HolderLookup.@NotNull Provider registries) {
    // Deserialize the inventory
    this.inputSlots.deserializeNBT(registries, tag.getCompound("inputSlots"));
    this.extraSlots.deserializeNBT(registries, tag.getCompound("extraSlots"));
    this.upgradeSlots.deserializeNBT(registries, tag.getCompound("upgradeSlots"));
    this.outputSlots.deserializeNBT(registries, tag.getCompound("outputInv"));
    
    // Load the progress
    this.progress = tag.getInt("progress");
    this.maxProgress = tag.getInt("maxProgress");
  }
  
  @Override
  protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
    super.saveAdditional(tag, registries);
    CompoundTag modData = getBEData(registries);
    tag.put(BetterFarms.MODID, modData);
  }
  
  @Override
  protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
    super.loadAdditional(tag, registries);
    // Check if we are on the client side
    if (level != null && level.isClientSide()) {
      // Deserialize data from the tag for client-side
      deserializeFromTag(tag, registries);
    } else {
      CompoundTag modData = tag.getCompound(BetterFarms.MODID);
      deserializeFromTag(modData, registries);
    }
  }
  
  public void drops() {
    if (this.level == null) return;
    Containers.dropContents(this.level,this.worldPosition,this.inputSlots.getContainer());
    Containers.dropContents(this.level,this.worldPosition,this.extraSlots.getContainer());
    Containers.dropContents(this.level,this.worldPosition,this.upgradeSlots.getContainer());
    Containers.dropContents(this.level,this.worldPosition,this.outputSlots.getContainer());
  }
  
  public CustomItemStackHandler getInputSlotsItemHandler() {
    return this.inputSlots;
  }
  
  public CustomItemStackHandler getExtraSlotsItemHandler() {
    return this.extraSlots;
  }
  
  public CustomItemStackHandler getUpgradeSlotsItemHandler() {
    return this.upgradeSlots;
  }
  
  public CustomItemStackHandler getOutputSlotsItemHandler() {
    return this.outputSlots;
  }
  
  public @Nullable IItemHandler getCapabilityHandler(@Nullable Direction side) {
    return this.combinedInvWrapper;
  }
}
