package ca.fxco.experimentalperformance.asm;

import ca.fxco.experimentalperformance.ExperimentalPerformance;
import ca.fxco.experimentalperformance.utils.NodeUtils;
import com.chocohead.mm.api.ClassTinkerers;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

public class ModifyEntity implements Runnable {

    List<String> removeFieldsFromEntity = List.of(
            "submergedFluidTag",
            "trackedPosition",
            "portalCooldown",
            "pistonMovementTick",
            "lastChimeIntensity",
            "lastChimeAge"
    );

    @Override
    public void run() {
        // net.minecraft.entity.Entity
        ClassTinkerers.addTransformation("net/minecraft/entity/Entity", node -> {
            if (ExperimentalPerformance.VERBOSE) {
                node.fields.removeIf(fieldNode -> { // Remove methods (will be moved to EntityInfo)
                    if (this.removeFieldsFromEntity.contains(fieldNode.name)) {
                        System.out.println("Removed `" + fieldNode.name + "` from `Entity`");
                        return true;
                    }
                    return false;
                });
            } else {
                // Remove methods (will be moved to EntityInfo)
                node.fields.removeIf(fieldNode -> this.removeFieldsFromEntity.contains(fieldNode.name));
            }
            // Pure ASM, doing the dirty work that Mixin can't
            byte count4 = 0;
            for (MethodNode methodNode : node.methods) {
                if (methodNode.name.equals("writeNbt")) {
                    InsnList methodInsn = methodNode.instructions;
                    for (int i = 154; i >= 148; i--) // Remove `nbt.putInt("PortalCooldown", this.portalCooldown)`
                        methodInsn.remove(methodInsn.get(i));
                    count4++;
                }
                if (methodNode.name.equals("readNbt")) {
                    InsnList methodInsn = methodNode.instructions;
                    for (int i = 197; i >= 191; i--) // Remove `this.portalCooldown = nbt.getInt("PortalCooldown")`
                        methodInsn.remove(methodInsn.get(i));
                    count4++;
                }
                if (methodNode.name.equals("adjustMovementForPiston")) {
                    InsnList methodInsn = methodNode.instructions;
                    for (int i = 36; i >= 19; i--) // Remove incorrect code
                        methodInsn.remove(methodInsn.get(i));
                    count4++;
                }
                if (methodNode.name.equals("updateSubmergedInWaterState")) {
                    InsnList methodInsn = methodNode.instructions;
                    for (int i = 101; i >= 85; i--) // Remove `if (e > d) fluidState.streamTags().forEach(this.submergedFluidTag::add)`
                        methodInsn.remove(methodInsn.get(i));
                    for (int i = 12; i >= 8; i--) // Remove `this.submergedFluidTag.clear();`
                        methodInsn.remove(methodInsn.get(i));
                    count4++;
                }
                if (count4 >= 4) break;
            }
            InsnList constInsn = node.methods.get(0).instructions;
            for (int i = 79; i >= 73; i--) // Remove cinit `trackedPosition`
                constInsn.remove(constInsn.get(i));
            for (int i = 62; i >= 56; i--) // Remove cinit `submergedFluidTag`
                constInsn.remove(constInsn.get(i));
        });
    }
}
