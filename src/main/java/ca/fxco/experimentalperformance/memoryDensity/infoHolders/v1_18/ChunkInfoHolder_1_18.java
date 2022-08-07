package ca.fxco.experimentalperformance.memoryDensity.infoHolders.v1_18;

import ca.fxco.experimentalperformance.memoryDensity.infoHolders.ChunkInfoHolder;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;

public class ChunkInfoHolder_1_18 extends ChunkInfoHolder {
    @Nullable
    private RegistryEntry<Biome> biome;
}
