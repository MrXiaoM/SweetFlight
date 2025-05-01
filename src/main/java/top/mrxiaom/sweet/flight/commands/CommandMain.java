package top.mrxiaom.sweet.flight.commands;
        
import com.google.common.collect.Lists;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.flight.SweetFlight;
import top.mrxiaom.sweet.flight.func.AbstractModule;
import top.mrxiaom.sweet.flight.func.FlightManager;
import top.mrxiaom.sweet.flight.func.entry.PlayerData;

import java.util.*;

@AutoRegister
public class CommandMain extends AbstractModule implements CommandExecutor, TabCompleter, Listener {
    public CommandMain(SweetFlight plugin) {
        super(plugin);
        registerCommand("sweetflight", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 3 && "set".equalsIgnoreCase(args[0]) && sender.isOp()) {
            Player player = Util.getOnlinePlayer(args[1]).orElse(null);
            if (player == null) {
                return t(sender, "&e玩家不存在 (或不在线)");
            }
            FlightManager manager = FlightManager.inst();
            PlayerData data = manager.get(player);
            if (data == null) {
                return t(sender, "&e玩家数据异常");
            }
            int value = Util.parseInt(args[2]).orElse(-1);
            if (value < 0) {
                return t(sender, "&e请输入大于等于0的整数");
            }
            data.extra = value;
            plugin.getFlightDatabase().setPlayerExtra(player, data.extra);
            return t(sender, "&a已设置玩家&e " + player.getName() + " &a的额外飞行时间为&e " + manager.formatTime(value));
        }
        if (args.length == 3 && "add".equalsIgnoreCase(args[0]) && sender.isOp()) {
            Player player = Util.getOnlinePlayer(args[1]).orElse(null);
            if (player == null) {
                return t(sender, "&e玩家不存在 (或不在线)");
            }
            FlightManager manager = FlightManager.inst();
            PlayerData data = manager.get(player);
            if (data == null) {
                return t(sender, "&e玩家数据异常");
            }
            int value = Util.parseInt(args[2]).orElse(0);
            if (value <= 0) {
                return t(sender, "&e请输入大于0的整数");
            }
            data.extra += value;
            plugin.getFlightDatabase().setPlayerExtra(player, data.extra);
            return t(sender, "&a已为玩家&e " + player.getName() + " &a增加&e " + manager.formatTime(value) + " &a的额外飞行时间，增加后为&e " + manager.formatTime(data.extra));
        }
        if (args.length >= 1 && "reload".equalsIgnoreCase(args[0]) && sender.isOp()) {
            if (args.length == 2 && "database".equalsIgnoreCase(args[1])) {
                plugin.options.database().reloadConfig();
                plugin.options.database().reconnect();
                return t(sender, "&a已重新连接数据库");
            }
            plugin.reloadConfig();
            return t(sender, "&a配置文件已重载");
        }
        return true;
    }

    private static final List<String> emptyList = Lists.newArrayList();
    private static final List<String> listArg0 = Lists.newArrayList();
    private static final List<String> listOpArg0 = Lists.newArrayList(
            "set", "add", "reload");
    private static final List<String> listArg1Reload = Lists.newArrayList("database");
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return startsWith(sender.isOp() ? listOpArg0 : listArg0, args[0]);
        }
        if (args.length == 2) {
            if (sender.isOp()) {
                if ("set".equalsIgnoreCase(args[0]) || "add".equalsIgnoreCase(args[0])) {
                    return null;
                }
                if ("reload".equalsIgnoreCase(args[0])) {
                    return startsWith(listArg1Reload, args[1]);
                }
            }
        }
        return emptyList;
    }

    public List<String> startsWith(Collection<String> list, String s) {
        return startsWith(null, list, s);
    }
    public List<String> startsWith(String[] addition, Collection<String> list, String s) {
        String s1 = s.toLowerCase();
        List<String> stringList = new ArrayList<>(list);
        if (addition != null) stringList.addAll(0, Lists.newArrayList(addition));
        stringList.removeIf(it -> !it.toLowerCase().startsWith(s1));
        return stringList;
    }
}
