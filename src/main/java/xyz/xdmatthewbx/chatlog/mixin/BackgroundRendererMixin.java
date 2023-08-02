package xyz.xdmatthewbx.chatlog.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.CameraSubmersionType;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.xdmatthewbx.chatlog.ChatLog;
import xyz.xdmatthewbx.chatlog.modules.AntiBlindModule;
import xyz.xdmatthewbx.chatlog.modules.AntiFogModule;

@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin {

	@WrapOperation(method = "applyFog", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/BackgroundRenderer;getFogModifier(Lnet/minecraft/entity/Entity;F)Lnet/minecraft/client/render/BackgroundRenderer$StatusEffectFogModifier;"))
	private static BackgroundRenderer.StatusEffectFogModifier applyFog$getFogModifier(Entity entity, float tickDelta, Operation<BackgroundRenderer.StatusEffectFogModifier> original) {
		if (AntiBlindModule.INSTANCE.enabled)
			return null;

		return original.call(entity, tickDelta);
	}

	@ModifyArg(method = "applyFog", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFogStart(F)V", remap = false))
	private static float setShaderFogStart(float f) {
		if (AntiFogModule.INSTANCE.enabled) {
			return -5F;
		} else {
			return f;
		}
	}

	@ModifyArg(method = "applyFog", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFogEnd(F)V", remap = false))
	private static float setShaderFogEnd(float f) {
		if (AntiFogModule.INSTANCE.enabled) {
			return 10000000F;
		} else {
			return f;
		}
	}

	@WrapOperation(method = "applyFog", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;getSubmersionType()Lnet/minecraft/client/render/CameraSubmersionType;"))
	private static CameraSubmersionType applyFog$getSubmersionType(Camera instance, Operation<CameraSubmersionType> original) {
		if (AntiBlindModule.INSTANCE.enabled) {
			return CameraSubmersionType.NONE;
		}
		return original.call(instance);
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;getSubmersionType()Lnet/minecraft/client/render/CameraSubmersionType;"))
	private static CameraSubmersionType render$getSubmersionType(Camera instance, Operation<CameraSubmersionType> original) {
		if (AntiBlindModule.INSTANCE.enabled) {
			return CameraSubmersionType.NONE;
		}
		return original.call(instance);
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/entity/effect/StatusEffect;)Z"))
	private static boolean render$hasStatusEffect(LivingEntity instance, StatusEffect effect, Operation<Boolean> original) {
		if (AntiBlindModule.INSTANCE.enabled &&
			(effect == StatusEffects.BLINDNESS || effect == StatusEffects.DARKNESS))
			return false;

		return original.call(instance, effect);
	}

	@SuppressWarnings("InvalidInjectorMethodSignature") // not sure why this shows in the first place
	@ModifyVariable(method = "render", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/util/CubicSampler;sampleColor(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/CubicSampler$RgbFetcher;)Lnet/minecraft/util/math/Vec3d;"), ordinal = 2, require = 1, allow = 1)
	private static Vec3d onSampleColor(Vec3d value) {
		assert ChatLog.CLIENT.world != null;
		if (AntiFogModule.INSTANCE.enabled && ChatLog.CLIENT.world.getDimension().hasSkyLight()) {
			return ChatLog.CLIENT.world.getSkyColor(ChatLog.CLIENT.gameRenderer.getCamera().getPos(), ChatLog.CLIENT.getLastFrameDuration());
		}
		return value;
	}

	@ModifyVariable(method = "render", at = @At(value = "INVOKE_ASSIGN", target = "Lorg/joml/Vector3f;dot(Lorg/joml/Vector3fc;)F", remap = false), ordinal = 7, require = 1, allow = 1)
	private static float afterPlaneDot(float dotProduct) {
		if (AntiFogModule.INSTANCE.enabled) {
			return 0;
		}
		return dotProduct;
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getRainGradient(F)F"), require = 1, allow = 1)
	private static float onGetRainGradient(ClientWorld instance, float tickDelta, Operation<Float> original) {
		if (AntiFogModule.INSTANCE.enabled)
			return 0;

		return original.call(instance, tickDelta);
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getThunderGradient(F)F"), require = 1, allow = 1)
	private static float onGetThunderGradient(ClientWorld instance, float tickDelta, Operation<Float> original) {
		if (AntiFogModule.INSTANCE.enabled)
			return 0;

		return original.call(instance, tickDelta);
	}

}
