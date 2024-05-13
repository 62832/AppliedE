package gripe._90.appliede.me.strategy;

import java.util.concurrent.atomic.AtomicLong;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import appeng.api.behaviors.StackExportStrategy;
import appeng.api.behaviors.StackTransferContext;
import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import appeng.api.storage.StorageHelper;

import gripe._90.appliede.me.key.EMCKey;

import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.block_entity.IEmcStorage;

@SuppressWarnings("UnstableApiUsage")
public record EMCExportStrategy(ServerLevel level, BlockPos pos, Direction side) implements StackExportStrategy {
    @Override
    public long transfer(StackTransferContext context, AEKey what, long maxAmount) {
        if (!(what instanceof EMCKey)) {
            return 0;
        }

        var be = level.getBlockEntity(pos);

        if (be == null) {
            return 0;
        }

        var transferred = new AtomicLong(0);

        be.getCapability(PECapabilities.EMC_STORAGE_CAPABILITY).ifPresent(emcStorage -> {
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

            transferred.addAndGet(extracted);
        });

        return transferred.get();
    }

    @Override
    public long push(AEKey what, long maxAmount, Actionable mode) {
        if (!(what instanceof EMCKey)) {
            return 0;
        }

        var be = level.getBlockEntity(pos);

        if (be == null) {
            return 0;
        }

        var transferred = new AtomicLong(0);

        be.getCapability(PECapabilities.EMC_STORAGE_CAPABILITY).ifPresent(emcStorage -> {
            var inserted = Math.min(maxAmount, emcStorage.insertEmc(maxAmount, IEmcStorage.EmcAction.SIMULATE));

            if (inserted > 0) {
                emcStorage.insertEmc(inserted, IEmcStorage.EmcAction.EXECUTE);
            }

            transferred.addAndGet(inserted);
        });

        return transferred.get();
    }
}
