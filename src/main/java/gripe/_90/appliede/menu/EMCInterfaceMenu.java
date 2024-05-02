package gripe._90.appliede.menu;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import appeng.api.stacks.AEItemKey;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.InterfaceMenu;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.slot.FakeSlot;

import gripe._90.appliede.me.helpers.EMCInterfaceLogicHost;

public class EMCInterfaceMenu extends AEBaseMenu {
    private static final String ACTION_OPEN_SET_AMOUNT = InterfaceMenu.ACTION_OPEN_SET_AMOUNT;

    public static final MenuType<EMCInterfaceMenu> TYPE = MenuTypeBuilder.create(
                    EMCInterfaceMenu::new, EMCInterfaceLogicHost.class)
            .build("emc_interface");

    private final EMCInterfaceLogicHost host;

    public EMCInterfaceMenu(MenuType<?> menuType, int id, Inventory playerInventory, EMCInterfaceLogicHost host) {
        super(menuType, id, playerInventory, host);
        this.host = host;
        createPlayerInventorySlots(playerInventory);

        registerClientAction(ACTION_OPEN_SET_AMOUNT, Integer.class, this::openSetAmountMenu);

        var logic = host.getInterfaceLogic();
        var config = logic.getConfig().createMenuWrapper();
        var storage = logic.getStorage().createMenuWrapper();

        for (var i = 0; i < config.size(); i++) {
            addSlot(new FakeSlot(config, i), SlotSemantics.CONFIG);
        }

        for (var i = 0; i < storage.size(); i++) {
            addSlot(new AppEngSlot(storage, i), SlotSemantics.STORAGE);
        }
    }

    public void openSetAmountMenu(int configSlot) {
        if (isClientSide()) {
            sendClientAction(ACTION_OPEN_SET_AMOUNT, configSlot);
        } else {
            var stack = host.getConfig().getStack(configSlot);

            if (stack != null && stack.what() instanceof AEItemKey item) {
                EMCSetStockAmountMenu.open(
                        (ServerPlayer) getPlayer(), getLocator(), configSlot, item, (int) stack.amount());
            }
        }
    }
}
