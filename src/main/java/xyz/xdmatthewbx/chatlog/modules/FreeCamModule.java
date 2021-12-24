package xyz.xdmatthewbx.chatlog.modules;

import net.minecraft.client.option.Perspective;
import net.minecraft.entity.Entity;
import net.minecraft.util.ActionResult;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents;
import xyz.xdmatthewbx.chatlog.ChatLog;
import xyz.xdmatthewbx.chatlog.KeyBind;

import static xyz.xdmatthewbx.chatlog.ChatLog.*;

public class FreeCamModule extends BaseModule {

	public static final String MODULE_ID = "freecam";
	public static FreeCamModule INSTANCE;

	private static KeyBind keyBind;

	public boolean enabled = false;
	private Perspective actualPerspective = null;
	private final Perspective ACTIVE_PERSPECTIVE = Perspective.THIRD_PERSON_BACK;

	public FreeCamModule() {
		super(MODULE_ID);
		INSTANCE = this;
	}

	@Override
	public void onInitializeClient() {
		keyBind = new KeyBind(CONFIG.get().main.freeCamModule.keyBind);
		registerChangeListener(CONFIG, (configHolder, chatLogConfig) -> {
			keyBind.setBoundKey(chatLogConfig.main.freeCamModule.keyBind);
			return ActionResult.PASS;
		});

		ClientTickEvents.START.register(client -> {
			if (CLIENT.player != null) {
				if (keyBind.wasPressed() && cameraLock.isLocked() == enabled && movementLock.isLocked() == enabled) {
					enabled = !enabled;

					if (enabled) {
						cameraLock.obtain();
						movementLock.obtain();
						Entity cameraEntity = CLIENT.cameraEntity;
						if (cameraEntity == null) cameraEntity = CLIENT.player;
						cameraPos = cameraEntity.getClientCameraPosVec(ChatLog.getTickDelta());
						actualPerspective = CLIENT.options.getPerspective();
						CLIENT.options.setPerspective(ACTIVE_PERSPECTIVE);
					} else {
						CLIENT.options.setPerspective(actualPerspective);
						actualPerspective = null;
						cameraPos = null;
						cameraLock.release();
						movementLock.release();
					}
				}
			}
		});
	}

}
