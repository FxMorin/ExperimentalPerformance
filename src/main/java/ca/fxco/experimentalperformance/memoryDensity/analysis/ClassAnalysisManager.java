package ca.fxco.experimentalperformance.memoryDensity.analysis;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import org.openjdk.jol.datamodel.*;
import org.openjdk.jol.layouters.CurrentLayouter;
import org.openjdk.jol.layouters.HotSpotLayouter;
import org.openjdk.jol.layouters.Layouter;
import org.openjdk.jol.layouters.RawLayouter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ClassAnalysisManager {

    private final Layouter[] layouts;
    private final List<Future<ClassAnalysis.AnalysisResults>> futureAnalysisResults = new ArrayList<>();

    public ClassAnalysisManager(boolean attemptAllLayouts) {
        this.layouts = attemptAllLayouts ? new Layouter[] {
                new RawLayouter(new Model32()),
                new RawLayouter(new Model64()),
                new RawLayouter(new Model64_COOPS_CCPS()),
                new HotSpotLayouter(new Model32(), 8),
                new HotSpotLayouter(new Model64(), 8),
                new HotSpotLayouter(new Model64_COOPS_CCPS(), 8),
                new HotSpotLayouter(new Model64_COOPS_CCPS(16), 8),
                new HotSpotLayouter(new Model32(), 15),
                new HotSpotLayouter(new Model64(), 15),
                new HotSpotLayouter(new Model64_CCPS(), 15),
                new HotSpotLayouter(new Model64_COOPS_CCPS(), 15),
                new HotSpotLayouter(new Model64_CCPS(), 15),
                new HotSpotLayouter(new Model64_CCPS(16), 15),
        } : new Layouter[]{new CurrentLayouter()}; // Automatically detects current model
    }

    public void runSingleAnalysis(Class<?> clazz) {
        for (Layouter model : this.layouts)
            futureAnalysisResults.add(new ClassAnalysis(clazz, model).runAnalysis());
    }

    public void runAllMcClasses() {
        try (ScanResult scanResult = new ClassGraph()
                .enableAllInfo()
                .ignoreClassVisibility()
                .acceptPackages("net.minecraft")
                .scan()) {
            ClassInfoList allClassInfo = scanResult.getAllClasses();
            List<Class<?>> allClasses = allClassInfo.loadClasses(); // Load all classes
            for (Class<?> clazz : allClasses)
                runSingleAnalysis(clazz);
        }
    }

    public void printResults() {
        try {
            System.out.println("ClassAnalysisManager - Total: " + futureAnalysisResults.size());
            for (Future<ClassAnalysis.AnalysisResults> task : futureAnalysisResults) {
                ClassAnalysis.AnalysisResults analysisResults = task.get();
                System.out.println(analysisResults.toString());
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void printResults(ResultConditions conditions) {
        System.out.println("ClassAnalysisManager - Total: " + futureAnalysisResults.size());
        int count = 0;
        int fails = 0;
        for (Future<ClassAnalysis.AnalysisResults> task : futureAnalysisResults) {
            try {
                ClassAnalysis.AnalysisResults results = task.get();
                if (
                        results.getSize() >= conditions.minSize &&
                        results.getSize() <= conditions.maxSize &&
                        results.getPrivateSize() >= conditions.minPrivateSize &&
                        results.getPrivateSize() <= conditions.maxPrivateSize
                ) {
                    System.out.println(results);
                    count++;
                }
            } catch (ExecutionException | InterruptedException e) {
                fails++;
            }
        }
        System.out.println("Total Shown: " + count + " - Total Fails: " + fails);
    }

    public void simulateResults() {
        try {
            for (Future<ClassAnalysis.AnalysisResults> task : futureAnalysisResults) task.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public List<ClassAnalysis.AnalysisResults> getResults() {
        List<ClassAnalysis.AnalysisResults> list = new ArrayList<>();
        for (Future<ClassAnalysis.AnalysisResults> task : futureAnalysisResults)
            try {
                list.add(task.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        return list;
    }

    public List<ClassAnalysis.AnalysisResults> getResults(ResultConditions conditions) {
        List<ClassAnalysis.AnalysisResults> list = new ArrayList<>();
        for (Future<ClassAnalysis.AnalysisResults> task : futureAnalysisResults)
            try {
                ClassAnalysis.AnalysisResults results = task.get();
                if (
                        results.getSize() >= conditions.minSize &&
                        results.getSize() <= conditions.maxSize &&
                        results.getPrivateSize() >= conditions.minPrivateSize &&
                        results.getPrivateSize() <= conditions.maxPrivateSize
                ) {
                    list.add(results);
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        return list;
    }

    public static class ResultConditions {
        public final long minSize;
        public final long maxSize;
        public final long minPrivateSize;
        public final long maxPrivateSize;

        public ResultConditions(long minSize) {
            this(minSize, Long.MAX_VALUE);
        }

        public ResultConditions(long minSize, long maxSize) {
            this(minSize, maxSize, 0L);
        }

        public ResultConditions(long minSize, long maxSize, long minPrivateSize) {
            this(minSize, maxSize, minPrivateSize, Long.MAX_VALUE);
        }

        public ResultConditions(long minSize, long maxSize, long minPrivateSize, long maxPrivateSize) {
            this.minSize = minSize;
            this.maxSize = maxSize;
            this.minPrivateSize = minPrivateSize;
            this.maxPrivateSize = maxPrivateSize;
        }
    }
}
