package ca.fxco.experimentalperformance.memoryDensity.mixinHacks;

import ca.fxco.experimentalperformance.ExperimentalPerformance;
import ca.fxco.experimentalperformance.memoryDensity.analysis.ClassAnalysisManager;
import ca.fxco.experimentalperformance.memoryDensity.analysis.FieldReferenceAnalysis;
import ca.fxco.experimentalperformance.utils.asm.AsmUtils;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.transformer.Config;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PreLoadMixins {

    private final FieldReferenceAnalysis fieldReferenceAnalysis;
    private final ClassAnalysisManager classAnalysisManager;

    public PreLoadMixins() {
        this.fieldReferenceAnalysis = new FieldReferenceAnalysis();
        this.classAnalysisManager = new ClassAnalysisManager();
    }

    public void PreLoadAllMixins() {
        try {
            ExperimentalPerformance.LOGGER.info("Attempting to pre-load all mixin!");
            Class<?> knotClassLoaderClass = ExperimentalPerformance.KNOT_CLASS_LOADER.getClass();
            System.out.println(knotClassLoaderClass.getName());
            Field delegateField = knotClassLoaderClass.getDeclaredField("delegate");
            delegateField.setAccessible(true);
            Object delegate = delegateField.get(ExperimentalPerformance.KNOT_CLASS_LOADER);
            System.out.println(delegate.getClass().getName());
            Method getPostMixinMethod = delegate.getClass()
                    .getDeclaredMethod("getPostMixinClassByteArray", String.class, boolean.class);
            getPostMixinMethod.setAccessible(true);
            Map<String, byte[]> classBytes = new HashMap<>();
            for (Config mixinConfig : Mixins.getConfigs())
                for (String className : mixinConfig.getConfig().getTargets())
                    if (!classBytes.containsKey(className))
                        classBytes.put(className, (byte[])getPostMixinMethod.invoke(delegate, className, false));
            // TODO: Check if some config's failed and if we need to remove any configs from our list
            Map<String, ClassNode> classNodes = new HashMap<>();
            for (Map.Entry<String, byte[]> entry : classBytes.entrySet()) { // Convert to ASM & scan
                ClassNode node = AsmUtils.getClassNodeFromBytecode(entry.getValue());
                this.fieldReferenceAnalysis.scanClassNode(node);
                classNodes.put(entry.getKey(), node);
            }
            ExperimentalPerformance.LOGGER.info("Identify & Remove HOT fields, they make everyone else look bad!");
            this.fieldReferenceAnalysis.processFieldData(classNodes);
            this.fieldReferenceAnalysis.processHotFields();
            // Pass fieldReferenceAnalysis in order to remove the hot fields from the private field analysis
            this.classAnalysisManager.runBulkClassNodeAnalysis(classNodes, this.fieldReferenceAnalysis);
            // Pass classAnalysisManager in order to get the field sizes and also to scan newly created holders
            this.fieldReferenceAnalysis.generateInfoHolderData(this.classAnalysisManager);
        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /*public static void forceApplyAllMixins() {
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
    }*/
}
