package dev.nolij.chatlog.modules;

import net.minecraft.util.ActionResult;

import static dev.nolij.chatlog.ChatLog.CONFIG;
import static dev.nolij.chatlog.ChatLog.registerChangeListener;

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
			enabled = chatLogConfig.main.antiDistortionModule.enabled && chatLogConfig.main.general.enabled;
			return ActionResult.PASS;
		});
	}

}
