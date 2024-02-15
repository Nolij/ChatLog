package dev.nolij.chatlog.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.nolij.chatlog.ChatLog;
import dev.nolij.chatlog.modules.AntiDistortionModule;
import dev.nolij.chatlog.modules.FreeCamModule;
import dev.nolij.chatlog.render.Renderer;
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

@Mixin(GameRenderer.class)
public class GameRendererMixin {

	@Shadow @Final private Camera camera;

	@ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;renderNausea(Lnet/minecraft/client/gui/DrawContext;F)V"), index = 1)
	public float setShaderColor(float opacity) {
		if (AntiDistortionModule.INSTANCE.enabled) {
			return Math.min(opacity, (float) ChatLog.CONFIG.get().main.antiDistortionModule.nauseaOverlayScale);
		}
		return opacity;
	}

	@WrapOperation(method = {"render", "renderWorld"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;getDistortionEffectScale()Lnet/minecraft/client/option/SimpleOption;"))
	public SimpleOption<Double> renderDistortionEffectScale(GameOptions instance, Operation<SimpleOption<Double>> original) {
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
		
		return original.call(instance);
	}

	@Inject(method = "renderWorld", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = {"ldc=hand"}))
	private void renderWorld(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo ci) {
		Renderer.renderAll(matrix, camera);
	}

	@Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
	private void bobView(MatrixStack matrices, float f, CallbackInfo ci) {
		if (ChatLog.cameraLock.isLocked() && ChatLog.CONFIG.get().main.render.disableBobbingWhenCameraLocked)
			ci.cancel();
	}

	@ModifyExpressionValue(method = "renderWorld", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/GameRenderer;renderHand:Z"))
	private boolean shouldRenderHand(boolean original) {
		if (original && FreeCamModule.INSTANCE.enabled && !ChatLog.CONFIG.get().main.freeCamModule.renderHand)
			return false;

		return original;
	}

}
