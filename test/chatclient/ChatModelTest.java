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

	private String sentText = ""; // 送信された文字列
	private Queue<String> receivedText = new ArrayDeque<>(); // サーバから受信した文字列（疑似的に）
	private Object sync = new Object(); // テスト用ソケットのreadが抜けるタイミングを制御するための待ち
	private Thread receiveThread;
	
	//　テスト用のソケットクラス
	class TestSocket implements ISocket {
		@Override
		public void writeText(String text) {
			//　サーバに向けて送信された文字列を保持する
			sentText = text;
		}

		@Override
		public String readLine() {
			receiveThread = Thread.currentThread();
			// テストコードからの指示されるまで待つ
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
	
	// GUIから送信依頼された文字列をサーバに送信するテスト
	public void testTalk() {
		ISocket socket = new TestSocket();
		ChatModel chatModel = new ChatModel(socket);
		
		chatModel.setMyName("Andre Young");
		// GUIからtalkが呼ばれた
		chatModel.talk("Hello!");
		
		// ソケットにそれがWriteされたらOK
		assertEquals("Andre Young;Hello!", sentText);
		
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		
	// テストを完了を待たせるための同期オブジェクト
	//　待たせないと受信スレッドが受信する前にテストが終わってしまう為
	private Object syncTest = new Object();
	
	// 現在のtalker一覧
	private List<String> talkers = new ArrayList<>();
	
	// サーバから受信したメッセージがログとして表示されるテスト
	public void testReceived() {	
		ISocket socket = new TestSocket();
		ChatModel chatModel = new ChatModel(socket);

		// chatModelからのコールバックを登録する。本来GUIが受けるもの。
		chatModel.setListener(createChatModelListener());
		
		// 受信メッセージの設定
		receivedText.add("Marshal Mathers;Wassap Dre!");
		receivedText.add("Andre Young;Yo Eminem.");
		
		// サーバからの受信データリードを模擬する
		simulateReceivingDataFromServer();
		// 受信完了のコールバックを受けるのを待つ。
		waitReceiveCompletion();
		
		// 正しく受信される。
		assertEquals("Marshal Mathers : Wassap Dre!\n", chatModel.getConversation());	
				
		// サーバからの受信データリードを模擬する
		simulateReceivingDataFromServer();
		
		waitReceiveCompletion();
		// 前回受けたメッセーじ追加されていればOK
		assertEquals("Marshal Mathers : Wassap Dre!\nAndre Young : Yo Eminem.\n", chatModel.getConversation());	
		
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 発言者毎のメッセージのフィルタができる
	 */
	public void testNewTalkerFilter() {
		ISocket socket = new TestSocket();
		ChatModel chatModel = new ChatModel(socket);
		// chatModelからのコールバックを登録する。本来GUIが受けるもの。
		chatModel.setListener(createChatModelListener());
		
		// 受信メッセージの設定
		receivedText.add("Marshal Mathers;Wassap Dre!");
		receivedText.add("Andre Young;Yo Eminem.");
		receivedText.add("Marshal Mathers;Give me some beats");
		receivedText.add("Andre Young;OK.");
		
		// 1個目のメッセージを受けたときはMarshal Mathersだけ
		simulateReceivingDataFromServer(); // メッセージ受信を模擬
		waitReceiveCompletion();
		assertEquals(1, talkers.size());
		assertEquals("Marshal Mathers", talkers.get(0));
		
		// 2個目のメッセージを受けたときはMarshal MathersとAndre Youngになる
		simulateReceivingDataFromServer(); // メッセージ受信を模擬
		waitReceiveCompletion();
		assertEquals(2, talkers.size());
		assertEquals("Marshal Mathers", talkers.get(0));	
		assertEquals("Andre Young", talkers.get(1));	
		
		// 4個までうけてもMarshal MathersとAndre Young
		simulateReceivingDataFromServer(); // メッセージ受信を模擬
		waitReceiveCompletion();
		simulateReceivingDataFromServer(); // メッセージ受信を模擬
		waitReceiveCompletion();
		assertEquals(2, talkers.size());
		assertEquals("Marshal Mathers", talkers.get(0));	
		assertEquals("Andre Young", talkers.get(1));
		
		// Marshal Mathersのメッセージををフィルタする
		chatModel.setFilter("Marshal Mathers", false);
		// Andre Youngのメッセージだけになる
		assertEquals("Andre Young : Yo Eminem.\nAndre Young : OK.\n", chatModel.getConversation());
		
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private ChatModelListener createChatModelListener() {
		//　使いまわすのでインスタンス化
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
