package xyz.xdmatthewbx.chatlog.mixin;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import xyz.xdmatthewbx.chatlog.ChatLog;

@Mixin(MouseHandler.class)
public class MouseMixin {

	@Inject(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/tutorial/Tutorial;onMouse(DD)V"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
	private void onUpdateMouse(CallbackInfo ci, double d, double e, double yaw, double pitch, double f, double g, double h, int invert) {
		if (ChatLog.cameraLock.isLocked()) {
			ChatLog.cameraYaw += yaw / 8F;
			ChatLog.cameraPitch += (pitch * invert) / 8F;

			if (Math.abs(ChatLog.cameraPitch) > 90F) {
				ChatLog.cameraPitch = ChatLog.cameraPitch > 0F ? 90F : -90F;
			}
		}
	}

	@Inject(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"), cancellable = true)
	private void changeLookDirection(CallbackInfo ci) {
		if (ChatLog.cameraLock.isLocked()) {
			ci.cancel();
		}
	}

}
