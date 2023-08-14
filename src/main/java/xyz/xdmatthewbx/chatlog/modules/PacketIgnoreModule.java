package xyz.xdmatthewbx.chatlog.modules;

import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.util.ActionResult;
import xyz.xdmatthewbx.chatlog.ChatLog;
import xyz.xdmatthewbx.chatlog.ChatLogConfig;

import static xyz.xdmatthewbx.chatlog.ChatLog.CONFIG;
import static xyz.xdmatthewbx.chatlog.ChatLog.registerChangeListener;

@Module
public class PacketIgnoreModule extends BaseModule {

	public static final String MODULE_ID = "packet_ignore";
	public static PacketIgnoreModule INSTANCE;

	public ChatLogConfig.OffSafeUnsafe ignoreCloseScreenPackets = ChatLogConfig.OffSafeUnsafe.SAFE;

	public PacketIgnoreModule() {
		super(MODULE_ID);
		INSTANCE = this;
	}

	@Override
	public void onInitializeClient() {
		registerChangeListener(CONFIG, (configHolder, chatLogConfig) -> {
			ignoreCloseScreenPackets =
				chatLogConfig.main.general.enabled
				? chatLogConfig.main.packetIgnoreModule.ignoreCloseScreenPackets
				: ChatLogConfig.OffSafeUnsafe.OFF;
			return ActionResult.PASS;
		});
	}

	public boolean shouldIgnorePacket(CloseScreenS2CPacket packet) {
		switch (PacketIgnoreModule.INSTANCE.ignoreCloseScreenPackets) {
			case OFF -> {
				return false;
			}
			case UNSAFE -> {
				return true;
			}
		}

		var screen = ChatLog.CLIENT.currentScreen;
		if (screen == null)
			return false;

		// block server closing these types of screens
		if (screen instanceof AbstractInventoryScreen<?> || // player inventory
			screen instanceof GameOptionsScreen || // menus
			screen instanceof OptionsScreen ||
			screen instanceof GameMenuScreen ||
			screen instanceof ChatScreen) // chat
			return true;

		// block server closing modded screens; allow it to close non-whitelisted vanilla screens
		var packageName = screen.getClass().getPackageName();
		return !(packageName.equals("net.minecraft") || packageName.startsWith("net.minecraft."));
	}
}
