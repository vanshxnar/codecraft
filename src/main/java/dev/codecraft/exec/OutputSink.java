package dev.codecraft.exec;

/** Receives streamed output from a running lesson program. All methods may be called off-thread. */
public interface OutputSink {
	void onOutput(String line);

	void onError(String line);

	void onFinished(boolean success);
}
