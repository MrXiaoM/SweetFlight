package top.mrxiaom.sweet.flight.func.display;

import org.bukkit.entity.Player;

public interface IBarDisplay {
    void removeAll();
    void addPlayer(Player player);
    void setProgress(double progress);
    void setTitle(String text);
}
