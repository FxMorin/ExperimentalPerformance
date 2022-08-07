package ca.fxco.experimentalperformance.patches;

import net.minecraft.util.math.BlockPos;

public interface EntityInfoAccess {
    EntityInfo getEntityInfo();
    BlockPos getLastNetherPortalPosition();
}
