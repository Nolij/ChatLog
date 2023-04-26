package xyz.xdmatthewbx.chatlog.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.xdmatthewbx.chatlog.ChatLog;
import xyz.xdmatthewbx.chatlog.modules.AntiOverlayModule;

@Mixin(ScreenEffectRenderer.class)
public class InGameOverlayRendererMixin {

	@Inject(method = "renderScreenEffect", at = @At("HEAD"), cancellable = true)
	private static void renderOverlays(Minecraft client, PoseStack matrices, CallbackInfo ci) {
		if (AntiOverlayModule.INSTANCE.enabled && ChatLog.CONFIG.get().main.antiOverlayModule.overlayOpacity == 0F) {
			ci.cancel();
		}
	}

	@Inject(method = "renderTex", at = @At(value = "HEAD"), cancellable = true)
	private static void renderInWallOverlay(TextureAtlasSprite sprite, PoseStack matrices, CallbackInfo ci) {
		if (AntiOverlayModule.INSTANCE.enabled) {
			ci.cancel();
		}
	}

	@Inject(method = "renderFire", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V"))
	private static void renderFireOverlay(Minecraft client, PoseStack matrices, CallbackInfo ci) {
		if (AntiOverlayModule.INSTANCE.enabled) {
			RenderSystem.setShaderColor(1F, 1F, 1F, (float) ChatLog.CONFIG.get().main.antiOverlayModule.overlayOpacity);
		}
	}

//	@ModifyArg(method = "renderUnderwaterOverlay", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderColor(FFFF)V"), index = 3)
//	private static float renderUnderwaterOverlay(float alpha) {
//		if (ChatLog.ANTI_OVERLAY_MODULE.enabled) {
//			return (float) ChatLog.CONFIG.main.overlayOpacity * 0.1F;
//		}
//		return alpha;
//	}

}
