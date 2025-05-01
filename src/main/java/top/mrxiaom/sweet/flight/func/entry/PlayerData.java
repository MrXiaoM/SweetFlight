package top.mrxiaom.sweet.flight.func.entry;

import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;

public class PlayerData {
    public final Player player;
    public int status;
    public int extra;
    public LocalDateTime outdate;
    public BossBar bossBar;
    public int saveCounter = 60;

    public PlayerData(Player player, int status, int extra, LocalDateTime outdate) {
        this.player = player;
        this.status = status;
        this.extra = extra;
        this.outdate = outdate;
    }
}
