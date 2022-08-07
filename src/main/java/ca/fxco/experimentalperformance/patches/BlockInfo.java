package ca.fxco.experimentalperformance.patches;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

public class BlockInfo {

    private BlockState defaultState;
    @Nullable
    private String translationKey;
    @Nullable
    private Item cachedItem;

    public BlockInfo() {}

    public final void setDefaultState(BlockState state) {
        this.defaultState = state;
    }

    public final BlockState getDefaultState() {
        return this.defaultState;
    }

    public String getTranslationKey(Block block) {
        if (this.translationKey == null)
            this.translationKey = Util.createTranslationKey("block", Registry.BLOCK.getId(block));
        return this.translationKey;
    }

    public Item asItem(Block block) {
        return this.cachedItem == null ? (this.cachedItem = Item.fromBlock(block)) : this.cachedItem;
    }
}
