package me.jetby.treexclans.gui.core;

import com.jodexindustries.jguiwrapper.api.item.ItemWrapper;
import com.jodexindustries.jguiwrapper.gui.advanced.GuiItemController;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.clan.Clan;
import me.jetby.treexclans.clan.Member;
import me.jetby.treexclans.gui.Button;
import me.jetby.treexclans.gui.Gui;
import me.jetby.treexclans.gui.Menu;
import me.jetby.treexclans.gui.SkullCreator;
import me.jetby.treexclans.tools.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static me.jetby.treexclans.TreexClans.NAMESPACED_KEY;

public class MembersGui extends Gui {


    public MembersGui(TreexClans plugin, Menu menu, Player player, Clan clan) {
        super(plugin, menu, player, clan);
        registerButtons();

        setupMembersPagination();

        openPage(0);
    }

    @Override
    protected void onRegister(Player player, Button button, GuiItemController.Builder builder) {
        if (button == null) return;
        switch (button.type().toLowerCase()) {
            case "members": {
                break;
            }
            case "leader": {
                Member leaderMember = getClan().getLeader();
                if (leaderMember == null) {
                    builder.defaultItem(ItemWrapper.builder(Material.AIR).build());
                    break;
                }
                replaceMemberPlaceholders(leaderMember);

                OfflinePlayer target = Bukkit.getOfflinePlayer(leaderMember.getUuid());
                ItemStack itemStack = SkullCreator.itemFromName(target.getName());
                ItemWrapper wrapper = new ItemWrapper(itemStack);

                wrapper.displayName(applyDefaultPlaceholders(button.displayName()));

                wrapper.lore(button.lore().stream()
                        .map(this::applyDefaultPlaceholders)
                        .collect(Collectors.toList()));

                wrapper.customModelData(button.customModelData());
                wrapper.enchanted(button.enchanted());
                wrapper.update();

                ItemMeta itemMeta = itemStack.getItemMeta();
                if (itemMeta != null) {
                    itemMeta.getPersistentDataContainer().set(NAMESPACED_KEY, PersistentDataType.STRING, "menu_item");
                    itemStack.setItemMeta(itemMeta);
                }

                builder.defaultItem(wrapper);
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
        return button != null && button.type().equals("members");
    }

    private void setupMembersPagination() {

        List<Button> memberButtons = getMenu().buttons().stream()
                .filter(b -> "members".equalsIgnoreCase(b.type()))
                .toList();

        List<Integer> sortedMemberSlots = memberButtons.stream().map(Button::slot).toList();
        if (memberButtons.isEmpty()) return;

        int itemsPerPage = sortedMemberSlots.size();

        List<Member> members = getClan().getMembers().stream()
                .filter(m -> !m.equals(getClan().getLeader()))
                .toList();

        int totalPages = (int) Math.ceil((double) members.size() / itemsPerPage);

        Button button = memberButtons.get(0);

        for (int page = 0; page < totalPages; page++) {
            int start = page * itemsPerPage;
            int end = Math.min(start + itemsPerPage, members.size());

            Consumer<GuiItemController.Builder>[] consumers = new Consumer[itemsPerPage];

            for (int i = 0; i < itemsPerPage; i++) {
                int memberIndex = start + i;
                int slot = sortedMemberSlots.get(i);

                if (memberIndex >= end) {
                    consumers[i] = builder -> {
                        builder.slots(slot);
                        builder.defaultItem(ItemWrapper.builder(Material.AIR).build());
                        builder.defaultClickHandler((event, ctrl) -> event.setCancelled(true));
                    };
                    continue;
                }

                Member member = members.get(memberIndex);
                OfflinePlayer target = Bukkit.getOfflinePlayer(member.getUuid());

                consumers[i] = builder -> {
                    replaceMemberPlaceholders(member);
                    ItemStack itemStack = SkullCreator.itemFromName(target.getName());
                    ItemWrapper wrapper = new ItemWrapper(itemStack);

                    wrapper.displayName(applyDefaultPlaceholders(button.displayName()));

                    wrapper.lore(button.lore().stream()
                            .map(this::applyDefaultPlaceholders)
                            .collect(Collectors.toList()));

                    wrapper.customModelData(button.customModelData());
                    wrapper.enchanted(button.enchanted());
                    wrapper.update();

                    builder.defaultItem(wrapper);
                    builder.slots(slot);
                    builder.defaultClickHandler((event, ctrl) -> event.setCancelled(true));
                };
            }
            if (consumers[page] == null) continue;

            addPage(consumers);
        }
    }

    private void replaceMemberPlaceholders(Member member) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(member.getUuid());
        setCustomPlaceholder("%joined-at%", getPlugin().getFormatTime().stringFormat(System.currentTimeMillis() - member.getJoinedAt()));
        setCustomPlaceholder("%last-online%", getPlugin().getClanManager().getLastOnlineFormatted(member));
        setCustomPlaceholder("%target_name%", offlinePlayer.getName());
        setCustomPlaceholder("%rank%", member.getRank().name());
        setCustomPlaceholder("%kills%", String.valueOf(member.getKills()));
        setCustomPlaceholder("%deaths%", String.valueOf(member.getDeaths()));
        setCustomPlaceholder("%kd%", calculateKD(member));
        setCustomPlaceholder("%exp%", String.valueOf(member.getExp()));
        setCustomPlaceholder("%coin%", String.valueOf(member.getCoin()));
    }

    private String calculateKD(Member member) {
        int kills = member.getKills();
        int deaths = member.getDeaths();
        return deaths == 0 ? kills + "" : NumberUtils.formatWithCommas((double) kills / deaths);
    }
}