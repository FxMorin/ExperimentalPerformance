package ca.fxco.experimentalperformance.memoryDensity.mixinHacks;

import ca.fxco.experimentalperformance.ExperimentalPerformance;
import ca.fxco.experimentalperformance.memoryDensity.analysis.ClassAnalysisManager;
import ca.fxco.experimentalperformance.memoryDensity.analysis.FieldReferenceAnalysis;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.transformers.TreeTransformer;

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
        List<ClassNode> classNodes = getAllMixinClassNodes(); // Do reflection to get class bytes
        for (ClassNode classNode : classNodes) {
            this.fieldReferenceAnalysis.scanClassNode(classNode);
        }
        ExperimentalPerformance.LOGGER.info("Identify & Remove HOT fields, they make everyone else look bad!");
        this.fieldReferenceAnalysis.processHotFields();
        this.fieldReferenceAnalysis.processFieldData(classNodes);
        // Pass fieldReferenceAnalysis in order to remove the hot fields from the private field analysis
        this.classAnalysisManager.runBulkClassNodeAnalysis(
                classNodes,
                this.fieldReferenceAnalysis
        );
        // Pass classAnalysisManager in order to get the field sizes and also to scan newly created holders
        this.fieldReferenceAnalysis.generateInfoHolderData(this.classAnalysisManager);
    }

    public static List<ClassNode> getAllMixinClassNodes() {
        try {
            // Get KnotClassDelegate in order to access `getPostMixinClassByteArray()`
            Class<?> knotClassLoaderClass = ExperimentalPerformance.KNOT_CLASS_LOADER.getClass();
            Field delegateField = knotClassLoaderClass.getDeclaredField("delegate");
            delegateField.setAccessible(true);
            Object delegate = delegateField.get(ExperimentalPerformance.KNOT_CLASS_LOADER);
            Method getRawClassBytesMethod = delegate.getClass()
                    .getDeclaredMethod("getRawClassBytes", String.class);
            getRawClassBytesMethod.setAccessible(true);

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

            // Swap configs
            Field configsField = mixinProcessor.getClass().getDeclaredField("configs");
            configsField.setAccessible(true);
            List<Object> configs = (List<Object>)configsField.get(mixinProcessor);

           // List<Object> configsBackup = new ArrayList<>(configs);
            //configs.clear();
            //configs.addAll(pendingConfigs);

            // Get readClass method from MixinTransformer
            Method readClassMethod = TreeTransformer.class
                    .getDeclaredMethod("readClass", String.class, byte[].class, boolean.class);
            readClassMethod.setAccessible(true);

            // Get applyMixins method from MixinProcessor
            Method applyMixinsMethod = mixinProcessor.getClass()
                    .getDeclaredMethod("applyMixins", MixinEnvironment.class, String.class, ClassNode.class);
            applyMixinsMethod.setAccessible(true);

            // Get `unhandledTargets` for each pendingConfig
            Map<String, ClassNode> classNodes = new HashMap<>();
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
                for (String className : targets) {
                    if (!classNodes.containsKey(className)) {
                        try {
                            byte[] rawBytes = (byte[]) getRawClassBytesMethod.invoke(delegate, className);
                            ClassNode classNode = (ClassNode) readClassMethod.invoke(
                                    currentTransformer, className, rawBytes, false
                            );
                            applyMixinsMethod.invoke(mixinProcessor, currentEnvironment, className, classNode);
                            classNodes.put(className, classNode);
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to invoke methods for class: " + className, e);
                        }
                    }
                }
            }
            //configs.clear();
            //configs.addAll(configsBackup);
            ExperimentalPerformance.LOGGER.info("Class nodes gathered: " + classNodes.size());
            return new ArrayList<>(classNodes.values());
        } catch (Exception e) {
            throw new RuntimeException("Unable to generate classBytes", e);
        }
    }
}
