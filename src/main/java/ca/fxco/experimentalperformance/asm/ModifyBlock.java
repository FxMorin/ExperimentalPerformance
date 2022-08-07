package ca.fxco.experimentalperformance.asm;

import ca.fxco.experimentalperformance.ExperimentalPerformance;
import com.chocohead.mm.api.ClassTinkerers;

import java.util.List;

public class ModifyBlock implements Runnable {

    List<String> removeFieldsFromBlock = List.of(
            "defaultState",
            "translationKey",
            "cachedItem"
    );

    @Override
    public void run() {
        // net.minecraft.block.Block
        ClassTinkerers.addTransformation("net/minecraft/block/Block", node -> {
            if (ExperimentalPerformance.VERBOSE) {
                node.fields.removeIf(fieldNode -> { // Remove methods (will be moved to BlockInfo)
                    if (this.removeFieldsFromBlock.contains(fieldNode.name)) {
                        System.out.println("Removed `" + fieldNode.name + "` from `Block`");
                        return true;
                    }
                    return false;
                });
            } else {
                // Remove methods (will be moved to BlockInfo)
                node.fields.removeIf(fieldNode -> this.removeFieldsFromBlock.contains(fieldNode.name));
            }
        });
    }
}
