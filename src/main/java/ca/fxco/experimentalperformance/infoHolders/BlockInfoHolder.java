package ca.fxco.experimentalperformance.infoHolders;

import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import org.jetbrains.annotations.Nullable;

public class BlockInfoHolder {
    public BlockState defaultState;
    @Nullable
    public String translationKey;
    @Nullable
    public Item cachedItem;
}
