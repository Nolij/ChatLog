package xyz.xdmatthewbx.chatlog.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBind;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.xdmatthewbx.chatlog.ChatLog;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

	@Redirect(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBind;wasPressed()Z", ordinal = 0))
	public boolean wasPressed(KeyBind instance) {
		var wasPressed = instance.wasPressed(); // flush pressed status even if spoofing
		if (ChatLog.cameraLock.isLocked()) {
			return false;
		}

		return wasPressed;
	}

}
