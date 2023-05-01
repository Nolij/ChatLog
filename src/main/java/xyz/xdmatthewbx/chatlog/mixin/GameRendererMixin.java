package xyz.xdmatthewbx.chatlog.mixin;

import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
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

	@Shadow @Final private Camera camera;

	@Shadow
	private boolean renderHand;

	@ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;renderNausea(F)V"))
	public float setShaderColor(float opacity) {
		if (AntiDistortionModule.INSTANCE.enabled) {
			return Math.min(opacity, (float) ChatLog.CONFIG.get().main.antiDistortionModule.nauseaOverlayScale);
		}
		return opacity;
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;getDistortionEffectScale()Lnet/minecraft/client/option/SimpleOption;"))
	public SimpleOption<Double> renderDistortionEffectScale(GameOptions instance) {
		if (AntiDistortionModule.INSTANCE.enabled) {
			return new SimpleOption<>(
				"options.screenEffectScale",
				SimpleOption.emptyTooltip(),
				(text, value) -> Text.empty(),
				SimpleOption.DoubleSliderCallbacks.INSTANCE,
				0D,
				value -> {}
			);
		}
		return instance.getDistortionEffectScale();
	}

	@Redirect(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;getDistortionEffectScale()Lnet/minecraft/client/option/SimpleOption;"))
	public SimpleOption<Double> renderWorldDistortionEffectScale(GameOptions instance) {
		if (AntiDistortionModule.INSTANCE.enabled) {
			return new SimpleOption<>(
				"options.screenEffectScale",
				SimpleOption.emptyTooltip(),
				(text, value) -> Text.empty(),
				SimpleOption.DoubleSliderCallbacks.INSTANCE,
				0D,
				value -> {}
			); // TODO: deduplicate
		}
		return instance.getDistortionEffectScale();
	}

	@Inject(method = "renderWorld", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = {"ldc=hand"}))
	private void renderWorld(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo ci) {
		Renderer.renderAll(matrix, camera);
	}

	@Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
	private void bobView(MatrixStack matrices, float f, CallbackInfo ci) {
		if (ChatLog.cameraLock.isLocked() && ChatLog.CONFIG.get().main.render.disableBobbingWhenCameraLocked) {
			ci.cancel();
		}
	}

	@Redirect(method = "renderWorld", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/GameRenderer;renderHand:Z"))
	private boolean shouldRenderHand(GameRenderer instance) {
		if (FreeCamModule.INSTANCE.enabled && !ChatLog.CONFIG.get().main.freeCamModule.renderHand) {
			return false;
		}

		return this.renderHand;
	}

}
