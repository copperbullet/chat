package chatserver;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.ServerSocket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

import share.ISocket;
import share.RealSocket;
import chatserver.ChatServer.ServerState;

public class ChatServerUi extends JFrame {
	protected static ChatServer createChatServer() throws IOException {
		final ServerSocket realServerSocket = new ServerSocket(8001);
	
		IServerSocket serverSocket = new IServerSocket() {
			@Override
			public ISocket accept() throws IOException {
				return new RealSocket(realServerSocket.accept());
			}

			@Override
			public void close() throws IOException {
				realServerSocket.close();
			}
		};
		ChatServer chatServer = new ChatServer(serverSocket);
		return chatServer;
	}
	
	public static void main(String[] args) throws IOException {		
		new ChatServerUi(createChatServer()).setVisible(true);
	}

	public ChatServerUi(final ChatServer chatServer) {
		
		this.setSize(new Dimension(200, 100));
		this.getContentPane().setLayout(new FlowLayout());
		
		final JButton startButton = new JButton(chatServer.getButtonLabel());
		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				chatServer.toggleStart();
			}
		});
		final JLabel status = new JLabel(chatServer.getStatus().toString());
		
		this.getContentPane().add(startButton);
		this.getContentPane().add(status);
		
		chatServer.setListener(new ChatServerListener() {
			@Override
			public void onStateChanged(ServerState state) {
				startButton.setText(chatServer.getButtonLabel());
				status.setText(state.toString());
			}
		});
	}
}
