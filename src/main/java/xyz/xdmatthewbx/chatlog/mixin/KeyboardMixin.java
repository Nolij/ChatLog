package xyz.xdmatthewbx.chatlog.mixin;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.xdmatthewbx.chatlog.KeyBind;

@Mixin(Keyboard.class)
public class KeyboardMixin {

	@Shadow @Final private MinecraftClient client;

	@Inject(method = "onKey", at = @At("HEAD"))
	public void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
		if (window != this.client.getWindow().getHandle()) return;
		Screen currentScreen = this.client.currentScreen;
		if (action == 0 || currentScreen == null || currentScreen.passEvents) {
			for (KeyBind bind : KeyBind.getAllBinds()) {
				if (bind.matches(key, scancode)) {
					bind.setPressed(action != 0);
				}
			}
 		}
	}

}
