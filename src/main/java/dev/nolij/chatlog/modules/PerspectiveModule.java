package dev.nolij.chatlog.modules;

import me.shedaniel.clothconfig2.api.ModifierKeyCode;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.option.Perspective;
import net.minecraft.util.ActionResult;
import dev.nolij.chatlog.ChatLog;
import dev.nolij.chatlog.ChatLogConfig;
import dev.nolij.chatlog.KeyBind;

import static dev.nolij.chatlog.ChatLog.*;

@Module
public class PerspectiveModule extends BaseModule {

	public static final String MODULE_ID = "perspective";
	public static PerspectiveModule INSTANCE;

	private KeyBind keyBind;

	public boolean enabled;
	private boolean held = false;
	private Perspective actualPerspective = null;
	private final Perspective ACTIVE_PERSPECTIVE = Perspective.THIRD_PERSON_BACK;

	public PerspectiveModule() {
		super(MODULE_ID);
		INSTANCE = this;
	}

	@Override
	public void onInitializeClient() {
		keyBind = new KeyBind(ChatLog.CONFIG.get().main.perspectiveModule.keyBind);
		registerChangeListener(CONFIG, (configHolder, chatLogConfig) -> {
			keyBind.setBoundKey(
				chatLogConfig.main.general.enabled
				? chatLogConfig.main.perspectiveModule.keyBind
				: ModifierKeyCode.unknown());
			return ActionResult.PASS;
		});

		ClientTickEvents.START_CLIENT_TICK.register(e -> {
			if (CLIENT != null && CLIENT.player != null) {
				if (ChatLog.CONFIG.get().main.perspectiveModule.mode == ChatLogConfig.KeyBindMode.HOLD) {
					if (!enabled && keyBind.isPressed()) actualPerspective = CLIENT.options.getPerspective();
					if (cameraLock.isLocked() == enabled && (enabled = keyBind.isPressed()) && !held) {
						cameraYaw = CLIENT.player.getYaw(getTickDelta());
						cameraPitch = CLIENT.player.getPitch(getTickDelta());
						cameraLock.obtain();
						held = true;
						CLIENT.options.setPerspective(ACTIVE_PERSPECTIVE);
					}
				} else if (ChatLog.CONFIG.get().main.perspectiveModule.mode == ChatLogConfig.KeyBindMode.TOGGLE) {
					if (keyBind.wasPressed()) {
						if (enabled || !cameraLock.isLocked()) {
							if (!enabled) {
								cameraYaw = CLIENT.player.getYaw(getTickDelta());
								cameraPitch = CLIENT.player.getPitch(getTickDelta());
								cameraLock.obtain();
								actualPerspective = CLIENT.options.getPerspective();
							}

							enabled = !enabled;

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
