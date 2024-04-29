package gripe._90.appliede.module;

import java.util.List;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;

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
import gripe._90.appliede.service.KnowledgeService;

import moze_intel.projecte.api.event.PlayerKnowledgeChangeEvent;

public final class EMCModulePart extends AEBasePart
        implements IStorageProvider, ICraftingProvider, IPriorityHost, IGridTickable {
    @PartModels
    private static final IPartModel MODEL = new PartModel(AppliedE.id("part/emc_module"));

    private final Object2LongMap<AEKey> outputs = new Object2LongOpenHashMap<>();

    private boolean wasOnline = false;
    private int priority = 0;

    public EMCModulePart(IPartItem<?> partItem) {
        super(partItem);
        getMainNode()
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .addService(IStorageProvider.class, this)
                .addService(ICraftingProvider.class, this)
                .addService(IGridTickable.class, this)
                .setIdlePowerUsage(5.0);

        MinecraftForge.EVENT_BUS.addListener(
                (PlayerKnowledgeChangeEvent event) -> ICraftingProvider.requestUpdate(getMainNode()));
    }

    @Override
    public void writeToNBT(CompoundTag data) {
        super.writeToNBT(data);
        data.putInt("priority", priority);
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        super.readFromNBT(data);
        priority = data.getInt("priority");
    }

    @Override
    protected void onMainNodeStateChanged(IGridNodeListener.State reason) {
        super.onMainNodeStateChanged(reason);
        var online = getMainNode().isOnline();

        if (wasOnline != online) {
            wasOnline = online;
            IStorageProvider.requestUpdate(getMainNode());
            ICraftingProvider.requestUpdate(getMainNode());
        }
    }

    @Override
    public void mountInventories(IStorageMounts mounts) {
        var grid = getMainNode().getGrid();

        if (grid != null) {
            mounts.mount(grid.getService(KnowledgeService.class).getStorage(this));
        }
    }

    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        var grid = getMainNode().getGrid();
        return grid != null ? grid.getService(KnowledgeService.class).getPatterns() : List.of();
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
        return new TickingRequest(1, 1, outputs.isEmpty(), true);
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
    public boolean onPartActivate(Player player, InteractionHand hand, Vec3 pos) {
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
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.closeContainer();
        }
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return AppliedE.EMC_MODULE.get().getDefaultInstance();
    }
}
