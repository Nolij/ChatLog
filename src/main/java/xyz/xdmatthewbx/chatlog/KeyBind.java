package xyz.xdmatthewbx.chatlog;

import com.google.common.collect.*;
import com.mojang.blaze3d.platform.InputUtil;
import me.shedaniel.clothconfig2.api.Modifier;
import me.shedaniel.clothconfig2.api.ModifierKeyCode;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

public class KeyBind {

	private static final Multimap<ModifierKeyCode, KeyBind> BINDS = LinkedListMultimap.create();

	public static Collection<KeyBind> getBinds(ModifierKeyCode key) {
		return BINDS.get(key);
	}

	public static Collection<KeyBind> getAllBinds() {
		return BINDS.values();
	}

	public static void setPressed(ModifierKeyCode key, boolean value) {
		for (KeyBind bind : BINDS.get(key)) {
			bind.setPressed(value);
		}
	}

	public static void resetAll() {
		for (KeyBind bind : BINDS.values()) {
			bind.reset();
		}
	}

	private ModifierKeyCode boundKey;
	private boolean pressed;
	public int timesPressed;

	public KeyBind(ModifierKeyCode key) {
		BINDS.put(key, this);
		boundKey = key;
	}

	public ModifierKeyCode getBoundKey() {
		return boundKey;
	}

	public void setBoundKey(ModifierKeyCode value) {
		BINDS.remove(boundKey, this);
		boundKey = value;
		BINDS.put(boundKey, this);
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
		if (value && !pressed) {
			timesPressed++;
			pressed = true;
		} else {
			pressed = value;
		}
	}

	public void reset() {
		timesPressed = 0;
		pressed = false;
	}

	public boolean matches(int key, int scancode, short modifiers) {
		return boundKey == ModifierKeyCode.of(InputUtil.fromKeyCode(key, scancode), Modifier.of(modifiers));
	}

	public boolean matches(int key, int scancode) {
		return boundKey.getKeyCode().getKeyCode() == key;
	}

}
