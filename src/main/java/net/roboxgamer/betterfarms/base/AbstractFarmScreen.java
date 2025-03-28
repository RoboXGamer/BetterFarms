package net.roboxgamer.betterfarms.base;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract base class for farm GUI screens.
 * Handles common setup like increased height adjustments and the main render loop.
 * Subclasses MUST implement renderBg to draw their specific background and dynamic elements.
 *
 * @param <T> The type of AbstractFarmMenu this screen is associated with.
 * @param <E> The type of AbstractFarmBlockEntity the menu interacts with.
 */
public abstract class AbstractFarmScreen<T extends AbstractFarmMenu<E>, E extends AbstractFarmBlockEntity> extends AbstractContainerScreen<T> {
  
  /**
   * Constructor for the abstract farm screen.
   * Adjusts default height and label positions common to farm GUIs.
   *
   * @param menu           The container menu instance.
   * @param playerInventory The player's inventory.
   * @param title          The title of the screen.
   */
  public AbstractFarmScreen(T menu, Inventory playerInventory, Component title) {
    super(menu, playerInventory, title);
    
    // --- Common Adjustments for Taller Farm GUIs ---
    // Assuming all farm GUIs use the extra 40px height like the original CropFarmScreen
    int additionalHeight = 40;
    this.imageHeight += additionalHeight; // Increase the background rendering area height
    this.inventoryLabelY = this.imageHeight - 94; // Move player inventory label down accordingly
    // Title label Y position might also need adjustment depending on layout
    // this.titleLabelY = 6; // Default, adjust if needed
  }
  
  @Override
  protected void init() {
    super.init();
    // Adjust label X positions if needed, typically centered by default.
    // this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
  }
  
  /**
   * Main render method. Calls super render and tooltip rendering.
   */
  @Override
  public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    super.render(guiGraphics, mouseX, mouseY, partialTick);
    this.renderTooltip(guiGraphics, mouseX, mouseY);
  }
  
  /**
   * Renders the background layer of the screen.
   * Subclasses MUST implement this method to draw their specific background texture
   * and any dynamic elements like progress bars or energy meters.
   * <p>
   * Example implementation in subclass:
   * <pre>{@code
   * ResourceLocation texture = getBackgroundTexture();
   * int relX = (this.width - this.imageWidth) / 2; // this.leftPos
   * int relY = (this.height - this.imageHeight) / 2; // this.topPos
   * guiGraphics.blit(texture, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
   * // Draw progress bar if needed
   * if(menu.isProcessing()) {
   *     int progress = menu.getScaledProgress(PROGRESS_BAR_WIDTH);
   *     guiGraphics.blit(texture, relX + PROGRESS_BAR_X, relY + PROGRESS_BAR_Y, UV_PROGRESS_X, UV_PROGRESS_Y, progress, PROGRESS_BAR_HEIGHT);
   * }
   * }</pre>
   *
   * @param guiGraphics The GuiGraphics instance.
   * @param partialTick Partial tick time.
   * @param mouseX      Mouse X position.
   * @param mouseY      Mouse Y position.
   */
  @Override
  protected abstract void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY);
  
  /**
   * Renders the foreground layer (labels).
   * Overridden primarily to ensure correct label positioning if defaults are insufficient.
   * Default implementation draws title and inventory labels based on adjusted coordinates.
   */
  @Override
  protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
    // Render Title Label (uses titleLabelX/Y set in init/constructor)
    guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false); // 4210752
    // Render Player Inventory Label (uses inventoryLabelX/Y set in constructor)
    guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
  }
  
  /**
   * @return The ResourceLocation of the background texture for this specific screen.
   */
  protected abstract ResourceLocation getBackgroundTexture();
  
  // --- Optional Helper Methods ---
  
  /**
   * Example helper to render a standard progress arrow.
   * @param guiGraphics The GuiGraphics instance.
   * @param x Screen X position for the arrow.
   * @param y Screen Y position for the arrow.
   * @param progressScaled Scaled progress value (0 to arrow width).
   * @param u Texture U coordinate for the empty arrow background.
   * @param v Texture V coordinate for the empty arrow background.
   * @param uFilled Texture U coordinate for the filled arrow overlay.
   * @param vFilled Texture V coordinate for the filled arrow overlay.
   * @param width Pixel width of the arrow graphic.
   * @param height Pixel height of the arrow graphic.
   */
  protected void renderProgressArrow(GuiGraphics guiGraphics, int x, int y, int progressScaled, int u, int v, int uFilled, int vFilled, int width, int height) {
    // Draw background part of arrow
    guiGraphics.blit(getBackgroundTexture(), x, y, u, v, width, height);
    // Draw filled part of arrow based on progress
    if (progressScaled > 0) {
      guiGraphics.blit(getBackgroundTexture(), x, y, uFilled, vFilled, progressScaled, height);
    }
  }
  
  /**
   * Gets the Block Entity associated with this screen's menu.
   * @return The AbstractFarmBlockEntity instance.
   */
  protected E getBlockEntity() {
    return this.menu.getBlockEntity();
  }
}
