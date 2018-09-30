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
	public void testChatServerDefaultValues() {
		TestServerSocket serverSocket = new TestServerSocket();
		ChatServer server = new ChatServer(serverSocket);
		assertEquals("Start", server.getButtonLabel());
		assertEquals(ServerState.IDLE, server.getStatus());
	}

	@Test
	public void testGuiStatusWhenStartIsPushed() {
		TestServerSocket serverSocket = new TestServerSocket();
		ChatServer server = new ChatServer(serverSocket);
		
		ChatServerListenerImpl listener = new ChatServerListenerImpl();
		server.setListener(listener);
		
		// Start�������B
		server.toggleStart();
		
		// Start��ԂɂȂ�̂�҂�
		listener.waitStateChange();
		
		// ��Ԃ�Running�ɂȂ��Ă����OK
		assertEquals(ServerState.RUNNING, server.getStatus());
		assertEquals(ServerState.RUNNING, listener.getServerStateLog().get(0));
		// �{�^����STOP�ɂȂ��Ă���
		assertEquals("Stop", server.getButtonLabel());
	}

	@Test
	public void testWhenClientIsConnected() throws InterruptedException {
		TestServerSocket serverSocket = new TestServerSocket();
		ChatServer server = new ChatServer(serverSocket);
		
		ChatServerListenerImpl listener = new ChatServerListenerImpl();
		server.setListener(listener);
		
		server.toggleStart();
		
		// Start��ԂɂȂ�̂�҂�
		listener.waitStateChange();
		
		// �N���C�A���g���ڑ����Ă���
		serverSocket.simulateAccept(); //�@1�ڂ̃N���C�A���g
		serverSocket.simulateAccept(); // 2�ڂ̃N���C�A���g
		
		TestClientSocket clientSocket1 = serverSocket.getClients().get(0);
		TestClientSocket clientSocket2 = serverSocket.getClients().get(1);
		
//		System.out.println("1");
		// �N���C�A���g1��HELLO�����C�g�������Ƃ�͋[
		clientSocket1.simulateRead("HELLO");
		
		// 2�ڂ̃N���C�A���g����M������܂ő҂�
		clientSocket2.waitWritten();
		
		// �S�N���C�A���g�Ƀ}���`�L���X�g�����
		assertEquals("HELLO", clientSocket1.getWrittenText().get(0));
		assertEquals("HELLO", clientSocket2.getWrittenText().get(0));
	}
}
