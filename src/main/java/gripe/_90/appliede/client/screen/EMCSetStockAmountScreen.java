package gripe._90.appliede.client.screen;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.NumberEntryType;
import appeng.client.gui.implementations.AESubScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.NumberEntryWidget;
import appeng.core.localization.GuiText;

import gripe._90.appliede.menu.EMCSetStockAmountMenu;

public class EMCSetStockAmountScreen extends AEBaseScreen<EMCSetStockAmountMenu> {
    private final NumberEntryWidget amount;
    private boolean amountInitialised;

    public EMCSetStockAmountScreen(
            EMCSetStockAmountMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);

        widgets.addButton("save", GuiText.Set.text(), this::confirm);
        AESubScreen.addBackButton(menu, "back", widgets);

        amount = widgets.addNumberEntryWidget("amountToStock", NumberEntryType.UNITLESS);
        amount.setLongValue(1);
        amount.setTextFieldStyle(style.getWidget("amountToStockInput"));
        amount.setMinValue(0);
        amount.setHideValidationIcon(true);
        amount.setOnConfirm(this::confirm);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        if (!amountInitialised) {
            var whatToStock = menu.getWhatToStock();
            if (whatToStock != null) {
                amount.setType(NumberEntryType.of(whatToStock));
                amount.setLongValue(menu.getInitialAmount());

                amount.setMaxValue(menu.getMaxAmount());
                amountInitialised = true;
            }
        }
    }

    private void confirm() {
        amount.getIntValue().ifPresent(menu::confirm);
    }
}
