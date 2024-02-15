package dev.nolij.chatlog.modules;

import dev.nolij.chatlog.util.Lock;
import me.shedaniel.clothconfig2.api.ModifierKeyCode;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.option.Perspective;
import net.minecraft.util.ActionResult;
import dev.nolij.chatlog.ChatLog;
import dev.nolij.chatlog.KeyBind;

import static dev.nolij.chatlog.ChatLog.*;

@Module
public class PerspectiveModule extends BaseModule {

	public static final String MODULE_ID = "perspective";
	public static PerspectiveModule INSTANCE;
	
	private static final Perspective ACTIVE_PERSPECTIVE = Perspective.THIRD_PERSON_BACK;
	
	private KeyBind keyBind;
	private Lock.Lease lease = new Lock.NullLease();
	private boolean held = false;

	public PerspectiveModule() {
		super(MODULE_ID);
		INSTANCE = this;
	}
	
	private void enable() throws Lock.LeaseFailedException {
		assert CLIENT.player != null;
		
		lease = Lock.obtain(cameraLock, perspectiveLock);
        cameraYaw = CLIENT.player.getYaw(getTickDelta());
		cameraPitch = CLIENT.player.getPitch(getTickDelta());
		perspective = ACTIVE_PERSPECTIVE;
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
            if (CLIENT == null || CLIENT.player == null)
                return;
            
            switch (ChatLog.CONFIG.get().main.perspectiveModule.mode) {
                case HOLD -> {
                    if (keyBind.isPressed() && !held) {
                        try {
                            enable();
                        } catch (Lock.LeaseFailedException ignored) {
                            return;
                        }
                        held = true;
                    }
                }
                case TOGGLE -> {
                    if (keyBind.wasPressed()) {
                        if (lease.isValid()) {
                            try {
                                lease.release();
                            } catch (Lock.LeaseInvalidException ex) {
                                throw new AssertionError(ex);
                            }
                        } else {
                            try {
                                enable();
                            } catch (Lock.LeaseFailedException ignored) {
                                return;
                            }
                        }
                    }
                }
            }
            
            if (!keyBind.isPressed() && held) {
                held = false;
                try {
                    lease.release();
                } catch (Lock.LeaseInvalidException ignored) {}
            }
        });
	}

}
