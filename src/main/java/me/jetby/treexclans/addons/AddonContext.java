package me.jetby.treexclans.addons;

import me.jetby.treexclans.TreexClans;

import java.io.File;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Контекст инициализации аддона.
 * <p>Передаётся только при вызове {@link TreexAddon#initialize(AddonContext)}.</p>
 */
public record AddonContext(
        TreexClans plugin,
        Logger logger,
        File addonsFolder,
        Function<String, TreexAddon> resolve
) {}
