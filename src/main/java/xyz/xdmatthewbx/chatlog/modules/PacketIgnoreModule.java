package xyz.xdmatthewbx.chatlog.modules;

import net.minecraft.util.ActionResult;

import static xyz.xdmatthewbx.chatlog.ChatLog.CONFIG;
import static xyz.xdmatthewbx.chatlog.ChatLog.registerChangeListener;

@Module
public class PacketIgnoreModule extends BaseModule {

	public static final String MODULE_ID = "packet_ignore";
	public static PacketIgnoreModule INSTANCE;

	public boolean enabled = true;

	public PacketIgnoreModule() {
		super(MODULE_ID);
		INSTANCE = this;
	}

	@Override
	public void onInitializeClient() {
		registerChangeListener(CONFIG, (configHolder, chatLogConfig) -> {
			enabled = chatLogConfig.main.packetIgnoreModule.enabled;
			return ActionResult.PASS;
		});
	}
}
