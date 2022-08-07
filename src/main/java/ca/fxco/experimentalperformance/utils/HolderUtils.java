package ca.fxco.experimentalperformance.utils;

import ca.fxco.experimentalperformance.ExperimentalPerformance;
import ca.fxco.experimentalperformance.memoryDensity.InfoHolderData;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;

import java.util.Optional;

public class HolderUtils {

    public static boolean shouldRunHolder(InfoHolderData holderData) {
        String modId = holderData.getModId();
        if (FabricLoader.getInstance().isModLoaded(modId)) {
            Optional<ModContainer> modContainer = FabricLoader.getInstance().getModContainer(modId);
            if (modContainer.isPresent()) {
                try {
                    return VersionPredicate.parse(holderData.getversionPredicate())
                        .test(modContainer.get().getMetadata().getVersion());
                } catch (VersionParsingException e) {
                    ExperimentalPerformance.LOGGER.error("Unable to parse version predicate for mod: " + modId, e);
                }
            }
        }
        return false;
    }
}
