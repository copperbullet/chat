package chatclient;

public class ChatMessage {
	public ChatMessage(String talker2, String message2) {
		this.talker = talker2;
		this.message = message2;
	}
	private String talker;
	private String message;
	public String getTalker() {
		return talker;
	}
	public String getMessage() {
		return message;
	}
	
}
