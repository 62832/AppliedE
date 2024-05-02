package gripe._90.appliede.iface;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.IconButton;
import appeng.core.localization.ButtonToolTips;
import appeng.init.client.InitScreens;
import appeng.menu.SlotSemantics;

import gripe._90.appliede.AppliedE;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = AppliedE.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EMCInterfaceScreen extends AEBaseScreen<EMCInterfaceMenu> {
    private final List<Button> amountButtons = new ArrayList<>();

    public EMCInterfaceScreen(EMCInterfaceMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);

        widgets.addOpenPriorityButton();
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

    @SubscribeEvent
    public static void register(FMLClientSetupEvent event) {
        event.enqueueWork(() -> InitScreens.register(
                EMCInterfaceMenu.TYPE, EMCInterfaceScreen::new, "/screens/appliede/emc_interface.json"));
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
