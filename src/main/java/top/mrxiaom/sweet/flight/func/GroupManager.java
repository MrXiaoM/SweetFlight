package top.mrxiaom.sweet.flight.func;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permissible;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.sweet.flight.SweetFlight;
import top.mrxiaom.sweet.flight.func.entry.Group;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@AutoRegister
public class GroupManager extends AbstractModule implements Listener {
    private List<Group> groups = new ArrayList<>();
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
        ConfigurationSection section = config.getConfigurationSection("groups");
        if (section != null) for (String key : section.getKeys(false)) {
            int priority = section.getInt(key + ".priority", 1000);
            int timeSecond = 0;
            String str = section.getString(key + ".time", "");
            Group.Mode mode;
            if (str.equals("infinite")) {
                timeSecond = -1;
                mode = Group.Mode.SET;
            } else {
                boolean plus = str.startsWith("+");
                Integer parsed = parseTime(plus ? str.substring(1) : str);
                if (parsed == null) {
                    warn("[groups/" + key + "] 输入的时间格式不正确");
                    continue;
                }
                timeSecond = parsed;
                mode = plus ? Group.Mode.ADD : Group.Mode.SET;
            }
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
    }

    /**
     * 获取玩家的所有飞行组
     * @return 按优先级倒序排序
     */
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

    public int getFlightSeconds(Permissible p) {
        int seconds = 0;
        List<Group> list = getGroups(p);
        for (Group group : list) {
            if (group.getMode().equals(Group.Mode.ADD)) {
                seconds += group.getTimeSecond();
            }
            if (group.getMode().equals(Group.Mode.SET)) {
                seconds = group.getTimeSecond();
            }
            if (seconds == -1) {
                break;
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
