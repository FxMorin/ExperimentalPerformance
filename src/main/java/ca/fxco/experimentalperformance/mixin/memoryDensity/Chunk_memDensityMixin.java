package ca.fxco.experimentalperformance.mixin.memoryDensity;

import ca.fxco.experimentalperformance.memoryDensity.infoHolders.ChunkInfoHolder;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.*;

@Mixin(Chunk.class)
public class Chunk_memDensityMixin {
    @Unique
    private final ChunkInfoHolder infoHolder = new ChunkInfoHolder();
}
