package ca.fxco.experimentalperformance.memoryDensity;

import java.util.List;

public class VersionedInfoHolderData {

    private final String targetClassName;
    private final List<String> redirectFields;
    private final String modId;

    private final List<InfoHolderPart> holderVersions;

    public VersionedInfoHolderData(String targetClassName, List<String> redirectFields,
                                   List<InfoHolderPart> holderVersions) {
        this(targetClassName, redirectFields, holderVersions, HolderDataRegistry.MINECRAFT_ID);
    }

    public VersionedInfoHolderData(String targetClassName, List<String> redirectFields,
                                   List<InfoHolderPart> holderVersions, String modId) {
        this.targetClassName = targetClassName;
        this.holderVersions = holderVersions;
        this.redirectFields = redirectFields;
        this.modId = modId;
    }

    public String getTargetClassName() {
        return this.targetClassName;
    }

    public List<String> getRedirectFields() {
        return this.redirectFields;
    }

    public String getModId() {
        return this.modId;
    }

    public List<InfoHolderPart> getVersionedInfoHolderParts() {
        return this.holderVersions;
    }

    public static InfoHolderPart part(String holderClass, List<String> extraRedirectFields, String versionPredicate) {
        return new InfoHolderPart(holderClass, versionPredicate, extraRedirectFields);
    }

    public record InfoHolderPart(String holderClassName, String versionPredicate, List<String> extraRedirectFields) {

        public String getHolderClassName() {
            return this.holderClassName;
        }

        public String getVersionPredicate() {
            return this.versionPredicate;
        }

        public List<String> getExtraRedirectFields() {
            return this.extraRedirectFields;
        }
    }
}
