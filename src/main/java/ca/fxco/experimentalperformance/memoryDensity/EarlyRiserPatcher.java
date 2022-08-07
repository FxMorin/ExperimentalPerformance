package ca.fxco.experimentalperformance.memoryDensity;

import ca.fxco.experimentalperformance.utils.HolderUtils;
import net.fabricmc.loader.api.FabricLoader;

import java.util.List;

public class EarlyRiserPatcher implements Runnable {

    private static final String MINECRAFT_PATH = "net/minecraft/";
    private static final String INFOHOLDER_PATH = "ca/fxco/experimentalperformance/memoryDensity/infoHolders/";

    public static String mc(String str) {
        return MINECRAFT_PATH + str;
    }

    private static String infoHolder(String str) { // Used for my own personal info holders
        return INFOHOLDER_PATH + str;
    }

    private final List<InfoHolderData> infoHolderDataList = List.of(
            new InfoHolderData(mc("block/Block"), infoHolder("BlockInfoHolder"),
                    List.of("defaultState", "translationKey", "cachedItem")),
            new InfoHolderData(mc("world/chunk/Chunk"), infoHolder("ChunkInfoHolder"),
                    List.of("inhabitedTime", "blendingData", "structureStarts",
                            "generationSettings", "chunkNoiseSampler")),
            new InfoHolderData(mc("entity/Entity"), infoHolder("EntityInfoHolder"),
                    List.of("submergedFluidTag", "trackedPosition", "portalCooldown",
                            "pistonMovementTick", "lastChimeIntensity", "lastChimeAge"))
    );

    private static void attemptToApplyHolders(List<InfoHolderData> infoHolderDataList) {
        for (InfoHolderData infoHolderData : infoHolderDataList)
            if (HolderUtils.shouldRunHolder(infoHolderData))
                infoHolderData.apply(); // Run class tweaker
    }

    @Override
    public void run() {
        // Here you will run all the infoHolders.
        attemptToApplyHolders(infoHolderDataList); // Run built-in holder data list first
        // Remember that this entrypoint is an early riser!
        FabricLoader.getInstance()
                .getEntrypointContainers("experimentalperformance-holder", HolderDataContainer.class)
                .forEach(entrypoint -> {
                    HolderDataContainer container = entrypoint.getEntrypoint();
                    attemptToApplyHolders(container.getHolderDataList()); // Run holder data list for mod
                });
    }
}