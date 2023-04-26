package xyz.xdmatthewbx.chatlog.modules;

import static xyz.xdmatthewbx.chatlog.ChatLog.CONFIG;
import static xyz.xdmatthewbx.chatlog.ChatLog.registerChangeListener;

import net.minecraft.world.InteractionResult;

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
			enabled = chatLogConfig.main.inputUnlockModule.enabled;
			return InteractionResult.PASS;
		});
	}

}
