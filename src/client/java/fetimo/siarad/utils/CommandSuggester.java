package fetimo.siarad.utils;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.SuggestionContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandSuggester {
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("(\\s+)");
    public final List<OrderedText> messages = Lists.newArrayList();
    final MinecraftClient client;
    final TextFieldWidget textField;
    final TextRenderer textRenderer;
    private final boolean slashOptional;
    private final boolean suggestingWhenEmpty;
    public CompletableFuture<Suggestions> pendingSuggestions;
    boolean completingSuggestions;
    private ParseResults<CommandSource> parse;

    public CommandSuggester(
            MinecraftClient client,
            TextFieldWidget textField,
            TextRenderer textRenderer,
            boolean slashOptional,
            boolean suggestingWhenEmpty
    ) {
        this.client = client;
        this.textField = textField;
        this.textRenderer = textRenderer;
        this.slashOptional = slashOptional;
        this.suggestingWhenEmpty = suggestingWhenEmpty;
    }
    
    private static OrderedText formatException(CommandSyntaxException exception) {
        Text text = Texts.toText(exception.getRawMessage());
        String string = exception.getContext();
        return string == null
                ? text.asOrderedText()
                : Text.translatable("command.context.parse_error", text, exception.getCursor(), string).asOrderedText();
    }

    private static int getStartOfCurrentWord(String input) {
        if (Strings.isNullOrEmpty(input)) {
            return 0;
        } else {
            int i = 0;
            Matcher matcher = WHITESPACE_PATTERN.matcher(input);

            while (matcher.find()) {
                i = matcher.end();
            }

            return i;
        }
    }

    public void showCommandSuggestions() {
        boolean bl = false;
        if (this.textField.getCursor() == this.textField.getText().length()) {
            if (this.pendingSuggestions.join().isEmpty() && !this.parse.getExceptions().isEmpty()) {
                int i = 0;

                for (Map.Entry<CommandNode<CommandSource>, CommandSyntaxException> entry : this.parse.getExceptions().entrySet()) {
                    CommandSyntaxException commandSyntaxException = entry.getValue();
                    if (commandSyntaxException.getType() == CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect()) {
                        i++;
                    } else {
                        this.messages.add(formatException(commandSyntaxException));
                    }
                }

                if (i > 0) {
                    this.messages.add(formatException(CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().create()));
                }
            } else if (this.parse.getReader().canRead()) {
                bl = true;
            }
        }

        if (this.messages.isEmpty() && !this.showUsages(Formatting.GRAY) && bl) {
            this.messages.add(formatException(CommandManager.getException(this.parse)));
        }
    }

    public CompletableFuture<Suggestions> refresh(String string, Boolean preemptive) {
        if (this.parse != null && !this.parse.getReader().getString().equals(string)) {
            this.parse = null;
        }

        if (!this.completingSuggestions) {
            this.textField.setSuggestion(null);
        }

        this.messages.clear();
        StringReader stringReader = new StringReader(string);
        boolean bl = stringReader.canRead() && stringReader.peek() == '/';
        if (bl) {
            stringReader.skip();
        }

        boolean bl2 = this.slashOptional || bl;
        int i = this.textField.getCursor() + (preemptive ? 1 : 0);
        if (bl2) {
            CommandDispatcher<CommandSource> commandDispatcher = this.client.player.networkHandler.getCommandDispatcher();
            if (this.parse == null) {
                this.parse = commandDispatcher.parse(stringReader, this.client.player.networkHandler.getCommandSource());
            }

            int j = this.suggestingWhenEmpty ? stringReader.getCursor() : 1;
            if (i >= j && !this.completingSuggestions) {
                this.pendingSuggestions = commandDispatcher.getCompletionSuggestions(this.parse, i);
                this.pendingSuggestions.thenRun(() -> {
                    if (this.pendingSuggestions.isDone()) {
                        this.showCommandSuggestions();
                    }
                });
                return this.pendingSuggestions;  // Return the CompletableFuture
            }
        } else {
            String string2 = string.substring(0, i);
            int j = getStartOfCurrentWord(string2);
            Collection<String> collection = this.client.player.networkHandler.getCommandSource().getChatSuggestions();
            this.pendingSuggestions = CommandSource.suggestMatching(collection, new SuggestionsBuilder(string2, j));
        }
        return this.pendingSuggestions;  // Return the CompletableFuture
    }

    public boolean showUsages(Formatting formatting) {
        CommandContextBuilder<CommandSource> commandContextBuilder = this.parse.getContext();
        SuggestionContext<CommandSource> suggestionContext = commandContextBuilder.findSuggestionContext(this.textField.getCursor());
        Map<CommandNode<CommandSource>, String> map = this.client
                .player
                .networkHandler
                .getCommandDispatcher()
                .getSmartUsage(suggestionContext.parent, this.client.player.networkHandler.getCommandSource());
        List<OrderedText> list = Lists.newArrayList();
        int i = 0;
        Style style = Style.EMPTY.withColor(formatting);

        for (Map.Entry<CommandNode<CommandSource>, String> entry : map.entrySet()) {
            if (!(entry.getKey() instanceof LiteralCommandNode)) {
                list.add(OrderedText.styledForwardsVisitedString(entry.getValue(), style));
                i = Math.max(i, this.textRenderer.getWidth(entry.getValue()));
            }
        }

        if (!list.isEmpty()) {
            this.messages.addAll(list);
            return true;
        } else {
            return false;
        }
    }
}
