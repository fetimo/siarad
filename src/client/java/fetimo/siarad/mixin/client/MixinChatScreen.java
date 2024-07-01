package fetimo.siarad.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.screen.Screen;
import fetimo.siarad.CustomChatScreen;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public class MixinChatScreen extends Screen {

	@Unique
	private static final Logger log = LogManager.getLogger(MixinChatScreen.class);

	protected MixinChatScreen(Text title) {
		super(title);
	}

	@Inject(method = "init", at = @At("HEAD"), cancellable = true)
	private void init(CallbackInfo info) {
		MinecraftClient client = MinecraftClient.getInstance();
		CustomChatScreen customChatScreen = new CustomChatScreen(this);
		client.setScreen(customChatScreen);
//		info.cancel(); // Cancel the original initialization
	}
}