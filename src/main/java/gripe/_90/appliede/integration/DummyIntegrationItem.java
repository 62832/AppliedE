package gripe._90.appliede.integration;

import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import gripe._90.appliede.AppliedE;

public class DummyIntegrationItem extends Item {
    private final Addons addon;

    public DummyIntegrationItem(Properties props, Addons addon) {
        super(props);
        this.addon = addon;
    }

    @ParametersAreNonnullByDefault
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("tooltip." + AppliedE.MODID + ".not_installed." + addon.getModId())
                .withStyle(ChatFormatting.GRAY));
    }
}
