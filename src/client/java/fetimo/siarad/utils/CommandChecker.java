package fetimo.siarad.utils;

import java.util.HashSet;
import java.util.Set;

public class CommandChecker {
    Set<String> commands = new HashSet<>();

    public CommandChecker() {
        commands.add("msg");
        commands.add("co ");
        commands.add("invisac");
        commands.add("vanish");
        commands.add("rg ");
        commands.add("tp");
        commands.add("playmusic");
        commands.add("sendtospawn");
        commands.add("spawn");
        commands.add("notes");
        commands.add("mv");
        commands.add("whois");
        commands.add("gc");
        commands.add("modreq");
        commands.add("acalert");
        commands.add("achelper");
        commands.add("acm");
        commands.add("acph");
        commands.add("afk");
        commands.add("act");
        commands.add("autcrafttools");
        commands.add("advancement");
        commands.add("alert");
        commands.add("allowfire");
        commands.add("alts");
        commands.add("amsg");
        commands.add("announce");
        commands.add("antioch");
        commands.add("antispam");
        commands.add("anychest");
        commands.add("anycontainer");
        commands.add("silentcontainer");
        commands.add("silentchest");
        commands.add("armoreffect");
        commands.add("armorstand");
        commands.add("asc");
        commands.add("ascend");
        commands.add("attribute");
        commands.add("back");
        commands.add("backup");
        commands.add("bal");
        commands.add("baltop");
        commands.add("ban");
        commands.add("ban-ip");
        commands.add("banlist");
        commands.add("bard");
        commands.add("bc");
        commands.add("bcw");
        commands.add("bday");
        commands.add("bdm");
        commands.add("beecannon");
        commands.add("beezooka");
        commands.add("bellyflop");
        commands.add("betterlogs");
        commands.add("bigtree");
        commands.add("biomeinfo");
        commands.add("broadcast");
        commands.add("bukkit:");
        commands.add("book");
        commands.add("bossbar");
        commands.add("bportals");
        commands.add("buy");
        commands.add("bw");
        commands.add("bungee");
        commands.add("bungeeportals");
        commands.add("brush");
        commands.add("blockinfo");
        commands.add("buttonwarp");
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