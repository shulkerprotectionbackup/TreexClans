package me.jetby.treexclans.commands.admin;

import lombok.Getter;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.commands.Subcommand;
import me.jetby.treexclans.commands.admin.subcommands.ReloadSubcommand;

public enum AdminCommandArgs {
    RELOAD(new ReloadSubcommand(TreexClans.getInstance()));

    @Getter
    private Subcommand subcommand;

    AdminCommandArgs(Subcommand subcommand) {
        this.subcommand = subcommand;
    }
}
