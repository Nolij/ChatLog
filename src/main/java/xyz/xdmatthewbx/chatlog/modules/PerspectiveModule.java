package xyz.xdmatthewbx.chatlog.modules;

import net.minecraft.client.option.Perspective;
import net.minecraft.util.ActionResult;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents;
import xyz.xdmatthewbx.chatlog.ChatLog;
import xyz.xdmatthewbx.chatlog.ChatLogConfig;
import xyz.xdmatthewbx.chatlog.KeyBind;

import static xyz.xdmatthewbx.chatlog.ChatLog.*;

public class PerspectiveModule extends BaseModule {

	public static final String MODULE_ID = "perspective";
	public static PerspectiveModule INSTANCE;

	private static KeyBind keyBind;

	public boolean enabled;
	public float pitch;
	public float yaw;
	private boolean held = false;
	private Perspective actualPerspective = null;
	private final Perspective ACTIVE_PERSPECTIVE = Perspective.THIRD_PERSON_BACK;

	public PerspectiveModule() {
		super(MODULE_ID);
		INSTANCE = this;
	}

	@Override
	public void onInitializeClient() {
//		Registry.register(ChatLog.KEYBIND_REGISTRY, new Identifier(ChatLog.MOD_ID, MODULE_ID + "_toggle"), toggleKey = new KeyBinding("", InputUtil.Type.KEYSYM, ChatLog.CONFIG.main.perspectiveToggleKey.getKeyCode().getCode(), ""));
//		ChatLog.CONFIG_HOLDER.registerSaveListener((configHolder, chatLogConfig) -> {
//			toggleKey.setBoundKey(chatLogConfig.main.perspectiveToggleKey.getKeyCode());
//			return ActionResult.PASS;
//		});
		keyBind = new KeyBind(ChatLog.CONFIG.get().main.perspectiveModule.keyBind);
		registerChangeListener(CONFIG, (configHolder, chatLogConfig) -> {
			keyBind.setBoundKey(chatLogConfig.main.perspectiveModule.keyBind);
			return ActionResult.PASS;
		});

		ClientTickEvents.START.register(e -> {
			if (ChatLog.CLIENT != null && ChatLog.CLIENT.player != null) {
				if (ChatLog.CONFIG.get().main.perspectiveModule.mode == ChatLogConfig.PerspectiveMode.HOLD) {
					if (!enabled && keyBind.isPressed()) actualPerspective = CLIENT.options.getPerspective();
					if (cameraLock.isLocked() == enabled && (enabled = keyBind.isPressed()) && !held) {
						cameraLock.obtain();
						held = true;
						pitch = ChatLog.CLIENT.player.getPitch();
						yaw = ChatLog.CLIENT.player.getYaw();
						CLIENT.options.setPerspective(ACTIVE_PERSPECTIVE);
					}
				} else if (ChatLog.CONFIG.get().main.perspectiveModule.mode == ChatLogConfig.PerspectiveMode.TOGGLE) {
					if (keyBind.wasPressed()) {
						if (enabled || !cameraLock.isLocked()) {
							if (!enabled) {
								cameraLock.obtain();
								actualPerspective = CLIENT.options.getPerspective();
							}

							enabled = !enabled;

							pitch = ChatLog.CLIENT.player.getPitch();
							yaw = ChatLog.CLIENT.player.getYaw();

							CLIENT.options.setPerspective(enabled ? ACTIVE_PERSPECTIVE : actualPerspective);

							if (!enabled) {
								cameraLock.release();
							}
						}
					}
				}

				if (!enabled && held) {
					held = false;
					CLIENT.options.setPerspective(actualPerspective);
					actualPerspective = null;
					cameraLock.release();
				}

				if (enabled && CLIENT.options.getPerspective() != ACTIVE_PERSPECTIVE) {
					enabled = false;
					cameraLock.release();
				}
			}
		});
	}

}
