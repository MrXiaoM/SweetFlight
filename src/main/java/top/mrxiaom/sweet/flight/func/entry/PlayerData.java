package top.mrxiaom.sweet.flight.func.entry;

import org.bukkit.entity.Player;
import top.mrxiaom.sweet.flight.func.display.IBarDisplay;

import java.time.LocalDateTime;

public class PlayerData {
    public final Player player;
    /**
     * 玩家剩余的基础飞行时间
     */
    public int status;
    /**
     * 玩家剩余的额外飞行时间
     */
    public int extra;
    /**
     * 数据到期时间，过了这个时间后，将重置剩余基础飞行时间
     */
    public LocalDateTime outdate;
    /**
     * 玩家的 BOSS 血条实例
     */
    public IBarDisplay bossBar;
    /**
     * 存档计数器，减少到 0 之后将上传数据到数据库
     */
    public int saveCounter = 60;

    public PlayerData(Player player, int status, int extra, LocalDateTime outdate) {
        this.player = player;
        this.status = status;
        this.extra = extra;
        this.outdate = outdate;
    }
}
