package gripe._90.appliede.part;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import appeng.api.behaviors.StackImportStrategy;
import appeng.api.networking.IGrid;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.stacks.AEItemKey;
import appeng.client.gui.implementations.IOBusScreen;
import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.core.settings.TickRates;
import appeng.init.client.InitScreens;
import appeng.items.parts.PartModels;
import appeng.menu.implementations.IOBusMenu;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.parts.PartModel;
import appeng.parts.automation.IOBusPart;

import gripe._90.appliede.AppliedE;
import gripe._90.appliede.me.misc.EMCTransferContext;
import gripe._90.appliede.me.service.KnowledgeService;
import gripe._90.appliede.me.strategy.EMCItemImportStrategy;

@SuppressWarnings("UnstableApiUsage")
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

    private StackImportStrategy importStrategy;

    public EMCImportBusPart(IPartItem<?> partItem) {
        super(TickRates.ImportBus, AEItemKey.filter(), partItem);
    }

    public static void registerScreen(FMLClientSetupEvent event) {
        event.enqueueWork(() -> InitScreens.register(MENU, IOBusScreen::new, "/screens/import_bus.json"));
    }

    @Override
    protected MenuType<?> getMenuType() {
        return MENU;
    }

    @Override
    protected boolean doBusWork(IGrid grid) {
        if (importStrategy == null) {
            var self = getHost().getBlockEntity();
            var fromPos = self.getBlockPos().relative(getSide());
            var fromSide = getSide().getOpposite();
            importStrategy = new EMCItemImportStrategy((ServerLevel) getLevel(), fromPos, fromSide);
        }

        var context = new EMCTransferContext(
                grid.getService(KnowledgeService.class).getStorage(), source, getFilter(), getOperationsPerTick());
        context.setInverted(this.isUpgradedWith(AEItems.INVERTER_CARD));
        importStrategy.transfer(context);
        return context.hasDoneWork();
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        bch.addBox(6, 6, 11, 10, 10, 13);
        bch.addBox(5, 5, 13, 11, 11, 14);
        bch.addBox(4, 4, 14, 12, 12, 16);
    }

    @Override
    public IPartModel getStaticModels() {
        if (this.isActive() && this.isPowered()) {
            return MODELS_HAS_CHANNEL;
        } else if (this.isPowered()) {
            return MODELS_ON;
        } else {
            return MODELS_OFF;
        }
    }
}
