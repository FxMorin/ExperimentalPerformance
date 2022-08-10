package classAnalysis;

import ca.fxco.experimentalperformance.memoryDensity.analysis.ClassAnalysisManager;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;

import java.util.List;

public class TestClassgraph {

    private static final ClassAnalysisManager classAnalysisManager = new ClassAnalysisManager(false);

    public static void main(String[] args) {
        try (ScanResult scanResult = new ClassGraph()
                .enableAllInfo()
                .ignoreClassVisibility()
                .acceptPackages("net.minecraft")
                .scan()) {
            ClassInfoList allClassInfo = scanResult.getAllClasses();
            List<Class<?>> allClasses = allClassInfo.loadClasses(); // Load all classes
            for (Class<?> clazz : allClasses)
                classAnalysisManager.runSingleAnalysis(clazz);
            classAnalysisManager.printResults(new ClassAnalysisManager.ResultConditions(65));
        }
    }
}
