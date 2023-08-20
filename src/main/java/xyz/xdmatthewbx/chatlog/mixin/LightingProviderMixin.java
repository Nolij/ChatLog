package xyz.xdmatthewbx.chatlog.mixin;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.light.LightingProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.xdmatthewbx.chatlog.modules.FullBrightModule;

@Mixin(LightingProvider.class)
public class LightingProviderMixin {
	
	@Unique
	private BlockView theWorld;
	
	@Inject(method = "<init>", at = @At("RETURN"))
	private void storeWorld(ChunkProvider chunkProvider, boolean hasBlockLight, boolean hasSkyLight, CallbackInfo ci) {
		theWorld = chunkProvider.getWorld();
	}
	
	@Inject(method = "checkBlock", at = @At("HEAD"), cancellable = true)
	public void checkBlock(BlockPos blockPos, CallbackInfo ci) {
		if (FullBrightModule.INSTANCE.enabled &&
			theWorld instanceof ClientWorld)
			ci.cancel();
	}

	@Inject(method = "doLightUpdates", at = @At("HEAD"), cancellable = true)
	public void doLightUpdates(CallbackInfoReturnable<Integer> cir) {
		if (FullBrightModule.INSTANCE.enabled &&
			theWorld instanceof ClientWorld)
			cir.setReturnValue(0);
	}

}
