package xyz.xdmatthewbx.chatlog.modules;

import net.minecraft.util.ActionResult;
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
}
