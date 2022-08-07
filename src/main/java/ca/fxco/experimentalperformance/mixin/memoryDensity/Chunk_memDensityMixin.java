package ca.fxco.experimentalperformance.mixin.memoryDensity;

import ca.fxco.experimentalperformance.memoryDensity.HolderId;
import ca.fxco.experimentalperformance.memoryDensity.infoHolders.ChunkInfoHolder;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.*;

@HolderId("experimentalperformance.Chunk")
@Mixin(Chunk.class)
public class Chunk_memDensityMixin {
    @Unique
    private final ChunkInfoHolder infoHolder = new ChunkInfoHolder();
}
