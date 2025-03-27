package net.roboxgamer.betterfarms;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public record ChanceResult(ItemStack stack, float chance) {
  public static final ChanceResult EMPTY = new ChanceResult(ItemStack.EMPTY, 1);
  public static final Codec<ChanceResult> CODEC = RecordCodecBuilder.create(inst -> inst.group(
      ItemStack.CODEC.fieldOf("item").forGetter(ChanceResult::stack),
      Codec.FLOAT.optionalFieldOf("chance", 1.0f).forGetter(ChanceResult::chance)
  ).apply(inst, ChanceResult::new));
  
  
  public static final StreamCodec<RegistryFriendlyByteBuf, ChanceResult> STREAM_CODEC =
      StreamCodec.composite(
          ItemStack.STREAM_CODEC,      // Codec for the ItemStack field
          ChanceResult::stack,         // Getter for the stack field
          
          ByteBufCodecs.FLOAT,         // Codec for the float field
          ChanceResult::chance,        // Getter for the chance field
          
          ChanceResult::new            // Constructor function to create a new instance from decoded fields
      );
  
  
  public ItemStack rollOutput(RandomSource rand) {
    int outputAmount = stack.getCount();
    for (int roll = 0; roll < stack.getCount(); roll++)
      if (rand.nextFloat() > chance)
        outputAmount--;
    if (outputAmount == 0)
      return ItemStack.EMPTY;
    ItemStack out = stack.copy();
    out.setCount(outputAmount);
    return out;
  }
}
