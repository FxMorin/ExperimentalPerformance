package ca.fxco.experimentalperformance.memoryDensity.analysis;

import ca.fxco.experimentalperformance.ExperimentalPerformance;
import ca.fxco.experimentalperformance.memoryDensity.HolderDataRegistry;
import ca.fxco.experimentalperformance.memoryDensity.InfoHolderData;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.openjdk.jol.util.MathUtil;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static ca.fxco.experimentalperformance.utils.GeneralUtils.getLastPathPart;
import static org.lwjgl.system.MemoryUtil.CACHE_LINE_SIZE;

public class FieldReferenceAnalysis {

    private static final String MINECRAFT_PACKAGE = "net/minecraft";

    private final HashMap<String, HashMap<String, AtomicInteger>> fieldRefCounter = new HashMap<>();
    private final HashMap<String, HashMap<Integer, FieldNode>> fieldRefData = new HashMap<>();

    public FieldReferenceAnalysis() {}

    public Set<String> getFieldsForClass(String className) {
        if (this.fieldRefData.containsKey(className)) {
            Set<String> fieldNames = new HashSet<>();
            for (Map.Entry<Integer, FieldNode> fieldData : this.fieldRefData.get(className).entrySet())
                fieldNames.add(fieldData.getValue().name);
            return fieldNames;
        }
        return Set.of();
    }

    public void generateInfoHolderData(ClassAnalysisManager classAnalysisManager) {
        ExperimentalPerformance.LOGGER.info("Amt AnalysisResults: " + classAnalysisManager.resultAmt());
        classAnalysisManager.forEach(analysisResults -> {
            String className = analysisResults.className;
            //ExperimentalPerformance.LOGGER.info(className);
            if (this.fieldRefData.containsKey(className)) {
                InfoHolderData infoHolderData = generateHolder(classAnalysisManager, analysisResults, className);
                if (infoHolderData != null) {
                    HolderDataRegistry.infoHolderDataMap.put(
                            ExperimentalPerformance.MODID + "." + getLastPathPart(className),
                            infoHolderData
                    );
                }
            }
        });
    }

    private InfoHolderData generateHolder(ClassAnalysisManager classAnalysisManager,
                                          ClassAnalysisManager.AnalysisResult analysisResults, String className) {
        ExperimentalPerformance.LOGGER.info("Generate Holder for: " + className);
        long amountOver = analysisResults.instanceSize % CACHE_LINE_SIZE;
        if (amountOver > (CACHE_LINE_SIZE - ClassNodeInstanceSizeCalculator.OBJECT_HEADER_SIZE)) return null;
        List<Map.Entry<Integer, FieldNode>> fields = new LinkedList<>(this.fieldRefData.get(className).entrySet());
        // Choose fields to use (lowest to highest references)
        fields.sort(new Comparator<>() {
            public int compare(Map.Entry<Integer, FieldNode> o1, Map.Entry<Integer, FieldNode> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });
        // Attempt to grow the holder class until you removed enough so its under the cache line
        Set<String> fieldNames = new HashSet<>();
        long instanceSize = ClassNodeInstanceSizeCalculator.OBJECT_HEADER_SIZE;
        for (Map.Entry<Integer, FieldNode> field : fields) {
            instanceSize += ClassNodeInstanceSizeCalculator.calculateFieldCost(field.getValue());
            if (MathUtil.align(instanceSize, ClassNodeInstanceSizeCalculator.OBJECT_ALIGNMENT) > amountOver)
                break; // Stop if holder is larger than CACHE_LINE_SIZE
            fieldNames.add(field.getValue().name);
        }
        ExperimentalPerformance.LOGGER.info("Fields names: " + fieldNames + " with holder size: " + instanceSize);
        // Pack and ship
        return new InfoHolderData(className, new ArrayList<>(fieldNames));
    }

    // TODO: This is a temporary hot field check.
    //  We plan to use a combination of: ScanningAgent, DepthSearch, and FieldReferences
    // This method will identify and remove all the fields from the scans that it claims are too Hot to move
    public void processHotFields() {
        ExperimentalPerformance.LOGGER.info("Classes gathered from field analysis: " + this.fieldRefCounter.size());
        AtomicInteger fieldRemovalCount = new AtomicInteger();
        AtomicInteger fieldAverageReferenceSize = new AtomicInteger();
        AtomicInteger classRemovalCount = new AtomicInteger();
        this.fieldRefCounter.entrySet().removeIf(each -> {
            final HashMap<String, AtomicInteger> value = each.getValue();
            value.entrySet().removeIf(entry -> {
                boolean shouldRemove = entry.getValue().get() > 10;
                if (shouldRemove) {
                    fieldRemovalCount.getAndIncrement();
                    fieldAverageReferenceSize.addAndGet(entry.getValue().get());
                }
                return shouldRemove;
            }); // If more than 4 references, remove it
            boolean willRemoveClass = value.size() == 0;
            if (willRemoveClass) classRemovalCount.getAndIncrement();
            return willRemoveClass; // Remove class if it has no entries
        });
        ExperimentalPerformance.LOGGER.info(
                "HotFields removed " + fieldRemovalCount.get() + " fields with an average of " +
                        (fieldAverageReferenceSize.get()/fieldRemovalCount.get()) +
                        " references each. This removed a total of " +
                        classRemovalCount.get() + " classes"
        );
    }

    // This method will remove all public and static fields from the analysis
    public void processFieldData(Map<String, ClassNode> classNodes) {
        for (Map.Entry<String, HashMap<String, AtomicInteger>> entry : this.fieldRefCounter.entrySet()) {
            String className = entry.getKey();
            ClassNode classNode = classNodes.get(className);
            if (classNode == null) continue;
            ExperimentalPerformance.LOGGER.info(classNode.name);

            HashMap<Integer, FieldNode> fields = this.fieldRefData.computeIfAbsent(className, c -> new HashMap<>());
            for (FieldNode fieldNode : classNode.fields) {
                if ((fieldNode.access & Opcodes.ACC_STATIC) != 0) continue; // If isStatic
                if ((fieldNode.access & Opcodes.ACC_PRIVATE) == 0) continue; // If not private
                //ExperimentalPerformance.LOGGER.info(className + "|" + fieldNode.name);
                AtomicInteger integer = entry.getValue().get(fieldNode.name);
                if (integer == null) continue; // Was removed for being too hot
                fields.put(integer.get(), fieldNode);
            }
        }
        this.fieldRefCounter.clear();
    }

    public void scanClassNode(ClassNode classNode) {
        ExperimentalPerformance.LOGGER.info("Starting class node scanning for class: " + classNode.name);
        for (MethodNode methodNode : classNode.methods)
            scanMethodNode(methodNode);
    }

    public void scanMethodNode(MethodNode methodNode) {
        for (AbstractInsnNode insnNode : methodNode.instructions) {
            if (insnNode instanceof FieldInsnNode fieldInsn) {
                //ExperimentalPerformance.LOGGER.info("FieldInsnNode) owner: " + fieldInsn.owner + " - name: " + fieldInsn.name + " - desc: " + fieldInsn.desc);
                if (fieldInsn.owner.startsWith(MINECRAFT_PACKAGE)) {
                    //System.out.println(fieldInsn.owner + "|" + fieldInsn.name);
                    this.fieldRefCounter
                            .computeIfAbsent(fieldInsn.owner, c -> new HashMap<>())
                            .computeIfAbsent(fieldInsn.name, f -> new AtomicInteger(0))
                            .incrementAndGet();
                }
            }
        }
    }
}