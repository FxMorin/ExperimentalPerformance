package ca.fxco.experimentalperformance;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExperimentalPerformance implements ModInitializer {

    /*
     TODO:
        - Add Config to allow people to disable specific optimizations for mod compatibility
        - World is 104, make a WorldInfo consisting of all weather and dimension values. Which would bring it down to 64
        - Entity is 272 (4.25 -> 5 cache lines) Entities not only take up a massive amount,
          they also transfer that massive amount all over the place.
     */

    public static final String MODID = "experimentalperformance";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public static final boolean VERBOSE = true;

    @Override
    public void onInitialize() {}
}
