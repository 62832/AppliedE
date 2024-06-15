package gripe._90.appliede.me.misc;

import org.jetbrains.annotations.Nullable;

import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionHost;
import appeng.api.storage.ITerminalHost;

public interface TransmutationTerminalHost extends ITerminalHost, IActionHost {
    boolean getShiftToTransmute();

    void setShiftToTransmute(boolean toggle);

    @Nullable
    IGrid getGrid();
}
