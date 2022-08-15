package ca.fxco.experimentalperformance.config;

import ca.fxco.experimentalperformance.ExperimentalPerformance;
import ca.fxco.experimentalperformance.memoryDensity.HolderDataRegistry;
import ca.fxco.experimentalperformance.memoryDensity.HolderPatcher;
import ca.fxco.experimentalperformance.memoryDensity.InfoHolderData;
import ca.fxco.experimentalperformance.memoryDensity.mixinHacks.PreLoadMixins;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.*;

public class SimpleConfigMixinPlugin implements IMixinConfigPlugin {

    private static TransformationManager transformationManager;
    private static PreLoadMixins preloadMixins;
    private static boolean firstEarlyEntrypoint = true;

    private String mixinPackage;

    @Override
    public void onLoad(String mixinPackage) {
        this.mixinPackage = mixinPackage;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    // Happens right after all the configs where set (Cause of the plugin priority)
    // and before `getMixins()` so we can still inject our newly created mixins
    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        if (firstEarlyEntrypoint) {
            firstEarlyEntrypoint = false;
            // Now do some logic
            transformationManager = new TransformationManager(this.mixinPackage);
            preloadMixins = new PreLoadMixins();
            ExperimentalPerformance.CONFIG.parseConfig();
            preloadMixins.PreLoadAllMixins();

            // Here you will run all the infoHolders. - Run built-in holder data list first
            Map<String, InfoHolderData> allInfoHolderData = new HashMap<>();
            HolderPatcher.attemptToAddHolders(allInfoHolderData, HolderDataRegistry.infoHolderDataMap);
            HolderPatcher.attemptToAddVersionedHolders(allInfoHolderData, HolderDataRegistry.versionedInfoHolderDataMap);
            for (Map.Entry<String, InfoHolderData> entry : allInfoHolderData.entrySet())
                entry.getValue().apply(entry.getKey(), transformationManager);
            transformationManager.onLoad();
        }
        return transformationManager.onGetMixins(); // Add new mixins
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
