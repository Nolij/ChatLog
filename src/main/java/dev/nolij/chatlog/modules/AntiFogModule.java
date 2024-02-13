package dev.nolij.chatlog.modules;

import net.minecraft.util.ActionResult;

import static dev.nolij.chatlog.ChatLog.CONFIG;
import static dev.nolij.chatlog.ChatLog.registerChangeListener;

@Module
public class AntiFogModule extends BaseModule {

	public static final String MODULE_ID = "antifog";
	public static AntiFogModule INSTANCE;

	public boolean enabled = true;

	public AntiFogModule() {
		super(MODULE_ID);
		INSTANCE = this;
	}

	@Override
	public void onInitializeClient() {
		registerChangeListener(CONFIG, (configHolder, chatLogConfig) -> {
			enabled = chatLogConfig.main.antiFogModule.enabled && chatLogConfig.main.general.enabled;
			return ActionResult.PASS;
		});
	}

}
