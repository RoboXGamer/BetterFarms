package net.roboxgamer.betterfarms;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.roboxgamer.betterfarms.CropFarmDir.CropFarmScreen;
import net.roboxgamer.betterfarms.TreeFarmDir.TreeFarmScreen;
import net.roboxgamer.betterfarms.base.AbstractFarmBlockEntity;
import org.slf4j.Logger;

@Mod(net.roboxgamer.betterfarms.BetterFarms.MODID)
public class BetterFarms {
  public static final String MODID = "betterfarms";
  private static final Logger LOGGER = LogUtils.getLogger();
  
  public static ResourceLocation location(String path){
    return ResourceLocation.fromNamespaceAndPath(MODID, path);
  }
  
  public BetterFarms(IEventBus modEventBus, ModContainer modContainer) {
    NeoForge.EVENT_BUS.register(this);
    
    ModRegistry.register(modEventBus);
    
    modEventBus.addListener(this::registerCapabilities);
    modEventBus.addListener(this::registerScreens);
  }
  
  private void registerCapabilities(RegisterCapabilitiesEvent event) {
    event.registerBlockEntity(
        Capabilities.ItemHandler.BLOCK,
        ModRegistry.CROP_FARM_BLOCK_ENTITY.get(),
        AbstractFarmBlockEntity::getCapabilityHandler);
    event.registerBlockEntity(
        Capabilities.ItemHandler.BLOCK,
        ModRegistry.TREE_FARM_BLOCK_ENTITY.get(),
        AbstractFarmBlockEntity::getCapabilityHandler);
  }
  
  private void registerScreens(RegisterMenuScreensEvent event) {
    event.register(ModRegistry.CROP_FARM_MENU.get(), CropFarmScreen::new);
    event.register(ModRegistry.TREE_FARM_MENU.get(), TreeFarmScreen::new);
  }
  
  @SubscribeEvent
  public void onServerStarting(ServerStartingEvent event) {
    // Do something when the server starts
    LOGGER.info("HELLO from server starting");
  }
  
  @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
  public static class ClientModEvents {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
      // Some client setup code
      LOGGER.info("HELLO FROM CLIENT SETUP");
      LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }
  }
}
