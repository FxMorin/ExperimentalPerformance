package ca.fxco.experimentalperformance.memoryDensity;

import java.util.Map;

/**
    This interface is what gets called when using the entrypoint `experimentalperformance-holder`.
    It allows you to specify your own InfoHolderData in order to change how a mod, or minecraft should pack fields.
    This is pretty experimental xD
    @since v0.1.0
 */
public interface HolderDataContainer {

    /**
     This should return a map of {@link InfoHolderData} that each specify what fields to pack together.
     The key is the id of the holder, this is what will be used in the config file.
     @since v0.1.0
     */
    Map<String, InfoHolderData> getHolderDataList();
}
