package gripe._90.appliede.me.misc;

import com.google.common.primitives.Ints;

import appeng.api.behaviors.StackTransferContext;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.util.prioritylist.IPartitionList;

import gripe._90.appliede.me.service.EMCStorage;

@SuppressWarnings({"UnstableApiUsage", "NonExtendableApiUsage"})
public class EMCTransferContext implements StackTransferContext {
    private final EMCStorage emcStorage;
    private final IActionSource actionSource;
    private final IPartitionList filter;
    private final int initialOperations;

    private int operationsRemaining;
    private boolean isInverted;

    public EMCTransferContext(
            EMCStorage emcStorage, IActionSource actionSource, IPartitionList filter, int operationsRemaining) {
        this.emcStorage = emcStorage;
        this.actionSource = actionSource;
        this.filter = filter;

        initialOperations = operationsRemaining;
        this.operationsRemaining = operationsRemaining;
    }

    public EMCStorage getEmcStorage() {
        return emcStorage;
    }

    @Override
    public IStorageService getInternalStorage() {
        return null;
    }

    @Override
    public IEnergySource getEnergySource() {
        return null;
    }

    @Override
    public IActionSource getActionSource() {
        return actionSource;
    }

    @Override
    public int getOperationsRemaining() {
        return operationsRemaining;
    }

    @Override
    public void setOperationsRemaining(int operationsRemaining) {
        this.operationsRemaining = operationsRemaining;
    }

    @Override
    public boolean hasOperationsLeft() {
        return operationsRemaining > 0;
    }

    @Override
    public boolean hasDoneWork() {
        return initialOperations > operationsRemaining;
    }

    @Override
    public boolean isKeyTypeEnabled(AEKeyType space) {
        return space.equals(AEKeyType.items());
    }

    @Override
    public boolean isInFilter(AEKey key) {
        return filter.isEmpty() || filter.isListed(key);
    }

    @Override
    public IPartitionList getFilter() {
        return filter;
    }

    @Override
    public void setInverted(boolean inverted) {
        isInverted = inverted;
    }

    @Override
    public boolean isInverted() {
        return isInverted;
    }

    @Override
    public boolean canInsert(AEItemKey what, long amount) {
        return true;
    }

    @Override
    public void reduceOperationsRemaining(long inserted) {
        operationsRemaining -= Ints.saturatedCast(inserted);
    }
}
