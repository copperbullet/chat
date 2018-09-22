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
		
		//　自分の名前を入力するテキストフィールド
		final JTextField myname = new JTextField("Your Name");
		myname.setPreferredSize(new Dimension(300, 24));
		this.add(myname);
		JButton nameButton = new JButton("Set Name");
		nameButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// ボタンが押されたら呼ぶ
				chatModel.setName(myname.getText());
			}
		});
		this.add(nameButton);
		
		//　送信する言葉を入力するテキストフィールド
		final JTextField words = new JTextField("");
		words.setPreferredSize(new Dimension(300, 24));
		this.add(words);
		
		// 送信ボタン
		JButton talkButton = new JButton("Talk");
		talkButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// ボタンが押されたら呼ぶ
				chatModel.talk(words.getText());
			}
		});
		this.add(talkButton);
		
		// 会話の履歴を表示するテキストエリア
		final JTextArea conversation = new JTextArea("");
		conversation.setPreferredSize(new Dimension(400, 200));
		// 現在の履歴を表示
		conversation.setText(chatModel.getConversation());
		this.add(conversation);
		
		final JPanel talkers = new JPanel();
		this.add(talkers);
		
		//　データを受信したときに呼ばれる
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
