package xyz.xdmatthewbx.chatlog.mixin;

import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import xyz.xdmatthewbx.chatlog.ChatLog;

@Mixin(Mouse.class)
public class MouseMixin {

	@Inject(method = "updateMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/tutorial/TutorialManager;onUpdateMouse(DD)V"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
	private void onUpdateMouse(CallbackInfo ci, double d, double e, double f, double g, double yaw, double pitch, int invert) {
		if (ChatLog.cameraLock.isLocked()) {
			ChatLog.cameraYaw += yaw / 8F;
			ChatLog.cameraPitch += (pitch * invert) / 8F;

			if (Math.abs(ChatLog.cameraPitch) > 90F) {
				ChatLog.cameraPitch = ChatLog.cameraPitch > 0F ? 90F : -90F;
			}
		}
	}

	@Inject(method = "updateMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"), cancellable = true)
	private void changeLookDirection(CallbackInfo ci) {
		if (ChatLog.cameraLock.isLocked()) {
			ci.cancel();
		}
	}

}
