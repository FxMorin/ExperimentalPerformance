package ca.fxco.experimentalperformance.mixin.memoryDensity;

import ca.fxco.experimentalperformance.memoryDensity.HolderDataRegistry;
import ca.fxco.experimentalperformance.memoryDensity.HolderId;
import ca.fxco.experimentalperformance.memoryDensity.infoHolders.v1_18.ChunkInfoHolder_1_18;
import ca.fxco.experimentalperformance.memoryDensity.infoHolders.v1_19.ChunkInfoHolder_1_19;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.*;

@HolderId("experimentalperformance.Chunk")
@Restriction(require = @Condition(value = HolderDataRegistry.MINECRAFT_ID, versionPredicates = "1.19.x"))
@Mixin(Chunk.class)
class Chunk_1_19_memDensityMixin {
    @Unique
    private final ChunkInfoHolder_1_19 infoHolder = new ChunkInfoHolder_1_19();
}

@HolderId("experimentalperformance.Chunk")
@Restriction(require = @Condition(value = HolderDataRegistry.MINECRAFT_ID, versionPredicates = "1.18.x"))
@Mixin(Chunk.class)
class Chunk_1_18_memDensityMixin {
    @Unique
    private final ChunkInfoHolder_1_18 infoHolder = new ChunkInfoHolder_1_18();
}
