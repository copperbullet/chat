package chatclient;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import share.RealSocket;

public class ChatClientUi extends JFrame {
	public static void main(String[] args) throws UnknownHostException, IOException {
		
		new ChatClientUi(new ChatModel(new RealSocket(new Socket("localhost", 8001)))).setVisible(true);
	}
	
	public ChatClientUi(final ChatModel chatModel) {
		this.setSize(new Dimension(600,400));
		this.setLayout(new FlowLayout());
		
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent arg0) {
				chatModel.exit();
			}
		});
		//�@�����̖��O����͂���e�L�X�g�t�B�[���h
		final JTextField myname = new JTextField(chatModel.getMyName());
		myname.setPreferredSize(new Dimension(300, 24));
		this.add(myname);
		myname.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				// �{�^���������ꂽ��Ă�
				chatModel.setMyName(myname.getText());
			}
		});
		
		//�@���M���錾�t����͂���e�L�X�g�t�B�[���h
		final JTextField words = new JTextField("");
		words.setPreferredSize(new Dimension(300, 24));
		this.add(words);
		
		// ���M�{�^��
		JButton talkButton = new JButton("Talk");
		talkButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// �{�^���������ꂽ��Ă�
				chatModel.talk(words.getText());
			}
		});
		this.add(talkButton);
		
		// ��b�̗�����\������e�L�X�g�G���A
		final JTextArea conversation = new JTextArea("");
		conversation.setPreferredSize(new Dimension(400, 200));
		// ���݂̗�����\��
		conversation.setText(chatModel.getConversation());
		this.add(conversation);
		
		final JPanel talkers = new JPanel();
		this.add(talkers);
		
		//�@�f�[�^����M�����Ƃ��ɌĂ΂��
		chatModel.setListener(new ChatModelListener() {
			public void onConversationUpdated(String text) {
				conversation.setText(text);
			}

			@Override
			public void onTalkerAdded(String talker) {
				final JCheckBox checkName = new JCheckBox(talker, true);
				talkers.add(checkName);
				checkName.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						chatModel.setFilter(checkName.getText(), checkName.isSelected());
					}
				});
			}

		});		
	}
}
