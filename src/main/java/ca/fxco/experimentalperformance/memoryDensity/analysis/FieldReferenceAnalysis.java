package ca.fxco.experimentalperformance.memoryDensity.analysis;

import ca.fxco.experimentalperformance.memoryDensity.InfoHolderData;
import ca.fxco.experimentalperformance.memoryDensity.mixinHacks.RuntimeRetransform;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class FieldReferenceAnalysis {

    private static final String MINECRAFT_PACKAGE = "net.minecraft";

    private final HashMap<String, HashMap<String, AtomicInteger>> fieldRefCounter = new HashMap<>();

    private final HashSet<String> seenClasses = new HashSet<>();

    public FieldReferenceAnalysis() {}

    // Should run after all scans are done. So after a forceload
    public void applyChanges(List<ClassAnalysis.AnalysisResults> results) {
        for (ClassAnalysis.AnalysisResults result : results) {
            String className = result.getClassName();
            if (!this.seenClasses.contains(className)) {
                try { // Get all field data for these classes if they have not already been collected
                    RuntimeRetransform.retransform(Class.forName(className), (n, classNode) -> {
                        this.scanClassNode(classNode);
                    });
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            // Get the class name and its size

        }
        System.out.println("Field reference mapping is now complete!");
        int countfields = 0;
        int countReferences = 0;
        for (HashMap<String, AtomicInteger> fields : this.fieldRefCounter.values()) {
            countfields += fields.size();
            for (AtomicInteger integer : fields.values()) {
                countReferences += integer.get();
            }
        }
        System.out.printf("%d fields - %d field references - %d classes%n", countfields, countReferences,this.fieldRefCounter.size());
        this.seenClasses.clear(); // Not needed anymore
        // Find best values to change. I'm just going to pick that are random for now ;) //TODO: CHANGE THIS
        int testSize = 20;
        int classCount = 0;
        for (Map.Entry<String, HashMap<String, AtomicInteger>> entry : this.fieldRefCounter.entrySet()) {
            String targetClassName = entry.getKey();
            InfoHolderData infoHolderData = new InfoHolderData(targetClassName, new ArrayList<>(entry.getValue().keySet()));
            System.out.println("Modified class: " + targetClassName + " - Removed Fields: " + entry.getValue().keySet());
            RuntimeRetransform.retransform(targetClassName, (className, classNode) -> {
                infoHolderData.runTransformation(className).accept(classNode);
            });
            if (classCount++ == testSize) break;
        }
        this.fieldRefCounter.clear();
    }

    public void scanClass(String className, ClassNode classNode) {
        if (this.seenClasses.contains(className)) return;
        this.seenClasses.add(className);
        scanClassNode(classNode);
    }

    public void scanClassNode(ClassNode classNode) {
        for (MethodNode methodNode : classNode.methods)
            scanMethodNode(methodNode);
    }

    public void scanMethodNode(MethodNode methodNode) {
        for (AbstractInsnNode insnNode : methodNode.instructions)
            if (insnNode instanceof FieldInsnNode fieldInsn && fieldInsn.owner.startsWith(MINECRAFT_PACKAGE))
                this.fieldRefCounter
                        .computeIfAbsent(fieldInsn.owner, c -> new HashMap<>())
                        .computeIfAbsent(fieldInsn.name, f -> new AtomicInteger(0))
                        .incrementAndGet();
    }
}
