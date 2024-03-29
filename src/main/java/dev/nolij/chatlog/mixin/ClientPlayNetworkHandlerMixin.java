package dev.nolij.chatlog.mixin;

import dev.nolij.chatlog.modules.ESPModule;
import dev.nolij.chatlog.modules.PacketIgnoreModule;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

	@Inject(method = "onBlockEntityUpdate", at = @At("TAIL"))
	public void onBlockEntityUpdate(BlockEntityUpdateS2CPacket packet, CallbackInfo ci) {
		if (ESPModule.INSTANCE.enabled)
			ESPModule.INSTANCE.cacheBlockPosAsync(packet.getPos().toImmutable());
	}

	@Inject(method = "onCloseScreen", at = @At("HEAD"), cancellable = true)
	public void onCloseScreen(CloseScreenS2CPacket packet, CallbackInfo ci) {
		if (PacketIgnoreModule.INSTANCE.shouldIgnorePacket(packet))
			ci.cancel();
	}

}
