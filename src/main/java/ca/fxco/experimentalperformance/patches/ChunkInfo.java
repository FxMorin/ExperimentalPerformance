package ca.fxco.experimentalperformance.patches;

import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.gen.chunk.BlendingData;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

public class ChunkInfo {

    private long inhabitedTime;
    @Nullable
    private GenerationSettings generationSettings;
    @Nullable
    protected ChunkNoiseSampler chunkNoiseSampler;
    @Nullable
    protected BlendingData blendingData;
    protected final UpgradeData upgradeData;

    public ChunkInfo(long inhabitedTime, @Nullable BlendingData blendingData, UpgradeData upgradeData) {
        this.inhabitedTime = inhabitedTime;
        this.blendingData = blendingData;
        this.upgradeData = upgradeData;
    }

    public UpgradeData getUpgradeData() {
        return this.upgradeData;
    }

    @Nullable
    public BlendingData getBlendingData() {
        return this.blendingData;
    }

    public void setBlendingData(@Nullable BlendingData blendingData) {
        this.blendingData = blendingData;
    }

    public long getInhabitedTime() {
        return this.inhabitedTime;
    }

    public void increaseInhabitedTime(long delta) {
        this.inhabitedTime += delta;
    }

    public void setInhabitedTime(long inhabitedTime) {
        this.inhabitedTime = inhabitedTime;
    }

    public GenerationSettings getOrCreateGenerationSettings(Supplier<GenerationSettings> generationSettingsCreator) {
        if (this.generationSettings == null)
            this.generationSettings = generationSettingsCreator.get();
        return this.generationSettings;
    }

    public ChunkNoiseSampler getOrCreateChunkNoiseSampler(Chunk chunk, Function<Chunk, ChunkNoiseSampler> chunkNoiseSamplerCreator) {
        if (this.chunkNoiseSampler == null)
            this.chunkNoiseSampler = chunkNoiseSamplerCreator.apply(chunk);
        return this.chunkNoiseSampler;
    }
}
