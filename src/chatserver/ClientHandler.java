package chatserver;

import java.io.IOException;

import javax.swing.SwingUtilities;

import share.ISocket;

public abstract class ClientHandler {

	private ISocket socket;

	public ClientHandler(final ISocket clientSocket) {	
		this.socket = clientSocket;
		
		new Thread() {
			@Override
			public void run() {
				try {
					String message = "";
				
					while ((message = clientSocket.readLine()) != null) {
						toMainThread(message);
					}
				} catch (IOException e) {
					
				}
				onClosed(ClientHandler.this);
			}
		}.start();
	}

	protected void toMainThread(final String message) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				onMessageReceived(message);
			}	
		});
	}
	protected abstract void onMessageReceived(String message);
	protected abstract void onClosed(ClientHandler clientHandler);
	
	public void sendMessage(String message) {
		socket.writeText(message);
	}

	public void stop() {
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
