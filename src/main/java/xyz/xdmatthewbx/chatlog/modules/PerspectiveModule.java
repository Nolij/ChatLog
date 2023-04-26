package xyz.xdmatthewbx.chatlog.modules;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.CameraType;
import net.minecraft.world.InteractionResult;
import xyz.xdmatthewbx.chatlog.ChatLog;
import xyz.xdmatthewbx.chatlog.ChatLogConfig;
import xyz.xdmatthewbx.chatlog.KeyBind;

import static xyz.xdmatthewbx.chatlog.ChatLog.*;

@Module
public class PerspectiveModule extends BaseModule {

	public static final String MODULE_ID = "perspective";
	public static PerspectiveModule INSTANCE;

	private KeyBind keyBind;

	public boolean enabled;
	private boolean held = false;
	private CameraType actualPerspective = null;
	private final CameraType ACTIVE_PERSPECTIVE = CameraType.THIRD_PERSON_BACK;

	public PerspectiveModule() {
		super(MODULE_ID);
		INSTANCE = this;
	}

	@Override
	public void onInitializeClient() {
		keyBind = new KeyBind(ChatLog.CONFIG.get().main.perspectiveModule.keyBind);
		registerChangeListener(CONFIG, (configHolder, chatLogConfig) -> {
			keyBind.setBoundKey(chatLogConfig.main.perspectiveModule.keyBind);
			return InteractionResult.PASS;
		});

		ClientTickEvents.START_CLIENT_TICK.register(e -> {
			if (CLIENT != null && CLIENT.player != null) {
				if (ChatLog.CONFIG.get().main.perspectiveModule.mode == ChatLogConfig.KeyBindMode.HOLD) {
					if (!enabled && keyBind.isPressed()) actualPerspective = CLIENT.options.getCameraType();
					if (cameraLock.isLocked() == enabled && (enabled = keyBind.isPressed()) && !held) {
						cameraYaw = CLIENT.player.getViewYRot(getTickDelta());
						cameraPitch = CLIENT.player.getViewXRot(getTickDelta());
						cameraLock.obtain();
						held = true;
						CLIENT.options.setCameraType(ACTIVE_PERSPECTIVE);
					}
				} else if (ChatLog.CONFIG.get().main.perspectiveModule.mode == ChatLogConfig.KeyBindMode.TOGGLE) {
					if (keyBind.wasPressed()) {
						if (enabled || !cameraLock.isLocked()) {
							if (!enabled) {
								cameraYaw = CLIENT.player.getViewYRot(getTickDelta());
								cameraPitch = CLIENT.player.getViewXRot(getTickDelta());
								cameraLock.obtain();
								actualPerspective = CLIENT.options.getCameraType();
							}

							enabled = !enabled;

							CLIENT.options.setCameraType(enabled ? ACTIVE_PERSPECTIVE : actualPerspective);

							if (!enabled) {
								cameraLock.release();
							}
						}
					}
				}

				if (!enabled && held) {
					held = false;
					CLIENT.options.setCameraType(actualPerspective);
					actualPerspective = null;
					cameraLock.release();
				}

				if (enabled && CLIENT.options.getCameraType() != ACTIVE_PERSPECTIVE) {
					enabled = false;
					cameraLock.release();
				}
			}
		});
	}

}
