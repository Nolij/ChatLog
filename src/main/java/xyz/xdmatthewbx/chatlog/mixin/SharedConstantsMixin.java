package xyz.xdmatthewbx.chatlog.mixin;

import net.minecraft.SharedConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.xdmatthewbx.chatlog.ChatLog;
import xyz.xdmatthewbx.chatlog.modules.InputUnlockModule;

@Mixin(SharedConstants.class)
public class SharedConstantsMixin {

	@Inject(method = "isValidChar", at = @At("HEAD"), cancellable = true)
	private static void isValidChar(char chr, CallbackInfoReturnable<Boolean> cir) {
		if (InputUnlockModule.INSTANCE.enabled && chr == '\u00a7') {
			cir.setReturnValue(true);
		}
	}

}
