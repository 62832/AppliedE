package gripe._90.appliede.part;

import java.util.List;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.helpers.IPriorityHost;
import appeng.items.parts.PartModels;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.PriorityMenu;
import appeng.menu.locator.MenuLocators;
import appeng.parts.AEBasePart;
import appeng.parts.PartModel;

import gripe._90.appliede.AppliedE;
import gripe._90.appliede.AppliedEConfig;
import gripe._90.appliede.me.misc.TransmutationPattern;
import gripe._90.appliede.me.service.KnowledgeService;

public final class EMCModulePart extends AEBasePart
        implements IStorageProvider, ICraftingProvider, IPriorityHost, IGridTickable {
    @PartModels
    private static final IPartModel MODEL = new PartModel(AppliedE.id("part/emc_module"));

    private final Object2LongMap<AEKey> outputs = new Object2LongOpenHashMap<>();

    private int priority = 0;

    public EMCModulePart(IPartItem<?> partItem) {
        super(partItem);
        getMainNode()
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .addService(IStorageProvider.class, this)
                .addService(ICraftingProvider.class, this)
                .addService(IGridTickable.class, this)
                .setIdlePowerUsage(AppliedEConfig.CONFIG.getModuleEnergyUsage());
    }

    @Override
    public void writeToNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.writeToNBT(data, registries);
        data.putInt("priority", priority);
    }

    @Override
    public void readFromNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.readFromNBT(data, registries);
        priority = data.getInt("priority");
    }

    @Override
    protected void onMainNodeStateChanged(IGridNodeListener.State reason) {
        super.onMainNodeStateChanged(reason);
        IStorageProvider.requestUpdate(getMainNode());
        ICraftingProvider.requestUpdate(getMainNode());
    }

    @Override
    public void mountInventories(IStorageMounts mounts) {
        var grid = getMainNode().getGrid();

        if (grid != null) {
            mounts.mount(grid.getService(KnowledgeService.class).getStorage(getMainNode()));
        }
    }

    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        var grid = getMainNode().getGrid();
        return grid != null ? grid.getService(KnowledgeService.class).getPatterns(getMainNode()) : List.of();
    }

    @Override
    public int getPatternPriority() {
        return priority;
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        if (!getMainNode().isActive() || !(patternDetails instanceof TransmutationPattern pattern)) {
            return false;
        }

        var output = pattern.getPrimaryOutput();
        outputs.merge(output.what(), output.amount(), Long::sum);

        getMainNode().ifPresent((grid, node) -> grid.getTickManager().alertDevice(node));
        return true;
    }

    @Override
    public boolean isBusy() {
        return false;
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(1, 1, outputs.isEmpty());
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        var storage = node.getGrid().getStorageService().getInventory();

        for (var output : new Object2LongOpenHashMap<>(outputs).object2LongEntrySet()) {
            var what = output.getKey();
            var amount = output.getLongValue();
            var inserted = storage.insert(what, amount, Actionable.MODULATE, IActionSource.ofMachine(this));

            if (inserted >= amount) {
                outputs.removeLong(what);
            } else if (inserted > 0) {
                outputs.put(what, amount - inserted);
            }
        }

        return TickRateModulation.URGENT;
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        bch.addBox(3, 3, 12, 13, 13, 16);
        bch.addBox(5, 5, 11, 11, 11, 12);
    }

    @Override
    public IPartModel getStaticModels() {
        return MODEL;
    }

    @Override
    public boolean onUseWithoutItem(Player player, Vec3 pos) {
        if (!player.getCommandSenderWorld().isClientSide()) {
            MenuOpener.open(PriorityMenu.TYPE, player, MenuLocators.forPart(this));
        }

        return true;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void setPriority(int newPriority) {
        priority = newPriority;
        getHost().markForSave();
        IStorageProvider.requestUpdate(getMainNode());
        ICraftingProvider.requestUpdate(getMainNode());
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        player.closeContainer();
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return AppliedE.EMC_MODULE.asItem().getDefaultInstance();
    }
}
