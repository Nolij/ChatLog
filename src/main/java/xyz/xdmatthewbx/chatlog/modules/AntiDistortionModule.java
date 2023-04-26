package xyz.xdmatthewbx.chatlog.modules;

import static xyz.xdmatthewbx.chatlog.ChatLog.CONFIG;
import static xyz.xdmatthewbx.chatlog.ChatLog.registerChangeListener;

import net.minecraft.world.InteractionResult;

@Module
public class AntiDistortionModule extends BaseModule {

	public static final String MODULE_ID = "anti_distortion";
	public static AntiDistortionModule INSTANCE;

	public boolean enabled = true;

	public AntiDistortionModule() {
		super(MODULE_ID);
		INSTANCE = this;
	}

	@Override
	public void onInitializeClient() {
		registerChangeListener(CONFIG, (configHolder, chatLogConfig) -> {
			enabled = chatLogConfig.main.antiDistortionModule.enabled;
			return InteractionResult.PASS;
		});
	}

}
