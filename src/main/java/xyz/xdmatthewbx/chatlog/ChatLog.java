package xyz.xdmatthewbx.chatlog;

import com.google.gson.GsonBuilder;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.Vec3;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	public static Minecraft CLIENT;

	public static ChatLog INSTANCE;

	public static ConfigHolder<ChatLogConfig> CONFIG;
//	public static Registry<KeyBinding> KEYBIND_REGISTRY = new SimpleRegistry<>(RegistryKey.ofRegistry(new Identifier(MOD_ID, "keybinds")), Lifecycle.stable());

	public static KeyBind configKeyBind;

	public static Lock cameraLock = new Lock();

	public static Vec3 prevCameraPos;
	public static Vec3 cameraPos;
	public static float cameraPitch;
	public static float cameraYaw;

	public static Lock movementLock = new Lock();

	@SuppressWarnings("unchecked")
	private static Class<? extends BaseModule> castToModuleType(Class<?> moduleType) throws ClassCastException {
		return (Class<? extends BaseModule>) moduleType;
	}

	static {
		Reflections reflections = new Reflections(BaseModule.class.getPackageName());
		for (var MODULE_TYPE : reflections.getTypesAnnotatedWith(Module.class)) {
			if (BaseModule.class.isAssignableFrom(MODULE_TYPE)) {
				LOGGER.info("Found module {}", MODULE_TYPE.getTypeName());
				MODULE_TYPES.add(castToModuleType(MODULE_TYPE));
			} else {
				LOGGER.error("Class {} is marked @Module but does not extend {}.", MODULE_TYPE.getTypeName(), BaseModule.class.getTypeName());
			}
		}
	}

	public ChatLog() {
		INSTANCE = this;
		CLIENT = Minecraft.getInstance();

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
	public void onInitializeClient() {
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
			return InteractionResult.PASS;
		});

		ClientTickEvents.START_CLIENT_TICK.register(e -> {
			if (configKeyBind.wasPressed()) {
				CLIENT.setScreen(AutoConfig.getConfigScreen(ChatLogConfig.class, CLIENT.screen).get());
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
		InteractionResult onChange(ConfigHolder<T> configHolder, T config);
	}

	public static <T extends ConfigData> void registerChangeListener(ConfigHolder<T> configHolder, ConfigChangeListener<T> listener) {
		configHolder.registerSaveListener(listener::onChange);
		configHolder.registerLoadListener(listener::onChange);
	}

	public static float getTickDelta() {
		return CLIENT.isPaused() ? CLIENT.pausePartialTick : CLIENT.getFrameTime();
	}

}
