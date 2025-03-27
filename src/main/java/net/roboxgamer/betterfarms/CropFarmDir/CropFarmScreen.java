package net.roboxgamer.betterfarms.CropFarmDir;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.roboxgamer.betterfarms.BetterFarms;
import org.jetbrains.annotations.NotNull;

public class CropFarmScreen extends AbstractContainerScreen<CropFarmMenu> {
  public CropFarmScreen(CropFarmMenu menu, Inventory playerInventory, Component title) {
    super(menu, playerInventory, title);
    this.imageHeight += 40;
    this.inventoryLabelY += 40;
  }
  
  @Override
  protected void init() {
    super.init();
  }
  
  @Override
  protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
    guiGraphics.blit(BetterFarms.location("textures/gui/crop_farm_screen.png"), this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    
    // Input Slots
    renderSlotBg(guiGraphics,98,14);
    renderSlotBg(guiGraphics,116,14);
    renderSlotBg(guiGraphics,134,14);
    
    
    
    // Extra Slots
    renderSlotBg(guiGraphics,98,34);
    renderSlotBg(guiGraphics,116,34);
    renderSlotBg(guiGraphics,134,34);
    
    
    // Upgrade Slots
    renderSlotBg(guiGraphics,98,54);
    renderSlotBg(guiGraphics,116,54);
    renderSlotBg(guiGraphics,134,54);
    
    // Output Slots
    int outputSlotsYStart = 74;
    for (int j = 0; j < 2; j++) {
      for (int i = 0; i < 9; i++) {
        renderSlotBg(guiGraphics, 8 + i * 18, outputSlotsYStart + j * 18);
      }
    }
  }
  
  private void renderSlotBg(GuiGraphics guiGraphics, int x, int y) {
    var location = ResourceLocation.withDefaultNamespace("container/slot");
    var sprite = Minecraft.getInstance().getGuiSprites().getSprite(location);
    guiGraphics.blit(this.leftPos + x - 1,this.topPos + y - 1,0,18,18,sprite);
  }
  
  public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    super.render(guiGraphics, mouseX, mouseY, partialTick);
    this.renderTooltip(guiGraphics, mouseX, mouseY);
  }
}
