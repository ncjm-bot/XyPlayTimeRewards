package com.ixyber.xyplaytimerewards.command;

import com.ixyber.xyplaytimerewards.XyPlayTimeRewardsPlugin;
import com.ixyber.xyplaytimerewards.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class PlaytimeRewardsAdminCommand implements CommandExecutor {

    private final XyPlayTimeRewardsPlugin plugin;

    public PlaytimeRewardsAdminCommand(XyPlayTimeRewardsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("xyplaytimerewards.admin")) {
            sender.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.no-permission", "&cYou do not have permission.")));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ColorUtil.color("&8&m------------------------------------------------"));
            sender.sendMessage(ColorUtil.color("&b&lPlaytime Rewards &8• &fAdmin Commands"));
            sender.sendMessage(ColorUtil.color("&7/ptradmin reload"));
            sender.sendMessage(ColorUtil.color("&7/ptradmin reset <player>"));
            sender.sendMessage(ColorUtil.color("&7/ptradmin settime <player> <minutes>"));
            sender.sendMessage(ColorUtil.color("&8&m------------------------------------------------"));
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "reload" -> {
                plugin.reloadConfig();
                plugin.getDataManager().reload();
                plugin.getRewardManager().loadRewards();
                sender.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.reload-success", "&aPlaytimeRewards reloaded.")));
                return true;
            }

            case "reset" -> {
                if (args.length < 2) {
                    sender.sendMessage(ColorUtil.color("&cUsage: /playtimerewardsadmin reset <player>"));
                    return true;
                }

                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                plugin.getPlaytimeManager().resetPlayer(target.getUniqueId());

                String msg = plugin.getConfig().getString("messages.reset-success", "&aReset playtime reward data for &e%player%&a.");
                sender.sendMessage(ColorUtil.color(msg.replace("%player%", args[1])));
                return true;
            }

            case "settime" -> {
                if (args.length < 3) {
                    sender.sendMessage(ColorUtil.color("&cUsage: /playtimerewardsadmin settime <player> <minutes>"));
                    return true;
                }

                long minutes;
                try {
                    minutes = Long.parseLong(args[2]);
                } catch (NumberFormatException exception) {
                    sender.sendMessage(ColorUtil.color("&cMinutes must be a number."));
                    return true;
                }

                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                plugin.getPlaytimeManager().setPlaytimeMinutes(target.getUniqueId(), minutes);

                String msg = plugin.getConfig().getString("messages.settime-success", "&aSet %player%'s playtime to &e%minutes% &aminutes.");
                msg = msg.replace("%player%", args[1]).replace("%minutes%", String.valueOf(minutes));
                sender.sendMessage(ColorUtil.color(msg));
                return true;
            }

            default -> {
                sender.sendMessage(ColorUtil.color("&cUnknown subcommand."));
                return true;
            }
        }
    }
}