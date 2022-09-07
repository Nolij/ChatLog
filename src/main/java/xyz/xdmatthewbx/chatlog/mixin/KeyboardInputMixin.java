package xyz.xdmatthewbx.chatlog.mixin;

import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.option.KeyBind;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.xdmatthewbx.chatlog.ChatLog;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin {

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBind;isPressed()Z"))
	public boolean isPressed(KeyBind instance) {
		if (ChatLog.movementLock.isLocked()) {
			return false;
		}

		return instance.isPressed();
	}

}
