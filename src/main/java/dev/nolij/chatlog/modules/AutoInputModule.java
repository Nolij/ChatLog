package dev.nolij.chatlog.modules;

import com.google.common.base.Suppliers;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.ActionResult;
import org.lwjgl.glfw.GLFW;
import dev.nolij.chatlog.ChatLogConfig;
import dev.nolij.chatlog.KeyBind;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static dev.nolij.chatlog.ChatLog.*;

@Module
public class AutoInputModule extends BaseModule {

	public static final String MODULE_ID = "autoinput";
	public static AutoInputModule INSTANCE;

	private static class AutoInputEntry {
		public boolean active;
		public boolean pressing;
		
		public KeyBind keyBind = new KeyBind(InputUtil.UNKNOWN_KEY);
		public ChatLogConfig.KeyBindMode mode;
		public Supplier<List<KeyBinding>> targets;
		
		public long pressDelay;
		public long releaseDelay;
		public long lastPress;
		public long nextRelease;
		public int maxJitter;
	}
	
	private final ArrayList<AutoInputEntry> entries = new ArrayList<>();

	public AutoInputModule() {
		super(MODULE_ID);
		INSTANCE = this;
	}

	private static final InputUtil.Key MIGRATED_KEY = InputUtil.fromKeyCode(-1337, -1337);
	
	@Override
	public void onInitializeClient() {
		registerChangeListener(CONFIG, (configHolder, chatLogConfig) -> {
			if (chatLogConfig.main.autoClickerModule.keyBind.getCode() != -1337) {
				var keyBind = chatLogConfig.main.autoClickerModule.keyBind;
				var mode = chatLogConfig.main.autoClickerModule.mode;
				var cps = chatLogConfig.main.autoClickerModule.cps;
				var maxJitter = chatLogConfig.main.autoClickerModule.maxJitter;
				chatLogConfig.main.autoClickerModule.keyBind = MIGRATED_KEY;
				
				var entry = new ChatLogConfig.AutoInputEntry();
				entry.targetKey = InputUtil.Type.MOUSE.createFromCode(GLFW.GLFW_MOUSE_BUTTON_LEFT);
				entry.keyBind = keyBind;
				entry.mode = mode;
				entry.cps = cps;
				entry.maxJitter = (int) maxJitter;
				chatLogConfig.main.autoInputModule.entries.add(entry);
			}
			
			entries.clear();
			if (chatLogConfig.main.general.enabled) {
				entries.ensureCapacity(chatLogConfig.main.autoInputModule.entries.size());
				for (var configEntry : chatLogConfig.main.autoInputModule.entries) {
					if (configEntry.keyBind == null)
						continue;
					
					var entry = new AutoInputEntry();
					entry.keyBind.setBoundKey(configEntry.keyBind);
					entry.mode = configEntry.mode;
					entry.pressDelay = Math.round((1 / configEntry.cps) * 1000D);
					entry.releaseDelay = (int) configEntry.releaseDelay;
					entry.maxJitter = (int) configEntry.maxJitter;
					entry.targets = Suppliers.memoize(() -> {
						var targets = new ArrayList<KeyBinding>();
						for (var keyBinding : CLIENT.options.allKeys) {
							if (!keyBinding.isUnbound()) {
								if (keyBinding.boundKey.equals(configEntry.targetKey)) {
									targets.add(keyBinding);
								}
							}
						}
						targets.trimToSize();
						return targets;
					});
					entries.add(entry);
				}
			}
			
			return ActionResult.PASS;
		});

		WorldRenderEvents.START.register(context -> {
            if (CLIENT == null || CLIENT.player == null)
                return;
			
            for (var entry : entries) {
                if (entry.mode == ChatLogConfig.KeyBindMode.HOLD) {
                    entry.active = entry.keyBind.isPressed();
                } else if (entry.mode == ChatLogConfig.KeyBindMode.TOGGLE) {
                    if (entry.keyBind.wasPressed()) {
                        entry.active = !entry.active;
                    }
                }
                
                if (entry.pressing && System.currentTimeMillis() >= entry.nextRelease) {
                    entry.pressing = false;
                    for (var target : entry.targets.get()) {
                        target.setPressed(false);
                    }
                }
                if (entry.active && !entry.pressing) {
                    if ((System.currentTimeMillis() - entry.lastPress) > (entry.pressDelay + getJitter(entry.maxJitter))) {
                        entry.pressing = true;
                        for (var target : entry.targets.get()) {
                            target.timesPressed++;
                            target.setPressed(true);
                        }
                        entry.lastPress = System.currentTimeMillis();
                        entry.nextRelease = entry.lastPress + entry.releaseDelay + getJitter(entry.maxJitter / 2D);
                    }
                }
            }
        });
	}

	private long getJitter(double jitter) {
		return Math.round(Math.random() * jitter - (jitter / 2D));
	}
}
