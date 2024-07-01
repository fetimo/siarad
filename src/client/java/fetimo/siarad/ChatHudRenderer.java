package fetimo.siarad;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.List;

public class ChatHudRenderer {
    private final MinecraftClient client;
    private int scrollOffset = 0;
    private static final int MESSAGE_HEIGHT = 10; // Height of each chat message
    private static final int MAX_MESSAGES = 10; // Maximum number of messages to display

    public ChatHudRenderer(MinecraftClient client) {
        this.client = client;
    }

    public void render(DrawContext context) {
        List<String> messages = ChatListener.getChatMessages();
        int startY = 10; // Starting Y position for messages
        int startX = 10; // Starting X position for messages

        int maxScroll = Math.max(0, messages.size() - MAX_MESSAGES);
        scrollOffset = Math.min(scrollOffset, maxScroll);

        for (int i = 0; i < MAX_MESSAGES; i++) {
            int messageIndex = i + scrollOffset;
            if (messageIndex >= messages.size()) break;

            String message = messages.get(messageIndex);
            context.drawTextWithShadow(this.client.textRenderer, Text.of(message), startX, startY + (i * MESSAGE_HEIGHT), 0xFFFFFF);
        }
    }

    public void scrollUp() {
        if (scrollOffset > 0) {
            scrollOffset--;
        }
    }

    public void scrollDown() {
        List<String> messages = ChatListener.getChatMessages();
        if (scrollOffset < messages.size() - MAX_MESSAGES) {
            scrollOffset++;
        }
    }

    public void handleMouseScroll(double deltaY) {
        // deltaY is positive when scrolling up and negative when scrolling down
        if (deltaY < 0 && scrollOffset > 0) {
            scrollOffset--; // Scroll up
        } else if (deltaY > 0) {
            List<String> messages = ChatListener.getChatMessages();
            if (scrollOffset < messages.size() - MAX_MESSAGES) {
                scrollOffset++; // Scroll down
            }
        }
    }
}
