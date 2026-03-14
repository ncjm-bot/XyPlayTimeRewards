package com.ixyber.xyplaytimerewards.manager;

import com.ixyber.xyplaytimerewards.XyPlayTimeRewardsPlugin;
import com.ixyber.xyplaytimerewards.model.RewardTier;
import com.ixyber.xyplaytimerewards.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.UUID;

public class PlaytimeManager {

    private final XyPlayTimeRewardsPlugin plugin;
    private final DataManager dataManager;
    private final RewardManager rewardManager;

    private BukkitTask trackingTask;
    private BukkitTask saveTask;

    public PlaytimeManager(XyPlayTimeRewardsPlugin plugin, DataManager dataManager, RewardManager rewardManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.rewardManager = rewardManager;
    }

    public void startTrackingTask() {
        long trackIntervalSeconds = plugin.getConfig().getLong("settings.track-interval-seconds", 60L);
        long saveIntervalSeconds = plugin.getConfig().getLong("settings.save-interval-seconds", 60L);

        long trackTicks = Math.max(20L, trackIntervalSeconds * 20L);
        long saveTicks = Math.max(20L, saveIntervalSeconds * 20L);

        trackingTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID uuid = player.getUniqueId();
                dataManager.addPlaytimeSeconds(uuid, trackIntervalSeconds);

                long totalSeconds = dataManager.getPlaytimeSeconds(uuid);
                checkUnlocks(player, totalSeconds);
            }
        }, trackTicks, trackTicks);

        saveTask = Bukkit.getScheduler().runTaskTimer(plugin, dataManager::save, saveTicks, saveTicks);
    }

    private void checkUnlocks(Player player, long totalSeconds) {
        List<RewardTier> rewards = rewardManager.getRewards();

        for (RewardTier reward : rewards) {
            String key = reward.getKey();
            long requiredSeconds = reward.getRequiredMinutes() * 60L;

            if (totalSeconds >= requiredSeconds
                    && !dataManager.hasClaimed(player.getUniqueId(), key)
                    && !dataManager.hasBeenNotified(player.getUniqueId(), key)) {

                dataManager.setNotified(player.getUniqueId(), key, true);

                String title = plugin.getConfig().getString("alerts.unlock-title", "&6&lReward Unlocked!");
                String subtitle = plugin.getConfig().getString("alerts.unlock-subtitle", "&f%reward% &7is now ready to claim.");
                String chat = plugin.getConfig().getString("alerts.unlock-chat", "&6✦ &eNew playtime reward unlocked: &f%reward%");
                String actionbar = plugin.getConfig().getString("alerts.unlock-actionbar", "&6Reward Ready &8• &f/playtimerewards");

                title = title.replace("%reward%", reward.getDisplayName());
                subtitle = subtitle.replace("%reward%", reward.getDisplayName());
                chat = chat.replace("%reward%", reward.getDisplayName());
                actionbar = actionbar.replace("%reward%", reward.getDisplayName());

                player.sendTitle(ColorUtil.color(title), ColorUtil.color(subtitle), 10, 60, 20);
                player.sendMessage(ColorUtil.color(chat));
                player.sendActionBar(ColorUtil.color(actionbar));
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.12f);

                if (reward.getRequiredMinutes() >= 1440) {
                    spawnFirework(player);
                }

                if (reward.isBroadcast()) {
                    String broadcast = plugin.getConfig().getString("alerts.broadcast-unlock", "&6✦ &f%player% &ehas unlocked the playtime reward &f%reward%&e!");
                    broadcast = broadcast
                            .replace("%player%", player.getName())
                            .replace("%reward%", reward.getDisplayName());

                    Bukkit.broadcastMessage(ColorUtil.color(broadcast));
                }
            }
        }
    }

    private void spawnFirework(Player player) {
        Firework firework = player.getWorld().spawn(player.getLocation().add(0, 1, 0), Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();

        meta.addEffect(FireworkEffect.builder()
                .with(FireworkEffect.Type.BALL_LARGE)
                .withColor(Color.AQUA, Color.YELLOW, Color.FUCHSIA)
                .withFade(Color.WHITE)
                .trail(true)
                .flicker(true)
                .build());

        meta.setPower(1);
        firework.setFireworkMeta(meta);
    }

    public long getPlaytimeSeconds(Player player) {
        return dataManager.getPlaytimeSeconds(player.getUniqueId());
    }

    public long getPlaytimeSeconds(UUID uuid) {
        return dataManager.getPlaytimeSeconds(uuid);
    }

    public long getPlaytimeMinutes(Player player) {
        return getPlaytimeSeconds(player) / 60L;
    }

    public void setPlaytimeMinutes(UUID uuid, long minutes) {
        dataManager.setPlaytimeSeconds(uuid, Math.max(0L, minutes) * 60L);
        dataManager.save();
    }

    public void resetPlayer(UUID uuid) {
        dataManager.resetPlayer(uuid);
        dataManager.save();
    }

    public boolean canClaimDaily(UUID uuid) {
        long cooldownHours = plugin.getConfig().getLong("daily.cooldown-hours", 24L);
        long cooldownMillis = cooldownHours * 60L * 60L * 1000L;
        long lastClaim = dataManager.getLastDailyClaim(uuid);

        return lastClaim <= 0 || System.currentTimeMillis() - lastClaim >= cooldownMillis;
    }

    public long getDailyRemainingSeconds(UUID uuid) {
        long cooldownHours = plugin.getConfig().getLong("daily.cooldown-hours", 24L);
        long cooldownMillis = cooldownHours * 60L * 60L * 1000L;
        long lastClaim = dataManager.getLastDailyClaim(uuid);

        if (lastClaim <= 0) {
            return 0L;
        }

        long elapsed = System.currentTimeMillis() - lastClaim;
        long remaining = cooldownMillis - elapsed;

        return Math.max(0L, remaining / 1000L);
    }

    public void claimDaily(UUID uuid) {
        dataManager.setLastDailyClaim(uuid, System.currentTimeMillis());
        dataManager.save();
    }

    public void saveAll() {
        if (trackingTask != null) {
            trackingTask.cancel();
        }

        if (saveTask != null) {
            saveTask.cancel();
        }

        dataManager.save();
    }
}