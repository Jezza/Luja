package me.jezza.luja;

import me.jezza.luja.reader.LuaNavigator;
import me.jezza.luja.reader.LuaReader;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Jezza
 */
public class Luja {

	public static void main(String[] args) throws IOException {
		InputStream resource = Luja.class.getResourceAsStream("/MethodTest.lua");
		LuaNavigator navigator = new LuaReader(resource).read();

		long start = System.currentTimeMillis();
		while (navigator.hasNext()) {
			navigator.next();
			System.out.println(navigator.typeString() + ": "+ navigator.asString());
		}
		long end = System.currentTimeMillis();
	}
}
