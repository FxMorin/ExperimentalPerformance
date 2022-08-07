package ca.fxco.experimentalperformance.mixin;

import ca.fxco.experimentalperformance.patches.ChunkInfo;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.gen.chunk.BlendingData;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;
import java.util.function.Supplier;

@Mixin(Chunk.class)
public abstract class ChunkMixin {

    @Unique
    private ChunkInfo chunkInfo;

    public ChunkMixin(ChunkInfo chunkInfo) {
        this.chunkInfo = chunkInfo;
        throw new IllegalStateException();
    }


    @Inject(
            method = "<init>(Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/world/chunk/UpgradeData;" +
                    "Lnet/minecraft/world/HeightLimitView;Lnet/minecraft/util/registry/Registry;" +
                    "J[Lnet/minecraft/world/chunk/ChunkSection;Lnet/minecraft/world/gen/chunk/BlendingData;)V",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/chunk/Chunk;sectionArray:[Lnet/minecraft/world/chunk/ChunkSection;",
                    shift = At.Shift.AFTER,
                    ordinal = 0
            )
    )
    private void onInit(ChunkPos pos, UpgradeData upgradeData, HeightLimitView heightLimitView, Registry<Biome> biome,
                        long inhabitedTime, @Nullable ChunkSection[] sectionArrayInitializer,
                        @Nullable BlendingData blendingData, CallbackInfo ci) {
        this.chunkInfo = new ChunkInfo(inhabitedTime, blendingData, upgradeData);
    }

    /**
     * @author FX - PR0CESS
     * @reason Moving into ChunkInfo
     */
    @Overwrite
    public UpgradeData getUpgradeData() {
        return this.chunkInfo.getUpgradeData();
    }

    /**
     * @author FX - PR0CESS
     * @reason Moving into ChunkInfo
     */
    @Deprecated
    @Overwrite
    public GenerationSettings getOrCreateGenerationSettings(Supplier<GenerationSettings> generationSettingsCreator) {
        return this.chunkInfo.getOrCreateGenerationSettings(generationSettingsCreator);
    }

    /**
     * @author FX - PR0CESS
     * @reason Moving into ChunkInfo
     */
    @Overwrite
    public ChunkNoiseSampler getOrCreateChunkNoiseSampler(Function<Chunk, ChunkNoiseSampler> chunkNoiseSamplerCreator) {
        return this.chunkInfo.getOrCreateChunkNoiseSampler((Chunk)(Object)this, chunkNoiseSamplerCreator);
    }

    /**
     * @author FX - PR0CESS
     * @reason Moving into ChunkInfo
     */
    @Overwrite
    public boolean usesOldNoise() {
        return this.chunkInfo.getBlendingData() != null;
    }

    /**
     * @author FX - PR0CESS
     * @reason Moving into ChunkInfo
     */
    @Overwrite
    @Nullable
    public BlendingData getBlendingData() {
        return this.chunkInfo.getBlendingData();
    }

    /**
     * @author FX - PR0CESS
     * @reason Moving into ChunkInfo
     */
    @Overwrite
    public void setBlendingData(@Nullable BlendingData blendingData) {
        this.chunkInfo.setBlendingData(blendingData);
    }

    /**
     * @author FX - PR0CESS
     * @reason Moving into ChunkInfo
     */
    @Overwrite
    public long getInhabitedTime() {
        return this.chunkInfo.getInhabitedTime();
    }

    /**
     * @author FX - PR0CESS
     * @reason Moving into ChunkInfo
     */
    @Overwrite
    public void increaseInhabitedTime(long delta) {
        this.chunkInfo.increaseInhabitedTime(delta);
    }

    /**
     * @author FX - PR0CESS
     * @reason Moving into ChunkInfo
     */
    @Overwrite
    public void setInhabitedTime(long inhabitedTime) {
        this.chunkInfo.setInhabitedTime(inhabitedTime);
    }
}
