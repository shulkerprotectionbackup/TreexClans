package me.jetby.treexclans.addons;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IAddonManager {

    /**
     * Disables a specific addon (invokes onDisable).
     *
     * @param addon The addon instance to disable.
     * @return true if successfully disabled.
     */
    boolean disable(@NotNull TreexAddon addon);

    /**
     * Retrieves the loaded/enabled addon by ID.
     *
     * @param addonId The ID of the addon.
     * @return The addon instance or null if not found.
     */
    @Nullable
    TreexAddon getAddon(@NotNull String addonId);

    /**
     * Checks if the addon is loaded.
     *
     * @param addonId The ID of the addon.
     * @return true if the addon is loaded.
     */
    boolean isLoaded(@NotNull String addonId);
}