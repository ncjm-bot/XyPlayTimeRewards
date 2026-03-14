package com.ixyber.xyplaytimerewards.model;

import java.util.ArrayList;
import java.util.List;

public class RewardTier {

    private final String key;
    private final long requiredMinutes;
    private final String materialName;
    private final String displayName;
    private final boolean broadcast;
    private final long cash;
    private final long coins;
    private final List<String> rewardLore;
    private final List<String> commands;

    public RewardTier(
            String key,
            long requiredMinutes,
            String materialName,
            String displayName,
            boolean broadcast,
            long cash,
            long coins,
            List<String> rewardLore,
            List<String> commands
    ) {
        this.key = key;
        this.requiredMinutes = requiredMinutes;
        this.materialName = materialName;
        this.displayName = displayName;
        this.broadcast = broadcast;
        this.cash = cash;
        this.coins = coins;
        this.rewardLore = rewardLore == null ? new ArrayList<>() : new ArrayList<>(rewardLore);
        this.commands = commands == null ? new ArrayList<>() : new ArrayList<>(commands);
    }

    public String getKey() {
        return key;
    }

    public long getRequiredMinutes() {
        return requiredMinutes;
    }

    public String getMaterialName() {
        return materialName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isBroadcast() {
        return broadcast;
    }

    public long getCash() {
        return cash;
    }

    public long getCoins() {
        return coins;
    }

    public List<String> getRewardLore() {
        return new ArrayList<>(rewardLore);
    }

    public List<String> getCommands() {
        return new ArrayList<>(commands);
    }
}