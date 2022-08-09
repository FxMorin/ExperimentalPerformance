package ca.fxco.experimentalperformance.memoryDensity.analysis;

import org.openjdk.jol.datamodel.*;
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
        this.layouts = attemptAllLayouts ? new Layouter[]{
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
        } : new Layouter[]{new RawLayouter(new ModelVM())}; // Automatically detects current model
    }

    public void runSingleAnalysis(Class<?> clazz) {
        for (Layouter model : this.layouts) {
            futureAnalysisResults.add(new ClassAnalysis(clazz, model).runAnalysis());
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

    public void simulateResults() {
        try {
            for (Future<ClassAnalysis.AnalysisResults> task : futureAnalysisResults) task.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
