package xyz.xdmatthewbx.chatlog.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.xdmatthewbx.chatlog.ChatLog;

@Mixin(WorldChunk.class)
public abstract class WorldChunkMixin {

	@Shadow
	public abstract World getWorld();

	@Inject(method = "setBlockState", at = @At("RETURN"))
	private void setBlockState(BlockPos pos, BlockState state, boolean moved, CallbackInfoReturnable<BlockState> cir) {
		if (ChatLog.ESP_MODULE.enabled && cir.getReturnValue() != null && this.getWorld().isClient) {
			ChatLog.ESP_MODULE.cacheBlockPosAsync(pos.toImmutable());
		}
	}

}
