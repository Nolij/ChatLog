package xyz.xdmatthewbx.chatlog.modules;

import static xyz.xdmatthewbx.chatlog.ChatLog.CONFIG;
import static xyz.xdmatthewbx.chatlog.ChatLog.registerChangeListener;

import net.minecraft.world.InteractionResult;

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
			return InteractionResult.PASS;
		});
	}

}
