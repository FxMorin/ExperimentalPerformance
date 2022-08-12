package ca.fxco.experimentalperformance.memoryDensity.analysis;

import ca.fxco.experimentalperformance.ExperimentalPerformance;
import ca.fxco.experimentalperformance.memoryDensity.HolderDataRegistry;
import ca.fxco.experimentalperformance.memoryDensity.InfoHolderData;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.openjdk.jol.info.ClassData;
import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.info.FieldData;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.lwjgl.system.MemoryUtil.CACHE_LINE_SIZE;

public class FieldReferenceAnalysis {

    private static final String MINECRAFT_PACKAGE = "net.minecraft";

    private final HashMap<String, HashMap<String, AtomicInteger>> fieldRefCounter = new HashMap<>();
    private final HashMap<String, HashMap<Integer, FieldData>> fieldRefData = new HashMap<>();

    public FieldReferenceAnalysis() {}

    public Set<String> getFieldsForClass(String className) {
        if (this.fieldRefCounter.containsKey(className))
            return this.fieldRefCounter.get(className).keySet();
        return Set.of();
    }

    public void generateInfoHolderData(ClassAnalysisManager classAnalysisManager) {
        classAnalysisManager.forEach(analysisResults -> { //TODO: Allow more then one info holder per class
            String className = analysisResults.getClassName();
            if (this.fieldRefCounter.containsKey(className)) {
                InfoHolderData infoHolderData = generateHolder(classAnalysisManager, analysisResults, className);
                if (infoHolderData != null) {
                    HolderDataRegistry.infoHolderDataMap.put(
                            ExperimentalPerformance.MODID + "." + className,
                            infoHolderData
                    );
                }
            }
        });
    }

    private InfoHolderData generateHolder(ClassAnalysisManager classAnalysisManager,
                                          ClassAnalysis.AnalysisResults analysisResults, String className) {
        long amountOver = analysisResults.getSize() % CACHE_LINE_SIZE;
        if (amountOver > (CACHE_LINE_SIZE - analysisResults.getHeaderSize())) return null;
        List<Map.Entry<Integer, FieldData>> fields = new LinkedList<>(this.fieldRefData.get(className).entrySet());
        // Choose fields to use (lowest to highest references)
        Collections.sort(fields, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o1)).getKey()).compareTo(((Map.Entry)(o2)).getKey());
            }
        });
        // Attempt to grow the holder class until you removed enough so its under the cache line
        /*ClassData classData = new ClassData(className);
        boolean isValid = true;
        classData.addField(this.fieldRefData.get(0));
        while(true) {
            classData.addField(FieldData.create(className, ));
        }*/

        // Pack and ship
        /*ClassData classData = new ClassData(className);
        ClassLayout classLayout = classAnalysisManager.getCurrentLayouter().layout(
                createClassData(classNode, false, validFields)
        );*/
        //TODO: ALl of it xD
        return null;
    }

    // TODO: This is a temporary hot field check.
    //  We plan to use a combination of: ScanningAgent, DepthSearch, and FieldReferences
    // This method will identify and remove all the fields from the scans that it claims are too Hot to move
    public void processHotFields() {
        this.fieldRefCounter.entrySet().removeIf(each -> {
            final HashMap<String, AtomicInteger> value = each.getValue();
            value.entrySet().removeIf(entry -> entry.getValue().get() > 4); // If more than 4 references, remove it
            return value.size() == 0; // Remove class if it has no entries
        });
    }

    // This method will remove all public and static fields from the analysis
    public void processFieldData(Map<String, ClassNode> classNodes) {
        for (Map.Entry<String, HashMap<String, AtomicInteger>> entry : this.fieldRefCounter.entrySet()) {
            String className = entry.getKey();
            ClassNode classNode = classNodes.get(className);
            for (FieldNode fieldNode : classNode.fields) {
                if ((fieldNode.access & Opcodes.ACC_STATIC) != 0) continue; // If isStatic
                if ((fieldNode.access & Opcodes.ACC_PRIVATE) == 0) continue; // If not private
                HashMap<Integer, FieldData> fields = this.fieldRefData.computeIfAbsent(className, c -> new HashMap<>());
                fields.put( // Remove all fields that don't fit the data
                        entry.getValue().get(fieldNode.name).get(),
                        FieldData.create(className, fieldNode.name, Type.getType(fieldNode.desc).getClassName())
                );
            }
        }
        this.fieldRefCounter.clear();
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