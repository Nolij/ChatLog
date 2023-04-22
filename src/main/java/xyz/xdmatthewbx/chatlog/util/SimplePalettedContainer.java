package xyz.xdmatthewbx.chatlog.util;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class SimplePalettedContainer<T> {
	private final CopyOnWriteArrayList<T> entries;
	private final int maxSize;

	public SimplePalettedContainer(int size) {
		this.entries = new CopyOnWriteArrayList<>();
		this.maxSize = size;
	}

	public T get(int id) {
		return this.entries.get(id);
	}

	public synchronized int put(T entry) {
		/* iterate instead of using a hashmap for memory reasons */
		for (int i = 0; i < this.entries.size(); i++) {
			T old = this.entries.get(i);
			if (Objects.equals(old, entry))
				return i;
		}
		int newId = this.entries.size();
		if (newId > this.maxSize) {
			throw new IndexOutOfBoundsException("Out of palette space");
		}
		this.entries.add(entry);
		return newId;
	}
}
