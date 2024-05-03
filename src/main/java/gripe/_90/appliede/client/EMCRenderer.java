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
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import appeng.api.client.AEKeyRenderHandler;
import appeng.api.client.AEKeyRendering;
import appeng.client.gui.style.Blitter;

import gripe._90.appliede.AppliedE;
import gripe._90.appliede.me.key.EMCKey;
import gripe._90.appliede.me.key.EMCKeyType;

@SuppressWarnings("unused")
public final class EMCRenderer implements AEKeyRenderHandler<EMCKey> {
    private final Supplier<TextureAtlasSprite> sprite = () -> Minecraft.getInstance()
            .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
            .apply(AppliedE.id("item/dummy_emc_item"));

    private EMCRenderer() {}

    public static void register(FMLClientSetupEvent event) {
        event.enqueueWork(() -> AEKeyRendering.register(EMCKeyType.TYPE, EMCKey.class, new EMCRenderer()));
    }

    @Override
    public void drawInGui(Minecraft minecraft, GuiGraphics guiGraphics, int x, int y, EMCKey stack) {
        Blitter.sprite(sprite.get())
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
        buffer.vertex(transform, x0, y1, 0)
                .color(-1)
                .uv(s.getU0(), s.getV1())
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(combinedLight)
                .normal(0, 0, 1)
                .endVertex();
        buffer.vertex(transform, x1, y1, 0)
                .color(-1)
                .uv(s.getU1(), s.getV1())
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(combinedLight)
                .normal(0, 0, 1)
                .endVertex();
        buffer.vertex(transform, x1, y0, 0)
                .color(-1)
                .uv(s.getU1(), s.getV0())
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(combinedLight)
                .normal(0, 0, 1)
                .endVertex();
        buffer.vertex(transform, x0, y0, 0)
                .color(-1)
                .uv(s.getU0(), s.getV0())
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(combinedLight)
                .normal(0, 0, 1)
                .endVertex();

        poseStack.popPose();
    }

    @Override
    public Component getDisplayName(EMCKey stack) {
        return stack.getDisplayName();
    }
}
