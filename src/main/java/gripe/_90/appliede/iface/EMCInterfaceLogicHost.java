package gripe._90.appliede.iface;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.helpers.IConfigInvHost;
import appeng.helpers.IPriorityHost;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocator;

public interface EMCInterfaceLogicHost extends IPriorityHost, IConfigInvHost {
    BlockEntity getBlockEntity();

    void saveChanges();

    EMCInterfaceLogic getInterfaceLogic();

    @Override
    default int getPriority() {
        return getInterfaceLogic().getPriority();
    }

    @Override
    default void setPriority(int priority) {
        getInterfaceLogic().setPriority(priority);
    }

    @Override
    default GenericStackInv getConfig() {
        return getInterfaceLogic().getConfig();
    }

    default GenericStackInv getStorage() {
        return getInterfaceLogic().getStorage();
    }

    default void openMenu(Player player, MenuLocator locator) {
        MenuOpener.open(EMCInterfaceMenu.TYPE, player, locator);
    }

    @Override
    default void returnToMainMenu(Player player, ISubMenu subMenu) {
        MenuOpener.returnTo(EMCInterfaceMenu.TYPE, player, subMenu.getLocator());
    }
}
