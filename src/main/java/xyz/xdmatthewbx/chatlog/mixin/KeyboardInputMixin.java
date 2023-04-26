package xyz.xdmatthewbx.chatlog.mixin;

import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.xdmatthewbx.chatlog.ChatLog;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin {

	@Inject(method = "tick", at = @At("TAIL"))
	public void verticalLock(boolean slowDown, float f, CallbackInfo ci) {
		if (ChatLog.movementLock.isLocked()) {
			((KeyboardInput) (Object) this).up = false;
			((KeyboardInput) (Object) this).down = false;
			((KeyboardInput) (Object) this).left = false;
			((KeyboardInput) (Object) this).right = false;
			((KeyboardInput) (Object) this).forwardImpulse = 0;
			((KeyboardInput) (Object) this).leftImpulse = 0;
			((KeyboardInput) (Object) this).jumping = false;
			((KeyboardInput) (Object) this).shiftKeyDown = false;
		}
	}

}
