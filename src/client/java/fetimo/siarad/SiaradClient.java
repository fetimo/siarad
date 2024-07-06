package fetimo.siarad;

import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.hud.Hud;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SiaradClient implements ClientModInitializer {
	private static final Logger log = LogManager.getLogger(SiaradClient.class);
	public static final Identifier COMPONENT_ID = Identifier.of("siarad", "chat_history_screen");

	@Override
	public void onInitializeClient() {
		ChatListener.register();

		// Add recent messages HUD.
		Component outer = Containers.verticalFlow(Sizing.content(), Sizing.content())
			.padding(Insets.of(10))
			.surface(Surface.flat(0x77000000))
			.positioning(Positioning.relative(5, 70))
			.margins(Insets.top(5));

		Hud.add(COMPONENT_ID, () -> outer);
	}

	public static void addChatMessage(String message) {
		// TODO this will need to parse an actual message.
		FlowLayout chatHud = (FlowLayout) Hud.getComponent(COMPONENT_ID);

		// Add a recent message to the HUD.
		if (chatHud != null) {
			Component textComponent = Components.label(Text.of(message))
					.color(Color.WHITE)
					.margins(Insets.vertical(2)); // Add some spacing between messages

			chatHud.child(textComponent);
		}
	}
}