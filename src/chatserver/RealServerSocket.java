package chatserver;

import java.io.IOException;
import java.net.ServerSocket;

import share.ISocket;
import share.RealSocket;

public class RealServerSocket implements IServerSocket {	

	private ServerSocket realServerSocket;

	public RealServerSocket(int port) throws IOException {
		realServerSocket = new ServerSocket(port);
	}
	@Override
	public ISocket accept() throws IOException {
		return new RealSocket(realServerSocket.accept());
	}

	@Override
	public void close() throws IOException {
		realServerSocket.close();
	}
}
