package fetimo.siarad;

import fetimo.siarad.screens.MessageHud;
import net.fabricmc.api.ClientModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SiaradClient implements ClientModInitializer {
	private static final Logger log = LogManager.getLogger(SiaradClient.class);

	@Override
	public void onInitializeClient() {
		ChatListener.register();

		new MessageHud();
	}
}