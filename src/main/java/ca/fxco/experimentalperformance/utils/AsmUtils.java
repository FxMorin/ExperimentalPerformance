package ca.fxco.experimentalperformance.utils;

import ca.fxco.experimentalperformance.ExperimentalPerformance;
import com.chocohead.mm.api.ClassTinkerers;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class AsmUtils {

    public static void applyInfoHolder(String targetClass, String holderClass, List<String> redirectFields) {
        ClassTinkerers.addTransformation(targetClass, node -> {
            String[] classPath = targetClass.split("/");
            String className = classPath[classPath.length - 1];
            AsmUtils.removeFieldsContaining(className, node.fields, redirectFields);
            AsmUtils.redirectFieldsToInfoHolder(node.methods, targetClass, holderClass, redirectFields);
        });
    }

    public static void removeFieldsContaining(String className, List<FieldNode> fields, List<String> removeFields) {
        final Iterator<FieldNode> each = fields.iterator();
        while (each.hasNext()) {
            String fieldName = each.next().name;
            if (removeFields.contains(fieldName)) {
                each.remove();
                if (ExperimentalPerformance.VERBOSE)
                    System.out.println("Removed `" + fieldName + "` from `" + className + "`");
            }
        }
    }

    public static void redirectFieldsToInfoHolder(List<MethodNode> methods, String targetClass,
                                                  String holderClass, List<String> redirectFields) {
        for (MethodNode methodNode : methods) {
            for (ListIterator<AbstractInsnNode> it = methodNode.instructions.iterator(); it.hasNext(); ) {
                AbstractInsnNode insn = it.next();
                if (insn instanceof FieldInsnNode fieldInsn && fieldInsn.owner.equals(targetClass)) {
                    for (String name : redirectFields) {
                        if (fieldInsn.name.equals(name)) {
                            it.remove();
                            if (fieldInsn.getOpcode() == Opcodes.GETFIELD) {
                                replaceFieldGet(it, fieldInsn, name, holderClass);
                            } else {
                                replaceFieldSet(it, fieldInsn, name, holderClass);
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    private static void replaceFieldGet(ListIterator<AbstractInsnNode> it, FieldInsnNode fieldInsn,
                                       String newFieldName, String holderClass) {
        it.add(new FieldInsnNode(Opcodes.GETFIELD, fieldInsn.owner, "infoHolder", 'L' + holderClass + ';'));
        it.add(new FieldInsnNode(Opcodes.GETFIELD, holderClass, newFieldName, fieldInsn.desc));
    }

    private static void replaceFieldSet(ListIterator<AbstractInsnNode> it, FieldInsnNode fieldInsn,
                                       String newFieldName, String holderClass) {
        var size = Type.getType(fieldInsn.desc).getSize();
        if (size == 2) {
            it.add(new InsnNode(Opcodes.DUP2_X1));
            it.add(new InsnNode(Opcodes.POP2));
        } else {
            it.add(new InsnNode(Opcodes.SWAP));
        }
        it.add(new FieldInsnNode(Opcodes.GETFIELD, fieldInsn.owner, "infoHolder", 'L' + holderClass + ';'));
        if (size == 2) {
            it.add(new InsnNode(Opcodes.DUP_X2));
            it.add(new InsnNode(Opcodes.POP));
        } else {
            it.add(new InsnNode(Opcodes.SWAP));
        }
        it.add(new FieldInsnNode(Opcodes.PUTFIELD, holderClass, newFieldName, fieldInsn.desc));
    }
}
