# TreexClans

<div align="center">

![TreexClans Banner](https://img.shields.io/badge/TreexClans-v1.0-orange?style=for-the-badge)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.16+-green?style=for-the-badge&logo=minecraft)](https://www.minecraft.net/)

**Advanced Clan System for Minecraft Servers with Addons Support**

[Features](#-features) • [Installation](#-installation) • [API](#-api-for-developers) • [Commands](#-commands) • [Support](#-support)

</div>

---

## 📋 Description

TreexClans is a powerful clan management plugin for Minecraft 1.16+ servers. It provides a comprehensive clan system with levels, quests, ranks, economy, and unique player glow effects.

---

## ✨ Features

### Core Features
- 🏰 **Complete Clan System** with leaders and members
- 📊 **Level System** with progression and rewards
- 🎯 **Quest System** (individual and global)
- 👥 **Flexible Rank System** with customizable permissions
- 💰 **Clan Economy** with balance and coins
- ✨ **Player Glow Effects** with customizable colors for clan members
- 📦 **Clan Storage** that expands with clan levels
- 💬 **Clan Chat** for member communication
- 🛡️ **PvP Protection** between clan members (optional)
- 📈 **Clan Leaderboards** with multiple sorting options

### Customization
- 🎨 **Fully Customizable GUIs** via YAML configuration
- 🌍 **Multi-language Support** with custom message files
- 🔧 **Flexible Permission System** for ranks
- 📝 **Custom Quest System** with various quest types
- 🎭 **Custom Actions System** for rewards and events

### Developer Features
- 🔌 **Powerful Addon System** for extending functionality
- 🛠️ **Comprehensive API** for developers
- 📚 **Custom GUI API** for creating new menu types
- 🎯 **Event System** for clan-related actions
- 📦 **Custom Command API** for adding new commands

---

## 🔧 Installation

### Requirements
- **Minecraft**: 1.16+
- **Server**: Spigot, Paper, or forks
- **Java**: 16+

### Dependencies
**Required:**
- [PacketEvents](https://github.com/retrooper/packetevents)

**Optional:**
- [Vault](https://www.spigotmc.org/resources/vault.34315/) - for economy
- [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) - for placeholders
- [Treex](https://github.com/MrJetby/Treex) - auto-downloads on first startup

### Steps
1. Download the latest version
2. Place `.jar` file in `plugins/` folder
3. Install **PacketEvents**
4. Restart server
5. Configure in `plugins/TreexClans/`

---

## 📝 Commands

### Player Commands

| Command | Description
|---------|-------------|
| `/clan` | Open clan menu
| `/clan create <tag>` | Create a new clan
| `/clan invite <player>` | Invite player to clan
| `/clan accept <tag>` | Accept clan invitation
| `/clan kick <player>` | Kick player from clan
| `/clan leave` | Leave your clan
| `/clan disband` | Disband your clan
| `/clan chat [message]` | Toggle or send clan chat
| `/clan glow` | Toggle clan member glow
| `/clan setbase` | Set clan base location
| `/clan base` | Teleport to clan base
| `/clan deposit <amount>` | Deposit money to clan
| `/clan withdraw <amount>` | Withdraw money from clan
| `/clan balance` | Check clan balance
| `/clan setrank <player> <rank>` | Set player rank
| `/clan pvp` | Toggle clan PvP
| `/clan top` | View clan leaderboard

### Admin Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/xclan reload` | Reload plugin configuration | `clan.admin` |

---

## 🎯 Placeholders

### PlaceholderAPI Support

| Placeholder | Description |
|------------|-------------|
| `%clan_coin%` | Player's clan coins |
| `%clan_balance%` | Clan balance |
| `%clan_level%` | Clan level |
| `%clan_clan_exp%` | Clan experience |
| `%clan_exp%` | Player's clan experience |

---

## 🔗 Links
- **Discord (EN)**: [dsc.gg/jetby](https://dsc.gg/jetby)
- **Discord (RU)**: [dsc.gg/treexstudio](https://dsc.gg/treexstudio)
- **Documentation**: Coming soon
- **Issues**: [GitHub Issues](https://github.com/yourusername/TreexClans/issues)

---

## 📄 License

This project is licensed under a MIT License. See LICENSE file for details.

---

## 👨‍💻 Author
** MRJETBY **
###### Support me: https://ko-fi.com/jetby
---
## 💻 API for Developers

### Getting Started

Add TreexClans as a dependency to your project:

**Maven:**
```xml
	<repository>
	    <id>jitpack.io</id>
	    <url>https://jitpack.io</url>
	</repository>
```
```xml
<dependency>
    <groupId>com.github.MrJetby</groupId>
    <artifactId>TreexClans</artifactId>
    <version>1.1</version>
    <scope>provided</scope>
</dependency>
```

**Gradle:**
```gradle
dependencyResolutionManagement {
		repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		repositories {
			mavenCentral()
			maven { url 'https://jitpack.io' }
		}
	}
```
```gradle
	dependencies {
	        implementation 'com.github.MrJetby:TreexClans:1.1'
	}
```

### Creating an Addon

#### 1. Basic Addon Structure

Create a class that extends `TreexAddon`:
```java
package com.example.myaddon;

import me.jetby.treexclans.addons.TreexAddon;
import me.jetby.treexclans.TreexClans;
import org.bukkit.Bukkit;

public class MyAddon extends TreexAddon {

    @Override
    public void onEnable() {
        // Called when addon is loaded
        getClansPlugin().getLogger().info(getName() + " v" + getVersion() + " enabled!");
        
        // Register events, commands, etc.
        registerListeners();
        registerCommands();
    }

    @Override
    public void onDisable() {
        // Called when addon is unloaded
        getClansPlugin().getLogger().info(getName() + " disabled!");
    }
    
    private void registerListeners() {
        // Register your listeners
        Bukkit.getPluginManager().registerEvents(new MyListener(this), getClansPlugin());
    }
    
    private void registerCommands() {
        // Register custom commands
    }
}
```

#### 2. Create addon.yml

Create `addon.yml` in your resources folder:
```yaml
main: com.example.myaddon.MyAddon
name: MyAddon
version: 1.0.0
author: YourName
description: My awesome addon for TreexClans
```

#### 3. Working with Clan API
```java
import me.jetby.treexclans.ClanManager;
import me.jetby.treexclans.clan.Clan;
import me.jetby.treexclans.clan.Member;
import org.bukkit.entity.Player;

public class ClanAPIExample {
    
    private final TreexAddon addon;
    private final ClanManager clanManager;
    
    public ClanAPIExample(TreexAddon addon) {
        this.addon = addon;
        this.clanManager = addon.getClansPlugin().getClanManager();
    }
    
    // Check if player is in a clan
    public boolean isInClan(Player player) {
        return clanManager.isInClan(player.getUniqueId());
    }
    
    // Get player's clan
    public Clan getClan(Player player) {
        return clanManager.getClanByMember(player.getUniqueId());
    }
    
    // Get clan member data
    public Member getMember(Player player) {
        Clan clan = getClan(player);
        return clan != null ? clan.getMember(player.getUniqueId()) : null;
    }
    
    // Add experience to clan
    public void addClanExp(Clan clan, int amount) {
        clan.addExp(amount, addon.getClansPlugin().getCfg().getLevels());
    }
    
    // Give coins to member
    public void giveCoins(Member member, int amount) {
        member.addCoin(amount);
    }
    
    // Add money to clan balance
    public void addClanBalance(Clan clan, double amount) {
        clanManager.addBalance(amount, clan);
    }
}
```

#### 4. Custom GUI Example
```java
import me.jetby.treexclans.api.gui.GuiApi;
import me.jetby.treexclans.gui.Gui;
import me.jetby.treexclans.gui.Menu;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.clan.Clan;
import org.bukkit.entity.Player;

public class MyCustomGui extends Gui {

    public MyCustomGui(TreexClans plugin, Menu menu, Player player, Clan clan) {
        super(plugin, menu, player, clan);
        registerButtons();
    }
    
    @Override
    protected void onClick(Player player, Button button, GuiItemController controller) {
        // Handle button clicks
        if (button.type().equals("my_custom_button")) {
            player.sendMessage("Custom button clicked!");
        }
    }
}

// Register the GUI in your addon
@Override
public void onEnable() {
    GuiApi.registerGui("my_custom_gui", (plugin, menu, player, clan, objects) ->
        new MyCustomGui(plugin, menu, player, clan)
    );
}
```

#### 5. Custom Command Example
```java
import me.jetby.treexclans.api.CustomCommandApi;
import me.jetby.treexclans.commands.Subcommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class MyCustomCommand implements Subcommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }
        
        // Your command logic here
        player.sendMessage("Custom command executed!");
        return true;
    }

    @Override
    public @Nullable List<String> onTabCompleter(@NotNull CommandSender sender, 
                                                   @NotNull Command command, 
                                                   @NotNull String s, 
                                                   @NotNull String[] args) {
        return List.of("option1", "option2", "option3");
    }

    @Override
    public CustomCommandApi.CommandType type() {
        return CustomCommandApi.CommandType.CLAN; // or ADMIN
    }
}

// Register the command in your addon
@Override
public void onEnable() {
    CustomCommandApi.register("mycommand", new MyCustomCommand());
}

@Override
public void onDisable() {
    CustomCommandApi.unregister("mycommand");
}
```

#### 6. Listening to Clan Events
```java
import me.jetby.treexclans.api.events.OnClanCreate;
import me.jetby.treexclans.api.events.OnClanDelete;
import me.jetby.treexclans.api.events.OnQuestComplete;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MyListener implements Listener {

    private final MyAddon addon;
    
    public MyListener(MyAddon addon) {
        this.addon = addon;
    }

    @EventHandler
    public void onClanCreate(OnClanCreate event) {
        Clan clan = event.getClan();
        Player player = event.getPlayer();
        
        if (player != null) {
            player.sendMessage("Welcome to your new clan: " + clan.getId());
        }
    }

    @EventHandler
    public void onClanDelete(OnClanDelete event) {
        Clan clan = event.getClan();
        // Cleanup your addon data for this clan
    }

    @EventHandler
    public void onQuestComplete(OnQuestComplete event) {
        Quest quest = event.getQuest();
        Player player = event.getPlayer();
        Clan clan = event.getClan();
        
        if (player != null) {
            player.sendMessage("Quest completed: " + quest.name());
        }
    }
}
```

#### 7. Complete Addon Example
```java
package com.example.clanaddon;

import me.jetby.treexclans.addons.TreexAddon;
import me.jetby.treexclans.api.CustomCommandApi;
import me.jetby.treexclans.api.gui.GuiApi;
import me.jetby.treexclans.clan.Clan;
import me.jetby.treexclans.clan.Member;
import me.jetby.treexclans.commands.Subcommand;
import me.jetby.treexclans.gui.Gui;
import me.jetby.treexclans.gui.Menu;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ExampleAddon extends TreexAddon implements Listener {

    private FileConfiguration config;

    @Override
    public void onEnable() {
        // Load config
        config = getConfig();
        saveConfig();
        
        // Register events
        Bukkit.getPluginManager().registerEvents(this, getClansPlugin());
        
        // Register custom command
        CustomCommandApi.register("stats", new StatsCommand());
        
        // Register custom GUI
        GuiApi.registerGui("example_gui", this::createExampleGui);
        
        getClansPlugin().getLogger().info(getName() + " v" + getVersion() + " enabled!");
    }

    @Override
    public void onDisable() {
        // Unregister custom command
        CustomCommandApi.unregister("stats");
        
        // Unregister custom GUI
        GuiApi.unregisterGui("example_gui");
        
        getClansPlugin().getLogger().info(getName() + " disabled!");
    }
    
    private Gui createExampleGui(TreexClans plugin, Menu menu, Player player, Clan clan, Object... args) {
        return new ExampleGui(plugin, menu, player, clan);
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        if (getClansPlugin().getClanManager().isInClan(player.getUniqueId())) {
            Clan clan = getClansPlugin().getClanManager().getClanByMember(player.getUniqueId());
            player.sendMessage("Welcome back to clan: " + clan.getId());
        }
    }
    
    // Custom Stats Command
    private class StatsCommand implements Subcommand {
        
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can use this command!");
                return true;
            }
            
            if (!getClansPlugin().getClanManager().isInClan(player.getUniqueId())) {
                player.sendMessage("You are not in a clan!");
                return true;
            }
            
            Clan clan = getClansPlugin().getClanManager().getClanByMember(player.getUniqueId());
            Member member = clan.getMember(player.getUniqueId());
            
            player.sendMessage("§6§lYour Stats:");
            player.sendMessage("§7Kills: §e" + member.getKills());
            player.sendMessage("§7Deaths: §e" + member.getDeaths());
            player.sendMessage("§7Coins: §e" + member.getCoin());
            player.sendMessage("§7Experience: §e" + member.getExp());
            
            return true;
        }
        
        @Override
        public @Nullable List<String> onTabCompleter(@NotNull CommandSender sender, 
                                                      @NotNull Command command, 
                                                      @NotNull String s, 
                                                      @NotNull String[] args) {
            return List.of();
        }
        
        @Override
        public CustomCommandApi.CommandType type() {
            return CustomCommandApi.CommandType.CLAN;
        }
    }
    
    // Custom GUI
    private class ExampleGui extends Gui {
        
        public ExampleGui(TreexClans plugin, Menu menu, Player player, Clan clan) {
            super(plugin, menu, player, clan);
            registerButtons();
        }
    }
}
```

### Building Your Addon

1. Compile your addon as a JAR file
2. Place it in `plugins/TreexClans/addons/` folder
3. Restart the server or use `/xclan reload`

---
<div align="center">

Made with ❤️ by MrJetby

</div>
