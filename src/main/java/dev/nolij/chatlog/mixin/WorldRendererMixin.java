package dev.nolij.chatlog.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.nolij.chatlog.ChatLog;
import dev.nolij.chatlog.modules.FreeCamModule;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;isThirdPerson()Z"))
	public boolean isThirdPerson(Camera instance, Operation<Boolean> original) {
		if (FreeCamModule.INSTANCE.enabled)
			return true;

		return original.call(instance);
	}

	@ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;setupTerrain(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;ZZ)V"), index = 3)
	public boolean setupTerrain(boolean original) {
		if (!original && FreeCamModule.INSTANCE.enabled && ChatLog.CONFIG.get().main.render.allowRenderThroughBlocks)
			return true;
		
		return original;
	}

}
