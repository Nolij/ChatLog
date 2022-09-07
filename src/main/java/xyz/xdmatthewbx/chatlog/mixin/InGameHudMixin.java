package xyz.xdmatthewbx.chatlog.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import xyz.xdmatthewbx.chatlog.ChatLog;

@Mixin(InGameHud.class)
public class InGameHudMixin {

	@ModifyVariable(method = "renderOverlay", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	public float renderOverlay(float opacity) {
		if (ChatLog.ANTI_OVERLAY_MODULE.enabled) {
			return Math.min(opacity, (float) ChatLog.CONFIG.get().main.antiOverlayModule.overlayOpacity);
		}
		return opacity;
	}

	@ModifyArgs(method = "renderPortalOverlay", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderColor(FFFF)V", remap = false))
	public void renderPortalOverlay(Args args) {
		if (ChatLog.ANTI_OVERLAY_MODULE.enabled) {
			args.set(3, Math.min(args.get(3), (float) ChatLog.CONFIG.get().main.antiOverlayModule.overlayOpacity * 0.7F));
		}
	}

	@Inject(method = "renderSpyglassOverlay", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/util/Identifier;)V"))
	public void renderSpyglassOverlay(float scale, CallbackInfo ci) {
		if (ChatLog.ANTI_OVERLAY_MODULE.enabled) {
			RenderSystem.setShaderColor(1F, 1F, 1F, (float) ChatLog.CONFIG.get().main.antiOverlayModule.overlayOpacity);
		}
	}

}
