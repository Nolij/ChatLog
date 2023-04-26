package xyz.xdmatthewbx.chatlog.modules;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.world.InteractionResult;
import xyz.xdmatthewbx.chatlog.ChatLog;
import xyz.xdmatthewbx.chatlog.ChatLogConfig;
import xyz.xdmatthewbx.chatlog.KeyBind;

import static xyz.xdmatthewbx.chatlog.ChatLog.*;

@Module
public class AutoClickerModule extends BaseModule {

	public static final String MODULE_ID = "autoclicker";
	public static AutoClickerModule INSTANCE;

	private KeyBind keyBind;
	public boolean enabled;

	private long pressDelay;
	private long releaseDelay;
	private long lastPress;
	private long nextRelease;
	private boolean attacking = false;

	private double maxJitter;

	public AutoClickerModule() {
		super(MODULE_ID);
		INSTANCE = this;
	}

	@Override
	public void onInitializeClient() {
		keyBind = new KeyBind(CONFIG.get().main.autoClickerModule.keyBind);
		registerChangeListener(CONFIG, (configHolder, chatLogConfig) -> {
			keyBind.setBoundKey(chatLogConfig.main.autoClickerModule.keyBind);
			pressDelay = Math.round((1 / chatLogConfig.main.autoClickerModule.cps) * 1000D);
			releaseDelay = Math.round((50D / 7D) * (7 + (7 - chatLogConfig.main.autoClickerModule.cps)));
			maxJitter = chatLogConfig.main.autoClickerModule.maxJitter;
			return InteractionResult.PASS;
		});

		WorldRenderEvents.START.register(context -> {
			if (CLIENT != null && CLIENT.player != null) {
				if (ChatLog.CONFIG.get().main.autoClickerModule.mode == ChatLogConfig.KeyBindMode.HOLD) {
					enabled = keyBind.isPressed();
				} else if (ChatLog.CONFIG.get().main.autoClickerModule.mode == ChatLogConfig.KeyBindMode.TOGGLE) {
					if (keyBind.wasPressed()) {
						enabled = !enabled;
					}
				}

				var jitter = getJitter(maxJitter);

				if (attacking && System.currentTimeMillis() >= nextRelease) {
					attacking = false;
					CLIENT.options.keyAttack.setDown(false);
				}
				if (enabled && !attacking) {
					if ((System.currentTimeMillis() - lastPress) > (pressDelay + jitter)) {
						attacking = true;
						CLIENT.options.keyAttack.clickCount++;
						CLIENT.options.keyAttack.setDown(true);
						lastPress = System.currentTimeMillis();
						nextRelease = lastPress + releaseDelay + getJitter(maxJitter / 2D);
					}
				}
			}
		});
	}

	private long getJitter(double jitter) {
		return Math.round(Math.random() * jitter - (jitter / 2D));
	}
}
