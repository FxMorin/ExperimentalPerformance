package ca.fxco.experimentalperformance.mixin.memoryDensity;

import ca.fxco.experimentalperformance.memoryDensity.HolderDataRegistry;
import ca.fxco.experimentalperformance.memoryDensity.HolderId;
import ca.fxco.experimentalperformance.memoryDensity.infoHolders.v1_18.EntityInfoHolder_1_18;
import ca.fxco.experimentalperformance.memoryDensity.infoHolders.v1_19.EntityInfoHolder_1_19;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.*;

@HolderId("experimentalperformance.Entity")
@Restriction(require = @Condition(value = HolderDataRegistry.MINECRAFT_ID, versionPredicates = "1.19.x"))
@Mixin(Entity.class)
class Entity_1_19_memDensityMixin {
    @Unique
    private final EntityInfoHolder_1_19 infoHolder = new EntityInfoHolder_1_19();
}

@HolderId("experimentalperformance.Entity")
@Restriction(require = @Condition(value = HolderDataRegistry.MINECRAFT_ID, versionPredicates = "1.18.x"))
@Mixin(Entity.class)
class Entity_1_18_memDensityMixin {
    @Unique
    private final EntityInfoHolder_1_18 infoHolder = new EntityInfoHolder_1_18();
}
