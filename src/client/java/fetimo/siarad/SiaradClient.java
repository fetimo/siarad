package fetimo.siarad;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SiaradClient implements ClientModInitializer {
	private static final Logger log = LogManager.getLogger(SiaradClient.class);
	private ChatHudRenderer chatHudRenderer;

	@Override
	public void onInitializeClient() {
		ChatListener.register();
		// Initialize the scrollable chat HUD
		MinecraftClient client = MinecraftClient.getInstance();
		chatHudRenderer = new ChatHudRenderer(client);

		ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
	}

	private void onClientTick(MinecraftClient client) {
		if (client.player != null && client.world != null) {
			// Check for mouse scroll events
			double deltaY = client.mouse.getY();
			log.info("deltaY: " + deltaY);
			if (deltaY != 0) {
				chatHudRenderer.handleMouseScroll(deltaY);
			}
		}
	}
}