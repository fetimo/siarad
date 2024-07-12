package fetimo.siarad.mixin.client;

import fetimo.siarad.screens.MessageInputScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {
    @Unique
    private String firstKeyStroke = "";

    // This is super important because it shows our custom input when the game would show the normal one.
    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void onSetScreen(Screen screen, CallbackInfo info) {
        if (screen instanceof ChatScreen) {
            MinecraftClient client = (MinecraftClient) (Object) this;

            client.setScreen(new MessageInputScreen(firstKeyStroke));
            firstKeyStroke = "";
            info.cancel();
        }
    }

    // This captures the key which triggered the chat screen to open.
    // Important for commands.
    @Inject(method = "openChatScreen", at = @At("HEAD"))
    private void openChatScreen(String text, CallbackInfo ci) {
        firstKeyStroke = text;
    }
}