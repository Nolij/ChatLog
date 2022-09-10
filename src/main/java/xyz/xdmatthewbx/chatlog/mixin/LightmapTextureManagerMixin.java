package xyz.xdmatthewbx.chatlog.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.xdmatthewbx.chatlog.ChatLog;
import xyz.xdmatthewbx.chatlog.modules.FullBrightModule;

@Mixin(LightmapTextureManager.class)
public class LightmapTextureManagerMixin {

	@Shadow
	@Final
	private MinecraftClient client;

	@Redirect(method = "update", at = @At(value = "INVOKE", target = "Ljava/lang/Double;floatValue()F"))
	public float update(Double instance) {
		if (FullBrightModule.INSTANCE.enabled) {
			return 1E7F;
		}

		return client.options.getGamma().get().floatValue();
	}

}
