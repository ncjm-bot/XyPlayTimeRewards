# XyPlayTimeRewards

A lightweight and configurable **playtime rewards system** for Minecraft servers.

Players can earn rewards based on their playtime, claim rewards through a GUI, and compete on the playtime leaderboard.

---

## Features

- Playtime milestone rewards
- GUI menu for claiming rewards
- Daily reward system
- Playtime leaderboard
- Fully configurable reward tiers
- Command-based reward support
- Lightweight playtime tracking

---

## Commands

### Player Commands

`/playtimerewards`  
Open the rewards menu.

`/playtimerewards top`  
View the playtime leaderboard.

---

### Admin Commands

`/playtimerewardsadmin reload`  
Reload the plugin configuration.

`/playtimerewardsadmin reset <player>`  
Reset a player's playtime data.

`/playtimerewardsadmin settime <player> <minutes>`  
Manually set a player's playtime.

---

## Installation

1. Download the latest release from the **Releases** page.
2. Place the `.jar` file into your server's `plugins` folder.
3. Restart the server.
4. Configure rewards in `rewards.yml`.

---

## Configuration

The plugin automatically generates configuration files:
config.yml
rewards.yml
data.yml

Edit these files to customize:

- reward tiers
- reward commands
- GUI settings
- playtime milestones

---

## Requirements

- Minecraft Server (Paper / Spigot)
- Java 17+
- Tested on modern Paper versions

---

## Support

If you encounter issues or have suggestions, feel free to open an **Issue** on GitHub.

---

## License

This project is licensed under the **MIT License**.
