package me.jetby.treexclans.addons.exemple;

import me.jetby.treexclans.addons.TreexAddon;
import me.jetby.treexclans.addons.annotations.Dependency;
import me.jetby.treexclans.addons.annotations.TreexAddonInfo;

@TreexAddonInfo(
        id = "test-addon",
        version = "1.0.0",
        authors = {"JetBy"},
        depends = {
                @Dependency(id = "core-api", version = "^1.4.1"),
                @Dependency(id = "database", version = ">=2.0.0")
        },
        softDepends = {
                @Dependency(id = "economy")
        },
        loadBefore = {"promo-system"},
        loadAfter = {"core-api"}
)
public final class TestAddon extends TreexAddon {

    @Override
    public void onEnable() {
        this.getLogger().info("Test enabled!");

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
