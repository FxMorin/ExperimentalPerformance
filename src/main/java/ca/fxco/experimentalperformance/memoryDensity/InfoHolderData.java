package ca.fxco.experimentalperformance.memoryDensity;

import ca.fxco.experimentalperformance.ExperimentalPerformance;
import ca.fxco.experimentalperformance.config.TransformationManager;
import ca.fxco.experimentalperformance.memoryDensity.infoHolders.InfoHolderGenerator;
import ca.fxco.experimentalperformance.utils.asm.AsmUtils;
import ca.fxco.experimentalperformance.utils.GeneralUtils;

import java.util.List;

import static ca.fxco.experimentalperformance.memoryDensity.HolderDataRegistry.ALL_VERSIONS;
import static ca.fxco.experimentalperformance.memoryDensity.HolderDataRegistry.MINECRAFT_ID;

public class InfoHolderData {

    private static final String INFOHOLDER_PATH = "ca/fxco/experimentalperformance/memoryDensity/infoHolders/";

    private final String targetClassName;
    private final List<String> redirectFields;
    private final String modId;
    private final String versionPredicate;
    private final boolean defaultValue;

    public InfoHolderData(String targetClassName, List<String> redirectFields) {
        this(targetClassName, redirectFields, ALL_VERSIONS);
    }

    public InfoHolderData(String targetClassName, List<String> redirectFields, String versionPredicate) {
        this(targetClassName, redirectFields, versionPredicate, MINECRAFT_ID);
    }

    public InfoHolderData(String targetClassName, List<String> redirectFields, String versionPredicate, String modId) {
        this(targetClassName, redirectFields, versionPredicate, modId, true);
    }

    public InfoHolderData(String targetClassName, List<String> redirectFields,
                          String versionPredicate, String modId, boolean defaultValue) {
        if (redirectFields.size() == 0)
            throw new IllegalArgumentException("`redirectFields` must have at least 1 field to redirect!");
        if (redirectFields.size() == 1) // Allow 1 field although it's not recommended
            ExperimentalPerformance.LOGGER.warn("`redirectFields` should have more than 1 field to redirect.");
        this.targetClassName = targetClassName;
        this.redirectFields = redirectFields;
        this.modId = modId;
        this.versionPredicate = versionPredicate;
        this.defaultValue = defaultValue;
    }

    public boolean getDefaultValue() {
        return this.defaultValue;
    }

    public String getModId() {
        return this.modId;
    }

    public String getVersionPredicate() {
        return this.versionPredicate;
    }

    // Override this method to add your own loading logic
    public boolean shouldLoad() {
        return true;
    }

    // This gets called before any mixins have been applied and before any classes have been loaded!
    public void apply(String holderId, TransformationManager transformationManager) {
        if (!shouldLoad()) return;
        transformationManager.addPostTransformer(targetClassName, node -> {
            String generatedHolderClassName = (INFOHOLDER_PATH + holderId + "InfoHolder").replace(".","_");
            System.out.println(generatedHolderClassName);
            String className = ExperimentalPerformance.VERBOSE ? GeneralUtils.getLastPathPart(targetClassName) : "";
            InfoHolderGenerator generator = new InfoHolderGenerator();
            generator.createInfoHolder(node, generatedHolderClassName, redirectFields);
            AsmUtils.removeFieldsContaining(className, node.fields, redirectFields);
            node.fields.add(AsmUtils.generateInfoHolderField(generatedHolderClassName));
            AsmUtils.redirectFieldsToInfoHolder(node.methods, node.superName, targetClassName, generatedHolderClassName, redirectFields);
        });
    }

    //May want to setup a builder if I add any more options
}
