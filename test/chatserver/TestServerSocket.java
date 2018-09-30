package chatserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import share.ISocket;

public 	class TestServerSocket implements IServerSocket {
	private Object acceptLock = null;
	private Object acceptExitLock = new Object();
	private List<TestClientSocket> clients = new ArrayList<>();
	
	@Override
	public ISocket accept() throws IOException {
		acceptLock = new Object();
		synchronized(acceptLock) {
			try {
				acceptLock.wait();
				acceptLock = null;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		TestClientSocket client = new TestClientSocket();
		clients.add(client);
		synchronized(acceptExitLock) {
			acceptExitLock.notify();
		}
		return client;
	}
	
	public void simulateAccept() {
		while (acceptLock == null) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		synchronized(acceptLock) {
			acceptLock.notify();
		}
		synchronized(acceptExitLock) {
			try {
				acceptExitLock.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public List<TestClientSocket> getClients() {
		return clients;
	}

	@Override
	public void close() throws IOException {
		if (this.acceptExitLock != null) {
			this.acceptExitLock.notify();
		}
		if (this.acceptLock != null) {
			this.acceptLock.notify();
		}
	}
}
