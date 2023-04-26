package xyz.xdmatthewbx.chatlog.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.xdmatthewbx.chatlog.ChatLog;
import xyz.xdmatthewbx.chatlog.modules.FullBrightModule;

@Mixin(LightTexture.class)
public class LightmapTextureManagerMixin {

	@Shadow
	@Final
	private Minecraft minecraft;

	@Redirect(method = "updateLightTexture", at = @At(value = "INVOKE", target = "Ljava/lang/Double;floatValue()F"))
	public float update(Double instance) {
		if (FullBrightModule.INSTANCE.enabled) {
			return 1E7F;
		}

		return minecraft.options.gamma().get().floatValue();
	}

}
