package top.mrxiaom.sweet.flight.func.display;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import top.mrxiaom.pluginbase.utils.Util;

public class DisplayBossBar implements IBarDisplay {
    private final BossBar bossBar;
    public DisplayBossBar(String title, String barColor, String barStyle) {
        BarColor color = Util.valueOr(BarColor.class, barColor, BarColor.BLUE);
        BarStyle style = Util.valueOr(BarStyle.class, barStyle, BarStyle.SEGMENTED_10);
        this.bossBar = Bukkit.createBossBar(title, color, style);
    }
    @Override
    public void removeAll() {
        bossBar.removeAll();
    }

    @Override
    public void addPlayer(Player player) {
        bossBar.addPlayer(player);
    }

    @Override
    public void setProgress(double progress) {
        bossBar.setProgress(progress);
    }

    @Override
    public void setTitle(String text) {
        bossBar.setTitle(text);
    }
}
