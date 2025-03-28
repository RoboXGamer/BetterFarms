package net.roboxgamer.betterfarms.CropFarmDir;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.roboxgamer.betterfarms.ModRegistry;
import net.roboxgamer.betterfarms.base.AbstractFarmBlock;

public class CropFarmBlock extends AbstractFarmBlock {
  
  public CropFarmBlock(Properties properties) {
    super(properties);
  }
  
  @Override
  public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
    return new CropFarmBlockEntity(blockPos, blockState);
  }
  
  @Override
  public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> blockEntityType) {
    if (level.isClientSide()) {
      return null;
    }
    return createTickerHelper(blockEntityType, ModRegistry.CROP_FARM_BLOCK_ENTITY.get(),
                              (lvl, pos, blockState, blockEntity) -> blockEntity.tick(lvl, pos, blockState));
  }
}