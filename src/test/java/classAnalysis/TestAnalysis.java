package classAnalysis;

import ca.fxco.experimentalperformance.memoryDensity.analysis.ClassAnalysisManager;
import classAnalysis.testClasses.*;

public class TestAnalysis {

    private static final ClassAnalysisManager classAnalysisManager = new ClassAnalysisManager(false);

    public static void main(String[] args) {
        // TODO: Fix compiler optimizations getting rid of all the private fields xD
        System.out.println("Starting Class Analysis Test");
        long startTime = System.nanoTime();
        int count = 0;
        for (int i = 0; i < 100000; i++) {
            //classAnalysisManager.runSingleAnalysis(NothingBoi.class);
            //classAnalysisManager.runSingleAnalysis(PrivateBoi.class);
            //classAnalysisManager.runSingleAnalysis(BigBoi.class);
            //classAnalysisManager.runSingleAnalysis(SmallBoi.class);
            classAnalysisManager.runSingleAnalysis(TinyBoi.class);
            count++;
        }
        System.out.println("Attempting to generate data, this may take some time - " + count);
        //classAnalysisManager.printResults();
        classAnalysisManager.simulateResults();
        long duration = System.nanoTime() - startTime;
        double seconds = (double)duration/1000000000;
        System.out.println("Done - Took: " + seconds + " seconds");
    }
}
