package gripe._90.appliede.key;

import java.awt.Color;
import java.util.function.Supplier;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import appeng.api.client.AEKeyRenderHandler;
import appeng.api.client.AEKeyRendering;
import appeng.client.gui.style.Blitter;

import gripe._90.appliede.AppliedE;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = AppliedE.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class EMCRenderer implements AEKeyRenderHandler<EMCKey> {
    private static final Supplier<Texture> TEXTURE = () -> Texture.INSTANCE;

    private EMCRenderer() {}

    @SubscribeEvent
    public static void register(FMLClientSetupEvent event) {
        event.enqueueWork(() -> AEKeyRendering.register(EMCKeyType.TYPE, EMCKey.class, new EMCRenderer()));
    }

    @SubscribeEvent
    public static void loadTexture(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(TEXTURE.get());
    }

    @Override
    public void drawInGui(Minecraft minecraft, GuiGraphics guiGraphics, int x, int y, EMCKey stack) {
        Blitter.sprite(TEXTURE.get().get())
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
        var sprite = TEXTURE.get().get();
        poseStack.pushPose();
        // Push it out of the block face a bit to avoid z-fighting
        poseStack.translate(0, 0, 0.01f);

        var buffer = buffers.getBuffer(RenderType.solid());

        // y is flipped here
        var x0 = -scale / 2;
        var y0 = scale / 2;
        var x1 = scale / 2;
        var y1 = -scale / 2;

        var transform = poseStack.last().pose();
        buffer.vertex(transform, x0, y1, 0)
                .color(-1)
                .uv(sprite.getU0(), sprite.getV1())
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(combinedLight)
                .normal(0, 0, 1)
                .endVertex();
        buffer.vertex(transform, x1, y1, 0)
                .color(-1)
                .uv(sprite.getU1(), sprite.getV1())
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(combinedLight)
                .normal(0, 0, 1)
                .endVertex();
        buffer.vertex(transform, x1, y0, 0)
                .color(-1)
                .uv(sprite.getU1(), sprite.getV0())
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(combinedLight)
                .normal(0, 0, 1)
                .endVertex();
        buffer.vertex(transform, x0, y0, 0)
                .color(-1)
                .uv(sprite.getU0(), sprite.getV0())
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

    private static class Texture extends TextureAtlasHolder {
        private static final Texture INSTANCE = new Texture();

        private Texture() {
            super(
                    Minecraft.getInstance().getTextureManager(),
                    AppliedE.id("textures/atlas/emc.png"),
                    AppliedE.id("emc"));
        }

        private TextureAtlasSprite get() {
            return getSprite(AppliedE.id("me/emc"));
        }
    }
}
