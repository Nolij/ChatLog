package xyz.xdmatthewbx.chatlog.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import xyz.xdmatthewbx.chatlog.ChatLog;
import xyz.xdmatthewbx.chatlog.modules.AntiOverlayModule;

@Mixin(Gui.class)
public class InGameHudMixin {

	@ModifyVariable(method = "renderTextureOverlay", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	public float renderOverlay(float opacity) {
		if (AntiOverlayModule.INSTANCE.enabled) {
			return Math.min(opacity, (float) ChatLog.CONFIG.get().main.antiOverlayModule.overlayOpacity);
		}
		return opacity;
	}

	@ModifyArgs(method = "renderPortalOverlay", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderColor(FFFF)V", remap = false))
	public void renderPortalOverlay(Args args) {
		if (AntiOverlayModule.INSTANCE.enabled) {
			args.set(3, Math.min(args.get(3), (float) ChatLog.CONFIG.get().main.antiOverlayModule.overlayOpacity * 0.7F));
		}
	}

	@Inject(method = "renderSpyglassOverlay", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V"))
	public void renderSpyglassOverlay(PoseStack matrices, float scale, CallbackInfo ci) {
		if (AntiOverlayModule.INSTANCE.enabled) {
			RenderSystem.setShaderColor(1F, 1F, 1F, (float) ChatLog.CONFIG.get().main.antiOverlayModule.overlayOpacity);
		}
	}

}
