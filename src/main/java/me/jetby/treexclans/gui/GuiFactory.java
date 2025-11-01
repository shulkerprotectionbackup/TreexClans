package me.jetby.treexclans.gui;

import lombok.experimental.UtilityClass;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.clan.Clan;
import me.jetby.treexclans.clan.Member;
import me.jetby.treexclans.clan.rank.Rank;
import me.jetby.treexclans.functions.tops.TopType;
import me.jetby.treexclans.gui.core.*;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class GuiFactory {
    private final Map<String, Gui> customGuis = new HashMap<>();

    public Gui create(TreexClans plugin,
                      Menu menu,
                      Player player,
                      Clan clan,
                      Object... customObjects) {
        switch (GuiType.valueOf(menu.type())) {
            case MEMBERS -> {
                return new MembersGui(plugin, menu, player, clan);
            }
            case CHOOSE_COLOR -> {
                if (customObjects!=null) {
                    for (Object obj : customObjects) {
                        if (obj instanceof Member target) return new ChooseColorGui(plugin, menu, player, clan, target);
                    }
                }
            }
            case CHEST -> {
                return new ChestGui(plugin, menu, player, clan);
            }
            case QUESTS -> {
                return new QuestsGui(plugin, menu, player, clan);
            }
            case RANKS -> {
                return new RanksGui(plugin, menu, player, clan);
            }
            case RANK_PERMISSIONS -> {
                if (customObjects!=null) {
                    for (Object obj : customObjects) {
                        if (obj instanceof Rank rank) return new RankPermissionsGui(plugin, menu, player, clan, rank);
                    }
                }
            }
            case CHOOSE_PLAYER_COLOR -> {
                if (customObjects!=null) {
                    for (Object obj : customObjects) {
                        if (obj instanceof Member target) return new ChoosePlayerColorGui(plugin, menu, player, clan, target);
                    }
                }
            }
            case MENU -> {
                return new DefaultGui(plugin, menu, player, clan);
            }
            case TOP_CLANS -> {
                if (customObjects!=null) {
                    TopType topType = null;
                    int num = 1;
                    for (Object obj : customObjects) {
                        if (obj instanceof TopType t) {
                            topType = t;
                        }
                        if (obj instanceof Integer i)  {
                            num = i;
                        }
                    }
                    return new TopClansGui(plugin, menu, player, clan, topType, num);
                }
            }
            default -> {
                return getCustomGuiOrDefault(plugin, menu, player, clan, menu.type());
            }
        }
        return null;
    }

    private Gui getCustomGuiOrDefault(TreexClans plugin, Menu menu, Player player, Clan clan, String type) {
        var gui = customGuis.get(type);
        if (gui!=null) return gui;

        return new DefaultGui(plugin, menu, player, clan);
    }

    public void registerCustomGui(String type, Gui gui) {
        customGuis.put(type.toUpperCase(), gui);
    }
    public void unregisterCustomGui(String type) {
        customGuis.remove(type.toUpperCase());
    }
}
