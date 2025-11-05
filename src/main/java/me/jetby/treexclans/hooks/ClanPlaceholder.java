package me.jetby.treexclans.hooks;

import lombok.Getter;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.clan.Clan;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClanPlaceholder extends PlaceholderExpansion {
    private final TreexClans plugin;
    @Getter
    private final boolean papi;

    public ClanPlaceholder(TreexClans plugin) {
        this.plugin = plugin;
        this.papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null && Bukkit.getPluginManager().getPlugin("PlaceholderAPI").isEnabled();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "clan";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.valueOf(plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String identifier) {
        String[] args = identifier.split("_");
        Clan clan = plugin.getClanManager().getClanByMember(player.getUniqueId());

        return switch (args[0].toLowerCase()) {
            case "tag" -> {
                if (!plugin.getClanManager().isInClan(player.getUniqueId()))
                    yield plugin.getCfg().getTagPlaceholder_noClan();

                yield plugin.getCfg().getTagPlaceholder_hasClan()
                        .replace("{tag}", clan.getId())
                        .replace("{prefix}", clan.getPrefix() == null ? "" : clan.getPrefix());
            }
            case "prefix" -> {
                if (!plugin.getClanManager().isInClan(player.getUniqueId()))
                    yield plugin.getCfg().getPrefixPlaceholder_noClan();
                if (clan.getPrefix() == null) yield plugin.getCfg().getPrefixPlaceholder_noPrefix()
                        .replace("{tag}", clan.getId());

                yield plugin.getCfg().getPrefixPlaceholder_hasPrefix()
                        .replace("{tag}", clan.getId())
                        .replace("{prefix}", clan.getPrefix());
            }
            case "coin" -> {
                if (!plugin.getClanManager().isInClan(player.getUniqueId())) yield "0";
                yield String.valueOf(clan.getMember(player.getUniqueId()).getCoin());
            }
            case "slogan" -> {
                if (!plugin.getClanManager().isInClan(player.getUniqueId())) yield "";
                yield clan.getSlogan();
            }
            case "balance" -> {
                if (!plugin.getClanManager().isInClan(player.getUniqueId())) yield "0";
                yield String.valueOf(clan.getBalance());
            }
            case "level" -> {
                if (!plugin.getClanManager().isInClan(player.getUniqueId())) yield "0";
                yield String.valueOf(clan.getLevel());
            }
            case "clan" -> {
                if (args[1].equalsIgnoreCase("exp")) {
                    if (!plugin.getClanManager().isInClan(player.getUniqueId())) yield "0";
                    yield String.valueOf(clan.getExp());
                }
                yield "";
            }
            case "exp" -> {
                if (!plugin.getClanManager().isInClan(player.getUniqueId())) yield "0";
                yield String.valueOf(clan.getMember(player.getUniqueId()).getExp());
            }
            default -> null;
        };
    }
}
