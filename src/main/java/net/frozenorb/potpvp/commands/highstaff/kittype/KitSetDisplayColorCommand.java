package net.frozenorb.potpvp.commands.highstaff.kittype;

import net.frozenorb.potpvp.game.kittype.KitType;
import net.frozenorb.potpvp.kt.command.Command;
import net.frozenorb.potpvp.kt.command.data.parameter.Param;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class KitSetDisplayColorCommand {

    @Command(names = {"kittype setdisplaycolor"}, permission = "op", description = "Sets a kit-type's display color")
    public static void execute(Player player, @Param(name = "kittype") KitType kitType, @Param(name = "displayColor", wildcard = true) String color) {
        // Split the color and formatting to handle both color and bold separately
        String[] parts = color.split(" ");
        ChatColor chatColor = ChatColor.valueOf(parts[0].toUpperCase().replace(" ", "_"));
        boolean isBold = parts.length > 1 && parts[1].equalsIgnoreCase("bold");

        // Apply both color and bold if applicable
        String finalColor = chatColor.toString();
        if (isBold) {
            finalColor += ChatColor.BOLD;
        }

        kitType.setDisplayColor(ChatColor.valueOf(finalColor));
        kitType.saveAsync();

        player.sendMessage(ChatColor.GREEN + "You've updated this kit-type's display color.");
    }

}
