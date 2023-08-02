package xyz.xdmatthewbx.chatlog.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import xyz.xdmatthewbx.chatlog.ChatLog;
import xyz.xdmatthewbx.chatlog.modules.FreeCamModule;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;isThirdPerson()Z"))
	public boolean isThirdPerson(Camera instance, Operation<Boolean> original) {
		if (FreeCamModule.INSTANCE.enabled) {
			return true;
		}

		return original.call(instance);
	}

	@ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;setupTerrain(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;ZZ)V"))
	public void setupTerrain(Args args) {
		if (FreeCamModule.INSTANCE.enabled && ChatLog.CONFIG.get().main.render.allowRenderThroughBlocks) {
			args.set(3, true);
		}
	}

}
