package gripe._90.appliede.client;

import java.awt.Color;
import java.util.function.Supplier;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;

import appeng.api.client.AEKeyRenderHandler;
import appeng.client.gui.style.Blitter;

import gripe._90.appliede.AppliedE;
import gripe._90.appliede.AppliedEConfig;
import gripe._90.appliede.me.key.EMCKey;

public final class EMCRenderer implements AEKeyRenderHandler<EMCKey> {
    public static final EMCRenderer INSTANCE = new EMCRenderer();

    private final Supplier<TextureAtlasSprite> sprite = () -> Minecraft.getInstance()
            .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
            .apply(AppliedE.id("item/dummy_emc_item"));

    private EMCRenderer() {}

    @Override
    public void drawInGui(Minecraft minecraft, GuiGraphics guiGraphics, int x, int y, EMCKey stack) {
        var hueIntervals = AppliedEConfig.Client.CONFIG.getEmcTierColours();
        var hue = ((stack.getTier() - 1) * 360F / hueIntervals) / 360;

        Blitter.sprite(sprite.get())
                .blending(false)
                .dest(x, y, 16, 16)
                .colorRgb(Color.HSBtoRGB(hue, stack.getTier() == 1 ? 0 : 0.6F, 1))
                .blit(guiGraphics);
    }

    @Override
    public void drawOnBlockFace(
            PoseStack poseStack, MultiBufferSource buffers, EMCKey what, float scale, int combinedLight, Level level) {
        var s = sprite.get();
        poseStack.pushPose();
        // Push it out of the block face a bit to avoid z-fighting
        poseStack.translate(0, 0, 0.01f);

        var buffer = buffers.getBuffer(RenderType.cutoutMipped());

        // y is flipped here
        var x0 = -scale / 2;
        var y0 = scale / 2;
        var x1 = scale / 2;
        var y1 = -scale / 2;

        var transform = poseStack.last().pose();
        buffer.addVertex(transform, x0, y1, 0)
                .setColor(-1)
                .setUv(s.getU0(), s.getV1())
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(combinedLight)
                .setNormal(0, 0, 1);
        buffer.addVertex(transform, x1, y1, 0)
                .setColor(-1)
                .setUv(s.getU1(), s.getV1())
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(combinedLight)
                .setNormal(0, 0, 1);
        buffer.addVertex(transform, x1, y0, 0)
                .setColor(-1)
                .setUv(s.getU1(), s.getV0())
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(combinedLight)
                .setNormal(0, 0, 1);
        buffer.addVertex(transform, x0, y0, 0)
                .setColor(-1)
                .setUv(s.getU0(), s.getV0())
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(combinedLight)
                .setNormal(0, 0, 1);

        poseStack.popPose();
    }

    @Override
    public Component getDisplayName(EMCKey stack) {
        return stack.getDisplayName();
    }
}
