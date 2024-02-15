package dev.nolij.chatlog.mixin;

import dev.nolij.chatlog.KeyBind;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyBinding.class)
public class KeyBindingMixin {
	
	@Inject(method = "setKeyPressed", at = @At("HEAD"))
	private static void chatlog$setKeyPressed(InputUtil.Key key, boolean pressed, CallbackInfo ci) {
		KeyBind.setPressed(key, pressed);
	}
	
	@Inject(method = "onKeyPressed", at = @At("HEAD"))
	private static void chatlog$onKeyPressed(InputUtil.Key key, CallbackInfo ci) {
		KeyBind.press(key);
	}
	
}
