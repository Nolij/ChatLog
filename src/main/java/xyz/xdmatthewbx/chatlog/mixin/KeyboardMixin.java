package xyz.xdmatthewbx.chatlog.mixin;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.xdmatthewbx.chatlog.KeyBind;

@Mixin(KeyboardHandler.class)
public class KeyboardMixin {

	@Shadow @Final private Minecraft minecraft;

	@Inject(method = "keyPress", at = @At("HEAD"))
	public void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
		if (window != this.minecraft.getWindow().getWindow()) return;
		Screen currentScreen = this.minecraft.screen;
		if (action == 0 || currentScreen == null || currentScreen.passEvents) {
			for (KeyBind bind : KeyBind.getAllBinds()) {
				if (bind.matches(key, scancode)) {
					bind.setPressed(action != 0);
				}
			}
 		}
	}

}
