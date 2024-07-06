package fetimo.siarad.mixin.client;

import fetimo.siarad.screens.MessageInputScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {
    // This is super important because it shows our custom input when the game would show the normal one.
    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void onSetScreen(Screen screen, CallbackInfo info) {
        if (screen instanceof ChatScreen) {
            MinecraftClient client = (MinecraftClient) (Object) this;
            client.setScreen(new MessageInputScreen());
            info.cancel();
        }
    }
}