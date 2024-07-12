package fetimo.siarad.screens;

import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import fetimo.siarad.ChatListener;
import fetimo.siarad.utils.CommandChecker;
import fetimo.siarad.utils.CommandSuggester;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.DropdownComponent;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Component.FocusSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MessageInputScreen extends BaseUIModelScreen<FlowLayout> {
    private static final Logger log = LogManager.getLogger(MessageInputScreen.class);
    TextBoxComponent messageInput;
    CommandChecker commandChecker;
    List<Suggestion> previousSuggestions;
    String firstKeyStroke;
    private CommandSuggester commandSuggester;

    public MessageInputScreen(String firstKeyStrokeParam) {
        super(FlowLayout.class, DataSource.asset(Identifier.of("siarad", "chat_hud_screen")));
        commandChecker = new CommandChecker();
        firstKeyStroke = firstKeyStrokeParam;
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        messageInput = rootComponent.childById(TextBoxComponent.class, "message-input");

        messageInput.onChanged().subscribe((text) -> {
            DropdownComponent commandSuggestionsDropdown = rootComponent.childById(
                    DropdownComponent.class,
                    "temp-dropdown"
            );

            if (commandSuggestionsDropdown != null) {
                commandSuggestionsDropdown.remove();
            }

            // Handle non-commands first because they're simpler.
            if (!text.trim().startsWith("/")) {
                messageInput.setEditableColor(Color.WHITE.rgb());
                // Reset suggestion just in case.
                messageInput.setSuggestion(null);
            } else {
                messageInput.setEditableColor(Color.GREEN.rgb());
                DropdownComponent x = DropdownComponent.openContextMenu(
                        this,
                        rootComponent,
                        FlowLayout::child,
                        0,
                        40,
                        (dropdown) -> {
                            dropdown.id("temp-dropdown");
                        }
                );

                // Handle rendering the suggestions in the dropdown.
                CompletableFuture<Suggestions> future = this.commandSuggester.refresh(text.toLowerCase(), false);
                future.thenAccept(suggestions -> {
                    Suggestion firstSuggestion = suggestions.getList().getFirst();
                    previousSuggestions = suggestions.getList();

                    // -1 because we need to account for the '/' suffix.
                    if (messageInput.getText().trim().length() - 1 < firstSuggestion.getText().length()) {
                        log.info("setSuggestion");
                        messageInput.setSuggestion(
                                firstSuggestion.getText().substring(
                                        text.trim().length() - 1
                                )
                        );
                    } else {
                        log.info("unsetSuggestion");
                        messageInput.setSuggestion(null);
                    }

                    List<Suggestion> fullList = suggestions.getList();
                    List<Suggestion> relevantList = fullList.subList(0, Math.min(fullList.size(), 5));

                    for (Suggestion suggestion : relevantList) {
                        // TODO need to break this down into words.
                        x.button(Text.of(suggestion.getText()), (foo) -> {
                            messageInput.setSuggestion(null);
                            messageInput.setText("/" + suggestion.getText());
                        });
                    }
                });
            }
        });

        messageInput.keyPress().subscribe((keyCode, scale, modifier) -> {
            log.info("main input received keypress: " + keyCode + " " + KeyEvent.getKeyText(keyCode));
            String letter = KeyEvent.getKeyText(keyCode);
            boolean preemptive = letter.length() == 1;
            final String text = messageInput.getText() + (preemptive ? letter.toLowerCase() : "");

            if (keyCode == GLFW.GLFW_KEY_ENTER) {
                if (text.trim().isEmpty()) {
                    return false;
                }

                // Process the message for sending.
                // Is this a command in disguise?
                boolean hasCommandWithoutSlash = commandChecker.check(text);

                ChatListener.sendChatMessage(hasCommandWithoutSlash ? "/" + text : text);
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

            // Navigate to suggestion dropdown.
            if (keyCode == GLFW.GLFW_KEY_DOWN || keyCode == GLFW.GLFW_KEY_UP) {
                DropdownComponent dropdown = rootComponent.childById(
                        DropdownComponent.class,
                        "temp-dropdown"
                );

                if (dropdown == null) {
                    return false;
                }

                ArrayList<Component> components = new ArrayList<>();
                dropdown.collectDescendants(components);
                Component entryToFocus = components.getLast();
                dropdown.focusHandler().focus(entryToFocus, FocusSource.KEYBOARD_CYCLE);
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
        this.commandSuggester = new CommandSuggester(
                client,
                messageInput,
                client.textRenderer,
                false,
                false);

        // This must be after the command suggester is initialised.
        // If '/' is the initiator we want to show command usage.
        if (firstKeyStroke != null) {
            messageInput.setText(firstKeyStroke);
            firstKeyStroke = null;
        }
    }
}