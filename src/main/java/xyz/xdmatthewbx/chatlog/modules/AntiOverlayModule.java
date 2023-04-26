package xyz.xdmatthewbx.chatlog.modules;

import static xyz.xdmatthewbx.chatlog.ChatLog.CONFIG;
import static xyz.xdmatthewbx.chatlog.ChatLog.registerChangeListener;

import net.minecraft.world.InteractionResult;

@Module
public class AntiOverlayModule extends BaseModule {

	public static final String MODULE_ID = "anti_overlay";
	public static AntiOverlayModule INSTANCE;

	public boolean enabled = true;

	public AntiOverlayModule() {
		super(MODULE_ID);
		INSTANCE = this;
	}

	@Override
	public void onInitializeClient() {
		registerChangeListener(CONFIG, (configHolder, chatLogConfig) -> {
			enabled = chatLogConfig.main.antiOverlayModule.enabled;
			return InteractionResult.PASS;
		});
	}

}
