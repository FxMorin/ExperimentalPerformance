package ca.fxco.experimentalperformance.mixin;

import ca.fxco.experimentalperformance.infoHolders.ChunkInfoHolder;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.*;

@Mixin(Chunk.class)
public class ChunkMixin {
    @Unique
    private final ChunkInfoHolder infoHolder = new ChunkInfoHolder();
}
