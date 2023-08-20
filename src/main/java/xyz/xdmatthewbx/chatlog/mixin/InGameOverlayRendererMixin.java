package xyz.xdmatthewbx.chatlog.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.xdmatthewbx.chatlog.ChatLog;
import xyz.xdmatthewbx.chatlog.modules.AntiOverlayModule;

@Mixin(InGameOverlayRenderer.class)
public class InGameOverlayRendererMixin {

	@Inject(method = "renderOverlays", at = @At("HEAD"), cancellable = true)
	private static void renderOverlays(MinecraftClient client, MatrixStack matrices, CallbackInfo ci) {
		if (AntiOverlayModule.INSTANCE.enabled && ChatLog.CONFIG.get().main.antiOverlayModule.overlayOpacity == 0F) {
			ci.cancel();
		}
	}

	@Inject(method = "renderInWallOverlay", at = @At(value = "HEAD"), cancellable = true)
	private static void renderInWallOverlay(MinecraftClient minecraftClient, Sprite sprite, MatrixStack matrixStack, CallbackInfo ci) {
		if (AntiOverlayModule.INSTANCE.enabled) {
			ci.cancel();
		}
	}

	@ModifyConstant(method = "renderFireOverlay", constant = @Constant(floatValue = 0.9f))
	private static float renderFireOverlay(float constant) {
		if (AntiOverlayModule.INSTANCE.enabled) {
			return (float)ChatLog.CONFIG.get().main.antiOverlayModule.overlayOpacity;
		}
		return constant;
	}

//	@ModifyArg(method = "renderUnderwaterOverlay", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderColor(FFFF)V"), index = 3)
//	private static float renderUnderwaterOverlay(float alpha) {
//		if (ChatLog.ANTI_OVERLAY_MODULE.enabled) {
//			return (float) ChatLog.CONFIG.main.overlayOpacity * 0.1F;
//		}
//		return alpha;
//	}

}
