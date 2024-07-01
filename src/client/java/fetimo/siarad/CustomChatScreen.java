package fetimo.siarad;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

public class CustomChatScreen extends Screen {
    private static final Logger log = LogManager.getLogger(CustomChatScreen.class);
    private TextFieldWidget inputField;

    public CustomChatScreen(Screen parent) {
        super(Text.of("Siarad Chat"));
    }

    @Override
    protected void init() {
        int width = 200;
        int height = 20;
        int x = (this.width - width) / 2;
        int y = (this.height - height) / 2;

        inputField = new TextFieldWidget(this.textRenderer, x, y, width, height, Text.of(""));
        inputField.setMaxLength(256);

        this.addSelectableChild(inputField);
        this.setInitialFocus(inputField);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);


        int labelX = inputField.getX();
        int labelY = inputField.getY() - 15;
        context.drawTextWithShadow(this.textRenderer, Text.of("Enter your message:"), labelX, labelY, 0xFFFFFF);

        inputField.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean charTyped(char chr, int keyCode) {
        if (inputField.charTyped(chr, keyCode)) {
            return true;
        }
        return super.charTyped(chr, keyCode);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER) { // Enter key
            String text = inputField.getText();
            ChatListener.addChatMessage(text);

            // Reset input.
            inputField.setText("");

            // Return to main screen.
            this.client.setScreen(null);
            return true;
        }
        if (inputField.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (inputField.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}