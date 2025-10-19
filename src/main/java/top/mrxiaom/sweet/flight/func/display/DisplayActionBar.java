package top.mrxiaom.sweet.flight.func.display;

import org.bukkit.entity.Player;
import top.mrxiaom.pluginbase.utils.AdventureUtil;

import java.util.ArrayList;
import java.util.List;

public class DisplayActionBar implements IBarDisplay {
    private final List<Player> players = new ArrayList<>();
    private String title;
    public DisplayActionBar(String title) {
        this.title = title;
    }

    @Override
    public void removeAll() {
        for (Player player : players) {
            AdventureUtil.sendActionBar(player, " ");
        }
        players.clear();
    }

    @Override
    public void addPlayer(Player player) {
        players.add(player);
        send();
    }

    @Override
    public void setProgress(double progress) {
    }

    @Override
    public void setTitle(String text) {
        this.title = text;
        send();
    }

    private void send() {
        if (title == null) return;
        for (Player player : players) {
            AdventureUtil.sendActionBar(player, title);
        }
    }
}
