package gripe._90.appliede.key;

import java.awt.Color;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import appeng.api.client.AEKeyRenderHandler;
import appeng.api.client.AEKeyRendering;
import appeng.client.gui.style.Blitter;

import gripe._90.appliede.AppliedE;

public final class EMCRenderer implements AEKeyRenderHandler<EMCKey> {
    public static final EMCRenderer INSTANCE = new EMCRenderer();

    private EMCRenderer() {}

    public static void register() {
        AEKeyRendering.register(EMCKeyType.TYPE, EMCKey.class, INSTANCE);
    }

    @Override
    public void drawInGui(Minecraft minecraft, GuiGraphics guiGraphics, int x, int y, EMCKey stack) {
        Blitter.texture(AppliedE.id("textures/me/emc.png"))
                .blending(false)
                .dest(x, y, 16, 16)
                .colorRgb(hueShift(stack.getTier()))
                .blit(guiGraphics);
    }

    private int hueShift(int tier) {
        var intervals = 10;
        var hue = ((tier - 1) * 360F / intervals) / 360;
        var wrap = (tier - 1) % intervals == 0;
        return Color.HSBtoRGB(hue, wrap ? 0 : 0.6F, 1);
    }

    @Override
    public void drawOnBlockFace(
            PoseStack poseStack, MultiBufferSource buffers, EMCKey what, float scale, int combinedLight, Level level) {}

    @Override
    public Component getDisplayName(EMCKey stack) {
        return stack.getDisplayName();
    }
}
