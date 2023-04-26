package xyz.xdmatthewbx.chatlog.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import xyz.xdmatthewbx.chatlog.ChatLog;
import xyz.xdmatthewbx.chatlog.modules.FreeCamModule;

@Mixin(LevelRenderer.class)
public class WorldRendererMixin {

	@Redirect(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;isDetached()Z"))
	public boolean isThirdPerson(Camera instance) {
		if (FreeCamModule.INSTANCE.enabled) {
			return true;
		}

		return instance.isDetached();
	}

	@ModifyArgs(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;setupRender(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/culling/Frustum;ZZ)V"))
	public void setupTerrain(Args args) {
		if (FreeCamModule.INSTANCE.enabled && ChatLog.CONFIG.get().main.render.allowRenderThroughBlocks) {
			args.set(3, true);
		}
	}

}
