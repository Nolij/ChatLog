package xyz.xdmatthewbx.chatlog.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.xdmatthewbx.chatlog.ChatLog;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

	@Inject(method = "updateBlock", at = @At("TAIL"))
	public void updateBlock(BlockView world, BlockPos pos, BlockState oldState, BlockState newState, int flags, CallbackInfo ci) {
//		if (ChatLog.ESP_MODULE.enabled) {
//			ChatLog.ESP_MODULE.cacheBlockPosAsync(pos);
//		}
	}

}
