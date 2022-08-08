package ca.fxco.experimentalperformance.memoryDensity.infoHolders;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class EntityExtremeInfoHolder {
    public int id;
    public EntityType<?> type;
    public ImmutableList<Entity> passengerList;
    @Nullable
    public Entity vehicle;
    public Vec3d pos;
    public BlockPos blockPos;
    public ChunkPos chunkPos;
    public Vec3d velocity;
    public float yaw;
    public float pitch;
    public Box boundingBox;
    @Nullable
    public Entity.RemovalReason removalReason;
    public int fireTicks;
}
