package ca.fxco.experimentalperformance.memoryDensity;

import ca.fxco.experimentalperformance.ExperimentalPerformance;
import ca.fxco.experimentalperformance.memoryDensity.infoHolders.InfoHolderGenerator;
import ca.fxco.experimentalperformance.utils.AsmUtils;
import ca.fxco.experimentalperformance.utils.GeneralUtils;
import com.chocohead.mm.api.ClassTinkerers;

import java.util.List;

import static ca.fxco.experimentalperformance.memoryDensity.HolderDataRegistry.ALL_VERSIONS;
import static ca.fxco.experimentalperformance.memoryDensity.HolderDataRegistry.MINECRAFT_ID;

public class InfoHolderData {

    private final String targetClassName;
    private final String holderClassName;
    private final List<String> redirectFields;
    private final String modId;
    private final String versionPredicate;
    private final boolean defaultValue;

    public InfoHolderData(String targetClassName, String holderClassName, List<String> redirectFields) {
        this(targetClassName, holderClassName, redirectFields, ALL_VERSIONS);
    }

    public InfoHolderData(String targetClassName, String holderClassName,
                           List<String> redirectFields, String versionPredicate) {
        this(targetClassName, holderClassName, redirectFields, versionPredicate, MINECRAFT_ID);
    }

    public InfoHolderData(String targetClassName, String holderClassName, List<String> redirectFields,
                          String versionPredicate, String modId) {
        this(targetClassName, holderClassName, redirectFields, versionPredicate, modId, true);
    }

    public InfoHolderData(String targetClassName, String holderClassName, List<String> redirectFields,
                          String versionPredicate, String modId, boolean defaultValue) {
        if (redirectFields.size() == 0)
            throw new IllegalArgumentException("`redirectFields` must have at least 1 field to redirect!");
        if (redirectFields.size() == 1) // Allow 1 field although it's not recommended
            ExperimentalPerformance.LOGGER.warn("`redirectFields` should have more than 1 field to redirect.");
        this.targetClassName = targetClassName;
        this.holderClassName = holderClassName;
        this.redirectFields = redirectFields;
        this.modId = modId;
        this.versionPredicate = versionPredicate;
        this.defaultValue = defaultValue;
    }

    public boolean getDefaultValue() {
        return this.defaultValue;
    }

    public String getHolderClassName() {
        return this.holderClassName;
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

    public void apply() {
        if (!shouldLoad()) return;
        ClassTinkerers.addTransformation(targetClassName, node -> {
            String className = ExperimentalPerformance.VERBOSE ? GeneralUtils.getLastPathPart(targetClassName) : "";
            InfoHolderGenerator generator = new InfoHolderGenerator();
            Class<?> infoHolderClass = generator.createInfoHolder(node, holderClassName, redirectFields);
            System.out.println(infoHolderClass);
            AsmUtils.removeFieldsContaining(className, node.fields, redirectFields);
            node.fields.add(AsmUtils.generateInfoHolderField(holderClassName, infoHolderClass)); // Attempt to add field to class (basically replace the mixin)
            AsmUtils.redirectFieldsToInfoHolder(node.methods, targetClassName, holderClassName, redirectFields);
        });
    }

    //May want to setup a builder if I add any more options
}
