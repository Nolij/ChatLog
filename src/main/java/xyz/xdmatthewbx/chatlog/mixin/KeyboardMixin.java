package xyz.xdmatthewbx.chatlog.mixin;

import me.shedaniel.clothconfig2.api.Modifier;
import me.shedaniel.clothconfig2.api.ModifierKeyCode;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.xdmatthewbx.chatlog.ChatLog;
import xyz.xdmatthewbx.chatlog.KeyBind;

import java.util.Collection;

@Environment(EnvType.CLIENT)
@Mixin(Keyboard.class)
public class KeyboardMixin {

	@Shadow @Final private MinecraftClient client;

	@Inject(method = "onKey", at = @At("HEAD"))
	public void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
		if (window != this.client.getWindow().getHandle()) return;
		Screen currentScreen = this.client.currentScreen;
		if (action == 0 || currentScreen == null || currentScreen.passEvents) {
//			for (KeyBinding bind : ChatLog.KEYBIND_REGISTRY.stream().toList()) {
//				if (!KeyBinding.KEY_TO_BINDINGS.containsKey(InputUtil.fromKeyCode(key, scancode)) && bind.matchesKey(key, scancode)) {
//					bind.setPressed(action != 0);
//					if (action != 0) bind.timesPressed++;
//				}
//			}
			for (KeyBind bind : KeyBind.getAllBinds()) {
				if (bind.matches(key, scancode)) {
					bind.setPressed(action != 0);
				}
			}
 		}
	}

}
