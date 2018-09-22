package chatserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public abstract class ClientHandler {

	private PrintWriter writer;

	public ClientHandler(final Socket clientSocket) {
			try {
				writer = new PrintWriter(clientSocket.getOutputStream(), true);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			new Thread() {
				@Override
				public void run() {
					try {
						BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
						String message = "";
					
						while ((message = reader.readLine()) != null) {
							onMessageReceived(message);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}.start();
	}

	protected abstract void onMessageReceived(String message);

	public void sendMessage(String message) {
		writer.write(message + "\n");
		writer.flush();
	}

}
