package gripe._90.appliede.me.misc;

import appeng.api.networking.security.IActionHost;

public interface TransmutationCapable extends IActionHost {
    boolean mayLearn();

    default void onLearn() {}

    default boolean consumePowerOnInsert() {
        return true;
    }
}
