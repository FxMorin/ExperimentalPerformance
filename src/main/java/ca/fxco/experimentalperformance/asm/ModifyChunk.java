package ca.fxco.experimentalperformance.asm;

import ca.fxco.experimentalperformance.infoHolders.ChunkInfoHolder;
import ca.fxco.experimentalperformance.utils.AsmUtils;
import org.objectweb.asm.Type;

import java.util.List;

public class ModifyChunk implements Runnable {

    @Override
    public void run() {
        final String targetClass = "net/minecraft/world/chunk/Chunk";
        final String holderClass = Type.getInternalName(ChunkInfoHolder.class);
        AsmUtils.applyInfoHolder(targetClass, holderClass, List.of(
                "inhabitedTime",
                "blendingData",
                "structureStarts",
                "generationSettings",
                "chunkNoiseSampler"
        ));
    }
}