package net.roboxgamer.betterfarms.TreeFarmDir;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.roboxgamer.betterfarms.ModRegistry;
import net.roboxgamer.betterfarms.base.AbstractFarmBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TreeFarmBlock extends AbstractFarmBlock {
  
  public TreeFarmBlock(Properties properties) {
    super(properties);
  }
  
  @Override
  public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
    return new TreeFarmBlockEntity(blockPos, blockState);
  }
  
  @Override
  public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> blockEntityType) {
    if (level.isClientSide()) {
      return null;
    }
    return createTickerHelper(blockEntityType, ModRegistry.TREE_FARM_BLOCK_ENTITY.get(),
                              (lvl, pos, blockState, blockEntity) -> blockEntity.tick(lvl, pos, blockState));
  }
}