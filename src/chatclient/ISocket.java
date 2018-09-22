package chatclient;
import java.io.IOException;

public interface ISocket {
	void writeText(String text);
	String readLine() throws IOException;
	void close() throws IOException;
}
