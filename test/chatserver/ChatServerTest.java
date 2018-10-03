package chatserver;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import share.ISocket;
import chatserver.ChatServer.ServerState;

public class ChatServerTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
		
	
	@Test
	public void testChatServerDefaultValues() throws IOException {
		final TestServerSocket serverSocket = new TestServerSocket();
		ChatServer server = new ChatServer(6001) {
			@Override
			protected IServerSocket createServerSocket(int port)
					throws IOException {
				return serverSocket;
			}
		};
		assertEquals("Start", server.getButtonLabel());
		assertEquals(ServerState.IDLE, server.getStatus());
	}

	@Test
	public void testGuiStatusWhenStartIsPushed() throws IOException {
		final TestServerSocket serverSocket = new TestServerSocket();
		ChatServer server = new ChatServer(6001) {
			@Override
			protected IServerSocket createServerSocket(int port)
					throws IOException {
				return serverSocket;
			}
		};	
		
		ChatServerListenerImpl listener = new ChatServerListenerImpl();
		server.setListener(listener);
		
		// Startを押す。
		server.toggleStart();
		
		// Start状態になるのを待つ
		listener.waitStateChange();
		
		// 状態はRunningになっていればOK
		assertEquals(ServerState.RUNNING, server.getStatus());
		assertEquals(ServerState.RUNNING, listener.getServerStateLog().get(0));
		// ボタンはSTOPになっている
		assertEquals("Stop", server.getButtonLabel());
	}

	@Test
	public void testWhenClientIsConnected() throws InterruptedException, IOException {
		final TestServerSocket serverSocket = new TestServerSocket();
		ChatServer server = new ChatServer(6001) {
			@Override
			protected IServerSocket createServerSocket(int port)
					throws IOException {
				return serverSocket;
			}
		};
		
		ChatServerListenerImpl listener = new ChatServerListenerImpl();
		server.setListener(listener);
		
		server.toggleStart();
		
		// Start状態になるのを待つ
		listener.waitStateChange();
		
		// クライアントが接続してきた
		serverSocket.simulateAccept(); //　1個目のクライアント
		serverSocket.simulateAccept(); // 2個目のクライアント
		
		TestClientSocket clientSocket1 = serverSocket.getClients().get(0);
		TestClientSocket clientSocket2 = serverSocket.getClients().get(1);
		
		//　クライアント2が受信するまで待つようにする 　テスト用
		clientSocket2.setWaitEnabledWhenWritten(true);
		
//		System.out.println("1");
		// クライアント1がHELLOをライトしたことを模擬
		clientSocket1.simulateRead("HELLO");
		
		// 2個目のクライアントが受信をするまで待つ
		clientSocket2.waitWritten();
		
		// 全クライアントにマルチキャストされる
		assertEquals("HELLO", clientSocket1.getWrittenText().get(0));
		assertEquals("HELLO", clientSocket2.getWrittenText().get(0));
	}
	
	@Test
	public void testServerSocketThrowsErrorAtStart() throws IOException {
		ChatServer server = new ChatServer(6001) {
			@Override
			protected IServerSocket createServerSocket(int port)
					throws IOException {
				// ソケットオープン時にエラーにする
				throw new IOException();
			}
		};
		
		ChatServerListenerImpl listener = new ChatServerListenerImpl();
		server.setListener(listener);
		
		assertEquals("Start", server.getButtonLabel());
		server.toggleStart();
		assertEquals("Start", server.getButtonLabel());
		assertEquals("Cannot start server", listener.getErrorMessage());
	}
}
