package ca.fxco.experimentalperformance.memoryDensity.mixinHacks;

import ca.fxco.experimentalperformance.ExperimentalPerformance;
import ca.fxco.experimentalperformance.memoryDensity.analysis.ClassAnalysisManager;
import ca.fxco.experimentalperformance.memoryDensity.analysis.FieldReferenceAnalysis;
import ca.fxco.experimentalperformance.utils.asm.AsmUtils;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import static ca.fxco.experimentalperformance.ExperimentalPerformance.MODID;

public class PreLoadMixins {

    private final FieldReferenceAnalysis fieldReferenceAnalysis;
    private final ClassAnalysisManager classAnalysisManager;

    public PreLoadMixins() {
        this.fieldReferenceAnalysis = new FieldReferenceAnalysis();
        this.classAnalysisManager = new ClassAnalysisManager();
    }

    public void PreLoadAllMixins() {
        ExperimentalPerformance.LOGGER.info("Attempting to pre-load all mixin!");

        // TODO: Check if some config's failed and if we need to remove any configs from our list
        Map<String, byte[]> classBytes = getAllMixinClassBytes(); // Do reflection to get class bytes
        Map<String, ClassNode> classNodes = new HashMap<>();
        for (Map.Entry<String, byte[]> entry : classBytes.entrySet()) { // Convert to ASM & scan
            ClassNode node = AsmUtils.getClassNodeFromBytecode(entry.getValue());
            this.fieldReferenceAnalysis.scanClassNode(node);
            classNodes.put(node.name, node);
        }
        ExperimentalPerformance.LOGGER.info("Identify & Remove HOT fields, they make everyone else look bad!");
        this.fieldReferenceAnalysis.processHotFields();
        this.fieldReferenceAnalysis.processFieldData(classNodes, classBytes);
        // Pass fieldReferenceAnalysis in order to remove the hot fields from the private field analysis
        this.classAnalysisManager.runBulkClassNodeAnalysis(classNodes, classBytes, this.fieldReferenceAnalysis);
        // Pass classAnalysisManager in order to get the field sizes and also to scan newly created holders
        this.fieldReferenceAnalysis.generateInfoHolderData(this.classAnalysisManager);
    }

    public static Map<String, byte[]> getAllMixinClassBytes() {
        try {
            // Get KnotClassDelegate in order to access `getPostMixinClassByteArray()`
            Class<?> knotClassLoaderClass = ExperimentalPerformance.KNOT_CLASS_LOADER.getClass();
            Field delegateField = knotClassLoaderClass.getDeclaredField("delegate");
            delegateField.setAccessible(true);
            Object delegate = delegateField.get(ExperimentalPerformance.KNOT_CLASS_LOADER);
            Method getPostMixinMethod = delegate.getClass()
                    .getDeclaredMethod("getPostMixinClassByteArray", String.class, boolean.class);
            getPostMixinMethod.setAccessible(true);

            // Get MixinProcessor in order to access `pendingConfigs`
            MixinEnvironment currentEnvironment = MixinEnvironment.getCurrentEnvironment();
            Object currentTransformer = currentEnvironment.getActiveTransformer();
            Field processorField = currentTransformer.getClass().getDeclaredField("processor");
            processorField.setAccessible(true);
            Object mixinProcessor = processorField.get(currentTransformer);
            Field pendingConfigsField = mixinProcessor.getClass().getDeclaredField("pendingConfigs");
            pendingConfigsField.setAccessible(true);
            List<Object> pendingConfigs = (List<Object>)pendingConfigsField.get(mixinProcessor);
            ExperimentalPerformance.LOGGER.info("Mixin configs to scan: " + pendingConfigs.size());

            // Get `unhandledTargets` for each pendingConfig
            Map<String, byte[]> classBytes = new HashMap<>();
            for (Object mixinConfig : pendingConfigs) {
                // Make sure it's not this mod's config, you skip that one
                Field nameField = mixinConfig.getClass().getDeclaredField("name");
                nameField.setAccessible(true);
                String mixinFileName = (String)nameField.get(mixinConfig);
                if (mixinFileName.equals(MODID + ".mixins.json")) continue;
                ExperimentalPerformance.LOGGER.info("MixinConfigName: " + mixinFileName);
                // Get unhandledTargets so that we can go through them first
                Field unhandledTargetsField = mixinConfig.getClass().getDeclaredField("unhandledTargets");
                //Method getTargetsMethod = mixinConfig.getClass().getDeclaredMethod("getTargets");
                //getTargetsMethod.setAccessible(true);
                unhandledTargetsField.setAccessible(true);
                Set<String> targets = (Set<String>) unhandledTargetsField.get(mixinConfig);
                //Set<String> targets = (Set<String>) getTargetsMethod.invoke(mixinConfig);
                ExperimentalPerformance.LOGGER.info("Targets Found: " + targets.size());
                for (String className : targets)
                    if (!classBytes.containsKey(className))
                        classBytes.put(className, (byte[]) getPostMixinMethod.invoke(delegate, className, false));
            }
            ExperimentalPerformance.LOGGER.info("Class bytes gathered: " + classBytes.size());
            return classBytes;
        } catch (Exception e) {
            throw new RuntimeException("Unable to generate classBytes", e);
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
