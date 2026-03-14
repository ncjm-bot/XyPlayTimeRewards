package com.ixyber.xyplaytimerewards.manager;

import com.ixyber.xyplaytimerewards.XyPlayTimeRewardsPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class DataManager {

    private final XyPlayTimeRewardsPlugin plugin;
    private final File file;
    private FileConfiguration config;

    public DataManager(XyPlayTimeRewardsPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "data.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public long getPlaytimeSeconds(UUID uuid) {
        return config.getLong("players." + uuid + ".playtime-seconds", 0L);
    }

    public void setPlaytimeSeconds(UUID uuid, long seconds) {
        config.set("players." + uuid + ".playtime-seconds", Math.max(0L, seconds));
    }

    public void addPlaytimeSeconds(UUID uuid, long seconds) {
        setPlaytimeSeconds(uuid, getPlaytimeSeconds(uuid) + Math.max(0L, seconds));
    }

    public Set<String> getClaimedRewards(UUID uuid) {
        return new HashSet<>(config.getStringList("players." + uuid + ".claimed"));
    }

    public boolean hasClaimed(UUID uuid, String rewardKey) {
        return getClaimedRewards(uuid).contains(rewardKey);
    }

    public void setClaimed(UUID uuid, String rewardKey, boolean claimed) {
        Set<String> claimedRewards = getClaimedRewards(uuid);

        if (claimed) {
            claimedRewards.add(rewardKey);
        } else {
            claimedRewards.remove(rewardKey);
        }

        config.set("players." + uuid + ".claimed", new ArrayList<>(claimedRewards));
    }

    public Set<String> getNotifiedRewards(UUID uuid) {
        return new HashSet<>(config.getStringList("players." + uuid + ".notified"));
    }

    public boolean hasBeenNotified(UUID uuid, String rewardKey) {
        return getNotifiedRewards(uuid).contains(rewardKey);
    }

    public void setNotified(UUID uuid, String rewardKey, boolean notified) {
        Set<String> notifiedRewards = getNotifiedRewards(uuid);

        if (notified) {
            notifiedRewards.add(rewardKey);
        } else {
            notifiedRewards.remove(rewardKey);
        }

        config.set("players." + uuid + ".notified", new ArrayList<>(notifiedRewards));
    }

    public long getLastDailyClaim(UUID uuid) {
        return config.getLong("players." + uuid + ".daily-last-claim", 0L);
    }

    public void setLastDailyClaim(UUID uuid, long timestamp) {
        config.set("players." + uuid + ".daily-last-claim", timestamp);
    }

    public void resetPlayer(UUID uuid) {
        config.set("players." + uuid, null);
    }

    public Set<UUID> getAllStoredPlayers() {
        ConfigurationSection section = config.getConfigurationSection("players");
        if (section == null) {
            return new HashSet<>();
        }

        return section.getKeys(false).stream()
                .map(key -> {
                    try {
                        return UUID.fromString(key);
                    } catch (IllegalArgumentException exception) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException exception) {
            plugin.getLogger().severe("Failed to save data.yml");
            exception.printStackTrace();
        }
    }

    public void reload() {
        this.config = YamlConfiguration.loadConfiguration(file);
    }
}