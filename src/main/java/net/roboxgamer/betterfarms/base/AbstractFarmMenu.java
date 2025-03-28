package net.roboxgamer.betterfarms.base;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Abstract base class for farm menus.
 * Handles common logic like player inventory/hotbar slots, data slots,
 * validity checks, and shift-click (quickMoveStack) behavior.
 * Subclasses must provide the specific MenuType and implement the layout
 * for the Block Entity's own inventory slots.
 *
 * @param <T> The type of AbstractFarmBlockEntity this menu interacts with.
 */
public abstract class AbstractFarmMenu<T extends AbstractFarmBlockEntity> extends AbstractContainerMenu {
  
  protected final T blockEntity;
  protected final ContainerLevelAccess levelAccess;
  protected final ContainerData data;
  
  /**
   * Constructor for the abstract farm menu.
   *
   * @param menuType    The specific MenuType being registered.
   * @param containerId The container ID.
   * @param playerInv   The player's inventory.
   * @param blockEntity The associated BlockEntity instance.
   * @param data        The ContainerData for syncing values (e.g., progress).
   */
  protected AbstractFarmMenu(@Nullable MenuType<?> menuType, int containerId, @NotNull Inventory playerInv, T blockEntity, ContainerData data) {
    super(menuType, containerId);
    this.blockEntity = blockEntity;
    // Ensure level is not null before creating ContainerLevelAccess
    if (blockEntity.getLevel() == null) {
      throw new IllegalStateException("Cannot create menu for BlockEntity with null Level!");
    }
    this.levelAccess = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
    this.data = data;
    
    // Add data slots (e.g., for progress bar)
    this.addDataSlots(data);
    
    // --- Subclass Responsibility ---
    // Subclass constructor should call addBlockEntitySlots() HERE
    // to add the slots specific to the farm block's inventory.
    // Example: addBlockEntitySlots();
    
    // --- Common Slots ---
    createPlayerInventory(playerInv);
    createPlayerHotbar(playerInv);
  }
  
  /**
   * Adds the slots specific to the Block Entity's inventory.
   * MUST be implemented by subclasses to define their layout.
   */
  protected abstract void addBlockEntitySlots();
  
