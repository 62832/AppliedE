package gripe._90.appliede.me.strategy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import appeng.api.behaviors.StackExportStrategy;
import appeng.api.behaviors.StackTransferContext;
import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import appeng.api.storage.StorageHelper;
import appeng.util.BlockApiCache;

import gripe._90.appliede.me.key.EMCKey;

import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.block_entity.IEmcStorage;

@SuppressWarnings("UnstableApiUsage")
public class EMCExportStrategy implements StackExportStrategy {
    private final BlockApiCache<IEmcStorage> apiCache;
    private final Direction fromSide;

    public EMCExportStrategy(ServerLevel level, BlockPos fromPos, Direction fromSide) {
        apiCache = BlockApiCache.create(PECapabilities.EMC_STORAGE_CAPABILITY, level, fromPos);
        this.fromSide = fromSide;
    }

    @Override
    public long transfer(StackTransferContext context, AEKey what, long maxAmount) {
        if (!(what instanceof EMCKey)) {
            return 0;
        }

        var emcStorage = apiCache.find(fromSide);

        if (emcStorage != null) {
            var insertable = emcStorage.insertEmc(maxAmount, IEmcStorage.EmcAction.SIMULATE);
            var extracted = StorageHelper.poweredExtraction(
                    context.getEnergySource(),
                    context.getInternalStorage().getInventory(),
                    EMCKey.BASE,
                    insertable,
                    context.getActionSource(),
                    Actionable.MODULATE);

            if (extracted > 0) {
                emcStorage.insertEmc(extracted, IEmcStorage.EmcAction.EXECUTE);
            }

            return extracted;
        }

        return 0;
    }

    @Override
    public long push(AEKey what, long maxAmount, Actionable mode) {
        if (!(what instanceof EMCKey)) {
            return 0;
        }

        var emcStorage = apiCache.find(fromSide);

        if (emcStorage != null) {
            var inserted = Math.min(maxAmount, emcStorage.insertEmc(maxAmount, IEmcStorage.EmcAction.SIMULATE));

            if (inserted > 0) {
                emcStorage.insertEmc(inserted, IEmcStorage.EmcAction.EXECUTE);
            }

            return inserted;
        }

        return 0;
    }
}
