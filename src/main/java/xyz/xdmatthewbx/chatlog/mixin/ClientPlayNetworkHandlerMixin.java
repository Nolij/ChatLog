package xyz.xdmatthewbx.chatlog.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.xdmatthewbx.chatlog.modules.ESPModule;

@Mixin(ClientPacketListener.class)
public class ClientPlayNetworkHandlerMixin {

	@Inject(method = "handleBlockEntityData", at = @At("TAIL"))
	public void onBlockEntityUpdate(ClientboundBlockEntityDataPacket packet, CallbackInfo ci) {
		if (ESPModule.INSTANCE.enabled) {
			ESPModule.INSTANCE.cacheBlockPosAsync(packet.getPos().immutable());
		}
	}

}
