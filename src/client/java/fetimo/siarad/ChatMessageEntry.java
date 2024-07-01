package fetimo.siarad;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.Text;

import java.util.Collections;
import java.util.List;

public class ChatMessageEntry extends ElementListWidget.Entry<ChatMessageEntry> {
    private final Text text;
    protected int indent;

    public ChatMessageEntry(Text message, int indent) {
        this.text = message;
        this.indent = indent;
    }

    public ChatMessageEntry(Text text) {
        this(text, 0);
    }

    @Override
    public void render(
            DrawContext DrawContext,
            int index,
            int y,
            int x,
            int itemWidth,
            int itemHeight,
            int mouseX,
            int mouseY,
            boolean isSelected,
            float delta
    ) {

//        DrawContext.drawTextWithShadow(textRenderer, text, x + indent, y, 0xAAAAAA);
    }

    @Override
    public List<? extends Element> children() {
        return Collections.emptyList();
    }

    @Override
    public List<? extends Selectable> selectableChildren() {
        return Collections.emptyList();
    }
}