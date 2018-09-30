package chatserver;

import chatserver.ChatServer.ServerState;

public interface ChatServerListener {
	void onStateChanged(ServerState state);
}
