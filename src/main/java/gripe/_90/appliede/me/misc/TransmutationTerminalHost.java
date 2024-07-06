package gripe._90.appliede.me.misc;

import appeng.api.networking.security.IActionHost;
import appeng.api.storage.ITerminalHost;

public interface TransmutationTerminalHost extends ITerminalHost, IActionHost {
    boolean getShiftToTransmute();

    void setShiftToTransmute(boolean shift);
}
