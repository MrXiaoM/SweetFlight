package top.mrxiaom.sweet.flight.func;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.ColorHelper;
import top.mrxiaom.sweet.flight.Messages;
import top.mrxiaom.sweet.flight.SweetFlight;
import top.mrxiaom.sweet.flight.database.FlightDatabase;
import top.mrxiaom.sweet.flight.func.entry.Group;
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
    private String bossBarFlying;
    private String formatHour, formatHours, formatMinute, formatMinutes, formatSecond, formatSeconds, formatInfinite;
    private List<Player> toLoad = new ArrayList<>();
    public FlightManager(SweetFlight plugin) {
        super(plugin);
        toLoad.addAll(Bukkit.getOnlinePlayers());
        registerEvents();
        plugin.getScheduler().runTaskTimer(this::everySecond, 20L, 20L);
    }

    public PlayerData get(Player player) {
        return players.get(player.getUniqueId());
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
        this.bossBarFlying = config.getString("boss-bar.flying", "");
        this.formatHour = config.getString("time-format.hour", "%d时");
        this.formatHours = config.getString("time-format.hours", "%d时");
        this.formatMinutes = config.getString("time-format.minute", "%d分");
        this.formatMinutes = config.getString("time-format.minutes", "%d分");
        this.formatSecond = config.getString("time-format.second", "%d秒");
        this.formatSeconds = config.getString("time-format.seconds", "%d秒");
        this.formatInfinite = config.getString("time-format.infinite", "无限");
        for (Player player : toLoad) {
            onJoin(player);
        }
        toLoad.clear();
    }

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

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        plugin.getScheduler().runTask(() -> onJoin(player));
    }
    public void onJoin(Player player) {
        Group group = GroupManager.inst().getGroup(player);
        UUID uuid = player.getUniqueId();
        String id = plugin.key(player);
        int status, extra;
        LocalDateTime nextOutdate = nextOutdate();
        try (Connection conn = plugin.getConnection()) {
            FlightDatabase db = plugin.getFlightDatabase();
            Integer statusRaw = db.getPlayerStatus(conn, id);
            status = statusRaw == null ? group.getTimeSecond() : statusRaw;
            extra = db.getPlayerExtra(conn, id);
            players.put(uuid, new PlayerData(player, status, extra, nextOutdate));
        } catch (SQLException ex) {
            warn(ex);
            status = extra = 0;
        }
        if (!player.hasPermission("sweet.flight.bypass")) {
            if (group.getTimeSecond() == -1) {
                player.setAllowFlight(true);
            } else {
                if (extra == 0 && status == 0) {
                    Messages.time_not_enough__join.tm(player);
                    player.setFlying(false);
                    player.setAllowFlight(false);
                } else {
                    player.setAllowFlight(true);
                }
            }
        } else {
            player.setAllowFlight(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        PlayerData data = players.remove(e.getPlayer().getUniqueId());
        if (data != null) {
            if (data.bossBar != null) {
                data.bossBar.removeAll();
                data.bossBar = null;
            }
            FlightDatabase db = plugin.getFlightDatabase();
            db.setPlayer(data.player, data.status, data.extra, data.outdate);
        }
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent e) {
        Player player = e.getPlayer();
        PlayerData data = players.get(player.getUniqueId());
        if (!e.isFlying()) {
            if (data != null && data.bossBar != null) {
                data.bossBar.removeAll();
                data.bossBar = null;
            }
            return;
        }
        if (player.hasPermission("sweet.flight.bypass")) return;
        if (data == null) {
            Messages.player__data_invalid_start.tm(player);
            e.setCancelled(true);
            return;
        }
        if (data.status == 0 && data.extra == 0) {
            Messages.time_not_enough__start.tm(player);
            player.setFlying(false);
            player.setAllowFlight(false);
            e.setCancelled(true);
        } else {
            updateBossBar(data);
        }
    }

    private void everySecond() {
        FlightDatabase db = plugin.getFlightDatabase();
        GroupManager groups = GroupManager.inst();
        LocalDateTime now = LocalDateTime.now();
        for (Player player : Bukkit.getOnlinePlayers()) {
            Group group = groups.getGroup(player);
            int status = group.getTimeSecond();
            PlayerData data = players.get(player.getUniqueId());
            if (data == null) continue;
            if (now.isAfter(data.outdate)) {
                data.outdate = nextOutdate();
                data.status = Math.max(0, status);
                db.setPlayerStatus(player, data.status, data.outdate);
            } else {
                if (player.isFlying()) {
                    boolean update = true;
                    if (status >= 0) {
                        if (data.extra > 0) data.extra--;
                        else if (data.status > 0) data.status--;
                        else {
                            update = false;
                            Messages.time_not_enough__timer.tm(player);
                            player.setFlying(false);
                            player.setAllowFlight(false);
                            data.bossBar.removeAll();
                            data.bossBar = null;
                        }
                    }
                    if (update) updateBossBar(data);
                } else {
                    if (data.bossBar != null) {
                        data.bossBar.removeAll();
                        data.bossBar = null;
                    }
                }
                if (--data.saveCounter == 0) {
                    data.saveCounter = 60;
                    db.setPlayerStatus(player, data.status, data.outdate);
                }
            }
        }
    }

    private void updateBossBar(PlayerData data) {
        Group group = GroupManager.inst().getGroup(data.player);
        int current = data.status + data.extra;
        int status = group.getTimeSecond();
        double progress = status <= 0 ? 1.0 : Math.min(1.0, (double) current / status);
        String format = status == -1 ? formatInfinite : formatTime(current);
        String title = ColorHelper.parseColor(bossBarFlying.replace("%format%", format));
        BossBar bar;
        if (data.bossBar == null) {
            data.bossBar = Bukkit.createBossBar(title, BarColor.BLUE, BarStyle.SEGMENTED_10);
            data.bossBar.setProgress(progress);
            data.bossBar.addPlayer(data.player);
        } else {
            bar = data.bossBar;
            bar.setTitle(title);
            bar.setProgress(progress);
        }
    }

    public String formatTime(int seconds) {
        int hour = seconds / 3600, minute = seconds / 60 % 60, second = seconds % 60;
        return (hour > 0 ? String.format(hour > 1 ? formatHours : formatHour, hour) : "") +
                (minute > 0 || hour > 0 ? String.format(minute > 1 ? formatMinutes : formatMinute, minute) : "") +
                String.format(second > 1 ? formatSeconds : formatSecond, second);
    }

    public static FlightManager inst() {
        return instanceOf(FlightManager.class);
    }
}
