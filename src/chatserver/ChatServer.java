package chatserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {

	public static void main(String[] arg) {
		new ChatServer(8001);
	}
	
	private List<ClientHandler> clients = new ArrayList<>();
	
	public ChatServer(int port) {
		try {
			ServerSocket serverSocket = new ServerSocket(port);
			while (true) {
				Socket clientSocket = serverSocket.accept();
				
				clients.add(new ClientHandler(clientSocket) {
					@Override
					protected void onMessageReceived(String message) {
						sendMessageToAllClients(message);
					}
				});
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	protected void sendMessageToAllClients(String message) {
		for (ClientHandler client : this.clients) {
			client.sendMessage(message);
		}
	}
}
