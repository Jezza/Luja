package me.jezza.luja.reader;

import me.jezza.luja.buffer.CharBuffer;
import me.jezza.luja.buffer.IndexBuffer;
import me.jezza.luja.lib.IO;
import me.jezza.luja.lib.Input;

import java.io.*;

import static me.jezza.luja.reader.ElementTypes.*;

/**
 * @author Jezza
 */
public class LuaReader {
	public static final char NEW_LINE = '\n';

	private int position = 0;
	private int elementIndex = 0;

	private final CharBuffer dataBuffer;
	private IndexBuffer elementBuffer;
	private final Input in;

	public LuaReader(final String string) {
		this(() -> string);
	}

	public LuaReader(final File file) throws FileNotFoundException {
		this(new FileReader(file));
	}

	public LuaReader(final Reader in) {
		this(() -> IO.toString(in));
	}

	public LuaReader(final InputStream in) {
		this(() -> IO.toString(in));
	}

	public LuaReader(final Input in) {
		if (in == null) {
			throw new NullPointerException("Input is null.");
		}
		this.in = in;
		dataBuffer = new CharBuffer();
	}

	public LuaNavigator read() throws IOException {
		String input = in.input();
		dataBuffer.set(input.toCharArray());
		elementBuffer = new IndexBuffer(dataBuffer.length, true);
		int expectedPosition;
		for (position = 0; position < dataBuffer.length; incrementPosition(expectedPosition)) {
			final char c = dataBuffer.data[position];
			expectedPosition = position;
			if (isNamespace(c)) {
				position = consumeNamespace();
				continue;
			}
			if (Character.isDigit(c)) {
				position = consumeNumber();
				continue;
			}
			switch (c) {
				case '\'':
				case '"':
					position = consumeString();
					break;
				//
				case '{':
					setElementData(elementIndex++, OBJECT_START, position);
					break;
				case '}':
					setElementData(elementIndex++, OBJECT_END, position);
					break;
				case '(':
					setElementData(elementIndex++, FUNCTION_START, position);
					break;
				case ')':
					setElementData(elementIndex++, FUNCTION_END, position);
					break;
				case '[':
					setElementData(elementIndex++, ARRAY_START, position);
					break;
				case ']':
					setElementData(elementIndex++, ARRAY_END, position);
					break;
				//
				case ':':
					setElementData(elementIndex++, COLON, position);
					break;
				case '?':
					setElementData(elementIndex++, QUESTION, position);
					break;
				case ';':
					setElementData(elementIndex++, SEMI_COLON, position);
					break;
				case ',':
					setElementData(elementIndex++, COMMA, position);
					break;
				case '.':
					if (peek('.')) {
						if (peek(position + 2, '.')) {
							setElementData(elementIndex++, VARARGS, position, 3);
							position += 3;
						} else {
							setElementData(elementIndex++, CONCAT, position, 2);
							position += 2;
						}
					} else {
						setElementData(elementIndex++, PERIOD, position);
					}
					break;
				case '#':
					setElementData(elementIndex++, HASH, position);
					break;
				case '\n':
					setElementData(elementIndex++, EOL, position);
					break;
				//
				case '=':
					setElementData(elementIndex++, EQUAL, position);
					break;
				case '+':
					setElementData(elementIndex++, ADD, position);
					break;
				case '*':
					setElementData(elementIndex++, MUL, position);
					break;
				//
				case '-':
					position = consumeDash();
					break;
				//
				case '!':
					setElementData(elementIndex++, NOT, position);
					break;
				case '~':
					setElementData(elementIndex++, LOGIC_NOT, position);
					break;
				default:
			}
		}

		elementBuffer.size = elementIndex;
		return new LuaNavigator(dataBuffer, elementBuffer);
	}

	private int consumeString() {
		int tempPos = position;
		while (true) {
			if (++tempPos >= dataBuffer.length)
				throw exception("Unexpected end of String");
			switch (dataBuffer.data[tempPos]) {
				case '\'':
				case '"':
					if (dataBuffer.data[tempPos - 1] != '\\') {
						setElementData(elementIndex++, STRING, position, tempPos - position + 1);
						return tempPos + 1;
					}
					break;
				default:
			}
		}
	}

