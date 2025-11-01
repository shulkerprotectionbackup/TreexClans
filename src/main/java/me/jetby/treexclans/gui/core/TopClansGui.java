package me.jetby.treexclans.gui.core;

import com.jodexindustries.jguiwrapper.api.item.ItemWrapper;
import com.jodexindustries.jguiwrapper.api.placeholder.PlaceholderEngine;
import com.jodexindustries.jguiwrapper.gui.advanced.GuiItemController;
import me.jetby.treex.text.Colorize;
import me.jetby.treex.text.Papi;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.clan.Clan;
import me.jetby.treexclans.clan.Member;
import me.jetby.treexclans.functions.tops.TopType;
import me.jetby.treexclans.gui.*;
import me.jetby.treexclans.tools.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static me.jetby.treexclans.TreexClans.LOGGER;
import static me.jetby.treexclans.TreexClans.NAMESPACED_KEY;

public class TopClansGui extends Gui {

    private final TopType currentSort;
    private int s;

    public TopClansGui(TreexClans plugin, @Nullable Menu menu, Player player, Clan clan, TopType topType, int s) {
        super(plugin, menu, player, clan);
        this.s = s;
        this.currentSort = Objects.requireNonNullElse(topType, TopType.KILLS);
        registerButtons();
        setupMembersPagination();
        openPage(0);
    }

