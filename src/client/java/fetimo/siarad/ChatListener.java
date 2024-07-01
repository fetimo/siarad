package fetimo.siarad;

import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ChatListener {
    public static final Logger log = LoggerFactory.getLogger(ChatListener.class);
    private static final List<String> chatMessages = new ArrayList<>();

    public static void register() {
        // Listen to chat messages
        ServerMessageEvents.CHAT_MESSAGE.register((signedMessage, playerEntity, parameters) -> {
            String message = signedMessage.getContent().getString();
            log.info(message);
            synchronized (chatMessages) {
                chatMessages.add(message);
                if (chatMessages.size() > 10) { // Limit the number of stored messages
                    chatMessages.removeFirst();
                }
            }
        });

    }

    public static List<String> getChatMessages() {
        synchronized (chatMessages) {
            return new ArrayList<>(chatMessages);
        }
    }

    public static String addChatMessage(String message) {
        chatMessages.add(message);
        return message;
    }
}