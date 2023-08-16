package xyz.xdmatthewbx.chatlog.mixin;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.chunk.light.LightingProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.xdmatthewbx.chatlog.modules.FullBrightModule;

@Mixin(LightingProvider.class)
public class LightingProviderMixin {
	
	@Shadow
	@Final
	protected HeightLimitView world;
	
	@Inject(method = "checkBlock", at = @At("HEAD"), cancellable = true)
	public void checkBlock(BlockPos blockPos, CallbackInfo ci) {
		if (FullBrightModule.INSTANCE.enabled &&
			world instanceof ClientWorld)
			ci.cancel();
	}

	@Inject(method = "doLightUpdates", at = @At("HEAD"), cancellable = true)
	public void doLightUpdates(CallbackInfoReturnable<Integer> cir) {
		if (FullBrightModule.INSTANCE.enabled &&
			world instanceof ClientWorld)
			cir.setReturnValue(0);
	}

}
