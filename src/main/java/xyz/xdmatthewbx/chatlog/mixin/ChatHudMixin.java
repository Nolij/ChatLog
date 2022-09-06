package xyz.xdmatthewbx.chatlog.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(ChatHud.class)
public class ChatHudMixin {

	@Inject(method = "getStyleAt", at = @At("RETURN"), cancellable = true)
	public void getStyleAt(double x, double y, CallbackInfoReturnable<Style> cir) {
//		Style style = cir.getReturnValue();
//		if (style != null) cir.setReturnValue(ChatLogClient.styleFormatter(style));
	}

}
