package me.jezza.luja.buffer;

/**
 * @author jezza
 * @date 4/12/2015
 */
public class IndexBuffer {
	public int[] position = null;
	public int[] length = null;
	public byte[] type = null;
	public int size = 0;

	public IndexBuffer() {
	}

	public IndexBuffer(final int capacity, final boolean useTypeArray) {
		position = new int[capacity];
		length = new int[capacity];
		if (useTypeArray) {
			type = new byte[capacity];
		}
	}
}
