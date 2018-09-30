package chatserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import share.ISocket;

public 	class TestClientSocket implements ISocket {
	private Object readLock = null;
	private String readText;
	private List<String> writtenText = new ArrayList<>();
	private Object writtenLock;
	
	@Override
	public void writeText(String text) {
		this.writtenText.add(text);
		
		if (writtenLock != null) {
			synchronized(writtenLock) {
				writtenLock.notify();
			}
		}
	}

	@Override
	public String readLine() throws IOException {
		readLock = new Object();
		synchronized(readLock) {
			try {
				readLock.wait();
				readLock = null;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return readText;
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}
	
	public void simulateRead(String text) {
		this.readText = text;
		while (readLock == null) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		synchronized(readLock) {
			readLock.notify();
		}
		
	}

	public List<String> getWrittenText() {
		return writtenText;
	}
	
	public void waitWritten() {
		this.writtenLock = new Object();
		synchronized(this.writtenLock) {
			try {
				this.writtenLock.wait();
				this.writtenLock = null;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
