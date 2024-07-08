package fetimo.siarad.screens;

import fetimo.siarad.utils.AnimationTimer;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.hud.Hud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static fetimo.siarad.utils.SignedMessageUtils.getMessageId;

public class MessageHud {
    public static final Identifier COMPONENT_ID = Identifier.of("siarad", "chat_history_screen");
    private static final Logger log = LogManager.getLogger(MessageHud.class);

    public MessageHud() {
        Hud.add(COMPONENT_ID, () -> {
            FlowLayout rootComponent = Containers.verticalFlow(Sizing.fill(45), Sizing.fill(45));
            rootComponent.verticalAlignment(VerticalAlignment.BOTTOM);

             rootComponent.child(
                    Containers.verticalFlow(
                            Sizing.fill(), Sizing.content()
                    ).<FlowLayout>configure(flowLayout -> {
                        flowLayout.id("scroll-container");
                    })
            ).positioning(Positioning.relative(0, 70)).margins(Insets.top(5));

             return rootComponent;
        });
    }

    public static void addChatMessage(SignedMessage message) throws InterruptedException {
        FlowLayout chatHud = (FlowLayout) Hud.getComponent(COMPONENT_ID);

        if (chatHud == null) {
            return;
        }

        FlowLayout scrollContainer = chatHud.childById(FlowLayout.class, "scroll-container");

        //* Time
        LocalDateTime localDateTime = LocalDateTime.ofInstant(message.getTimestamp(), ZoneId.systemDefault());

        // Define the DateTimeFormatter.
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        // Format the LocalDateTime to a string.
        String formattedTime = localDateTime.format(formatter);

        //* Player
        // Find the name of the sender by getting all online players and filtering.
        MinecraftClient client = MinecraftClient.getInstance();
        List<ServerPlayerEntity> playerList = client.getServer().getPlayerManager().getPlayerList().stream().toList();

        ServerPlayerEntity matchingPlayer = playerList.stream()
                .filter(player -> player.getUuid().equals(message.getSender()))
                .findFirst()
                .orElse(null);

        Text username = matchingPlayer.getDisplayName();

        // TODO work out what happens for non-player messages.
        // Create the styled text segments
        Text timeText = Text.literal(formattedTime).append(" ").styled(style -> style.withColor(Color.BLUE.rgb()));
        MutableText usernameText = username.copy().append(" ").withColor(Color.GREEN.rgb());
        MutableText messageText = message.getContent().copy().withColor(Color.WHITE.rgb());

        // Combine the text segments into one Text object
        Text combinedText = timeText.copy().append(usernameText).append(messageText);

        // Create a single label component with the combined text
        Component chatLabel = Components.label(combinedText).sizing(Sizing.fill(), Sizing.content()).id("chat-label");

        //* Row
        FlowLayout row = Containers
                .horizontalFlow(Sizing.fill(), Sizing.content());

        row.child(chatLabel);
        row.surface(Surface.VANILLA_TRANSLUCENT);

        scrollContainer.child(row);

        Runnable runnable = () -> {
            int width = row.width();
            LabelComponent label = row.childById(LabelComponent.class, "chat-label");
            label.horizontalSizing(Sizing.fixed(width));
            row.horizontalSizing().animate(300, Easing.LINEAR, Sizing.fill(0)).forwards();
        };
        
        AnimationTimer.start(
                runnable,
                row::remove
        );
    }
}
