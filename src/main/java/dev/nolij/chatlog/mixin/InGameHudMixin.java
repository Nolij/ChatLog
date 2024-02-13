package dev.nolij.chatlog.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.nolij.chatlog.ChatLog;
import dev.nolij.chatlog.modules.AntiOverlayModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

	@ModifyVariable(method = "renderOverlay", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	public float renderOverlay(float opacity) {
		if (AntiOverlayModule.INSTANCE.enabled) {
			return Math.min(opacity, (float) ChatLog.CONFIG.get().main.antiOverlayModule.overlayOpacity);
		}
		
		return opacity;
	}

	@ModifyArg(method = "renderPortalOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;setShaderColor(FFFF)V"), index = 3)
	public float renderPortalOverlay(float original) {
		if (AntiOverlayModule.INSTANCE.enabled) {
			return Math.min(original, (float) ChatLog.CONFIG.get().main.antiOverlayModule.overlayOpacity * 0.7F);
		}
		
		return original;
	}

	@Inject(method = "renderSpyglassOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIFFIIII)V"))
	public void renderSpyglassOverlay(DrawContext context, float scale, CallbackInfo ci) {
		if (AntiOverlayModule.INSTANCE.enabled) {
			RenderSystem.setShaderColor(1F, 1F, 1F, (float) ChatLog.CONFIG.get().main.antiOverlayModule.overlayOpacity);
		}
	}

}
