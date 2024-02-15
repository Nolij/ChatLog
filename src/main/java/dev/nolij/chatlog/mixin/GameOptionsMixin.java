package dev.nolij.chatlog.mixin;

import dev.nolij.chatlog.ChatLog;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.Perspective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameOptions.class)
public class GameOptionsMixin {
	
	@Inject(method = "getPerspective", at = @At("TAIL"), cancellable = true)
	public void chatlog$getPerspective(CallbackInfoReturnable<Perspective> cir) {
		if (ChatLog.perspectiveLock.isLocked() && ChatLog.perspective != null) {
			cir.setReturnValue(ChatLog.perspective);
		}
	}
	
}
