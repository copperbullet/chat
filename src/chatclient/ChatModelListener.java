package chatclient;

public interface ChatModelListener {
	void onConversationUpdated(String conversation);

	void onTalkerAdded(String talker);
}
