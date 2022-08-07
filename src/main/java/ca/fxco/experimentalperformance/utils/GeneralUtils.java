package ca.fxco.experimentalperformance.utils;

public class GeneralUtils {

    public static String getLastPathPart(String path) {
        String[] classPath = path.split("/");
        return classPath[classPath.length - 1];
    }
}
