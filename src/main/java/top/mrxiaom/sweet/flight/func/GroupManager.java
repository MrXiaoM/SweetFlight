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
            if (str.equals("infinite")) {
                timeSecond = -1;
            } else {
                Integer parsed = parseTime(str);
                if (parsed == null) {
                    warn("[groups/" + key + "] 输入的时间格式不正确");
                    continue;
                }
                timeSecond = parsed;
            }
            Group group = new Group(priority, key, timeSecond);
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

    public Group getGroup(Permissible p) {
        for (Group group : groups) {
            if (p.hasPermission("sweet.flight.group." + group.getName())) {
                return group;
            }
        }
        return defaultGroup;
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
