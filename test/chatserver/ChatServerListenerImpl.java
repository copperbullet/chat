package chatserver;

import java.util.ArrayList;
import java.util.List;

import chatserver.ChatServer.ServerState;

public 	class ChatServerListenerImpl implements ChatServerListener{
	private List<ServerState> serverStateLog = new ArrayList<>();
	private Object sync = null;
	private String errorMessage = "";
	
	@Override
	public void onStateChanged(ServerState state) {
		serverStateLog.add(state);
		while(sync == null) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		synchronized(sync) {
			sync.notify();
		}
	}
	
	public void waitStateChange() {
		sync = new Object();
		synchronized(sync) {
			try {
				sync.wait();
				sync = null;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}	
	}

	public List<ServerState> getServerStateLog() {
		return serverStateLog;
	}

	
	public String getErrorMessage() {
		return errorMessage;
	}

	@Override
	public void onError(String message) {
		errorMessage = message;
	}
}
