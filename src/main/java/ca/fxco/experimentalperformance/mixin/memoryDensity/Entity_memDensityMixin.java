package ca.fxco.experimentalperformance.mixin.memoryDensity;

import ca.fxco.experimentalperformance.memoryDensity.infoHolders.EntityInfoHolder;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.*;

@Mixin(Entity.class)
public class Entity_memDensityMixin {
    @Unique
    private final EntityInfoHolder infoHolder = new EntityInfoHolder();
}
