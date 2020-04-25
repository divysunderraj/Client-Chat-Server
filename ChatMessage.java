import java.io.Serializable;

/**
 * This class helps control the properties of sending a message
 *
 * @author Adhi Ramkumar & Divy Sunderraj
 * @version April 25 2020
 */
final class ChatMessage implements Serializable {
    private static final long serialVersionUID = 6898543889087L;
    // Here is where you should implement the chat message object.
    // Variables, Constructors, Methods, etc.
    private String message;
    private int type;
    private String recipient;

    public ChatMessage() {
    }

    public ChatMessage(String message, int type, String recipient) {
        this.message = message;
        this.type = type;
        this.recipient = recipient;
    }


    public String getMessage() {
        return message;
    }

    public int getType() {
        return type;
    }

    public String getRecipient() {
        return recipient;
    }
}
