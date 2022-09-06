package xyz.xdmatthewbx.chatlog.mixin;

import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.chunk.ChunkRenderRegionCache;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.xdmatthewbx.chatlog.ChatLog;

@Mixin(ChunkBuilder.BuiltChunk.class)
public abstract class BuiltChunkMixin {

	@Shadow
	public abstract BlockPos getOrigin();

	@Inject(method = "scheduleRebuild(Z)V", at = @At("HEAD"))
	public void scheduleRebuild(boolean important, CallbackInfo ci) {
//		if (ChatLog.ESP_MODULE.enabled) {
//			ChatLog.ESP_MODULE.cacheChunkAsync(getOrigin());
//		}
	}

}
