package fetimo.siarad.mixin.client;

import fetimo.siarad.ChatHudRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public class MixinChatHud {

    private static final Logger log = LogManager.getLogger(MixinChatHud.class);
    @Shadow
    @Final
    private MinecraftClient client;

    @Unique
    private ChatHudRenderer customChatHud;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(MinecraftClient client, CallbackInfo info) {
        customChatHud = new ChatHudRenderer(client);

    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(DrawContext context, int currentTick, int mouseX, int mouseY, boolean focused, CallbackInfo ci) {
        if (customChatHud != null) {
            customChatHud.render(context);
            ci.cancel();
        }
    }

}
