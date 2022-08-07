package ca.fxco.experimentalperformance.patches;

import net.minecraft.entity.TrackedPosition;
import net.minecraft.fluid.Fluid;
import net.minecraft.tag.TagKey;
import net.minecraft.util.math.Vec3d;

import java.util.HashSet;
import java.util.Set;

public class EntityInfo {

    private final TrackedPosition trackedPosition = new TrackedPosition();
    private final Set<TagKey<Fluid>> submergedFluidTag = new HashSet<>();
    private int portalCooldown;
    private long pistonMovementTick;
    private float lastChimeIntensity;
    private int lastChimeAge;

    public EntityInfo() {}

    public void updateTrackedPosition(double x, double y, double z) {
        this.trackedPosition.setPos(new Vec3d(x, y, z));
    }

    public TrackedPosition getTrackedPosition() {
        return this.trackedPosition;
    }

    public Set<TagKey<Fluid>> getSubmergedFluidTag() {
        return this.submergedFluidTag;
    }

    public boolean isSubmergedIn(TagKey<Fluid> fluidTag) {
        return this.submergedFluidTag.contains(fluidTag);
    }

    public boolean hasPortalCooldown() {
        return this.portalCooldown > 0;
    }

    public int getPortalCooldown() {
        return this.portalCooldown;
    }

    public void tickPortalCooldown() {
        if (this.portalCooldown > 0) --this.portalCooldown;
    }

    public void setPortalCooldown(int cooldown) {
        this.portalCooldown = cooldown;
    }

    public long getPistonMovementTick() {
        return this.pistonMovementTick;
    }

    public void setPistonMovementTick(long tick) {
        this.pistonMovementTick = tick;
    }

    public float getLastChimeIntensity() {
        return this.lastChimeIntensity;
    }

    public void setLastChimeIntensity(float intensity) {
        this.lastChimeIntensity = intensity;
    }

    public int getLastChimeAge() {
        return this.lastChimeAge;
    }

    public void setLastChimeAge(int age) {
        this.lastChimeAge = age;
    }
}
