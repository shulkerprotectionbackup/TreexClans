package me.jetby.treexclans.gui.core;

import com.jodexindustries.jguiwrapper.api.item.ItemWrapper;
import com.jodexindustries.jguiwrapper.gui.advanced.GuiItemController;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.clan.Clan;
import me.jetby.treexclans.clan.Member;
import me.jetby.treexclans.gui.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ChoosePlayerColorGui extends Gui {
    public ChoosePlayerColorGui(TreexClans plugin, @Nullable Menu menu, Player player, Clan clan) {
        super(plugin, menu, player, clan);
        registerButtons();

        setupMembersPagination();

        openPage(0);
    }

    @Override
    protected void onRegister(Player player, Button button, GuiItemController.Builder builder) {
        if (button == null) return;
        switch (button.type().toLowerCase()) {
            case "players": {
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
    public boolean cancelRegistration(Player player, @javax.annotation.Nullable Button button) {
        return button != null && button.type().equals("players");
    }

    private void setupMembersPagination() {

        List<Button> memberButtons = getMenu().buttons().stream()
                .filter(b -> "players".equalsIgnoreCase(b.type()))
                .toList();

        List<Integer> sortedMemberSlots = memberButtons.stream().map(Button::slot).toList();
        if (memberButtons.isEmpty()) return;

        int itemsPerPage = sortedMemberSlots.size();

        List<Member> members = new ArrayList<>(getClan().getMembers());
        members.add(getClan().getLeader());
        members.removeIf(m -> m.getUuid().equals(getPlayer().getUniqueId()));


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
                if (member.equals(getClan().getMember(getPlayer().getUniqueId()))) continue;

                OfflinePlayer target = Bukkit.getOfflinePlayer(member.getUuid());

                setCustomPlaceholder("%target_name%", target.getName());

                consumers[i] = builder -> {
                    ItemStack itemStack = SkullCreator.itemFromName(target.getName());
                    ItemMeta meta = itemStack.getItemMeta();
                    meta.addItemFlags(ItemFlag.HIDE_DYE);
                    itemStack.setItemMeta(meta);

                    ItemWrapper wrapper = new ItemWrapper(itemStack);

                    wrapper.displayName(applyDefaultPlaceholders(button.displayName()));
                    List<String> processedLore = button.lore().stream()
                            .map(this::applyDefaultPlaceholders)
                            .collect(Collectors.toList());
                    wrapper.lore(processedLore);

                    wrapper.customModelData(button.customModelData());
                    wrapper.enchanted(button.enchanted());
                    wrapper.update();

                    builder.defaultItem(wrapper);
                    builder.slots(slot);
                    builder.defaultClickHandler((event, ctrl) -> {
                        event.setCancelled(true);

                        Bukkit.getScheduler().runTaskLater(getPlugin(), () ->
                                GuiFactory.create(
                                                getPlugin(),
                                                getPlugin().getGuiLoader().getMenus().get(button.openGui()),
                                                getPlayer(), getClan(), member)
                                        .open(getPlayer()), 1L);
                    });
                };
            }
            addPage(consumers);
        }
    }
}