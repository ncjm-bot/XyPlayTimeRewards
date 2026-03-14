package com.ixyber.xyplaytimerewards.command;

import com.ixyber.xyplaytimerewards.XyPlayTimeRewardsPlugin;
import com.ixyber.xyplaytimerewards.util.ColorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlaytimeRewardsCommand implements CommandExecutor {

    private final XyPlayTimeRewardsPlugin plugin;

    public PlaytimeRewardsCommand(XyPlayTimeRewardsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.players-only", "&cOnly players can use this command.")));
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("top")) {
            plugin.getRewardsMenu().openLeaderboard(player, 0);
            return true;
        }

        plugin.getRewardsMenu().openMain(player, 0);
        return true;
    }
}