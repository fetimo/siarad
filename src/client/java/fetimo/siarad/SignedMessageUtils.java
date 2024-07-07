package fetimo.siarad;

import java.time.Instant;
import java.util.UUID;
import net.minecraft.network.message.SignedMessage;

public class SignedMessageUtils {

    // Messages don't have an ID, so we make one out of existing fields. Should be idempotent.
    public static String getMessageId(SignedMessage message) {
        UUID senderUuid = message.getSender();
        Instant timestamp = message.getTimestamp();
        long salt = message.getSalt();

        return senderUuid.toString() + "-" + timestamp.toEpochMilli() + "-" + salt;
    }
}