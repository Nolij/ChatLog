package xyz.xdmatthewbx.chatlog;

import com.google.common.collect.*;
import me.shedaniel.clothconfig2.api.Modifier;
import me.shedaniel.clothconfig2.api.ModifierKeyCode;
import net.minecraft.client.util.InputUtil;

import java.lang.ref.WeakReference;
import java.util.*;

public class KeyBind {

	private static final Multimap<ModifierKeyCode, WeakReference<KeyBind>> BINDS = HashMultimap.create();

	public static Collection<KeyBind> getBinds(ModifierKeyCode key) {
		return ChatLog.resolveValidReferences(BINDS.get(key));
	}

	public static Collection<KeyBind> getAllBinds() {
		return ChatLog.resolveValidReferences(BINDS.values());
	}

	public static void setPressed(ModifierKeyCode key, boolean value) {
		for (KeyBind bind : ChatLog.resolveValidReferences(BINDS.get(key))) {
			bind.setPressed(value);
		}
	}

	public static void resetAll() {
		for (KeyBind bind : ChatLog.resolveValidReferences(BINDS.values())) {
			bind.reset();
		}
	}

	private ModifierKeyCode boundKey;
	private boolean pressed;
	public int timesPressed;

	public KeyBind(ModifierKeyCode key) {
		BINDS.put(key, new WeakReference<>(this));
		boundKey = key;
	}

	public ModifierKeyCode getBoundKey() {
		return boundKey;
	}

	public void setBoundKey(ModifierKeyCode value) {
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
		return boundKey.getKeyCode().getCode() == key;
	}

}
