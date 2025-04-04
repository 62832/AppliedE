package gripe._90.appliede.me.misc;

import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.me.storage.DelegatingMEInventory;
import appeng.util.ConfigInventory;
import appeng.util.Platform;

import gripe._90.appliede.AppliedE;
import gripe._90.appliede.me.service.EMCStorage;
import gripe._90.appliede.me.service.KnowledgeService;

import moze_intel.projecte.api.proxy.IEMCProxy;

public class EMCInterfaceLogic implements IActionHost, IGridTickable, IUpgradeableObject {
    protected final EMCInterfaceLogicHost host;
    protected final IManagedGridNode mainNode;

    private final ConfigInventory config;
    private final ConfigInventory storage;
    private final IUpgradeInventory upgrades;

    private final MEStorage localInvHandler;
    private final GenericStack[] plannedWork;
    private final IActionSource source = IActionSource.ofMachine(this);

    @Nullable
    private WrappedEMCStorage emcStorage;

    private boolean hasConfig;

    public EMCInterfaceLogic(IManagedGridNode node, EMCInterfaceLogicHost host, Item is) {
        this(node, host, is, 9);
    }

    public EMCInterfaceLogic(IManagedGridNode node, EMCInterfaceLogicHost host, Item is, int slots) {
        this.host = host;
        mainNode = node.setFlags(GridFlags.REQUIRE_CHANNEL)
                .addService(IGridTickable.class, this)
                .setIdlePowerUsage(10);

        config = ConfigInventory.configStacks(slots)
                .slotFilter(what -> AEItemKey.is(what))
                .changeListener(this::onConfigRowChanged)
                .build();
        storage = ConfigInventory.storage(slots)
                .slotFilter(this::storageFilter)
                .changeListener(this::onStorageChanged)
                .build();
        upgrades = UpgradeInventories.forMachine(is, 1, host::saveChanges);

        localInvHandler = new DelegatingMEInventory(storage);
        plannedWork = new GenericStack[slots];

        config.useRegisteredCapacities();
        storage.useRegisteredCapacities();
    }

    public ConfigInventory getConfig() {
        return config;
    }

    public ConfigInventory getStorage() {
        return storage;
    }

    public MEStorage getInventory() {
        return hasConfig ? localInvHandler : emcStorage;
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return upgrades;
    }

    private boolean storageFilter(AEKey what) {
        if (!(what instanceof AEItemKey item)) {
            return false;
        }

        var grid = mainNode.getGrid();
        var node = mainNode.getNode();

        if (grid == null || node == null) {
            // client-side, allow everything in order for items to actually display
            return true;
        }

        var knowledge = grid.getService(KnowledgeService.class);
        return knowledge.getKnownItems().contains(item)
                || (isUpgradedWith(AppliedE.LEARNING_CARD)
                        && IEMCProxy.INSTANCE.hasValue(item.toStack())
                        && knowledge.getProviderFor(node.getOwningPlayerProfileId()) != null);
    }

    public void readFromNBT(CompoundTag tag, HolderLookup.Provider registries) {
        config.readFromChildTag(tag, "config", registries);
        storage.readFromChildTag(tag, "storage", registries);
        upgrades.readFromNBT(tag, "upgrades", registries);
        readConfig();
    }

    public void writeToNBT(CompoundTag tag, HolderLookup.Provider registries) {
        config.writeToChildTag(tag, "config", registries);
        storage.writeToChildTag(tag, "storage", registries);
        upgrades.writeToNBT(tag, "upgrades", registries);
    }

    @Nullable
    @Override
    public IGridNode getActionableNode() {
        return mainNode.getNode();
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(5, 120, !hasWorkToDo());
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (!mainNode.isActive()) {
            return TickRateModulation.SLEEP;
        }

        var couldDoWork = false;

        for (var i = 0; i < plannedWork.length; i++) {
            var work = plannedWork[i];

            if (work != null) {
                couldDoWork = tryUsePlan(i, work.what(), (int) work.amount()) || couldDoWork;

                if (couldDoWork) {
                    updatePlan(i);
                }
            }
        }

        return hasWorkToDo()
                ? couldDoWork ? TickRateModulation.URGENT : TickRateModulation.SLOWER
                : TickRateModulation.SLEEP;
    }

    private boolean hasWorkToDo() {
        for (var requiredWork : plannedWork) {
            if (requiredWork != null) {
                return true;
            }
        }

        return false;
    }

