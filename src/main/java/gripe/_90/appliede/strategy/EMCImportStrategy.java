package gripe._90.appliede.strategy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import appeng.api.behaviors.StackImportStrategy;
import appeng.api.behaviors.StackTransferContext;
import appeng.api.config.Actionable;
import appeng.util.BlockApiCache;

import gripe._90.appliede.key.EMCKey;
import gripe._90.appliede.key.EMCKeyType;

import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.block_entity.IEmcStorage;

@SuppressWarnings("UnstableApiUsage")
public class EMCImportStrategy implements StackImportStrategy {
    private final BlockApiCache<IEmcStorage> blockApiCache;
    private final Direction fromSide;

    public EMCImportStrategy(ServerLevel level, BlockPos fromPos, Direction fromSide) {
        blockApiCache = BlockApiCache.create(PECapabilities.EMC_STORAGE_CAPABILITY, level, fromPos);
        this.fromSide = fromSide;
    }

    @Override
    public boolean transfer(StackTransferContext context) {
        if (!context.isKeyTypeEnabled(EMCKeyType.TYPE)) {
            return false;
        }

        var emcStorage = blockApiCache.find(fromSide);

        if (emcStorage == null) {
            return false;
        }

        var remainingTransfer = context.getOperationsRemaining() * EMCKeyType.TYPE.getAmountPerOperation();
        var inserted = Math.min(remainingTransfer, emcStorage.getStoredEmc());

        context.getInternalStorage()
                .getInventory()
                .insert(EMCKey.BASE, inserted, Actionable.MODULATE, context.getActionSource());
        emcStorage.extractEmc(inserted, IEmcStorage.EmcAction.EXECUTE);

        var opsUsed = Math.max(1, inserted / EMCKeyType.TYPE.getAmountPerOperation());
        context.reduceOperationsRemaining(opsUsed);

        return inserted > 0;
    }
}
