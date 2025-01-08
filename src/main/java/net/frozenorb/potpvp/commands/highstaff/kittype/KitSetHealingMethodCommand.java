package net.frozenorb.potpvp.commands.highstaff.kittype;

import net.frozenorb.potpvp.game.kittype.HealingMethod;
import net.frozenorb.potpvp.game.kittype.KitType;
import net.frozenorb.potpvp.kt.command.Command;
import net.frozenorb.potpvp.kt.command.data.parameter.Param;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class KitSetHealingMethodCommand {

    @Command(names = {"kittype sethealingmethod"}, permission = "op", description = "Sets a kit-type's healing method")
    public static void execute(Player player, @Param(name = "kittype") KitType kitType, @Param(name = "healingMethod") String healingMethodName) {
        // Convert the input to a HealingMethod
        HealingMethod healingMethod = null;
        for (HealingMethod method : HealingMethod.values()) {
            if (method.name().equalsIgnoreCase(healingMethodName)
                    || method.getShortSingular().equalsIgnoreCase(healingMethodName)
                    || method.getShortPlural().equalsIgnoreCase(healingMethodName)) {
                healingMethod = method;
                break;
            }
        }

        if (healingMethod == null) {
            player.sendMessage(ChatColor.RED + "Invalid healing method! Available options: pot, soup, gap.");
            return;
        }
        kitType.setHealingMethod(healingMethod);
        kitType.saveAsync();

        player.sendMessage(ChatColor.GREEN + "You've updated this kit-type's healing method to " + healingMethod.getLongSingular() + ".");
    }

}
