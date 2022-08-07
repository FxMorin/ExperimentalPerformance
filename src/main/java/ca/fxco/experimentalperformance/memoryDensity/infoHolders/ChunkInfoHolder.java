package ca.fxco.experimentalperformance.memoryDensity.infoHolders;

import net.minecraft.structure.StructureStart;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.gen.chunk.BlendingData;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.structure.Structure;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ChunkInfoHolder {
    // TODO: Add support to make these final if the original value is final,
    //  than create constructor argument to pass value through
    public long inhabitedTime;
    @Nullable
    public GenerationSettings generationSettings;
    @Nullable
    public ChunkNoiseSampler chunkNoiseSampler;
    @Nullable
    public BlendingData blendingData;
    public Map<Structure, StructureStart> structureStarts;
}
