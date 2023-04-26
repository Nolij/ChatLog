package xyz.xdmatthewbx.chatlog.modules;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.CameraType;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import xyz.xdmatthewbx.chatlog.ChatLog;
import xyz.xdmatthewbx.chatlog.KeyBind;

import static xyz.xdmatthewbx.chatlog.ChatLog.*;

@Module
public class FreeCamModule extends BaseModule {

	public static final String MODULE_ID = "freecam";
	public static FreeCamModule INSTANCE;

	private KeyBind keyBind;

	public boolean enabled = false;
	private CameraType actualPerspective = null;
	private final CameraType ACTIVE_PERSPECTIVE = CameraType.FIRST_PERSON;

	private Vec3 cameraVelocity = Vec3.ZERO;

	public FreeCamModule() {
		super(MODULE_ID);
		INSTANCE = this;
	}

	@Override
	public void onInitializeClient() {
		keyBind = new KeyBind(CONFIG.get().main.freeCamModule.keyBind);
		registerChangeListener(CONFIG, (configHolder, chatLogConfig) -> {
			keyBind.setBoundKey(chatLogConfig.main.freeCamModule.keyBind);
			return InteractionResult.PASS;
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
						cameraPos = cameraEntity.getLightProbePosition(ChatLog.getTickDelta());
						cameraYaw = CLIENT.player.getViewYRot(getTickDelta());
						cameraPitch = CLIENT.player.getViewXRot(getTickDelta());
						cameraVelocity = Vec3.ZERO;
						actualPerspective = CLIENT.options.getCameraType();
						CLIENT.options.setCameraType(ACTIVE_PERSPECTIVE);
					} else {
						CLIENT.options.setCameraType(actualPerspective);
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

					if (CLIENT.options.keyUp.isDown())
						deltaZ++;
					if (CLIENT.options.keyDown.isDown())
						deltaZ--;
					if (CLIENT.options.keyLeft.isDown())
						deltaX++;
					if (CLIENT.options.keyRight.isDown())
						deltaX--;
					if (CLIENT.options.keyJump.isDown())
						deltaY++;
					if (CLIENT.options.keyShift.isDown())
						deltaY--;
					var delta = new Vec3(deltaX, deltaY, deltaZ);

					var speed = 1F;
					if (CLIENT.options.keySprint.isDown())
						speed *= 2F;

					cameraVelocity = cameraVelocity
						.add(Entity.getInputVector(delta, speed, cameraYaw))
						.scale(0.65F);

					cameraPos = cameraPos.add(cameraVelocity);
				}
			}
		});
	}

}
