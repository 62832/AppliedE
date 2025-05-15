package gripe._90.appliede.api;

import appeng.api.networking.security.IActionHost;

/**
 * An extension of {@link IActionHost} implemented on machines or other objects (such as {@linkplain
 * gripe._90.appliede.menu.TransmutationTerminalMenu menus}) capable of transmuting items into / out of EMC when
 * carrying out regular {@linkplain appeng.api.networking.storage.IStorageService storage service} I/O.
 */
public interface TransmutationCapable extends IActionHost {
    /**
     * @return Whether this machine is allowed to "learn" an inserted item on behalf of the player owning the associated
     * grid node.
     */
    boolean mayLearn();

    /**
     * Overridden to provide custom behaviour upon {@linkplain TransmutationCapable#mayLearn() learning} an item, e.g.
     * {@link gripe._90.appliede.menu.TransmutationTerminalMenu TransmutationTerminalMenu} displaying a text label on
     * its associated screen.
     */
    default void onLearn() {}

    /**
     * @return Whether this machine should consume any network power specifically when <i>inserting</i> items into
     * storage to turn into EMC.
     *
     * @apiNote This should typically only be overridden if a machine <b>isn't meant primarily for insertion to begin
     * with</b>, but may still need to insert items back into the network in case of overflow issues when already
     * retrieved, such as the {@linkplain gripe._90.appliede.part.EMCExportBusPart Transmutation Export Bus}.</p>
     */
    default boolean consumePowerOnInsert() {
        return true;
    }
}
