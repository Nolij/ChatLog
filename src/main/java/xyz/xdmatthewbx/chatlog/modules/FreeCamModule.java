package xyz.xdmatthewbx.chatlog.modules;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.Entity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;
import xyz.xdmatthewbx.chatlog.ChatLog;
import xyz.xdmatthewbx.chatlog.KeyBind;

import static xyz.xdmatthewbx.chatlog.ChatLog.*;

@Module
public class FreeCamModule extends BaseModule {

	public static final String MODULE_ID = "freecam";
	public static FreeCamModule INSTANCE;

	private KeyBind keyBind;

	public boolean enabled = false;
	private Perspective actualPerspective = null;
	private final Perspective ACTIVE_PERSPECTIVE = Perspective.FIRST_PERSON;

	private Vec3d cameraVelocity = Vec3d.ZERO;

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

		ClientTickEvents.START_CLIENT_TICK.register(client -> {
			if (CLIENT.player != null) {
				if (keyBind.wasPressed() && cameraLock.isLocked() == enabled && movementLock.isLocked() == enabled) {
					enabled = !enabled;

					if (enabled) {
						cameraLock.obtain();
						movementLock.obtain();
						Entity cameraEntity = CLIENT.cameraEntity;
						if (cameraEntity == null) cameraEntity = CLIENT.player;
						cameraPos = cameraEntity.getClientCameraPosVec(ChatLog.getTickDelta());
						cameraYaw = CLIENT.player.getYaw(getTickDelta());
						cameraPitch = CLIENT.player.getPitch(getTickDelta());
						cameraVelocity = Vec3d.ZERO;
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

				if (enabled) {
					float deltaX = 0F;
					float deltaY = 0F;
					float deltaZ = 0F;

					if (CLIENT.options.forwardKey.isPressed())
						deltaZ++;
					if (CLIENT.options.backKey.isPressed())
						deltaZ--;
					if (CLIENT.options.leftKey.isPressed())
						deltaX++;
					if (CLIENT.options.rightKey.isPressed())
						deltaX--;
					if (CLIENT.options.jumpKey.isPressed())
						deltaY++;
					if (CLIENT.options.sneakKey.isPressed())
						deltaY--;
					var delta = new Vec3d(deltaX, deltaY, deltaZ);

					var speed = 1F;
					if (CLIENT.options.sprintKey.isPressed())
						speed *= 2F;

					cameraVelocity = cameraVelocity
						.add(Entity.movementInputToVelocity(delta, speed, cameraYaw))
						.multiply(0.65F);

					cameraPos = cameraPos.add(cameraVelocity);
				}
			}
		});
	}

}
