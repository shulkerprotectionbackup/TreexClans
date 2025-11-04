package me.jetby.treexclans.commands.admin.subcommands;


import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.api.CustomCommandApi;
import me.jetby.treexclans.clan.Clan;
import me.jetby.treexclans.commands.Subcommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ExpSubcommand implements Subcommand {
    private final TreexClans plugin = TreexClans.getInstance();
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {

        if (args.length==0) {
            sender.sendMessage("/xclan exp give/set/take <clan> <amount>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give": {
                if (args.length<2) break;
                String clanName = args[1];
                int amount = Integer.parseInt(args[2]);
                Clan clan = plugin.getClanManager().getClan(clanName);
                if (clan==null) break;
                if (amount<1) break;
                clan.addExp(amount, plugin.getCfg().getLevels());
                sender.sendMessage("Clan "+clanName+" has "+clan.getExp()+ " now.");
                break;
            }
            case "set": {
                if (args.length<2) break;
                String clanName = args[1];
                int amount = Integer.parseInt(args[2]);
                Clan clan = plugin.getClanManager().getClan(clanName);
                if (clan==null) break;
                if (amount<0) amount = 0;
                clan.setExp(amount);
                sender.sendMessage("Clan "+clanName+" has "+clan.getExp()+ " now.");
                break;
            }
            case "take": {
                if (args.length<2) break;
                String clanName = args[1];
                int amount = Integer.parseInt(args[2]);
                Clan clan = plugin.getClanManager().getClan(clanName);
                if (clan==null) break;
                if (amount<1) break;
                clan.takeExp(amount);
                sender.sendMessage("Clan "+clanName+" has "+clan.getExp()+ " now.");
                break;
            }
            default: {
                sender.sendMessage("/xclan exp give/set/take <clan> <amount>");
                break;
            }
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabCompleter(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        return List.of();
    }

    @Override
    public CustomCommandApi.CommandType type() {
        return CustomCommandApi.CommandType.ADMIN;
    }
}
