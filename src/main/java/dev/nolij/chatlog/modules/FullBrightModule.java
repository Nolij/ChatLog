package dev.nolij.chatlog.modules;

import net.minecraft.util.ActionResult;

import static dev.nolij.chatlog.ChatLog.CONFIG;
import static dev.nolij.chatlog.ChatLog.registerChangeListener;

@Module
public class FullBrightModule extends BaseModule {

	public static final String MODULE_ID = "fullbright";
	public static FullBrightModule INSTANCE;

	public boolean enabled = true;

	public FullBrightModule() {
		super(MODULE_ID);
		INSTANCE = this;
	}

	@Override
	public void onInitializeClient() {
		registerChangeListener(CONFIG, (configHolder, chatLogConfig) -> {
			enabled = chatLogConfig.main.fullBrightModule.enabled;
			return ActionResult.PASS;
		});
	}

}
