package xyz.xdmatthewbx.chatlog.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.xdmatthewbx.chatlog.ChatLog;
import xyz.xdmatthewbx.chatlog.modules.AntiDistortionModule;
import xyz.xdmatthewbx.chatlog.modules.FreeCamModule;
import xyz.xdmatthewbx.chatlog.render.Renderer;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

	@Shadow @Final private Camera mainCamera;

	@Shadow
	private boolean renderHand;

	@ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;renderConfusionOverlay(F)V"))
	public float setShaderColor(float opacity) {
		if (AntiDistortionModule.INSTANCE.enabled) {
			return Math.min(opacity, (float) ChatLog.CONFIG.get().main.antiDistortionModule.nauseaOverlayScale);
		}
		return opacity;
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;screenEffectScale()Lnet/minecraft/client/OptionInstance;"))
	public OptionInstance<Double> renderDistortionEffectScale(Options instance) {
		if (AntiDistortionModule.INSTANCE.enabled) {
			return new OptionInstance<>(
				"options.screenEffectScale",
				OptionInstance.noTooltip(),
				(text, value) -> Component.empty(),
				OptionInstance.UnitDouble.INSTANCE,
				0D,
				value -> {}
			);
		}
		return instance.screenEffectScale();
	}

	@Redirect(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;screenEffectScale()Lnet/minecraft/client/OptionInstance;"))
	public OptionInstance<Double> renderWorldDistortionEffectScale(Options instance) {
		if (AntiDistortionModule.INSTANCE.enabled) {
			return new OptionInstance<>(
				"options.screenEffectScale",
				OptionInstance.noTooltip(),
				(text, value) -> Component.empty(),
				OptionInstance.UnitDouble.INSTANCE,
				0D,
				value -> {}
			); // TODO: deduplicate
		}
		return instance.screenEffectScale();
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", args = {"ldc=hand"}))
	private void renderWorld(float tickDelta, long limitTime, PoseStack matrix, CallbackInfo ci) {
		Renderer.renderAll(matrix, mainCamera);
	}

	@Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
	private void bobView(PoseStack matrices, float f, CallbackInfo ci) {
		if (ChatLog.cameraLock.isLocked() && ChatLog.CONFIG.get().main.render.disableBobbingWhenCameraLocked) {
			ci.cancel();
		}
	}

	@Redirect(method = "renderLevel", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/GameRenderer;renderHand:Z"))
	private boolean shouldRenderHand(GameRenderer instance) {
		if (FreeCamModule.INSTANCE.enabled && !ChatLog.CONFIG.get().main.freeCamModule.renderHand) {
			return false;
		}

		return this.renderHand;
	}

}
