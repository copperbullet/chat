package share;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class RealSocket implements ISocket {
	private Socket socket = null;
	private PrintWriter out;
	private BufferedReader in;
	
	public RealSocket(Socket socket) {
		try {
			this.socket = socket;
			out = new PrintWriter(socket.getOutputStream());
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void writeText(String text) {
		out.write(text + "\n");
		out.flush();
	}

	@Override
	public String readLine() throws IOException {
		return in.readLine();
	}

	@Override
	public void close() throws IOException {
		this.socket.close();
	}
}
