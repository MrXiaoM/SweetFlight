package top.mrxiaom.sweet.flight.func;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.sweet.flight.SweetFlight;
import top.mrxiaom.sweet.flight.func.entry.ByLocale;
import top.mrxiaom.sweet.flight.func.entry.EnumMode;
import top.mrxiaom.sweet.flight.func.entry.Group;

import java.io.File;
import java.util.*;

@AutoRegister
public class GroupManager extends AbstractModule implements Listener {
    private List<Group> groups = new ArrayList<>();
    private Map<String, ByLocale> byLocaleMap = new HashMap<>();
    private Group defaultGroup;
    public GroupManager(SweetFlight plugin) {
        super(plugin);
        registerEvents();
    }

    @Override
    public int priority() {
        return 999;
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        File file = plugin.resolve("./groups.yml");
        if (!file.exists()) {
            plugin.saveResource("groups.yml");
        }
        reload(file, 0);
    }

    public void reload(File file, int recursion) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        String gotoPath = config.getString("goto");
        if (gotoPath != null && recursion < 100) {
            reload(plugin.resolve(gotoPath), recursion + 1);
            return;
        }
        defaultGroup = null;
        groups.clear();
        ConfigurationSection section;

        section = config.getConfigurationSection("groups");
        if (section != null) for (String key : section.getKeys(false)) {
            int priority = section.getInt(key + ".priority", 1000);
            Pair<EnumMode, Integer> pair = parseTimeMode(section.getString(key + ".time", ""));
            if (pair == null) {
                warn("[groups/" + key + "] 输入的时间格式不正确");
                continue;
            }
            EnumMode mode = pair.key();
            int timeSecond = pair.value();
            Group group = new Group(priority, key, timeSecond, mode);
            groups.add(group);
            if (key.equals("default")) {
                defaultGroup = group;
            }
        }
        groups.sort(Comparator.comparingInt(Group::priority));
        if (defaultGroup == null) {
            warn("[groups] 找不到默认组 default，可能会出现不可预料的问题");
        }

        byLocaleMap.clear();
        section = config.getConfigurationSection("by-locale");
        if (section != null) for (String key : section.getKeys(false)) {
            if (!section.getBoolean(key + ".enable", false)) {
                continue;
            }
            Pair<EnumMode, Integer> pair = parseTimeMode(section.getString(key + ".time", ""));
            if (pair == null) {
                warn("[by-locale/" + key + "] 输入的时间格式不正确");
                continue;
            }
            String id = key.toLowerCase();
            EnumMode mode = pair.key();
            int timeSecond = pair.value();
            ByLocale locale = new ByLocale(id, timeSecond, mode);
            byLocaleMap.put(id, locale);
        }
    }

    @Nullable
    private static Pair<EnumMode, Integer> parseTimeMode(@NotNull String str) {
        EnumMode mode;
        if (str.equals("infinite")) {
            return Pair.of(EnumMode.SET, -1);
        } else {
            boolean plus = str.startsWith("+");
            Integer parsed = parseTime(plus ? str.substring(1) : str);
            if (parsed == null) {
                return null;
            }
            return Pair.of(plus ? EnumMode.ADD : EnumMode.SET, parsed);
        }
    }

    /**
     * 获取玩家的所有飞行组
     * @return 按优先级倒序排序
     */
    @NotNull
    public List<Group> getGroups(Permissible p) {
        List<Group> list = new ArrayList<>();
        for (Group group : groups) { // 按优先级匹配组
            if (p.hasPermission("sweet.flight.group." + group.getName())) {
                list.add(group);
            }
        }
        Collections.reverse(list); // 反转匹配到的组
        return list;
    }

    @Nullable
    public ByLocale getLocale(String locale) {
        return byLocaleMap.get(locale.toLowerCase().replace("-", "_"));
    }

    @Nullable
    public ByLocale getLocale(Player player) {
        return getLocale(player.getLocale());
    }

    public int getFlightSeconds(Permissible p) {
        int seconds = 0;
        List<Group> list = getGroups(p);
        for (Group group : list) {
            if (group.getTimeMode().equals(EnumMode.ADD)) {
                seconds += group.getTimeSecond();
            }
            if (group.getTimeMode().equals(EnumMode.SET)) {
                seconds = group.getTimeSecond();
            }
            if (seconds == -1) {
                break;
            }
        }
        if (p instanceof Player && seconds != -1) {
            ByLocale locale = getLocale((Player) p);
            if (locale != null) {
                if (locale.getTimeMode().equals(EnumMode.ADD)) {
                    seconds += locale.getTimeSecond();
                }
                if (locale.getTimeMode().equals(EnumMode.SET)) {
                    seconds = locale.getTimeSecond();
                }
            }
        }
        return seconds;
    }

    public static GroupManager inst() {
        return instanceOf(GroupManager.class);
    }

    public static Integer parseTime(String str) {
        Integer parsed = 0;
        String numberBuffer = "";
        for (char c : str.toCharArray()) {
            if (c == '0' || c == '1' || c == '2'
                    || c == '3' || c == '4' || c == '5'
                    || c == '6' || c == '7' || c == '8' || c == '9') {
                numberBuffer += c;
                continue;
            }
            if (c == 'h') {
                int hours = Integer.parseInt(numberBuffer);
                numberBuffer = "";
                parsed += hours * 3600;
                continue;
            }
            if (c == 'm') {
                int minutes = Integer.parseInt(numberBuffer);
                numberBuffer = "";
                parsed += minutes * 60;
                continue;
            }
            if (c == 's') {
                int seconds = Integer.parseInt(numberBuffer);
                numberBuffer = "";
                parsed += seconds;
                continue;
            }
            parsed = null;
            break;
        }
        return parsed;
    }
}
