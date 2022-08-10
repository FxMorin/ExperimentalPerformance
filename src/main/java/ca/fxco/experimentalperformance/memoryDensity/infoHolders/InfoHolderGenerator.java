package ca.fxco.experimentalperformance.memoryDensity.infoHolders;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.lang.invoke.MethodHandles;
import java.util.List;

import static ca.fxco.experimentalperformance.utils.CommonConst.INIT;
import static ca.fxco.experimentalperformance.utils.CommonConst.OBJECT_PATH;

public class InfoHolderGenerator {

    public InfoHolderGenerator() {}

    public void createInfoHolder(ClassNode targetClassNode, String holderClassName, List<String> redirectFields) {
        ClassNode node = new ClassNode();
        node.name = holderClassName;
        node.superName = OBJECT_PATH;
        node.version = Opcodes.V17;
        node.access = Opcodes.ACC_PUBLIC;
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        node.accept(cw);
        try {
            for (String fieldName : redirectFields) {
                for (FieldNode fieldNode : targetClassNode.fields) {
                    if (fieldNode.name.equals(fieldName)) {
                        cw.visitField(
                                Opcodes.ACC_PUBLIC, fieldName, fieldNode.desc, fieldNode.signature, fieldNode.value
                        ).visitEnd();
                        break;
                    }
                }
            }
            addConstructor(cw);
            MethodHandles.lookup().in(InfoHolderGenerator.class).defineClass(cw.toByteArray());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static void addConstructor(ClassWriter cw) {
        GeneratorAdapter ga = new GeneratorAdapter(
                Opcodes.ACC_PUBLIC,
                new Method(INIT, Type.VOID_TYPE, new Type[]{}),
                null,
                null,
                cw
        );
        ga.visitCode();
        ga.loadThis();
        ga.invokeConstructor(Type.getObjectType(OBJECT_PATH), Method.getMethod("void <init> ()"));
        ga.returnValue();
        ga.endMethod();
        ga.visitEnd();
    }
}
