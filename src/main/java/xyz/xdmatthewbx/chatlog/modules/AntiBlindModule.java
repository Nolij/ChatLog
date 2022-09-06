package xyz.xdmatthewbx.chatlog.modules;

import net.minecraft.util.ActionResult;

import static xyz.xdmatthewbx.chatlog.ChatLog.CONFIG;
import static xyz.xdmatthewbx.chatlog.ChatLog.registerChangeListener;

public class AntiBlindModule extends BaseModule {

	public static final String MODULE_ID = "antiblind";
	public static AntiBlindModule INSTANCE;

	public boolean enabled = true;

	public AntiBlindModule() {
		super(MODULE_ID);
		INSTANCE = this;
	}

	@Override
	public void onInitializeClient() {
		registerChangeListener(CONFIG, (configHolder, chatLogConfig) -> {
			enabled = chatLogConfig.main.antiBlindModule.enabled;
			return ActionResult.PASS;
		});
	}

}