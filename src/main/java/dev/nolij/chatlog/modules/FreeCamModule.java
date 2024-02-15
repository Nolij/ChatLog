package dev.nolij.chatlog.modules;

import dev.nolij.chatlog.ChatLogConfig;
import dev.nolij.chatlog.util.Lock;
import me.shedaniel.clothconfig2.api.ModifierKeyCode;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.Entity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import dev.nolij.chatlog.ChatLog;
import dev.nolij.chatlog.KeyBind;
import org.jetbrains.annotations.NotNull;

import static dev.nolij.chatlog.ChatLog.*;

@Module
public class FreeCamModule extends BaseModule {

	public static final String MODULE_ID = "freecam";
	public static FreeCamModule INSTANCE;
	
	private static final Perspective ACTIVE_PERSPECTIVE = Perspective.FIRST_PERSON;

	private KeyBind keyBind;

	public boolean enabled = false;
	
	private Lock.Lease lease = new Lock.NullLease();
	private Lock.Lease scrollLease = new Lock.NullLease();

	private boolean wasSprinting = false;
	private double acceleration = 1D;
	private Vec3d velocity = Vec3d.ZERO;

	public FreeCamModule() {
		super(MODULE_ID);
		INSTANCE = this;
	}

	@Override
	public void onInitializeClient() {
		keyBind = new KeyBind(CONFIG.get().main.freeCamModule.keyBind);
		registerChangeListener(CONFIG, (configHolder, chatLogConfig) -> {
			keyBind.setBoundKey(
				chatLogConfig.main.general.enabled
				? chatLogConfig.main.freeCamModule.keyBind
				: ModifierKeyCode.unknown());
			return ActionResult.PASS;
		});

		ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (CLIENT.player == null)
                return;
			
            if (keyBind.wasPressed()) {
				if (enabled) {
                    try {
                        lease.release();
						if (scrollLease.isValid())
							scrollLease.release();
                    } catch (Lock.LeaseInvalidException e) {
                        throw new AssertionError(e);
                    }
				} else {
					try {
						lease = Lock.obtain(cameraLock, movementLock, interactionLock, perspectiveLock);
						scrollDelta = 0D;
					} catch (Lock.LeaseFailedException ignored) {
						return;
					}
				}
				
                enabled = lease.isValid();
				
                if (enabled) {
                    Entity cameraEntity = CLIENT.cameraEntity;
                    if (cameraEntity == null)
                        cameraEntity = CLIENT.player;
                    cameraPos = cameraEntity.getClientCameraPosVec(ChatLog.getTickDelta());
                    cameraYaw = CLIENT.player.getYaw(getTickDelta());
                    cameraPitch = CLIENT.player.getPitch(getTickDelta());
	                acceleration = 1D;
	                velocity = Vec3d.ZERO;
					perspective = ACTIVE_PERSPECTIVE;
                } else {
                    cameraPos = null;
					perspective = null;
                }
            }
            
            if (enabled) {
	            tickVelocity();
	            
	            cameraPos = cameraPos.add(velocity);
            }
        });
	}
	
	private void tickAcceleration() {
		final boolean isSprinting = CLIENT.options.sprintKey.isPressed();
		
		if (isSprinting) {
			final ChatLogConfig.FreeCamConfig config = CONFIG.get().main.freeCamModule;
			
			if (!wasSprinting) {
				if (!scrollLease.isValid()) {
                    try {
                        scrollLease = scrollLock.obtainLease();
                    } catch (Lock.LeaseFailedException ignored) {}
                }
				acceleration = config.accelerationMin;
			}
			
			if (scrollLease.isValid()) {
				acceleration += scrollDelta * Math.abs(acceleration * config.accelerationSpeed);
				acceleration = MathHelper.clamp(acceleration, config.accelerationMin, config.accelerationMax);
			}
		} else {
			if (wasSprinting && scrollLease.isValid()) {
                try {
                    scrollLease.release();
                } catch (Lock.LeaseInvalidException e) {
                    throw new AssertionError(e);
                }
            }
			acceleration = 1D;
		}
		
		scrollDelta = 0D;
		wasSprinting = isSprinting;
	}
	
	@NotNull
	private Vec3d calculateDelta() {
		var deltaX = 0F;
		var deltaY = 0F;
		var deltaZ = 0F;
		
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
		
		return new Vec3d(deltaX, deltaY, deltaZ);
	}
	
	private void tickVelocity() {
		tickAcceleration();
		
		final Vec3d delta = calculateDelta();
		velocity = velocity
			.add(Entity.movementInputToVelocity(delta, (float) acceleration, cameraYaw))
			.multiply(0.65F);
	}
	
}
