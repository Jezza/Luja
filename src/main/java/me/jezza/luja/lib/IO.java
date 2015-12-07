package me.jezza.luja.lib;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Scanner;

/**
 * @author Jezza
 */
public class IO {
	public static final int EOL = -1;
	public static final int DEFAULT_BUFFER_SIZE = 4096;
	public static final String SCANNER_END_OF_FILE_TOKEN = "\\A";

	public static String toString(Reader in) throws IOException {
		char[] arr = new char[DEFAULT_BUFFER_SIZE];
		StringBuilder buffer = new StringBuilder();
		int numCharsRead;
		while ((numCharsRead = in.read(arr, 0, DEFAULT_BUFFER_SIZE)) != EOL)
			buffer.append(arr, 0, numCharsRead);
		return buffer.toString();
	}

	public static String toString(InputStream in) throws IOException {
		try (Scanner s = new Scanner(in).useDelimiter(SCANNER_END_OF_FILE_TOKEN)) {
			return s.hasNext() ? s.next() : "";
		}
	}
}
