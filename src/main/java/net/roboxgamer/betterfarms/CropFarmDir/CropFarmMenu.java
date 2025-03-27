package net.roboxgamer.betterfarms.CropFarmDir;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.roboxgamer.betterfarms.ModRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CropFarmMenu extends AbstractContainerMenu {
  private final CropFarmBlockEntity blockEntity;
  private final ContainerLevelAccess levelAccess;
  private final ContainerData data;
  
  // Client Constructor
  public CropFarmMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf extraData) {
    this(containerId, playerInv,
         (CropFarmBlockEntity) playerInv.player.level().getBlockEntity(extraData.readBlockPos()));
  }
  
  public CropFarmMenu(int containerId, @NotNull Inventory playerInv,
                               CropFarmBlockEntity blockEntity) {
    this(containerId, playerInv, blockEntity, new SimpleContainerData(2));
  }
  
  // Server Constructor
  public CropFarmMenu(int containerId, @NotNull Inventory playerInv, CropFarmBlockEntity blockEntity, ContainerData data) {
    super(ModRegistry.CROP_FARM_MENU.get(), containerId);
    this.blockEntity = blockEntity;
    this.levelAccess = ContainerLevelAccess.create(Objects.requireNonNull(blockEntity.getLevel()),
                                                   blockEntity.getBlockPos());
    this.data = data;
    this.addDataSlots(data);
    createBlockEntityInventory(this.blockEntity);
    
    createPlayerHotbar(playerInv);
    createPlayerInventory(playerInv);
  }
  
  private void createBlockEntityInventory(CropFarmBlockEntity blockEntity) {
    IItemHandler inputSlots = blockEntity.getInputSlotsItemHandler();
    // Input Slots
    for (int i = 0; i < inputSlots.getSlots(); i++) {
      this.addSlot(new SlotItemHandler(inputSlots, i, 98 + i * 18, 14));
    }
    
    IItemHandler extraSlots = blockEntity.getExtraSlotsItemHandler();
    // Extra Slots
    for (int i = 0; i < extraSlots.getSlots(); i++) {
      this.addSlot(new SlotItemHandler(extraSlots, i, 98 + i * 18, 34));
    }
    
    IItemHandler upgradeSlots = blockEntity.getUpgradeSlotsItemHandler();
    // Upgrade Slots
    for (int i = 0; i < upgradeSlots.getSlots(); i++) {
      this.addSlot(new SlotItemHandler(upgradeSlots, i, 98 + i * 18, 54));
    }
    
    // Output Slots
    IItemHandler outputSlots = blockEntity.getOutputSlotsItemHandler();
    int outputSlotsYStart = 74;
    for (int j = 0; j < 2; j++) {
      for (int i = 0; i < 9; i++) {
        this.addSlot(new SlotItemHandler(outputSlots, i + j * 9, 8 + i * 18, outputSlotsYStart + j * 18));
      }
    }
  }
  
  private void createPlayerInventory(@NotNull Inventory playerInv) {
    var playerInvYStart = 124;
    for (int row = 0; row < 3; ++row) {
      for (int col = 0; col < 9; ++col) {
        this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, playerInvYStart + row * 18));
      }
    }
  }
  
  private void createPlayerHotbar(@NotNull Inventory playerInv) {
    var hotbarYStart = 182;
    for (int col = 0; col < 9; ++col) {
      this.addSlot(new Slot(playerInv, col, 8 + col * 18, hotbarYStart));
    }
  }
  
  // CREDIT GOES TO: diesieben07 | https://github.com/diesieben07/SevenCommons
  // must assign a slot number to each of the slots used by the GUI.
  // For this container, we can see both the tile inventory's slots as well as the player inventory slots and the hotbar.
  // Each time we add a Slot to the container, it automatically increases the slotIndex, which means
  //  0 - 8 = hotbar slots (which will map to the InventoryPlayer slot numbers 0 - 8)
  //  9 - 35 = player inventory slots (which map to the InventoryPlayer slot numbers 9 - 35)
  //  36 - 44 = TileInventory slots, which map to our TileEntity slot numbers 0 - 8)
  private static final int HOTBAR_SLOT_COUNT = 9;
  private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
  private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
  private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
  private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
  private static final int VANILLA_FIRST_SLOT_INDEX = 0;
  private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;
  
  // THIS YOU HAVE TO DEFINE!
  private static final int TE_INVENTORY_SLOT_COUNT = CropFarmBlockEntity.TOTAL_SLOT_COUNT;  // must be the number of slots you have!
  @Override
  public @NotNull ItemStack quickMoveStack(@NotNull Player playerIn, int pIndex) {
    Slot sourceSlot = slots.get(pIndex);
    if (!sourceSlot.hasItem()) return ItemStack.EMPTY;  //EMPTY_ITEM
    ItemStack sourceStack = sourceSlot.getItem();
    ItemStack copyOfSourceStack = sourceStack.copy();
    
    // Check if the slot clicked is one of the vanilla container slots
    if (pIndex < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
      // This is a vanilla container slot so merge the stack into the tile inventory
      if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX
          + TE_INVENTORY_SLOT_COUNT, false)) {
        return ItemStack.EMPTY;  // EMPTY_ITEM
      }
    } else if (pIndex < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
      // This is a TE slot so merge the stack into the players inventory
      if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
        return ItemStack.EMPTY;
      }
    } else {
      System.out.println("Invalid slotIndex:" + pIndex);
      return ItemStack.EMPTY;
    }
    // If stack size == 0 (the entire stack was moved) set slot contents to null
    if (sourceStack.getCount() == 0) {
      sourceSlot.set(ItemStack.EMPTY);
    } else {
      sourceSlot.setChanged();
    }
    sourceSlot.onTake(playerIn, sourceStack);
    return copyOfSourceStack;
  }
  
  @Override
  public boolean stillValid(@NotNull Player player) {
    return stillValid(this.levelAccess, player, ModRegistry.CROP_FARM_BLOCK.get());
  }
  
  public boolean isProcessing() {
    return data.get(0) > 0;
  }
}
