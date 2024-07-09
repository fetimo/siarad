package fetimo.siarad.utils;

import java.util.HashSet;
import java.util.Set;

public class CommandChecker {
    Set<String> commands = new HashSet<>();

    public CommandChecker() {
        commands.add("msg");
        commands.add("co ");
    }

    public boolean check(String text) {
        // Trim the text to remove leading and trailing spaces
        String trimmedText = text.trim();

        // Check if the text starts with any of the commands
        boolean commandFound = false;
        for (String command : this.commands) {
            if (trimmedText.startsWith(command)) {
                commandFound = true;
                // You can do your specific processing here based on the command
                System.out.println("Command found: " + command);
                // Break the loop if a command is found
                break;
            }
        }

        if (!commandFound) {
            System.out.println("No command found in the text.");
        }

        return commandFound;
    }
}