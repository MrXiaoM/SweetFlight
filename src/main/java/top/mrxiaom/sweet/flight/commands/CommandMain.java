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
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.flight.Messages;
import top.mrxiaom.sweet.flight.SweetFlight;
import top.mrxiaom.sweet.flight.func.AbstractModule;
import top.mrxiaom.sweet.flight.func.FlightManager;
import top.mrxiaom.sweet.flight.func.GroupManager;
import top.mrxiaom.sweet.flight.func.entry.Group;
import top.mrxiaom.sweet.flight.func.entry.PlayerData;

import java.util.*;

@AutoRegister
public class CommandMain extends AbstractModule implements CommandExecutor, TabCompleter, Listener {
    public CommandMain(SweetFlight plugin) {
        super(plugin);
        registerCommand("sweetflight", this);
    }

    private void postSetFlightTime(PlayerData data) {
        Player player = data.player;
        int standard = GroupManager.inst().getFlightSeconds(player);
        if (!player.hasPermission("sweet.flight.bypass")) {
            if (standard == -1) {
                player.setAllowFlight(true);
            } else {
                if (data.extra == 0 && data.status == 0) {
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

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length >= 1 && "on".equalsIgnoreCase(args[0])) {
            Player target;
            if (args.length == 2) {
                if (!sender.hasPermission("sweet.flight.toggle.other")) {
                    return Messages.no_permission.tm(sender);
                }
                target = Util.getOnlinePlayer(args[1]).orElse(null);
                if (target == null) {
                    return Messages.player__not_online.tm(sender);
                }
            } else {
                if (sender instanceof Player) {
                    target = (Player) sender;
                } else {
                    return Messages.player__only.tm(sender);
                }
            }
            FlightManager manager = FlightManager.inst();
            int standard = GroupManager.inst().getFlightSeconds(target);
            if (standard >= 0) {
                PlayerData data = manager.get(target);
                if (data == null) {
                    return Messages.player__data_not_found.tm(sender);
                }
                if (data.extra == 0 && data.status == 0) {
                    target.setFlying(false);
                    target.setAllowFlight(false);
                    return Messages.time_not_enough__command.tm(sender);
                }
            }
            target.setAllowFlight(true);
            target.setFlying(true);
            return Messages.command__on__success.tm(sender);
        }
        if (args.length >= 1 && "off".equalsIgnoreCase(args[0])) {
            Player target;
            if (args.length == 2) {
                if (!sender.hasPermission("sweet.flight.toggle.other")) {
                    return Messages.no_permission.tm(sender);
                }
                target = Util.getOnlinePlayer(args[1]).orElse(null);
                if (target == null) {
                    return Messages.player__not_online.tm(sender);
                }
            } else {
                if (sender instanceof Player) {
                    target = (Player) sender;
                } else {
                    return Messages.player__only.tm(sender);
                }
            }
            target.setFlying(false);
            FlightManager manager = FlightManager.inst();
            PlayerData data = manager.get(target);
            if (data == null) {
                return Messages.player__data_not_found.tm(sender);
            }
            if (data.extra == 0 && data.status == 0) {
                target.setAllowFlight(false);
            }
            return Messages.command__off__success.tm(sender);
        }
        if (args.length >= 1 && "check".equalsIgnoreCase(args[0]) && sender.hasPermission("sweet.flight.check")) {
            Player target;
            if (args.length == 2) {
                if (!sender.hasPermission("sweet.flight.check.other")) {
                    return Messages.no_permission.tm(sender);
                }
                target = Util.getOnlinePlayer(args[1]).orElse(null);
                if (target == null) {
                    return Messages.player__not_online.tm(sender);
                }
            } else {
                if (sender instanceof Player) {
                    target = (Player) sender;
                } else {
                    return Messages.player__only.tm(sender);
                }
            }
            if (target.equals(sender)) {
                Messages.command__check__header_yourself.tm(sender);
            } else {
                Messages.command__check__header_other.tm(sender,
                        Pair.of("%player%", target.getName()));
            }
            FlightManager flight = FlightManager.inst();
            GroupManager manager = GroupManager.inst();
            PlayerData data = flight.get(target);
            if (data == null) {
                return Messages.player__data_not_found.tm(sender);
            }
            List<Group> groups = manager.getGroups(target);
            int standard = 0;
            int status = data.status;
            int extra = data.extra;
            for (Group group : groups) {
                if (group.getMode().equals(Group.Mode.ADD)) {
                    int value = group.getTimeSecond();
                    Messages.command__check__group_add.tm(sender,
                            Pair.of("%group%", group.getName()),
                            Pair.of("%time%", flight.formatTime(value)));
                    standard += value;
                }
                if (group.getMode().equals(Group.Mode.SET)) {
                    int value = group.getTimeSecond();
                    if (value >= 0) {
                        Messages.command__check__group_set.tm(sender,
                                Pair.of("%group%", group.getName()),
                                Pair.of("%time%", flight.formatTime(value)));
                    }
                    standard = group.getTimeSecond();
                }
                if (standard == -1) {
                    Messages.command__check__group_infinite.tm(sender,
                            Pair.of("%group%", group.getName()));
                    break;
                }
            }
            Messages.command__check__standard.tm(sender, Pair.of("%time%", flight.formatTimeMax(standard)));
            Messages.command__check__remaining.tm(sender, Pair.of("%time%", flight.formatTime(status + extra)));
            Messages.command__check__remaining_status.tm(sender, Pair.of("%time%", flight.formatTime(status)));
            Messages.command__check__remaining_extra.tm(sender, Pair.of("%time%", flight.formatTime(extra)));
            return true;
        }
        if (args.length == 3 && "set".equalsIgnoreCase(args[0]) && sender.isOp()) {
            Player player = Util.getOnlinePlayer(args[1]).orElse(null);
            if (player == null) {
                return Messages.player__not_online.tm(sender);
            }
            FlightManager manager = FlightManager.inst();
            PlayerData data = manager.get(player);
            if (data == null) {
                return Messages.player__data_not_found.tm(sender);
            }
            Integer value = GroupManager.parseTime(args[2]);
            if (value == null || value < 0) {
                return Messages.command__set__not_time.tm(sender);
            }
            data.extra = value;
            plugin.getFlightDatabase().setPlayerExtra(player, data.extra);
            postSetFlightTime(data);
            return Messages.command__set__success.tm(sender,
                    Pair.of("%player%", player.getName()),
                    Pair.of("%time%", manager.formatTime(data.extra)));
        }
        if (args.length == 3 && "add".equalsIgnoreCase(args[0]) && sender.isOp()) {
            Player player = Util.getOnlinePlayer(args[1]).orElse(null);
            if (player == null) {
                return Messages.player__not_online.tm(sender);
            }
            FlightManager manager = FlightManager.inst();
            PlayerData data = manager.get(player);
            if (data == null) {
                return Messages.player__data_not_found.tm(sender);
            }
            Integer value = GroupManager.parseTime(args[2]);
            if (value == null || value <= 0) {
                return Messages.command__add__not_time.tm(sender);
            }
            data.extra += value;
            plugin.getFlightDatabase().setPlayerExtra(player, data.extra);
            postSetFlightTime(data);
            return Messages.command__add__success.tm(sender,
                    Pair.of("%player%", player.getName()),
                    Pair.of("%added%", manager.formatTime(value)),
                    Pair.of("%time%", manager.formatTime(data.extra)));
        }
        if (args.length == 2 && "reset".equalsIgnoreCase(args[0]) && sender.isOp()) {
            Player player = Util.getOnlinePlayer(args[1]).orElse(null);
            if (player == null) {
                return Messages.player__not_online.tm(sender);
            }
            FlightManager manager = FlightManager.inst();
            GroupManager groups = GroupManager.inst();
            PlayerData data = manager.getOrCreate(player);
            int standard = groups.getFlightSeconds(player);
            data.status = Math.max(0, standard);
            data.outdate = manager.nextOutdate();
            plugin.getFlightDatabase().setPlayer(data);
            postSetFlightTime(data);
            return Messages.command__reset__success.tm(sender,
                    Pair.of("%player%", player.getName()),
                    Pair.of("%time%", manager.formatTime(data.status)));
        }
        if (args.length >= 1 && "reload".equalsIgnoreCase(args[0]) && sender.isOp()) {
            if (args.length == 2 && "database".equalsIgnoreCase(args[1])) {
                plugin.options.database().reloadConfig();
                plugin.options.database().reconnect();
                return Messages.command__reload__database.tm(sender);
            }
            plugin.reloadConfig();
            return Messages.command__reload__config.tm(sender);
        }
        return (sender.isOp() ? Messages.help__admin : Messages.help__normal).tm(sender);
    }

    private static final List<String> emptyList = Lists.newArrayList();
    private static final List<String> listArg0 = Lists.newArrayList(
            "on", "off", "check");
    private static final List<String> listOpArg0 = Lists.newArrayList(
            "set", "add", "check", "reset", "reload");
    private static final List<String> listArg1Reload = Lists.newArrayList("database");
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return startsWith(sender.isOp() ? listOpArg0 : listArg0, args[0]);
        }
        if (args.length == 2) {
            if (sender.isOp()) {
                if ("set".equalsIgnoreCase(args[0]) || "add".equalsIgnoreCase(args[0])
                || "reset".equalsIgnoreCase(args[0]) || "check".equalsIgnoreCase(args[0])
                || "on".equalsIgnoreCase(args[0]) || "off".equalsIgnoreCase(args[0])) {
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
