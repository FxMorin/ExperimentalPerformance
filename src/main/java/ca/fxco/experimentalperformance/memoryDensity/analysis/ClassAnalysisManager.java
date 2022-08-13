package ca.fxco.experimentalperformance.memoryDensity.analysis;

import ca.fxco.experimentalperformance.ExperimentalPerformance;
import net.minecraft.util.Pair;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.openjdk.jol.util.MathUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class ClassAnalysisManager {

    private final List<AnalysisResult> analysisResults = new ArrayList<>();

    public ClassAnalysisManager() {}

    public void runSingleAnalysis(ClassNode classNode, Set<String> validFields) {
        Pair<Long, Long> classNodeSizes = ClassNodeInstanceSizeCalculator.calculateDualClassNodeSize(
                classNode,
                fieldNode -> validFields == null || validFields.contains(fieldNode.name),
                fieldNode -> (validFields == null || validFields.contains(fieldNode.name)) &&
                        (fieldNode.access & Opcodes.ACC_PRIVATE) != 0
        );
        this.analysisResults.add(new AnalysisResult(
                classNode.name,
                MathUtil.align(classNodeSizes.getLeft(), ClassNodeInstanceSizeCalculator.OBJECT_ALIGNMENT),
                MathUtil.align(classNodeSizes.getRight(), ClassNodeInstanceSizeCalculator.OBJECT_ALIGNMENT)
        ));
    }

    public void runBulkClassNodeAnalysis(List<ClassNode> classNodes) {
        for (ClassNode classNode : classNodes)
            runSingleAnalysis(classNode, null);
    }

    public void runBulkClassNodeAnalysis(List<ClassNode> classNodes, FieldReferenceAnalysis fieldReferenceAnalysis) {
        for (ClassNode classNode : classNodes) {
            Set<String> validFields = fieldReferenceAnalysis.getFieldsForClass(classNode.name);
            if (validFields.size() >= 2) {
                ExperimentalPerformance.LOGGER.info("Run Bulk Analysis for: " + classNode.name + " - validFields: " + validFields);
                runSingleAnalysis(classNode, validFields);
            }
        }
    }

    public void clear() {
        this.analysisResults.clear();
    }

    public int resultAmt() {
        return this.analysisResults.size();
    }

    public List<AnalysisResult> getResults() {
        return this.analysisResults;
    }

    public void forEach(Consumer<AnalysisResult> resultsConsumer) {
        this.analysisResults.forEach(resultsConsumer);
    }

    public void printResults() {
        System.out.println("ClassAnalysisManager - Total: " + this.analysisResults.size());
        for (AnalysisResult analysisResults : this.analysisResults)
            System.out.println(analysisResults);
    }

    public void printResults(ResultConditions conditions) {
        System.out.println("ClassAnalysisManager - Total: " + this.analysisResults.size());
        int count = 0;
        int fails = 0;
        for (AnalysisResult results : this.analysisResults) {
            if (
                    results.instanceSize >= conditions.minSize &&
                    results.instanceSize <= conditions.maxSize &&
                    results.privateInstanceSize >= conditions.minPrivateSize &&
                    results.privateInstanceSize <= conditions.maxPrivateSize
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

    public static class AnalysisResult {
        public final String className;
        public final long instanceSize;
        public final long privateInstanceSize;

        public AnalysisResult(final String className, final long instanceSize, final long privateInstanceSize) {
            this.className = className;
            this.instanceSize = instanceSize;
            this.privateInstanceSize = privateInstanceSize;
            ExperimentalPerformance.LOGGER.info(this.toString());
        }

        @Override
        public String toString() {
            return this.className + ") size: " + this.instanceSize + " - privateSize: " + this.privateInstanceSize;
        }
    }
}
