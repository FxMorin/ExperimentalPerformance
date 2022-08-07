package ca.fxco.experimentalperformance.memoryDensity;

import ca.fxco.experimentalperformance.ExperimentalPerformance;
import ca.fxco.experimentalperformance.utils.HolderUtils;
import net.fabricmc.loader.api.FabricLoader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ca.fxco.experimentalperformance.ExperimentalPerformance.MODID;

public class EarlyRiserPatcher implements Runnable {

    private static final String MINECRAFT_PATH = "net/minecraft/";
    private static final String INFOHOLDER_PATH = "ca/fxco/experimentalperformance/memoryDensity/infoHolders/";

    public static String mc(String str) {
        return MINECRAFT_PATH + str;
    }

    // Used for my own personal info holders
    private static String infoHolder(String str) { // Used for my own personal info holders
        return INFOHOLDER_PATH + str;
    }

    // Use this for my own personal id's
    private static String ep(String str) {
        return MODID + "." + str;
    }

    private final Map<String, InfoHolderData> infoHolderDataMap = Map.of(
            ep("Block"), new InfoHolderData(mc("block/Block"), infoHolder("BlockInfoHolder"),
                    List.of("defaultState", "translationKey", "cachedItem")),
            ep("Chunk"), new InfoHolderData(mc("world/chunk/Chunk"), infoHolder("ChunkInfoHolder"),
                    List.of("inhabitedTime", "blendingData", "structureStarts",
                            "generationSettings", "chunkNoiseSampler")),
            ep("Entity"), new InfoHolderData(mc("entity/Entity"), infoHolder("EntityInfoHolder"),
                    List.of("submergedFluidTag", "trackedPosition", "portalCooldown",
                            "pistonMovementTick", "lastChimeIntensity", "lastChimeAge"))
    );

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

    @Override
    public void run() {
        Map<String, InfoHolderData> allInfoHolderData = new HashMap<>();

        // Here you will run all the infoHolders.
        attemptToAddHolders(allInfoHolderData, infoHolderDataMap); // Run built-in holder data list first
        // Remember that this entrypoint is an early riser!
        FabricLoader.getInstance()
                .getEntrypointContainers("experimentalperformance-holder", HolderDataContainer.class)
                .forEach(entrypoint -> {
                    HolderDataContainer container = entrypoint.getEntrypoint();
                    attemptToAddHolders(allInfoHolderData, container.getHolderDataList());
                });

        for (Map.Entry<String, InfoHolderData> entry : allInfoHolderData.entrySet())
            if (ExperimentalPerformance.CONFIG.shouldLoad(entry.getKey()))
                entry.getValue().apply();
    }
}