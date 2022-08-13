package ca.fxco.experimentalperformance.memoryDensity.mixinHacks;

import java.security.SecureClassLoader;

import static ca.fxco.experimentalperformance.utils.GeneralUtils.formatPathDot;

/**
 Used for isolated class loading,
 in order to get around Mixin & Fabric complaining about the class having already been loaded.
 */
public class HacktasticClassLoader extends SecureClassLoader {

    public Class<?> defineClass(String className, byte[] classBytes) {
        if (classBytes.length == 0) return null;
        return defineClass(formatPathDot(className), classBytes, 0, classBytes.length);
    }
}
