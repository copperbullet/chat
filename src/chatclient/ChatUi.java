package chatclient;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ChatUi extends JFrame {
	public static void main(String[] args) {
		
		new ChatUi(new ChatModel(new RealSocket("localhost", 8001))).setVisible(true);
	}
	
	public ChatUi(final ChatModel chatModel) {
		this.setSize(new Dimension(600,400));
		this.setLayout(new FlowLayout());
		
		//�@�����̖��O����͂���e�L�X�g�t�B�[���h
		final JTextField myname = new JTextField("Your Name");
		myname.setPreferredSize(new Dimension(300, 24));
		this.add(myname);
		JButton nameButton = new JButton("Set Name");
		nameButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// �{�^���������ꂽ��Ă�
				chatModel.setName(myname.getText());
			}
		});
		this.add(nameButton);
		
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
