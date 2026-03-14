package com.ixyber.xyplaytimerewards.manager;

import com.ixyber.xyplaytimerewards.XyPlayTimeRewardsPlugin;
import com.ixyber.xyplaytimerewards.model.RewardTier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RewardManager {

    private final XyPlayTimeRewardsPlugin plugin;
    private final List<RewardTier> rewards = new ArrayList<>();
    private FileConfiguration rewardsConfig;

    public RewardManager(XyPlayTimeRewardsPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadRewards() {
        rewards.clear();

        File file = new File(plugin.getDataFolder(), "rewards.yml");
        rewardsConfig = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection section = rewardsConfig.getConfigurationSection("rewards");
        if (section == null) {
            plugin.getLogger().warning("No rewards section found in rewards.yml");
            return;
        }

        for (String key : section.getKeys(false)) {
            String path = "rewards." + key + ".";

            long requiredMinutes = rewardsConfig.getLong(path + "required-minutes");
            String material = rewardsConfig.getString(path + "material", "CHEST");
            String displayName = rewardsConfig.getString(path + "display-name", key);
            boolean broadcast = rewardsConfig.getBoolean(path + "broadcast", false);

            long cash = rewardsConfig.getLong(path + "cash", 0L);
            long coins = rewardsConfig.getLong(path + "coins", 0L);

            List<String> rewardLore = rewardsConfig.getStringList(path + "reward-lore");
            List<String> commands = rewardsConfig.getStringList(path + "commands");

            rewards.add(new RewardTier(
                    key,
                    requiredMinutes,
                    material,
                    displayName,
                    broadcast,
                    cash,
                    coins,
                    rewardLore,
                    commands
            ));
        }

        rewards.sort(Comparator.comparingLong(RewardTier::getRequiredMinutes));
    }

    public List<RewardTier> getRewards() {
        return new ArrayList<>(rewards);
    }

    public RewardTier getReward(String key) {
        for (RewardTier reward : rewards) {
            if (reward.getKey().equalsIgnoreCase(key)) {
                return reward;
            }
        }
        return null;
    }
}