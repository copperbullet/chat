package chatserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import share.ISocket;

public 	class TestClientSocket implements ISocket {
	private Object readLock = null;
	private String readText;
	private List<String> writtenText = new ArrayList<>();
	private Object writtenLock = null;
	private boolean waitEnabledWhenWritten = false;
	
	public void setWaitEnabledWhenWritten(boolean enabled) {
		this.waitEnabledWhenWritten  = enabled;
	}
	
	@Override
	public void writeText(String text) {
		this.writtenText.add(text);
		if (!waitEnabledWhenWritten) {
//			System.out.println("writeText return");
			return;
		}
		while (writtenLock == null) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		synchronized(writtenLock) {
//			System.out.println("writtenLock.notify()");
			writtenLock.notify();
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
//		System.out.println("waitWritten");
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
