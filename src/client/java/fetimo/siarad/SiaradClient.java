package fetimo.siarad;

import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.hud.Hud;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static fetimo.siarad.SignedMessageUtils.getMessageId;

public class SiaradClient implements ClientModInitializer {
	private static final Logger log = LogManager.getLogger(SiaradClient.class);
	public static final Identifier COMPONENT_ID = Identifier.of("siarad", "chat_history_screen");

	@Override
	public void onInitializeClient() {
		ChatListener.register();

		// Add recent messages HUD.
		Hud.add(COMPONENT_ID, () -> Containers.verticalFlow(Sizing.content(), Sizing.content())
				.padding(Insets.of(10))
				.surface(Surface.flat(0x77000000))
				.positioning(Positioning.relative(0, 70))
				.margins(Insets.top(5)));
	}

	public static void addChatMessage(SignedMessage message) {
		FlowLayout chatHud = (FlowLayout) Hud.getComponent(COMPONENT_ID);

		if (chatHud == null) {
			return;
		}

		// Add a recent message to the HUD.
        LocalDateTime localDateTime = LocalDateTime.ofInstant(message.getTimestamp(), ZoneId.systemDefault());

        // Define the DateTimeFormatter.
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        // Format the LocalDateTime to a string.
        String formattedTime = localDateTime.format(formatter);

        Component timestampComponent = Components.label(Text.of(formattedTime))
                .color(Color.BLUE)
                .margins(Insets.both(2, 2));

		// Find the name of the sender by getting all online players and filtering.
		MinecraftClient client = MinecraftClient.getInstance();
		List<ServerPlayerEntity> playerList = client.getServer().getPlayerManager().getPlayerList().stream().toList();

		ServerPlayerEntity matchingPlayer = playerList.stream()
				.filter(player -> player.getUuid().equals(message.getSender()))
				.findFirst()
				.orElse(null);

		// TODO work out what happens for non-player messages.
		Component playerComponent = Components.label(Text.of(matchingPlayer.getDisplayName()))
                .color(Color.GREEN)
                .margins(Insets.both(2, 2));
        Component textComponent = Components.label(message.getContent())
                            .margins(Insets.both(2, 2));
        FlowLayout row = Containers
				.horizontalFlow(Sizing.content(), Sizing.content());

		// Messages don't have an ID, so we make one out of existing fields. Should be idempotent.
		String rowId = getMessageId(message);
		log.info("rowId: {}", rowId);
		row.id(rowId);

		// Add all the sections of the message to the row.
        row.child(timestampComponent);
        row.child(playerComponent);
        row.child(textComponent);

        chatHud.child(row);

        // TODO Find and prune old messages. Need to work out state management.

	}
}