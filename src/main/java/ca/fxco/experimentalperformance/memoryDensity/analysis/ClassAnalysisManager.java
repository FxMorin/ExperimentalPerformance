package ca.fxco.experimentalperformance.memoryDensity.analysis;

import org.objectweb.asm.tree.ClassNode;
import org.openjdk.jol.datamodel.*;
import org.openjdk.jol.layouters.CurrentLayouter;
import org.openjdk.jol.layouters.HotSpotLayouter;
import org.openjdk.jol.layouters.Layouter;
import org.openjdk.jol.layouters.RawLayouter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class ClassAnalysisManager {

    private final Layouter currentLayout = new CurrentLayouter();
    private final List<Future<ClassAnalysis.AnalysisResults>> futureAnalysisResults = new ArrayList<>();

    public ClassAnalysisManager() {}

    public Layouter getCurrentLayouter() {
        return this.currentLayout;
    }

    public void runSingleAnalysis(Class<?> clazz, Set<String> validFields) {
        this.futureAnalysisResults.add(new ClassAnalysis(clazz.getName(), currentLayout)
                .runAnalysis(clazz, validFields));
    }

    public void runSingleAnalysis(ClassNode classNode, Set<String> validFields) {
        this.futureAnalysisResults.add(new ClassAnalysis(classNode.name, currentLayout)
                .runAnalysis(classNode, validFields));
    }

    public void runBulkClassAnalysis(List<Class<?>> clazzes) {
        for (Class<?> clazz : clazzes)
            runSingleAnalysis(clazz, null);
    }

    public void runBulkClassNodeAnalysis(List<ClassNode> classNodes) {
        for (ClassNode classNode : classNodes)
            runSingleAnalysis(classNode, null);
    }

    public void runBulkClassAnalysis(List<Class<?>> clazzes, FieldReferenceAnalysis fieldReferenceAnalysis) {
        for (Class<?> clazz : clazzes)
            runSingleAnalysis(clazz, fieldReferenceAnalysis.getFieldsForClass(clazz.getName()));
    }

    public void runBulkClassNodeAnalysis(Map<String, ClassNode> classNodes, FieldReferenceAnalysis fieldReferenceAnalysis) {
        for (ClassNode classNode : classNodes.values())
            runSingleAnalysis(classNode, fieldReferenceAnalysis.getFieldsForClass(classNode.name));
    }

    public void clear() {
        this.futureAnalysisResults.clear();
    }

    public void forEach(Consumer<ClassAnalysis.AnalysisResults> resultsConsumer) {
        this.futureAnalysisResults.forEach(futureResult -> {
            try {
                resultsConsumer.accept(futureResult.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
    }

    public void printResults() {
        try {
            System.out.println("ClassAnalysisManager - Total: " + this.futureAnalysisResults.size());
            for (Future<ClassAnalysis.AnalysisResults> task : this.futureAnalysisResults) {
                ClassAnalysis.AnalysisResults analysisResults = task.get();
                System.out.println(analysisResults.toString());
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void printResults(ResultConditions conditions) {
        System.out.println("ClassAnalysisManager - Total: " + this.futureAnalysisResults.size());
        int count = 0;
        int fails = 0;
        for (Future<ClassAnalysis.AnalysisResults> task : this.futureAnalysisResults) {
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
            for (Future<ClassAnalysis.AnalysisResults> task : this.futureAnalysisResults) task.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
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
