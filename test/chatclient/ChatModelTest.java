package chatclient;
import java.io.IOException;
import java.lang.Thread.State;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import share.ISocket;
import junit.framework.TestCase;


public class ChatModelTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
		this.sentText = "";
		this.receivedText.clear();
		this.talkers.clear();
		this.sync = new Object();
		this.syncTest = new Object();
		this.receiveThread = null;
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	private String sentText = ""; // ���M���ꂽ������
	private Queue<String> receivedText = new ArrayDeque<>(); // �T�[�o�����M����������i�^���I�Ɂj
	private Object sync = new Object(); // �e�X�g�p�\�P�b�g��read��������^�C�~���O�𐧌䂷�邽�߂̑҂�
	private Thread receiveThread;
	
	//�@�e�X�g�p�̃\�P�b�g�N���X
	class TestSocket implements ISocket {
		@Override
		public void writeText(String text) {
			//�@�T�[�o�Ɍ����đ��M���ꂽ�������ێ�����
			sentText = text;
		}

		@Override
		public String readLine() {
			receiveThread = Thread.currentThread();
			// �e�X�g�R�[�h����̎w�������܂ő҂�
			synchronized(sync) {
				try {
					sync.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				};
			}
			String ret = receivedText.poll();
			
			return ret;
		}

		@Override
		public void close() throws IOException {
			synchronized(sync) {
				sync.notify();
			}
		}
	}
	
	// GUI���瑗�M�˗����ꂽ��������T�[�o�ɑ��M����e�X�g
	public void testTalk() {
		ISocket socket = new TestSocket();
		ChatModel chatModel = new ChatModel(socket);
		
		chatModel.setMyName("Andre Young");
		// GUI����talk���Ă΂ꂽ
		chatModel.talk("Hello!");
		
		// �\�P�b�g�ɂ��ꂪWrite���ꂽ��OK
		assertEquals("Andre Young;Hello!", sentText);
		
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		
	// �e�X�g��������҂����邽�߂̓����I�u�W�F�N�g
	//�@�҂����Ȃ��Ǝ�M�X���b�h����M����O�Ƀe�X�g���I����Ă��܂���
	private Object syncTest = new Object();
	
	// ���݂�talker�ꗗ
	private List<String> talkers = new ArrayList<>();
	
	// �T�[�o�����M�������b�Z�[�W�����O�Ƃ��ĕ\�������e�X�g
	public void testReceived() {	
		ISocket socket = new TestSocket();
		ChatModel chatModel = new ChatModel(socket);

		// chatModel����̃R�[���o�b�N��o�^����B�{��GUI���󂯂���́B
		chatModel.setListener(createChatModelListener());
		
		// ��M���b�Z�[�W�̐ݒ�
		receivedText.add("Marshal Mathers;Wassap Dre!");
		receivedText.add("Andre Young;Yo Eminem.");
		
		// �T�[�o����̎�M�f�[�^���[�h��͋[����
		simulateReceivingDataFromServer();
		// ��M�����̃R�[���o�b�N���󂯂�̂�҂B
		waitReceiveCompletion();
		
		// ��������M�����B
		assertEquals("Marshal Mathers : Wassap Dre!\n", chatModel.getConversation());	
				
		// �T�[�o����̎�M�f�[�^���[�h��͋[����
		simulateReceivingDataFromServer();
		
		waitReceiveCompletion();
		// �O��󂯂����b�Z�[���ǉ�����Ă����OK
		assertEquals("Marshal Mathers : Wassap Dre!\nAndre Young : Yo Eminem.\n", chatModel.getConversation());	
		
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * �����Җ��̃��b�Z�[�W�̃t�B���^���ł���
	 */
	public void testNewTalkerFilter() {
		ISocket socket = new TestSocket();
		ChatModel chatModel = new ChatModel(socket);
		// chatModel����̃R�[���o�b�N��o�^����B�{��GUI���󂯂���́B
		chatModel.setListener(createChatModelListener());
		
		// ��M���b�Z�[�W�̐ݒ�
		receivedText.add("Marshal Mathers;Wassap Dre!");
		receivedText.add("Andre Young;Yo Eminem.");
		receivedText.add("Marshal Mathers;Give me some beats");
		receivedText.add("Andre Young;OK.");
		
		// 1�ڂ̃��b�Z�[�W���󂯂��Ƃ���Marshal Mathers����
		simulateReceivingDataFromServer(); // ���b�Z�[�W��M��͋[
		waitReceiveCompletion();
		assertEquals(1, talkers.size());
		assertEquals("Marshal Mathers", talkers.get(0));
		
		// 2�ڂ̃��b�Z�[�W���󂯂��Ƃ���Marshal Mathers��Andre Young�ɂȂ�
		simulateReceivingDataFromServer(); // ���b�Z�[�W��M��͋[
		waitReceiveCompletion();
		assertEquals(2, talkers.size());
		assertEquals("Marshal Mathers", talkers.get(0));	
		assertEquals("Andre Young", talkers.get(1));	
		
		// 4�܂ł����Ă�Marshal Mathers��Andre Young
		simulateReceivingDataFromServer(); // ���b�Z�[�W��M��͋[
		waitReceiveCompletion();
		simulateReceivingDataFromServer(); // ���b�Z�[�W��M��͋[
		waitReceiveCompletion();
		assertEquals(2, talkers.size());
		assertEquals("Marshal Mathers", talkers.get(0));	
		assertEquals("Andre Young", talkers.get(1));
		
		// Marshal Mathers�̃��b�Z�[�W�����t�B���^����
		chatModel.setFilter("Marshal Mathers", false);
		// Andre Young�̃��b�Z�[�W�����ɂȂ�
		assertEquals("Andre Young : Yo Eminem.\nAndre Young : OK.\n", chatModel.getConversation());
		
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private ChatModelListener createChatModelListener() {
		//�@�g���܂킷�̂ŃC���X�^���X��
		ChatModelListener chatModelListner = new ChatModelListener() {
			@Override
			public void onConversationUpdated(String conversation) {
				synchronized (syncTest) {
					syncTest.notify();
				}
			}

			@Override
			public void onTalkerAdded(String talker) {
				talkers.add(talker);
			}
		};
		return chatModelListner;
	}
	
	protected void simulateReceivingDataFromServer() {
		while ( (receiveThread == null) || !receiveThread.getState().equals(State.WAITING)) {
			try {
				Thread.sleep(10);
				System.out.println("Wait..");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		synchronized(sync) {
			sync.notify();
		}
	}

	protected void waitReceiveCompletion() {
		synchronized (syncTest) {
			try {
				syncTest.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
}
