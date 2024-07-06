package fetimo.siarad;

import java.time.Instant;
import java.util.UUID;
import net.minecraft.network.message.SignedMessage;

public class SignedMessageUtils {

    public static String getMessageId(SignedMessage message) {
        UUID senderUuid = message.getSender();
        Instant timestamp = message.getTimestamp();
        long salt = message.getSalt();

        return senderUuid.toString() + "-" + timestamp.toEpochMilli() + "-" + salt;
    }
}