    @Override
    protected void onRegister(Player player, Button button, GuiItemController.Builder builder) {
        if (button == null) return;
        switch (button.type().toLowerCase()) {
            case "clans": {
                break;
            }
            case "filter": {
                if (s+1>getTops(button).size()) s = 0;
                builder.defaultItem(ItemWrapper.builder(button.itemStack().getType())
                                .displayName(button.displayName())
                                .lore(button.lore().stream().map(s1 -> setPlaceholders(s1, null)).toList())
                        .build());
                builder.defaultClickHandler((event, controller) -> {
                    close(player);
                    Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
                        GuiFactory.create(getPlugin(), getMenu(), player, getClan(), getTops(button).get(s), s+1).open(player);
                    }, 1L);
                });
                break;
            }
            case "next_page": {
                builder.defaultClickHandler((e, gui) -> {
                    e.setCancelled(true);
                    nextPage();
                });
                break;
            }
            case "prev_page": {
                builder.defaultClickHandler((e, gui) -> {
                    e.setCancelled(true);
                    previousPage();
                });
                break;
            }
        }
    }

    @Override
    public boolean cancelRegistration(Player player, @Nullable Button button) {
        return button != null && (button.type().equals("clans"));
    }

    private void setupMembersPagination() {
        List<Button> clanButtons = getMenu().buttons().stream()
                .filter(b -> "clans".equalsIgnoreCase(b.type()))
                .toList();

        List<Integer> sortedClansSlots = clanButtons.stream().map(Button::slot).toList();
        if (clanButtons.isEmpty()) return;

        int itemsPerPage = sortedClansSlots.size();

        List<Clan> clans = new ArrayList<>();
        int a = 1;
        for (Button button : clanButtons) {
            Clan clan = getPlugin().getTopManager().getTopClan(currentSort, a);
            if (clan != null) {
                clans.add(clan);
            }
            a++;
        }

        if (clans.isEmpty()) return;

        int totalPages = (int) Math.ceil((double) clans.size() / itemsPerPage);

        Button button = clanButtons.get(0);

        for (int page = 0; page < totalPages; page++) {
            int start = page * itemsPerPage;
            int end = Math.min(start + itemsPerPage, clans.size());

            Consumer<GuiItemController.Builder>[] consumers = new Consumer[itemsPerPage];

            int topNum = 0;
            for (int i = 0; i < itemsPerPage; i++) {
                int clanIndex = start + i;
                int slot = sortedClansSlots.get(i);
                topNum++;
                if (clanIndex >= end) {
                    consumers[i] = builder -> {
                        builder.slots(slot);
                        builder.defaultItem(ItemWrapper.builder(Material.AIR).build());
                        builder.defaultClickHandler((event, ctrl) -> event.setCancelled(true));
                    };
                    continue;
                }

                final Clan clan = clans.get(clanIndex);
                if (clan == null) {
                    consumers[i] = builder -> {
                        builder.slots(slot);
                        builder.defaultItem(ItemWrapper.builder(Material.AIR).build());
                        builder.defaultClickHandler((event, ctrl) -> event.setCancelled(true));
                    };
                    continue;
                }

                int finalTopNum = topNum;
                consumers[i] = builder -> {
                    ItemStack itemStack = SkullCreator.itemFromUuid(clan.getLeader().getUuid());
                    ItemMeta meta = itemStack.getItemMeta();
                    meta.getPersistentDataContainer().set(NAMESPACED_KEY, PersistentDataType.STRING, "clans");
                    itemStack.setItemMeta(meta);
                    ItemWrapper wrapper = new ItemWrapper(itemStack);

                    String processedDisplayName = setPlaceholders(
                            applyDefaultPlaceholders(button.displayName()),
                            clan
                    );
                    processedDisplayName = Papi.setPapi(getPlayer(), processedDisplayName);
                    processedDisplayName = processedDisplayName.replace("%top_num%", String.valueOf(finalTopNum));
                    wrapper.displayName(Colorize.text(processedDisplayName));

                    List<String> processedLore = button.lore().stream()
                            .map(this::applyDefaultPlaceholders)
                            .map(l -> setPlaceholders(l, clan))
                            .map(l -> Papi.setPapi(getPlayer(), l))
                            .map(l -> l.replace("%top_num%", String.valueOf(finalTopNum)))
                            .map(Colorize::text)
                            .collect(Collectors.toList());
                    wrapper.lore(processedLore);

                    wrapper.customModelData(button.customModelData());
                    wrapper.enchanted(button.enchanted());
                    wrapper.update();

                    builder.defaultItem(wrapper);
                    builder.slots(slot);
                    builder.defaultClickHandler((event, ctrl) -> event.setCancelled(true));
                };
            }

            addPage(consumers);

        }
    }

    private List<TopType> getTops(Button button) {
        List<TopType> list = new ArrayList<>();
        for (String s : button.lore()) {
            if (s.contains("%top_kills_set%")) {
                list.add(TopType.KILLS);
                continue;
            }
            if (s.contains("%top_deaths_set%")) {
                list.add(TopType.DEATHS);
                continue;
            }
            if (s.contains("%top_kd_set%")) {
                list.add(TopType.KD);
                continue;
            }
            if (s.contains("%top_balance_set%")) {
                list.add(TopType.BALANCE);
                continue;
            }
            if (s.contains("%top_level_set%")) {
                list.add(TopType.LEVEL);
                continue;
            }
            if (s.contains("%top_members_set%")) {
                list.add(TopType.MEMBERS);
            }

        }

        return list;
    }
    private String setPlaceholders(String text, Clan clan) {
        if (text == null) return null;

        if (currentSort==TopType.KILLS) {
            text = text.replace("%top_kills_set%", getPlugin().getLang().getMessage("gui.tops.kills.set"));
        } else {
            text = text.replace("%top_kills_set%", getPlugin().getLang().getMessage("gui.tops.kills.unset"));
        }
        if (currentSort==TopType.DEATHS) {
            text = text.replace("%top_deaths_set%", getPlugin().getLang().getMessage("gui.tops.deaths.set"));
        } else {
            text = text.replace("%top_deaths_set%", getPlugin().getLang().getMessage("gui.tops.deaths.unset"));
        }
        if (currentSort==TopType.KD) {
            text = text.replace("%top_kd_set%", getPlugin().getLang().getMessage("gui.tops.kd.set"));
        } else {
            text = text.replace("%top_kd_set%", getPlugin().getLang().getMessage("gui.tops.kd.unset"));
        }
        if (currentSort==TopType.BALANCE) {
            text = text.replace("%top_balance_set%", getPlugin().getLang().getMessage("gui.tops.balance.set"));
        } else {
            text = text.replace("%top_balance_set%", getPlugin().getLang().getMessage("gui.tops.balance.unset"));
        }
        if (currentSort==TopType.LEVEL) {
            text = text.replace("%top_level_set%", getPlugin().getLang().getMessage("gui.tops.level.set"));
        } else {
            text = text.replace("%top_level_set%", getPlugin().getLang().getMessage("gui.tops.level.unset"));
        }
        if (currentSort==TopType.MEMBERS) {
            text = text.replace("%top_members_set%", getPlugin().getLang().getMessage("gui.tops.members.set"));
        } else {
            text = text.replace("%top_members_set%", getPlugin().getLang().getMessage("gui.tops.members.unset"));
        }


        if (clan==null) return text;

        text = text.replace("%level%", clan.getLevel().id());

        int kills = 0;
        int deaths = 0;
        for (Member member : clan.getMembersWithLeader()) {
            kills += member.getKills();
            deaths += member.getDeaths();
        }

        if (clan.getPrefix() != null) {
            text = text.replace("%prefix%", clan.getPrefix());
        } else {
            text = text.replace("%prefix%", clan.getId().toUpperCase());
        }

        OfflinePlayer leader = Bukkit.getOfflinePlayer(clan.getLeader().getUuid());
        String leaderName = leader.getName() != null ? leader.getName() : "Unknown";
        text = text.replace("%leader_name%", leaderName);
        text = text.replace("%kills%", String.valueOf(kills));
        text = text.replace("%deaths%", String.valueOf(deaths));
        text = text.replace("%kd%", calculateKD(kills, deaths));
        text = text.replace("%balance%", String.valueOf(clan.getBalance()));



        return text;
    }

    private String calculateKD(int kills, int deaths) {
        return deaths == 0 ? kills + "" : NumberUtils.formatWithCommas((double) kills / deaths);
    }

    private double calculateClanKD(Clan clan) {
        int kills = clan.getMembersWithLeader().stream().mapToInt(Member::getKills).sum();
        int deaths = clan.getMembersWithLeader().stream().mapToInt(Member::getDeaths).sum();
        return deaths == 0 ? kills : (double) kills / deaths;
    }
}