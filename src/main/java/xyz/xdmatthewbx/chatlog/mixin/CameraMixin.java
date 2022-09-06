package xyz.xdmatthewbx.chatlog.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import xyz.xdmatthewbx.chatlog.ChatLog;

@Environment(EnvType.CLIENT)
@Mixin(Camera.class)
public class CameraMixin {

	@Shadow private float pitch;
	@Shadow	private float yaw;

	@Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;moveBy(DDD)V", ordinal = 0))
	private void moveBy(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
		if (ChatLog.PERSPECTIVE_MODULE.enabled) {
			this.pitch = ChatLog.PERSPECTIVE_MODULE.pitch;
			this.yaw = ChatLog.PERSPECTIVE_MODULE.yaw;
		}
	}

	@ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V", ordinal = 0))
	private void setRotation(Args args) {
		if (ChatLog.PERSPECTIVE_MODULE.enabled) {
			args.set(0, ChatLog.PERSPECTIVE_MODULE.yaw);
			args.set(1, ChatLog.PERSPECTIVE_MODULE.pitch);
		}
	}

}