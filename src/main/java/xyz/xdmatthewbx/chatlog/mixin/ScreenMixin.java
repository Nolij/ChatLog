package xyz.xdmatthewbx.chatlog.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.xdmatthewbx.chatlog.ChatLog;

@Environment(EnvType.CLIENT)
@Mixin(Screen.class)
public class ScreenMixin {

	@Redirect(method = "renderTextHoverEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/Style;getHoverEvent()Lnet/minecraft/text/HoverEvent;"))
	public HoverEvent formatStyles(Style instance) {
		if (ChatLog.TOOLTIP_INFO_MODULE.enabled) {
			return ChatLog.TOOLTIP_INFO_MODULE.getHoverEvent(instance);
		} else {
			return instance.getHoverEvent();
		}
	}

}
