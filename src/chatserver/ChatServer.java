package chatserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import share.ISocket;

public class ChatServer {
	
	private List<ClientHandler> clients = new ArrayList<>();
	private ServerState status = ServerState.IDLE;
	private IServerSocket serverSocket;
	private ChatServerListener chatServerListener = new ChatServerListener() {
		@Override
		public void onStateChanged(ServerState state) {

		}
	};
	
	public ChatServer(IServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}

	public enum ServerState {
		IDLE,
		RUNNING
	}
	
	protected void startServer() {
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				setStatus(ServerState.RUNNING);
			}
		});
		
		while (true) {
			try {
				ISocket clientSocket = serverSocket.accept();
				
				clients.add(new ClientHandler(clientSocket) {
					@Override
					protected void onMessageReceived(String message) {
						sendMessageToAllClients(message);
					}
	
					@Override
					protected void onClosed(ClientHandler clientHandler) {
						clients.remove(clientHandler);
					}
				});
			}
			catch(Exception e) {
				break;
			}
		}
		setStatus(ServerState.IDLE);
	}

	private void setStatus(ServerState status) {
		this.status = status;
		this.chatServerListener.onStateChanged(this.status);
	}

	protected void sendMessageToAllClients(String message) {
		for (ClientHandler client : this.clients) {
			client.sendMessage(message);
		}
	}

	public void setListener(ChatServerListener chatServerListener) {
		this.chatServerListener = chatServerListener;
	}

	public String getButtonLabel() {
		if (this.status.equals(ServerState.IDLE)) {
			return "Start";
		}
		else {
			return "Stop";
		}
	}

	public void toggleStart() {
		if (this.status.equals(ServerState.IDLE)) {
			new Thread() {
				@Override
				public void run() {
					startServer();
				}
			}.start();
			
		}
		else {
			stopServer();
		}
	}

	private void stopServer() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (ClientHandler client : this.clients) {
			client.stop();
		}
	}

	public ServerState getStatus() {
		return status;
	}
}
