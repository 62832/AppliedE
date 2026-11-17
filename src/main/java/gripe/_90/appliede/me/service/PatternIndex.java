package gripe._90.appliede.me.service;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;

import gripe._90.appliede.mixin.crafting.CraftingServiceAccess;

/**
 * Adds and removes individual crafting-pattern entries on one provider.
 * A full provider refresh throws away every entry and inserts them again.
 */
public interface PatternIndex {
    @Nullable
    static PatternIndex of(IGrid grid) {
        if (!(grid.getCraftingService() instanceof CraftingServiceAccess access)) {
            return null;
        }

        var providers = access.appliede$craftingProviders();
        return providers instanceof PatternIndex index ? index : null;
    }

    boolean appliede$hasProvider(IGridNode node);

    void appliede$replacePatterns(IGridNode node, List<IPatternDetails> removed, List<IPatternDetails> added);
}
