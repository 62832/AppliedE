package gripe._90.appliede.me.misc;

import appeng.api.networking.security.IActionHost;
import appeng.api.storage.ITerminalHost;

/**
 * An extension of {@link ITerminalHost} for functionality specific to the {@linkplain
 * gripe._90.appliede.menu.TransmutationTerminalMenu Transmutation Terminal menu}.
 */
public interface TransmutationTerminalHost extends ITerminalHost, IActionHost {
    /**
     * @return Whether "Shift to Transmute" mode is set for the menu, or in other words whether quick-moved
     * (Shift+Click) item stacks should be sent to the "transmutation" slot and turned to EMC rather than deposited to
     * the network's regular item storage.
     */
    boolean getShiftToTransmute();

    /**
     * Toggles where quick-moved (Shift+Click) item stacks are sent within the Transmutation Terminal menu, i.e. whether
     * these should be directly transmuted to EMC or sent to the network's regular item storage.
     */
    default void toggleShiftToTransmute() {
        setShiftToTransmute(getShiftToTransmute());
    }

    /**
     * Sets whether quick-moved (Shift+Click) item stacks should be directly transmuted to EMC or sent to the network's
     * regular item storage.
     *
     * @deprecated Replaced by {@code toggleShiftToTransmute()}.
     */
    @Deprecated(forRemoval = true)
    default void setShiftToTransmute(boolean shift) {}
}
