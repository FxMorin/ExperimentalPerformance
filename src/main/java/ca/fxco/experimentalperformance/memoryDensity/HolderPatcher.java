package ca.fxco.experimentalperformance.memoryDensity;

import ca.fxco.experimentalperformance.ExperimentalPerformance;
import ca.fxco.experimentalperformance.utils.HolderUtils;

import java.util.Map;

public class HolderPatcher {

    public static void attemptToAddHolders(Map<String, InfoHolderData> mainHolderMap,
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

    public static void attemptToAddVersionedHolders(Map<String, InfoHolderData> mainHolderMap,
                                                    Map<String, VersionedInfoHolderData> versionedInfoHolderDataMap) {
        for (Map.Entry<String, VersionedInfoHolderData> entry : versionedInfoHolderDataMap.entrySet()) {
            String key = entry.getKey();
            VersionedInfoHolderData versionedInfoHolderData = entry.getValue();
            VersionedInfoHolderData.InfoHolderPart infoHolderPart =
                    HolderUtils.getBestInfoHolderPart(versionedInfoHolderData);
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
}