package top.mrxiaom.sweet.flight.func;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.AdventureUtil;
import top.mrxiaom.pluginbase.utils.ColorHelper;
import top.mrxiaom.pluginbase.utils.PAPI;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.flight.Messages;
import top.mrxiaom.sweet.flight.SweetFlight;
import top.mrxiaom.sweet.flight.api.EnumWorldMode;
import top.mrxiaom.sweet.flight.api.IFlyChecker;
import top.mrxiaom.sweet.flight.database.FlightDatabase;
import top.mrxiaom.sweet.flight.func.display.*;
import top.mrxiaom.sweet.flight.func.entry.PlayerData;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@AutoRegister
public class FlightManager extends AbstractModule implements Listener {
    private LocalTime resetTime;
    private Map<UUID, PlayerData> players = new HashMap<>();
    private EnumDisplayMode bossBarDisplayMode;
    private String bossBarFlying, bossBarColor, bossBarStyle;
    private String formatHour, formatHours, formatMinute, formatMinutes, formatSecond, formatSeconds, formatInfinite;
    private List<Player> toLoad = new ArrayList<>();
    private List<IFlyChecker> flyCheckers = new ArrayList<>();
    private List<String> timeConsumeOrder = new ArrayList<>();
    private EnumWorldMode worldMode;
    private final Set<String> worldWhiteList = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    private final Set<String> worldBlackList = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    public FlightManager(SweetFlight plugin) {
        super(plugin);
        toLoad.addAll(Bukkit.getOnlinePlayers());
        registerEvents();
        plugin.getScheduler().runTaskTimer(this::everySecond, 20L, 20L);
    }

    public PlayerData get(Player player) {
        return players.get(player.getUniqueId());
    }

    public PlayerData getOrCreate(Player player) {
        return players.computeIfAbsent(player.getUniqueId(), uuid -> new PlayerData(player, 0, 0, LocalDateTime.now()));
    }

    /**
     * 注册飞行兼容检查器
     */
    public void registerChecker(IFlyChecker checker) {
        flyCheckers.add(checker);
        flyCheckers.sort(Comparator.comparingInt(IFlyChecker::priority));
    }

    /**
     * 世界是否处于 <code>config.yml</code> 定义的“指定世界”中
     * @see FlightManager#isEnabledWorld(String)
     */
    public boolean isEnabledWorld(World world) {
        return isEnabledWorld(world.getName());
    }

