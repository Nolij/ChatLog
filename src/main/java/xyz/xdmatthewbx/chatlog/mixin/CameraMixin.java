package xyz.xdmatthewbx.chatlog.mixin;

import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import xyz.xdmatthewbx.chatlog.ChatLog;

@Mixin(Camera.class)
public class CameraMixin {

	@Shadow private float xRot;
	@Shadow	private float yRot;

	@Inject(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;move(DDD)V", ordinal = 0))
	private void moveBy(BlockGetter area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
		if (ChatLog.cameraLock.isLocked()) {
			this.xRot = ChatLog.cameraPitch;
			this.yRot = ChatLog.cameraYaw;
		}
	}

	@ModifyArgs(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V"))
	private void setRotation(Args args) {
		if (ChatLog.cameraLock.isLocked()) {
			args.set(0, ChatLog.cameraYaw);
			args.set(1, ChatLog.cameraPitch);
		}
	}

	@ModifyArgs(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setPosition(DDD)V", ordinal = 0))
	private void setPos(Args args) {
		if (ChatLog.cameraLock.isLocked() && ChatLog.cameraPos != null) {
			if (ChatLog.prevCameraPos == null) {
				args.set(0, ChatLog.cameraPos.x);
				args.set(1, ChatLog.cameraPos.y);
				args.set(2, ChatLog.cameraPos.z);
			} else {
				args.set(0, Mth.lerp(ChatLog.getTickDelta(), ChatLog.prevCameraPos.x, ChatLog.cameraPos.x));
				args.set(1, Mth.lerp(ChatLog.getTickDelta(), ChatLog.prevCameraPos.y, ChatLog.cameraPos.y));
				args.set(2, Mth.lerp(ChatLog.getTickDelta(), ChatLog.prevCameraPos.z, ChatLog.cameraPos.z));
			}
		}
	}

}
