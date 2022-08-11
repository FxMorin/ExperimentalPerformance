package ca.fxco.experimentalperformance;

import ca.fxco.experimentalperformance.memoryDensity.analysis.ClassAnalysis;
import ca.fxco.experimentalperformance.memoryDensity.analysis.ClassAnalysisManager;
import ca.fxco.experimentalperformance.memoryDensity.analysis.FieldReferenceAnalysis;
import ca.fxco.experimentalperformance.memoryDensity.mixinHacks.RuntimeRetransform;
import net.devtech.grossfabrichacks.unsafe.UnsafeUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.logging.LoggerAdapterDefault;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static ca.fxco.experimentalperformance.utils.CommonConst.CLASS_EXT;
import static ca.fxco.experimentalperformance.utils.GeneralUtils.formatPathSlash;

public class ExperimentalPerformance implements ModInitializer {

    /*
     TODO:
        - Redo the README, It really hard to follow and the allocation descriptions most just describe the allocations in general
        - World is 104, make a WorldInfo consisting of all weather and dimension values. Which would bring it down to 64
        - Entity is 272 (4.25 -> 5 cache lines) Entities not only take up a massive amount,
          they also transfer that massive amount all over the place.
          Currently the mod reduces it to: 252
     */

    public static final String MODID = "experimentalperformance";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    //public static final SimpleConfig CONFIG = new SimpleConfig();

    public static ClassAnalysisManager classAnalysisManager = new ClassAnalysisManager(false);
    public static FieldReferenceAnalysis fieldReferenceAnalysis = new FieldReferenceAnalysis();

    public static final boolean VERBOSE = true;

    @Override
    public void onInitialize() {}

    public static void afterForceLoad() {
        classAnalysisManager.runAllMcClasses(); // Needs classes to be loaded
        ClassAnalysisManager.ResultConditions conditions = new ClassAnalysisManager.ResultConditions(65);
        List<ClassAnalysis.AnalysisResults> results = classAnalysisManager.getResults(conditions);
        fieldReferenceAnalysis.applyChanges(results); // Now that everything is loaded, scan field references
        fieldReferenceAnalysis = null;
    }

    static {

        final String[] forceDefineClasses = {
                "ca.fxco.experimentalperformance.memoryDensity.mixinHacks.InstrumentationAgent",
                "ca.fxco.experimentalperformance.memoryDensity.mixinHacks.RuntimeRetransform"
        };

        final ClassLoader mcClassLoader = FabricLoader.class.getClassLoader();
        for (final String clazz : forceDefineClasses) {
            UnsafeUtil.findAndDefineClass(clazz, mcClassLoader);
        }
    }

    public static void forceLoadAllMixins() {
        LOGGER.info("[ExperimentalPerformance] Attempting to ForceLoad All Mixins");
        silenceAuditLogger();
        MixinEnvironment.getCurrentEnvironment().audit();
        LOGGER.info("[ExperimentalPerformance] Done ForceLoad");
        afterForceLoad();
    }

    private static Class<?> getMixinLoggerClass() throws ClassNotFoundException {
        try {
            return Class.forName("net.fabricmc.loader.impl.launch.knot.MixinLogger");
        } catch (ClassNotFoundException err) {
            return Class.forName("org.quiltmc.loader.impl.launch.knot.MixinLogger");
        }
    }

    private static void silenceAuditLogger() {
        try {
            Field loggerField = getMixinLoggerClass().getDeclaredField("LOGGER_MAP");
            loggerField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, ILogger> loggerMap = (Map<String, ILogger>)loggerField.get(null);
            loggerMap.put("mixin.audit", new LoggerAdapterDefault("mixin.audit"));
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }
}
