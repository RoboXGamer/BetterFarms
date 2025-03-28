package net.roboxgamer.betterfarms.CropFarmDir;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.roboxgamer.betterfarms.ModRegistry;
import org.jetbrains.annotations.NotNull;

import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.roboxgamer.betterfarms.base.AbstractFarmMenu;

import java.util.Objects;

public class CropFarmMenu extends AbstractFarmMenu<CropFarmBlockEntity> {
  
  // Client Constructor
  public CropFarmMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf extraData) {
    this(containerId, playerInv, getBlockEntity(playerInv, extraData));
  }
  
  private static CropFarmBlockEntity getBlockEntity(Inventory playerInv, RegistryFriendlyByteBuf extraData) {
    Objects.requireNonNull(playerInv, "Player Inventory cannot be null");
    Objects.requireNonNull(extraData, "Extra Data cannot be null");
    final var blockEntity = playerInv.player.level().getBlockEntity(extraData.readBlockPos());
    if (blockEntity instanceof CropFarmBlockEntity be) {
      return be;
    }
    throw new IllegalStateException("Block entity is not correct type! " + blockEntity);
  }
  
  
  public CropFarmMenu(int containerId, @NotNull Inventory playerInv, CropFarmBlockEntity blockEntity) {
    this(containerId, playerInv, blockEntity, new SimpleContainerData(2));
  }
  
  public CropFarmMenu(int containerId, @NotNull Inventory playerInv, CropFarmBlockEntity blockEntity, ContainerData data) {
    super(ModRegistry.CROP_FARM_MENU.get(), containerId, playerInv, blockEntity, data);
    addBlockEntitySlots();
  }
  
  @Override
  protected void addBlockEntitySlots() {
    // Access the handlers from the block entity
    IItemHandler inputSlots = blockEntity.getInputSlotsHandler();
    IItemHandler upgradeSlots = blockEntity.getUpgradeSlotsItemHandler();
    IItemHandler outputSlots = blockEntity.getOutputSlotsItemHandler();
    
    // Input Slots
    this.addSlot(new SlotItemHandler(inputSlots, 0, 98, 14)); // Tool
    this.addSlot(new SlotItemHandler(inputSlots, 1, 116, 14)); // Ingredient
    this.addSlot(new SlotItemHandler(inputSlots, 2, 134, 14)); // Base
    
    // Upgrade Slots
    this.addSlot(new SlotItemHandler(upgradeSlots, 0, 98, 34));
    this.addSlot(new SlotItemHandler(upgradeSlots, 1, 116, 34));
    this.addSlot(new SlotItemHandler(upgradeSlots, 2, 134, 34));
    
    // Output Slots
    int outputSlotsYStart = 74;
    for (int j = 0; j < 2; j++) {
      for (int i = 0; i < 9; i++) {
        this.addSlot(new SlotItemHandler(outputSlots, i + j * 9, 8 + i * 18, outputSlotsYStart + j * 18));
      }
    }
  }
  
  @Override
  protected int getBlockEntitySlotCount() {
    return CropFarmBlockEntity.TOTAL_SLOT_COUNT;
  }
  
  @Override
  public boolean stillValid(@NotNull Player player) {
    return stillValid(player, ModRegistry.CROP_FARM_BLOCK.get());
  }
}