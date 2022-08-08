package ca.fxco.experimentalperformance.mixin.memoryDensity;

import ca.fxco.experimentalperformance.memoryDensity.HolderId;
import ca.fxco.experimentalperformance.memoryDensity.infoHolders.EntityExtremeInfoHolder;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@HolderId("experimentalperformance.EntityExtreme")
@Mixin(Entity.class)
public class Entity_extremeMixin {
    @Unique
    private final EntityExtremeInfoHolder infoHolder = new EntityExtremeInfoHolder();
}
