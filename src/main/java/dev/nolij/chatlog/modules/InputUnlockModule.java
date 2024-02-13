package dev.nolij.chatlog.modules;

import net.minecraft.util.ActionResult;

import static dev.nolij.chatlog.ChatLog.CONFIG;
import static dev.nolij.chatlog.ChatLog.registerChangeListener;

@Module
public class InputUnlockModule extends BaseModule {

	public static final String MODULE_ID = "input_unlock";
	public static InputUnlockModule INSTANCE;

	public boolean enabled = true;

	public InputUnlockModule() {
		super(MODULE_ID);
		INSTANCE = this;
	}

	@Override
	public void onInitializeClient() {
		registerChangeListener(CONFIG, (configHolder, chatLogConfig) -> {
			enabled = chatLogConfig.main.inputUnlockModule.enabled && chatLogConfig.main.general.enabled;
			return ActionResult.PASS;
		});
	}

}
