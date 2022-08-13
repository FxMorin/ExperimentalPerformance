package ca.fxco.experimentalperformance.memoryDensity.analysis;

import ca.fxco.experimentalperformance.ExperimentalPerformance;
import org.objectweb.asm.tree.ClassNode;
import org.openjdk.jol.layouters.CurrentLayouter;
import org.openjdk.jol.layouters.Layouter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static ca.fxco.experimentalperformance.utils.GeneralUtils.formatPathDot;

public class ClassAnalysisManager {

    private final Layouter currentLayout = new CurrentLayouter();
    private final List<ClassAnalysis.AnalysisResults> analysisResults = new ArrayList<>();

    public ClassAnalysisManager() {}

    public Layouter getCurrentLayouter() {
        return this.currentLayout;
    }

    public void runSingleAnalysis(Class<?> clazz, Set<String> validFields) {
        this.analysisResults.add(new ClassAnalysis(clazz.getName(), currentLayout)
                .runAnalysis(clazz, validFields));
    }

    public void runSingleAnalysis(ClassNode classNode, byte[] classBytes, Set<String> validFields) {
        this.analysisResults.add(new ClassAnalysis(classNode.name, currentLayout)
                .runAnalysis(classNode, classBytes, validFields));
    }

    public void runBulkClassAnalysis(List<Class<?>> clazzes) {
        for (Class<?> clazz : clazzes)
            runSingleAnalysis(clazz, null);
    }

    public void runBulkClassNodeAnalysis(List<ClassNode> classNodes, List<byte[]> classBytes) {
        for (int i = 0; i < classNodes.size(); i++) {
            runSingleAnalysis(classNodes.get(i), classBytes.get(i), null);
        }
    }

    public void runBulkClassAnalysis(List<Class<?>> clazzes, FieldReferenceAnalysis fieldReferenceAnalysis) {
        for (Class<?> clazz : clazzes) {
            Set<String> validFields = fieldReferenceAnalysis.getFieldsForClass(clazz.getName());
            if (validFields.size() >= 2)
                runSingleAnalysis(clazz, validFields);
        }
    }

    public void runBulkClassNodeAnalysis(Map<String, ClassNode> classNodes, Map<String, byte[]> classBytes, FieldReferenceAnalysis fieldReferenceAnalysis) {
        for (Map.Entry<String, ClassNode> entry : classNodes.entrySet()) {
            ClassNode classNode = entry.getValue();
            Set<String> validFields = fieldReferenceAnalysis.getFieldsForClass(entry.getKey());
            if (validFields.size() >= 2) {
                ExperimentalPerformance.LOGGER.info("Run Bulk Analysis for: " + classNode.name + " - validFields: " + validFields);
                byte[] bytes = classBytes.get(formatPathDot(entry.getKey()));
                if (bytes != null) {
                    runSingleAnalysis(classNode, bytes, validFields);
                } else {
                    ExperimentalPerformance.LOGGER.info("Unable to get classBytes `" + entry.getKey() + "`");
                }
            }
        }
    }

    public void clear() {
        this.analysisResults.clear();
    }

    public int resultAmt() {
        return this.analysisResults.size();
    }

    public List<ClassAnalysis.AnalysisResults> getResults() {
        return this.analysisResults;
    }

    public void forEach(Consumer<ClassAnalysis.AnalysisResults> resultsConsumer) {
        this.analysisResults.forEach(resultsConsumer);
    }

    public void printResults() {
        System.out.println("ClassAnalysisManager - Total: " + this.analysisResults.size());
        for (ClassAnalysis.AnalysisResults analysisResults : this.analysisResults) {
            System.out.println(analysisResults.toString());
        }
    }

    public void printResults(ResultConditions conditions) {
        System.out.println("ClassAnalysisManager - Total: " + this.analysisResults.size());
        int count = 0;
        int fails = 0;
        for (ClassAnalysis.AnalysisResults results : this.analysisResults) {
            if (
                    results.getSize() >= conditions.minSize &&
                    results.getSize() <= conditions.maxSize &&
                    results.getPrivateSize() >= conditions.minPrivateSize &&
                    results.getPrivateSize() <= conditions.maxPrivateSize
            ) {
                System.out.println(results);
                count++;
            }
        }
        System.out.println("Total Shown: " + count + " - Total Fails: " + fails);
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
