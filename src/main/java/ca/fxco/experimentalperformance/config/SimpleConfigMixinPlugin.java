package ca.fxco.experimentalperformance.config;

import ca.fxco.experimentalperformance.ExperimentalPerformance;
import ca.fxco.experimentalperformance.memoryDensity.HolderId;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.util.Annotations;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class SimpleConfigMixinPlugin implements IMixinConfigPlugin {

    @Override
    public void onLoad(String mixinPackage) {
        ExperimentalPerformance.CONFIG.parseConfig();
    }

    @Nullable
    private AnnotationNode getHolderIdAnnotation(String className) {
        try {
            return Annotations.getVisible(
                    MixinService.getService().getBytecodeProvider().getClassNode(className),
                    HolderId.class
            );
        } catch (IOException | ClassNotFoundException err) {
            ExperimentalPerformance.LOGGER.error("Error while attempting to get Mixin Annotation", err);
            return null;
        }
    }


    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        AnnotationNode holderId = getHolderIdAnnotation(mixinClassName);
        if (holderId != null) {
            String holderIdValue = Annotations.getValue(holderId, "value");
            if (ExperimentalPerformance.VERBOSE)
                System.out.println(
                        "mixinClassName: " + mixinClassName +
                        " - holderId: "+ holderIdValue +
                        " - shouldLoad: "+ ExperimentalPerformance.CONFIG.shouldLoad(holderIdValue)
                );
            return ExperimentalPerformance.CONFIG.shouldLoad(holderIdValue);
        }
        return true;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
