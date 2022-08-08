package ca.fxco.experimentalperformance.memoryDensity.infoHolders;

import net.minecraft.fluid.Fluid;
import net.minecraft.tag.TagKey;

import java.util.Set;

public class EntityInfoHolder {
    public Set<TagKey<Fluid>> submergedFluidTag;
    public int portalCooldown;
    public long pistonMovementTick;
    public float lastChimeIntensity;
    public int lastChimeAge;
    public float nextStepSoundDistance;
}
