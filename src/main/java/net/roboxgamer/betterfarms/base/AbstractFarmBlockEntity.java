package net.roboxgamer.betterfarms.base; // Assuming base package structure

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import net.roboxgamer.betterfarms.util.ChanceResult;
import net.roboxgamer.betterfarms.util.CustomItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Abstract base class for farm Block Entities.
 * Handles common ticking logic, progress, inventory management (output/upgrade),
 * NBT saving/loading, capability exposure, and recipe processing flow.
 * Subclasses must provide specific input/extra inventories, recipe type,
 * recipe input creation, display name, and menu creation.
 */
public abstract class AbstractFarmBlockEntity extends BlockEntity implements MenuProvider {
  
  // --- Constants ---
  protected static final int DEFAULT_MAX_PROGRESS = 10;
  public static final int UPGRADE_SLOTS_COUNT = 3;
  public static final int OUTPUT_SLOTS_COUNT = 18;
  
  // --- Common Fields ---
  protected final CustomItemStackHandler upgradeSlots;
  protected final CustomItemStackHandler outputSlots;
  protected final ContainerData data;
  protected int progress = 0;
  protected int maxProgress = DEFAULT_MAX_PROGRESS;
  protected List<ItemStack> outputItems = new ArrayList<>();
  
  /**
   * Combined wrapper for external access (hoppers, pipes).
   * Initialized in constructor using handlers provided by subclasses via abstract methods.
   */
  protected CombinedInvWrapper combinedInvWrapper;
  
  // --- Constructor ---
  public AbstractFarmBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
    super(pType, pPos, pBlockState);
    
    // Initialize common inventories
    this.upgradeSlots = new CustomItemStackHandler(UPGRADE_SLOTS_COUNT, this);
    this.outputSlots = new CustomItemStackHandler(OUTPUT_SLOTS_COUNT, this);
    
