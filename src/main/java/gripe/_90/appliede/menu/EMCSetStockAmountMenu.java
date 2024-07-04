package gripe._90.appliede.menu;

import java.util.Objects;

import com.google.common.primitives.Ints;

import org.jetbrains.annotations.Nullable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.menu.AEBaseMenu;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.implementations.SetStockAmountMenu;
import appeng.menu.locator.MenuLocator;
import appeng.menu.slot.InaccessibleSlot;
import appeng.util.inv.AppEngInternalInventory;

import gripe._90.appliede.me.misc.EMCInterfaceLogicHost;

public class EMCSetStockAmountMenu extends AEBaseMenu implements ISubMenu {
    public static final MenuType<EMCSetStockAmountMenu> TYPE = MenuTypeBuilder.create(
                    EMCSetStockAmountMenu::new, EMCInterfaceLogicHost.class)
            .build("emc_set_stock_amount");

    private final EMCInterfaceLogicHost host;
    private final Slot stockedItem;
    private AEItemKey whatToStock;

    @GuiSync(1)
    private int initialAmount = -1;

    @GuiSync(2)
    private int maxAmount = -1;

    private int slot;

    public EMCSetStockAmountMenu(int id, Inventory playerInventory, EMCInterfaceLogicHost host) {
        super(TYPE, id, playerInventory, host);
        registerClientAction(SetStockAmountMenu.ACTION_SET_STOCK_AMOUNT, Integer.class, this::confirm);
        this.host = host;
        stockedItem = new InaccessibleSlot(new AppEngInternalInventory(1), 0);
        addSlot(stockedItem, SlotSemantics.MACHINE_OUTPUT);
    }

    @Override
    public EMCInterfaceLogicHost getHost() {
        return host;
    }

    public static void open(ServerPlayer player, MenuLocator locator, int slot, AEItemKey toStock, int initialAmount) {
        MenuOpener.open(TYPE, player, locator);

        if (player.containerMenu instanceof EMCSetStockAmountMenu stockMenu) {
            stockMenu.setWhatToStock(slot, toStock, initialAmount);
            stockMenu.broadcastChanges();
        }
    }

    private void setWhatToStock(int slot, AEItemKey whatToStock, int initialAmount) {
        this.slot = slot;
        this.whatToStock = Objects.requireNonNull(whatToStock, "whatToStock");
        this.initialAmount = initialAmount;
        maxAmount = Ints.saturatedCast(host.getConfig().getMaxAmount(whatToStock));
        stockedItem.set(whatToStock.wrapForDisplayOrFilter());
    }

    public int getMaxAmount() {
        return maxAmount;
    }

    public void confirm(int amount) {
        if (isClientSide()) {
            sendClientAction(SetStockAmountMenu.ACTION_SET_STOCK_AMOUNT, amount);
            return;
        }

        var config = host.getConfig();

        if (!Objects.equals(config.getKey(slot), whatToStock)) {
            host.returnToMainMenu(getPlayer(), this);
            return;
        }

        amount = (int) Math.min(amount, config.getMaxAmount(whatToStock));
        config.setStack(slot, amount <= 0 ? null : new GenericStack(whatToStock, amount));
        host.returnToMainMenu(getPlayer(), this);
    }

    public int getInitialAmount() {
        return initialAmount;
    }

    @Nullable
    public AEItemKey getWhatToStock() {
        var stack = GenericStack.fromItemStack(stockedItem.getItem());
        return stack != null && stack.what() instanceof AEItemKey item ? item : null;
    }
}
