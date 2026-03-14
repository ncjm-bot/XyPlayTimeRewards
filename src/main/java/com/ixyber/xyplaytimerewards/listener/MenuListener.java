package com.ixyber.xyplaytimerewards.listener;

import com.ixyber.xyplaytimerewards.XyPlayTimeRewardsPlugin;
import com.ixyber.xyplaytimerewards.model.RewardTier;
import com.ixyber.xyplaytimerewards.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class MenuListener implements Listener {

    private final XyPlayTimeRewardsPlugin plugin;

    public MenuListener(XyPlayTimeRewardsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof com.ixyber.xyplaytimerewards.gui.RewardsMenu.MainMenuHolder)
                && !(holder instanceof com.ixyber.xyplaytimerewards.gui.RewardsMenu.LeaderboardHolder)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemStack current = event.getCurrentItem();
        if (current == null) {
            return;
        }

        String action = plugin.getRewardsMenu().getAction(current);
        String value = plugin.getRewardsMenu().getValue(current);

        if (action == null) {
            return;
        }

        FileConfiguration config = plugin.getConfig();

        switch (action.toLowerCase()) {
            case "reward" -> handleRewardClaim(player, value, config);
            case "daily" -> handleDailyClaim(player, config);
            case "open-leaderboard" -> plugin.getRewardsMenu().openLeaderboard(player, 0);
            case "back-main" -> plugin.getRewardsMenu().openMain(player, 0);
            case "prev-main", "next-main" -> plugin.getRewardsMenu().openMain(player, parsePage(value));
            case "prev-top", "next-top" -> plugin.getRewardsMenu().openLeaderboard(player, parsePage(value));
            default -> {
            }
        }
    }

    private void handleRewardClaim(Player player, String rewardKey, FileConfiguration config) {
        RewardTier reward = plugin.getRewardManager().getReward(rewardKey);
        if (reward == null) {
            return;
        }

        if (plugin.getDataManager().hasClaimed(player.getUniqueId(), reward.getKey())) {
            player.sendMessage(ColorUtil.color(config.getString("messages.reward-already-claimed", "&cYou already claimed this reward.")));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.85f);
            return;
        }

        long playerSeconds = plugin.getPlaytimeManager().getPlaytimeSeconds(player);
        long requiredSeconds = reward.getRequiredMinutes() * 60L;

        if (playerSeconds < requiredSeconds) {
            player.sendMessage(ColorUtil.color(config.getString("messages.reward-locked", "&cYou have not unlocked this reward yet.")));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.85f);
            return;
        }

        giveCash(player, reward.getCash(), config);
        giveCoins(player, reward.getCoins(), config);

        for (String command : reward.getCommands()) {
            String parsed = command.replace("%player%", player.getName());
            dispatchConsole(parsed);
        }

        plugin.getDataManager().setClaimed(player.getUniqueId(), reward.getKey(), true);
        plugin.getDataManager().save();

        String message = config.getString("messages.reward-claimed", "&6✦ &aYou claimed &f%reward%&a!");
        message = message.replace("%reward%", reward.getDisplayName());

        player.sendMessage(ColorUtil.color(message));
        player.sendActionBar(ColorUtil.color("&6Playtime Reward Collected &8• &f" + reward.getDisplayName()));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);

        if (reward.isBroadcast()) {
            String broadcast = config.getString("alerts.broadcast-claim", "&6✦ &f%player% &ehas claimed the playtime reward &f%reward%&e!");
            broadcast = broadcast
                    .replace("%player%", player.getName())
                    .replace("%reward%", reward.getDisplayName());

            Bukkit.broadcastMessage(ColorUtil.color(broadcast));
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            int page = 0;
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof com.ixyber.xyplaytimerewards.gui.RewardsMenu.MainMenuHolder mainHolder) {
                page = mainHolder.getPage();
            }
            plugin.getRewardsMenu().openMain(player, page);
        });
    }

    private void handleDailyClaim(Player player, FileConfiguration config) {
        if (!plugin.getPlaytimeManager().canClaimDaily(player.getUniqueId())) {
            player.sendMessage(ColorUtil.color(config.getString("messages.daily-on-cooldown", "&cYour daily reward is still on cooldown.")));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.85f);
            return;
        }

        long dailyCash = config.getLong("daily.cash", 0L);
        long dailyCoins = config.getLong("daily.coins", 0L);

        giveCash(player, dailyCash, config);
        giveCoins(player, dailyCoins, config);

        for (String command : config.getStringList("daily.commands")) {
            String parsed = command.replace("%player%", player.getName());
            dispatchConsole(parsed);
        }

        plugin.getPlaytimeManager().claimDaily(player.getUniqueId());

        String message = config.getString("messages.daily-claimed", "&6✦ &aYou claimed your daily reward!");
        player.sendMessage(ColorUtil.color(message));
        player.sendActionBar(ColorUtil.color("&6Daily Reward Collected &8• &fCome back tomorrow"));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.15f);

        Bukkit.getScheduler().runTask(plugin, () -> {
            int page = 0;
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof com.ixyber.xyplaytimerewards.gui.RewardsMenu.MainMenuHolder mainHolder) {
                page = mainHolder.getPage();
            }
            plugin.getRewardsMenu().openMain(player, page);
        });
    }

    private void giveCash(Player player, long amount, FileConfiguration config) {
        if (amount <= 0) {
            return;
        }

        String command = config.getString("settings.cash-command", "eco give %player% %amount%");
        command = command.replace("%player%", player.getName()).replace("%amount%", String.valueOf(amount));
        dispatchConsole(command);
    }

    private void giveCoins(Player player, long amount, FileConfiguration config) {
        if (amount <= 0) {
            return;
        }

        String command = config.getString("settings.coins-command", "coins give %player% %amount%");
        command = command.replace("%player%", player.getName()).replace("%amount%", String.valueOf(amount));
        dispatchConsole(command);
    }

    private void dispatchConsole(String command) {
        CommandSender console = Bukkit.getConsoleSender();
        Bukkit.dispatchCommand(console, command.startsWith("/") ? command.substring(1) : command);
    }

    private int parsePage(String value) {
        try {
            return Math.max(0, Integer.parseInt(value));
        } catch (NumberFormatException exception) {
            return 0;
        }
    }
}