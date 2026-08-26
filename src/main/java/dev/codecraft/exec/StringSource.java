package dev.codecraft.exec;

import javax.tools.SimpleJavaFileObject;
import java.net.URI;

final class StringSource extends SimpleJavaFileObject {
	private final String code;

	StringSource(String className, String code) {
		super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
		this.code = code;
	}

	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors) {
		return code;
	}
}
