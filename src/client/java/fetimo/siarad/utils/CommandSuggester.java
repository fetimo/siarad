package fetimo.siarad.utils;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
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
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class CommandSuggester {
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("(\\s+)");
    private static final Style ERROR_STYLE = Style.EMPTY.withColor(Formatting.RED);
    private static final Style INFO_STYLE = Style.EMPTY.withColor(Formatting.GRAY);
    private static final List<Style> HIGHLIGHT_STYLES = Stream.of(
                    Formatting.AQUA, Formatting.YELLOW, Formatting.GREEN, Formatting.LIGHT_PURPLE, Formatting.GOLD
            )
            .map(Style.EMPTY::withColor)
            .collect(ImmutableList.toImmutableList());
    public final List<OrderedText> messages = Lists.newArrayList();
    final MinecraftClient client;
    final TextFieldWidget textField;
    final TextRenderer textRenderer;
    private final boolean slashOptional;
    private final boolean suggestingWhenEmpty;
    public CompletableFuture<Suggestions> pendingSuggestions;
    public ParseResults<CommandSource> parse;
    boolean completingSuggestions;

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
        this.textField.setRenderTextProvider(this::provideRenderText);
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

    // The highlight function takes parsed command results and the original command string, then highlights different
    // parts of the command based on the arguments and their positions. It uses progressively darker shades of green
    // for each argument and gray for non-argument parts.
    // Any remaining unparsed part of the command is highlighted in red.
    private static OrderedText highlight(ParseResults<CommandSource> parse, String original, int firstCharacterIndex) {
        List<OrderedText> list = Lists.newArrayList();
        int currentPos = 0;

        int initialRed = 0x00;
        int initialGreen = 0xFF;
        int initialBlue = 0x00;
        int colorStep = 40;

        CommandContextBuilder<CommandSource> commandContextBuilder = parse.getContext().getLastChild();
        int argumentIndex = 0;

        for (ParsedArgument<CommandSource, ?> parsedArgument : commandContextBuilder.getArguments().values()) {
            int argStart = Math.max(parsedArgument.getRange().getStart() - firstCharacterIndex, 0);
            if (argStart >= original.length()) {
                break;
            }

            int argEnd = Math.min(parsedArgument.getRange().getEnd() - firstCharacterIndex, original.length());
            if (argEnd > 0) {
                list.add(OrderedText.styledForwardsVisitedString(original.substring(currentPos, argStart), Style.EMPTY.withColor(Formatting.OBFUSCATED)));

                int red = Math.max(0, initialRed - (colorStep * argumentIndex));
                int green = Math.max(0, initialGreen - (colorStep * argumentIndex));
                int blue = Math.max(0, initialBlue - (colorStep * argumentIndex));
                TextColor color = TextColor.fromRgb((red << 16) | (green << 8) | blue);

                list.add(OrderedText.styledForwardsVisitedString(original.substring(argStart, argEnd), Style.EMPTY.withColor(color)));
                currentPos = argEnd;
                argumentIndex++;
            }
        }

        if (parse.getReader().canRead()) {
            int remainingStart = Math.max(parse.getReader().getCursor() - firstCharacterIndex, 0);
            if (remainingStart < original.length()) {
                int remainingEnd = Math.min(remainingStart + parse.getReader().getRemainingLength(), original.length());
                list.add(OrderedText.styledForwardsVisitedString(original.substring(currentPos, remainingStart), Style.EMPTY.withColor(Formatting.DARK_AQUA)));
                list.add(OrderedText.styledForwardsVisitedString(original.substring(remainingStart, remainingEnd), Style.EMPTY.withColor(Formatting.RED)));
                currentPos = remainingEnd;
            }
        }

        list.add(OrderedText.styledForwardsVisitedString(original.substring(currentPos), Style.EMPTY.withColor(Formatting.DARK_PURPLE)));
        return OrderedText.concat(list);
    }

    private OrderedText provideRenderText(String original, int firstCharacterIndex) {
        return this.parse != null ? highlight(this.parse, original, firstCharacterIndex) : OrderedText.styledForwardsVisitedString(original, Style.EMPTY);
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
