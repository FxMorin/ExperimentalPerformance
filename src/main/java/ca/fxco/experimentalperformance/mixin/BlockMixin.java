package ca.fxco.experimentalperformance.mixin;

import ca.fxco.experimentalperformance.patches.BlockInfo;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Block.class)
public abstract class BlockMixin {

    @Unique
    private final BlockInfo blockInfo = new BlockInfo();


    /**
     * @author FX - PR0CESS
     * @reason Moving into BlockInfo
     */
    @Overwrite
    public final void setDefaultState(BlockState state) {
        this.blockInfo.setDefaultState(state);
    }

    /**
     * @author FX - PR0CESS
     * @reason Moving into BlockInfo
     */
    @Overwrite
    public final BlockState getDefaultState() {
        return this.blockInfo.getDefaultState();
    }

    /**
     * @author FX - PR0CESS
     * @reason Moving into BlockInfo
     */
    @Overwrite
    public String getTranslationKey() {
        return this.blockInfo.getTranslationKey((Block)(Object)this);
    }

    /**
     * @author FX - PR0CESS
     * @reason Moving into BlockInfo
     */
    @Overwrite
    public Item asItem() {
        return this.blockInfo.asItem((Block)(Object)this);
    }
}
