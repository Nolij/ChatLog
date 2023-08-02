package xyz.xdmatthewbx.chatlog.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.xdmatthewbx.chatlog.ChatLog;
import xyz.xdmatthewbx.chatlog.modules.FullBrightModule;

@Mixin(LightmapTextureManager.class)
public class LightmapTextureManagerMixin {

	@WrapOperation(method = "update", at = @At(value = "INVOKE", target = "Ljava/lang/Double;floatValue()F"))
	public float update(Double instance, Operation<Float> original) {
		if (FullBrightModule.INSTANCE.enabled) {
			return 1E7F;
		}

		return original.call(instance);
	}

}
