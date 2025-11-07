package me.jetby.treexclans.addons;

import me.jetby.treexclans.addons.service.ServiceManager;

import java.util.logging.Logger;

/**
 * Контекст инициализации аддона.
 * <p>Передаётся только при вызове {@link TreexAddon#initialize(AddonContext)}.</p>
 */
public record AddonContext(
        ServiceManager serviceManager,
        Logger logger
) {}