	private int consumeNamespace() {
		int tempPos = position;
		while (++tempPos < dataBuffer.length) {
			if (!isNamespace(dataBuffer.data[tempPos])) {
				setElementData(elementIndex++, NAMESPACE, this.position, tempPos - this.position);
				return tempPos;
			}
		}
		setElementData(elementIndex++, NAMESPACE, this.position, tempPos - this.position);
		return tempPos;
	}

	private int consumeNumber() {
		int tempPos = position;
		while (++tempPos < dataBuffer.length) {
			if (!Character.isDigit(dataBuffer.data[tempPos])) {
				setElementData(elementIndex++, NUMBER, this.position, tempPos - this.position);
				return tempPos;
			}
		}
		setElementData(elementIndex++, NUMBER, this.position, tempPos - this.position);
		return tempPos;
	}

	private int consumeDash() {
		if (!peek('-')) {
			// Consume full comment stuffs
			setElementData(elementIndex++, SUB, position);
			return position;
		}
		position++;
		if (peek("[["))
			return skipBlockComment();
		return skipLineComment();
	}

	private int skipBlockComment() {
		int tempPos = position;
		if (tempPos + 1 >= dataBuffer.length)
			throw exception("Unexpected end of block comment.", tempPos);
		while (++tempPos < dataBuffer.length) {
			if (tempPos + 1 >= dataBuffer.length)
				throw exception("Unexpected end of block comment.", tempPos);
			if (dataBuffer.data[tempPos] == ']' && peek(tempPos + 1, ']'))
				return tempPos + 2;
		}
		return tempPos;
	}

	private int skipLineComment() {
		int tempPos = position;
		while (++tempPos < dataBuffer.length) {
			if (dataBuffer.data[tempPos] == NEW_LINE)
				return tempPos + 1;
		}
		return tempPos;
	}

	private boolean peek(char c) {
		return peek(position + 1, c);
	}

	private boolean peek(int offset, char c) {
		return position + offset < dataBuffer.length && dataBuffer.data[offset] == c;
	}

	private boolean peek(String c) {
		return peek(1, c);
	}

	private boolean peek(int offset, String c) {
		if (position + offset + c.length() < dataBuffer.length) {
			for (int i = 0; i < c.length(); i++) {
				if (c.charAt(i) != dataBuffer.data[position + offset + i]) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	private void incrementPosition(int expected) {
		if (position == expected)
			position++;
	}

	private boolean isNamespace(char c) {
		return Character.isAlphabetic(c) || c == '_';
	}

	private void setElementData(final int index, final byte type, final int position) {
		elementBuffer.type[index] = type;
		elementBuffer.position[index] = position;
		elementBuffer.length[index] = 1;
	}

	private void setElementData(final int index, final byte type, final int position, final int length) {
		elementBuffer.type[index] = type;
		elementBuffer.position[index] = position;
		elementBuffer.length[index] = length;
	}

	private RuntimeException exception(final String message) {
		throw new RuntimeException(message + ':' + cursorPosition());
	}

	private RuntimeException exception(final String message, final Throwable cause) {
		throw new RuntimeException(message + ':' + cursorPosition(), cause);
	}

	private RuntimeException exception(final String message, final int position) {
		throw new RuntimeException(message + ':' + cursorPosition(position));
	}

	private RuntimeException exception(final String message, final int position, final Throwable cause) {
		throw new RuntimeException(message + ':' + cursorPosition(position), cause);
	}

	private String cursorPosition() {
		return cursorPosition(position);
	}

	private String cursorPosition(final int position) {
		int line = 1;
		int index = 0;

		for (int i = 0; i < position; i++) {
			if (dataBuffer.data[i] == NEW_LINE) {
				line++;
				index = 0;
			} else {
				index++;
			}
		}

		return String.format("Line #%s, Char #%s", Integer.toString(line), Integer.toString(index));
	}
}