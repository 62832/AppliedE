package gripe._90.appliede.integration.ae2wtlib;

import org.jetbrains.annotations.NotNull;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;

import appeng.menu.locator.ItemMenuHostLocator;

import de.mari_023.ae2wtlib.api.terminal.ItemWT;

public class WTTItem extends ItemWT {
    public WTTItem() {
        // ItemWT handles its own properties internally
    }

    @NotNull
    @Override
    public MenuType<?> getMenuType(@NotNull ItemMenuHostLocator locator, @NotNull Player player) {
        return WTTMenu.TYPE;
    }
}
