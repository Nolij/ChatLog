package xyz.xdmatthewbx.chatlog;

import com.google.gson.GsonBuilder;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.ActionResult;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.xdmatthewbx.chatlog.modules.*;

public class ChatLog implements ClientModInitializer {

	public static final String MOD_ID = "chatlog";
	public static final Logger LOGGER = LoggerFactory.getLogger("ChatLog");

	public static ToolTipInfoModule TOOL_TIP_INFO_MODULE;
	public static PerspectiveModule PERSPECTIVE_MODULE;
	public static AntiBlindModule ANTI_BLIND_MODULE;
	public static AntiFogModule ANTI_FOG_MODULE;
	public static AntiDistortionModule ANTI_DISTORTION_MODULE;
	public static AntiOverlayModule ANTI_OVERLAY_MODULE;
	public static ESPModule ESP_MODULE;
	public static InputUnlockModule INPUT_UNLOCK_MODULE;

	public static MinecraftClient CLIENT;

	public static ChatLog INSTANCE;

	public static ConfigHolder<ChatLogConfig> CONFIG;
//	public static Registry<KeyBinding> KEYBIND_REGISTRY = new SimpleRegistry<>(RegistryKey.ofRegistry(new Identifier(MOD_ID, "keybinds")), Lifecycle.stable());

	public static KeyBind configKeyBind;

	public ChatLog() {
		INSTANCE = this;
		CLIENT = MinecraftClient.getInstance();

		TOOL_TIP_INFO_MODULE = new ToolTipInfoModule();
		PERSPECTIVE_MODULE = new PerspectiveModule();
		ANTI_BLIND_MODULE = new AntiBlindModule();
		ANTI_FOG_MODULE = new AntiFogModule();
		ANTI_DISTORTION_MODULE = new AntiDistortionModule();
		ANTI_OVERLAY_MODULE = new AntiOverlayModule();
		ESP_MODULE = new ESPModule();
		INPUT_UNLOCK_MODULE = new InputUnlockModule();
	}

	@Override
	public void onInitializeClient(ModContainer mod) {
		CONFIG = AutoConfig.register(ChatLogConfig.class, (definition, configClass) ->
			new GsonConfigSerializer<>(definition, configClass, ChatLogConfig.buildGson(new GsonBuilder())));

//		Registry.register(KEYBIND_REGISTRY, new Identifier(MOD_ID, "base_open_config"), openConfigKey = new KeyBinding("", InputUtil.Type.KEYSYM, ChatLog.CONFIG.main.openConfigKey.getKeyCode().getCode(), ""));
//		CONFIG_HOLDER.registerSaveListener((configHolder, chatLogConfig) -> {
//			openConfigKey.setBoundKey(chatLogConfig.main.openConfigKey.getKeyCode());
//			return ActionResult.PASS;
//		});
		configKeyBind = new KeyBind(ChatLog.CONFIG.get().main.general.configKeyBind);
		registerChangeListener(CONFIG, (configHolder, chatLogConfig) -> {
			configKeyBind.setBoundKey(chatLogConfig.main.general.configKeyBind);
			KeyBind.resetAll();
			return ActionResult.PASS;
		});

		ClientTickEvents.START.register(e -> {
			if (configKeyBind.wasPressed()) {
				CLIENT.setScreen(AutoConfig.getConfigScreen(ChatLogConfig.class, CLIENT.currentScreen).get());
			}
		});

		TOOL_TIP_INFO_MODULE.onInitializeClient();
		PERSPECTIVE_MODULE.onInitializeClient();
		ANTI_BLIND_MODULE.onInitializeClient();
		ANTI_FOG_MODULE.onInitializeClient();
		ANTI_DISTORTION_MODULE.onInitializeClient();
		ANTI_OVERLAY_MODULE.onInitializeClient();
		ESP_MODULE.onInitializeClient();
		INPUT_UNLOCK_MODULE.onInitializeClient();

		CONFIG.load();
	}

	@FunctionalInterface
	public interface ConfigChangeListener<T extends ConfigData> {
		ActionResult onChange(ConfigHolder<T> configHolder, T config);
	}

	public static <T extends ConfigData> void registerChangeListener(ConfigHolder<T> configHolder, ConfigChangeListener<T> listener) {
		configHolder.registerSaveListener(listener::onChange);
		configHolder.registerLoadListener(listener::onChange);
	}

	public static float getTickDelta() {
		return CLIENT.isPaused() ? CLIENT.pausedTickDelta : CLIENT.getTickDelta();
	}

}
