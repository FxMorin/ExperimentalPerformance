package ca.fxco.experimentalperformance.utils;

import ca.fxco.experimentalperformance.ExperimentalPerformance;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.util.Bytecode;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class AsmUtils {

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

    public static FieldNode generateInfoHolderField(String holderClassName) {
        return new FieldNode(Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL, "infoHolder", 'L' + holderClassName + ';', null, null);
    }

    public static void redirectFieldsToInfoHolder(List<MethodNode> methods, String superClass, String targetClass,
                                                  String holderClass, List<String> redirectFields) {
        boolean hasInjectedInfoHolder = false;
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
            if (!hasInjectedInfoHolder) {
                if (methodNode.name.equals("<cinit>") || methodNode.name.equals("<init>")) {
                    hasInjectedInfoHolder = true;
                    Bytecode.DelegateInitialiser delegateInit = Bytecode.findDelegateInit(methodNode, superClass, targetClass);
                    InsnList injectInfoHolder = new InsnList();
                    injectInfoHolder.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    injectInfoHolder.add(new TypeInsnNode(Opcodes.NEW, holderClass));
                    injectInfoHolder.add(new InsnNode(Opcodes.DUP));
                    injectInfoHolder.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, holderClass, "<init>", "()V"));
                    injectInfoHolder.add(new FieldInsnNode(Opcodes.PUTFIELD, targetClass, "infoHolder", 'L' + holderClass + ';'));
                    if (delegateInit.isPresent) {
                        methodNode.instructions.insert(delegateInit.insn, injectInfoHolder);
                    } else {
                        methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), injectInfoHolder);
                    }
                }
            }
        }
    }

    private static void replaceFieldGet(ListIterator<AbstractInsnNode> it, FieldInsnNode fieldInsn,
                                       String newFieldName, String holderClassName) {
        it.add(new FieldInsnNode(Opcodes.GETFIELD, fieldInsn.owner, "infoHolder", 'L' + holderClassName + ';'));
        it.add(new FieldInsnNode(Opcodes.GETFIELD, holderClassName, newFieldName, fieldInsn.desc));
    }

    private static void replaceFieldSet(ListIterator<AbstractInsnNode> it, FieldInsnNode fieldInsn,
                                       String newFieldName, String holderClassName) {
        int size = Type.getType(fieldInsn.desc).getSize();
        if (size == 2) {
            it.add(new InsnNode(Opcodes.DUP2_X1));
            it.add(new InsnNode(Opcodes.POP2));
        } else {
            it.add(new InsnNode(Opcodes.SWAP));
        }
        it.add(new FieldInsnNode(Opcodes.GETFIELD, fieldInsn.owner, "infoHolder", 'L' + holderClassName + ';'));
        if (size == 2) {
            it.add(new InsnNode(Opcodes.DUP_X2));
            it.add(new InsnNode(Opcodes.POP));
        } else {
            it.add(new InsnNode(Opcodes.SWAP));
        }
        it.add(new FieldInsnNode(Opcodes.PUTFIELD, holderClassName, newFieldName, fieldInsn.desc));
    }
}
