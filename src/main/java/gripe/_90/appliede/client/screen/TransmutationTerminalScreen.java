package gripe._90.appliede.client.screen;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import appeng.client.gui.Icon;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ToggleButton;

import gripe._90.appliede.AppliedE;
import gripe._90.appliede.me.misc.LearnAllItemsPacket;
import gripe._90.appliede.menu.TransmutationTerminalMenu;

public class TransmutationTerminalScreen<C extends TransmutationTerminalMenu> extends MEStorageScreen<C> {
    private static final Component SHIFT_STORING = guiString("shift_storing");
    private static final Component SHIFT_TRANSMUTING = guiString("shift_transmuting");
    private static final Component TOGGLE_STORING = guiString("toggle_storage");
    private static final Component TOGGLE_TRANSMUTING = guiString("toggle_transmutation");

    private static final Component LEARN_ALL = guiString("learn_all");
    private static final Component ARE_YOU_SURE = guiString("are_you_sure");

    private final ToggleButton toggleShiftButton;
    private final ToggleButton learnAllButton;

    private boolean attemptingToLearn;

    public TransmutationTerminalScreen(C menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        toggleShiftButton = new ToggleButton(Icon.ARROW_LEFT, Icon.ARROW_UP, b -> menu.toggleShiftToTransmute());
        toggleShiftButton.setTooltipOn(List.of(SHIFT_TRANSMUTING, TOGGLE_STORING));
        toggleShiftButton.setTooltipOff(List.of(SHIFT_STORING, TOGGLE_TRANSMUTING));
        widgets.add("toggleShiftToTransmute", toggleShiftButton);

        var learnIcon = Icon.CONDENSER_OUTPUT_SINGULARITY;
        learnAllButton = new ToggleButton(learnIcon, learnIcon, this::learnAll);
        learnAllButton.setTooltipOff(List.of(LEARN_ALL));
        learnAllButton.setTooltipOn(List.of(LEARN_ALL, ARE_YOU_SURE));
        widgets.add("learnAllItems", learnAllButton);
    }

    private static Component guiString(String key) {
        return Component.translatable("gui." + AppliedE.MODID + "." + key);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        toggleShiftButton.setState(menu.shiftToTransmute);
        learnAllButton.setState(attemptingToLearn);
        setTextHidden("learned", menu.learnedLabelTicks <= 0);
        setTextHidden("unlearned", menu.unlearnedLabelTicks <= 0);

        if (menu.learnedLabelTicks > 0) {
            menu.decrementLearnedTicks();
        }

        if (menu.unlearnedLabelTicks > 0) {
            menu.decrementUnlearnedTicks();
        }
    }

    private void learnAll(boolean learned) {
        attemptingToLearn = learned;

        if (!learned) {
            PacketDistributor.sendToServer(LearnAllItemsPacket.INSTANCE);
        }
    }
}
