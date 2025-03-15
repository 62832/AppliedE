package gripe._90.appliede.integration;

import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import gripe._90.appliede.AppliedE;

public class DummyIntegrationItem extends Item {
    private final String addon;

    public DummyIntegrationItem(Properties props, String addon) {
        super(props);
        this.addon = addon;
    }

    @ParametersAreNonnullByDefault
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("tooltip." + AppliedE.MODID + ".not_installed", addon)
                .withStyle(ChatFormatting.GRAY));
    }
}
