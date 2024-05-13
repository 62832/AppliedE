package gripe._90.appliede.part;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.MenuType;

import appeng.api.behaviors.StackExportStrategy;
import appeng.api.config.SchedulingMode;
import appeng.api.config.Settings;
import appeng.api.networking.IGrid;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.stacks.AEItemKey;
import appeng.core.AppEng;
import appeng.core.settings.TickRates;
import appeng.items.parts.PartModels;
import appeng.menu.implementations.IOBusMenu;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.parts.PartModel;
import appeng.parts.automation.IOBusPart;
import appeng.util.prioritylist.DefaultPriorityList;

import gripe._90.appliede.AppliedE;
import gripe._90.appliede.me.service.KnowledgeService;
import gripe._90.appliede.me.strategy.EMCItemExportStrategy;
import gripe._90.appliede.me.strategy.EMCTransferContext;

@SuppressWarnings("UnstableApiUsage")
public class EMCExportBusPart extends IOBusPart {
    public static final MenuType<IOBusMenu> MENU =
            MenuTypeBuilder.create(IOBusMenu::new, EMCExportBusPart.class).build("emc_export_bus");

    private static final ResourceLocation MODEL_BASE = AppliedE.id("part/emc_export_bus");

    @PartModels
    private static final PartModel MODELS_OFF = new PartModel(MODEL_BASE, AppEng.makeId("part/export_bus_off"));

    @PartModels
    private static final PartModel MODELS_ON = new PartModel(MODEL_BASE, AppEng.makeId("part/export_bus_on"));

    @PartModels
    private static final PartModel MODELS_HAS_CHANNEL =
            new PartModel(MODEL_BASE, AppEng.makeId("part/export_bus_has_channel"));

    private StackExportStrategy strategy;
    private int nextSlot = 0;

    public EMCExportBusPart(IPartItem<?> partItem) {
        super(TickRates.ExportBus, AEItemKey.filter(), partItem);
        getConfigManager().registerSetting(Settings.SCHEDULING_MODE, SchedulingMode.DEFAULT);
    }

    @Override
    public void readFromNBT(CompoundTag extra) {
        super.readFromNBT(extra);
        nextSlot = extra.getInt("nextSlot");
    }

    @Override
    public void writeToNBT(CompoundTag extra) {
        super.writeToNBT(extra);
        extra.putInt("nextSlot", nextSlot);
    }

    @Override
    protected boolean doBusWork(IGrid grid) {
        var emcStorage = grid.getService(KnowledgeService.class).getStorage();
        var schedulingMode = getConfigManager().getSetting(Settings.SCHEDULING_MODE);
        var context = new EMCTransferContext(emcStorage, source, DefaultPriorityList.INSTANCE, getOperationsPerTick());
        var slot = 0;

        for (slot = 0; slot < availableSlots() && context.hasOperationsLeft(); slot++) {
            var what = getConfig().getKey(getStartingSlot(schedulingMode, slot));

            if (!(what instanceof AEItemKey item)) {
                continue;
            }

            var amount = getStrategy().transfer(context, item, context.getOperationsRemaining());

            if (amount > 0) {
                context.reduceOperationsRemaining(amount);
            }
        }

        if (context.hasDoneWork()) {
            updateSchedulingMode(schedulingMode, slot);
        }

        return context.hasDoneWork();
    }

    private StackExportStrategy getStrategy() {
        if (strategy == null) {
            var self = getHost().getBlockEntity();
            var fromPos = self.getBlockPos().relative(getSide());
            var fromSide = getSide().getOpposite();
            strategy = new EMCItemExportStrategy((ServerLevel) getLevel(), fromPos, fromSide);
        }

        return strategy;
    }

    private int getStartingSlot(SchedulingMode schedulingMode, int x) {
        if (schedulingMode == SchedulingMode.RANDOM) {
            return getLevel().getRandom().nextInt(availableSlots());
        }

        if (schedulingMode == SchedulingMode.ROUNDROBIN) {
            return (nextSlot + x) % availableSlots();
        }

        return x;
    }

    private void updateSchedulingMode(SchedulingMode schedulingMode, int x) {
        if (schedulingMode == SchedulingMode.ROUNDROBIN) {
            nextSlot = (nextSlot + x) % availableSlots();
        }
    }

    @Override
    protected MenuType<?> getMenuType() {
        return MENU;
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
