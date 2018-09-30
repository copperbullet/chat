package chatserver;

import java.io.IOException;

import share.ISocket;

public interface IServerSocket {

	ISocket accept() throws IOException;

	void close() throws IOException;

}
