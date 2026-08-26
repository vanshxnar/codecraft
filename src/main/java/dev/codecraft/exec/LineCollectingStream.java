package dev.codecraft.exec;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/** Buffers bytes and hands complete lines to a callback, so streamed console output arrives one line at a time. */
final class LineCollectingStream extends OutputStream {
	private final Consumer<String> lineConsumer;
	private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

	LineCollectingStream(Consumer<String> lineConsumer) {
		this.lineConsumer = lineConsumer;
	}

	@Override
	public synchronized void write(int b) {
		if (b == '\n') {
			lineConsumer.accept(buffer.toString(StandardCharsets.UTF_8));
			buffer.reset();
		} else if (b != '\r') {
			buffer.write(b);
		}
	}
}
