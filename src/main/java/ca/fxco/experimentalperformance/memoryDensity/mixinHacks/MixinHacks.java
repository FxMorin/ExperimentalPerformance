package ca.fxco.experimentalperformance.memoryDensity.mixinHacks;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MixinHacks {


    public static void forceApplyAllMixins() {
        try {
            MixinEnvironment currentEnvironment = MixinEnvironment.getCurrentEnvironment();
            Object currentTransformer = currentEnvironment.getActiveTransformer();

            Method transformClassMethod = Class.forName("org.spongepowered.asm.mixin.transformer.MixinTransformer")
                    .getDeclaredMethod("transformClass", MixinEnvironment.class, String.class, byte[].class);
            transformClassMethod.setAccessible(true);

            String className = "name";
            byte[] classBytes = new byte[0];
            byte[] newClassBytes = (byte[])transformClassMethod.invoke(
                    currentTransformer,
                    currentEnvironment,
                    className,
                    classBytes
            );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void forceApplyMixin(Object mixinProcessor, Method applyMixinsMethod, ClassNode classNode, String name) throws InvocationTargetException, IllegalAccessException {


    }
}
