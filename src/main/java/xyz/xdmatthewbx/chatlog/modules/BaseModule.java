package xyz.xdmatthewbx.chatlog.modules;

import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.xdmatthewbx.chatlog.ChatLog;

public abstract class BaseModule {

	public final String MODULE_ID;
	protected final Logger LOGGER;
	protected final Minecraft CLIENT;
	protected final ChatLog MOD;

	public BaseModule(String moduleId) {
		if (!this.getClass().isAnnotationPresent(Module.class)) {
			throw new IllegalStateException();
		}
		MODULE_ID = moduleId;
		LOGGER = LoggerFactory.getLogger(ChatLog.MOD_ID + "." + MODULE_ID);
		CLIENT = Minecraft.getInstance();
		MOD = ChatLog.INSTANCE;
	}

	public abstract void onInitializeClient();

}
