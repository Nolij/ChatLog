package xyz.xdmatthewbx.chatlog.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.CameraSubmersionType;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.xdmatthewbx.chatlog.ChatLog;

@Environment(EnvType.CLIENT)
@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin {

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/entity/effect/StatusEffect;)Z"))
	private static boolean renderHasStatusEffect(LivingEntity instance, StatusEffect effect) {
		if (ChatLog.ANTI_BLIND_MODULE.enabled && effect == StatusEffects.BLINDNESS) {
			return false;
		}
		return instance.hasStatusEffect(effect);
	}

	@Redirect(method = "applyFog", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/entity/effect/StatusEffect;)Z"))
	private static boolean applyFogHasStatusEffect(LivingEntity instance, StatusEffect effect) {
		if (ChatLog.ANTI_BLIND_MODULE.enabled && effect == StatusEffects.BLINDNESS) {
			return false;
		}
		return instance.hasStatusEffect(effect);
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;getSubmersionType()Lnet/minecraft/client/render/CameraSubmersionType;"))
	private static CameraSubmersionType renderGetSubmersionType(Camera instance) {
		if (ChatLog.ANTI_BLIND_MODULE.enabled) {
			return CameraSubmersionType.NONE;
		}
		return instance.getSubmersionType();
	}

	@Redirect(method = "applyFog", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;getSubmersionType()Lnet/minecraft/client/render/CameraSubmersionType;"))
	private static CameraSubmersionType applyFogGetSubmersionType(Camera instance) {
		if (ChatLog.ANTI_BLIND_MODULE.enabled) {
			return CameraSubmersionType.NONE;
		}
		return instance.getSubmersionType();
	}

	@ModifyArg(method = "applyFog", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFogStart(F)V"))
	private static float setShaderFogStart(float f) {
		if (ChatLog.ANTI_FOG_MODULE.enabled) {
			return -5F;
		} else {
			return f;
		}
	}

	@ModifyArg(method = "applyFog", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFogEnd(F)V"))
	private static float setShaderFogEnd(float f) {
		if (ChatLog.ANTI_FOG_MODULE.enabled) {
			return 10000000F;
		} else {
			return f;
		}
	}

	@ModifyVariable(method = "render", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/util/CubicSampler;sampleVec3d(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/CubicSampler$Vec3dFetcher;)Lnet/minecraft/util/math/Vec3d;"), ordinal = 2, require = 1, allow = 1)
	private static Vec3d onSampleColor(Vec3d value) {
		assert ChatLog.CLIENT.world != null;
		if (ChatLog.ANTI_FOG_MODULE.enabled && ChatLog.CLIENT.world.getDimension().hasSkyLight()) {
			return ChatLog.CLIENT.world.getSkyColor(ChatLog.CLIENT.gameRenderer.getCamera().getPos(), ChatLog.CLIENT.getLastFrameDuration());
		}
		return value;
	}

	@ModifyVariable(method = "render", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/util/math/Vec3f;dot(Lnet/minecraft/util/math/Vec3f;)F"), ordinal = 7, require = 1, allow = 1)
	private static float afterPlaneDot(float dotProduct) {
		if (ChatLog.ANTI_FOG_MODULE.enabled) {
			return 0;
		}
		return dotProduct;
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getRainGradient(F)F"), require = 1, allow = 1)
	private static float onGetRainGradient(ClientWorld instance, float tickDelta) {
		if (ChatLog.ANTI_FOG_MODULE.enabled) {
			return 0;
		}
		return instance.getRainGradient(tickDelta);
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getThunderGradient(F)F"), require = 1, allow = 1)
	private static float onGetThunderGradient(ClientWorld instance, float tickDelta) {
		if (ChatLog.ANTI_FOG_MODULE.enabled) {
			return 0;
		}
		return instance.getThunderGradient(tickDelta);
	}

}
