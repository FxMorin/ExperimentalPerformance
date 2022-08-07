package ca.fxco.experimentalperformance.mixin.memoryDensity;

import ca.fxco.experimentalperformance.memoryDensity.HolderId;
import ca.fxco.experimentalperformance.memoryDensity.infoHolders.BlockInfoHolder;
import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@HolderId("experimentalperformance.Block")
@Mixin(Block.class)
public class Block_memDensityMixin {
    @Unique
    private final BlockInfoHolder infoHolder = new BlockInfoHolder();
}
