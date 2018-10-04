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
		
	/**
	 * �N�����̏�����Ԃ̃e�X�g
	 * GUI�̕\����Ԃ��e�X�g����
	 */
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
		// �{�^����Start�ł��邱��
		assertEquals("Start", server.getButtonLabel());
		
		//�@��Ԃ�IDLE�ł��邱��
		assertEquals(ServerState.IDLE, server.getStatus());
	}

	/**
	 * Start�{�^���������ꂽ�Ƃ��̃e�X�g
	 * GUI�̕\����Ԃ��e�X�g����
	 */
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
		
		TestChatServerListener listener = new TestChatServerListener();
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

	/**
	 * Start�{�^���������ꂽ�Ƃ��̃e�X�g
	 * �����̃N���C�A���g�Ƀ��b�Z�[�W���}���`�L���X�g����邱�Ƃ��e�X�g
	 */
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
		
		TestChatServerListener listener = new TestChatServerListener();
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
	
	
	/**
	 * Start�{�^���������ꂽ�Ƃ��Ƀ\�P�b�g���G���[���o�����Ƃ��̃e�X�g
	 */
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
		
		TestChatServerListener listener = new TestChatServerListener();
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
