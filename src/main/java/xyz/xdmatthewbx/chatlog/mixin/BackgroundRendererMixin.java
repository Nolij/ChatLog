package xyz.xdmatthewbx.chatlog.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.xdmatthewbx.chatlog.ChatLog;
import xyz.xdmatthewbx.chatlog.modules.AntiBlindModule;
import xyz.xdmatthewbx.chatlog.modules.AntiFogModule;

@Mixin(FogRenderer.class)
public class BackgroundRendererMixin {

	@Redirect(method = "setupColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hasEffect(Lnet/minecraft/world/effect/MobEffect;)Z"))
	private static boolean renderHasStatusEffect(LivingEntity instance, MobEffect effect) {
		if (AntiBlindModule.INSTANCE.enabled && effect == MobEffects.BLINDNESS) {
			return false;
		}
		return instance.hasEffect(effect);
	}

	@Redirect(method = "setupFog", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hasEffect(Lnet/minecraft/world/effect/MobEffect;)Z"))
	private static boolean applyFogHasStatusEffect(LivingEntity instance, MobEffect effect) {
		if (AntiBlindModule.INSTANCE.enabled && effect == MobEffects.BLINDNESS) {
			return false;
		}
		return instance.hasEffect(effect);
	}

	@Redirect(method = "setupColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getFluidInCamera()Lnet/minecraft/world/level/material/FogType;"))
	private static FogType renderGetSubmersionType(Camera instance) {
		if (AntiBlindModule.INSTANCE.enabled) {
			return FogType.NONE;
		}
		return instance.getFluidInCamera();
	}

	@Redirect(method = "setupFog", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getFluidInCamera()Lnet/minecraft/world/level/material/FogType;"))
	private static FogType applyFogGetSubmersionType(Camera instance) {
		if (AntiBlindModule.INSTANCE.enabled) {
			return FogType.NONE;
		}
		return instance.getFluidInCamera();
	}

	@ModifyArg(method = "setupFog", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFogStart(F)V", remap = false))
	private static float setShaderFogStart(float f) {
		if (AntiFogModule.INSTANCE.enabled) {
			return -5F;
		} else {
			return f;
		}
	}

	@ModifyArg(method = "setupFog", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFogEnd(F)V", remap = false))
	private static float setShaderFogEnd(float f) {
		if (AntiFogModule.INSTANCE.enabled) {
			return 10000000F;
		} else {
			return f;
		}
	}

	@SuppressWarnings("InvalidInjectorMethodSignature") // not sure why this shows in the first place
	@ModifyVariable(method = "setupColor", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/util/CubicSampler;gaussianSampleVec3(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/util/CubicSampler$Vec3Fetcher;)Lnet/minecraft/world/phys/Vec3;"), ordinal = 2, require = 1, allow = 1)
	private static Vec3 onSampleColor(Vec3 value) {
		assert ChatLog.CLIENT.level != null;
		if (AntiFogModule.INSTANCE.enabled && ChatLog.CLIENT.level.dimensionType().hasSkyLight()) {
			return ChatLog.CLIENT.level.getSkyColor(ChatLog.CLIENT.gameRenderer.getMainCamera().getPosition(), ChatLog.CLIENT.getDeltaFrameTime());
		}
		return value;
	}

	@ModifyVariable(method = "setupColor", at = @At(value = "INVOKE_ASSIGN", target = "Lorg/joml/Vector3f;dot(Lorg/joml/Vector3fc;)F", remap = false), ordinal = 7, require = 1, allow = 1)
	private static float afterPlaneDot(float dotProduct) {
		if (AntiFogModule.INSTANCE.enabled) {
			return 0;
		}
		return dotProduct;
	}

	@Redirect(method = "setupColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getRainLevel(F)F"), require = 1, allow = 1)
	private static float onGetRainGradient(ClientLevel instance, float tickDelta) {
		if (AntiFogModule.INSTANCE.enabled) {
			return 0;
		}
		return instance.getRainLevel(tickDelta);
	}

	@Redirect(method = "setupColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getThunderLevel(F)F"), require = 1, allow = 1)
	private static float onGetThunderGradient(ClientLevel instance, float tickDelta) {
		if (AntiFogModule.INSTANCE.enabled) {
			return 0;
		}
		return instance.getThunderLevel(tickDelta);
	}

}
