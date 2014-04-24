package net.indiespot.struct.runtime;

public class StructAllocationStack extends StructAllocationBlock {
	private final int[] stack = new int[100];
	private int level;

	public StructAllocationStack(int handleOffset, int sizeof) {
		super(handleOffset, sizeof);
	}

	public void save() {
		stack[level++] = wordsAllocated;
	}

	public void restore() {
		wordsAllocated = stack[--level];
	}

	public int level() {
		return level;
	}

	public boolean isOnStack(int handle) {
		// whether handle is in parent stack
		return (handle < handleOffset + wordsAllocated);
	}
}
