package xyz.xdmatthewbx.chatlog.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkState;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.xdmatthewbx.chatlog.modules.LagSwitchModule;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin {
	
	@Shadow
	protected abstract void sendInternal(Packet<?> packet, @Nullable PacketCallbacks callbacks, NetworkState packetState, NetworkState currentState);
	
	@Shadow
	private static <T extends PacketListener> void handlePacket(Packet<T> packet, PacketListener listener) {}
	
	@Inject(method = "sendInternal", at = @At("HEAD"), cancellable = true)
	private void sendInternal(Packet<?> packet, @Nullable PacketCallbacks callbacks, NetworkState packetState,
	                          NetworkState currentState, CallbackInfo ci) {
		if (LagSwitchModule.INSTANCE.active) {
			LagSwitchModule.INSTANCE.QUEUED_PACKETS.add(() -> sendInternal(packet, callbacks, packetState, currentState));
			ci.cancel();
		}
	}
	
	@Inject(method = "handlePacket", at = @At("HEAD"), cancellable = true)
	private static <T extends PacketListener> void handlePacket(Packet<T> packet, PacketListener listener, CallbackInfo ci) {
		if (LagSwitchModule.INSTANCE.active) {
			LagSwitchModule.INSTANCE.QUEUED_PACKETS.add(() -> handlePacket(packet, listener));
			ci.cancel();
		}
	}
	
}
