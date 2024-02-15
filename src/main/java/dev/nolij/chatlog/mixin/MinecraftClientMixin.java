package dev.nolij.chatlog.mixin;

import dev.nolij.chatlog.ChatLog;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
	
	@Shadow @Final public GameOptions options;
	
	@Redirect(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;wasPressed()Z"))
	public boolean handleInputEvents$wasPressed(KeyBinding instance) {
		var wasPressed = instance.wasPressed(); // flush pressed status even if spoofing
		if ((ChatLog.cameraLock.isLocked() && instance == options.togglePerspectiveKey) ||
			(ChatLog.interactionLock.isLocked() && (instance == options.attackKey || instance == options.useKey)))
			return false;
		
		return wasPressed;
	}
	
	@Redirect(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;isPressed()Z"))
	public boolean handleInputEvents$isPressed(KeyBinding instance) {
		if (ChatLog.interactionLock.isLocked() && (instance == options.attackKey || instance == options.useKey))
			return false;
		
		return instance.isPressed();
	}

}
