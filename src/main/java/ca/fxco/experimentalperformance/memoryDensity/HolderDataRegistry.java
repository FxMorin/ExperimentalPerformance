package ca.fxco.experimentalperformance.memoryDensity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ca.fxco.experimentalperformance.ExperimentalPerformance.MODID;

public class HolderDataRegistry {

    public static final String ALL_VERSIONS = "*";
    public static final String MINECRAFT_ID = "minecraft";

    private static final String MINECRAFT_PATH = "net/minecraft/";

    public static String mc(String str) {
        return MINECRAFT_PATH + str;
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
            List.of("defaultState", "translationKey", "cachedItem")
    );

    public static InfoHolderData infoHolder_EntityExtreme = new InfoHolderData(
            mc("entity/Entity"),
            List.of("id", "type", "passengerList", "vehicle", "pos", "blockPos", "chunkPos", "velocity",
                    "yaw", "pitch", "boundingBox", "removalReason", "fireTicks"),
            ALL_VERSIONS,
            MINECRAFT_ID,
            false
    );


    /*================================
      VERSIONED INFO HOLDER DATA MAP
    ================================*/
    public static VersionedInfoHolderData infoHolder_Chunk = new VersionedInfoHolderData(
            mc("world/chunk/Chunk"),
            List.of("inhabitedTime", "blendingData", "structureStarts", "chunkNoiseSampler"),
            List.of(
                VersionedInfoHolderData.part(List.of("generationSettings"), "1.19.x"),
                VersionedInfoHolderData.part(List.of("biome"), "1.18.x")
            )
    );

    public static VersionedInfoHolderData infoHolder_Entity = new VersionedInfoHolderData(
            mc("entity/Entity"),
            List.of("submergedFluidTag", "trackedPosition", "portalCooldown", "pistonMovementTick",
                    "lastChimeIntensity", "lastChimeAge", "nextStepSoundDistance"),
            List.of(
                VersionedInfoHolderData.part("1.19.x"),
                VersionedInfoHolderData.part("1.18.x")
            )
    );


    /*public static final Map<String, InfoHolderData> infoHolderDataMap = new HashMap<>() {{
        put(ep("Block"), infoHolder_Block);
        put(ep("EntityExtreme"), infoHolder_EntityExtreme);
    }};

    public static final Map<String, VersionedInfoHolderData> versionedInfoHolderDataMap = Map.of(
            ep("Chunk"), infoHolder_Chunk,
            ep("Entity"), infoHolder_Entity
    );*/

    public static final Map<String, InfoHolderData> infoHolderDataMap = new HashMap<>();

    public static final Map<String, VersionedInfoHolderData> versionedInfoHolderDataMap = Map.of();
}
