package chatserver;

import java.util.ArrayList;
import java.util.List;

import chatserver.ChatServer.ServerState;

public 	class ChatServerListenerImpl implements ChatServerListener{
	private List<ServerState> serverStateLog = new ArrayList<>();
	private Object sync = null;
	@Override
	public void onStateChanged(ServerState state) {
		serverStateLog.add(state);
		synchronized(sync) {
			while(sync == null) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
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
}
