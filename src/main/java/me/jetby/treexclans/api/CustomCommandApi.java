package me.jetby.treexclans.api;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import me.jetby.treexclans.commands.Subcommand;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class CustomCommandApi {

    @Getter
    private final Map<String, Subcommand> subcommands = new HashMap<>();

    public void registerSubcommand(String name, Subcommand subcommand, CommandType commandType) {
        String lowerName = name.toLowerCase();
        subcommands.put(lowerName, subcommand);
    }

    public enum CommandType {
        CLAN,
        ADMIN
    }

}
