package fetimo.siarad.screens;

import fetimo.siarad.ChatListener;
import fetimo.siarad.utils.CommandChecker;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.Component.FocusSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

public class MessageInputScreen extends BaseUIModelScreen<FlowLayout> {
    private ChatInputSuggestor commandSuggestor;
    private static final Logger log = LogManager.getLogger(MessageInputScreen.class);
    TextBoxComponent messageInput;
    CommandChecker commandChecker;

    public MessageInputScreen() {
        super(FlowLayout.class, DataSource.asset(Identifier.of("siarad", "chat_hud_screen")));
        commandChecker = new CommandChecker();
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        messageInput = rootComponent.childById(TextBoxComponent.class, "message-input");

        messageInput.keyPress().subscribe((keyCode, scale, modifier) -> {
           String text = messageInput.getText();

            // Make text green for commands or white for prose.
            if (text.trim().startsWith("/")) {
                this.commandSuggestor.refresh();
                commandSuggestor.show(true);
                commandSuggestor.setWindowActive(true);
                log.info("open? " + commandSuggestor.isOpen());
                messageInput.setEditableColor(Color.GREEN.rgb());
            } else {
                messageInput.setEditableColor(Color.WHITE.rgb());
            }

            if (keyCode == GLFW.GLFW_KEY_TAB) {
                // TODO
                commandSuggestor.show(false);
                return false;
            }

           if (keyCode == GLFW.GLFW_KEY_ENTER) {
               if (text.trim().isEmpty()) {
                   return false;
               }

               // Process the message for sending.
               // Is this a command in disguise?
               boolean hasCommandWithoutSlash = commandChecker.check(text);
               if (hasCommandWithoutSlash) {
                   text = "/" + text;
               }

               ChatListener.addChatMessage(text);
               // Reset input.
               messageInput.setText("");

               // Close chat input.
               if (this.client != null) {
                   this.client.setScreen(null);
               }
           }

           if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
               if (this.client != null) {
                   this.client.setScreen(null);
               }
           }

           return true;
        });
    }

    @Override
    protected void init() {
        super.init();

        // Focus on the input as soon as the screen initialises.
        assert messageInput.focusHandler() != null;
        messageInput.focusHandler().focus(messageInput, FocusSource.MOUSE_CLICK);




        // Setup command completion
        MinecraftClient client = MinecraftClient.getInstance();
        this.commandSuggestor =
                new ChatInputSuggestor(
                        client,
                        this,
                        messageInput,
                        client.textRenderer,
                        false,
                        false,
                        0,
                        10,
                        false,
                        0xFF0000);

        commandSuggestor.show(true);

    }
}