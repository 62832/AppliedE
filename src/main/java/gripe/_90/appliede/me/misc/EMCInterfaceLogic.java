package gripe._90.appliede.me.misc;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

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
import appeng.api.storage.AEKeyFilter;
import appeng.api.storage.MEStorage;
import appeng.capabilities.Capabilities;
import appeng.helpers.externalstorage.GenericStackItemStorage;
import appeng.me.storage.DelegatingMEInventory;
import appeng.util.ConfigInventory;
import appeng.util.Platform;

import gripe._90.appliede.me.service.KnowledgeService;

@SuppressWarnings("UnstableApiUsage")
public class EMCInterfaceLogic implements IActionHost, IGridTickable {
    protected final EMCInterfaceLogicHost host;
    protected final IManagedGridNode mainNode;

    @Nullable
    private MEStorage localInvHandler;

    private final ConfigInventory config;
    private final ConfigInventory storage;

    protected final IActionSource requestSource = new RequestSource();
    private final GenericStack[] plannedWork;
    private int priority = 0;

    private final LazyOptional<IItemHandler> storageHolder;
    private final LazyOptional<MEStorage> localInvHolder;

    public EMCInterfaceLogic(IManagedGridNode node, EMCInterfaceLogicHost host) {
        this(node, host, 9);
    }

    public EMCInterfaceLogic(IManagedGridNode node, EMCInterfaceLogicHost host, int slots) {
        this.host = host;
        config = ConfigInventory.configStacks(AEItemKey.filter(), slots, this::onConfigRowChanged, false);
        storage = ConfigInventory.storage(new StorageFilter(), slots, this::onStorageChanged);
        plannedWork = new GenericStack[slots];
        mainNode = node.setFlags(GridFlags.REQUIRE_CHANNEL)
                .addService(IGridTickable.class, this)
                .setIdlePowerUsage(10);

        config.useRegisteredCapacities();
        storage.useRegisteredCapacities();

        storageHolder = LazyOptional.of(() -> storage).lazyMap(GenericStackItemStorage::new);
        localInvHolder = LazyOptional.of(this::getInventory);
    }

    public ConfigInventory getConfig() {
        return config;
    }

    public ConfigInventory getStorage() {
        return storage;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
        host.saveChanges();
    }

    public void readFromNBT(CompoundTag tag) {
        config.readFromChildTag(tag, "config");
        storage.readFromChildTag(tag, "storage");
        priority = tag.getInt("priority");

        updatePlan();
        notifyNeighbours();
    }

    public void writeToNBT(CompoundTag tag) {
        config.writeToChildTag(tag, "config");
        storage.writeToChildTag(tag, "storage");
        tag.putInt("priority", priority);
    }

    private MEStorage getInventory() {
        if (localInvHandler == null) {
            localInvHandler = new Inventory();
        }

        return localInvHandler;
    }

    @Nullable
    @Override
    public IGridNode getActionableNode() {
        return mainNode.getNode();
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(5, 120, !hasWorkToDo(), true);
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

        var knowledge = grid.getService(KnowledgeService.class);

        if (!knowledge.knowsItem(item)) {
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
                    .insertItem(item, amount, Actionable.MODULATE, requestSource);

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
                .extractItem(item, amount, Actionable.MODULATE, requestSource, true);

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

    private void onConfigRowChanged() {
        host.saveChanges();
        updatePlan();
        notifyNeighbours();
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

    public void addDrops(List<ItemStack> drops) {
        for (var i = 0; i < storage.size(); i++) {
            var stack = storage.getStack(i);

            if (stack != null) {
                stack.what()
                        .addDrops(
                                stack.amount(),
                                drops,
                                host.getBlockEntity().getLevel(),
                                host.getBlockEntity().getBlockPos());
            }
        }
    }

    public <T> LazyOptional<T> getCapability(Capability<T> cap) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return storageHolder.cast();
        } else if (cap == Capabilities.STORAGE) {
            return localInvHolder.cast();
        } else {
            return LazyOptional.empty();
        }
    }

    public void invalidateCaps() {
        storageHolder.invalidate();
        localInvHolder.invalidate();
    }

    private class Inventory extends DelegatingMEInventory {
        private Inventory() {
            super(storage);
        }

        @Override
        public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
            return getRequestInterfacePriority(source).isPresent() && isSameGrid(source)
                    ? 0
                    : super.insert(what, amount, mode, source);
        }

        @Override
        public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
            var requestPriority = getRequestInterfacePriority(source);
            return requestPriority.isPresent() && requestPriority.getAsInt() <= getPriority() && isSameGrid(source)
                    ? 0
                    : super.extract(what, amount, mode, source);
        }

        private OptionalInt getRequestInterfacePriority(IActionSource source) {
            return source.context(RequestContext.class)
                    .map(ctx -> OptionalInt.of(ctx.getPriority()))
                    .orElseGet(OptionalInt::empty);
        }

        private boolean isSameGrid(IActionSource source) {
            return source.machine()
                            .map(IActionHost::getActionableNode)
                            .map(IGridNode::getGrid)
                            .orElse(null)
                    == mainNode.getGrid();
        }

        @Override
        public Component getDescription() {
            return host.getMainMenuIcon().getHoverName();
        }
    }

    private class RequestSource implements IActionSource {
        private final RequestContext context = new RequestContext();

        @Override
        public Optional<Player> player() {
            return Optional.empty();
        }

        @Override
        public Optional<IActionHost> machine() {
            return Optional.of(EMCInterfaceLogic.this);
        }

        @Override
        public <T> Optional<T> context(Class<T> key) {
            return key == RequestContext.class ? Optional.of(key.cast(context)) : Optional.empty();
        }
    }

    private class RequestContext {
        public int getPriority() {
            return priority;
        }
    }

    private class StorageFilter implements AEKeyFilter {
        @Override
        public boolean matches(AEKey what) {
            if (!(what instanceof AEItemKey item)) {
                return false;
            }

            var grid = mainNode.getGrid();
            return grid == null || grid.getService(KnowledgeService.class).knowsItem(item);
        }
    }
}
