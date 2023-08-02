package xyz.xdmatthewbx.chatlog.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.xdmatthewbx.chatlog.modules.ToolTipInfoModule;

@Mixin(DrawContext.class)
public class DrawContextMixin {

	@WrapOperation(method = "drawHoverEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/Style;getHoverEvent()Lnet/minecraft/text/HoverEvent;"))
	public HoverEvent formatStyles(Style instance, Operation<HoverEvent> original) {
		if (ToolTipInfoModule.INSTANCE.enabled) {
			return ToolTipInfoModule.INSTANCE.getHoverEvent(instance);
		} else {
			return original.call(instance);
		}
	}

}
