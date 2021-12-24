package xyz.xdmatthewbx.chatlog.util;

public class Lock {

	private boolean isLocked = false;

	public boolean isLocked() {
		return isLocked;
	}

	public void obtain() {
		if (isLocked) throw new IllegalStateException();
		isLocked = true;
	}

	public void release() {
		if (!isLocked) throw new IllegalStateException();
		isLocked = false;
	}

}