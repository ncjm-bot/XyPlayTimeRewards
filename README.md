# XyPlayTimeRewards

[![License](https://img.shields.io/github/license/ncjm-bot/XyPlayTimeRewards)](LICENSE)
[![Release](https://img.shields.io/github/v/release/ncjm-bot/XyPlayTimeRewards)](https://github.com/ncjm-bot/XyPlayTimeRewards/releases)
[![Downloads](https://img.shields.io/github/downloads/ncjm-bot/XyPlayTimeRewards/total)](https://github.com/ncjm-bot/XyPlayTimeRewards/releases)

A lightweight and configurable **playtime rewards plugin** for Minecraft servers.

Players can earn rewards based on their playtime, claim rewards through a GUI, and compete on the server leaderboard.

---

## Download

➡ **Get the latest version**

[![Download](https://img.shields.io/badge/Download-Latest%20Release-blue?style=for-the-badge)](https://github.com/ncjm-bot/XyPlayTimeRewards/releases/latest)

---

## Features

• Playtime milestone rewards  
• GUI menu for claiming rewards  
• Daily reward system  
• Playtime leaderboard  
• Fully configurable reward tiers  
• Command-based reward support  
• Lightweight playtime tracking  

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
2. Place the `.jar` file inside your server's `plugins` folder.
3. Restart the server.
4. Configure reward tiers inside `rewards.yml`.

---

## Configuration

The plugin automatically generates the following files:
• config.yml
• rewards.yml
• data.yml

These files allow you to customize:

• reward tiers  
• reward commands  
• GUI settings  
• playtime milestones  

---

## Requirements

• Minecraft Server (Paper / Spigot)  
• Java 17 or newer  
• Tested on modern Paper builds  

---

## Support

If you encounter issues or have suggestions, please open an **Issue** on GitHub.

---

## License

This project is licensed under the **MIT License**.
