package fetimo.siarad;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ChatListener {
    public static final Logger log = LoggerFactory.getLogger(ChatListener.class);
    private static final List<SignedMessage> chatMessages = new ArrayList<>();

    public static void register() {
        // Listen for chat messages to render.
        ClientReceiveMessageEvents.CHAT.register((text, signedMessage, gameProfile, parameters, time) -> {
            if (signedMessage == null) {
                // I don't know when this would happen but just in case.
                return;
            }

            try {
                SiaradClient.addChatMessage(signedMessage);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // Thread-safe sync! This means that messages can't be added out of order.
            synchronized (chatMessages) {
                chatMessages.add(signedMessage);
                if (chatMessages.size() > 10) { // Limit the number of stored messages
                    chatMessages.removeFirst();
                }
            }
        });
    }

    public static List<SignedMessage> getChatMessages() {
        synchronized (chatMessages) {
            return new ArrayList<>(chatMessages);
        }
    }

    public static void addChatMessage(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.networkHandler.sendChatMessage(message);
        }
    }
}