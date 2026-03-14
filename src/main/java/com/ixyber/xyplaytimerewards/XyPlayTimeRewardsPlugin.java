package com.ixyber.xyplaytimerewards;

import com.ixyber.xyplaytimerewards.command.PlaytimeRewardsAdminCommand;
import com.ixyber.xyplaytimerewards.command.PlaytimeRewardsCommand;
import com.ixyber.xyplaytimerewards.gui.RewardsMenu;
import com.ixyber.xyplaytimerewards.listener.MenuListener;
import com.ixyber.xyplaytimerewards.manager.DataManager;
import com.ixyber.xyplaytimerewards.manager.PlaytimeManager;
import com.ixyber.xyplaytimerewards.manager.RewardManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class XyPlayTimeRewardsPlugin extends JavaPlugin {

    private static XyPlayTimeRewardsPlugin instance;

    private DataManager dataManager;
    private RewardManager rewardManager;
    private PlaytimeManager playtimeManager;
    private RewardsMenu rewardsMenu;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        saveMissingResource("rewards.yml");
        saveMissingResource("data.yml");

        dataManager = new DataManager(this);
        rewardManager = new RewardManager(this);
        rewardManager.loadRewards();

        playtimeManager = new PlaytimeManager(this, dataManager, rewardManager);
        rewardsMenu = new RewardsMenu(this);

        if (getCommand("playtimerewards") != null) {
            getCommand("playtimerewards").setExecutor(new PlaytimeRewardsCommand(this));
        }

        if (getCommand("playtimerewardsadmin") != null) {
            getCommand("playtimerewardsadmin").setExecutor(new PlaytimeRewardsAdminCommand(this));
        }

        getServer().getPluginManager().registerEvents(new MenuListener(this), this);

        playtimeManager.startTrackingTask();
        rewardsMenu.startAnimationTask();

        getLogger().info("XyPlayTimeRewards enabled.");
    }

    @Override
    public void onDisable() {
        if (rewardsMenu != null) {
            rewardsMenu.stopAnimationTask();
        }

        if (playtimeManager != null) {
            playtimeManager.saveAll();
        }

        if (dataManager != null) {
            dataManager.save();
        }

        getLogger().info("XyPlayTimeRewards disabled.");
    }

    public static XyPlayTimeRewardsPlugin getInstance() {
        return instance;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public RewardManager getRewardManager() {
        return rewardManager;
    }

    public PlaytimeManager getPlaytimeManager() {
        return playtimeManager;
    }

    public RewardsMenu getRewardsMenu() {
        return rewardsMenu;
    }

    private void saveMissingResource(String resourcePath) {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        File file = new File(getDataFolder(), resourcePath);
        if (!file.exists()) {
            saveResource(resourcePath, false);
        }
    }
}