package ca.fxco.experimentalperformance.asm;

import ca.fxco.experimentalperformance.infoHolders.BlockInfoHolder;
import ca.fxco.experimentalperformance.utils.AsmUtils;
import org.objectweb.asm.Type;

import java.util.List;

public class ModifyBlock implements Runnable {

    @Override
    public void run() {
        final String targetClass = "net/minecraft/block/Block";
        final String holderClass = Type.getInternalName(BlockInfoHolder.class);
        AsmUtils.applyInfoHolder(targetClass, holderClass, List.of(
                "defaultState",
                "translationKey",
                "cachedItem"
        ));
    }
}
