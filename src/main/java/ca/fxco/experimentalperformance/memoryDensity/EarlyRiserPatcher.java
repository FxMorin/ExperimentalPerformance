package ca.fxco.experimentalperformance.memoryDensity;

import ca.fxco.experimentalperformance.ExperimentalPerformance;
import ca.fxco.experimentalperformance.utils.HolderUtils;
import net.fabricmc.loader.api.FabricLoader;

import java.util.HashMap;
import java.util.Map;

public class EarlyRiserPatcher implements Runnable {

    private static void attemptToAddHolders(Map<String, InfoHolderData> mainHolderMap,
                                            Map<String, InfoHolderData> infoHolderDataMap) {
        for (Map.Entry<String, InfoHolderData> infoHolderDataEntry : infoHolderDataMap.entrySet()) {
            String key = infoHolderDataEntry.getKey();
            InfoHolderData infoHolderData = infoHolderDataEntry.getValue();
            if (HolderUtils.shouldRunHolder(infoHolderData)) {
                if (mainHolderMap.containsKey(key)) { // Add more info to this later
                    ExperimentalPerformance.LOGGER.warn("Duplicate holder keys! - " + key);
                } else {
                    mainHolderMap.put(key, infoHolderData);
                }
            }
        }
    }

    private static void attemptToAddVersionedHolders(Map<String, InfoHolderData> mainHolderMap,
                                                     Map<String, VersionedInfoHolderData> versionedInfoHolderDataMap) {
        for (Map.Entry<String, VersionedInfoHolderData> entry : versionedInfoHolderDataMap.entrySet()) {
            String key = entry.getKey();
            VersionedInfoHolderData versionedInfoHolderData = entry.getValue();
            VersionedInfoHolderData.InfoHolderPart infoHolderPart =
                    HolderUtils.getBestVersionedInfoHolderPart(versionedInfoHolderData);
            if (infoHolderPart != null) {
                if (mainHolderMap.containsKey(key)) { // Add more info to this later
                    ExperimentalPerformance.LOGGER.warn("Duplicate holder keys! - " + key);
                } else {
                    mainHolderMap.put(
                            key,
                            HolderUtils.createInfoHolderFromPart(versionedInfoHolderData, infoHolderPart)
                    );
                }
            }
        }
    }

    @Override
    public void run() {
        Map<String, InfoHolderData> allInfoHolderData = new HashMap<>();

        // Here you will run all the infoHolders.
        attemptToAddHolders(allInfoHolderData, HolderDataRegistry.infoHolderDataMap); // Run built-in holder data list first
        attemptToAddVersionedHolders(allInfoHolderData, HolderDataRegistry.versionedInfoHolderDataMap);
        // Remember that this entrypoint is an early riser!
        FabricLoader.getInstance()
                .getEntrypointContainers("experimentalperformance-holder", HolderDataContainer.class)
                .forEach(entrypoint -> {
                    HolderDataContainer container = entrypoint.getEntrypoint();
                    attemptToAddHolders(allInfoHolderData, container.getHolderDataList());
                    attemptToAddVersionedHolders(allInfoHolderData, container.getVersionedHolderDataList());
                });

        for (Map.Entry<String, InfoHolderData> entry : allInfoHolderData.entrySet())
            if (ExperimentalPerformance.CONFIG.shouldLoad(entry.getKey()))
                entry.getValue().apply();
    }
}