package ca.fxco.experimentalperformance.mixin;

import ca.fxco.experimentalperformance.infoHolders.BlockInfoHolder;
import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Block.class)
public class BlockMixin {
    @Unique
    private final BlockInfoHolder infoHolder = new BlockInfoHolder();
}
