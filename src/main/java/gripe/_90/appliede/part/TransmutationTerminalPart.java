package gripe._90.appliede.part;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;

import appeng.api.networking.storage.IStorageService;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.util.AEColor;
import appeng.client.render.StaticItemColor;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;
import appeng.parts.reporting.AbstractTerminalPart;

import gripe._90.appliede.AppliedE;
import gripe._90.appliede.me.misc.ITransmutationTerminalHost;
import gripe._90.appliede.me.service.KnowledgeService;
import gripe._90.appliede.menu.TransmutationTerminalMenu;

public class TransmutationTerminalPart extends AbstractTerminalPart implements ITransmutationTerminalHost {
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

    public static void registerColour(RegisterColorHandlersEvent.Item event) {
        event.register(new StaticItemColor(AEColor.TRANSPARENT), AppliedE.TRANSMUTATION_TERMINAL.get());
    }

    @Override
    public boolean getShiftToTransmute() {
        return shiftToTransmute;
    }

    @Override
    public void setShiftToTransmute(boolean toggle) {
        shiftToTransmute = toggle;
        saveChanges();
    }

    @Nullable
    @Override
    public KnowledgeService getKnowledgeService() {
        var grid = getMainNode().getGrid();
        return grid != null ? grid.getService(KnowledgeService.class) : null;
    }

    @Nullable
    @Override
    public IStorageService getStorageService() {
        var grid = getMainNode().getGrid();
        return grid != null ? grid.getStorageService() : null;
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
        return TransmutationTerminalMenu.TYPE;
    }

    @Override
    public IPartModel getStaticModels() {
        return selectModel(MODELS_OFF, MODELS_ON, MODELS_HAS_CHANNEL);
    }
}
