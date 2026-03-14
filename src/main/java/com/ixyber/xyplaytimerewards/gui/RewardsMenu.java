package com.ixyber.xyplaytimerewards.gui;

import com.ixyber.xyplaytimerewards.XyPlayTimeRewardsPlugin;
import com.ixyber.xyplaytimerewards.model.RewardTier;
import com.ixyber.xyplaytimerewards.util.ColorUtil;
import com.ixyber.xyplaytimerewards.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class RewardsMenu {

    private static final int[] REWARD_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };

    private static final int[] LEADERBOARD_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };

    private final XyPlayTimeRewardsPlugin plugin;
    private final NamespacedKey actionKey;
    private final NamespacedKey valueKey;
    private BukkitTask animationTask;

    public RewardsMenu(XyPlayTimeRewardsPlugin plugin) {
        this.plugin = plugin;
        this.actionKey = new NamespacedKey(plugin, "ptr-action");
        this.valueKey = new NamespacedKey(plugin, "ptr-value");
    }

    public void startAnimationTask() {
        animationTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Inventory top = player.getOpenInventory().getTopInventory();
                if (top == null) {
                    continue;
                }

                InventoryHolder holder = top.getHolder();
                if (holder instanceof MainMenuHolder mainHolder) {
                    fillMainInventory(player, top, mainHolder.getPage());
                } else if (holder instanceof LeaderboardHolder leaderboardHolder) {
                    fillLeaderboardInventory(top, leaderboardHolder.getPage());
                }
            }
        }, 20L, 20L);
    }

    public void stopAnimationTask() {
        if (animationTask != null) {
            animationTask.cancel();
        }
    }

    public void openMain(Player player, int page) {
        String title = ColorUtil.color(plugin.getConfig().getString("theme.main-title", "&#8FD3FF&lA&#84C8FD&lr&#79BDFB&li&#6EB2F9&la&#63A7F7&lM&#589CF5&lC &8✦ &e&lPlaytime Rewards"));
        MainMenuHolder holder = new MainMenuHolder(page);
        Inventory inventory = Bukkit.createInventory(holder, 54, title);
        holder.setInventory(inventory);

        fillMainInventory(player, inventory, page);
        player.openInventory(inventory);
    }

    public void openLeaderboard(Player player, int page) {
        String title = ColorUtil.color(plugin.getConfig().getString("theme.leaderboard-title", "&#8FD3FF&lA&#84C8FD&lr&#79BDFB&li&#6EB2F9&la&#63A7F7&lM&#589CF5&lC &8✦ &d&lTop Playtime"));
        LeaderboardHolder holder = new LeaderboardHolder(page);
        Inventory inventory = Bukkit.createInventory(holder, 54, title);
        holder.setInventory(inventory);

        fillLeaderboardInventory(inventory, page);
        player.openInventory(inventory);
    }

    public void fillMainInventory(Player player, Inventory inventory, int page) {
        clearAndFrame(inventory);

        int maxPage = getMaxRewardPage();
        page = Math.max(0, Math.min(page, maxPage));

        inventory.setItem(4, buildDailyRewardItem(player));
        inventory.setItem(46, buildActionItem(
                Material.CLOCK,
                "&6&lPage Navigator",
                Arrays.asList(
                        "&7You are viewing page &f" + (page + 1) + "&7 of &f" + (maxPage + 1),
                        "&7Total milestones: &f" + plugin.getRewardManager().getRewards().size(),
                        "",
                        "&8Premium progression rewards"
                ),
                "none",
                "none"
        ));
        inventory.setItem(49, buildProfileItem(player));
        inventory.setItem(50, buildActionItem(
                Material.KNOWLEDGE_BOOK,
                "&d&lTop Playtime",
                Arrays.asList(
                        "&7See who has the highest",
                        "&7recorded playtime on AriaMC.",
                        "",
                        "&eClick to open."
                ),
                "open-leaderboard",
                "0"
        ));

        if (page > 0) {
            inventory.setItem(45, buildActionItem(
                    Material.ARROW,
                    "&e&lPrevious Page",
                    Collections.singletonList("&7Return to the previous reward page."),
                    "prev-main",
                    String.valueOf(page - 1)
            ));
        } else {
            inventory.setItem(45, createPane(Material.GRAY_STAINED_GLASS_PANE, "&8"));
        }

        if (page < maxPage) {
            inventory.setItem(53, buildActionItem(
                    Material.ARROW,
                    "&e&lNext Page",
                    Collections.singletonList("&7Continue to the next reward page."),
                    "next-main",
                    String.valueOf(page + 1)
            ));
        } else {
            inventory.setItem(53, createPane(Material.GRAY_STAINED_GLASS_PANE, "&8"));
        }

        inventory.setItem(47, buildStatsItem(player, page, maxPage));
        inventory.setItem(51, buildMilestoneHintItem());

        List<RewardTier> rewards = plugin.getRewardManager().getRewards();
        int fromIndex = page * REWARD_SLOTS.length;
        int toIndex = Math.min(fromIndex + REWARD_SLOTS.length, rewards.size());
        int animationPhase = (int) ((System.currentTimeMillis() / 350L) % 5L);

        for (int i = fromIndex; i < toIndex; i++) {
            int slot = REWARD_SLOTS[i - fromIndex];
            inventory.setItem(slot, buildRewardItem(player, rewards.get(i), animationPhase));
        }
    }

    public void fillLeaderboardInventory(Inventory inventory, int page) {
        clearAndFrame(inventory);

        List<Map.Entry<UUID, Long>> sorted = plugin.getDataManager().getAllStoredPlayers().stream()
                .map(uuid -> Map.entry(uuid, plugin.getDataManager().getPlaytimeSeconds(uuid)))
                .sorted(Map.Entry.<UUID, Long>comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toList());

        int maxPage = Math.max(0, (int) Math.ceil(sorted.size() / (double) LEADERBOARD_SLOTS.length) - 1);
        page = Math.max(0, Math.min(page, maxPage));

        inventory.setItem(4, buildLeaderboardHeaderItem(sorted.size()));
        inventory.setItem(49, buildActionItem(
                Material.ARROW,
                "&6&lBack to Rewards",
                Collections.singletonList("&7Return to the playtime rewards menu."),
                "back-main",
                "0"
        ));
        inventory.setItem(47, buildLeaderboardInfoItem(page, maxPage, sorted.size()));

        if (page > 0) {
            inventory.setItem(45, buildActionItem(
                    Material.ARROW,
                    "&e&lPrevious Page",
                    Collections.singletonList("&7View the previous leaderboard page."),
                    "prev-top",
                    String.valueOf(page - 1)
            ));
        } else {
            inventory.setItem(45, createPane(Material.GRAY_STAINED_GLASS_PANE, "&8"));
        }

        if (page < maxPage) {
            inventory.setItem(53, buildActionItem(
                    Material.ARROW,
                    "&e&lNext Page",
                    Collections.singletonList("&7View the next leaderboard page."),
                    "next-top",
                    String.valueOf(page + 1)
            ));
        } else {
            inventory.setItem(53, createPane(Material.GRAY_STAINED_GLASS_PANE, "&8"));
        }

        int fromIndex = page * LEADERBOARD_SLOTS.length;
        int toIndex = Math.min(fromIndex + LEADERBOARD_SLOTS.length, sorted.size());

        for (int i = fromIndex; i < toIndex; i++) {
            int displayIndex = i - fromIndex;
            inventory.setItem(LEADERBOARD_SLOTS[displayIndex], buildLeaderboardHead(i + 1, sorted.get(i).getKey(), sorted.get(i).getValue()));
        }
    }

    private void clearAndFrame(Inventory inventory) {
        inventory.clear();

        for (int slot = 0; slot < 54; slot++) {
            inventory.setItem(slot, createPane(Material.BLACK_STAINED_GLASS_PANE, "&8"));
        }

        int[] aquaSlots = {0, 1, 2, 6, 7, 8, 36, 37, 38, 42, 43, 44};
        int[] lightSlots = {3, 5, 39, 41};
        int[] darkSlots = {9, 17, 18, 26, 27, 35};

        for (int slot : aquaSlots) {
            inventory.setItem(slot, createPane(Material.LIGHT_BLUE_STAINED_GLASS_PANE, "&8"));
        }

        for (int slot : lightSlots) {
            inventory.setItem(slot, createPane(Material.CYAN_STAINED_GLASS_PANE, "&8"));
        }

        for (int slot : darkSlots) {
            inventory.setItem(slot, createPane(Material.GRAY_STAINED_GLASS_PANE, "&8"));
        }

        inventory.setItem(40, createPane(Material.BLUE_STAINED_GLASS_PANE, "&8"));
    }


