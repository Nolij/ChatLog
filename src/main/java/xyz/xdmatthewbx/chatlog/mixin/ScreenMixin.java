package xyz.xdmatthewbx.chatlog.mixin;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.xdmatthewbx.chatlog.modules.ToolTipInfoModule;

@Mixin(Screen.class)
public class ScreenMixin {

	@Redirect(method = "renderComponentHoverEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Style;getHoverEvent()Lnet/minecraft/network/chat/HoverEvent;"))
	public HoverEvent formatStyles(Style instance) {
		if (ToolTipInfoModule.INSTANCE.enabled) {
			return ToolTipInfoModule.INSTANCE.getHoverEvent(instance);
		} else {
			return instance.getHoverEvent();
		}
	}

}
