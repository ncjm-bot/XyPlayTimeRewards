package com.ixyber.xyplaytimerewards.listener;

import com.ixyber.xyplaytimerewards.XyPlayTimeRewardsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinQuitListener implements Listener {

    private final XyPlayTimeRewardsPlugin plugin;

    public JoinQuitListener(XyPlayTimeRewardsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getDataManager().getPlaytimeSeconds(event.getPlayer().getUniqueId());
    }
}