package ca.fxco.experimentalperformance.memoryDensity.analysis;

import ca.fxco.experimentalperformance.ExperimentalPerformance;
import ca.fxco.experimentalperformance.memoryDensity.HolderDataRegistry;
import ca.fxco.experimentalperformance.memoryDensity.InfoHolderData;
import ca.fxco.experimentalperformance.memoryDensity.mixinHacks.HacktasticClassLoader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.openjdk.jol.info.ClassData;
import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.info.FieldData;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static ca.fxco.experimentalperformance.utils.GeneralUtils.formatPathDot;
import static ca.fxco.experimentalperformance.utils.GeneralUtils.getLastPathPart;
import static org.lwjgl.system.MemoryUtil.CACHE_LINE_SIZE;

public class FieldReferenceAnalysis {

    private static final String MINECRAFT_PACKAGE = "net/minecraft";

    private final HashMap<String, HashMap<String, AtomicInteger>> fieldRefCounter = new HashMap<>();
    private final HashMap<String, HashMap<Integer, FieldData>> fieldRefData = new HashMap<>();

    public FieldReferenceAnalysis() {}

    public Set<String> getFieldsForClass(String className) {
        if (this.fieldRefData.containsKey(className)) {
            Set<String> fieldNames = new HashSet<>();
            for (Map.Entry<Integer, FieldData> fieldData : this.fieldRefData.get(className).entrySet())
                fieldNames.add(fieldData.getValue().name());
            return fieldNames;
        }
        return Set.of();
    }

    public void generateInfoHolderData(ClassAnalysisManager classAnalysisManager) {
        ExperimentalPerformance.LOGGER.info("Amt AnalysisResults: " + classAnalysisManager.resultAmt());
        classAnalysisManager.forEach(analysisResults -> {
            String className = analysisResults.getClassName();
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
                                          ClassAnalysis.AnalysisResults analysisResults, String className) {
        ExperimentalPerformance.LOGGER.info("Generate Holder for: " + className);
        long amountOver = analysisResults.getSize() % CACHE_LINE_SIZE;
        if (amountOver > (CACHE_LINE_SIZE - analysisResults.getHeaderSize())) return null;
        List<Map.Entry<Integer, FieldData>> fields = new LinkedList<>(this.fieldRefData.get(className).entrySet());
        // Choose fields to use (lowest to highest references)
        fields.sort(new Comparator<>() {
            public int compare(Map.Entry<Integer, FieldData> o1, Map.Entry<Integer, FieldData> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });
        ExperimentalPerformance.LOGGER.info("Fields to remove: " + fields.size());
        // Attempt to grow the holder class until you removed enough so its under the cache line
        Set<String> fieldNames = new HashSet<>();
        ClassData testClassData = new ClassData(className);
        int count = 0;
        for (Map.Entry<Integer, FieldData> field : fields) {
            testClassData.addField(field.getValue());
            count++;
            if (count > 2) { // Start checking after 2 fields
                ClassLayout classLayout = classAnalysisManager.getCurrentLayouter().layout(testClassData);
                if (classLayout.instanceSize() > amountOver) // Stop if holder is larger than CACHE_LINE_SIZE
                    break;
            }
            fieldNames.add(field.getValue().name());
        }
        ExperimentalPerformance.LOGGER.info("Fields names: " + fieldNames);
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
    public void processFieldData(Map<String, ClassNode> classNodes, Map<String, byte[]> classBytes) {
        for (Map.Entry<String, HashMap<String, AtomicInteger>> entry : this.fieldRefCounter.entrySet()) {
            String className = entry.getKey();
            ClassNode classNode = classNodes.get(className);
            if (classNode == null) continue;
            ExperimentalPerformance.LOGGER.info(classNode.name);

            // Get class without loading it with the normal classLoaders
            HacktasticClassLoader hackyClassLoader = new HacktasticClassLoader();
            Class clazz = hackyClassLoader.defineClass(className, classBytes.get(formatPathDot(entry.getKey())));

            for (FieldNode fieldNode : classNode.fields) {
                if ((fieldNode.access & Opcodes.ACC_STATIC) != 0) continue; // If isStatic
                if ((fieldNode.access & Opcodes.ACC_PRIVATE) == 0) continue; // If not private
                //ExperimentalPerformance.LOGGER.info(className + "|" + fieldNode.name);
                AtomicInteger integer = entry.getValue().get(fieldNode.name);
                if (integer == null) continue; // Was removed for being too hot
                HashMap<Integer, FieldData> fields = this.fieldRefData.computeIfAbsent(className, c -> new HashMap<>());
                try { // Remove all fields that don't fit the data
                    fields.put(integer.get(), FieldData.parse(clazz.getDeclaredField(fieldNode.name)));
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
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