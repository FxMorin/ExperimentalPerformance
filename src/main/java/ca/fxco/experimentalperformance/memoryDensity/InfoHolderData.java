package ca.fxco.experimentalperformance.memoryDensity;

import ca.fxco.experimentalperformance.ExperimentalPerformance;
import ca.fxco.experimentalperformance.utils.AsmUtils;
import com.chocohead.mm.api.ClassTinkerers;

import java.util.List;

public class InfoHolderData {

    private static final String ALL_VERSIONS = "*";
    private static final String MINECRAFT_ID = "minecraft";

    private final String targetClassName;
    private final String holderClassName;
    private final List<String> redirectFields;
    private final String modId;
    private final String versionPredicate;

    public InfoHolderData(String targetClassName, String holderClassName, List<String> redirectFields) {
        this(targetClassName, holderClassName, redirectFields, MINECRAFT_ID);
    }

    public InfoHolderData(String targetClassName, String holderClassName,
                           List<String> redirectFields, String modId) {
        this(targetClassName, holderClassName, redirectFields, modId, ALL_VERSIONS);
    }

    public InfoHolderData(String targetClassName, String holderClassName,
                           List<String> redirectFields, String modId, String versionPredicate) {
        if (redirectFields.size() == 0)
            throw new IllegalArgumentException("`redirectFields` must have at least 1 field to redirect!");
        if (redirectFields.size() == 1) // Allow 1 field although it's not recommended
            ExperimentalPerformance.LOGGER.warn("`redirectFields` should have more than 1 field to redirect.");
        this.targetClassName = targetClassName;
        this.holderClassName = holderClassName;
        this.redirectFields = redirectFields;
        this.modId = modId;
        this.versionPredicate = versionPredicate;
    }

    public String getModId() {
        return this.modId;
    }

    public String getversionPredicate() {
        return this.versionPredicate;
    }

    public void apply() {
        ClassTinkerers.addTransformation(targetClassName, node -> {
            String className;
            if (ExperimentalPerformance.VERBOSE) {
                String[] classPath = targetClassName.split("/");
                className = classPath[classPath.length - 1];
            } else {
                className = "";
            }
            AsmUtils.removeFieldsContaining(className, node.fields, redirectFields);
            AsmUtils.redirectFieldsToInfoHolder(node.methods, targetClassName, holderClassName, redirectFields);
        });
    }
}
