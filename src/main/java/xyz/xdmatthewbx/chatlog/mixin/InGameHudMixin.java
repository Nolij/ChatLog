package xyz.xdmatthewbx.chatlog.mixin;

import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import xyz.xdmatthewbx.chatlog.ChatLog;
import xyz.xdmatthewbx.chatlog.modules.AntiOverlayModule;

@Mixin(InGameHud.class)
public class InGameHudMixin {

	@ModifyArg(method = "renderPumpkinOverlay", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;color4f(FFFF)V"), index = 3)
	public float renderOverlay(float opacity) {
		if (AntiOverlayModule.INSTANCE.enabled) {
			return Math.min(opacity, (float) ChatLog.CONFIG.get().main.antiOverlayModule.overlayOpacity);
		}
		return opacity;
	}

	@ModifyArgs(method = "renderPortalOverlay", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;color4f(FFFF)V"))
	public void renderPortalOverlay(Args args) {
		if (AntiOverlayModule.INSTANCE.enabled) {
			args.set(3, Math.min(args.get(3), (float) ChatLog.CONFIG.get().main.antiOverlayModule.overlayOpacity * 0.7F));
		}
	}

	/*
	
	@Inject(method = "renderSpyglassOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/BufferBuilder;begin(Lnet/minecraft/client/render/VertexFormat$DrawMode;Lnet/minecraft/client/render/VertexFormat;)V", ordinal = 0))
	public void renderSpyglassOverlay(float scale, CallbackInfo ci) {
		if (AntiOverlayModule.INSTANCE.enabled) {
			RenderSystem.setShaderColor(1F, 1F, 1F, (float) ChatLog.CONFIG.get().main.antiOverlayModule.overlayOpacity);
		}
	}
	
	*/
}
