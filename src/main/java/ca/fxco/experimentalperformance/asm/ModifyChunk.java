package ca.fxco.experimentalperformance.asm;

import ca.fxco.experimentalperformance.ExperimentalPerformance;
import com.chocohead.mm.api.ClassTinkerers;
import org.objectweb.asm.tree.InsnList;

import java.util.List;

public class ModifyChunk implements Runnable {

    List<String> removeFieldsFromChunk = List.of(
            "inhabitedTime",
            "blendingData",
            "upgradeData",
            "generationSettings",
            "chunkNoiseSampler"
    );

    @Override
    public void run() {
        // net.minecraft.world.chunk.Chunk
        ClassTinkerers.addTransformation("net/minecraft/world/chunk/Chunk", node -> {
            if (ExperimentalPerformance.VERBOSE) {
                node.fields.removeIf(fieldNode -> { // Remove methods (will be moved to ChunkInfo)
                    if (this.removeFieldsFromChunk.contains(fieldNode.name)) {
                        System.out.println("Removed `" + fieldNode.name + "` from `Chunk`");
                        return true;
                    }
                    return false;
                });
            } else {
                // Remove methods (will be moved to ChunkInfo)
                node.fields.removeIf(fieldNode -> this.removeFieldsFromChunk.contains(fieldNode.name));
            }
            InsnList constInsn = node.methods.get(0).instructions;
            for (int i = 68; i >= 64; i--) // Get rid of blendingData
                constInsn.remove(constInsn.get(i));
            print("Removed `blendingData` instructions from `Chunk` constructor");
            for (int i = 56; i >= 52; i--) // Get rid of inhabitedTime
                constInsn.remove(constInsn.get(i));
            print("Removed `inhabitedTime` instructions from `Chunk` constructor");
            for (int i = 40; i >= 36; i--) // Get rid of upgradeData
                constInsn.remove(constInsn.get(i));
            print("Removed `upgradeData` instructions from `Chunk` constructor");
            //NodeUtils.printInstructions(constInsn, true);
        });
    }

    public static void print(String msg) {
        if (ExperimentalPerformance.VERBOSE)
            System.out.println(msg);
    }
}