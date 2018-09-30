package chatclient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import share.ISocket;

/**
 * @author a1199022
 *
 */
public class ChatModel {
	private ISocket socket; // �\�P�b�g�B���̃N���X�͂��̃\�P�b�g���{�����e�X�g�p���͒m��Ȃ�
	private List<ChatMessage> messageLog = new ArrayList<>(); // �ߋ��Ɏ�M�������b�Z�[�W�̃��O
	private ChatModelListener listener;
	private String myName;
	private Map<String, Boolean> talkerFilter = new HashMap<>();
	
	public ChatModel(final ISocket socket) {
		this.socket = socket;

		// �T�[�o����̎�M�X���b�h
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					String line;
					// �T�[�o����̎�M��҂B
					while ( (line = socket.readLine()) != null ) {
						// ��M������ߋ��̃��O�ɒǉ�����
						appendChatMessage(line);
						
						// ���C���X���b�h��GUI�ɒʒm
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								fireConversationUpdate();
							}
						});
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		thread.start();
	}
	
	
	protected void appendChatMessage(String line) {
		int pos = line.indexOf(";");
		String talker = line.substring(0, pos);
		String message = line.substring(pos+1, line.length());
		this.messageLog.add(new ChatMessage(talker, message));
		
		if (!this.talkerFilter.keySet().contains(talker)) {
			listener.onTalkerAdded(talker);
			this.talkerFilter.put(talker, true);
		}
		
	}

	public void talk(String text) {
		this.socket.writeText(this.myName + ";" + text);
	}

	public String getConversation() {
		String ret = "";
		for (ChatMessage message : this.messageLog) {
			if (!this.talkerFilter.get(message.getTalker())) {
				continue;
			}
			ret += message.getTalker() + " : " + message.getMessage() + "\n";
		}
		return ret;
	}

	public void setListener(ChatModelListener chartModelListener) {
		this.listener = chartModelListener;
	}

	public void setMyName(String text) {
		this.myName = text;
	}

	public void setFilter(String name, boolean selected) {
		this.talkerFilter.put(name, selected);
		fireConversationUpdate();
	}

	protected void fireConversationUpdate() {
		listener.onConversationUpdated(getConversation());
	}


	public void exit() {
		try {
			this.socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public String getMyName() {
		return this.myName;
	}
}
