package dev.nolij.chatlog.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import dev.nolij.chatlog.ChatLog;
import net.minecraft.client.Mouse;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Mouse.class)
public class MouseMixin {

	@Inject(method = "updateMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/tutorial/TutorialManager;onUpdateMouse(DD)V"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
	private void onUpdateMouse(CallbackInfo ci, double d, double e, double yaw, double pitch, double f, double g, double h, int invert) {
		if (ChatLog.cameraLock.isLocked()) {
			ChatLog.cameraYaw += (float) (yaw / 8D);
			ChatLog.cameraPitch += (float) ((pitch * invert) / 8D);

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
	
	@ModifyExpressionValue(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isSpectator()Z"))
	public boolean onMouseScroll$isSpectator(boolean original) {
		if (ChatLog.scrollLock.isLocked())
			return false;
		
		return original;
	}
	
	@WrapWithCondition(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;scrollInHotbar(D)V"))
	public boolean onMouseScroll$scrollInHotbar(PlayerInventory instance, double scrollAmount) {
		if (!ChatLog.scrollLock.isLocked())
			return true;
		
		ChatLog.scrollDelta += scrollAmount;
		return false;
	}

}
