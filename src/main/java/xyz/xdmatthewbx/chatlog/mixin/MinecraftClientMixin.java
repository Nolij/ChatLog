package xyz.xdmatthewbx.chatlog.mixin;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.xdmatthewbx.chatlog.ChatLog;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {

	@Redirect(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;consumeClick()Z", ordinal = 0))
	public boolean wasPressed(KeyMapping instance) {
		var wasPressed = instance.consumeClick(); // flush pressed status even if spoofing
		if (ChatLog.cameraLock.isLocked()) {
			return false;
		}

		return wasPressed;
	}

}
