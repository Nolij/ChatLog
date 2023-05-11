package xyz.xdmatthewbx.chatlog.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.xdmatthewbx.chatlog.modules.ToolTipInfoModule;

@Mixin(DrawContext.class)
public class DrawContextMixin {

	@Redirect(method = "drawHoverEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/Style;getHoverEvent()Lnet/minecraft/text/HoverEvent;"))
	public HoverEvent formatStyles(Style instance) {
		if (ToolTipInfoModule.INSTANCE.enabled) {
			return ToolTipInfoModule.INSTANCE.getHoverEvent(instance);
		} else {
			return instance.getHoverEvent();
		}
	}

}
