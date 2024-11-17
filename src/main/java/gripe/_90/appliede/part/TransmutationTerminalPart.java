package gripe._90.appliede.part;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;
import appeng.parts.reporting.AbstractTerminalPart;

import gripe._90.appliede.AppliedE;
import gripe._90.appliede.me.misc.TransmutationTerminalHost;

public class TransmutationTerminalPart extends AbstractTerminalPart implements TransmutationTerminalHost {
    @PartModels
    public static final ResourceLocation MODEL_OFF = AppliedE.id("part/transmutation_terminal_off");

    @PartModels
    public static final ResourceLocation MODEL_ON = AppliedE.id("part/transmutation_terminal_on");

    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, MODEL_OFF, MODEL_STATUS_OFF);
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_ON);
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_HAS_CHANNEL);

    private boolean shiftToTransmute;

    public TransmutationTerminalPart(IPartItem<?> partItem) {
        super(partItem);
    }

    @Override
    public boolean getShiftToTransmute() {
        return shiftToTransmute;
    }

    @Override
    public void setShiftToTransmute(boolean shift) {
        shiftToTransmute = shift;
        saveChanges();
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        super.readFromNBT(data);
        shiftToTransmute = data.getBoolean("shiftToTransmute");
    }

    @Override
    public void writeToNBT(CompoundTag data) {
        super.writeToNBT(data);
        data.putBoolean("shiftToTransmute", shiftToTransmute);
    }

    @Override
    public MenuType<?> getMenuType(Player player) {
        return AppliedE.TRANSMUTATION_TERMINAL_MENU.get();
    }

    @Override
    public IPartModel getStaticModels() {
        return isActive() ? MODELS_HAS_CHANNEL : isPowered() ? MODELS_ON : MODELS_OFF;
    }
}
