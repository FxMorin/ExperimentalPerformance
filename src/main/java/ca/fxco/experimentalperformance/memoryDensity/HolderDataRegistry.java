package ca.fxco.experimentalperformance.memoryDensity;

import java.util.List;
import java.util.Map;

import static ca.fxco.experimentalperformance.ExperimentalPerformance.MODID;

public class HolderDataRegistry {

    public static final String ALL_VERSIONS = "*";
    public static final String MINECRAFT_ID = "minecraft";

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


    /*======================
      INFO HOLDER DATA MAP
    ======================*/
    public static InfoHolderData infoHolder_Block = new InfoHolderData(
            mc("block/Block"),
            infoHolder("BlockInfoHolder"),
            List.of("defaultState", "translationKey", "cachedItem")
    );

    /*================================
      VERSIONED INFO HOLDER DATA MAP
    ================================*/
    public static VersionedInfoHolderData infoHolder_Chunk = new VersionedInfoHolderData(
            mc("world/chunk/Chunk"),
            List.of("inhabitedTime", "blendingData", "structureStarts", "chunkNoiseSampler"),
            List.of(
                VersionedInfoHolderData.part(
                        infoHolder("v1_19/ChunkInfoHolder_1_19"),
                        List.of("generationSettings"),
                        "1.19.x"
                ),
                VersionedInfoHolderData.part(
                        infoHolder("v1_18/ChunkInfoHolder_1_18"),
                        List.of("biome"),
                        "1.18.x"
                )
            )
    );

    public static VersionedInfoHolderData infoHolder_Entity = new VersionedInfoHolderData(
            mc("entity/Entity"),
            List.of("submergedFluidTag", "trackedPosition", "portalCooldown", "pistonMovementTick",
                    "lastChimeIntensity", "lastChimeAge"),
            List.of(
                VersionedInfoHolderData.part(
                        infoHolder("v1_19/EntityInfoHolder_1_19"),
                        List.of(),
                        "1.19.x"
                ),
                VersionedInfoHolderData.part(
                        infoHolder("v1_18/EntityInfoHolder_1_18"),
                        List.of(),
                        "1.18.x"
                )
            )
    );


    public static final Map<String, InfoHolderData> infoHolderDataMap = Map.of(
            ep("Block"), infoHolder_Block
    );

    public static final Map<String, VersionedInfoHolderData> versionedInfoHolderDataMap = Map.of(
            ep("Chunk"), infoHolder_Chunk,
            ep("Entity"), infoHolder_Entity
    );
}
