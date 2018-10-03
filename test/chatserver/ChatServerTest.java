package chatserver;

import static org.junit.Assert.*;

import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
		
		// Start�������B
		server.toggleStart();
		
		// Start��ԂɂȂ�̂�҂�
		listener.waitStateChange();
		
		// �N���C�A���g���ڑ����Ă���
		serverSocket.simulateAccept(); //�@1�ڂ̃N���C�A���g
		serverSocket.simulateAccept(); // 2�ڂ̃N���C�A���g
		
		TestClientSocket clientSocket1 = serverSocket.getClients().get(0);
		TestClientSocket clientSocket2 = serverSocket.getClients().get(1);
		
		//�@�N���C�A���g2����M����܂ő҂悤�ɂ��� �@�e�X�g�p
		clientSocket2.setWaitEnabledWhenWritten(true);
		
		// �N���C�A���g1��HELLO�����C�g�������Ƃ�͋[
		clientSocket1.simulateRead("HELLO");
		
		// 2�ڂ̃N���C�A���g����M������܂ő҂�
		clientSocket2.waitWritten();
		
		// �S�N���C�A���g�Ƀ}���`�L���X�g�����
		assertEquals("HELLO", clientSocket1.getWrittenText().get(0));
		assertEquals("HELLO", clientSocket2.getWrittenText().get(0));
	}
	
	@Test
	public void testServerSocketThrowsErrorAtStart() throws IOException {
		ChatServer server = new ChatServer(6001) {
			@Override
			protected IServerSocket createServerSocket(int port)
					throws IOException {
				// �\�P�b�g�I�[�v�����ɃG���[�ɂ���
				throw new IOException();
			}
		};
		
		ChatServerListenerImpl listener = new ChatServerListenerImpl();
		server.setListener(listener);
		
		// ������Ԃ̃{�^����Start
		assertEquals("Start", server.getButtonLabel());
		
		// Start���������\�P�b�g�I�[�v���ŃG���[���o��B
		server.toggleStart();
		
		// �G���[���b�Z�[�W���o�Ă���
		assertEquals("Cannot start server", listener.getErrorMessage());
		
		// �{�^����Start�̂܂�
		assertEquals("Start", server.getButtonLabel());
		
	}
}
