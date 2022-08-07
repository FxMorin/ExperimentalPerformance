package ca.fxco.experimentalperformance.utils;

import ca.fxco.experimentalperformance.ExperimentalPerformance;
import ca.fxco.experimentalperformance.memoryDensity.InfoHolderData;
import ca.fxco.experimentalperformance.memoryDensity.VersionedInfoHolderData;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class HolderUtils {

    public static boolean shouldRunHolder(InfoHolderData holderData) {
        String modId = holderData.getModId();
        if (FabricLoader.getInstance().isModLoaded(modId)) {
            Optional<ModContainer> modContainer = FabricLoader.getInstance().getModContainer(modId);
            if (modContainer.isPresent()) {
                try {
                    return VersionPredicate.parse(holderData.getVersionPredicate())
                        .test(modContainer.get().getMetadata().getVersion());
                } catch (VersionParsingException e) {
                    ExperimentalPerformance.LOGGER.error("Unable to parse version predicate for mod: " + modId, e);
                }
            }
        }
        return false;
    }

    @Nullable
    public static VersionedInfoHolderData.InfoHolderPart getBestVersionedInfoHolderPart(VersionedInfoHolderData holderData) {
        String modId = holderData.getModId();
        if (!FabricLoader.getInstance().isModLoaded(modId)) return null;
        List<VersionedInfoHolderData.InfoHolderPart> holderParts = holderData.getVersionedInfoHolderParts();
        if (holderParts.size() == 0) return null;
        Optional<ModContainer> modContainer = FabricLoader.getInstance().getModContainer(modId);
        if (modContainer.isEmpty()) return null;
        Version version = modContainer.get().getMetadata().getVersion();
        for (VersionedInfoHolderData.InfoHolderPart holderPart : holderParts) {
            try {
                if (VersionPredicate.parse(holderPart.versionPredicate()).test(version))
                    return holderPart;
            } catch (VersionParsingException e) {
                ExperimentalPerformance.LOGGER.error("Unable to parse version predicate for mod: " + modId, e);
            }
        }
        return null;
    }

    public static InfoHolderData createInfoHolderFromPart(VersionedInfoHolderData holderData,
                                                          VersionedInfoHolderData.InfoHolderPart holderPart) {
        return new InfoHolderData(
                holderData.getTargetClassName(),
                holderPart.holderClassName(),
                holderPart.extraRedirectFields().size() == 0 ? holderData.getRedirectFields() : Stream.concat(
                        holderData.getRedirectFields().stream(),
                        holderPart.extraRedirectFields().stream()
                ).toList(),
                holderPart.versionPredicate(),
                holderData.getModId()
        );
    }
}
