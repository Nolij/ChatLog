package dev.nolij.chatlog;

import com.google.common.collect.*;
import net.minecraft.client.util.InputUtil;

import java.lang.ref.WeakReference;
import java.util.*;

public class KeyBind {

	private static final Multimap<InputUtil.Key, WeakReference<KeyBind>> BINDS = HashMultimap.create();

	public static Collection<KeyBind> getBinds(InputUtil.Key key) {
		return ChatLog.resolveValidReferences(BINDS.get(key));
	}

	public static Collection<KeyBind> getAllBinds() {
		return ChatLog.resolveValidReferences(BINDS.values());
	}

	public static void setPressed(InputUtil.Key key, boolean value) {
		for (KeyBind bind : ChatLog.resolveValidReferences(BINDS.get(key))) {
			bind.setPressed(value);
		}
	}
	
	public static void press(InputUtil.Key key) {
		for (KeyBind bind : ChatLog.resolveValidReferences(BINDS.get(key))) {
			bind.timesPressed++;
		}
	}

	public static void resetAll() {
		for (KeyBind bind : ChatLog.resolveValidReferences(BINDS.values())) {
			bind.reset();
		}
	}

	private InputUtil.Key boundKey;
	private boolean pressed;
	public int timesPressed;

	public KeyBind(InputUtil.Key key) {
		BINDS.put(key, new WeakReference<>(this));
		boundKey = key;
	}

	public InputUtil.Key getBoundKey() {
		return boundKey;
	}

	public void setBoundKey(InputUtil.Key value) {
		for (var ref : BINDS.get(boundKey)) {
			if (ref.get() == this) {
				BINDS.remove(boundKey, ref);
				break;
			}
		}
		boundKey = value;
		BINDS.put(boundKey, new WeakReference<>(this));
	}

	public boolean isPressed() {
		return pressed;
	}

	public boolean wasPressed() {
		if (timesPressed > 0) {
			timesPressed--;
			return true;
		}
		return false;
	}

	public void setPressed(boolean value) {
		pressed = value;
	}

	public void reset() {
		timesPressed = 0;
		pressed = false;
	}

	public boolean matches(InputUtil.Key key) {
		return this.boundKey.equals(key);
	}

}
