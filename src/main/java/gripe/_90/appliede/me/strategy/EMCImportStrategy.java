package gripe._90.appliede.me.strategy;

import java.util.concurrent.atomic.AtomicLong;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import appeng.api.behaviors.StackImportStrategy;
import appeng.api.behaviors.StackTransferContext;
import appeng.api.config.Actionable;
import appeng.api.storage.StorageHelper;

import gripe._90.appliede.me.key.EMCKey;
import gripe._90.appliede.me.key.EMCKeyType;

import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.block_entity.IEmcStorage;

@SuppressWarnings("UnstableApiUsage")
public record EMCImportStrategy(ServerLevel level, BlockPos pos, Direction side) implements StackImportStrategy {
    @Override
    public boolean transfer(StackTransferContext context) {
        if (!context.isKeyTypeEnabled(EMCKeyType.TYPE)) {
            return false;
        }

        var be = level.getBlockEntity(pos);

        if (be == null) {
            return false;
        }

        var remainingTransfer = context.getOperationsRemaining() * EMCKeyType.TYPE.getAmountPerOperation();
        var inserted = new AtomicLong(0);

        be.getCapability(PECapabilities.EMC_STORAGE_CAPABILITY).ifPresent(emcStorage -> {
            inserted.set(Math.min(remainingTransfer, emcStorage.getStoredEmc()));
            StorageHelper.poweredInsert(
                    context.getEnergySource(),
                    context.getInternalStorage().getInventory(),
                    EMCKey.BASE,
                    inserted.get(),
                    context.getActionSource(),
                    Actionable.MODULATE);
            emcStorage.extractEmc(inserted.get(), IEmcStorage.EmcAction.EXECUTE);

            var opsUsed = Math.max(1, inserted.get() / EMCKeyType.TYPE.getAmountPerOperation());
            context.reduceOperationsRemaining(opsUsed);
        });

        return inserted.get() > 0;
    }
}
