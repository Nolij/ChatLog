package dev.nolij.chatlog.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.nolij.chatlog.ChatLog;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

	@Shadow private float pitch;
	@Shadow	private float yaw;
	
	@Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;moveBy(DDD)V", ordinal = 0))
	private void moveBy(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
		if (ChatLog.cameraLock.isLocked()) {
			this.pitch = ChatLog.cameraPitch;
			this.yaw = ChatLog.cameraYaw;
		}
	}

	@WrapOperation(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V", ordinal = 0))
	private void setRotation(Camera instance, float yaw, float pitch, Operation<Void> original) {
		if (ChatLog.cameraLock.isLocked()) {
			yaw = ChatLog.cameraYaw;
			pitch = ChatLog.cameraPitch;
		}
		
		original.call(instance, yaw, pitch);
	}

	@WrapOperation(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V", ordinal = 0))
	private void setPos(Camera instance, double x, double y, double z, Operation<Void> original) {
		if (ChatLog.cameraLock.isLocked() && ChatLog.cameraPos != null) {
			if (ChatLog.prevCameraPos == null) {
				x = ChatLog.cameraPos.x;
				y = ChatLog.cameraPos.y;
				z = ChatLog.cameraPos.z;
			} else {
				x = MathHelper.lerp(ChatLog.getTickDelta(), ChatLog.prevCameraPos.x, ChatLog.cameraPos.x);
				y = MathHelper.lerp(ChatLog.getTickDelta(), ChatLog.prevCameraPos.y, ChatLog.cameraPos.y);
				z = MathHelper.lerp(ChatLog.getTickDelta(), ChatLog.prevCameraPos.z, ChatLog.cameraPos.z);
			}
		}
		
		original.call(instance, x, y, z);
	}

}
