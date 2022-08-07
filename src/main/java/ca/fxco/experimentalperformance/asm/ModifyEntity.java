package ca.fxco.experimentalperformance.asm;

import ca.fxco.experimentalperformance.infoHolders.EntityInfoHolder;
import ca.fxco.experimentalperformance.utils.AsmUtils;
import org.objectweb.asm.Type;

import java.util.List;

public class ModifyEntity implements Runnable {

    @Override
    public void run() {
        final String targetClass = "net/minecraft/entity/Entity";
        final String holderClass = Type.getInternalName(EntityInfoHolder.class);
        AsmUtils.applyInfoHolder(targetClass, holderClass, List.of(
                "submergedFluidTag",
                "trackedPosition",
                "portalCooldown",
                "pistonMovementTick",
                "lastChimeIntensity",
                "lastChimeAge"
        ));
    }
}
