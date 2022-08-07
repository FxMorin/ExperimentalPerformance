package ca.fxco.experimentalperformance.mixin;

import ca.fxco.experimentalperformance.infoHolders.EntityInfoHolder;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.*;

@Mixin(Entity.class)
public class EntityMixin {
    @Unique
    private final EntityInfoHolder infoHolder = new EntityInfoHolder();
}
