package me.jetby.treexclans.gui;

import lombok.experimental.UtilityClass;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.api.gui.GuiApi;
import me.jetby.treexclans.clan.Clan;
import me.jetby.treexclans.clan.Member;
import me.jetby.treexclans.clan.rank.Rank;
import me.jetby.treexclans.functions.tops.TopType;
import me.jetby.treexclans.gui.core.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class GuiFactory {

    public Gui create(TreexClans plugin,
                      @NotNull Menu menu,
                      @NotNull Player player,
                      @NotNull Clan clan,
                      Object... customObjects) {

        String guiType = menu.type();

        Gui builtInGui = createBuiltInGui(plugin, menu, player, clan, guiType, customObjects);
        if (builtInGui != null) {
            return builtInGui;
        }

        Gui customGui = GuiApi.createGui(guiType, plugin, menu, player, clan, customObjects);
        if (customGui != null) {
            return customGui;
        }

        TreexClans.LOGGER.warn("GUI type '" + guiType + "' not found! Returning DefaultGui instead.");
        return new DefaultGui(plugin, menu, player, clan);
    }

    private Gui createBuiltInGui(TreexClans plugin,
                                 @NotNull Menu menu,
                                 @NotNull Player player,
                                 @NotNull Clan clan,
                                 String guiType,
                                 Object... customObjects) {
        try {
            GuiType type = GuiType.valueOf(guiType);

            return switch (type) {
                case MEMBERS -> new MembersGui(plugin, menu, player, clan);
                case CHOOSE_COLOR -> {
                    if (customObjects != null) {
                        for (Object obj : customObjects) {
                            if (obj instanceof Member target) {
                                yield new ChooseColorGui(plugin, menu, player, clan, target);
                            }
                        }
                    }
                    yield new ChooseColorGui(plugin, menu, player, clan, null);
                }

                case CHEST -> new ChestGui(plugin, menu, player, clan);

                case QUESTS -> new QuestsGui(plugin, menu, player, clan);

                case RANKS -> new RanksGui(plugin, menu, player, clan);

                case RANK_PERMISSIONS -> {
                    if (customObjects != null) {
                        for (Object obj : customObjects) {
                            if (obj instanceof Rank rank) {
                                yield new RankPermissionsGui(plugin, menu, player, clan, rank);
                            }
                        }
                    }
                    yield null;
                }

                case CHOOSE_PLAYER_COLOR -> new ChoosePlayerColorGui(plugin, menu, player, clan);

                case MENU, DEFAULT -> new DefaultGui(plugin, menu, player, clan);

                case TOP_CLANS -> {
                    if (customObjects != null) {
                        TopType topType = null;
                        int num = 1;
                        for (Object obj : customObjects) {
                            if (obj instanceof TopType t) topType = t;
                            if (obj instanceof Integer i) num = i;
                        }
                        yield new TopClansGui(plugin, menu, player, clan, topType, num);
                    }
                    yield new TopClansGui(plugin, menu, player, clan, null, 1);
                }
            };
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}