private ItemStack buildRewardItem(Player player, RewardTier reward, int animationPhase) {
    long playerSeconds = plugin.getPlaytimeManager().getPlaytimeSeconds(player);
    long requiredSeconds = reward.getRequiredMinutes() * 60L;

    boolean claimed = plugin.getDataManager().hasClaimed(player.getUniqueId(), reward.getKey());
    boolean unlocked = playerSeconds >= requiredSeconds;
    boolean major = reward.getRequiredMinutes() >= 1440;

    Material material;

    if (claimed) {
        material = Material.LIME_STAINED_GLASS_PANE;
    } else if (unlocked) {
        material = Material.NETHER_STAR;
    } else {
        material = Material.BARRIER;
    }

    ItemStack item = new ItemStack(material);
    ItemMeta meta = item.getItemMeta();
    if (meta == null) {
        return item;
    }

    String displayName;
    if (claimed) {
        displayName = "&a✔ " + reward.getDisplayName();
    } else if (unlocked) {
        displayName = "&e✦ " + reward.getDisplayName();
    } else {
        displayName = "&c✖ " + reward.getDisplayName();
    }

    meta.setDisplayName(ColorUtil.color(displayName));

    List<String> lore = new ArrayList<>();
    lore.add(ColorUtil.color("&8Playtime Milestone"));
    lore.add("");

    lore.add(ColorUtil.color("&f┃ &7Required Playtime: &e" + TimeUtil.formatDuration(requiredSeconds)));
    lore.add(ColorUtil.color("&f┃ &7Category: &b" + getTierLabel(reward.getRequiredMinutes())));
    lore.add("");

    lore.add(ColorUtil.color("&f┃ &6Rewards"));
    if (!reward.getRewardLore().isEmpty()) {
        for (String line : reward.getRewardLore()) {
            lore.add(ColorUtil.color("&f┃ " + line));
        }
    } else {
        if (reward.getCash() > 0) {
            lore.add(ColorUtil.color("&f┃ &a+" + ColorUtil.formatMoney(reward.getCash()) + " Cash"));
        }
        if (reward.getCoins() > 0) {
            lore.add(ColorUtil.color("&f┃ &e+" + String.format("%,d", reward.getCoins()) + " Coins"));
        }
        if (!reward.getCommands().isEmpty()) {
            lore.add(ColorUtil.color("&f┃ &d+" + reward.getCommands().size() + " bonus command reward(s)"));
        }
    }

    lore.add("");
    lore.add(ColorUtil.color("&f┃ &dProgress"));

    double progress = requiredSeconds <= 0 ? 1.0D : Math.min(1.0D, playerSeconds / (double) requiredSeconds);
    lore.add(animatedProgressBar(progress, animationPhase));

    lore.add("");

    if (claimed) {
        lore.add(ColorUtil.color("&f┃ &aStatus: Claimed"));
        lore.add(ColorUtil.color("&a✔ This reward has already been collected."));
    } else if (unlocked) {
        lore.add(ColorUtil.color("&f┃ &aStatus: Ready to Claim"));
        lore.add(ColorUtil.color("&eClick to claim this reward."));
    } else {
        long remaining = Math.max(0L, requiredSeconds - playerSeconds);
        lore.add(ColorUtil.color("&f┃ &cStatus: Locked"));
        lore.add(ColorUtil.color("&f┃ &7Remaining: &f" + TimeUtil.formatShortDuration(remaining)));
        lore.add(ColorUtil.color("&7Keep playing to unlock this reward."));
    }

    if (major) {
        lore.add("");
        lore.add(ColorUtil.color("&6✦ Major Milestone Reward"));
    }

    meta.setLore(lore);

    if (unlocked && !claimed) {
        meta.addEnchant(Enchantment.LURE, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
    }

    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
    tag(meta, "reward", reward.getKey());
    item.setItemMeta(meta);

    return item;
}


    private ItemStack buildDailyRewardItem(Player player) {
        boolean canClaim = plugin.getPlaytimeManager().canClaimDaily(player.getUniqueId());
        long remaining = plugin.getPlaytimeManager().getDailyRemainingSeconds(player.getUniqueId());

        Material material = canClaim ? Material.NETHER_STAR : Material.CLOCK;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        meta.setDisplayName(ColorUtil.color(plugin.getConfig().getString("daily.display-name", "&6&lDaily Reward")));

        List<String> lore = new ArrayList<>();
        lore.add(ColorUtil.color("&8Daily Loyalty Claim"));
        lore.add("");
        lore.add(ColorUtil.color("&f┃ &a+" + ColorUtil.formatMoney(plugin.getConfig().getLong("daily.cash", 100000)) + " Cash"));
        lore.add(ColorUtil.color("&f┃ &e+" + String.format("%,d", plugin.getConfig().getLong("daily.coins", 35)) + " Coins"));

        List<String> rewardLore = plugin.getConfig().getStringList("daily.reward-lore");
        for (String line : rewardLore) {
            lore.add(ColorUtil.color("&f┃ " + line));
        }

        if (!plugin.getConfig().getStringList("daily.commands").isEmpty()) {
            lore.add(ColorUtil.color("&f┃ &d+" + plugin.getConfig().getStringList("daily.commands").size() + " bonus command reward(s)"));
        }

        lore.add("");
        if (canClaim) {
            lore.add(ColorUtil.color("&f┃ &aStatus: Ready"));
            lore.add(ColorUtil.color("&f┃ &7Cooldown: &f24 hours"));
            lore.add("");
            lore.add(ColorUtil.color("&eClick to claim your daily reward."));
            meta.addEnchant(Enchantment.LURE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        } else {
            lore.add(ColorUtil.color("&f┃ &cStatus: On Cooldown"));
            lore.add(ColorUtil.color("&f┃ &7Remaining: &f" + TimeUtil.formatShortDuration(remaining)));
            lore.add("");
            lore.add(ColorUtil.color("&7Come back when the cooldown ends."));
        }

        meta.setLore(lore);
        tag(meta, "daily", "daily");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildProfileItem(Player player) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.setOwningPlayer(player);
        meta.setDisplayName(ColorUtil.color("&#8FD3FF&lYour &f&lProgress"));

        long totalSeconds = plugin.getPlaytimeManager().getPlaytimeSeconds(player);
        int claimed = plugin.getDataManager().getClaimedRewards(player.getUniqueId()).size();
        int totalRewards = plugin.getRewardManager().getRewards().size();
        long nextRemaining = getNextRemainingSeconds(player);

        List<String> lore = new ArrayList<>();
        lore.add(ColorUtil.color("&8Player Overview"));
        lore.add("");
        lore.add(ColorUtil.color("&f┃ &7Name: &f" + player.getName()));
        lore.add(ColorUtil.color("&f┃ &7Total Playtime: &e" + TimeUtil.formatDuration(totalSeconds)));
        lore.add(ColorUtil.color("&f┃ &7Claimed Rewards: &a" + claimed + "&7/&f" + totalRewards));
        lore.add(ColorUtil.color("&f┃ &7Daily Reward: " + (plugin.getPlaytimeManager().canClaimDaily(player.getUniqueId()) ? "&aReady" : "&cCooldown")));
        lore.add(ColorUtil.color("&f┃ &7Next Unlock In: &f" + (nextRemaining <= 0 ? "Available now" : TimeUtil.formatShortDuration(nextRemaining))));
        lore.add("");
        lore.add(ColorUtil.color("&8AriaMC loyalty progression"));

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildStatsItem(Player player, int page, int maxPage) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.setDisplayName(ColorUtil.color("&b&lReward Overview"));

        long claimable = plugin.getRewardManager().getRewards().stream()
                .filter(reward -> !plugin.getDataManager().hasClaimed(player.getUniqueId(), reward.getKey()))
                .filter(reward -> plugin.getPlaytimeManager().getPlaytimeSeconds(player) >= reward.getRequiredMinutes() * 60L)
                .count();

        List<String> lore = new ArrayList<>();
        lore.add(ColorUtil.color("&8Menu Information"));
        lore.add("");
        lore.add(ColorUtil.color("&f┃ &7Viewing Page: &f" + (page + 1) + "&7/&f" + (maxPage + 1)));
        lore.add(ColorUtil.color("&f┃ &7Claimable Rewards: &a" + claimable));
        lore.add(ColorUtil.color("&f┃ &7Tracked Players: &f" + plugin.getDataManager().getAllStoredPlayers().size()));
        lore.add("");
        lore.add(ColorUtil.color("&7Claimable milestones glow."));
        lore.add(ColorUtil.color("&7Major milestones broadcast."));
        lore.add(ColorUtil.color("&7Daily reward resets every 24h."));

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildMilestoneHintItem() {
        ItemStack item = new ItemStack(Material.END_CRYSTAL);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.setDisplayName(ColorUtil.color("&d&lMilestone Tiers"));
        List<String> lore = new ArrayList<>();
        lore.add(ColorUtil.color("&8Reward Categories"));
        lore.add("");
        lore.add(ColorUtil.color("&f┃ &aStarter &7• 5m to 1h"));
        lore.add(ColorUtil.color("&f┃ &bActive &7• 2h to 12h"));
        lore.add(ColorUtil.color("&f┃ &dDedicated &7• 1d to 20d"));
        lore.add(ColorUtil.color("&f┃ &6Prestige &7• 1 month+"));
        lore.add("");
        lore.add(ColorUtil.color("&8Long-term loyalty progression"));

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildLeaderboardHeaderItem(int totalPlayers) {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.setDisplayName(ColorUtil.color("&d&lTop Playtime"));
        List<String> lore = new ArrayList<>();
        lore.add(ColorUtil.color("&8Server Ranking"));
        lore.add("");
        lore.add(ColorUtil.color("&f┃ &7Tracked Players: &f" + totalPlayers));
        lore.add(ColorUtil.color("&f┃ &7Ordered by total recorded playtime"));
        lore.add("");
        lore.add(ColorUtil.color("&8AriaMC veterans and grinders"));

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildLeaderboardHead(int rank, UUID uuid, long playtimeSeconds) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();

        if (meta == null) {
            return item;
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        meta.setOwningPlayer(offlinePlayer);

        String rankColor = switch (rank) {
            case 1 -> "&6";
            case 2 -> "&f";
            case 3 -> "&e";
            default -> "&d";
        };

        String name = offlinePlayer.getName() == null ? uuid.toString().substring(0, 8) : offlinePlayer.getName();
        meta.setDisplayName(ColorUtil.color(rankColor + "&l#" + rank + " &f" + name));

        List<String> lore = new ArrayList<>();
        lore.add(ColorUtil.color("&8Leaderboard Entry"));
        lore.add("");
        lore.add(ColorUtil.color("&f┃ &7Playtime: &e" + TimeUtil.formatDuration(playtimeSeconds)));
        lore.add(ColorUtil.color("&f┃ &7Hours: &f" + String.format("%.1f", playtimeSeconds / 3600.0)));
        lore.add("");
        lore.add(ColorUtil.color("&8Top dedicated grinder"));

        if (rank <= 3) {
            meta.addEnchant(Enchantment.LURE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildLeaderboardInfoItem(int page, int maxPage, int totalPlayers) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.setDisplayName(ColorUtil.color("&b&lLeaderboard Info"));
        List<String> lore = new ArrayList<>();
        lore.add(ColorUtil.color("&8Ranking Overview"));
        lore.add("");
        lore.add(ColorUtil.color("&f┃ &7Page: &f" + (page + 1) + "&7/&f" + (maxPage + 1)));
        lore.add(ColorUtil.color("&f┃ &7Tracked Players: &f" + totalPlayers));
        lore.add("");
        lore.add(ColorUtil.color("&8Sorted by highest playtime"));

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildActionItem(Material material, String name, List<String> lore, String action, String value) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.setDisplayName(ColorUtil.color(name));
        meta.setLore(ColorUtil.color(lore));
        tag(meta, action, value);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createPane(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.setDisplayName(ColorUtil.color(name));
        item.setItemMeta(meta);
        return item;
    }

    private void tag(ItemMeta meta, String action, String value) {
        meta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, action);
        meta.getPersistentDataContainer().set(valueKey, PersistentDataType.STRING, value);
    }

    public String getAction(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }

        return meta.getPersistentDataContainer().get(actionKey, PersistentDataType.STRING);
    }

    public String getValue(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }

        return meta.getPersistentDataContainer().get(valueKey, PersistentDataType.STRING);
    }

    private int getMaxRewardPage() {
        int size = plugin.getRewardManager().getRewards().size();
        return Math.max(0, (int) Math.ceil(size / (double) REWARD_SLOTS.length) - 1);
    }

    private long getNextRemainingSeconds(Player player) {
        long total = plugin.getPlaytimeManager().getPlaytimeSeconds(player);

        for (RewardTier reward : plugin.getRewardManager().getRewards()) {
            if (plugin.getDataManager().hasClaimed(player.getUniqueId(), reward.getKey())) {
                continue;
            }

            long required = reward.getRequiredMinutes() * 60L;
            if (required > total) {
                return required - total;
            }
        }

        return 0L;
    }

    private String getTierLabel(long requiredMinutes) {
        if (requiredMinutes < 120) {
            return "Starter";
        }
        if (requiredMinutes < 1440) {
            return "Active";
        }
        if (requiredMinutes < 43200) {
            return "Dedicated";
        }
        return "Prestige";
    }

    private String animatedProgressBar(double progress, int animationPhase) {
        int totalBars = 18;
        int filled = (int) Math.round(progress * totalBars);

        StringBuilder builder = new StringBuilder();
        builder.append(ColorUtil.color("&f┃ "));

        for (int i = 0; i < totalBars; i++) {
            if (i < filled) {
                String color;
                int mod = (i + animationPhase) % 5;
                if (mod <= 1) {
                    color = "&#7EDBFF";
                } else if (mod == 2) {
                    color = "&#63C8FF";
                } else if (mod == 3) {
                    color = "&#4AA9FF";
                } else {
                    color = "&#9FE8FF";
                }
                builder.append(ColorUtil.color(color + "█"));
            } else {
                builder.append(ColorUtil.color("&8█"));
            }
        }

        builder.append(ColorUtil.color(" &f" + (int) Math.round(progress * 100D) + "%"));
        return builder.toString();
    }

    public static class MainMenuHolder implements InventoryHolder {

        private final int page;
        private Inventory inventory;

        public MainMenuHolder(int page) {
            this.page = page;
        }

        public int getPage() {
            return page;
        }

        public void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }

    public static class LeaderboardHolder implements InventoryHolder {

        private final int page;
        private Inventory inventory;

        public LeaderboardHolder(int page) {
            this.page = page;
        }

        public int getPage() {
            return page;
        }

        public void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }
}