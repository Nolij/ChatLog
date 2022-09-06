package xyz.xdmatthewbx.chatlog.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.xdmatthewbx.chatlog.ChatLog;

@Mixin(InGameOverlayRenderer.class)
public class InGameOverlayRendererMixin {

	@Inject(method = "renderOverlays", at = @At("HEAD"), cancellable = true)
	private static void renderOverlays(MinecraftClient client, MatrixStack matrices, CallbackInfo ci) {
		if (ChatLog.ANTI_OVERLAY_MODULE.enabled && ChatLog.CONFIG.get().main.antiOverlayModule.overlayOpacity == 0F) {
			ci.cancel();
		}
	}

	@Inject(method = "renderInWallOverlay", at = @At(value = "HEAD"), cancellable = true)
	private static void renderInWallOverlay(Sprite sprite, MatrixStack matrices, CallbackInfo ci) {
		if (ChatLog.ANTI_OVERLAY_MODULE.enabled) {
			ci.cancel();
		}
	}

	@Inject(method = "renderFireOverlay", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/util/Identifier;)V"))
	private static void renderFireOverlay(MinecraftClient client, MatrixStack matrices, CallbackInfo ci) {
		if (ChatLog.ANTI_OVERLAY_MODULE.enabled) {
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