    // ContainerData for syncing progress to client menu
    this.data = new ContainerData() {
      @Override
      public int get(int i) {
        return switch (i) {
          case 0 -> AbstractFarmBlockEntity.this.progress;
          case 1 -> AbstractFarmBlockEntity.this.maxProgress;
          default -> 0;
        };
      }
      
      @Override
      public void set(int i, int value) {
        switch (i) {
          case 0 -> AbstractFarmBlockEntity.this.progress = value;
          case 1 -> AbstractFarmBlockEntity.this.maxProgress = value;
        }
      }
      
      @Override
      public int getCount() {
        return 2; // Represents progress and maxProgress
      }
    };
  }
  
  /** Helper to create the CombinedInvWrapper. Can be overridden if needed. */
  protected CombinedInvWrapper createCombinedWrapper() {
    // Get handlers from subclass implementation
    CustomItemStackHandler inputHandler = getInputSlotsHandler();
    
    // Ensure handlers are not null
    if (inputHandler == null) throw new IllegalStateException("Subclass must provide a non-null input slot handler");
    
    return new CombinedInvWrapper(inputHandler, this.upgradeSlots, this.outputSlots) {
      // Prevent extraction from non-output slots externally
      @Override
      public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        // Calculate the starting index of output slots
        int outputSlotStartIndex = inputHandler.getSlots() + UPGRADE_SLOTS_COUNT;
        if (slot < outputSlotStartIndex) {
          return ItemStack.EMPTY; // Cannot extract from input, extra, or upgrade slots
        }
        // Adjust slot index for the super call relative to the output handler
        return super.extractItem(slot, amount, simulate);
      }
      
      // Prevent insertion into output slots externally
      @Override
      public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        // Calculate the starting index of output slots
        int outputSlotStartIndex = inputHandler.getSlots() + UPGRADE_SLOTS_COUNT;
        if (slot >= outputSlotStartIndex) {
          return stack; // Cannot insert into output slots externally, return original stack
        }
        return super.insertItem(slot, stack, simulate);
      }
    };
  }
  
  public CustomItemStackHandler getOutputSlotsItemHandler () { return this.outputSlots; }
  
  public CustomItemStackHandler getUpgradeSlotsItemHandler () { return this.upgradeSlots; }
  
  
  // --- Abstract Methods (Subclass MUST Implement) ---
  
  /** @return The specific RecipeType this farm processes (e.g., CROP_FARM_TYPE). */
  protected abstract RecipeType<? extends Recipe<? extends RecipeInput>> getRecipeType();
  
  /** @return A new RecipeInput object reflecting the current items in the relevant input slots. */
  protected abstract RecipeInput createRecipeInput();
  
  /** @return The CustomItemStackHandler for the primary input slots. */
  protected abstract CustomItemStackHandler getInputSlotsHandler();
  
  /** @return The user-facing name of the block/menu. */
  @Override
  public abstract @NotNull Component getDisplayName();
  
  /** @return A new instance of the specific AbstractContainerMenu for this farm. */
  @Override
  public abstract @Nullable AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInv, @NotNull Player player);
  
  
  // --- Common Ticking and Processing Logic ---
  
  /**
   * Server-side tick logic. Handles recipe checking and processing.
   */
  public void tick(Level level, BlockPos blockPos, BlockState blockState) {
    if (level.isClientSide()) {
      return; // Do nothing on client
    }
    
    if (hasRecipe()) {
      increaseCraftingProgress();
      setChanged(level, blockPos, blockState); // Mark dirty if progress changes
      
      if (hasCraftingFinished()) {
        craftItem();
        resetProgress();
        // setChanged is called within craftItem via handler changes and resetProgress
      }
    } else {
      resetProgress();
      // setChanged called within resetProgress if progress was > 0
    }
  }
  
  /**
   * Checks if a valid recipe exists for the current inputs and if the output can fit.
   * @return true if a recipe can be processed, false otherwise.
   */
  protected boolean hasRecipe() {
    Optional<RecipeHolder<Recipe<RecipeInput>>> recipeHolder = getCurrentRecipe();
    if (recipeHolder.isEmpty()) {
      return false;
    }
    
    // Assume recipes have a method to get potential results with chance
    // This requires a common interface or casting, adapt if structure differs
    Recipe<?> recipe = recipeHolder.get().value();
    if (recipe instanceof ICraftingFarmRecipe<?> farmRecipe) { // Example interface
      // Determine potential outputs (may involve simulation or fixed list)
      this.outputItems = farmRecipe.rollResults(this.level.random); // Roll results now for checking fit
      if (this.outputItems.isEmpty() && !farmRecipe.getRollResults().isEmpty()) {
        // Recipe has outputs defined, but RNG rolled nothing this time. Still counts as progress.
        // Or, should we require *potential* output space? Let's require space for now.
        // If rollResults is empty, it means no output is possible, proceed.
      }
      return canInsertItemIntoOutputSlots(this.outputItems);
    } else {
      // Fallback or error for unexpected recipe types
      // For now, assume it matches if found and output check isn't possible/needed
      System.err.println("Recipe " + recipeHolder.get().id() + " is not an instance of ICraftingFarmRecipe, cannot check output fit precisely.");
      // Try getting a single result item if possible for basic check
      ItemStack result = recipe.getResultItem(this.level.registryAccess());
      if (!result.isEmpty()) {
        this.outputItems = List.of(result.copy()); // Use single result for check
        return canInsertItemIntoOutputSlots(this.outputItems);
      }
      this.outputItems = List.of(); // No obvious output
      return true; // Assume it's fine if no output item specified
    }
  }
  
  /**
   * Gets the current recipe matching the inputs.
   * Uses an unchecked cast, assuming the RecipeType provided by getRecipeType()
   * corresponds to a Recipe that accepts the RecipeInput provided by createRecipeInput().
   * This assumption must be upheld by subclasses.
   *
   * @return Optional containing the RecipeHolder if a match is found. The Recipe within the holder
   *         is generalized to Recipe<RecipeInput> due to type erasure and casting.
   */
  protected Optional<RecipeHolder<Recipe<RecipeInput>>> getCurrentRecipe() {
    // Use the recipe type and input provided by the subclass
    @SuppressWarnings("unchecked")
    RecipeType<Recipe<RecipeInput>> castedRecipeType = (RecipeType<Recipe<RecipeInput>>) this.getRecipeType();
    return this.level.getRecipeManager().getRecipeFor(castedRecipeType, this.createRecipeInput(), this.level);
  }
  
  /**
   * Increases the crafting progress. Subclasses could override to modify rate based on upgrades.
   */
  protected void increaseCraftingProgress() {
    this.progress++;
  }
  
  /**
   * Checks if the crafting progress has reached the maximum.
   * @return true if crafting is finished.
   */
  protected boolean hasCraftingFinished() {
    // Apply speed upgrades here potentially by checking maxProgress against a modified value
    return this.progress >= this.maxProgress;
  }
  
  /**
   * Performs the crafting operation: consumes inputs (optional), applies tool damage (optional),
   * and inserts results into the output slots.
   * Subclasses SHOULD override this to handle specific input consumption and tool damage.
   */
  protected void craftItem() {
    Optional<RecipeHolder<Recipe<RecipeInput>>> recipe = getCurrentRecipe();
    if (recipe.isEmpty()) {
      // This shouldn't happen if hasRecipe passed, but safety check
      return;
    }
    
    // --- Subclass Responsibility ---
    // - Consume items from input/extra slots (e.g., inputSlots.extractItem(...))
    // - Damage tool in input slot (check for durability, apply damage)
    // Example:
    // ItemStack tool = getInputSlotsHandler().getStackInSlot(TOOL_SLOT_INDEX);
    // if (!tool.isEmpty() && tool.isDamageableItem()) {
    //     tool.hurtAndBreak(1, level.getServer().getLevel(level.dimension()), null, (item) -> {}); // Handle potential break
    //     getInputSlotsHandler().setStackInSlot(TOOL_SLOT_INDEX, tool); // Update slot if hurtAndBreak doesn't modify in-place
    // }
    // ItemStack primaryInput = getInputSlotsHandler().getStackInSlot(PRIMARY_SLOT_INDEX);
    // getInputSlotsHandler().extractItem(PRIMARY_SLOT_INDEX, 1, false); // Consume one item
    
    
    // --- Common Logic: Insert results ---
    // Assumes this.outputItems was populated by hasRecipe()
    for (ItemStack resultStack : this.outputItems) {
      if (resultStack == null || resultStack.isEmpty()) {
        continue;
      }
      
      // Insert into output slots, ItemHandlerHelper handles distribution/stacking
      ItemStack remainder = ItemHandlerHelper.insertItemStacked(this.outputSlots, resultStack.copy(), false);
      
      // Log error if insertion fails unexpectedly (shouldn't happen after canInsert checks)
      if (!remainder.isEmpty()) {
        System.err.println("CRITICAL: Failed to insert recipe output item " + resultStack
                               + " into output slots despite prior checks. Remainder: " + remainder + " in BE at " + worldPosition);
      }
    }
    // Clear rolled items for next tick
    this.outputItems.clear();
  }
  
  
  /**
   * Resets the crafting progress. Marks dirty if progress was > 0.
   */
  protected void resetProgress() {
    if (this.progress > 0) {
      this.progress = 0;
      // Consider resetting maxProgress if it's dynamic
      // this.maxProgress = DEFAULT_MAX_PROGRESS;
      setChanged(); // Mark dirty only if progress actually changed
    }
  }
  
  /**
   * Simulates inserting a list of items into the output slots to check if they fit.
   * @param itemsToInsert The list of ItemStacks to check.
   * @return true if all items could theoretically fit, false otherwise.
   */
  protected boolean canInsertItemIntoOutputSlots(List<ItemStack> itemsToInsert) {
    if (itemsToInsert == null || itemsToInsert.isEmpty()) {
      return true; // Nothing to insert, always fits
    }
    
    // Use a simulation handler to avoid modifying the actual inventory
    CustomItemStackHandler simulationHandler = new CustomItemStackHandler(this.outputSlots.getSlots(), this);
    for (int i = 0; i < this.outputSlots.getSlots(); i++) {
      simulationHandler.setStackInSlot(i, this.outputSlots.getStackInSlot(i).copy());
    }
    
    for (ItemStack originalStackToInsert : itemsToInsert) {
      if (originalStackToInsert == null || originalStackToInsert.isEmpty()) {
        continue;
      }
      
      ItemStack stackToSimulate = originalStackToInsert.copy();
      
      // Try inserting into the simulated handler
      ItemStack remainder = ItemHandlerHelper.insertItemStacked(simulationHandler, stackToSimulate, true); // Simulate!
      
      if (!remainder.isEmpty()) {
        // If any item doesn't fully fit, the whole operation fails
        return false;
      }
    }
    
    return true; // All items fit in the simulation
  }
  
  
  // --- Inventory Handling & Drops ---
  
  /**
   * Drops the contents of all managed inventories into the world.
   * Called by AbstractFarmBlock when the block is removed.
   */
  public void drops() {
    if (this.level == null || this.level.isClientSide()) return;
    
    // Use handlers provided by subclasses
    safeDropContents(getInputSlotsHandler());
    safeDropContents(this.upgradeSlots);
    safeDropContents(this.outputSlots);
  }
  
  private void safeDropContents(@Nullable IItemHandler handler) {
    if (this.level == null || handler == null) return;
    for (int i = 0; i < handler.getSlots(); i++) {
      ItemStack stack = handler.getStackInSlot(i);
      if (!stack.isEmpty()) {
        Containers.dropItemStack(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), stack);
      }
    }
  }
  
  
  // --- NBT Saving/Loading ---
  
  @Override
  protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
    super.saveAdditional(tag, registries);
    // Save common data directly to the main tag
    tag.putInt("progress", this.progress);
    tag.putInt("maxProgress", this.maxProgress);
    // Save inventories using their handlers
    tag.put("upgrades", this.upgradeSlots.serializeNBT(registries));
    tag.put("output", this.outputSlots.serializeNBT(registries));
    
    // Delegate to subclass handlers for their inventories
    CustomItemStackHandler inputHandler = getInputSlotsHandler();
      tag.put("inputs", inputHandler.serializeNBT(registries));
  }
  
  @Override
  protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
    super.loadAdditional(tag, registries);
    // Load common data
    this.progress = tag.getInt("progress");
    this.maxProgress = tag.getInt("maxProgress");
    if (this.maxProgress <= 0) this.maxProgress = DEFAULT_MAX_PROGRESS; // Safety check
    
    // Load inventories
    this.upgradeSlots.deserializeNBT(registries, tag.getCompound("upgrades"));
    this.outputSlots.deserializeNBT(registries, tag.getCompound("output"));
    
    // Delegate loading to subclass handlers
    CustomItemStackHandler inputHandler = getInputSlotsHandler();
    if (tag.contains("inputs", CompoundTag.TAG_COMPOUND)) {
      inputHandler.deserializeNBT(registries, tag.getCompound("inputs"));
    }
  }
  
  
  // --- Capabilities ---
  
  /**
   * Exposes the CombinedInvWrapper for item handling capabilities.
   * @param side The side being accessed (can be null).
   * @return The combined item handler capability.
   */
  public @Nullable IItemHandler getCapabilityHandler(@Nullable Direction side) {
    // Return the wrapper that controls input/output restrictions
    return this.combinedInvWrapper;
  }
  
  // --- Helper Interface for Recipe Handling ---
  /**
   * Generic interface that farm recipes should implement to provide standardized output methods.
   * @param <T> The specific RecipeInput type this recipe uses.
   */
  public interface ICraftingFarmRecipe<T extends RecipeInput> extends Recipe<T> {
    List<ItemStack> rollResults(net.minecraft.util.RandomSource random);
    List<ChanceResult> getRollResults();
    List<ItemStack> getResults();
  }
}