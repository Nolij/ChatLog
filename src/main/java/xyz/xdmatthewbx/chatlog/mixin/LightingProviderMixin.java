package xyz.xdmatthewbx.chatlog.mixin;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.light.LightingProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.xdmatthewbx.chatlog.modules.FullBrightModule;

@Mixin(LightingProvider.class)
public class LightingProviderMixin {

	@Inject(method = "checkBlock", at = @At("HEAD"), cancellable = true)
	public void checkBlock(BlockPos blockPos, CallbackInfo ci) {
		if (FullBrightModule.INSTANCE.enabled)
			ci.cancel();
	}

	@Inject(method = "doLightUpdates", at = @At("HEAD"), cancellable = true)
	public void doLightUpdates(CallbackInfoReturnable<Integer> cir) {
		if (FullBrightModule.INSTANCE.enabled)
			cir.setReturnValue(0);
	}

}
