package xyz.xdmatthewbx.chatlog.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.xdmatthewbx.chatlog.modules.AntiBlindModule;
import xyz.xdmatthewbx.chatlog.modules.FullBrightModule;

@Mixin(LightmapTextureManager.class)
public class LightmapTextureManagerMixin {

	@WrapOperation(method = "update", at = @At(value = "INVOKE", target = "Ljava/lang/Double;floatValue()F"))
	public float update(Double instance, Operation<Float> original) {
		if (FullBrightModule.INSTANCE.enabled) {
			return 1E7F;
		}

		return original.call(instance);
	}

	@Inject(method = "getDarkness", at = @At("HEAD"), cancellable = true)
	public void getDarkness(LivingEntity entity, float factor, float delta, CallbackInfoReturnable<Float> cir) {
		if (FullBrightModule.INSTANCE.enabled)
			cir.setReturnValue(0F);
	}

	@WrapOperation(method = "getDarknessFactor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;hasStatusEffect(Lnet/minecraft/entity/effect/StatusEffect;)Z"))
	public boolean getDarknessFactor$hasStatusEffect(ClientPlayerEntity instance, StatusEffect statusEffect, Operation<Boolean> original) {
		if (AntiBlindModule.INSTANCE.enabled &&
			(statusEffect == StatusEffects.BLINDNESS || statusEffect == StatusEffects.DARKNESS))
			return false;

		return original.call(instance, statusEffect);
	}

}
