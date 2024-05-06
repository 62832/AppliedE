package gripe._90.appliede.client.screen;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.client.gui.Icon;
import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.IconButton;
import appeng.core.localization.ButtonToolTips;
import appeng.menu.SlotSemantics;

import gripe._90.appliede.menu.EMCInterfaceMenu;

public class EMCInterfaceScreen<M extends EMCInterfaceMenu> extends UpgradeableScreen<M> {
    private final List<Button> amountButtons = new ArrayList<>();

    public EMCInterfaceScreen(M menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        var configSlots = menu.getSlots(SlotSemantics.CONFIG);

        for (int i = 0; i < configSlots.size(); i++) {
            var button = new SetAmountButton(btn -> {
                var idx = amountButtons.indexOf(btn);
                var configSlot = configSlots.get(idx);
                menu.openSetAmountMenu(configSlot.slot);
            });

            button.setDisableBackground(true);
            button.setMessage(ButtonToolTips.InterfaceSetStockAmount.text());
            widgets.add("amtButton" + (1 + i), button);
            amountButtons.add(button);
        }
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        for (int i = 0; i < amountButtons.size(); i++) {
            var button = amountButtons.get(i);
            var item = menu.getSlots(SlotSemantics.CONFIG).get(i).getItem();
            button.visible = !item.isEmpty();
        }
    }

    private static class SetAmountButton extends IconButton {
        public SetAmountButton(OnPress onPress) {
            super(onPress);
        }

        @Override
        protected Icon getIcon() {
            return isHoveredOrFocused() ? Icon.WRENCH : Icon.WRENCH_DISABLED;
        }
    }
}
