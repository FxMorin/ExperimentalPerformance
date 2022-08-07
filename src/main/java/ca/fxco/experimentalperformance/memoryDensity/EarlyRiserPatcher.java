package ca.fxco.experimentalperformance.memoryDensity;

import ca.fxco.experimentalperformance.utils.HolderUtils;

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

    @Override
    public void run() {
        // Here you will run all the infoHolders.
        for (InfoHolderData infoHolderData : infoHolderDataList)
            if (HolderUtils.shouldRunHolder(infoHolderData))
                infoHolderData.apply(); // Run class tweaker
        // TODO: Add entrypoint here so other mods can add there own infoHolderData
    }
}