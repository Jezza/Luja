package me.jezza.luja.lib;

import java.io.IOException;

/**
 * @author Jezza
 */
@FunctionalInterface
public interface Input {
	String input() throws IOException;
}
