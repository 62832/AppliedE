package gripe._90.appliede.part;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import appeng.api.config.Actionable;
import appeng.api.config.SchedulingMode;
import appeng.api.config.Settings;
import appeng.api.networking.IGrid;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.storage.StorageHelper;
import appeng.api.util.IConfigManagerBuilder;
import appeng.core.AppEng;
import appeng.core.settings.TickRates;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;
import appeng.parts.automation.IOBusPart;

import gripe._90.appliede.AppliedE;
import gripe._90.appliede.me.key.EMCKey;
import gripe._90.appliede.me.key.EMCKeyType;
import gripe._90.appliede.me.misc.TransmutationCapable;

import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.block_entity.IEmcStorage;

public class EMCExportBusPart extends IOBusPart implements TransmutationCapable {
    private static final ResourceLocation MODEL_BASE = AppliedE.id("part/emc_export_bus");

    @PartModels
    private static final PartModel MODELS_OFF = new PartModel(MODEL_BASE, AppEng.makeId("part/export_bus_off"));

    @PartModels
    private static final PartModel MODELS_ON = new PartModel(MODEL_BASE, AppEng.makeId("part/export_bus_on"));

    @PartModels
    private static final PartModel MODELS_HAS_CHANNEL =
            new PartModel(MODEL_BASE, AppEng.makeId("part/export_bus_has_channel"));

    private static final Logger LOGGER = LoggerFactory.getLogger(EMCExportBusPart.class);

    private BlockCapabilityCache<IItemHandler, Direction> itemCache;
    private BlockCapabilityCache<IEmcStorage, Direction> emcCache;

    private int nextSlot = 0;

    public EMCExportBusPart(IPartItem<?> partItem) {
        super(TickRates.ExportBus, Set.of(AEKeyType.items(), EMCKeyType.TYPE), partItem);
    }

    @Override
    public void readFromNBT(CompoundTag extra, HolderLookup.Provider registries) {
        super.readFromNBT(extra, registries);
        nextSlot = extra.getInt("nextSlot");
    }

    @Override
    public void writeToNBT(CompoundTag extra, HolderLookup.Provider registries) {
        super.writeToNBT(extra, registries);
        extra.putInt("nextSlot", nextSlot);
    }

    @Override
    protected void registerSettings(IConfigManagerBuilder builder) {
        super.registerSettings(builder);
        builder.registerSetting(Settings.SCHEDULING_MODE, SchedulingMode.DEFAULT);
    }

    @Override
    protected boolean doBusWork(IGrid grid) {
        if (itemCache == null || emcCache == null) {
            var adjacentPos = getHost().getBlockEntity().getBlockPos().relative(Objects.requireNonNull(getSide()));
            var facing = getSide().getOpposite();
            var level = (ServerLevel) getLevel();
            itemCache = BlockCapabilityCache.create(Capabilities.ItemHandler.BLOCK, level, adjacentPos, facing);
            emcCache = BlockCapabilityCache.create(PECapabilities.EMC_STORAGE_CAPABILITY, level, adjacentPos, facing);
        }

        var doneWork = false;
        var schedulingMode = getConfigManager().getSetting(Settings.SCHEDULING_MODE);
        var remaining = new AtomicInteger(getOperationsPerTick());
        var slot = 0;

        for (slot = 0; slot < availableSlots() && remaining.get() > 0; slot++) {
            var startingSlot =
                    switch (schedulingMode) {
                        case RANDOM -> getLevel().getRandom().nextInt(availableSlots());
                        case ROUNDROBIN -> (nextSlot + slot) % availableSlots();
                        default -> slot;
                    };
            var what = getConfig().getKey(startingSlot);

            if (what == EMCKey.BASE && emcCache.getCapability() instanceof IEmcStorage handler) {
                var rem = remaining.get() * EMCKeyType.TYPE.getAmountPerOperation();
                var insertable = handler.insertEmc(rem, IEmcStorage.EmcAction.SIMULATE);
                var extracted = StorageHelper.poweredExtraction(
                        grid.getEnergyService(),
                        grid.getStorageService().getInventory(),
                        EMCKey.BASE,
                        insertable,
                        source,
                        Actionable.MODULATE);

                if (extracted > 0) {
                    handler.insertEmc(extracted, IEmcStorage.EmcAction.EXECUTE);
                    remaining.addAndGet((int) -Math.max(1, extracted / EMCKeyType.TYPE.getAmountPerOperation()));
                }
            } else if (what instanceof AEItemKey item && itemCache.getCapability() instanceof IItemHandler handler) {
                var networkStorage = grid.getStorageService().getInventory();

                var rem = remaining.get();
                var stack = item.toStack(rem);
                var extracted = networkStorage.extract(item, rem, Actionable.SIMULATE, source);
                var remainder = ItemHandlerHelper.insertItem(handler, stack, true);
                var wasInserted = extracted - remainder.getCount();

                if (wasInserted > 0) {
                    extracted = networkStorage.extract(item, rem, Actionable.MODULATE, source);
                    remainder = ItemHandlerHelper.insertItem(handler, stack, false);
                    wasInserted = extracted - remainder.getCount();

                    if (wasInserted < extracted) {
                        var leftover = extracted - wasInserted;
                        leftover -= networkStorage.insert(item, rem, Actionable.MODULATE, source);

                        if (leftover > 0) {
                            LOGGER.error(
                                    "Storage export: adjacent block unexpectedly refused insert, voided {}x{}",
                                    leftover,
                                    item);
                        }
                    }
                }

                if (wasInserted > 0) {
                    remaining.addAndGet(-(int) wasInserted);
                }
            }
        }

        if (remaining.get() < getOperationsPerTick()) {
            if (schedulingMode == SchedulingMode.ROUNDROBIN) {
                nextSlot = (nextSlot + slot) % availableSlots();
            }

            doneWork = true;
        }

        return doneWork;
    }

    @Override
    public boolean mayLearn() {
        return false;
    }

    @Override
    public boolean consumePowerOnInsert() {
        return false;
    }

    @Override
    protected MenuType<?> getMenuType() {
        return AppliedE.EMC_EXPORT_BUS_MENU.get();
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        bch.addBox(4, 4, 12, 12, 12, 14);
        bch.addBox(5, 5, 14, 11, 11, 15);
        bch.addBox(6, 6, 15, 10, 10, 16);
        bch.addBox(6, 6, 11, 10, 10, 12);
    }

    @Override
    public IPartModel getStaticModels() {
        return isActive() ? MODELS_HAS_CHANNEL : isPowered() ? MODELS_ON : MODELS_OFF;
    }
}
