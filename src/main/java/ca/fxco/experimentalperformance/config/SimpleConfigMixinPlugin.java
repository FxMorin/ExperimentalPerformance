package ca.fxco.experimentalperformance.config;

import ca.fxco.experimentalperformance.ExperimentalPerformance;
import ca.fxco.experimentalperformance.memoryDensity.HolderDataRegistry;
import ca.fxco.experimentalperformance.memoryDensity.HolderPatcher;
import ca.fxco.experimentalperformance.memoryDensity.InfoHolderData;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.*;

public class SimpleConfigMixinPlugin implements IMixinConfigPlugin {

    private static TransformationManager transformationManager;

    @Override
    public void onLoad(String mixinPackage) {
        transformationManager = new TransformationManager(mixinPackage);
        ExperimentalPerformance.CONFIG.parseConfig();
        Map<String, InfoHolderData> allInfoHolderData = new HashMap<>();

        // Here you will run all the infoHolders. - Run built-in holder data list first
        HolderPatcher.attemptToAddHolders(allInfoHolderData, HolderDataRegistry.infoHolderDataMap);
        HolderPatcher.attemptToAddVersionedHolders(allInfoHolderData, HolderDataRegistry.versionedInfoHolderDataMap);

        for (Map.Entry<String, InfoHolderData> entry : allInfoHolderData.entrySet())
            if (ExperimentalPerformance.CONFIG.shouldLoad(entry.getKey()))
                entry.getValue().apply(entry.getKey(), transformationManager);
        transformationManager.onLoad();
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return transformationManager.onGetMixins();
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        transformationManager.onPreApply(targetClassName, targetClass, mixinClassName);
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        transformationManager.onPostApply(targetClassName, targetClass, mixinClassName);
    }
}
