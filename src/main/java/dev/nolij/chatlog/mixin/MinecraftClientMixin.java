package dev.nolij.chatlog.mixin;

import dev.nolij.chatlog.ChatLog;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

	@Redirect(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;wasPressed()Z", ordinal = 0))
	public boolean wasPressed(KeyBinding instance) {
		var wasPressed = instance.wasPressed(); // flush pressed status even if spoofing
		if (ChatLog.cameraLock.isLocked()) {
			return false;
		}

		return wasPressed;
	}

}
