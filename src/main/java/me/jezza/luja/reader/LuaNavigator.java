package me.jezza.luja.reader;

import me.jezza.luja.buffer.CharBuffer;
import me.jezza.luja.buffer.IndexBuffer;

import java.util.NoSuchElementException;

import static me.jezza.luja.reader.ElementTypes.*;

/**
 * @author jezza
 * @date 4/12/2015
 */
public class LuaNavigator {

	private final CharBuffer dataBuffer;
	private final IndexBuffer elementBuffer;
	private int elementIndex;

	private final int startIndex;

	public LuaNavigator(final CharBuffer dataBuffer, final IndexBuffer elementBuffer) {
		this(dataBuffer, elementBuffer, -1);
	}

	public LuaNavigator(final CharBuffer dataBuffer, final IndexBuffer elementBuffer, final int startingIndex) {
		this.dataBuffer = dataBuffer;
		this.elementBuffer = elementBuffer;
		elementIndex = startingIndex;
		startIndex = startingIndex;
	}

	public boolean hasNext() {
		return elementIndex + 1 < size();
	}

	public LuaNavigator next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		elementIndex++;
		return this;
	}

	public boolean hasPrevious() {
		return elementIndex > 0;
	}

	public LuaNavigator previous() {
		if (!hasPrevious()) {
			throw new NoSuchElementException();
		}
		elementIndex--;
		return this;
	}

	public int index() {
		return elementIndex;
	}

	public int size() {
		return elementBuffer.size;
	}

	public void resetPosition() {
		elementIndex = startIndex;
	}

	public int position() {
		return elementBuffer.position[elementIndex];
	}

	public int length() {
		return elementBuffer.length[elementIndex];
	}

	public byte type() {
		return elementBuffer.type[elementIndex];
	}

	public String cursorPosition() {
		int line = 1;
		int index = 0;

		for (int i = 0; i < position(); i++) {
			if (dataBuffer.data[i] == LuaReader.NEW_LINE) {
				line++;
				index = 0;
			} else {
				index++;
			}
		}

		return String.format("Line #%s, Char #%s", Integer.toString(line), Integer.toString(index));
	}

	public boolean isEqual(final String target) {
		final int pos = position();
		for (int j = 0; j < length(); j++) {
			if (target.charAt(j) != dataBuffer.data[pos + j]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @return the number of components that reside on the same depth inside of
	 * the method.
	 */
	public int countMethodComponents() {
		int count = 0;
		int depth = 0;
//		for (int tempIndex = elementIndex + 1;; tempIndex++) {
//			if (tempIndex >= elementBuffer.type.length) {
//				throw new RuntimeException("Something went wrong while parsing methods...");
//			}

//			final byte type = elementBuffer.type[tempIndex];
//			if (depth == 0 && type == BODY_END) {
//				break;
//			}
//
//			switch (type) {
//				case FUNCTION_START:
//				case BODY_START:
//					depth++;
//					break;
//				case FUNCTION_END:
//				case BODY_END:
//					depth--;
//					break;
//				default:
//			}
//			if (depth == 0) {
//				count++;
//			}
//		}
		return count;
	}

	public int countMethodComponentsNoDepth() {
		int tempIndex = elementIndex + 1;
		int count = 0;
		int depth = 0;
//		for (;; tempIndex++) {
//			if (tempIndex >= elementBuffer.type.length) {
//				throw new RuntimeException("Something went parsing methods...");
//			}
//
//			final byte type = elementBuffer.type[tempIndex];
//			if (depth == 0 && type == BODY_END) {
//				break;
//			}
//
//			switch (type) {
//				case FUNCTION_START:
//				case BODY_START:
//					depth++;
//					break;
//				case FUNCTION_END:
//				case BODY_END:
//					depth--;
//					break;
//				default:
//			}
//			count++;
//		}
		return count;
	}

	/**
	 * @return the number of components that reside inside of a function call.
	 */
	public int countFunctionComponents() {
		int count = 0;
		int tempIndex = elementIndex + 1;
		byte type = elementBuffer.type[tempIndex];
		while (type != FUNCTION_END) {
			switch (type) {
				case COLON:
				case COMMA:
					type = elementBuffer.type[++tempIndex];
					continue;
				default:
			}
			count++;
			type = elementBuffer.type[++tempIndex];
		}
		return count;
	}

	public String typeString() {
		switch (type()) {
			case ElementTypes.NAMESPACE:
				return "NAMESPACE";
			case ElementTypes.NUMBER:
				return "NUMBER";
			case ElementTypes.STRING:
				return "STRING";
			case ElementTypes.COMMENT:
				return "COMMENT";
			//
			case ElementTypes.OBJECT_START:
				return "OBJECT_START";
			case ElementTypes.OBJECT_END:
				return "OBJECT_END";
			case ElementTypes.FUNCTION_START:
				return "FUNCTION_START";
			case ElementTypes.FUNCTION_END:
				return "FUNCTION_END";
			case ElementTypes.ARRAY_START:
				return "ARRAY_START";
			case ElementTypes.ARRAY_END:
				return "ARRAY_END";
			//
			case ElementTypes.COLON:
				return "COLON";
			case ElementTypes.QUESTION:
				return "QUESTION";
			case ElementTypes.SEMI_COLON:
				return "SEMI_COLON";
			case ElementTypes.COMMA:
				return "COMMA";
			case ElementTypes.PERIOD:
				return "PERIOD";
			case ElementTypes.HASH:
				return "HASH";
			//
			case ElementTypes.EQUAL:
				return "EQUAL";
			case ElementTypes.ADD:
				return "ADD";
			case ElementTypes.SUB:
				return "SUB";
			case ElementTypes.MUL:
				return "MUL";
			case ElementTypes.DIV:
				return "DIV";
			case ElementTypes.CONCAT:
				return "CONCAT";
			case ElementTypes.VARARGS:
				return "VARARGS";
			//
			case ElementTypes.LOGIC_NOT:
				return "LOGIC_NOT";
			//
			case ElementTypes.AND:
				return "AND";
			case ElementTypes.BREAK:
				return "BREAK";
			case ElementTypes.DO:
				return "DO";
			case ElementTypes.ELSE:
				return "ELSE";
			case ElementTypes.ELSEIF:
				return "ELSEIF";
			case ElementTypes.END:
				return "END";
			case ElementTypes.FALSE:
				return "FALSE";
			case ElementTypes.FOR:
				return "FOR";
			case ElementTypes.FUNCTION:
				return "FUNCTION";
			case ElementTypes.IF:
				return "IF";
			case ElementTypes.IN:
				return "IN";
			case ElementTypes.LOCAL:
				return "LOCAL";
			case ElementTypes.NIL:
				return "NIL";
			case ElementTypes.NOT:
				return "NOT";
			case ElementTypes.OR:
				return "OR";
			case ElementTypes.REPEAT:
				return "REPEAT";
			case ElementTypes.RETURN:
				return "RETURN";
			case ElementTypes.THEN:
				return "THEN";
			case ElementTypes.TRUE:
				return "TRUE";
			case ElementTypes.UNTIL:
				return "UNTIL";
			case ElementTypes.WHILE:
				return "WHILE";
			default:
				return "UNDEFINED";
		}
	}

	public String asString() {
		final int pos = position();
		final int length = length();
		final StringBuilder builder = new StringBuilder(length);
		for (int j = 0; j < length; j++)
			builder.append(dataBuffer.data[pos + j]);
		return builder.toString();
	}

	public boolean asBoolean() {
		return type() == NAMESPACE && Boolean.parseBoolean(asString());
	}
}