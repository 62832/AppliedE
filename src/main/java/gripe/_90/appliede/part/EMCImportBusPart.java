package gripe._90.appliede.part;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.storage.StorageHelper;
import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.core.settings.TickRates;
import appeng.items.parts.PartModels;
import appeng.me.storage.ExternalStorageFacade;
import appeng.parts.PartModel;
import appeng.parts.automation.IOBusPart;

import gripe._90.appliede.AppliedE;
import gripe._90.appliede.me.key.EMCKey;
import gripe._90.appliede.me.key.EMCKeyType;
import gripe._90.appliede.me.service.KnowledgeService;

import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.block_entity.IEmcStorage;

public class EMCImportBusPart extends IOBusPart {
    private static final ResourceLocation MODEL_BASE = AppliedE.id("part/emc_import_bus");

    @PartModels
    private static final PartModel MODELS_OFF = new PartModel(MODEL_BASE, AppEng.makeId("part/import_bus_off"));

    @PartModels
    private static final PartModel MODELS_ON = new PartModel(MODEL_BASE, AppEng.makeId("part/import_bus_on"));

    @PartModels
    private static final PartModel MODELS_HAS_CHANNEL =
            new PartModel(MODEL_BASE, AppEng.makeId("part/import_bus_has_channel"));

    private BlockCapabilityCache<IItemHandler, Direction> itemCache;
    private BlockCapabilityCache<IEmcStorage, Direction> emcCache;

    public EMCImportBusPart(IPartItem<?> partItem) {
        super(TickRates.ImportBus, Set.of(AEKeyType.items(), EMCKeyType.TYPE), partItem);
    }

    @Override
    protected MenuType<?> getMenuType() {
        return AppliedE.EMC_IMPORT_BUS_MENU.get();
    }

    @Override
    protected boolean doBusWork(IGrid grid) {
        if (itemCache == null && emcCache == null) {
            var adjacentPos = getHost().getBlockEntity().getBlockPos().relative(getSide());
            var facing = getSide().getOpposite();
            var level = (ServerLevel) getLevel();
            itemCache = BlockCapabilityCache.create(Capabilities.ItemHandler.BLOCK, level, adjacentPos, facing);
            emcCache = BlockCapabilityCache.create(PECapabilities.EMC_STORAGE_CAPABILITY, level, adjacentPos, facing);
        }

        var doneWork = false;

        var networkEmc = grid.getService(KnowledgeService.class).getStorage();
        var remaining = new AtomicInteger(getOperationsPerTick());

        if (emcCache.getCapability() instanceof IEmcStorage handler) {
            if (getFilter().isEmpty() || getFilter().isListed(EMCKey.BASE) != isUpgradedWith(AEItems.INVERTER_CARD)) {
                var emcRemaining = remaining.get() * EMCKeyType.TYPE.getAmountPerOperation();
                var inserted = StorageHelper.poweredInsert(
                        grid.getEnergyService(),
                        grid.getStorageService().getInventory(),
                        EMCKey.BASE,
                        Math.min(emcRemaining, handler.getStoredEmc()),
                        source,
                        Actionable.MODULATE);
                handler.extractEmc(inserted, IEmcStorage.EmcAction.EXECUTE);
                remaining.addAndGet((int) -Math.max(1, inserted / EMCKeyType.TYPE.getAmountPerOperation()));
            }
        }

        if (itemCache.getCapability() instanceof IItemHandler handler) {
            var adjacentStorage = ExternalStorageFacade.of(handler);

            for (var slot = 0; slot < handler.getSlots() && remaining.get() > 0; slot++) {
                var item = AEItemKey.of(handler.getStackInSlot(slot));

                if (item == null) {
                    continue;
                }

                if (!getFilter().isEmpty() && getFilter().isListed(item) == isUpgradedWith(AEItems.INVERTER_CARD)) {
                    continue;
                }

                var amount = adjacentStorage.extract(item, remaining.get(), Actionable.SIMULATE, source);

                if (amount > 0) {
                    var mayLearn = isUpgradedWith(AppliedE.LEARNING_CARD.get());
                    amount = networkEmc.insertItem(item, amount, Actionable.MODULATE, source, mayLearn);
                    adjacentStorage.extract(item, amount, Actionable.MODULATE, source);
                    remaining.addAndGet(-(int) amount);
                }
            }
        }

        if (remaining.get() < getOperationsPerTick()) {
            doneWork = true;
        }

        return doneWork;
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        bch.addBox(6, 6, 11, 10, 10, 13);
        bch.addBox(5, 5, 13, 11, 11, 14);
        bch.addBox(4, 4, 14, 12, 12, 16);
    }

    @Override
    public IPartModel getStaticModels() {
        return isActive() ? MODELS_HAS_CHANNEL : isPowered() ? MODELS_ON : MODELS_OFF;
    }
}
