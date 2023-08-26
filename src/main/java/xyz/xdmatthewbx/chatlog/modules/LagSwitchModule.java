package xyz.xdmatthewbx.chatlog.modules;

import me.shedaniel.clothconfig2.api.ModifierKeyCode;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.util.ActionResult;
import xyz.xdmatthewbx.chatlog.KeyBind;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static xyz.xdmatthewbx.chatlog.ChatLog.CONFIG;
import static xyz.xdmatthewbx.chatlog.ChatLog.registerChangeListener;

@Module
public class LagSwitchModule extends BaseModule {
	
	public static final String MODULE_ID = "lagswitch";
	public static LagSwitchModule INSTANCE;
	
	private final KeyBind keyBind = new KeyBind(ModifierKeyCode.unknown());
	
	public boolean active = false;
	
	@FunctionalInterface
	public static interface QueuedPacket {
		void processPacket();
	}
	
	public final List<QueuedPacket> QUEUED_PACKETS = Collections.synchronizedList(new ArrayList<>());
	
	public LagSwitchModule() {
		super(MODULE_ID);
		INSTANCE = this;
	}
	
	@Override
	public void onInitializeClient() {
		registerChangeListener(CONFIG, (configHolder, chatLogConfig) -> {
			keyBind.setBoundKey(chatLogConfig.main.general.enabled ?
			                    chatLogConfig.main.lagSwitchModule.keyBind :
			                    ModifierKeyCode.unknown());
			
			active = false;
			
			return ActionResult.PASS;
		});
		
		ClientTickEvents.END_WORLD_TICK.register(world -> {
			var prevActive = active;
			
			switch (CONFIG.get().main.lagSwitchModule.mode) {
				case HOLD -> active = keyBind.isPressed();
				case TOGGLE -> {
					if (keyBind.wasPressed())
						active = !active;
				}
			}
			
			if (prevActive && !active) {
				for (var packet : QUEUED_PACKETS) {
					packet.processPacket();
				}
				QUEUED_PACKETS.clear();
			}
		});
	}
	
}
