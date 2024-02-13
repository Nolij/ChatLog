package dev.nolij.chatlog.mixin;

import dev.nolij.chatlog.modules.InputUnlockModule;
import net.minecraft.SharedConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SharedConstants.class)
public class SharedConstantsMixin {

	@Inject(method = "isValidChar", at = @At("HEAD"), cancellable = true)
	private static void isValidChar(char chr, CallbackInfoReturnable<Boolean> cir) {
		//noinspection UnnecessaryUnicodeEscape
		if (InputUnlockModule.INSTANCE.enabled && chr == '\u00a7') {
			cir.setReturnValue(true);
		}
	}

}
