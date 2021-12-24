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
		if (ChatLog.cameraLock.isLocked()) {
			this.pitch = ChatLog.cameraPitch;
			this.yaw = ChatLog.cameraYaw;
		}
	}

	@ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V", ordinal = 0))
	private void setRotation(Args args) {
		if (ChatLog.cameraLock.isLocked()) {
			args.set(0, ChatLog.cameraYaw);
			args.set(1, ChatLog.cameraPitch);
		}
	}

	@ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V", ordinal = 0))
	private void setPos(Args args) {
		if (ChatLog.cameraLock.isLocked() && ChatLog.cameraPos != null) {
			args.set(0, ChatLog.cameraPos.x);
			args.set(1, ChatLog.cameraPos.y);
			args.set(2, ChatLog.cameraPos.z);
		}
	}

}
