package ca.fxco.experimentalperformance.memoryDensity;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This interface should be used to allow ExperimentalPerformance to toggle your mixin if it's a Holder Mixin
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface HolderId {
    String value();
}
