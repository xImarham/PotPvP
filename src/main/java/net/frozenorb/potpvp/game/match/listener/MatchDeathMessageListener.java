package net.frozenorb.potpvp.game.match.listener;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnWeatherEntity;
import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.game.match.Match;
import net.frozenorb.potpvp.game.match.MatchHandler;
import net.frozenorb.potpvp.game.nametag.PotPvPNametagProvider;
import net.frozenorb.potpvp.player.setting.Setting;
import net.frozenorb.potpvp.player.setting.SettingHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class MatchDeathMessageListener implements Listener {

    public static final String NO_KILLER_MESSAGE = ChatColor.translateAlternateColorCodes('&', "%s&e died.");
    public static final String KILLED_BY_OTHER_MESSAGE = ChatColor.translateAlternateColorCodes('&', "%s &7was killed by %s&7.");

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerDeath(PlayerDeathEvent event) {
        SettingHandler settingHandler = PotPvPSI.getInstance().getSettingHandler();
        MatchHandler matchHandler = PotPvPSI.getInstance().getMatchHandler();
        Match match = matchHandler.getMatchPlaying(event.getEntity());

        if (match == null) {
            return;
        }

        Player killed = event.getEntity();
        Player killer = killed.getKiller();

        // Create lightning packet for the death location
        WrapperPlayServerSpawnWeatherEntity lightningPacket = createLightningPacket(killed.getLocation());

        float thunderSoundPitch = 0.8F + ThreadLocalRandom.current().nextFloat() * 0.2F;
        float explodeSoundPitch = 0.5F + ThreadLocalRandom.current().nextFloat() * 0.2F;

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            UUID onlinePlayerUuid = onlinePlayer.getUniqueId();

            // Skip players who aren't involved in the match
            if (match.getTeam(onlinePlayerUuid) == null && !match.isSpectator(onlinePlayerUuid)) {
                continue;
            }

            String killedNameColor = PotPvPNametagProvider.getNameColor(killed, onlinePlayer);
            String killedFormattedName = killedNameColor + killed.getName();

            // If no killer or if the killer is a spectator, only show that the player died
            if (killer == null || match.isSpectator(killer.getUniqueId())) {
                onlinePlayer.sendMessage(String.format(NO_KILLER_MESSAGE, killedFormattedName));
            } else {
                String killerNameColor = PotPvPNametagProvider.getNameColor(killer, onlinePlayer);
                String killerFormattedName = killerNameColor + killer.getName();
                onlinePlayer.sendMessage(String.format(KILLED_BY_OTHER_MESSAGE, killedFormattedName, killerFormattedName));
            }

            // Send lightning and thunder if the player has the setting enabled
            if (settingHandler.getSetting(onlinePlayer, Setting.VIEW_OTHERS_LIGHTNING)) {
                onlinePlayer.playSound(killed.getLocation(), Sound.AMBIENCE_THUNDER, 10000F, thunderSoundPitch);
                onlinePlayer.playSound(killed.getLocation(), Sound.EXPLODE, 2.0F, explodeSoundPitch);
                sendLightningPacket(onlinePlayer, lightningPacket);
            }
        }
    }

    /**
     * Creates a lightning spawn packet.
     *
     * @param location The location to spawn the lightning at.
     * @return The lightning packet.
     */
    public WrapperPlayServerSpawnWeatherEntity createLightningPacket(Location location) {
        WrapperPlayServerSpawnWeatherEntity lightningPacket = new WrapperPlayServerSpawnWeatherEntity(93, (byte) 1, location.getX(), location.getY(), location.getZ());
        return lightningPacket;
    }

    /**
     * Sends the lightning packet to a specific player.
     *
     * @param target The player to send the packet to.
     * @param packet The packet to send.
     */
    public void sendLightningPacket(Player target, WrapperPlayServerSpawnWeatherEntity packet) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(target,packet);


    }
}
