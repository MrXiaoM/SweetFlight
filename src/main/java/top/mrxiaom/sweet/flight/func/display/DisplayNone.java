package top.mrxiaom.sweet.flight.func.display;

import org.bukkit.entity.Player;

public class DisplayNone implements IBarDisplay {
    public static final DisplayNone INSTANCE = new DisplayNone();
    private DisplayNone() {}
    @Override
    public void removeAll() {
    }

    @Override
    public void addPlayer(Player player) {
    }

    @Override
    public void setProgress(double progress) {
    }

    @Override
    public void setTitle(String text) {
    }
}
