package ca.fxco.experimentalperformance.memoryDensity;

import java.util.List;

/**
    This interface is what gets called when using the entrypoint `experimentalperformance-holder`.
    It allows you to specify your own InfoHolderData in order to change how a mod, or minecraft should pack fields.
    This is pretty experimental xD
    @since v0.1.0
 */
public interface HolderDataContainer {

    /**
     This should return a list of {@link InfoHolderData} that each specify what fields to pack together
     @since v0.1.0
     */
    List<InfoHolderData> getHolderDataList();
}
