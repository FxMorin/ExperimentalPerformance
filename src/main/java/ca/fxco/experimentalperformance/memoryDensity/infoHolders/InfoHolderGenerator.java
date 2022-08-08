package ca.fxco.experimentalperformance.memoryDensity.infoHolders;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.lang.invoke.MethodHandles;
import java.util.List;

public class InfoHolderGenerator {

    public InfoHolderGenerator() {}

    public Class<?> createInfoHolder(ClassNode targetClassNode, String holderClassName, List<String> redirectFields) {
        ClassNode node = new ClassNode();
        node.name = holderClassName;
        node.superName = "java/lang/Object";
        node.version = Opcodes.V17;
        node.access = Opcodes.ACC_PUBLIC;
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        node.accept(cw);
        try {
            for (String fieldName : redirectFields) {
                for (FieldNode fieldNode : targetClassNode.fields) {
                    if (fieldNode.name.equals(fieldName)) {
                        node.fields.add(new FieldNode(
                                Opcodes.ACC_PUBLIC,
                                fieldName,
                                fieldNode.desc,
                                fieldNode.signature,
                                fieldNode.value
                        ));
                        break;
                    }
                }
            }
            addConstructor(cw);
            return MethodHandles.lookup().in(InfoHolderGenerator.class).defineClass(cw.toByteArray());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void addConstructor(ClassWriter cw) {
        GeneratorAdapter ga = new GeneratorAdapter(
                Opcodes.ACC_PUBLIC,
                new Method("<init>", Type.VOID_TYPE, new Type[]{}),
                null,
                null,
                cw
        );
        ga.visitCode();
        ga.loadThis();
        ga.invokeConstructor(Type.getObjectType("java/lang/Object"), Method.getMethod("void <init> ()"));
        ga.returnValue();
        ga.endMethod();
        ga.visitEnd();
    }
}
