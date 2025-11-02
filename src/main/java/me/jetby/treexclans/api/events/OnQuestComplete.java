package me.jetby.treexclans.api.events;

import lombok.Getter;
import me.jetby.treexclans.clan.Clan;
import me.jetby.treexclans.functions.quests.Quest;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class OnQuestComplete extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    @NotNull
    private final Clan clan;
    @Nullable
    private final Player player;
    @NotNull
    private final Quest quest;

    public OnQuestComplete(@NotNull Clan clan, @Nullable Player player, @NotNull Quest quest) {
        this.clan = clan;
        this.player = player;
        this.quest = quest;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        cancelled = b;
    }
}
