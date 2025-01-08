package net.frozenorb.potpvp.integration.holograms;

import lombok.Getter;
import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.game.kittype.KitType;
import net.frozenorb.potpvp.integration.holograms.task.HologramsTask;
import net.frozenorb.potpvp.kt.command.Command;
import net.frozenorb.potpvp.kt.command.data.parameter.Param;
import net.frozenorb.potpvp.util.LocationUtils;
import net.frozenorb.potpvp.util.bukkit.files.ConfigFile;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Getter
public class HologramsHandler {

    public final Map<Integer, Map.Entry<String, Integer>> globalPositions = new HashMap<>();
    public final Map<KitType, Map<Integer, Map.Entry<String, Integer>>> kitsPositions = new HashMap<>();
    public static final List<PracticeHologram> holograms = new ArrayList<>();
    private static final ConfigFile config = PotPvPSI.getInstance().getHologramsConfig();

    public HologramsHandler() {
        new HologramsTask().runTaskTimer(PotPvPSI.getInstance(), 0L, PotPvPSI.getInstance().getHologramsConfig().getInt("HOLOGRAMS.UPDATE-TIME") * 20L);
    }

    public void load() {
        // Ensure that the config object is initialized and the configuration file is loaded
        if (config == null) {
            System.out.println("Config object is not initialized!");
            return;
        }

        // Try to get the configuration
        if (config.getConfiguration() == null) {
            System.out.println("Config file is not loaded properly!");
            return;
        }

        // Get the 'PLACES' section from the config
        ConfigurationSection section = config.getConfiguration().getConfigurationSection("PLACES");
        if (section == null) {
            System.out.println("PLACES section is missing in the config!");
            return;
        }

        // Get the 'HOLOGRAMS' section from the config
        Set<String> hlms = config.getConfiguration().getConfigurationSection("HOLOGRAMS") != null
                ? config.getConfiguration().getConfigurationSection("HOLOGRAMS").getKeys(false)
                : Collections.emptySet();

        if (hlms.isEmpty()) {
            System.out.println("HOLOGRAMS section is missing or empty!");
        }

        // Iterate over the keys in the 'PLACES' section
        for (String s : section.getKeys(false)) {
            // Check if the corresponding hologram exists in 'HOLOGRAMS'
            if (!hlms.contains(s)) {
                System.out.println(s + " KitType does not exist anymore!");
                continue;
            }

            // Get the location for this hologram
            String locationString = config.getString("PLACES." + s);
            if (locationString == null) {
                System.out.println("Location for hologram " + s + " is missing in the config.");
                continue;
            }

            // Deserialize the location
            Location location = LocationUtils.deserialize(locationString);
            if (location == null) {
                System.out.println("Failed to deserialize location for hologram " + s);
                continue;
            }

            // Create and spawn the hologram
            PracticeHologram hologram = new PracticeHologram(s, location);
            hologram.spawn();
            holograms.add(hologram);
        }
    }


    public static void spawn(String hologram, Location location) {
        Set<String> hlms = config.getConfiguration().getConfigurationSection("HOLOGRAMS").getKeys(false);
        if (!hlms.contains(hologram)) {
            System.out.println(hologram + " Kittype dont not exist more!");
            return;
        }
        AtomicBoolean spawned = new AtomicBoolean(false);
        AtomicReference<PracticeHologram> practiceHologram = new AtomicReference<>();
        holograms.forEach(h -> {
            if(h.getKitType().equalsIgnoreCase(hologram)) {
                spawned.set(true);
                practiceHologram.set(h);
            }
        });
        if(spawned.get()) {
            holograms.remove(practiceHologram.get());
            practiceHologram.get().destroy();
        }
        PracticeHologram nH = new PracticeHologram(hologram, location);
        nH.spawn();
        holograms.add(nH);
        config.getConfiguration().set("PLACES." + hologram, LocationUtils.serialize(location));
        config.save();
    }

    @Command(names = "spawnhologram")
    public static void onCommand(Player player, @Param(name = "hologram") String str) {
        spawn(str, player.getLocation());
    }

    public void save() {
        holograms.forEach(hologram -> {
            config.getConfiguration().set("PLACES." + hologram.getKitType(), LocationUtils.serialize(hologram.getLocation()));
            config.save();

        });
    }
}