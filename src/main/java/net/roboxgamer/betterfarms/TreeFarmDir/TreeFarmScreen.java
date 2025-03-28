package net.roboxgamer.betterfarms.TreeFarmDir;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.roboxgamer.betterfarms.BetterFarms;
import net.roboxgamer.betterfarms.base.AbstractFarmScreen;
import org.jetbrains.annotations.NotNull;

public class TreeFarmScreen extends AbstractFarmScreen<TreeFarmMenu, TreeFarmBlockEntity> {
  
  private static final ResourceLocation TEXTURE = BetterFarms.location("textures/gui/generic_base_tall.png");
  
  private static final int PROGRESS_BAR_X = 80;
  private static final int PROGRESS_BAR_Y = 35;
  private static final int PROGRESS_BAR_WIDTH = 24;
  private static final int PROGRESS_BAR_HEIGHT = 17;
  
  public TreeFarmScreen(TreeFarmMenu menu, Inventory playerInventory, Component title) {
    super(menu, playerInventory, title);
    // Base constructor already adds 40px height and adjusts inventoryLabelY
  }
  
  @Override
  protected ResourceLocation getBackgroundTexture() {
    return TEXTURE;
  }
  
  
  @Override
  protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
    guiGraphics.blit(getBackgroundTexture(), this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    
    // Draw progress bar if processing
    if (menu.isProcessing()) {
      int progress = menu.getScaledProgress(PROGRESS_BAR_WIDTH); // Get scaled progress from menu
    //  TODO: Implement progress bar rendering
    }
    
    renderSlotBackgrounds(guiGraphics, this.leftPos, this.topPos);
  }
  
  private void renderSlotBackgrounds(GuiGraphics guiGraphics, int relX, int relY) {
    // Input Slots
    renderSingleSlotBg(guiGraphics, relX + 98, relY + 14);
    renderSingleSlotBg(guiGraphics, relX + 116, relY + 14);
    renderSingleSlotBg(guiGraphics, relX + 134, relY + 14);
    
    // Upgrade Slots
    renderSingleSlotBg(guiGraphics, relX + 98, relY + 34);
    renderSingleSlotBg(guiGraphics, relX + 116, relY + 34);
    renderSingleSlotBg(guiGraphics, relX + 134, relY + 34);
    
    // Output Slots
    int outputSlotsYStart = 74;
    for (int j = 0; j < 2; j++) {
      for (int i = 0; i < 9; i++) {
        renderSingleSlotBg(guiGraphics, relX + 8 + i * 18, relY + outputSlotsYStart + j * 18);
      }
    }
  }
  
  private void renderSingleSlotBg(GuiGraphics guiGraphics, int x, int y) {
    ResourceLocation location = ResourceLocation.withDefaultNamespace("container/slot");
    TextureAtlasSprite sprite = Minecraft.getInstance().getGuiSprites().getSprite(location);
    guiGraphics.blit(x - 1, y - 1, 0, 18, 18, sprite);
  }
}