    /**
     * 世界是否处于 <code>config.yml</code> 定义的“指定世界”中
     * @param worldName 世界名
     */
    public boolean isEnabledWorld(String worldName) {
        if (worldBlackList.contains(worldName)) return false;
        if (worldWhiteList.isEmpty()) return true;
        return worldWhiteList.contains(worldName);
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        String timeStr = config.getString("reset-time", "4:00:00");
        String[] split = timeStr.split(":", 3);
        LocalTime resetTime = null;
        try {
            if (split.length == 3) {
                int hour = Integer.parseInt(split[0]);
                int minute = Integer.parseInt(split[1]);
                int second = Integer.parseInt(split[2]);
                resetTime = LocalTime.of(hour, minute, second);
            } else if (split.length == 2) {
                int hour = Integer.parseInt(split[0]);
                int minute = Integer.parseInt(split[1]);
                resetTime = LocalTime.of(hour, minute);
            }
        } catch (NumberFormatException ignored) {
        }
        this.resetTime = resetTime;

        EnumWorldMode worldMode = Util.valueOrNull(EnumWorldMode.class, config.getString("worlds.mode"));
        if (worldMode == null) {
            this.worldMode = EnumWorldMode.HANDLE_ALL;
            warn("配置 worlds.mode 的值无效，已使用缺省值 HANDLE_ALL");
        } else {
            this.worldMode = worldMode;
        }
        worldWhiteList.clear();
        worldBlackList.clear();
        worldWhiteList.addAll(config.getStringList("worlds.whitelist"));
        worldBlackList.addAll(config.getStringList("worlds.blacklist"));

        this.bossBarFlying = config.getString("boss-bar.flying", "");
        this.bossBarColor = config.getString("boss-bar.color", "BLUE");
        this.bossBarStyle = config.getString("boss-bar.style", "SEGMENTED_10");
        EnumDisplayMode bossBarDisplayMode = Util.valueOr(EnumDisplayMode.class,
                config.getString("boss-bar.display-mode", "BOSS_BAR"),
                EnumDisplayMode.BOSS_BAR);
        if (bossBarDisplayMode.equals(EnumDisplayMode.BOSS_BAR) && !Util.isPresent("org.bukkit.boss.BossBar")) {
            // 如果当前版本不支持新版 BOSS 血条 (1.8)，不再对其支持，改用物品栏上方消息
            bossBarDisplayMode = EnumDisplayMode.ACTION_BAR;
        }
        boolean shouldUpdateBossBar = false;
        if (!bossBarDisplayMode.equals(this.bossBarDisplayMode)) {
            // 如果显示方式发生变动，移除所有人的当前剩余时间显示
            for (PlayerData data : players.values()) {
                if (data.bossBar != null) {
                    data.bossBar.removeAll();
                    data.bossBar = null;
                }
            }
            shouldUpdateBossBar = true;
        } else if (this.bossBarDisplayMode.equals(EnumDisplayMode.BOSS_BAR)) {
            for (PlayerData data : players.values()) {
                if (data.bossBar instanceof DisplayBossBar) {
                    DisplayBossBar display = (DisplayBossBar) data.bossBar;
                    display.updateStyle(bossBarColor, bossBarStyle);
                }
            }
        }
        this.bossBarDisplayMode = bossBarDisplayMode;
        if (shouldUpdateBossBar) {
            for (PlayerData data : players.values()) {
                if (isGameModeCannotFly(data.player)) {
                    int standard = GroupManager.inst().getFlightSeconds(data.player);
                    updateBossBar(data, standard);
                }
            }
        }

        this.formatHour = config.getString("time-format.hour", "%d时");
        this.formatHours = config.getString("time-format.hours", "%d时");
        this.formatMinute = config.getString("time-format.minute", "%d分");
        this.formatMinutes = config.getString("time-format.minutes", "%d分");
        this.formatSecond = config.getString("time-format.second", "%d秒");
        this.formatSeconds = config.getString("time-format.seconds", "%d秒");
        this.formatInfinite = config.getString("time-format.infinite", "无限");

        this.timeConsumeOrder.clear();
        this.timeConsumeOrder.addAll(config.getStringList("time-consume-order"));
        if (!timeConsumeOrder.contains("extra")) {
            timeConsumeOrder.add("extra");
            warn("time-consume-order 中未发现 extra");
        }
        if (!timeConsumeOrder.contains("standard")) {
            timeConsumeOrder.add("standard");
            warn("time-consume-order 中未发现 standard");
        }

        for (Player player : toLoad) {
            if (isEnabledWorld(player.getWorld())) {
                onJoin(player);
            } else {
                if (player.isFlying() || player.getAllowFlight()) {
                    Messages.flight__world_not_allow.tm(player);
                    player.setFlying(false);
                    player.setAllowFlight(false);
                }
            }
        }
        toLoad.clear();
    }

