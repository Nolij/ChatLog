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
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.util.math.Vec3d;
import xyz.xdmatthewbx.chatlog.modules.*;
import xyz.xdmatthewbx.chatlog.modules.Module;
import xyz.xdmatthewbx.chatlog.util.Lock;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class ChatLog implements ClientModInitializer {

	public static final String MOD_ID = "chatlog";
	public static final Logger LOGGER = LoggerFactory.getLogger("ChatLog");

	private static final Set<Class<? extends BaseModule>> MODULE_TYPES = new HashSet<>();
	private final Set<BaseModule> MODULES;


	public static MinecraftClient CLIENT;

	public static ChatLog INSTANCE;

	public static ConfigHolder<ChatLogConfig> CONFIG;
//	public static Registry<KeyBinding> KEYBIND_REGISTRY = new SimpleRegistry<>(RegistryKey.ofRegistry(new Identifier(MOD_ID, "keybinds")), Lifecycle.stable());

	public static KeyBind configKeyBind;

	public static Lock cameraLock = new Lock();

	public static Vec3d prevCameraPos;
	public static Vec3d cameraPos;
	public static float cameraPitch;
	public static float cameraYaw;

	public static Lock movementLock = new Lock();

	static {
		Reflections reflections = new Reflections(BaseModule.class.getPackageName());
		for (var MODULE_TYPE : reflections.getTypesAnnotatedWith(Module.class)) {
			if (BaseModule.class.isAssignableFrom(MODULE_TYPE)) {
				LOGGER.info("Found module {}", MODULE_TYPE.getTypeName());
				//noinspection unchecked
				MODULE_TYPES.add((Class<? extends BaseModule>) MODULE_TYPE);
			} else {
				LOGGER.error("Class {} is marked @Module but does not extend {}.", MODULE_TYPE.getTypeName(), BaseModule.class.getTypeName());
			}
		}
	}

	public ChatLog() {
		INSTANCE = this;
		CLIENT = MinecraftClient.getInstance();

		MODULES = new LinkedHashSet<>();

		for (var type : MODULE_TYPES) {
			try {
				MODULES.add(type.getConstructor().newInstance());
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
				LOGGER.error("Module Initialization failed for {}.", type.getTypeName());
			}
		}
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
			prevCameraPos = cameraPos;
		});

		for (var module : MODULES) {
			module.onInitializeClient();
		}

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
