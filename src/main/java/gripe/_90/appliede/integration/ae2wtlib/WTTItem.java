package gripe._90.appliede.integration.ae2wtlib;

import org.jetbrains.annotations.NotNull;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import de.mari_023.ae2wtlib.terminal.ItemWT;

public class WTTItem extends ItemWT {
    @NotNull
    @Override
    public MenuType<?> getMenuType(@NotNull ItemStack itemStack) {
        return WTTMenu.TYPE;
    }
}
