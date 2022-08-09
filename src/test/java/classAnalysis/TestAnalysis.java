package classAnalysis;

import ca.fxco.experimentalperformance.memoryDensity.analysis.ClassAnalysisManager;
import classAnalysis.testClasses.*;

public class TestAnalysis {

    private static final ClassAnalysisManager classAnalysisManager = new ClassAnalysisManager(false);

    public static void main(String[] args) {
        // TODO: Fix compiler optimizations getting rid of all the private fields xD
        System.out.println("Starting Class Analysis Test");
        classAnalysisManager.runSingleAnalysis(NothingBoi.class);
        classAnalysisManager.runSingleAnalysis(PrivateBoi.class);
        classAnalysisManager.runSingleAnalysis(BigBoi.class);
        classAnalysisManager.runSingleAnalysis(SmallBoi.class);
        classAnalysisManager.runSingleAnalysis(TinyBoi.class);
        classAnalysisManager.simulateResults();
        System.out.println("Done");
    }
}
