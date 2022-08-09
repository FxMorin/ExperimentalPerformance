package ca.fxco.experimentalperformance.utils;

import org.objectweb.asm.Type;

public class GeneralUtils {

    public static String getLastPathPart(String path) {
        String[] classPath = path.split("/");
        return classPath[classPath.length - 1];
    }

    public static String formatPathSlash(String path) {
        return path.replace('.', '/');
    }

    public static String formatPathDot(String path) {
        return path.replace('/', '.');
    }

    public static Type asType(String className) {
        return Type.getType('L' + className + ';');
    }
}
