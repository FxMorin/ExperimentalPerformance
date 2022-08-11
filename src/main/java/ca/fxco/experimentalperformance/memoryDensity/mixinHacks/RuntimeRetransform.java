package ca.fxco.experimentalperformance.memoryDensity.mixinHacks;

import ca.fxco.experimentalperformance.ExperimentalPerformance;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.io.IOUtils;
import net.bytebuddy.agent.ByteBuddyAgent;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.security.ProtectionDomain;

public final class RuntimeRetransform {

    public static Instrumentation instrumentation;

    public static void retransform(String clazz, AsmClassTransformer transformer) {
        try {
            retransform(Class.forName(clazz), transformer);
        } catch (final ClassNotFoundException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static void retransform(Class<?> clazz, AsmClassTransformer transformer) {
        retransform(clazz, transformer.asRaw());
    }

    public static void retransform(String clazz, BytecodeClassTransformer transformer) {
        try {
            retransform(Class.forName(clazz), transformer);
        } catch (final ClassNotFoundException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static void retransform(Class<?> clazz, BytecodeClassTransformer transformer) {
        try {
            PublicClassFileTransformer fileTransformer = (cl, className, retransformClassName, pd, buffer) -> {
                if (clazz == retransformClassName) return transformer.transform(className, buffer);
                return buffer;
            };
            instrumentation.addTransformer(fileTransformer, true);
            instrumentation.retransformClasses(clazz);
            instrumentation.removeTransformer(fileTransformer);
        } catch (UnmodifiableClassException e) {
            throw new RuntimeException(e);
        }
    }

    public interface PublicClassFileTransformer extends ClassFileTransformer {
        @Override
        byte[] transform(final ClassLoader cl, final String className, final Class<?> retransformClassName,
                         final ProtectionDomain pd, final byte[] buffer);
    }

    static {
        try {
            //GrossFabricHacks - Allows us to retransform already defined classes on the fly
            final String name = ManagementFactory.getRuntimeMXBean().getName();
            final File jar = new File(System.getProperty("user.home"), "experimentalperformance_agent.jar");

            ExperimentalPerformance.LOGGER.info("Attaching instrumentation agent to VM - " + jar.getAbsolutePath());

            IOUtils.write(IOUtils.toByteArray(ExperimentalPerformance.class.getClassLoader().getResource("jars/experimentalperformance_agent.jar")), new FileOutputStream(jar));
            ByteBuddyAgent.attach(jar, name.substring(0, name.indexOf('@')));

            ExperimentalPerformance.LOGGER.info("Successfully attached instrumentation agent.");

            jar.delete();

            final Field field = Class.forName("ca.fxco.experimentalperformance.memoryDensity.mixinHacks.InstrumentationAgent", false, FabricLoader.class.getClassLoader()).getDeclaredField("instrumentation");
            field.setAccessible(true);
            instrumentation = (Instrumentation) field.get(null);
        } catch (final Throwable throwable) {
            ExperimentalPerformance.LOGGER.error("An error occurred during an attempt to attach an instrumentation agent, which might be due to spaces in the path of the game's installation.", throwable);
        }
    }
}
