package gripe._90.appliede.me.strategy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

import appeng.api.behaviors.StackImportStrategy;
import appeng.api.behaviors.StackTransferContext;
import appeng.api.config.Actionable;
import appeng.api.stacks.AEItemKey;
import appeng.me.storage.ExternalStorageFacade;
import appeng.util.BlockApiCache;

@SuppressWarnings("UnstableApiUsage")
public class EMCItemImportStrategy implements StackImportStrategy {
    private final BlockApiCache<IItemHandler> apiCache;
    private final Direction fromSide;

    public EMCItemImportStrategy(ServerLevel level, BlockPos fromPos, Direction fromSide) {
        apiCache = BlockApiCache.create(ForgeCapabilities.ITEM_HANDLER, level, fromPos);
        this.fromSide = fromSide;
    }

    @Override
    public boolean transfer(StackTransferContext context) {
        if (!(context instanceof EMCTransferContext ctx)) {
            return false;
        }

        var itemHandler = apiCache.find(fromSide);

        if (itemHandler == null) {
            return false;
        }

        var adjacentStorage = ExternalStorageFacade.of(itemHandler);
        var remaining = ctx.getOperationsRemaining();

        for (var i = 0; i < adjacentStorage.getSlots() && remaining > 0; i++) {
            var resource = adjacentStorage.getStackInSlot(i);

            if (resource == null || !(resource.what() instanceof AEItemKey item)) {
                continue;
            }

            if (context.isInFilter(resource.what()) == context.isInverted()) {
                continue;
            }

            var amount = adjacentStorage.extract(item, remaining, Actionable.SIMULATE, context.getActionSource());

            if (amount > 0) {
                var inserted = ctx.getEmcStorage()
                        .insertItem(item, amount, Actionable.MODULATE, context.getActionSource(), ctx.canLearn());
                adjacentStorage.extract(item, inserted, Actionable.MODULATE, context.getActionSource());
                context.reduceOperationsRemaining(inserted);
                remaining -= (int) inserted;
            }
        }

        return true;
    }
}