    /**
     * 获取下次数据到期时间
     */
    public LocalDateTime nextOutdate() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate date;
        LocalTime time = now.toLocalTime();
        if (time.isAfter(resetTime)) {
            return now.toLocalDate().plusDays(1).atTime(resetTime);
        } else {
            return now.toLocalDate().atTime(resetTime);
        }
    }

    /**
     * 【其它插件兼容】获取玩家在此位置是否可以飞行
     * @param player 玩家
     * @param loc 目标位置
     */
    public boolean canPlayerFlyAt(@NotNull Player player, @NotNull Location loc) {
        for (IFlyChecker checker : flyCheckers) {
            if (!checker.canPlayerFlyAt(player, loc)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 【其它插件兼容】获取玩家在此位置是否可以无限飞行
     * @param player 玩家
     * @param loc 目标位置
     */
    public boolean canInfiniteFly(@NotNull Player player, @NotNull Location loc) {
        for (IFlyChecker checker : flyCheckers) {
            if (checker.canInfiniteFly(player, loc)) {
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if (isEnabledWorld(player.getWorld())) {
            plugin.getScheduler().runTask(() -> onJoin(player));
        } else {
            if (player.isFlying() || player.getAllowFlight()) {
                Messages.flight__world_not_allow.tm(player);
                player.setFlying(false);
                player.setAllowFlight(false);
            }
        }
    }

    public void onJoin(Player player) {
        int standard = GroupManager.inst().getFlightSeconds(player);
        UUID uuid = player.getUniqueId();
        String id = plugin.key(player);
        int status, extra;
        LocalDateTime nextOutdate = nextOutdate();
        try (Connection conn = plugin.getConnection()) { // 拉取玩家数据
            FlightDatabase db = plugin.getFlightDatabase();
            Integer statusRaw = db.getPlayerStatus(conn, id);
            // 剩余基础飞行时间
            status = statusRaw == null ? standard : statusRaw;
            // 剩余额外飞行时间
            extra = db.getPlayerExtra(conn, id);
            players.put(uuid, new PlayerData(player, status, extra, nextOutdate));
        } catch (SQLException ex) {
            warn(ex);
            status = extra = 0;
        }
        if (!player.hasPermission("sweet.flight.bypass")) {
            Location loc = player.getLocation();
            // 其它插件不允许玩家在此飞行，关闭玩家的飞行
            if (!canPlayerFlyAt(player, loc)) {
                player.setAllowFlight(false);
                player.setFlying(false);
            } else {
                if (canInfiniteFly(player, loc)) { // 其它插件允许玩家无限飞行，不进行任何操作
                    return;
                }
                if (standard == -1) { // 无限飞行时间，开启飞行
                    player.setAllowFlight(true);
                } else {
                    if (extra == 0 && status == 0) { // 如果时间耗尽，提示并关闭飞行
                        if (standard > 0) {
                            Messages.time_not_enough__join.tm(player);
                        }
                        player.setFlying(false);
                        player.setAllowFlight(false);
                    } else { // 如果时间未耗尽，开启飞行
                        player.setAllowFlight(true);
                    }
                }
            }
        } else {
            player.setAllowFlight(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        PlayerData data = players.remove(e.getPlayer().getUniqueId());
        if (data != null) { // 离开游戏自动上传数据到数据库
            if (data.bossBar != null) {
                data.bossBar.removeAll();
                data.bossBar = null;
            }
            FlightDatabase db = plugin.getFlightDatabase();
            db.setPlayer(data);
        }
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent e) {
        Player player = e.getPlayer();

        // 如果从 “指定世界” 进入其它世界，关闭玩家飞行
        if (isEnabledWorld(e.getFrom()) && !isEnabledWorld(player.getWorld())) {
            Messages.flight__world_not_allow.tm(player);
            player.setFlying(false);
            player.setAllowFlight(false);
        }
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent e) {
        Player player = e.getPlayer();

        if (e.isFlying()) {
            boolean flag = isEnabledWorld(player.getWorld());
            switch (worldMode) {
                case HANDLE_ALL:
                    if (!flag) {
                        Messages.flight__world_not_allow.tm(player);
                        player.setAllowFlight(false);
                        e.setCancelled(true);
                        return;
                    }
                    break;
                case NOT_HANDLE:
                    if (!flag) {
                        return;
                    }
                    break;
            }
        }
        int standard = GroupManager.inst().getFlightSeconds(player);
        PlayerData data = players.get(player.getUniqueId());
        if (!e.isFlying()) { // 关闭飞行时移除血条
            if (data != null && data.bossBar != null) {
                data.bossBar.removeAll();
                data.bossBar = null;
            }
            return;
        }
        if (standard > 0) { // 如果并非无限飞行时间
            if (!player.hasPermission("sweet.flight.bypass")) {
                if (data == null) { // 如果数据异常，提醒玩家
                    Messages.player__data_invalid_start.tm(player);
                    e.setCancelled(true);
                    return;
                }
                Location loc = e.getPlayer().getLocation();
                // 如果其它插件不允许玩家飞行，则关闭飞行
                if (!canPlayerFlyAt(player, loc)) {
                    e.setCancelled(true);
                    return;
                }
                if (isGameModeCannotFly(player)) {
                    // 如果时间耗尽，且其它插件没有允许玩家在此无限飞行，提醒玩家并关闭飞行
                    if (data.status == 0 && data.extra == 0 && !canInfiniteFly(player, loc)) {
                        Messages.time_not_enough__start.tm(player);
                        player.setFlying(false);
                        player.setAllowFlight(false);
                        e.setCancelled(true);
                        return;
                    }
                }
            }
        }
        if (isGameModeCannotFly(player)) {
            updateBossBar(data, standard);
        }
    }

    private boolean isGameModeCannotFly(Player player) {
        GameMode gameMode = player.getGameMode();
        return gameMode.equals(GameMode.SURVIVAL) || gameMode.equals(GameMode.ADVENTURE);
    }

    private void everySecond() {
        FlightDatabase db = plugin.getFlightDatabase();
        GroupManager groups = GroupManager.inst();
        LocalDateTime now = LocalDateTime.now();
        for (Player player : Bukkit.getOnlinePlayers()) {
            boolean flag = isEnabledWorld(player.getWorld());
            switch (worldMode) {
                case HANDLE_ALL:
                    if (!flag) {
                        if (player.isFlying() || player.getAllowFlight()) {
                            Messages.flight__world_not_allow.tm(player);
                            player.setFlying(false);
                            player.setAllowFlight(false);
                        }
                        continue;
                    }
                    break;
                case NOT_HANDLE:
                    if (!flag) {
                        continue;
                    }
                    break;
            }
            // 获取玩家的基础飞行时间
            int standard = groups.getFlightSeconds(player);
            PlayerData data = players.get(player.getUniqueId());
            if (data == null) continue;
            if (now.isAfter(data.outdate)) { // 如果数据到期了，重置基础飞行时间，并提交到数据库
                data.outdate = nextOutdate();
                data.status = Math.max(0, standard);
                db.setPlayerStatus(player, data.status, data.outdate);
            } else {
                // 如果玩家正在飞行
                if (isGameModeCannotFly(player) && player.isFlying()) {
                    boolean update = true;
                    Location loc = player.getLocation();
                    // 如果其它插件禁止玩家飞行
                    if (!canPlayerFlyAt(player, loc)) {
                        update = false;
                        // 关闭飞行，关闭血条
                        player.setFlying(false);
                        player.setAllowFlight(false);
                        if (data.bossBar != null) {
                            data.bossBar.removeAll();
                            data.bossBar = null;
                        }
                        // 如果不是无限飞行时间，且其它插件没有允许玩家进行无限飞行
                    } else if (standard >= 0 && !canInfiniteFly(player, loc)) {
                        // 优先扣除额外飞行时间
                        boolean success = false;
                        for (String order : timeConsumeOrder) {
                            if ("extra".equals(order) && data.extra > 0) {
                                data.extra--;
                                success = true;
                                break;
                            }
                            if ("standard".equals(order) && data.status > 0) {
                                data.status--;
                                success = true;
                                break;
                            }
                        }
                        if (!success) { // 如果时间都不够的话，取消飞行状态
                            update = false;
                            Messages.time_not_enough__timer.tm(player);
                            player.setFlying(false);
                            player.setAllowFlight(false);
                            if (data.bossBar != null) {
                                data.bossBar.removeAll();
                                data.bossBar = null;
                            }
                        }
                    }
                    if (update) updateBossBar(data, standard);
                } else {
                    // 如果玩家没在飞行，就关掉 BOSS 血条
                    if (data.bossBar != null) {
                        data.bossBar.removeAll();
                        data.bossBar = null;
                    }
                }
                // 如果存档计数器时间到了，提交数据到数据库
                if (--data.saveCounter == 0) {
                    data.saveCounter = 60;
                    db.setPlayerStatus(player, data.status, data.outdate);
                }
            }
        }
    }

    private IBarDisplay createBar(String title) {
        if (bossBarDisplayMode.equals(EnumDisplayMode.BOSS_BAR)) {
            return new DisplayBossBar(title, bossBarColor, bossBarStyle);
        }
        if (bossBarDisplayMode.equals(EnumDisplayMode.ACTION_BAR)) {
            return new DisplayActionBar(title);
        }
        return DisplayNone.INSTANCE;
    }

    /**
     * 更新飞行时间 BOSS 血条
     * @param data 玩家数据
     * @param standard 基础飞行时间
     */
    private void updateBossBar(PlayerData data, int standard) {
        int current = data.status + data.extra; // 当前剩余的总飞行时间
        // 更新血条进度
        double progress = standard <= 0 ? 1.0 : Math.min(1.0, Math.max(0.0, (double) current / standard));
        // 更新血条标题
        String format = standard == -1 ? formatInfinite : formatTime(current);
        Component component = AdventureUtil.miniMessage(PAPI.setPlaceholders(data.player, bossBarFlying.replace("%format%", format)));
        String title = LegacyComponentSerializer.legacySection().serialize(component);
        IBarDisplay bar;
        if (data.bossBar == null) {
            bar = data.bossBar = createBar(title);
            bar.setProgress(progress);
            bar.addPlayer(data.player);
        } else {
            bar = data.bossBar;
            bar.setTitle(title);
            bar.setProgress(progress);
        }
    }

    /**
     * 格式化飞行时间
     * @param seconds 总秒数
     * @return 格式化之后的字符串
     */
    public String formatTime(int seconds) {
        int hour = seconds / 3600, minute = seconds / 60 % 60, second = seconds % 60;
        return (hour > 0 ? String.format(hour > 1 ? formatHours : formatHour, hour) : "") +
                (minute > 0 || hour > 0 ? String.format(minute > 1 ? formatMinutes : formatMinute, minute) : "") +
                String.format(second > 1 ? formatSeconds : formatSecond, second);
    }

    /**
     * 格式化飞行时间，如果时间小于0，则显示 无限
     * @param seconds 总秒数
     * @return 格式化之后的字符串
     */
    public String formatTimeMax(int seconds) {
        return seconds < 0 ? formatInfinite : formatTime(seconds);
    }

    public String getFormatInfinite() {
        return formatInfinite;
    }

    public static FlightManager inst() {
        return instanceOf(FlightManager.class);
    }
}
