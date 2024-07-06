package fetimo.siarad.screens;

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.util.Identifier;

public class ChatHistoryScreen extends BaseUIModelScreen<FlowLayout> {

    public ChatHistoryScreen() {
        super(FlowLayout.class, DataSource.asset(Identifier.of("siarad", "chat_history_screen")));
    }

    @Override
    protected void build(FlowLayout rootComponent) {

    }
}
