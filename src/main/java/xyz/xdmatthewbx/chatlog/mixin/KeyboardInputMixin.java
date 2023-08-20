package xyz.xdmatthewbx.chatlog.mixin;

import net.minecraft.client.input.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.xdmatthewbx.chatlog.ChatLog;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin {

	@Inject(method = "tick", at = @At("TAIL"))
	public void verticalLock(boolean slowDown, CallbackInfo ci) {
		if (ChatLog.movementLock.isLocked()) {
			((KeyboardInput) (Object) this).pressingForward = false;
			((KeyboardInput) (Object) this).pressingBack = false;
			((KeyboardInput) (Object) this).pressingLeft = false;
			((KeyboardInput) (Object) this).pressingRight = false;
			((KeyboardInput) (Object) this).movementForward = 0;
			((KeyboardInput) (Object) this).movementSideways = 0;
			((KeyboardInput) (Object) this).jumping = false;
			((KeyboardInput) (Object) this).sneaking = false;
		}
	}

}
