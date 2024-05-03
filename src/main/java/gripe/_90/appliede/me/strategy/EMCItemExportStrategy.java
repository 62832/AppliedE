package gripe._90.appliede.me.strategy;

import com.google.common.primitives.Ints;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import appeng.api.behaviors.StackExportStrategy;
import appeng.api.behaviors.StackTransferContext;
import appeng.api.config.Actionable;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.util.BlockApiCache;

import gripe._90.appliede.me.helpers.EMCTransferContext;

@SuppressWarnings("UnstableApiUsage")
public class EMCItemExportStrategy implements StackExportStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(EMCItemExportStrategy.class);

    private final BlockApiCache<IItemHandler> apiCache;
    private final Direction fromSide;

    public EMCItemExportStrategy(ServerLevel level, BlockPos fromPos, Direction fromSide) {
        apiCache = BlockApiCache.create(ForgeCapabilities.ITEM_HANDLER, level, fromPos);
        this.fromSide = fromSide;
    }

    @Override
    public long transfer(StackTransferContext context, AEKey what, long amount) {
        if (!(context instanceof EMCTransferContext emcContext)) {
            return 0;
        }

        if (!(what instanceof AEItemKey item)) {
            return 0;
        }

        var adjacentStorage = apiCache.find(fromSide);

        if (adjacentStorage != null) {
            var emcStorage = emcContext.getEmcStorage();
            var stack = item.toStack(Ints.saturatedCast(amount));

            var extracted = emcStorage.extractItem(item, amount, Actionable.SIMULATE, context.getActionSource(), true);
            var remainder = ItemHandlerHelper.insertItem(adjacentStorage, stack, true);
            var wasInserted = extracted - remainder.getCount();

            if (wasInserted > 0) {
                extracted = emcStorage.extractItem(item, amount, Actionable.MODULATE, context.getActionSource(), true);
                remainder = ItemHandlerHelper.insertItem(adjacentStorage, stack, false);
                wasInserted = extracted - remainder.getCount();

                if (wasInserted < extracted) {
                    var leftover = extracted - wasInserted;
                    emcStorage.insertItem(item, leftover, Actionable.MODULATE, context.getActionSource());

                    if (leftover > 0) {
                        LOGGER.error(
                                "Storage export: adjacent block unexpectedly refused insert, voided {}x{}",
                                leftover,
                                item);
                    }
                }
            }

            return wasInserted;
        }

        return 0;
    }

    @Override
    public long push(AEKey what, long maxAmount, Actionable mode) {
        if (!(what instanceof AEItemKey item)) {
            return 0;
        }

        var adjacentStorage = apiCache.find(fromSide);

        if (adjacentStorage != null) {
            var stack = item.toStack(Ints.saturatedCast(maxAmount));
            var remainder = ItemHandlerHelper.insertItem(adjacentStorage, stack, mode.isSimulate());
            return maxAmount - remainder.getCount();
        }

        return 0;
    }
}