  /**
   * Creates and adds the player's main inventory slots.
   *
   * @param playerInv The player's inventory.
   */
  protected void createPlayerInventory(@NotNull Inventory playerInv) {
    int inventoryYOffset = getPlayerInventoryYOffset(); // Allow subclasses to override Y position easily
    for (int row = 0; row < 3; ++row) {
      for (int col = 0; col < 9; ++col) {
        this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, inventoryYOffset + row * 18));
      }
    }
  }
  
  /**
   * Creates and adds the player's hotbar slots.
   *
   * @param playerInv The player's inventory.
   */
  protected void createPlayerHotbar(@NotNull Inventory playerInv) {
    int hotbarYOffset = getPlayerHotbarYOffset(); // Allow subclasses to override Y position easily
    for (int col = 0; col < 9; ++col) {
      this.addSlot(new Slot(playerInv, col, 8 + col * 18, hotbarYOffset));
    }
  }
  
  /**
   * Defines the Y coordinate where the player's main inventory slots begin.
   * Can be overridden by subclasses if their GUI layout requires it.
   * Default value assumes a standard 176px high texture + additional space.
   * @return The Y offset for the player inventory.
   */
  protected int getPlayerInventoryYOffset() {
    return 84 + 40; // Example: 84 (standard) + 40 (added height for farm)
  }
  
  /**
   * Defines the Y coordinate where the player's hotbar slots begin.
   * Can be overridden by subclasses if their GUI layout requires it.
   * Default value assumes standard spacing below the main inventory.
   * @return The Y offset for the player hotbar.
   */
  protected int getPlayerHotbarYOffset() {
    return getPlayerInventoryYOffset() + 58; // Example: Inventory Y + 3 rows * 18px + 4px gap
  }
  
  
  // --- Shift-Click Behavior (quickMoveStack) ---
  
  // Vanilla slot constants (assuming standard player inventory layout)
  protected static final int HOTBAR_SLOT_COUNT = 9;
  protected static final int PLAYER_INVENTORY_ROW_COUNT = 3;
  protected static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
  protected static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
  protected static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
  
  // Slot index boundaries
  protected static final int VANILLA_FIRST_SLOT_INDEX = 0;
  protected static final int VANILLA_LAST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT - 1;
  protected static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_LAST_SLOT_INDEX + 1;
  
  // Subclasses MUST define this based on their total number of BE slots
  // Example: public static final int TE_INVENTORY_SLOT_COUNT = CropFarmBlockEntity.TOTAL_SLOT_COUNT;
  // It might be better for subclasses to override a method returning this value.
  /**
   * @return The total number of slots belonging to the Block Entity inventory.
   */
  protected abstract int getBlockEntitySlotCount();
  
  @Override
  public @NotNull ItemStack quickMoveStack(@NotNull Player playerIn, int index) {
    Slot sourceSlot = slots.get(index);
    if (!sourceSlot.hasItem()) {
      return ItemStack.EMPTY;
    }
    ItemStack sourceStack = sourceSlot.getItem();
    ItemStack copyOfSourceStack = sourceStack.copy();
    
    int teSlotCount = getBlockEntitySlotCount();
    int teLastSlotIndex = TE_INVENTORY_FIRST_SLOT_INDEX + teSlotCount - 1;
    
    // Check if the slot clicked is one of the vanilla container slots
    if (index >= VANILLA_FIRST_SLOT_INDEX && index <= VANILLA_LAST_SLOT_INDEX) {
      // Try moving from player inventory to TE inventory
      // We let moveItemStackTo handle finding a suitable slot based on item validity rules.
      if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, teLastSlotIndex + 1, false)) {
        // If TE is full or invalid, try moving within player inventory (e.g., inv to hotbar)
        if (index < PLAYER_INVENTORY_FIRST_SLOT_INDEX()) { // Hotbar slots
          if (!moveItemStackTo(sourceStack, PLAYER_INVENTORY_FIRST_SLOT_INDEX(), VANILLA_LAST_SLOT_INDEX + 1, false)) {
            return ItemStack.EMPTY;
          }
        } else { // Main inventory slots
          if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, HOTBAR_SLOT_COUNT, false)) {
            return ItemStack.EMPTY;
          }
        }
        // Note: Original code only tried moving TO TE. Added inv<->hotbar attempt.
        // return ItemStack.EMPTY; // Original behavior if TE move fails
      }
    }
    // Check if the slot clicked is one of the TE slots
    else if (index >= TE_INVENTORY_FIRST_SLOT_INDEX && index <= teLastSlotIndex) {
      // Try moving from TE inventory to player inventory
      if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_LAST_SLOT_INDEX + 1, false)) {
        return ItemStack.EMPTY;
      }
    }
    // Slot index is out of bounds? Should not happen.
    else {
      System.err.println("Invalid slotIndex:" + index + " in " + this.getClass().getSimpleName());
      return ItemStack.EMPTY;
    }
    
    // If stack size == 0 (the entire stack was moved), set slot contents to empty
    if (sourceStack.getCount() == 0) {
      sourceSlot.set(ItemStack.EMPTY);
    } else {
      sourceSlot.setChanged(); // Notify slot stack changed
    }
    
    // Perform check for crafting matrix updates etc.
    sourceSlot.onTake(playerIn, sourceStack);
    
    return copyOfSourceStack; // Return the copy
  }
  
  // Helper for quickMoveStack boundary check
  private static int PLAYER_INVENTORY_FIRST_SLOT_INDEX() {
    return HOTBAR_SLOT_COUNT;
  }
  
  // --- Validity Check ---
  
  /**
   * Checks if the menu is still valid (player is near the block).
   * Subclasses should provide the specific Block instance.
   * @param player The player using the menu.
   * @param targetBlock The block associated with this menu.
   * @return true if the menu is still valid.
   */
  protected boolean stillValid(@NotNull Player player, Block targetBlock) {
    return stillValid(this.levelAccess, player, targetBlock);
  }
  
  // Subclasses MUST override this to call the protected stillValid with their specific block.
  @Override
  public abstract boolean stillValid(@NotNull Player player);
  
  
  // --- Data Access Helpers ---
  
  /**
   * @return true if the farm is currently processing (progress > 0).
   */
  public boolean isProcessing() {
    // Assumes progress is at index 0 in ContainerData
    return data.get(0) > 0;
  }
  
  /**
   * Gets the current processing progress scaled for a progress bar.
   * @param progressBarWidth The pixel width of the progress bar in the GUI.
   * @return The scaled progress value.
   */
  public int getScaledProgress(int progressBarWidth) {
    int currentProgress = this.data.get(0);
    int maxProgress = this.data.get(1);
    // Prevent division by zero and handle case where maxProgress isn't loaded yet
    if (maxProgress <= 0) {
      return 0;
    }
    return currentProgress * progressBarWidth / maxProgress;
  }
  
  // Add other data accessors if needed (e.g., energy levels)
  
  public T getBlockEntity() {
    return this.blockEntity;
  }
}
