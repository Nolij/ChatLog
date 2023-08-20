package xyz.xdmatthewbx.chatlog.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.xdmatthewbx.chatlog.ChatLog;
import xyz.xdmatthewbx.chatlog.modules.AntiBlindModule;
import xyz.xdmatthewbx.chatlog.modules.AntiFogModule;

@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin {
	
	@WrapOperation(method = "applyFog", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/entity/effect/StatusEffect;)Z"))
	private static boolean ignoreBlindness(LivingEntity e, StatusEffect effect, Operation<Boolean> operation) {
		if(AntiFogModule.INSTANCE.enabled && effect == StatusEffects.BLINDNESS)
			return false;
		return operation.call(e, effect);
	}

	@ModifyArg(method = "applyFog", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;fogStart(F)V", remap = false))
	private static float applyFog$setShaderFogStart(float f) {
		if (AntiFogModule.INSTANCE.enabled) {
			return -5F;
		} else {
			return f;
		}
	}

	@ModifyArg(method = "applyFog", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;fogEnd(F)V", remap = false))
	private static float applyFog$setShaderFogEnd(float f) {
		if (AntiFogModule.INSTANCE.enabled) {
			return 10000000F;
		} else {
			return f;
		}
	}
	
	@Unique
	private static final FluidState EMPTY_FLUID = Fluids.EMPTY.getDefaultState();

	@WrapOperation(method = "applyFog", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;getSubmergedFluidState()Lnet/minecraft/fluid/FluidState;"))
	private static FluidState applyFog$getSubmersionType(Camera instance, Operation<FluidState> original) {
		if (AntiBlindModule.INSTANCE.enabled) {
			return EMPTY_FLUID;
		}
		return original.call(instance);
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;getSubmergedFluidState()Lnet/minecraft/fluid/FluidState;"))
	private static FluidState render$getSubmersionType(Camera instance, Operation<FluidState> original) {
		if (AntiBlindModule.INSTANCE.enabled) {
			return EMPTY_FLUID;
		}
		return original.call(instance);
	}

	/*
	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/entity/effect/StatusEffect;)Z"))
	private static boolean render$hasStatusEffect(LivingEntity instance, StatusEffect effect, Operation<Boolean> original) {
		if (AntiBlindModule.INSTANCE.enabled &&
			(effect == StatusEffects.BLINDNESS || effect == StatusEffects.DARKNESS))
			return false;

		return original.call(instance, effect);
	}
	 */

	@SuppressWarnings("InvalidInjectorMethodSignature") // not sure why this shows in the first place
	@ModifyVariable(method = "render", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/util/CubicSampler;sampleColor(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/CubicSampler$RgbFetcher;)Lnet/minecraft/util/math/Vec3d;"), ordinal = 2, require = 1, allow = 1)
	private static Vec3d onSampleColor(Vec3d value) {
		assert ChatLog.CLIENT.world != null;
		if (AntiFogModule.INSTANCE.enabled && ChatLog.CLIENT.world.getDimension().hasSkyLight()) {
			return ChatLog.CLIENT.world.method_23777(ChatLog.CLIENT.gameRenderer.getCamera().getBlockPos(), ChatLog.CLIENT.getLastFrameDuration());
		}
		return value;
	}

	@ModifyVariable(method = "render", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/util/math/Vec3f;dot(Lnet/minecraft/util/math/Vec3f;)F", remap = false), ordinal = 7, require = 1, allow = 1)
	private static float afterPlaneDot(float dotProduct) {
		if (AntiFogModule.INSTANCE.enabled)
			return 0;

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
