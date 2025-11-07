package me.jetby.treexclans.addons.configuration;

import org.bukkit.configuration.file.FileConfiguration;

public interface ServiceConfiguration {

    FileConfiguration getConfig();
    void saveConfig();

}