    private void updatePlan() {
        var hadWork = hasWorkToDo();

        for (var i = 0; i < config.size(); i++) {
            updatePlan(i);
        }

        var hasWork = hasWorkToDo();

        if (hadWork != hasWork) {
            mainNode.ifPresent((grid, node) -> {
                if (hasWork) {
                    grid.getTickManager().alertDevice(node);
                } else {
                    grid.getTickManager().sleepDevice(node);
                }
            });
        }
    }

    private void updatePlan(int slot) {
        var req = config.getStack(slot);
        var stored = storage.getStack(slot);

        if (req == null && stored != null) {
            plannedWork[slot] = new GenericStack(stored.what(), -stored.amount());
        } else if (req != null) {
            if (stored == null) {
                plannedWork[slot] = req;
            } else if (req.what().equals(stored.what())) {
                plannedWork[slot] = req.amount() != stored.amount()
                        ? new GenericStack(req.what(), req.amount() - stored.amount())
                        : null;
            } else {
                plannedWork[slot] = new GenericStack(stored.what(), -stored.amount());
            }
        } else {
            plannedWork[slot] = null;
        }
    }

    private boolean tryUsePlan(int slot, AEKey what, int amount) {
        if (!(what instanceof AEItemKey item)) {
            return false;
        }

        var grid = mainNode.getGrid();

        if (grid == null) {
            return false;
        }

        if (amount < 0) {
            amount = -amount;
            var inSlot = storage.getStack(slot);

            if (!what.matches(inSlot) || inSlot.amount() < amount) {
                return true;
            }

            var depositedItems = grid.getService(KnowledgeService.class)
                    .getStorage()
                    .insertItem(item, amount, Actionable.MODULATE, source, isUpgradedWith(AppliedE.LEARNING_CARD));

            if (depositedItems > 0) {
                storage.extract(slot, what, depositedItems, Actionable.MODULATE);
                return true;
            }
        }

        if (amount > 0) {
            return storage.insert(slot, what, amount, Actionable.SIMULATE) != amount
                    || acquireFromNetwork(grid, slot, what, amount);
        }

        return false;
    }

    private boolean acquireFromNetwork(IGrid grid, int slot, AEKey what, long amount) {
        if (!(what instanceof AEItemKey item)) {
            return false;
        }

        var acquiredItems = grid.getService(KnowledgeService.class)
                .getStorage()
                .extractItem(item, amount, Actionable.MODULATE, source, true);

        if (acquiredItems > 0) {
            var inserted = storage.insert(slot, what, acquiredItems, Actionable.MODULATE);

            if (inserted < acquiredItems) {
                throw new IllegalStateException("Bad attempt at managing inventory. Voided items: " + inserted);
            }

            return true;
        } else {
            return false;
        }
    }

    private void readConfig() {
        hasConfig = !config.isEmpty();
        updatePlan();
        notifyNeighbours();
    }

    private void onConfigRowChanged() {
        host.saveChanges();
        readConfig();
    }

    private void onStorageChanged() {
        host.saveChanges();
        updatePlan();
    }

    public void notifyNeighbours() {
        mainNode.ifPresent((grid, node) -> {
            if (node.isActive()) {
                grid.getTickManager().wakeDevice(node);
            }
        });

        var be = host.getBlockEntity();

        if (be != null && be.getLevel() != null) {
            Platform.notifyBlocksOfNeighbors(be.getLevel(), be.getBlockPos());
        }
    }

    public void gridChanged() {
        emcStorage = new WrappedEMCStorage(Objects.requireNonNull(mainNode.getGrid())
                .getService(KnowledgeService.class)
                .getStorage());
        notifyNeighbours();
    }

    public void addDrops(List<ItemStack> drops) {
        for (var i = 0; i < storage.size(); i++) {
            var stack = storage.getStack(i);

            if (stack != null) {
                var be = host.getBlockEntity();
                stack.what().addDrops(stack.amount(), drops, be.getLevel(), be.getBlockPos());
            }
        }

        for (var is : this.upgrades) {
            if (!is.isEmpty()) {
                drops.add(is);
            }
        }
    }

    public void clearContent() {
        storage.clear();
        upgrades.clear();
    }

    private class WrappedEMCStorage implements MEStorage {
        private final EMCStorage storage;

        private WrappedEMCStorage(EMCStorage storage) {
            this.storage = storage;
        }

        @Override
        public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
            return what instanceof AEItemKey item && mainNode.isActive()
                    ? storage.insertItem(item, amount, mode, source, isUpgradedWith(AppliedE.LEARNING_CARD))
                    : 0;
        }

        @Override
        public Component getDescription() {
            return AppliedE.EMC_INTERFACE.get().getName();
        }
    }
}
