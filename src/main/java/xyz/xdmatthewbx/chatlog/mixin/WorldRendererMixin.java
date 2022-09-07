package xyz.xdmatthewbx.chatlog.mixin;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import xyz.xdmatthewbx.chatlog.ChatLog;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;isThirdPerson()Z"))
	public boolean isThirdPerson(Camera instance) {
		if (ChatLog.FREECAM_MODULE.enabled) {
			return true;
		}

		return instance.isThirdPerson();
	}

	@ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;setupTerrain(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;ZZ)V"))
	public void setupTerrain(Args args) {
		if (ChatLog.FREECAM_MODULE.enabled && ChatLog.CONFIG.get().main.render.allowRenderThroughBlocks) {
			args.set(3, true);
		}
	}

}
