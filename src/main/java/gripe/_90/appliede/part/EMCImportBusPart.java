package gripe._90.appliede.part;

import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.primitives.Ints;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.stacks.AEItemKey;
import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.core.settings.TickRates;
import appeng.items.parts.PartModels;
import appeng.me.storage.ExternalStorageFacade;
import appeng.menu.implementations.IOBusMenu;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.parts.PartModel;
import appeng.parts.automation.IOBusPart;

import gripe._90.appliede.AppliedE;
import gripe._90.appliede.me.service.KnowledgeService;

public class EMCImportBusPart extends IOBusPart {
    public static final MenuType<IOBusMenu> MENU =
            MenuTypeBuilder.create(IOBusMenu::new, EMCImportBusPart.class).build("emc_import_bus");

    private static final ResourceLocation MODEL_BASE = AppliedE.id("part/emc_import_bus");

    @PartModels
    private static final PartModel MODELS_OFF = new PartModel(MODEL_BASE, AppEng.makeId("part/import_bus_off"));

    @PartModels
    private static final PartModel MODELS_ON = new PartModel(MODEL_BASE, AppEng.makeId("part/import_bus_on"));

    @PartModels
    private static final PartModel MODELS_HAS_CHANNEL =
            new PartModel(MODEL_BASE, AppEng.makeId("part/import_bus_has_channel"));

    public EMCImportBusPart(IPartItem<?> partItem) {
        super(TickRates.ImportBus, AEItemKey.filter(), partItem);
    }

    @Override
    protected MenuType<?> getMenuType() {
        return MENU;
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    protected boolean doBusWork(IGrid grid) {
        var adjacentPos = getHost().getBlockEntity().getBlockPos().relative(getSide());
        var facing = getSide().getOpposite();
        var blockEntity = getLevel().getBlockEntity(adjacentPos);

        if (blockEntity == null) {
            return false;
        }

        var doneWork = new AtomicBoolean(false);

        blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, facing).ifPresent(itemHandler -> {
            var emcStorage = grid.getService(KnowledgeService.class).getStorage();
            var adjacentStorage = ExternalStorageFacade.of(itemHandler);
            var remaining = getOperationsPerTick();

            for (var slot = 0; slot < itemHandler.getSlots() && remaining > 0; slot++) {
                var item = AEItemKey.of(itemHandler.getStackInSlot(slot));

                if (item == null) {
                    continue;
                }

                if (!getFilter().isEmpty() && getFilter().isListed(item) == isUpgradedWith(AEItems.INVERTER_CARD)) {
                    continue;
                }

                var amount = adjacentStorage.extract(item, remaining, Actionable.SIMULATE, source);

                if (amount > 0) {
                    var mayLearn = isUpgradedWith(AppliedE.LEARNING_CARD.get());
                    amount = emcStorage.insertItem(item, amount, Actionable.MODULATE, source, mayLearn);
                    adjacentStorage.extract(item, amount, Actionable.MODULATE, source);
                    remaining -= Ints.saturatedCast(amount);
                }
            }

            if (remaining < getOperationsPerTick()) {
                doneWork.set(true);
            }
        });

        return doneWork.get();
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
