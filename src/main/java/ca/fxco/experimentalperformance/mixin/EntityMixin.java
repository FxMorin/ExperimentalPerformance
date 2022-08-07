package ca.fxco.experimentalperformance.mixin;

import ca.fxco.experimentalperformance.patches.EntityInfo;
import ca.fxco.experimentalperformance.patches.EntityInfoAccess;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TrackedPosition;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Arrays;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityInfoAccess {

    @Unique
    private final EntityInfo entityInfo = new EntityInfo();


    @Shadow
    protected BlockPos lastNetherPortalPosition;

    @Shadow
    @Final
    private double[] pistonMovementDelta;

    @Shadow
    @Final
    protected Random random;

    @Shadow
    public int age;

    @Shadow
    public abstract int getDefaultPortalCooldown();

    @Shadow
    public abstract void playSound(SoundEvent sound, float volume, float pitch);


    public EntityInfo getEntityInfo() {
        return this.entityInfo;
    }

    public BlockPos getLastNetherPortalPosition() {
        return this.lastNetherPortalPosition;
    }

    /**
     * @author FX - PR0CESS
     * @reason Moving into EntityInfo
     */
    @Overwrite
    public void updateTrackedPosition(double x, double y, double z) {
        this.entityInfo.updateTrackedPosition(x, y, z);
    }

    /**
     * @author FX - PR0CESS
     * @reason Moving into EntityInfo
     */
    @Overwrite
    public TrackedPosition getTrackedPosition() {
        return this.entityInfo.getTrackedPosition();
    }

    /**
     * @author FX - PR0CESS
     * @reason Moving into EntityInfo
     */
    @Overwrite
    public boolean isSubmergedIn(TagKey<Fluid> fluidTag) {
        return this.entityInfo.isSubmergedIn(fluidTag);
    }

    /**
     * @author FX - PR0CESS
     * @reason Moving into EntityInfo
     */
    @Overwrite
    public boolean hasPortalCooldownn() { // some idiot named this incorrectly
        return this.entityInfo.hasPortalCooldown();
    }

    /**
     * @author FX - PR0CESS
     * @reason Moving into EntityInfo
     */
    @Overwrite
    public void resetPortalCooldown() {
        this.entityInfo.setPortalCooldown(this.getDefaultPortalCooldown());
    }

    /**
     * @author FX - PR0CESS
     * @reason Moving into EntityInfo
     */
    @Overwrite
    public void tickPortalCooldown() {
        this.entityInfo.tickPortalCooldown();
    }


    @Inject(
            method = "Lnet/minecraft/entity/Entity;copyFrom(Lnet/minecraft/entity/Entity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;readNbt(Lnet/minecraft/nbt/NbtCompound;)V",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void useCustomPortalCooldown(Entity original, CallbackInfo ci) {
        ((EntityInfoAccess)original).getEntityInfo().setPortalCooldown(this.entityInfo.getPortalCooldown());
        this.lastNetherPortalPosition = ((EntityInfoAccess)original).getLastNetherPortalPosition();
        ci.cancel();
    }


    @Inject(
            method = "writeNbt(Lnet/minecraft/nbt/NbtCompound;)Lnet/minecraft/nbt/NbtCompound;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/nbt/NbtCompound;putUuid(Ljava/lang/String;Ljava/util/UUID;)V",
                    shift = At.Shift.BEFORE,
                    ordinal = 0
            )
    )
    public void putPortalCooldown(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
        nbt.putInt("PortalCooldown", this.entityInfo.getPortalCooldown());
    }


    @Inject(
            method = "Lnet/minecraft/entity/Entity;readNbt(Lnet/minecraft/nbt/NbtCompound;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/nbt/NbtCompound;containsUuid(Ljava/lang/String;)Z",
                    shift = At.Shift.BEFORE,
                    ordinal = 0
            )
    )
    public void getPortalCooldown(NbtCompound nbt, CallbackInfo ci) {
        this.entityInfo.setPortalCooldown(nbt.getInt("PortalCooldown"));
    }


    @Redirect(
            method = "adjustMovementForPiston(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;getTime()J",
                    ordinal = 0
            )
    )
    private long doPistonTickLogic(World world) {
        long l = world.getTime();
        if (l != this.entityInfo.getPistonMovementTick()) {
            Arrays.fill(this.pistonMovementDelta, 0.0);
            this.entityInfo.setPistonMovementTick(l);
        }
        return l;
    }


    @Inject(
            method = "updateSubmergedInWaterState()V",
            at = @At(
                   value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;isSubmergedIn(Lnet/minecraft/tag/TagKey;)Z",
                    shift = At.Shift.AFTER,
                    ordinal = 0
            )
    )
    private void replaceSubmergedIn(CallbackInfo ci) {
        this.entityInfo.getSubmergedFluidTag().clear();
    }


    @Inject(
            method = "updateSubmergedInWaterState()V",
            locals = LocalCapture.CAPTURE_FAILHARD,
            at = @At("TAIL")
    )
    private void replaceSubmergedInForEach(CallbackInfo ci, double d, Entity entity, BlockPos blockPos,
                                           FluidState fluidState, double e) {
        if (e > d) fluidState.streamTags().forEach(this.entityInfo.getSubmergedFluidTag()::add);
    }


    /**
     * @author FX - PR0CESS
     * @reason Changing chime data to EntityInfo
     */
    @Overwrite
    private void playAmethystChimeSound(BlockState state) {
        if (state.isIn(BlockTags.CRYSTAL_SOUND_BLOCKS) && this.age >= this.entityInfo.getLastChimeAge() + 20) {
            this.entityInfo.setLastChimeIntensity(this.entityInfo.getLastChimeIntensity() * (float)Math.pow(0.997, this.age - this.entityInfo.getLastChimeAge()));
            this.entityInfo.setLastChimeIntensity(Math.min(1.0F, this.entityInfo.getLastChimeIntensity() + 0.07F));
            float f = 0.5F + this.entityInfo.getLastChimeIntensity() * this.random.nextFloat() * 1.2F;
            float g = 0.1F + this.entityInfo.getLastChimeIntensity() * 1.2F;
            this.playSound(SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, g, f);
            this.entityInfo.setLastChimeAge(this.age);
        }
    }
}
